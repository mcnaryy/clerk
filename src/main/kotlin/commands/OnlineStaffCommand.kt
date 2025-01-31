package net.hellz.commands

import revxrsal.commands.annotation.Command
import net.minestom.server.entity.Player
import net.minestom.server.MinecraftServer
import kotlinx.coroutines.runBlocking
import net.hellz.clerk.Profile
import net.hellz.clerk.staff.StaffManager
import net.hellz.util.ConfigurationYAML

class OnlineStaffCommand {

    private val config = ConfigurationYAML().loadConfig()
    private val staffManager = StaffManager

    @Command("onlinestaff")
    fun onlineStaff(sender: Player) {
        runBlocking {
            if (!Profile.hasPermission("${sender.uuid}", "commands.onlinestaff")) {
                sender.sendMessage("You do not have access to this command.")
            } else {
                val onlinePlayers = MinecraftServer.getConnectionManager().onlinePlayers
                val staffMembers = onlinePlayers.filter { staffManager.isStaffMember(it) }
                val staffList = staffMembers.joinToString(", ") { it.username }
                sender.sendMessage("Online Staff: [$staffList]")
            }
        }
    }
}