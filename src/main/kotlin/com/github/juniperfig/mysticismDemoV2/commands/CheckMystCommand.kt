package com.github.juniperfig.mysticismDemoV2.commands

import com.github.juniperfig.mysticismDemoV2.managers.MysticismTracker
import com.github.juniperfig.mysticismDemoV2.services.MessageService
import io.papermc.paper.command.brigadier.CommandSourceStack
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.entity.Player

/**
 * CommandExecutor for the /mysticism checkmyst command.
 * Allows players or console to check a player's current mysticism level.
 */

object CheckMystCommand : MystSubCommand {

    override val permission: String = "mysticism.checkmyst"

    override fun onCommand(sourceStack: CommandSourceStack, label: String, vararg args: String) {
        val sender = sourceStack.sender

        takeIf { hasPermission(sender) } ?: MessageService.sendNoPermission(sender, permission)

        val targetPlayer: Player?

        // Determine target player based on arguments
        when {
            args.size == 1 -> {
                // If one argument, it's a player name
                val targetName = args[0]
                targetPlayer = Bukkit.getPlayer(targetName)
                if (targetPlayer == null || !targetPlayer.isOnline) {
                    MessageService.sendPlayerNotFound(sender)
                    return
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
                return
            }
        }

        // Get mysticism level and send message
        val mysticismLevel = MysticismTracker.getMysticism(targetPlayer.uniqueId)
        val targetName = targetPlayer.name

        sender.sendMessage(
            MessageService.checkMysticismMessage(targetName, mysticismLevel)
        )

        return
    }
}