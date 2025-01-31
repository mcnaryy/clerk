package net.hellz.commands

import kotlinx.coroutines.runBlocking
import net.hellz.clerk.Profile
import net.hellz.clerk.staff.StaffManager
import net.hellz.util.ConfigurationYAML
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player
import revxrsal.commands.annotation.Command

class BroadcastCommand {
    @Command("broadcast", "bc")
    suspend fun broadcast(sender: Player, message: String) {
        if (!Profile.hasPermission("${sender.uuid}", "commands.broadcast")) {
            sender.sendMessage(Component.text("You do not have access to this command.").color(NamedTextColor.RED))
            return
        }

        val component = Component.text(message).color(NamedTextColor.RED)

        // Send message to all online players
        MinecraftServer.getConnectionManager().onlinePlayers.forEach { player ->
            player.sendMessage(component)
        }

        println("Broadcasting: $message")
    }
}
