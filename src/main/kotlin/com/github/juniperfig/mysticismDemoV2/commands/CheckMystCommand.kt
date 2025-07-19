package com.github.juniperfig.mysticismDemoV2.commands

import com.github.juniperfig.mysticismDemoV2.managers.MysticismTracker
import com.github.juniperfig.mysticismDemoV2.services.MessageService
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * CommandExecutor for the /mysticism checkmyst command.
 * Allows players or console to check a player's current mysticism level.
 */

class CheckMystCommand(
    private val mysticismTracker: MysticismTracker, // Needs access to mysticism data
    private val messageService: MessageService // Needs to send messages
) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {

        val targetPlayer: Player?

        // Determine target player based on arguments
        when {
            args.size == 1 -> {
                // If one argument, it's a player name
                val targetName = args[0]
                targetPlayer = Bukkit.getPlayer(targetName)
                if (targetPlayer == null || !targetPlayer.isOnline) {
                    messageService.sendPlayerNotFound(sender)
                    return true
                }
            }
            args.isEmpty() && sender is Player -> {
                // If no arguments and sender is a player, target self
                targetPlayer = sender
            }
            else -> {
                // Invalid usage or console trying to check self without specifying player
                // Use the 'label' passed from MysticismRootCommand for accurate usage display
                sender.sendMessage(Component.text("Usage: /$label [player]", NamedTextColor.YELLOW))
                return true
            }
        }

        // Get mysticism level and send message
        val mysticismLevel = mysticismTracker.getMysticism(targetPlayer.uniqueId)
        val targetName = targetPlayer.name

        sender.sendMessage(
            messageService.checkMysticismMessage(targetName, mysticismLevel)
        )

        return true
    }
}