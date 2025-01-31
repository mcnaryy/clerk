package net.hellz.commands

import kotlinx.coroutines.runBlocking
import net.minestom.server.entity.Player
import net.hellz.clerk.Profile
import net.hellz.clerk.staff.StaffManager
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Optional
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.annotation.Suggest

class StaffChatCommand {

    @Command("staffchat", "sc")
    fun sc(
        sender: Player,
        @Optional @Suggest("hide") arg1: String? = null,
    ) {
        runBlocking {
            if (!Profile.hasPermission("${sender.uuid}", "commands.staffchat")) {
                sender.sendMessage("You do not have access to this command.")
            } else {
                if (arg1 == "hide") {
                    StaffManager.hideStaffChat(sender)
                } else {
                    StaffManager.toggleStaffChat(sender)
                }
            }
        }
    }
}
