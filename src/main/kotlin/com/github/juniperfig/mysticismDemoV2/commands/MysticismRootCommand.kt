package com.github.juniperfig.mysticismDemoV2.commands

import io.papermc.paper.command.brigadier.BasicCommand
import io.papermc.paper.command.brigadier.CommandSourceStack
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

class MysticismRootCommand : BasicCommand {

    override fun permission(): String {
        return "mysticism.use"
    }

    // Basic suggestions when running commands
    override fun suggest(source: CommandSourceStack, args: Array<out String>): MutableCollection<String> {
        // No args show all available
        if (args.isEmpty()) {
            return mutableListOf("setmyst", "checkmyst", "reload")
        }

        val subCommand = args[0].lowercase()

        return when (subCommand) {
            "setmyst" -> {
                playerList()
            }

            "checkmyst" -> {
               playerList()
            }

            else -> {
                mutableListOf()
            }
        }
    }

    // Gets a list of online players for command suggestions
    private fun playerList() = Bukkit.getOnlinePlayers().map { it.name }.toMutableList()

    override fun execute(source: CommandSourceStack, args: Array<out String>) {
        // Help menu
        val sender = source.sender
        if (args.isEmpty()) {
            sendUsage(sender)
            return
        }

        val subCommand = args[0].lowercase()
        val subCommandArgs = args.drop(1).toTypedArray()

        when (subCommand) {
            "setmyst" -> {
                SetMystCommand.onCommand(source, "mysticism setmyst", *subCommandArgs)
            }
            "checkmyst" -> {
                CheckMystCommand.onCommand(source, "mysticism checkmyst", *subCommandArgs)
            }
            "reload" -> {
                MysticismReloadCommand.onCommand(source, "mysticism reload", *subCommandArgs)
            }
            else -> {
                sender.sendMessage(Component.text("Unknown subcommand: '$subCommand'.", NamedTextColor.RED))
                sendUsage(sender)
                return
            }
        }
    }

    private fun sendUsage(sender: CommandSender) {
        sender.sendMessage(Component.text("--- Mysticism Commands ---", NamedTextColor.GOLD))
        sender.sendMessage(Component.text("/mysticism setmyst <player> <amount> - Set a player's mysticism level.", NamedTextColor.YELLOW))
        sender.sendMessage(Component.text("/mysticism checkmyst [player] - Check a player's mysticism level.", NamedTextColor.YELLOW))
        sender.sendMessage(Component.text("/mysticism reload - Reload the plugin configuration.", NamedTextColor.YELLOW))
        sender.sendMessage(Component.text("------------------------", NamedTextColor.GOLD))
    }
}