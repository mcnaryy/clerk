package net.hellz.clerk

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.hellz.utils.LettuceConnection
import net.hellz.util.StreamConnection
import net.minestom.server.entity.Player
import org.bson.Document
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class Profile private constructor(val player: Player, val username: String) {
    val uuid: UUID = player.uuid
    val permissions: MutableSet<String> = mutableSetOf()
    var rank: Rank = Rank.defaultRank

    companion object {
        val profiles = ConcurrentHashMap<UUID, Profile>()
        private val gson = Gson()
        private const val CACHE_TTL = 600 // Cache time in seconds (10 minutes)

        suspend fun retrieve(player: Player) {
            val uuid = player.uuid
            val cacheKey = "profile:$uuid"

            // Try to retrieve from Redis first
            val cachedProfile = LettuceConnection.get(cacheKey)
            if (cachedProfile != null) {
                val profileData = gson.fromJson(cachedProfile, ProfileData::class.java)
                profiles[uuid] = Profile(player, profileData.username).apply {
                    permissions.addAll(profileData.permissions)
                    rank = profileData.rank?.let { Rank.getRankByName(it) } ?: Rank.defaultRank
                }
                println("[clerk] Loaded profile for ${player.username} from Redis cache.")
                return
            }

            // If not found in Redis, retrieve from MongoDB
            val filter = Document("_id", uuid.toString())
            val doc = withContext(Dispatchers.IO) {
                StreamConnection.readAsync("profiles", filter)
            }

            if (doc == null) {
                create(player)
            } else {
                val profile = Profile(player, doc.getString("username")).apply {
                    permissions.addAll(doc.getList("permissions", String::class.java))
                    rank = doc.getString("rank")?.let { Rank.getRankByName(it) } ?: Rank.defaultRank
                }
                profiles[uuid] = profile

                // Cache the profile in Redis
                LettuceConnection.setex(cacheKey, gson.toJson(ProfileData(profile)), CACHE_TTL.toLong())
                println("[clerk] Cached profile for ${player.username} in Redis.")
            }
        }

        private suspend fun create(player: Player) {
            val uuid = player.uuid
            val username = player.username
            val doc = Document("_id", uuid.toString())
                .append("username", username)
                .append("permissions", emptyList<String>())
                .append("rank", Rank.defaultRank.displayName)

            withContext(Dispatchers.IO) {
                StreamConnection.writeAsync("profiles", doc)
            }
            val profile = Profile(player, username)
            profiles[uuid] = profile

            // Cache new profile in Redis
            val cacheKey = "profile:$uuid"
            LettuceConnection.setex(cacheKey, gson.toJson(ProfileData(profile)), CACHE_TTL.toLong())

            println("[clerk] Successfully created and cached a new profile for ${player.username}.")
        }

        suspend fun addPermission(player: Player, permission: String) {
            val profile = profiles[player.uuid] ?: return
            if (profile.permissions.add(permission)) {
                updateProfile(profile)
                println("[clerk] Successfully granted ($permission) permission to ${player.username}")
            }
        }

        suspend fun removePermission(player: Player, permission: String) {
            val profile = profiles[player.uuid] ?: return
            if (profile.permissions.remove(permission)) {
                updateProfile(profile)
                println("[clerk] Successfully removed ($permission) permission from ${player.username}")
            }
        }

        suspend fun hasPermission(player: Player, permission: String): Boolean {
            return profiles[player.uuid]?.permissions?.contains(permission) ?: false
        }

        private suspend fun updateProfile(profile: Profile) {
            val filter = Document("_id", profile.uuid.toString())
            val updatedDoc = Document("\$set", Document()
                .append("username", profile.username)
                .append("permissions", profile.permissions.toList())
                .append("rank", profile.rank.displayName)
            )

            withContext(Dispatchers.IO) {
                StreamConnection.updateAsync("profiles", filter, updatedDoc)
            }

            // Update cache in Redis
            val cacheKey = "profile:${profile.uuid}"
            LettuceConnection.setex(cacheKey, gson.toJson(ProfileData(profile)), CACHE_TTL.toLong())
        }

        suspend fun setRank(player: Player, rank: Rank) {
            val profile = profiles[player.uuid] ?: return
            profile.rank = rank
            updateProfile(profile)
            println("[clerk] Successfully set rank '${rank.displayName}' for ${player.username}")
        }
    }

    // Helper class to serialize/deserialize profile data
    data class ProfileData(val username: String, val permissions: List<String>, val rank: String) {
        constructor(profile: Profile) : this(profile.username, profile.permissions.toList(), profile.rank.displayName)
    }
}