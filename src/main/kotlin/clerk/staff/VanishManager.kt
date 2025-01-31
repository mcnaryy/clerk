package net.hellz.clerk.staff

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.entity.Player
import java.util.*

object VanishManager{

    val vanishedPlayers = mutableSetOf<UUID>()

    fun toggleVanish(player: Player){
        if(vanishedPlayers.contains(player.uuid)){
            vanishedPlayers.remove(player.uuid)
            player.isInvisible = false
            player.sendMessage(
                Component.text()
                    .append(Component.text("[clerk] ").color(NamedTextColor.AQUA))
                    .append(Component.text("You are are now visible to the public.").color(NamedTextColor.WHITE))
            )
        }else{
            vanishedPlayers.add(player.uuid)
            player.isInvisible = true
            player.sendMessage(
                Component.text()
                    .append(Component.text("[clerk] ").color(NamedTextColor.AQUA))
                    .append(Component.text("You are now invisible to the public.").color(NamedTextColor.WHITE))
            )
        }
    }

    fun isVanished(player: Player): Boolean {
        return vanishedPlayers.contains(player.uuid)
    }


}