package net.hellz.util

import net.hellz.clerk.Profile
import net.minestom.server.entity.Player
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import revxrsal.commands.annotation.Subcommand
import java.io.File

data class ServerConfig(
    val name: String,
    val port: Int,
    val velocitySecretKey: String,
    val useVelocityProxy: Boolean,
    val useMojangAuth: Boolean,
    val maxPlayers: Int,
    val whitelist: Boolean
)

class ConfigurationYAML {

    private val configFile = File("config.yml")
    private val yaml: Yaml

    init {
        val options = DumperOptions().apply {
            defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
            indent = 2
        }
        yaml = Yaml(options)
    }

    fun loadConfig(): ServerConfig {
        if (!configFile.exists()) {
            val defaultConfig = ServerConfig("MyServer", 25566, "gHi7VvKJ3oXv", true, false, 500, true)
            saveConfig(defaultConfig)
            return defaultConfig
        }

        val configData: Map<String, Any> = yaml.load(configFile.readText())
        val serverData = configData["server"] as Map<String, Any>
        return ServerConfig(
            name = serverData["name"] as String,
            port = serverData["port"] as Int,
            velocitySecretKey = serverData["velocitySecretKey"] as String,
            useVelocityProxy = serverData["useVelocityProxy"] as Boolean,
            useMojangAuth = serverData["useMojangAuth"] as Boolean,
            maxPlayers = serverData["maxPlayers"] as Int,
            whitelist = serverData["whitelist"] as Boolean
        )
    }

    fun saveConfig(config: ServerConfig) {
        val configData = """
            server:
              name: ${config.name}
              port: ${config.port}
              velocitySecretKey: '${config.velocitySecretKey}'
              useVelocityProxy: ${config.useVelocityProxy} # Setting this to true will override the MojangAuth
              useMojangAuth: ${config.useMojangAuth} # Set this to true to use Mojang servers for authentication (skins, capes, etc.)
              maxPlayers: ${config.maxPlayers}
              whitelist: ${config.whitelist}
        """.trimIndent()
        configFile.writeText(configData)
    }
}

data class PlayerLocation(
    val uuid: String,
    val x: Double,
    val y: Double,
    val z: Double,
    val world: String
)

class LocationYAML {

    private val locationsFile = File("locations.yml")
    private val yaml: Yaml

    init {
        val options = DumperOptions().apply {
            defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
            indent = 2
        }
        yaml = Yaml(options)
    }

    fun loadLocations(): List<PlayerLocation> {
        if (!locationsFile.exists()) {
            return emptyList()
        }

        val locationsData: List<Map<String, Any>> = yaml.load(locationsFile.readText())
        return locationsData.map { data ->
            PlayerLocation(
                uuid = data["uuid"] as String,
                x = data["x"] as Double,
                y = data["y"] as Double,
                z = data["z"] as Double,
                world = data["world"] as String
            )
        }
    }

    fun saveLocations(locations: List<PlayerLocation>) {
        val locationsData = locations.map { location ->
            mapOf(
                "uuid" to location.uuid,
                "x" to location.x,
                "y" to location.y,
                "z" to location.z,
                "world" to location.world
            )
        }
        locationsFile.writeText(yaml.dump(locationsData))
    }

    fun updatePlayerLocation(playerLocation: PlayerLocation) {
        val locations = loadLocations().toMutableList()
        val existingLocation = locations.find { it.uuid == playerLocation.uuid }
        if (existingLocation != null) {
            locations.remove(existingLocation)
        }
        locations.add(playerLocation)
        saveLocations(locations)
    }
}