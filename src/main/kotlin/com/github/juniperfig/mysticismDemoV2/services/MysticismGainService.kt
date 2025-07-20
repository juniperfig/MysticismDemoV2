package com.github.juniperfig.mysticismDemoV2.services

import com.github.juniperfig.mysticismDemoV2.managers.MysticismTracker
import org.bukkit.entity.Player

/**
 * ## Mysticism Gain Service
 *
 * This service is responsible for handling the application of "mysticism charge" effects to players.
 * It acts as a central point for increasing a player's mysticism level, ensuring that all
 * necessary updates and notifications occur when mysticism is gained (e.g., from consuming a potion).
 */
object MysticismGainService {

    /**
     * Increases a player's mysticism level.
     * The new mysticism level is automatically clamped between 0.0 and the specified cap (defaulting to 1.0).
     *
     * This method leverages the [MysticismTracker]'s `setMysticism` method, which in turn
     * triggers the `onMysticismChange` callback defined in the main plugin class (`MysticismDemoV2`).
     * This callback then orchestrates other effects, such as updating the mysticism BossBar
     * and managing flight abilities via the [com.github.juniperfig.mysticismDemoV2.managers.MysticismEffectManager].
     *
     * @param player The [Player] to whom the mysticism charge will be applied.
     * @param amount The `Double` value representing the amount of mysticism to add to the player's current level.
     * @param cap The `Double` value representing the maximum mysticism level a player can have. Defaults to 1.0.
     */
    fun gainMysticism(player: Player, amount: Double, cap: Double = 1.0) {
        val uuid = player.uniqueId
        // Get the player's current mysticism level.
        val current = MysticismTracker.getMysticism(uuid)
        // Calculate the new mysticism level, ensuring it does not exceed the specified cap.
        val newLevel = (current + amount).coerceAtMost(cap)
        MysticismTracker.setMysticism(uuid, newLevel)
        // Send a message to the player indicating they felt a surge of mysticism.
        MessageService.sendMysticismSurge(player)
    }
}