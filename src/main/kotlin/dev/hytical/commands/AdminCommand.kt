package dev.hytical.commands

import dev.hytical.AdminGUIPlugin
import dev.hytical.gui.GuiManager
import dev.hytical.gui.MainGui
import dev.hytical.gui.PlayerSettingsGui
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

/**
 * Main /admin command executor.
 */
class AdminCommand(private val plugin: AdminGUIPlugin) : CommandExecutor, TabCompleter {

	override fun onCommand(
		sender: CommandSender,
		command: Command,
		label: String,
		args: Array<out String>
	): Boolean {
		if (sender !is Player) {
			sender.sendMessage("This command can only be used by players.")
			return true
		}

		if (!sender.hasPermission("admingui.admin")) {
			plugin.messageService.send(sender, "permission")
			return true
		}

		when (args.size) {
			0 -> {
				// Open main GUI for self
				GuiManager.setTarget(sender, sender)
				MainGui(plugin, plugin.messageService).open(sender)
			}

			1 -> {
				// Open target player GUI
				val targetName = args[0]
				val target = Bukkit.getPlayer(targetName)

				if (target == null || !target.isOnline) {
					plugin.messageService.send(
						sender, "is_not_a_player",
						plugin.messageService.playerPlaceholder("player", targetName)
					)
					return true
				}

				GuiManager.setTarget(sender, target)
				if (sender.name == target.name) {
					MainGui(plugin, plugin.messageService).open(sender)
				} else {
					PlayerSettingsGui(plugin, plugin.messageService).open(sender, target)
				}
			}

			else -> {
				plugin.messageService.send(sender, "wrong_arguments")
			}
		}

		return true
	}

	override fun onTabComplete(
		sender: CommandSender,
		command: Command,
		alias: String,
		args: Array<out String>
	): List<String> {
		if (args.size == 1) {
			val prefix = args[0].lowercase()
			return Bukkit.getOnlinePlayers()
				.map { it.name }
				.filter { it.lowercase().startsWith(prefix) }
		}
		return emptyList()
	}
}
