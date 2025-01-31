package net.hellz.clerk

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.hellz.clerk.Rank.Companion.getAllPermissions
import net.hellz.utils.LettuceConnection
import net.hellz.util.StreamConnection
import net.minestom.server.entity.Player
import org.bson.Document
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class Profile private constructor(val uuid: UUID, val username: String) {
    val permissions: MutableSet<String> = mutableSetOf()
    var rank: Rank = Rank.defaultRank


    companion object {
        val profiles = ConcurrentHashMap<UUID, Profile>()
        private val gson = Gson()
        private const val CACHE_TTL = 600 // Cache time in seconds (10 minutes)

        suspend fun retrieve(player: Player) {
            retrieve(player.uuid.toString(), player.username)
        }

        suspend fun retrieve(playerUuid: String, playerName: String? = null) {
            val uuid = UUID.fromString(playerUuid)
            val cacheKey = "profile:$playerUuid"

            // Try to retrieve from Redis first
            val cachedProfile = LettuceConnection.get(cacheKey)
            if (cachedProfile != null) {
                val profileData = gson.fromJson(cachedProfile, ProfileData::class.java)
                profiles[uuid] = Profile(uuid, profileData.username).apply {
                    permissions.addAll(profileData.permissions)
                    rank = profileData.rank?.let { Rank.getRankByName(it) } ?: Rank.defaultRank
                }
                println("[clerk] Loaded profile for ${profileData.username} from Redis cache.")
                return
            }

            // If not found in Redis, retrieve from MongoDB
            val filter = Document("_id", playerUuid)
            val doc = withContext(Dispatchers.IO) {
                StreamConnection.readAsync("profiles", filter)
            }

            if (doc == null) {
                playerName?.let { create(playerUuid, it) }
            } else {
                val profile = Profile(uuid, doc.getString("username")).apply {
                    permissions.addAll(doc.getList("permissions", String::class.java))
                    rank = doc.getString("rank")?.let { Rank.getRankByName(it) } ?: Rank.defaultRank
                }
                profiles[uuid] = profile

                // Cache the profile in Redis
                LettuceConnection.setex(cacheKey, gson.toJson(ProfileData(profile)), CACHE_TTL.toLong())
                println("[clerk] Cached profile for ${profile.username} in Redis.")
            }
        }

        private suspend fun create(playerUuid: String, username: String) {
            val uuid = UUID.fromString(playerUuid)
            val doc = Document("_id", playerUuid)
                .append("username", username)
                .append("permissions", emptyList<String>())
                .append("rank", Rank.defaultRank.displayName)

            withContext(Dispatchers.IO) {
                StreamConnection.writeAsync("profiles", doc)
            }
            val profile = Profile(uuid, username)
            profiles[uuid] = profile

            // Cache new profile in Redis
            val cacheKey = "profile:$playerUuid"
            LettuceConnection.setex(cacheKey, gson.toJson(ProfileData(profile)), CACHE_TTL.toLong())

            println("[clerk] Successfully created and cached a new profile for $username.")
        }

        suspend fun addPermission(playerUuid: String, permission: String) {
            val uuid = UUID.fromString(playerUuid)
            val profile = profiles[uuid] ?: return
            if (profile.permissions.add(permission)) {
                updateProfile(profile)
                println("[clerk] Successfully granted ($permission) permission to ${profile.username}")
            }
        }

        suspend fun removePermission(playerUuid: String, permission: String) {
            val uuid = UUID.fromString(playerUuid)
            val profile = profiles[uuid] ?: return
            if (profile.permissions.remove(permission)) {
                updateProfile(profile)
                println("[clerk] Successfully removed ($permission) permission from ${profile.username}")
            }
        }

        suspend fun hasPermission(playerUuid: String, permission: String): Boolean {
            val uuid = UUID.fromString(playerUuid)
            val profile = profiles[uuid] ?: return false

            // Check for exact permission in player's personal permissions
            if (profile.permissions.contains(permission)) return true

            // Check for wildcard permissions in player's personal permissions
            val permissionParts = permission.split(".")
            for (i in permissionParts.indices) {
                val wildcardPermission = permissionParts.subList(0, i).joinToString(".") + ".*"
                if (profile.permissions.contains(wildcardPermission)) return true
            }

            // Check if player's rank has the permission
            val rankPermissions = getAllPermissions(profile.rank)
            if (rankPermissions.contains(permission)) return true

            // Check for wildcard permissions in rank's permissions
            for (i in permissionParts.indices) {
                val wildcardPermission = permissionParts.subList(0, i).joinToString(".") + ".*"
                if (rankPermissions.contains(wildcardPermission)) return true
            }

            return false
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

        suspend fun setRank(playerUuid: String, rank: Rank) {
            val uuid = UUID.fromString(playerUuid)
            val profile = profiles[uuid] ?: return
            profile.rank = rank
            updateProfile(profile)
            println("[clerk] Successfully set rank '${rank.displayName}' for ${profile.username}")
        }
    }

    // Helper class to serialize/deserialize profile data
    data class ProfileData(val username: String, val permissions: List<String>, val rank: String) {
        constructor(profile: Profile) : this(profile.username, profile.permissions.toList(), profile.rank.displayName)
    }
}
