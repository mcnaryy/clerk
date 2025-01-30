package net.hellz.clerk

import kotlinx.coroutines.*
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.representer.Representer
import java.io.File
import java.util.concurrent.ConcurrentHashMap

sealed class Rank(
    val displayName: String,
    var weight: Int,
    var prefix: String,
    val permissions: MutableSet<String> = mutableSetOf(),
    val inherits: Rank? = null
) {
    companion object {
        private val ranks = ConcurrentHashMap<String, Rank>()
        val defaultRank: Rank by lazy {
            CustomRank(
                displayName = "Member",
                weight = 1,
                prefix = "[Member]",
                permissions = mutableSetOf("commands.version", "commands.test")
            ).also { ranks[it.displayName] = it }
        }

        fun createRank(
            displayName: String,
            weight: Int,
            prefix: String,
            permissions: Set<String> = emptySet(),
            inherits: Rank? = defaultRank
        ): Rank {
            val newRank = CustomRank(displayName, weight, prefix, permissions.toMutableSet(), inherits)
            ranks[displayName] = newRank
            CoroutineScope(Dispatchers.IO).launch { saveRanksToYaml() }
            return newRank
        }

        fun deleteRank(rank: Rank) {
            ranks.remove(rank.displayName)
            CoroutineScope(Dispatchers.IO).launch { saveRanksToYaml() }
        }

        fun listRanks(): List<String> = ranks.values.sortedBy { it.weight }.map { it.displayName }

        fun getAllPermissions(rank: Rank): Set<String> {
            val allPermissions = mutableSetOf<String>()
            var currentRank: Rank? = rank
            while (currentRank != null) {
                allPermissions.addAll(currentRank.permissions)
                currentRank = currentRank.inherits
            }
            return allPermissions
        }

        fun getRankByName(name: String): Rank? = ranks[name]

        suspend fun setRankWeight(rankName: String, weight: Int) {
            val rank = getRankByName(rankName) ?: return
            rank.weight = weight
            saveRanksToYaml()
        }

        suspend fun setRankPrefix(rankName: String, prefix: String) {
            val rank = getRankByName(rankName) ?: return
            rank.prefix = prefix
            saveRanksToYaml()
        }

        suspend fun addRankPermission(rankName: String, permission: String) {
            val rank = getRankByName(rankName) ?: return
            rank.permissions.add(permission)
            saveRanksToYaml()
        }

        suspend fun removeRankPermission(rankName: String, permission: String) {
            val rank = getRankByName(rankName) ?: return
            rank.permissions.remove(permission)
            saveRanksToYaml()
        }

        private suspend fun saveRanksToYaml() {
            withContext(Dispatchers.IO) {
                try {
                    val ranksData = ranks.values.map { rank ->
                        mapOf(
                            "rank" to rank.displayName,
                            "weight" to rank.weight,
                            "prefix" to rank.prefix,
                            "permissions" to rank.permissions.toList(),
                            "inherits" to rank.inherits?.displayName
                        )
                    }

                    val options = DumperOptions().apply {
                        defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
                        indent = 2
                    }
                    val yaml = Yaml(options)
                    val file = File("ranks.yml")
                    file.writeText(yaml.dump(ranksData))

                    println("[clerk] Successfully saved ranks to ranks.yml.")
                } catch (e: Exception) {
                    println("[clerk] Error saving ranks: ${e.message}")
                }
            }
        }

        fun loadRanksFromYaml() {
            val file = File("ranks.yml")
            if (!file.exists() || file.readText().isBlank()) {
                println("[clerk] ranks.yml not found or empty, creating default rank...")
                CoroutineScope(Dispatchers.IO).launch { saveRanksToYaml() }
                return
            }

            try {
                val yaml = Yaml()
                val ranksData: List<Map<String, Any>> = yaml.load(file.readText()) ?: emptyList()

                // Preserve default rank and clear others
                ranks.clear()
                ranks[defaultRank.displayName] = defaultRank

                runBlocking {
                    ranksData.map { rankData ->
                        async(Dispatchers.Default) {
                            val displayName = rankData["rank"] as String
                            if (getRankByName(displayName) != null) return@async // Prevent duplicates

                            val prefix = rankData["prefix"] as String
                            val weight = rankData["weight"] as Int
                            val permissions = (rankData["permissions"] as List<String>).toMutableSet()
                            val inheritsName = rankData["inherits"] as String?
                            val inherits = inheritsName?.let { getRankByName(it) }

                            val rank = CustomRank(displayName, weight, prefix, permissions, inherits)
                            ranks[displayName] = rank
                        }
                    }.awaitAll()
                }

                println("[clerk] Successfully loaded ranks from ranks.yml.")
            } catch (e: Exception) {
                println("[clerk] Error loading ranks: ${e.message}")
            }
        }
    }
}

class CustomRank(
    displayName: String,
    weight: Int,
    prefix: String,
    permissions: MutableSet<String> = mutableSetOf(),
    inherits: Rank? = null
) : Rank(displayName, weight, prefix, permissions, inherits)