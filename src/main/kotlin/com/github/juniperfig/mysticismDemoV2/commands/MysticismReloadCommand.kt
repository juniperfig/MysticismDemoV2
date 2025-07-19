package com.github.juniperfig.mysticismDemoV2.commands

import com.github.juniperfig.mysticismDemoV2.lifecycle.MysticismLifecycleManager
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin

class MysticismReloadCommand(
    private val plugin: JavaPlugin,
    private val lifecycleManager: MysticismLifecycleManager
) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {

        // Perform the actual reload logic
        plugin.reloadConfig() // Bukkit's built-in method to load config.yml from disk
        lifecycleManager.initializeAndRegisterComponents(sender) // Delegate to the lifecycle manager

        return true // Command handled successfully
    }
}