package com.github.juniperfig.mysticismDemoV2.commands

import com.github.juniperfig.mysticismDemoV2.managers.MysticismTracker
import com.github.juniperfig.mysticismDemoV2.services.MessageService
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class SetMystCommand(
    private val mysticismTracker: MysticismTracker,
    private val messageService: MessageService
) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        // Permissions are handled via plugin.yml defaults for now.
        // No need for 'command.name' check here, as MysticismRootCommand handles dispatching.

        // Check args
        if (args.size != 2) {
            // Use the 'label' passed from MysticismRootCommand for accurate usage display
            sender.sendMessage(Component.text("Usage: /$label <player> <amount (0.0 - 1.0)>", NamedTextColor.YELLOW))
            return true
        }

        val targetPlayerName = args[0]
        val target = Bukkit.getPlayer(targetPlayerName)

        if (target == null || !target.isOnline) {
            messageService.sendPlayerNotFound(sender) // Assuming this method exists in MessageService
            return true
        }

        try {
            val value = args[1].toDouble()
            if (value < 0.0 || value > 1.0) {
                messageService.sendValueOutOfRange(sender)
                return true
            }

            mysticismTracker.setMysticism(target.uniqueId, value)
            messageService.sendSetMysticismSuccess(sender, target.name, value)
            messageService.sendSetMysticismTargetMessage(target, value)

        } catch (ignored: NumberFormatException) {
            messageService.sendInvalidNumber(sender, args[1])
        }

        return true
    }
}