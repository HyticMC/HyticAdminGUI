package dev.hytical.commands

import dev.hytical.AdminGUIPlugin
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class AdminReloadCommand(
	private val plugin: AdminGUIPlugin
) : CommandExecutor {
	override fun onCommand(
		sender: CommandSender,
		cmd: Command,
		label: String,
		args: Array<out String>
	): Boolean {
		if(sender is Player) {
			if (!sender.hasPermission("admingui.admin")) {
				plugin.messageService.send(sender, "permission")
				return true
			}
		}

		plugin.messageService.reloadLanguage()
		plugin.messageService.send(sender, "config_reloaded")

		return true
	}

}