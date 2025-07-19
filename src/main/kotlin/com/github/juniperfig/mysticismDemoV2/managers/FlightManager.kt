// src/main/kotlin/com/github/juniperfig/mysticismDemoV2/managers/FlightManager.kt

package com.github.juniperfig.mysticismDemoV2.managers

import com.github.juniperfig.mysticismDemoV2.config.PluginConfig
import com.github.juniperfig.mysticismDemoV2.interfaces.FlightController
import com.github.juniperfig.mysticismDemoV2.services.MessageService
import com.github.juniperfig.mysticismDemoV2.services.MysticismDrainService
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerGameModeChangeEvent
import org.bukkit.event.player.PlayerToggleFlightEvent
import org.bukkit.plugin.Plugin

/**
 * ## Flight Manager
 *
 * This manager controls player flight ability based on their mysticism levels.
 * It implements the [FlightController] interface to provide a programmatic way to enable/disable flight,
 * and it also acts as a [Listener] to react to players attempting to toggle flight.
 *
 * Key responsibilities include:
 * - Granting or revoking the `player.allowFlight` property.
 * - Starting and stopping the mysticism drain for flight via [MysticismDrainService].
 * - Sending messages to players regarding their flight status or lack of mysticism.
 * - Preventing flight if mysticism is too low or the ability is not granted.
 *
 * @property plugin The main [Plugin] instance, used for logging.
 * @property mysticismTracker The [MysticismTracker] instance, used to check player mysticism levels.
 * @property mysticismDrainService The [MysticismDrainService] instance, used to manage the continuous mysticism drain.
 * @property messageService The [MessageService] instance, used to send various messages to players.
 * @property pluginConfig The [PluginConfig] instance, providing access to all configurable values.
 *
 */
