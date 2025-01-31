package net.hellz.commands

import kotlinx.coroutines.runBlocking
import net.minestom.server.entity.Player
import net.hellz.clerk.Profile
import net.hellz.clerk.staff.VanishManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Optional

class VanishCommand {

    @Command("vanish", "v")
    fun staffmode(
        sender: Player,
        @Optional target: Player? = null,
    ) {
        runBlocking {
            if (!Profile.hasPermission("${sender.uuid}", "commands.vanish")) {
                sender.sendMessage("You do not have access to this command.")
            } else {
                if (target == null) {
                    VanishManager.toggleVanish(sender)
                } else {
                    sender.sendMessage(
                        Component.text()
                            .append(Component.text("[clerk] ").color(NamedTextColor.AQUA))
                            .append(Component.text("You have toggled ${target.username}'s visibility.").color(NamedTextColor.WHITE))
                    )
                    VanishManager.toggleVanish(target)
                }
            }
        }
    }


}
