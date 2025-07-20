package com.github.juniperfig.mysticismDemoV2.services

import com.github.juniperfig.mysticismDemoV2.MysticismDemoV2.Companion.plugin
import com.github.juniperfig.mysticismDemoV2.managers.MysticismTracker
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * ## Mysticism Drain Service
 *
 * This service manages the continuous draining of mysticism from players based on active "drain sources".
 * Abilities or effects (like flight) can register themselves as a drain source for a player.
 * The service ensures that mysticism is drained only when there's at least one active source for a player.
 *
 * @property plugin The main [Plugin] instance, used for scheduling Bukkit tasks.
 * @property mysticismTracker The [MysticismTracker] instance, used to get and modify player mysticism levels.
 * @property activeDrainTasks A [ConcurrentHashMap] mapping a player's [UUID] to their active [BukkitTask]
 * responsible for draining mysticism. A task exists only if the player has active drain sources.
 * @property activePlayerDrainSources A [ConcurrentHashMap] mapping a player's [UUID] to another map.
 * The inner map maps a [String] drain source ID (e.g., "flight") to its
 * corresponding drain rate ([Double]). This tracks all active reasons for drain.
 */
object MysticismDrainService {
    // Stores the active drain task for each player
    private val activeDrainTasks: ConcurrentHashMap<UUID, BukkitTask> = ConcurrentHashMap()

    // Stores all active drain sources for each player (e.g., {playerUUID: {"flight": 0.005, "invisibility": 0.002}})
    private val activePlayerDrainSources: ConcurrentHashMap<UUID, ConcurrentHashMap<String, Double>> = ConcurrentHashMap()

    /**
     * Adds a new mysticism drain source for a specific player.
     * If this is the first active drain source for the player, a new drain task is started.
     * If the source already exists, its rate is updated.
     *
     * @param player The [Player] for whom to add the drain source.
     * @param sourceId A unique [String] identifier for this drain source (e.g., "flight", "invisibility_spell").
     * @param drainRate The [Double] amount of mysticism to drain per second for this source.
     */
    fun addDrainSource(player: Player, sourceId: String, drainRate: Double) {
        // Get or create the map of sources for this player
        val playerSources = activePlayerDrainSources.getOrPut(player.uniqueId) { ConcurrentHashMap() }
        playerSources[sourceId] = drainRate // Add or update the source with its rate

        // If a drain task isn't already running for this player, start one
        if (!activeDrainTasks.containsKey(player.uniqueId)) {
            startDrainTask(player)
        }
        plugin.logger.info("DRAIN: Added drain source '$sourceId' for ${player.name} with rate $drainRate. Total active sources: ${playerSources.size}")
    }

    /**
     * Removes an existing mysticism drain source for a specific player.
     * If this was the last active drain source for the player, their drain task is stopped.
     *
     * @param player The [Player] for whom to remove the drain source.
     * @param sourceId The unique [String] identifier of the drain source to remove.
     */
    fun removeDrainSource(player: Player, sourceId: String) {
        val playerSources = activePlayerDrainSources[player.uniqueId]
        if (playerSources != null) {
            playerSources.remove(sourceId) // Remove the specific source

            // If no more sources remain for this player, stop their drain task and clean up
            if (playerSources.isEmpty()) {
                activePlayerDrainSources.remove(player.uniqueId) // Remove player from the map if no sources left
                stopDrainTask(player.uniqueId)
                plugin.logger.info("DRAIN: All drain sources removed for ${player.name}. Drain task stopped.")
            } else {
                plugin.logger.info("DRAIN: Removed drain source '$sourceId' for ${player.name}. Remaining active sources: ${playerSources.size}")
            }
        }
    }

    /**
     * Clears all active mysticism drain sources for a given player and stops their drain task.
     * This method is ideal for player logout or when a player's mysticism abilities are fully reset.
     *
     * @param playerUuid The [UUID] of the player whose drain sources should be cleared.
     */
    fun clearAllDrainSourcesForPlayer(playerUuid: UUID) {
        if (activePlayerDrainSources.containsKey(playerUuid)) {
            activePlayerDrainSources.remove(playerUuid) // Remove all sources for this player
            stopDrainTask(playerUuid) // Stop their ongoing drain task
            plugin.logger.info("DRAIN: Cleared all drain sources and stopped task for player UUID: $playerUuid")
        }
    }

    /**
     * Starts a periodic task to drain mysticism from a player.
     * This task calculates the total drain rate from all active sources for that player.
     *
     * @param player The [Player] for whom to start the drain task.
     */
    private fun startDrainTask(player: Player) {
        // Schedule a repeating task to run every 20 ticks (1 second)
        val task = Bukkit.getScheduler().runTaskTimer(plugin, Runnable {
            // Re-check player online status inside the task to prevent errors if they log out
            val onlinePlayer = Bukkit.getPlayer(player.uniqueId)
            if (onlinePlayer == null || !onlinePlayer.isOnline) {
                // Player logged out, stop this task. PlayerQuitListener should handle cleanup,
                // but this is a safeguard.
                stopDrainTask(player.uniqueId)
                return@Runnable
            }

            val totalDrainRate = activePlayerDrainSources[player.uniqueId]?.values?.sum() ?: 0.0

            if (totalDrainRate > 0.0) {
                // Drain mysticism. The addMysticism method handles clamping at 0.0.
                // We pass a negative value to subtract.
                MysticismTracker.addMysticism(player.uniqueId, -totalDrainRate)
            } else {
                // No more active sources, stop the task.
                // This shouldn't be reached if removeDrainSource is called correctly,
                // but acts as a safeguard.
                stopDrainTask(player.uniqueId)
            }
        }, 0L, 20L) // Start immediately, repeat every 1 second (20 ticks)

        activeDrainTasks[player.uniqueId] = task
        plugin.logger.info("DRAIN: Started drain task for ${player.name}.")
    }

    /**
     * Stops the active mysticism drain task for a specific player.
     *
     * @param playerUuid The [UUID] of the player whose task should be stopped.
     */
    private fun stopDrainTask(playerUuid: UUID) {
        activeDrainTasks.remove(playerUuid)?.cancel()
    }

    /**
     * Stops all active mysticism drain tasks for all players.
     * This method is called during plugin disable to ensure no tasks are left running.
     */
    fun stopAllDrains() {
        activeDrainTasks.values.forEach { it.cancel() }
        activeDrainTasks.clear()
        activePlayerDrainSources.clear() // Also clear all sources
        plugin.logger.info("DRAIN: All mysticism drain tasks stopped and sources cleared.")
    }
}