package net.hellz.commands

import revxrsal.commands.annotation.Command
import net.minestom.server.entity.Player
import kotlinx.coroutines.runBlocking
import net.hellz.clerk.Profile
import net.hellz.util.ConfigurationYAML
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.utils.mojang.MojangUtils.getUUID
import net.minestom.server.utils.mojang.MojangUtils.getUsername
import revxrsal.commands.annotation.Suggest
import java.io.IOException

class WhitelistCommand {

    private val config = ConfigurationYAML().loadConfig()

    @Command("whitelist")
    fun whitelist(
        sender: Player,
        @Suggest("(name)") target: String
    ) {
        runBlocking {
            if (!Profile.hasPermission(sender.uuid.toString(), "commands.whitelist")) {
                sender.sendMessage("You do not have access to this command.")
                return@runBlocking
            }

            val targetUUID = try {
                getUUID(target)
            } catch (e: IOException) {
                sender.sendMessage("This profile does not exist")
                return@runBlocking
            }

            if (targetUUID == null) {
                sender.sendMessage("This profile does not exist")
                return@runBlocking
            }

            if (!Profile.hasPermission(targetUUID.toString(), "commands.version")) {
                println("[Clerk] The player $target does not exist, creating a new profile for their welcome.")
                Profile.retrieve(targetUUID.toString(), getUsername(targetUUID))
            }

            if (Profile.hasPermission(targetUUID.toString(), "*") || Profile.hasPermission(targetUUID.toString(), "whitelist.*")) {
                sender.sendMessage("[clerk] You cannot modify this player's whitelist.")
                return@runBlocking
            }

            if (!Profile.hasPermission(targetUUID.toString(), "whitelist.access.${config.name}")) {
                Profile.addPermission(targetUUID.toString(), "whitelist.access.${config.name}")
                sender.sendMessage(
                    Component.text()
                        .append(Component.text("[clerk] ").color(NamedTextColor.AQUA))
                        .append(Component.text("Added $target to the ${config.name} whitelist.").color(NamedTextColor.WHITE))
                        .build()
                )
            } else {
                Profile.removePermission(targetUUID.toString(), "whitelist.access.${config.name}")
                sender.sendMessage(
                    Component.text()
                        .append(Component.text("[clerk] ").color(NamedTextColor.AQUA))
                        .append(Component.text("Removed $target from the ${config.name} whitelist.").color(NamedTextColor.WHITE))
                        .build()
                )
            }
        }
    }
}