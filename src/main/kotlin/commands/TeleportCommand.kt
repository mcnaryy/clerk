package net.hellz.commands

import io.netty.handler.codec.compression.Zstd
import revxrsal.commands.annotation.*
import net.minestom.server.entity.Player
import net.hellz.clerk.Profile
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import net.hellz.clerk.Rank
import net.hellz.util.LocationYAML
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.utils.mojang.MojangUtils.getUUID
import revxrsal.commands.autocomplete.AutoCompleter
import revxrsal.commands.minestom.annotation.CommandPermission
import java.io.IOException

@Command("teleport", "tp")
class TeleportCommand {

    private val scope = CoroutineScope(Dispatchers.IO)  // You can use a custom scope for commands


    @Subcommand("<arg1>")
    suspend fun teleportPlayer(
        sender: Player,
        arg1: Player,
    ) {
        if (!Profile.hasPermission("${sender.uuid}", "commands.teleport")) {
            sender.sendMessage("You do not have access to this command.")
            return
        }
        sender.teleport(arg1.position)
        val name = arg1.username
        sender.sendMessage(
            Component.text()
                .append(Component.text("[clerk] ").color(NamedTextColor.AQUA))
                .append(Component.text("You have teleport to $name's location.").color(NamedTextColor.WHITE))
        )
    }
}
