package net.hellz

import net.hellz.clerk.Profile
import net.hellz.commands.CommandRegistrar
import net.hellz.util.StreamConnection
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import net.minestom.server.event.player.AsyncPlayerPreLoginEvent
import net.minestom.server.event.player.PlayerChatEvent
import net.minestom.server.extras.MojangAuth
import net.minestom.server.extras.velocity.VelocityProxy
import net.minestom.server.instance.block.Block

fun main() {
    // Connect to the Mongo Database
    StreamConnection()

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
    globalEventHandler.addListener(AsyncPlayerConfigurationEvent::class.java){ event ->
        val player = event.player
        event.spawningInstance = instanceContainer
        player.respawnPoint = Pos(0.0, 42.0, 0.0)
        Profile.retrieve(player)
    }

    globalEventHandler.addListener(PlayerChatEvent::class.java){ event ->
        val player = event.player
        Profile.removePermission(player, "commands.version")
    }

    // Mojang Authentication
    //MojangAuth.init()


    // Connects to the Velocity Proxy (Disabled MojangAuth)
    VelocityProxy.enable("gHi7VvKJ3oXv")

    // Start the server
    server.start("0.0.0.0", 25566)


    // Registers all the commands
    CommandRegistrar().reflect()
}