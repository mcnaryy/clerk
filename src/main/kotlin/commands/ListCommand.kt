package net.hellz.commands

import revxrsal.commands.annotation.Command
import net.minestom.server.entity.Player
import net.minestom.server.MinecraftServer
import kotlinx.coroutines.runBlocking
import net.hellz.clerk.Profile
import net.hellz.util.ConfigurationYAML

class ListCommand {

    private val config = ConfigurationYAML().loadConfig()

    @Command("list")
    fun list(sender: Player) {
        runBlocking {
            if (!Profile.hasPermission("${sender.uuid}", "commands.list")) {
                sender.sendMessage("You do not have access to this command.")
            } else {
                val onlinePlayers = MinecraftServer.getConnectionManager().onlinePlayers
                val playerCount = onlinePlayers.size
                val maxPlayers = config.maxPlayers
                val playerList = onlinePlayers.joinToString(", ") { it.username }
                sender.sendMessage("($playerCount/$maxPlayers) [$playerList]")
            }
        }
    }
}