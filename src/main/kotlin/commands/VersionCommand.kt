package net.hellz.commands

import revxrsal.commands.annotation.Command
import net.minestom.server.entity.Player
import net.hellz.clerk.Profile
import kotlinx.coroutines.runBlocking

class VersionCommand {

    @Command("version", "ver")
    fun version(sender: Player) {
        runBlocking {
            if (!Profile.hasPermission("${sender.uuid}", "commands.version")) {
                sender.sendMessage("You do not have access to this command.")
            } else {
                sender.sendMessage("You are currently running clerk.02")
            }
        }
    }
}