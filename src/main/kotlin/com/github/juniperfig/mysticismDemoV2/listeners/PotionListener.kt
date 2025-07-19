package com.github.juniperfig.mysticismDemoV2.listeners

import com.github.juniperfig.mysticismDemoV2.config.PluginConfig
import com.github.juniperfig.mysticismDemoV2.services.MysticismGainService
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PotionSplashEvent

class PotionListener(
    private val mysticismGainService: MysticismGainService,
    private val pluginConfig: PluginConfig
) : Listener {

    @EventHandler
    fun onPotionSplash(event: PotionSplashEvent) { //this needs to be updated later to only work with a specific potion but it's fine for now
        for (entity in event.affectedEntities) {
            if (entity is Player) {
                mysticismGainService.gainMysticism(entity, pluginConfig.chargeAmountPotion)
            }
        }
    }
}