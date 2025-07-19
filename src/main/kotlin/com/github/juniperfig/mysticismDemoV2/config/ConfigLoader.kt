package com.github.juniperfig.mysticismDemoV2.config

import org.bukkit.plugin.Plugin

class ConfigLoader(private val plugin: Plugin) {
    fun loadConfig(): PluginConfig {
        plugin.saveDefaultConfig()

        val drainRateFlight = plugin.config.getDouble("mysticism.drainRateFlight", 0.05)
        val chargeAmountPotion = plugin.config.getDouble("mysticism.chargeAmountPotion", 0.5)
        val minMysticismForBar = plugin.config.getDouble("barVisibility.minMysticismForBar", 0.05)

        return PluginConfig(drainRateFlight, chargeAmountPotion, minMysticismForBar)
    }
}