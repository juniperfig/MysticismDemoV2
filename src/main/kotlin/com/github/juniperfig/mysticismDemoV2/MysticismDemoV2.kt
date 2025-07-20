package com.github.juniperfig.mysticismDemoV2

import com.github.juniperfig.mysticismDemoV2.commands.MysticismRootCommand
import com.github.juniperfig.mysticismDemoV2.lifecycle.MysticismLifecycleManager
import com.github.juniperfig.mysticismDemoV2.listeners.PlayerJoinListener
import com.github.juniperfig.mysticismDemoV2.listeners.PlayerQuitListener
import com.github.juniperfig.mysticismDemoV2.managers.MysticismBar
import com.github.juniperfig.mysticismDemoV2.services.MysticismDrainService
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.event.HandlerList
import org.bukkit.plugin.java.JavaPlugin

class MysticismDemoV2 : JavaPlugin() {

    companion object {
        // Gets the instance of this plugin
        val plugin by lazy { getPlugin(MysticismDemoV2::class.java) }
    }

    override fun onEnable() {
        // Initialize the central lifecycle manager
        // This call will load config, initialize all managers, and register their listeners/commands
        MysticismLifecycleManager.initializeAndRegisterComponents(null) // Pass null as sender, since it's startup

        // Register commands in paper
        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) {
            it.registrar().register("mysticism", MysticismRootCommand())
        }

        // Register Listeners that are not handled by managers and don't change on reload
        // These need access to MysticismDrainService, MysticismTracker, and MysticismBar
        server.pluginManager.registerEvents(
            PlayerQuitListener(), this
        )
        server.pluginManager.registerEvents(
            PlayerJoinListener(), this
        )

        logger.info("MysticismDemoV2 enabled!")
    }

    override fun onDisable() {
        MysticismDrainService.stopAllDrains()
        MysticismBar.hideAllMysticismBars()

        // Unregister all listeners specifically registered by THIS plugin instance.
        HandlerList.unregisterAll(this)

        logger.info("MysticismDemoV2 disabled!")
    }
}