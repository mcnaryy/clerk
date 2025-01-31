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
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import net.hellz.clerk.Rank
import net.hellz.clerk.staff.StaffManager
import net.hellz.util.ConfigurationYAML
import net.hellz.util.LocationYAML
import net.hellz.util.PlayerLocation
import net.hellz.utils.LettuceConnection
import net.kyori.adventure.text.Component
import net.minestom.server.extras.MojangAuth

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.event.player.AsyncPlayerPreLoginEvent
import net.minestom.server.event.player.PlayerDisconnectEvent
import net.minestom.server.network.packet.server.ServerPacket.Play

suspend fun main() {
    // Load the server configuration
    val configYAML = ConfigurationYAML()
    val serverConfig = configYAML.loadConfig()
    LocationYAML()

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


        val locations = LocationYAML().loadLocations()
        val lastLocation = locations.find { it.uuid == player.uuid.toString() }
        if (lastLocation != null) {
            player.respawnPoint = Pos(lastLocation.x, lastLocation.y, lastLocation.z)
        } else {
            player.respawnPoint = Pos(0.0, 42.0, 0.0)
        }
    }

    globalEventHandler.addListener(AsyncPlayerPreLoginEvent::class.java) { event ->
        val player = event.gameProfile.uuid.toString()

        coroutineScope.launch {
            val profile = async { Profile.retrieve(player) }
            profile.await()
        }

    }


    globalEventHandler.addListener(PlayerChatEvent::class.java) { event ->
        val player = event.player
        val profile = Profile.profiles[player.uuid]
        val rankPrefix = profile?.rank?.prefix?.replace('&', '§') ?: ""
        if (StaffManager.isStaffMember(player)) {
            if (StaffManager.isStaffChatEnabled(player)) {
                StaffManager.sendStaffMessage(player, event.rawMessage)
                event.isCancelled = true
                return@addListener
            }
        }
        event.formattedMessage = Component.text("$rankPrefix ${player.username}§7: §f${event.rawMessage}")
    }

    globalEventHandler.addListener(PlayerDisconnectEvent::class.java) { event ->
        val player = event.player
        val playerLocation = PlayerLocation(
            uuid = player.uuid.toString(),
            x = player.position.x,
            y = player.position.y,
            z = player.position.z,
            world = player.instance!!.uniqueId.toString()
        )
        LocationYAML().updatePlayerLocation(playerLocation)
    }

    // Authentication
    if (serverConfig.useVelocityProxy) {
        VelocityProxy.enable(serverConfig.velocitySecretKey)
    } else if (serverConfig.useMojangAuth) {
         MojangAuth.init()
    }

    // Start the server
    server.start("0.0.0.0", serverConfig.port)

    // Registers all the commands
    CommandRegistrar().reflect()

    Rank.loadRanksFromYaml()

}
