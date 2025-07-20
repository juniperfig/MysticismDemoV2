package com.github.juniperfig.mysticismDemoV2.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PlayerJoinListener : Listener {
    @EventHandler
    fun onPlayerJoinEvent(event: PlayerJoinEvent) {
        //mysticismBar.showMysticismBar(event.player) //delete this eventually
    }
}