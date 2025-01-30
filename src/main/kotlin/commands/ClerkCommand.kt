package net.hellz.commands

import revxrsal.commands.annotation.*
import net.minestom.server.entity.Player
import net.hellz.clerk.Profile
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import net.hellz.clerk.Rank
import revxrsal.commands.autocomplete.AutoCompleter
import revxrsal.commands.minestom.annotation.CommandPermission


@Command("clerk", "co")
class ClerkCommand {

    private val scope = CoroutineScope(Dispatchers.IO)  // You can use a custom scope for commands

    @Subcommand("<arg1> inspect")
    suspend fun inspectProfile(
        sender: Player,
        arg1: Player
    ) {
        if (!Profile.hasPermission(sender, "commands.clerk")) {
            sender.sendMessage("You do not have access to this command.")
            return
        }
        val profile = Profile.profiles[arg1.uuid]
        if (profile == null) {
            sender.sendMessage("[clerk] Profile for player '${arg1.username}' does not exist.")
            return
        }
        val rank = profile.rank
        val permissions = profile.permissions.joinToString(", ")
        sender.sendMessage("[clerk] Player: ${arg1.username}")
        sender.sendMessage("[clerk] Rank: ${rank.displayName}")
        sender.sendMessage("[clerk] Permissions: $permissions")
    }

    @Subcommand("<arg1> setrank <arg2>")
    suspend fun setRank(
        sender: Player,
        arg1: Player,
        @Suggest("(name)") arg2: String,
    ) {
        if (!Profile.hasPermission(sender, "commands.clerk")) {
            sender.sendMessage("You do not have access to this command.")
            return
        }
        val rank = Rank.getRankByName(arg2)
        if (rank == null) {
            sender.sendMessage("[clerk] Rank '$arg2' does not exist.")
            return
        }
        scope.launch {
            Profile.setRank(arg1, rank)
        }
        sender.sendMessage("[clerk] Successfully set rank '$arg2' for player '${arg1.username}'.")
    }

    @Subcommand("<arg1> permission add <arg2>")
    suspend fun addPermission(
        sender: Player,
        arg1: Player,
        @Suggest("commands.example") arg2: String,
    ) {
        if (!Profile.hasPermission(sender, "commands.clerk")) {
            sender.sendMessage("You do not have access to this command.")
            return
        }
        sender.sendMessage("[clerk] You have granted ${arg1.username} the permission: ${arg2}")
        arg1.sendMessage("[clerk] You have been granted the permission: ${arg2}")
        scope.launch {
            Profile.addPermission(arg1, arg2)
        }
    }

    @Subcommand("<arg1> permission remove <arg2>")
    suspend fun removePermission(
        sender: Player,
        arg1: Player,
        @Suggest("commands.example") arg2: String,
    ) {
        if (!Profile.hasPermission(sender, "commands.clerk")) {
            sender.sendMessage("You do not have access to this command.")
            return
        }
        sender.sendMessage("[clerk] You have revoked ${arg1.username} the permission: ${arg2}")
        arg1.sendMessage("[clerk] You have been revoked the permission: ${arg2}")
        scope.launch {
            Profile.removePermission(arg1, arg2)
        }
    }

    @Subcommand("rank create <arg1>")
    suspend fun createRank(
        sender: Player,
        @Suggest("(name)") arg1: String,
    ) {
        if (!Profile.hasPermission(sender, "commands.clerk")) {
            sender.sendMessage("You do not have access to this command.")
            return
        }
        val newRank = Rank.createRank(
            displayName = arg1,
            weight = 1, // Default weight
            prefix = "[$arg1]", // Default prefix
            permissions = emptySet() // Default permissions
        )
        sender.sendMessage("[clerk] Successfully created the rank: $arg1")
    }

    @Subcommand("rank delete <arg1>")
    suspend fun deleteRank(
        sender: Player,
        @Suggest("(name)") arg1: String,
    ) {
        if (!Profile.hasPermission(sender, "commands.clerk")) {
            sender.sendMessage("You do not have access to this command.")
            return
        }
        Rank.deleteRank(Rank.getRankByName(arg1) ?: return)
        sender.sendMessage("[clerk] Successfully deleted the rank: $arg1")
    }

    @Subcommand("rank setweight <arg1> <arg2>")
    suspend fun setRankWeight(
        sender: Player,
        @Suggest("(name)") arg1: String,
        arg2: Int
    ) {
        if (!Profile.hasPermission(sender, "commands.clerk")) {
            sender.sendMessage("You do not have access to this command.")
            return
        }
        scope.launch {
            Rank.setRankWeight(arg1, arg2)
        }
        sender.sendMessage("[clerk] Successfully set the weight of rank '$arg1' to $arg2.")
    }

    @Subcommand("rank setprefix <arg1> <arg2>")
    suspend fun setRankPrefix(
        sender: Player,
        @Suggest("(name)") arg1: String,
        arg2: String
    ) {
        if (!Profile.hasPermission(sender, "commands.clerk")) {
            sender.sendMessage("You do not have access to this command.")
            return
        }
        scope.launch {
            Rank.setRankPrefix(arg1, arg2)
        }
        sender.sendMessage("[clerk] Successfully set the prefix of rank '$arg1' to '$arg2'.")
    }

    @Subcommand("rank addperm <arg1> <arg2>")
    suspend fun addRankPermission(
        sender: Player,
        @Suggest("(name)") arg1: String,
        @Suggest("commands.example") arg2: String,
    ) {
        if (!Profile.hasPermission(sender, "commands.clerk")) {
            sender.sendMessage("You do not have access to this command.")
            return
        }
        scope.launch {
            Rank.addRankPermission(arg1, arg2)
        }
        sender.sendMessage("[clerk] Successfully added permission '$arg2' to rank '$arg1'.")
    }

    @Subcommand("rank removeper <arg1> <arg2>")
    suspend fun removeRankPermission(
        sender: Player,
        @Suggest("(name)") arg1: String,
        @Suggest("commands.example") arg2: String,
    ) {
        if (!Profile.hasPermission(sender, "commands.clerk")) {
            sender.sendMessage("You do not have access to this command.")
            return
        }
        scope.launch {
            Rank.removeRankPermission(arg1, arg2)
        }
        sender.sendMessage("[clerk] Successfully removed permission '$arg2' from rank '$arg1'.")
    }

    @Subcommand("rank inspect <arg1>")
    suspend fun inspectRank(
        sender: Player,
        @Suggest("(name)") arg1: String
    ) {
        if (!Profile.hasPermission(sender, "commands.clerk")) {
            sender.sendMessage("You do not have access to this command.")
            return
        }
        val rank = Rank.getRankByName(arg1)
        if (rank == null) {
            sender.sendMessage("[clerk] Rank '$arg1' does not exist.")
            return
        }
        val permissions = rank.permissions.joinToString(", ")
        sender.sendMessage("[clerk] Rank: ${rank.displayName}")
        sender.sendMessage("[clerk] Weight: ${rank.weight}")
        sender.sendMessage("[clerk] Prefix: ${rank.prefix}")
        sender.sendMessage("[clerk] Permissions: $permissions")
        sender.sendMessage("[clerk] Inherits: ${rank.inherits?.displayName ?: "None"}")
    }

    @Subcommand("ranks")
    suspend fun listRanks(sender: Player) {
        if (!Profile.hasPermission(sender, "commands.clerk")) {
            sender.sendMessage("You do not have access to this command.")
            return
        }
        val ranks = Rank.listRanks().joinToString(", ")
        sender.sendMessage("[clerk] Available ranks: $ranks")
    }

}