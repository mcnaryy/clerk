package net.hellz.commands

import kotlinx.coroutines.runBlocking
import net.minestom.server.entity.Player
import net.hellz.clerk.Profile
import net.hellz.clerk.staff.StaffManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.entity.GameMode
import revxrsal.commands.annotation.Command

class NoClipCommand {

    @Command("noclip", "freecam")
    fun noclip(
        sender: Player,
    ) {
        runBlocking {
            if (!Profile.hasPermission("${sender.uuid}", "commands.noclip")) {
                sender.sendMessage("You do not have access to this command.")
            } else {
                if (sender.gameMode == GameMode.SPECTATOR) {
                    sender.gameMode = GameMode.CREATIVE
                    sender.sendMessage(Component.text("You have disabled no-clipping.").color(NamedTextColor.RED))
                } else {
                    sender.gameMode = GameMode.SPECTATOR
                    sender.sendMessage(Component.text("You have enabled no-clipping.").color(NamedTextColor.GREEN))
                }
            }
        }
    }
}
