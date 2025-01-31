package net.hellz.commands

import net.hellz.util.LocationYAML
import net.minestom.server.coordinate.Pos
import net.minestom.server.utils.mojang.MojangUtils.getUUID
import java.io.IOException
import net.minestom.server.entity.Player
import net.hellz.clerk.Profile
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Suggest

class TeleportOfflineCommand {

    @Command("tpo")
    suspend fun teleportPlayerOffline(
        sender: Player,
        @Suggest("<offline player>") arg1: String,
    ) {
        if (!Profile.hasPermission("${sender.uuid}", "commands.teleport")) {
            sender.sendMessage("You do not have access to this command.")
            return
        }

        val uuid = try {
            getUUID(arg1).toString()
        } catch (e: IOException) {
            sender.sendMessage(
                Component.text()
                    .append(Component.text("[clerk] ").color(NamedTextColor.AQUA))
                    .append(Component.text("We could not find a player by the name of '$arg1'.").color(NamedTextColor.WHITE))
            )
            return
        }

        val locations = LocationYAML().loadLocations()
        val targetLocation = locations.find { it.uuid == uuid }

        if (targetLocation != null) {
            sender.teleport(Pos(targetLocation.x, targetLocation.y, targetLocation.z))
            sender.sendMessage(
                Component.text()
                    .append(Component.text("[clerk] ").color(NamedTextColor.AQUA))
                    .append(Component.text("You have teleported to $arg1's offline location.").color(NamedTextColor.WHITE))
            )
        } else {
            sender.sendMessage(
                Component.text()
                    .append(Component.text("[clerk] ").color(NamedTextColor.AQUA))
                    .append(Component.text("There was no saved location of '$arg1' to teleport.").color(NamedTextColor.WHITE))
            )
        }
    }


}
