package com.github.juniperfig.mysticismDemoV2

import com.github.juniperfig.mysticismDemoV2.commands.CheckMystCommand
import com.github.juniperfig.mysticismDemoV2.commands.MysticismReloadCommand
import com.github.juniperfig.mysticismDemoV2.commands.SetMystCommand
import com.github.juniperfig.mysticismDemoV2.lifecycle.MysticismLifecycleManager
import com.github.juniperfig.mysticismDemoV2.listeners.PlayerJoinListener
import com.github.juniperfig.mysticismDemoV2.listeners.PlayerQuitListener
import org.bukkit.event.HandlerList
import org.bukkit.plugin.java.JavaPlugin

class MysticismDemoV2 : JavaPlugin() {

    // The main plugin class now only holds an instance of the lifecycle manager
    private lateinit var mysticismLifecycleManager: MysticismLifecycleManager

    override fun onEnable() {
        // Initialize the central lifecycle manager
        mysticismLifecycleManager = MysticismLifecycleManager(this)
        // This call will load config, initialize all managers, and register their listeners/commands
        mysticismLifecycleManager.initializeAndRegisterComponents(null) // Pass null as sender, since it's startup

        // Create instances of the individual command executors
        val setMystCommand = SetMystCommand(
            mysticismLifecycleManager.getMysticismTracker(),
            mysticismLifecycleManager.getMessageService()
        )
        val checkMystCommand = CheckMystCommand(
            mysticismLifecycleManager.getMysticismTracker(),
            mysticismLifecycleManager.getMessageService()
        )
        // The mysticismReloadCommand instance is created here
        val mysticismReloadCommand = MysticismReloadCommand(this, mysticismLifecycleManager)

        // Register the MysticismRootCommand as the executor for the "mysticism" command.
        // This command will now handle "setmyst", "checkmyst", and "reload" as subcommands.
        val rootCommand = getCommand("mysticism")
        if (rootCommand != null) {
            rootCommand.setExecutor(
                MysticismRootCommand(
                    this, // The 'plugin' instance
                    setMystCommand,
                    checkMystCommand,
                    mysticismReloadCommand
                )
            )
            logger.info("MysticismDemoV2: Successfully set MysticismRootCommand executor for /mysticism.")
        } else {
            logger.warning("MysticismDemoV2: Command 'mysticism' not found in plugin.yml or could not be retrieved. Command registration failed!")
        }

        // Register Listeners that are not handled by managers and don't change on reload
        // These need access to MysticismDrainService, MysticismTracker, and MysticismBar
        server.pluginManager.registerEvents(
            PlayerQuitListener(
                mysticismLifecycleManager.getMysticismDrainService(),
                mysticismLifecycleManager.getMysticismTracker()
            ), this
        )
        server.pluginManager.registerEvents(
            PlayerJoinListener(mysticismLifecycleManager.getMysticismBar()), this
        )

        logger.info("MysticismDemoV2 enabled!")
    }

    override fun onDisable() {
        // Delegate shutdown tasks to the lifecycle manager
        if (::mysticismLifecycleManager.isInitialized) {
            mysticismLifecycleManager.stopAllDrains()
            mysticismLifecycleManager.hideAllMysticismBars()
        }

        // Unregister all listeners specifically registered by THIS plugin instance.
        HandlerList.unregisterAll(this)

        logger.info("MysticismDemoV2 disabled!")
    }
}