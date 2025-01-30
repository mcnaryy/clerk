package net.hellz.clerk

import net.hellz.util.StreamConnection
import net.minestom.server.entity.Player
import org.bson.Document
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.stream.Stream

// Creates a profile for every player using their uuid
class Profile private constructor(val player: Player, val username: String){
    val uuid: UUID = player.uuid
    private val permissions: MutableSet<String> = mutableSetOf()

    // Functions for the Profile class (What to do with a profile)
    companion object {
        private val profiles = mutableMapOf<UUID, Profile>()

        // Check to see if a player even has a profile
        fun retrieve(player: Player){
            val uuid = player.uuid
            val filter = Document("_id", uuid.toString()) // Filter is what documents we are looking for
            val latch = CountDownLatch(1) // Timer to stop searching through the collection
            var found = false // Determine if a profile was found or not

            // Search for the players uuid in the profiles collection
            StreamConnection().read("profiles", filter) { doc ->
                found = doc != null
                latch.countDown()
            }

            // If there is no profile belonging the player, we will create one for them
            if (!latch.await(1, TimeUnit.SECONDS)) {
                println("[clerk] The player ${player.username} does not have a profile in the database.")
                create(player)
                return
            }
            if (!found){
                create(player)
            }

        }

        // Creating an empty profile for the player
        private fun create(player: Player){
            val uuid = player.uuid
            val username = player.username
            val doc = Document("_id", uuid.toString())
                .append("username", username)
                .append("permissions", emptyList<String>())

            StreamConnection().write("profiles", doc)
            profiles[uuid] = Profile(player, username)
            println("[clerk] Successfully created a new profile for ${player.username}.")
        }

        fun addPermission(player: Player, permission: String){
            val uuid = player.uuid
            val filter = Document("_id", uuid.toString())
            StreamConnection().read("profiles", filter) { doc ->
                if (doc != null){
                    val permissions = doc.getList("permissions", String::class.java).toMutableSet()
                    permissions.add(permission)

                    val updatedDoc = Document(doc).append("permissions", permissions.toList())
                    StreamConnection().update("profiles", filter, updatedDoc)
                    println("[clerk] Successfully granted (${permission}) permission to ${player.username}")
                }
            }
        }

        fun removePermission(player: Player, permission: String){
            val uuid = player.uuid
            val filter = Document("_id", uuid.toString())
            StreamConnection().read("profiles", filter) { doc ->
                if (doc != null) {
                    val permissions = doc.getList("permissions", String::class.java).toMutableSet()
                    permissions.remove(permission)
                    val updatedDoc = Document(doc).append("permissions", permissions.toList())
                    StreamConnection().update("profiles", filter, updatedDoc)
                    println("[clerk] Successfully removed (${permission}) permission from ${player.username}")
                }
            }
        }

        fun hasPermission(player: Player, permission: String): Boolean {
            val uuid = player.uuid
            val filter = Document("_id", uuid.toString())
            var hasPermission = false
            val latch = CountDownLatch(1)

            StreamConnection().read("profiles", filter) { doc ->
                if (doc != null){
                    val permissions = doc.getList("permissions", String::class.java)
                    hasPermission = permissions.contains(permission)
                }
                latch.countDown()
            }

            latch.await(1, TimeUnit.SECONDS)
            return hasPermission
        }
    }
}