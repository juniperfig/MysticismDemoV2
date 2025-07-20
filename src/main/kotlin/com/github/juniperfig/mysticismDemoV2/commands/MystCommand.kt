package com.github.juniperfig.mysticismDemoV2.commands

import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.command.CommandSender

interface MystSubCommand {

    fun onCommand(sourceStack: CommandSourceStack, label: String, vararg args: String)

    fun hasPermission(sender: CommandSender): Boolean = permission?.let { sender.hasPermission(it) } == true

    val permission: String?
}