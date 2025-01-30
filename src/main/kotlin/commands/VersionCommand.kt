package net.hellz.commands

import revxrsal.commands.annotation.Command
import net.minestom.server.entity.Player
import net.hellz.clerk.Profile

class VersionCommand {

    @Command("version", "ver")
    fun version(sender: Player){
        if (!Profile.hasPermission(sender, "commands.version")){
            sender.sendMessage("You do not have access to this command.")
        } else{
            sender.sendMessage("You are currently running clerk.01")
        }
    }
}