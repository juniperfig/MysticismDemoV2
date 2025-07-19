package com.github.juniperfig.mysticismDemoV2.lifecycle // Updated package name

import com.github.juniperfig.mysticismDemoV2.config.ConfigLoader
import com.github.juniperfig.mysticismDemoV2.config.PluginConfig
import com.github.juniperfig.mysticismDemoV2.listeners.PotionListener
import com.github.juniperfig.mysticismDemoV2.managers.FlightManager
import com.github.juniperfig.mysticismDemoV2.managers.MysticismBar
import com.github.juniperfig.mysticismDemoV2.managers.MysticismEffectManager
import com.github.juniperfig.mysticismDemoV2.managers.MysticismTracker
import com.github.juniperfig.mysticismDemoV2.services.MessageService
import com.github.juniperfig.mysticismDemoV2.services.MysticismDrainService
import com.github.juniperfig.mysticismDemoV2.services.MysticismGainService
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.event.HandlerList
import org.bukkit.plugin.java.JavaPlugin

/**
 * ## Mysticism Lifecycle Manager
 *
 * This class is the central orchestrator for the MysticismDemoV2 plugin's internal components.
 * It is responsible for:
 * - Loading and reloading the [PluginConfig].
 * - Initializing and managing the lifecycle of all other managers and services (e.g., [FlightManager], [MysticismTracker]).
 * - Handling dependency injection between components.
 * - Registering and unregistering Bukkit [org.bukkit.event.Listener]s and [CommandExecutor]s.
 * - Providing controlled access to core components for commands or other parts of the plugin.
 *
 * It centralizes the plugin's setup, reload, and cleanup logic, keeping the main [JavaPlugin] class clean.
 */
class MysticismLifecycleManager(private val plugin: JavaPlugin) {

    // Properties for all managers and services
    private lateinit var pluginConfig: PluginConfig
    private lateinit var messageService: MessageService
    private lateinit var mysticismBar: MysticismBar
    private lateinit var mysticismTracker: MysticismTracker
    private lateinit var mysticismDrainService: MysticismDrainService
    private lateinit var mysticismEffectManager: MysticismEffectManager
    private lateinit var flightManager: FlightManager // Note: FlightManager will also implement FlightController
    private lateinit var mysticismGainService: MysticismGainService

    // Store references to registered listeners and command executors that might change
    // on reload, so we can unregister them properly.
    private var registeredPotionListener: PotionListener? = null
    private var registeredFlightManagerListener: FlightManager? = null // Store specific instance

    /**
     * Initializes or re-initializes all plugin components based on the current configuration.
     * This method should be called on plugin enable and whenever the configuration is reloaded.
     *
     * It handles unregistering old listener instances and registering new ones to ensure clean reloads.
     *
     * @param sender The [CommandSender] who initiated the reload, or `null` if called on plugin startup.
     */
    fun initializeAndRegisterComponents(sender: CommandSender?) {
        plugin.logger.info("Initializing/Reloading MysticismDemoV2 components...")
        // Hide and remove boss bars from the PREVIOUS MysticismBar instance if it exists.
        if (::mysticismBar.isInitialized) {
            mysticismBar.hideAllMysticismBars()
        }
        // Step 1: Unregister any existing listeners before creating new instances.
        // allows for a clean reload to prevent duplicate event handling.
        registeredPotionListener?.let { HandlerList.unregisterAll(it) }
        registeredFlightManagerListener?.let { HandlerList.unregisterAll(it) }

        // Step 2: Load the latest configuration from disk
        val configLoader = ConfigLoader(plugin)
        pluginConfig = configLoader.loadConfig() // Load the NEW PluginConfig instance

        // Step 3: Initialize/Re-initialize core services that don't depend on config values
        // (or have config values passed directly to their methods, like MysticismGainService)
        messageService = MessageService()
        mysticismBar = MysticismBar()
        mysticismTracker = MysticismTracker()
        mysticismDrainService = MysticismDrainService(plugin, mysticismTracker)
        mysticismGainService = MysticismGainService(mysticismTracker, messageService)


        // Step 4: Initialize/Re-initialize managers that directly depend on pluginConfig
        mysticismEffectManager = MysticismEffectManager(plugin,
            null,
            mysticismBar,
            messageService,
            pluginConfig)

        flightManager = FlightManager(
            plugin,
            mysticismTracker,
            mysticismDrainService,
            messageService,
            pluginConfig
        )

        // Step 5: Set up cross-manager dependencies (delayed injection for circular dependencies)
        mysticismEffectManager.setFlightManager(flightManager)

        // Step 6: Set the MysticismTracker's effect change listener.
        // This callback is crucial: any change to mysticism in MysticismTracker will flow through MysticismEffectManager.
        mysticismTracker.setEffectChangeListener { player, newLevel ->
            mysticismEffectManager.onMysticismLevelChange(player, newLevel)
        }

        // Step 7: Register CommandExecutors and Listeners whose instances change on reload.
        // need to register the NEW instances created above!

        // Register FlightManager as a Listener
        plugin.server.pluginManager.registerEvents(flightManager, plugin)
        registeredFlightManagerListener = flightManager // Store reference to unregister on next reload

        val newPotionListener = PotionListener(mysticismGainService, pluginConfig)
        plugin.server.pluginManager.registerEvents(newPotionListener, plugin)
        registeredPotionListener = newPotionListener // Store reference to unregister on next reload

        // If a sender initiated the reload, send them a success message
        sender?.sendMessage(Component.text("MysticismDemoV2 configuration reloaded successfully!", NamedTextColor.GREEN))
        plugin.logger.info("MysticismDemoV2 components reloaded/initialized successfully!")
    }

    /**
     * Gets the current instance of the MysticismTracker.
     * This is needed by commands like SetMystCommand and CheckMystCommand.
     */
    fun getMysticismTracker(): MysticismTracker = mysticismTracker

    /**
     * Gets the current instance of the MessageService.
     * This is needed by various commands and services.
     */
    fun getMessageService(): MessageService = messageService

    /**
     * Gets the current instance of the MysticismDrainService.
     * This is needed by PlayerQuitListener.
     */
    fun getMysticismDrainService(): MysticismDrainService = mysticismDrainService

    /**
     * Gets the current instance of the MysticismBar.
     * This is needed by PlayerJoinListener.
     */
    fun getMysticismBar(): MysticismBar = mysticismBar

    /**
     * Stops all running mysticism drain tasks.
     * Called on plugin disable.
     */
    fun stopAllDrains() {
        if (::mysticismDrainService.isInitialized) {
            mysticismDrainService.stopAllDrains()
        }
    }

    /**
     * Hides all active mysticism BossBars.
     * Called on plugin disable.
     */
    fun hideAllMysticismBars() {
        if (::mysticismBar.isInitialized) {
            mysticismBar.hideAllMysticismBars()
        }
    }
}