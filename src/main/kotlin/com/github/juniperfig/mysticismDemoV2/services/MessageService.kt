package com.github.juniperfig.mysticismDemoV2.services

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * A service for sending various user-facing messages in the plugin.
 * Centralizes messaging logic for easier modification and consistency.
 */

object MessageService {

    // --- Mysticism Related Messages ---

    fun sendMysticismSurge(player: Player) {
        player.sendMessage(Component.text("You feel a surge of mysticism...", NamedTextColor.LIGHT_PURPLE))
    }

    // --- Command Related Messages (for /setmyst) ---

    fun sendSetMysticismSuccess(sender: CommandSender, targetName: String, value: Double) {
        sender.sendMessage(Component.text("Mysticism for $targetName set to $value", NamedTextColor.GREEN))
    }

    fun sendSetMysticismTargetMessage(player: Player, value: Double) {
        player.sendMessage(Component.text("Your mysticism level has been set to $value", NamedTextColor.AQUA))
    }

    fun sendCommandUsage(sender: CommandSender, usage: String) {
        sender.sendMessage(Component.text("Usage: $usage", NamedTextColor.YELLOW))
    }

    fun sendPlayerNotFound(sender: CommandSender) {
        sender.sendMessage(Component.text("Player not found or offline.", NamedTextColor.RED))
    }

    fun sendInvalidNumber(sender: CommandSender, input: String) {
        sender.sendMessage(Component.text("Invalid number: $input", NamedTextColor.RED))
    }

    fun sendValueOutOfRange(sender: CommandSender) {
        sender.sendMessage(Component.text("Value must be between 0.0 and 1.0.", NamedTextColor.RED))
    }

    fun sendNoPermission(sender: CommandSender, permission: String) {
        sender.sendMessage(Component.text("You don't have permission ($permission) to use this command.", NamedTextColor.RED))
    }

    /**
     * Creates a message component to display a player's mysticism level.
     *
     * @param playerName The name of the player whose mysticism is being checked.
     * @param level The current mysticism level (0.0 - 1.0).
     * @return A Component ready to be sent to a CommandSender.
     */
    fun checkMysticismMessage(playerName: String, level: Double): Component {
        val formattedLevel = String.format("%.2f", level) // Format to 2 decimal places
        return Component.text()
            .append(Component.text("Mysticism for ", NamedTextColor.GOLD))
            .append(Component.text(playerName, NamedTextColor.YELLOW, TextDecoration.BOLD))
            .append(Component.text(": ", NamedTextColor.GOLD))
            .append(Component.text(formattedLevel, NamedTextColor.AQUA))
            .build()
    }

    /**
     * Sends a simple colored message to a CommandSender.
     *
     * @param sender The recipient of the message (Player or ConsoleCommandSender).
     * @param message The string message to send.
     * @param color The NamedTextColor for the message (defaulting to NamedTextColor.RED for general info/error).
     */
    fun sendMessage(sender: CommandSender, message: String, color: NamedTextColor = NamedTextColor.RED) {
        sender.sendMessage(Component.text(message, color))
    }

    /**
     * Sends a specific message to the player indicating that flight has been enabled.
     *
     * @param player The [Player] who has had flight enabled.
     */
    fun sendFlightEnabled(player: Player) {
        player.sendMessage(Component.text("Flight enabled! Soar through the skies.", NamedTextColor.GREEN))
    }

    /**
     * Sends a specific message to the player indicating that flight has been disabled.
     *
     * @param player The [Player] who has had flight disabled.
     */
    fun sendFlightDisabled(player: Player) {
        player.sendMessage(Component.text("Flight disabled! Welcome back to solid ground.", NamedTextColor.RED))
    }
}