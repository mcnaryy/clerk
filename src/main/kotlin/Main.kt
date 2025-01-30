package net.hellz

import CommandRegistrar
import net.hellz.clerk.Profile
import net.hellz.util.StreamConnection
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import net.minestom.server.event.player.PlayerChatEvent
import net.minestom.server.extras.velocity.VelocityProxy
import net.minestom.server.instance.block.Block
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.hellz.clerk.Rank
import net.hellz.utils.LettuceConnection
import net.kyori.adventure.text.Component

suspend fun main() {
    // Connect to the Mongo Database
    StreamConnection
    LettuceConnection

    val coroutineScope = CoroutineScope(Dispatchers.IO)

    // Init the server
    val server = MinecraftServer.init()

    // Create the instance and container
    val instanceManager = MinecraftServer.getInstanceManager()
    val instanceContainer = instanceManager.createInstanceContainer()

    // Generate the world for the instance
    instanceContainer.setGenerator { unit ->
        unit.modifier().fillHeight(0, 40, Block.GRASS_BLOCK)
    }

    // Create the player spawning configuration (Call the spawning instance)
    val globalEventHandler = MinecraftServer.getGlobalEventHandler()
    globalEventHandler.addListener(AsyncPlayerConfigurationEvent::class.java) { event ->
        val player = event.player
        event.spawningInstance = instanceContainer
        player.respawnPoint = Pos(0.0, 42.0, 0.0)

        coroutineScope.launch {
            Profile.retrieve(player)
        }
    }

    globalEventHandler.addListener(PlayerChatEvent::class.java) { event ->
        println(Rank.listRanks())
        coroutineScope.launch {
            Profile.addPermission(event.player, "commands.clerk")
        }
        val player = event.player
        val profile = Profile.profiles[player.uuid]
        val rankPrefix = profile?.rank?.prefix ?: ""
        event.formattedMessage = Component.text("$rankPrefix ${player.username}: ${event.rawMessage}")
    }

    // Mojang Authentication
    //MojangAuth.init()

    // Connects to the Velocity Proxy (Disabled MojangAuth)
    VelocityProxy.enable("gHi7VvKJ3oXv")

    // Start the server
    server.start("0.0.0.0", 25566)

    // Registers all the commands
    CommandRegistrar().reflect()

    Rank.loadRanksFromYaml()
}