package com.github.juniperfig.mysticismDemoV2.commands

import com.github.juniperfig.mysticismDemoV2.lifecycle.MysticismLifecycleManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin

class MysticismRootCommand(
    private val plugin: JavaPlugin,
    private val mysticismLifecycleManager: MysticismLifecycleManager,
    private val setMystCommand: SetMystCommand,
    private val checkMystCommand: CheckMystCommand,
    private val mysticismReloadCommand: MysticismReloadCommand
) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            sendUsage(sender)
            return true
        }

        val subCommand = args[0].lowercase()
        val subCommandArgs = args.drop(1).toTypedArray()

        when (subCommand) {
            "setmyst" -> {
                return setMystCommand.onCommand(sender, command, "mysticism setmyst", subCommandArgs)
            }
            "checkmyst" -> {
                return checkMystCommand.onCommand(sender, command, "mysticism checkmyst", subCommandArgs)
            }
            "reload" -> {
                return mysticismReloadCommand.onCommand(sender, command, "mysticism reload", subCommandArgs)
            }
            else -> {
                sender.sendMessage(Component.text("Unknown subcommand: '$subCommand'.", NamedTextColor.RED))
                sendUsage(sender)
                return true
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