class FlightManager(
    private val plugin: Plugin,
    private val mysticismTracker: MysticismTracker,
    private val mysticismDrainService: MysticismDrainService,
    private val messageService: MessageService,
    private val pluginConfig: PluginConfig
) : FlightController, Listener {

    /**
     * Programmatically sets whether a player has the *ability* to fly (i.e., sets `player.allowFlight`).
     *
     * This method primarily controls the *permission* to fly. It does not directly
     * start or stop the actual mysticism drain; that is handled by the `onPlayerToggleFlight`
     * event handler when the player actively chooses to fly.
     *
     * When enabling flight, it simply grants the ability.
     * When disabling flight, it forces the player to land and removes the flight drain source.
     * The BossBar visibility is managed automatically by [MysticismEffectManager] based on mysticism level.
     *
     * @param player The [Player] whose flight ability is to be changed.
     * @param enabled A [Boolean] flag: `true` to allow flight, `false` to disallow.
     */
    override fun setFlightEnabled(player: Player, enabled: Boolean) {
        plugin.logger.info("FLIGHT_MANAGER: setFlightEnabled called for ${player.name}, enabled: $enabled. Current allowFlight: ${player.allowFlight}, isFlying: ${player.isFlying}")

        if (enabled) {
            if (!player.allowFlight) {
                player.allowFlight = true
                plugin.logger.info("FLIGHT_MANAGER: ${player.name} now has flight ability (allowFlight=true).")
            } else {
                plugin.logger.info("FLIGHT_MANAGER: ${player.name} already has flight ability (allowFlight=true), no change.")
            }
        } else { // Handle disabling the flight ability
            if (player.allowFlight) {
                player.allowFlight = false
                player.isFlying = false // Force the player to land if they were flying.
                mysticismDrainService.removeDrainSource(player, "flight")
                plugin.logger.info("FLIGHT_MANAGER: ${player.name} no longer has flight ability (allowFlight=false).")
            } else {
                plugin.logger.info("FLIGHT_MANAGER: ${player.name} already does not have flight ability (allowFlight=false), no change.")
            }
        }
    }

    /**
     * This is an [EventHandler] that listens for the [PlayerToggleFlightEvent],
     * which fires when a player attempts to start or stop flying.
     *
     * This is the primary method where the mysticism drain for flight is activated or deactivated,
     * and where checks for mysticism levels and game mode are performed.
     *
     * @param event The [PlayerToggleFlightEvent] triggered when a player tries to toggle flight.
     */
    @EventHandler
    fun onPlayerToggleFlight(event: PlayerToggleFlightEvent) {
        val player = event.player
        if (player.gameMode == GameMode.SURVIVAL || player.gameMode == GameMode.ADVENTURE) {
            val currentMysticism = mysticismTracker.getMysticism(player.uniqueId)
            // Access drainRateFlight directly from pluginConfig
            val requiredFlightMysticism = pluginConfig.drainRateFlight // NEW LOCAL VARIABLE FOR CLARITY

            plugin.logger.info("FLIGHT_MANAGER: ${player.name} attempted to toggle flight. isFlying event: ${event.isFlying}, current mysticism: $currentMysticism, player.allowFlight: ${player.allowFlight}.")
            if (event.isFlying) { // Player is attempting to *start* flying
                // MODIFIED: Use requiredFlightMysticism from pluginConfig
                if (currentMysticism >= requiredFlightMysticism && player.allowFlight) {
                    event.isCancelled = false
                    player.isFlying = true
                    // Inform the MysticismDrainService that "flight" is now an active drain source.
                    mysticismDrainService.addDrainSource(player, "flight", requiredFlightMysticism) // Use requiredFlightMysticism
                    messageService.sendFlightEnabled(player)
                    plugin.logger.info("FLIGHT_MANAGER: ${player.name} started flying. Drain source 'flight' added.")
                } else {
                    event.isCancelled = true
                    player.isFlying = false
                    // MODIFIED: Condition and message to reflect requiredFlightMysticism as the minimum
                    if (currentMysticism < requiredFlightMysticism) {
                        messageService.sendMessage(player, "You do not have enough mysticism to fly! (Requires at least ${requiredFlightMysticism * 100}%)")
                    } else if (!player.allowFlight) {
                        messageService.sendMessage(player, "Your flight ability is currently disabled!")
                    }
                    plugin.logger.info("FLIGHT_MANAGER: ${player.name} tried to fly but was prevented (Mysticism: $currentMysticism, AllowFlight: ${player.allowFlight}).")
                }
            } else { // Player is attempting to *stop* flying (e.g., landing)
                // Inform the MysticismDrainService that "flight" is no longer an active drain source.
                // The service will handle stopping the overall drain task if no other sources are active.
                mysticismDrainService.removeDrainSource(player, "flight")
                messageService.sendFlightDisabled(player)
                plugin.logger.info("FLIGHT_MANAGER: ${player.name} stopped flying. Drain source 'flight' removed.")
            }
        }
    }

    /**
     * Handles the [PlayerGameModeChangeEvent] to re-evaluate player flight status.
     * When a player switches into Survival or Adventure mode, their mysticism level
     * is checked, and flight is re-enabled if they have sufficient mysticism.
     *
     * @param event The [PlayerGameModeChangeEvent] that occurred.
     */
    @EventHandler
    fun onPlayerGameModeChange(event: PlayerGameModeChangeEvent) {
        val player = event.player
        val newGameMode = event.newGameMode

        plugin.logger.info("FLIGHT_MANAGER: ${player.name} changed gamemode from ${player.gameMode} to $newGameMode.")

        // Only re-evaluate flight if they are entering a game mode where our custom flight applies.
        if (newGameMode == GameMode.SURVIVAL || newGameMode == GameMode.ADVENTURE) {
            val currentMysticism = mysticismTracker.getMysticism(player.uniqueId)
            plugin.logger.info("FLIGHT_MANAGER: ${player.name} entered $newGameMode. Current mysticism: $currentMysticism.")

            // If the player has any mysticism (greater than 0), we want to allow them to fly.
            // The onPlayerToggleFlight event will enforce the `drainRateFlight` for actual takeoff.
            // Access drainRateFlight directly from pluginConfig for this check
            // MODIFIED: Use pluginConfig.drainRateFlight for the check
            if (currentMysticism >= pluginConfig.drainRateFlight) { // Changed to >= pluginConfig.drainRateFlight for consistency
                setFlightEnabled(player, true)
                messageService.sendMessage(player, "Your mysticism flight ability has been re-evaluated.")
                plugin.logger.info("FLIGHT_MANAGER: ${player.name}'s flight re-enabled due to mysticism after gamemode change.")
            } else {
                // If they have no mysticism or not enough for flight, ensure flight is disabled in these game modes.
                setFlightEnabled(player, false)
                plugin.logger.info("FLIGHT_MANAGER: ${player.name}'s flight disabled due to lack of mysticism after gamemode change.")
            }
        }
    }
}