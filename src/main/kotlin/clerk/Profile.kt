package net.hellz.clerk

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.hellz.util.StreamConnection
import net.minestom.server.entity.Player
import org.bson.Document
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class Profile private constructor(val player: Player, val username: String) {
    val uuid: UUID = player.uuid
    private val permissions: MutableSet<String> = mutableSetOf()

    companion object {
        private val profiles = ConcurrentHashMap<UUID, Profile>()

        suspend fun retrieve(player: Player) {
            val uuid = player.uuid
            val filter = Document("_id", uuid.toString())

            val doc = withContext(Dispatchers.IO) {
                StreamConnection.readAsync("profiles", filter)
            }

            if (doc == null) {
                create(player)
            } else {
                profiles[uuid] = Profile(player, doc.getString("username")).apply {
                    permissions.addAll(doc.getList("permissions", String::class.java))
                }
            }
        }

        private suspend fun create(player: Player) {
            val uuid = player.uuid
            val username = player.username
            val doc = Document("_id", uuid.toString())
                .append("username", username)
                .append("permissions", emptyList<String>())

            withContext(Dispatchers.IO) {
                StreamConnection.writeAsync("profiles", doc)
            }
            profiles[uuid] = Profile(player, username)
            println("[clerk] Successfully created a new profile for ${player.username}.")
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
            val updatedDoc = Document()
                .append("username", profile.username)
                .append("permissions", profile.permissions.toList())

            withContext(Dispatchers.IO) {
                StreamConnection.updateAsync("profiles", filter, updatedDoc)
            }
        }
    }
}