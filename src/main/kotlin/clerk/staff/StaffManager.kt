package net.hellz.clerk.staff

import kotlinx.coroutines.runBlocking
import net.hellz.clerk.Profile
import net.hellz.clerk.staff.VanishManager.vanishedPlayers
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.entity.Player
import net.minestom.server.MinecraftServer
import net.minestom.server.adventure.audience.PacketGroupingAudience
import net.minestom.server.entity.GameMode
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import java.util.UUID

object StaffManager {

    private val staffChatDisabledPlayers = mutableSetOf<UUID>()
    private val staffChatHiddenPlayers = mutableSetOf<UUID>()

    fun isStaffMember(player: Player): Boolean {
        return runBlocking { Profile.hasPermission("${player.uuid}", "clerk.staff") }
    }

    fun toggleStaffChat(player: Player) {
        if (staffChatDisabledPlayers.contains(player.uuid)) {
            staffChatDisabledPlayers.remove(player.uuid)
            staffChatHiddenPlayers.remove(player.uuid)
            player.sendMessage(
                Component.text()
                    .append(Component.text("[clerk] ").color(NamedTextColor.AQUA))
                    .append(Component.text("You are now talking to the staff channel.").color(NamedTextColor.WHITE))
            )
        } else {
            staffChatDisabledPlayers.add(player.uuid)
            player.sendMessage(
                Component.text()
                    .append(Component.text("[clerk] ").color(NamedTextColor.AQUA))
                    .append(Component.text("You are no longer talking in the staff channel.").color(NamedTextColor.WHITE))
            )
        }
    }

    fun isStaffChatEnabled(player: Player): Boolean {
        return !staffChatDisabledPlayers.contains(player.uuid)
    }

    fun hideStaffChat(player: Player) {
        if (staffChatHiddenPlayers.contains(player.uuid)) {
            staffChatHiddenPlayers.remove(player.uuid)
            player.sendMessage(
                Component.text()
                    .append(Component.text("[clerk] ").color(NamedTextColor.AQUA))
                    .append(Component.text("You are now listening to the staff channel.").color(NamedTextColor.WHITE))
            )
        } else {
            staffChatHiddenPlayers.add(player.uuid)
            staffChatDisabledPlayers.add(player.uuid)
            player.sendMessage(
                Component.text()
                    .append(Component.text("[clerk] ").color(NamedTextColor.AQUA))
                    .append(Component.text("You are no longer tuned in the staff channel.").color(NamedTextColor.WHITE))
            )
        }
    }

    private fun isStaffChatHidden(player: Player): Boolean {
        return !staffChatHiddenPlayers.contains(player.uuid)
    }

    fun sendStaffMessage(sender: Player, message: String) {
        if (!runBlocking { Profile.hasPermission("${sender.uuid}", "clerk.staffchat") }) {
            sender.sendMessage(Component.text("You do not have access to staff chat.").color(NamedTextColor.RED))
            return
        }

        val staffComponent = Component.text()
            .append(Component.text("[Staff] ").color(NamedTextColor.AQUA))
            .append(Component.text(sender.username).color(NamedTextColor.AQUA))
            .append(Component.text(": ").color(NamedTextColor.WHITE))
            .append(Component.text(message).color(NamedTextColor.WHITE))
            .build()

        val staffMembers = MinecraftServer.getConnectionManager().onlinePlayers.filter { player ->
            runBlocking { Profile.hasPermission("${player.uuid}", "clerk.staffchat") && isStaffChatHidden(player) }
        }
        val staffAudience = PacketGroupingAudience.of(staffMembers)
        staffAudience.sendMessage(staffComponent)
    }

    private val staffModePlayers = mutableSetOf<UUID>()

    fun toggleStaffMode(player: Player) {
        if (!staffModePlayers.contains(player.uuid)) {
            staffModePlayers.add(player.uuid)
            VanishManager.vanishedPlayers.add(player.uuid)
            player.isInvisible = true
            player.setGameMode(GameMode.CREATIVE)
            player.sendMessage(
                Component.text()
                    .append(Component.text("[clerk] ").color(NamedTextColor.AQUA))
                    .append(Component.text("You have entered into the staff-mode.").color(NamedTextColor.WHITE))
            )
        } else {
            staffModePlayers.remove(player.uuid)
            VanishManager.vanishedPlayers.remove(player.uuid)
            player.isInvisible = false
            player.sendMessage(
                Component.text()
                    .append(Component.text("[clerk] ").color(NamedTextColor.AQUA))
                    .append(Component.text("You are no longer in the staff-mode.").color(NamedTextColor.WHITE))
            )
        }
    }
}