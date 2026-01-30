package dev.hytical.commands

import dev.hytical.ServiceContext
import dev.hytical.gui.GuiManager
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class AdminCommand(private val ctx: ServiceContext) : CommandExecutor, TabCompleter {

	private val messageService get() = ctx.messageService

	override fun onCommand(
		sender: CommandSender,
		command: Command,
		label: String,
		args: Array<out String>
	): Boolean {
		if (sender !is Player) {
			messageService.send(sender, "only_player_can_use_this_command")
			return true
		}

		if (!sender.hasPermission("admingui.admin")) {
			messageService.send(sender, "permission")
			return true
		}

		when (args.size) {
			0 -> {
				GuiManager.setTarget(sender, sender)
				ctx.createMainGui().open(sender)
			}

			1 -> {
				val targetName = args[0]
				val target = Bukkit.getPlayer(targetName)

				if (target == null || !target.isOnline) {
					messageService.send(sender, "is_not_a_player", messageService.playerPlaceholder("player", targetName))
					return true
				}

				GuiManager.setTarget(sender, target)
				if (sender.name == target.name) {
					ctx.createMainGui().open(sender)
				} else {
					ctx.createPlayerSettingsGui().open(sender, target)
				}
			}

			else -> messageService.send(sender, "wrong_arguments")
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
