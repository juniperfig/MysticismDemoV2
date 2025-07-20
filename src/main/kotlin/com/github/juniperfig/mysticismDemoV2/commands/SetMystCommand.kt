package com.github.juniperfig.mysticismDemoV2.commands

import com.github.juniperfig.mysticismDemoV2.managers.MysticismTracker
import com.github.juniperfig.mysticismDemoV2.services.MessageService
import io.papermc.paper.command.brigadier.CommandSourceStack
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit

object SetMystCommand : MystSubCommand {

    override val permission: String = "mysticism.setmyst"

    override fun onCommand(source: CommandSourceStack, label: String, vararg args: String) {
        // Permissions are handled via plugin.yml defaults for now.
        // No need for 'command.name' check here, as MysticismRootCommand handles dispatching.
        val sender = source.sender

        // Check args
        if (args.size != 2) {
            // Use the 'label' passed from MysticismRootCommand for accurate usage display
            sender.sendMessage(Component.text("Usage: /$label <player> <amount (0.0 - 1.0)>", NamedTextColor.YELLOW))
            return
        }

        val targetPlayerName = args[0]
        val target = Bukkit.getPlayer(targetPlayerName)

        if (target == null || !target.isOnline) {
            MessageService.sendPlayerNotFound(sender) // Assuming this method exists in MessageService
            return
        }

        try {
            val value = args[1].toDouble()
            if (value < 0.0 || value > 1.0) {
                MessageService.sendValueOutOfRange(sender)
                return
            }

            MysticismTracker.setMysticism(target.uniqueId, value)
            MessageService.sendSetMysticismSuccess(sender, target.name, value)
            MessageService.sendSetMysticismTargetMessage(target, value)

        } catch (ignored: NumberFormatException) {
            MessageService.sendInvalidNumber(sender, args[1])
        }

        return
    }
}