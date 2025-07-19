package com.github.juniperfig.mysticismDemoV2.interfaces

import org.bukkit.entity.Player

interface FlightController {
    /**
     * Enables or disables the player's ability to fly.
     * This controls player.allowFlight and player.isFlying.
     * It does NOT directly manage mysticism drain; that is handled by FlightManager reacting to PlayerToggleFlightEvent.
     */
    fun setFlightEnabled(player: Player, enabled: Boolean)
}