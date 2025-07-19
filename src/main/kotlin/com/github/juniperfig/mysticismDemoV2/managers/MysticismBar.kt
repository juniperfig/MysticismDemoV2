// src/main/kotlin/com/github/juniperfig/mysticismDemoV2/managers/MysticismBar.kt

package com.github.juniperfig.mysticismDemoV2.managers

import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * ## Mysticism BossBar Manager
 *
 * This class manages the display of a custom BossBar for each player to visualize their mysticism level.
 * It handles the creation, updating, showing, and hiding of these player-specific BossBars.
 *
 * @property bossBars A [ConcurrentHashMap] that stores the [BossBar] instance for each player.
 * The key is the player's [UUID], and the value is their associated [BossBar].
 */
class MysticismBar {
    private val bossBars: ConcurrentHashMap<UUID, BossBar> = ConcurrentHashMap()

    /**
     * Retrieves or creates a [BossBar] for a given player.
     * If a BossBar already exists for the player, it is returned.
     * Otherwise, a new BossBar is created with default settings and stored.
     *
     * @param player The [Player] for whom to get or create the BossBar.
     * @return The [BossBar] instance for the player.
     */
    private fun getOrCreateBossBar(player: Player): BossBar {
        return bossBars.computeIfAbsent(player.uniqueId) {
            val newBar = org.bukkit.Bukkit.createBossBar("Mysticism: 0.0%", BarColor.PURPLE, BarStyle.SOLID)
            newBar.addPlayer(player) // Add player to the new bar immediately
            newBar.progress = 0.0 // Set initial progress
            newBar.isVisible = false // Initially hidden, to be shown by MysticismEffectManager
            newBar // Return the new bar
        }
    }

    /**
     * Shows the mysticism BossBar to the specified player.
     * If the player does not yet have a BossBar, one will be created.
     *
     * @param player The [Player] to whom the BossBar should be shown.
     */
    fun showMysticismBar(player: Player) {
        val bar = getOrCreateBossBar(player)
        if (!bar.isVisible) {
            bar.isVisible = true
            // Ensure the bar's progress is updated when shown, maybe from current mysticism
            // (This is primarily handled by MysticismEffectManager's onMysticismLevelChange now)
            // But good to have a default here in case.
            // If you track mysticism in MysticismBar, you could use player's current value.
            // For now, rely on MysticismEffectManager to push updates.
        }
    }

    /**
     * Hides the mysticism BossBar from the specified player.
     *
     * @param player The [Player] from whom the BossBar should be hidden.
     */
    fun hideMysticismBar(player: Player) {
        bossBars[player.uniqueId]?.isVisible = false
    }

    /**
     * Updates the progress of a player's mysticism BossBar.
     *
     * @param player The [Player] whose BossBar progress is to be updated.
     * @param progress The new progress value (0.0 to 1.0) for the BossBar.
     */
    fun updateMysticismBar(player: Player, progress: Double) {
        val bar = getOrCreateBossBar(player)
        // Clamping the progress to ensure it stays within 0.0 and 1.0
        val clampedProgress = progress.coerceIn(0.0, 1.0)
        bar.progress = clampedProgress
        bar.setTitle("Mysticism: ${"%.1f".format(clampedProgress * 100)}%") // Format to one decimal place percentage
    }

    /**
     * Hides and removes all active mysticism BossBars.
     * This is typically called when the plugin is disabled to clean up resources.
     */
    fun hideAllMysticismBars() {
        bossBars.values.forEach { bossBar -> // 'bossBar' here IS an individual BossBar instance
            bossBar.removeAll()      // Remove all players from this specific BossBar instance
            bossBar.isVisible = false // Set it to not visible
        }
        bossBars.clear()
    }
}