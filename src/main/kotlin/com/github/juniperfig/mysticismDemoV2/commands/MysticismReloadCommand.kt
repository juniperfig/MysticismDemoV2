package com.github.juniperfig.mysticismDemoV2.commands

import com.github.juniperfig.mysticismDemoV2.MysticismDemoV2.Companion.plugin
import com.github.juniperfig.mysticismDemoV2.lifecycle.MysticismLifecycleManager
import com.github.juniperfig.mysticismDemoV2.services.MessageService
import io.papermc.paper.command.brigadier.CommandSourceStack

object MysticismReloadCommand : MystSubCommand {

    override val permission: String = "mysticism.reload"

    override fun onCommand(source: CommandSourceStack, label: String, vararg args: String) {
        val sender = source.sender

        takeIf { hasPermission(sender) } ?: MessageService.sendNoPermission(sender, permission)

        // Perform the actual reload logic
        plugin.reloadConfig() // Bukkit's built-in method to load config.yml from disk
        MysticismLifecycleManager.initializeAndRegisterComponents(sender) // Delegate to the lifecycle manager
    }
}