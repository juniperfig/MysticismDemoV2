// src/main/kotlin/com/github/juniperfig/mysticismDemoV2/managers/MysticismEffectManager.kt

package com.github.juniperfig.mysticismDemoV2.managers

import com.github.juniperfig.mysticismDemoV2.config.PluginConfig
import com.github.juniperfig.mysticismDemoV2.services.MessageService
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

/**
 * ## Mysticism Effect Manager
 *
 * This manager is responsible for applying and coordinating various in-game effects
 * that are directly tied to a player's mysticism level.
 * It primarily handles:
 * - Updating the visual [MysticismBar] based on current mysticism levels.
 * - Managing player flight status (enabling if positive mysticism, disabling if zero/negative).
 *
 * The BossBar's visibility is now solely determined by whether a player has
 * any mysticism (level > 0) or no mysticism (level <= 0).
 *
 * @property plugin The main [Plugin] instance, used for logging.
 * @property flightManager The [FlightManager] instance, used to control player flight abilities.
 * @property mysticismBar The [MysticismBar] instance, used to display, hide, and update the BossBar.
 * @property messageService The [MessageService] instance, used to send messages to players.
 * @property pluginConfig The [PluginConfig] instance, providing access to all configurable values.
 */
class MysticismEffectManager(
    private val plugin: Plugin,
    @Volatile private var flightManager: FlightManager? = null,
    private val mysticismBar: MysticismBar,
    private val messageService: MessageService,
    private val pluginConfig: PluginConfig
) {
    /**
     * Sets the [FlightManager] instance for this effect manager.
     * This method is called during plugin initialization to resolve a potential circular dependency,
     * ensuring [FlightManager] is fully constructed before it's referenced here.
     *
     * @param manager The initialized [FlightManager] instance.
     */
    fun setFlightManager(manager: FlightManager) {
        this.flightManager = manager
        plugin.logger.info("EFFECT: FlightManager has been set in MysticismEffectManager.")
    }

    /**
     * Callback method invoked by the [MysticismTracker] whenever a player's mysticism level changes.
     * This method is the central point for reacting to mysticism level updates, handling:
     * - Updating the [MysticismBar]'s progress and visibility based on the new level.
     * - Enabling player flight if their mysticism is positive.
     * - Forcing a player to land if their mysticism drops to 0 or below while flying.
     *
     * @param player The [Player] whose mysticism level has changed.
     * @param newLevel The new mysticism level [Double] for the player (clamped between 0.0 and 1.0).
     */
    fun onMysticismLevelChange(player: Player, newLevel: Double) {
        plugin.logger.info("MYST_EFFECT: Mysticism for ${player.name} changed to $newLevel. FlightManager instance is: ${flightManager != null}")
        plugin.logger.info("MYST_EFFECT_DEBUG: === Start onMysticismLevelChange for ${player.name} ===")
        plugin.logger.info("MYST_EFFECT_DEBUG: Current newLevel for ${player.name}: $newLevel")
        plugin.logger.info("MYST_EFFECT_DEBUG: Configured minMysticismForBar: ${pluginConfig.minMysticismForBar}")
        plugin.logger.info("MYST_EFFECT_DEBUG: Configured drainRateFlight: ${pluginConfig.drainRateFlight}")

        mysticismBar.updateMysticismBar(player, newLevel)

        //Bar visibility based on minMysticismForBar from config
        if (newLevel >= pluginConfig.minMysticismForBar) {
            mysticismBar.showMysticismBar(player)
        } else { // newLevel < minMysticismForBar
            mysticismBar.hideMysticismBar(player)
        }

        // Flight ability is tied to drainRateFlight from config
        if (newLevel >= pluginConfig.drainRateFlight) {
            flightManager?.let { fm ->
                fm.setFlightEnabled(player, true)
                plugin.logger.info("MYST_EFFECT: Calling setFlightEnabled(true) for ${player.name} (Mysticism >= drainRateFlight).")
            } ?: plugin.logger.warning("MYST_EFFECT: FlightManager is null when trying to enable flight for ${player.name}!")
        } else {
            flightManager?.let { fm ->
                fm.setFlightEnabled(player, false)
                if (player.isFlying) {
                    messageService.sendMessage(player, "You have run out of mysticism and can no longer fly!")
                }
                plugin.logger.info("MYST_EFFECT: Calling setFlightEnabled(false) for ${player.name} (Mysticism < drainRateFlight).")
            } ?: plugin.logger.warning("MYST_EFFECT: FlightManager is null when trying to disable flight for ${player.name}!")
        }
    }
}