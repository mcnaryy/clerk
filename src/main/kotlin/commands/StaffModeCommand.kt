package net.hellz.commands

import kotlinx.coroutines.runBlocking
import net.minestom.server.entity.Player
import net.hellz.clerk.Profile
import net.hellz.clerk.staff.StaffManager
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Optional
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.annotation.Suggest

class StaffModeCommand {

    @Command("staffmode", "staff", "modmode", "mod", "h")
    fun staffmode(
        sender: Player,
    ) {
        runBlocking {
            if (!Profile.hasPermission("${sender.uuid}", "commands.staffchat")) {
                sender.sendMessage("You do not have access to this command.")
            } else {
                StaffManager.toggleStaffMode(sender)
            }
        }
    }
}
