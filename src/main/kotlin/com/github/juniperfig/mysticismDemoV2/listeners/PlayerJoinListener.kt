// src/main/kotlin/com/GitHub/juniperfig/mysticismDemo/listeners/PlayerJoinListener.kt
package com.github.juniperfig.mysticismDemoV2.listeners

import com.github.juniperfig.mysticismDemoV2.managers.MysticismBar
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PlayerJoinListener(
    private val mysticismBar: MysticismBar
) : Listener {
    @EventHandler
    fun onPlayerJoinEvent(event: PlayerJoinEvent) {
        //mysticismBar.showMysticismBar(event.player) //delete this eventually
    }
}