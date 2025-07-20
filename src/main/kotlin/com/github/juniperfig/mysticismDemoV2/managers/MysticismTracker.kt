// src/main/kotlin/com/github/juniperfig/mysticismDemoV2/managers/MysticismTracker.kt

package com.github.juniperfig.mysticismDemoV2.managers

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.max
import kotlin.math.min

/**
 * ## Mysticism Tracker
 *
 * This class is responsible for storing and managing the mysticism levels for all online players.
 * It acts as the central data repository for mysticism, providing methods to get, set, add,
 * and remove mysticism values.
 *
 * It also notifies registered listeners (via a callback) whenever a player's mysticism level changes,
 * allowing other parts of the plugin (like [MysticismEffectManager]) to react to these changes.
 *
 * @property mysticismLevels A [ConcurrentHashMap] where the key is the player's [UUID]
 * and the value is their current mysticism level ([Double]). This map is thread-safe.
 */
object MysticismTracker {
    private val mysticismLevels: ConcurrentHashMap<UUID, Double> = ConcurrentHashMap()

    /**
     * The maximum mysticism level a player can have.
     * Mysticism levels are clamped between 0.0 and this maximum.
     */
    private const val maxMysticismLevel = 1.0 // Mysticism is represented as a percentage (0.0 to 1.0)

    /**
     * A callback function that will be invoked whenever a player's mysticism level changes.
     * The lambda takes a [Player] object and the [Double] new mysticism level as arguments.
     * This allows other managers (e.g., [MysticismEffectManager]) to react to mysticism changes.
     * It is set externally via [setEffectChangeListener].
     */
    private var onMysticismChange: ((Player, Double) -> Unit)? = null

    /**
     * Registers a listener to be notified when a player's mysticism level changes.
     * This method is typically called once during plugin initialization to set up the callback.
     *
     * @param listener The lambda function to be called, taking the [Player] and new mysticism [Double] level.
     */
    fun setEffectChangeListener(listener: (Player, Double) -> Unit) {
        this.onMysticismChange = listener
    }

    /**
     * Retrieves the current mysticism level for a specific player.
     *
     * @param playerUuid The [UUID] of the player.
     * @return The player's mysticism level as a [Double], or 0.0 if the player has no recorded mysticism.
     */
    fun getMysticism(playerUuid: UUID): Double {
        return mysticismLevels[playerUuid] ?: 0.0
    }

    /**
     * Sets a player's mysticism level to a specific value. The value is clamped
     * between 0.0 and [maxMysticismLevel].
     *
     * After updating the level, it notifies any registered listeners about the change.
     *
     * @param playerUuid The [UUID] of the player.
     * @param amount The new mysticism level to set.
     */
    fun setMysticism(playerUuid: UUID, amount: Double) {
        val clampedAmount = max(0.0, min(amount, maxMysticismLevel))
        mysticismLevels[playerUuid] = clampedAmount
        // Notify the listener about the change if it's set
        val player = Bukkit.getPlayer(playerUuid)
        if (player != null && player.isOnline) {
            onMysticismChange?.invoke(player, clampedAmount) // Invoke the callback
        }
    }

    /**
     * Adds a specified amount to a player's current mysticism level.
     * The resulting level is clamped between 0.0 and [maxMysticismLevel].
     *
     * This method is typically used for gaining mysticism.
     * After updating the level, it notifies any registered listeners about the change.
     *
     * @param playerUuid The [UUID] of the player.
     * @param amount The amount of mysticism to add. Can be negative to subtract.
     */
    fun addMysticism(playerUuid: UUID, amount: Double) {
        val current = getMysticism(playerUuid)
        setMysticism(playerUuid, current + amount) // Use setMysticism to handle clamping and notification
    }

    /**
     * Removes a player's mysticism data from the tracker.
     * This is typically called when a player disconnects to clean up resources.
     *
     * @param playerUuid The [UUID] of the player to remove.
     */
    fun removeMysticism(playerUuid: UUID) {
        mysticismLevels.remove(playerUuid)
        // When a player is removed, also explicitly notify that their mysticism is effectively 0 for cleanup
        // (e.g., hide bar if it was still showing)
        val player = Bukkit.getPlayer(playerUuid)
        if (player != null && player.isOnline) { // Only notify if still online, otherwise it's handled by plugin disable/player quit cleanup
            onMysticismChange?.invoke(player, 0.0) // Inform listeners that mysticism is now 0 for this player
        }
    }
}