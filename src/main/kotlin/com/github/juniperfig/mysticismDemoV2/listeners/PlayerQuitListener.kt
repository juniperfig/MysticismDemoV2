// src/main/kotlin/com/github/juniperfig/mysticismDemoV2/listeners/PlayerQuitListener.kt

package com.github.juniperfig.mysticismDemoV2.listeners

import com.github.juniperfig.mysticismDemoV2.managers.MysticismTracker
import com.github.juniperfig.mysticismDemoV2.services.MysticismDrainService

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

/**
 * ## Player Quit Listener
 *
 * This listener handles events related to players disconnecting from the server.
 * Its primary responsibility is to ensure proper cleanup of player-specific data
 * and ongoing tasks when a player logs out.
 *
 * @property mysticismDrainService The [MysticismDrainService] instance, used to stop any active drain tasks for the quitting player.
 * @property mysticismTracker The [MysticismTracker] instance, used to remove the player's mysticism data.
 */
class PlayerQuitListener(
    private val mysticismDrainService: MysticismDrainService,
    private val mysticismTracker: MysticismTracker
) : Listener {

    /**
     * Handles the [PlayerQuitEvent], triggered when a player disconnects.
     *
     * This method performs the following cleanup operations:
     * - Clears *all* active mysticism drain sources for the quitting player, stopping their drain task.
     * - Removes the player's mysticism data from the [MysticismTracker].
     *
     * @param event The [PlayerQuitEvent] that occurred.
     */
    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player
        // Now, we use the robust method to clear ALL drain sources for the player.
        mysticismDrainService.clearAllDrainSourcesForPlayer(player.uniqueId)
        mysticismTracker.removeMysticism(player.uniqueId) // Remove player's data from the tracker
        // BossBar cleanup for a specific player on quit is implicitly handled if the level hits 0,
        // and globally by `mysticismBar.hideAllMysticismBars()` on plugin disable.
    }
}