package dev.hytical.listeners

import dev.hytical.AdminGUIPlugin
import dev.hytical.gui.GuiManager
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.event.player.PlayerQuitEvent

class PlayerLoginListener(private val plugin: AdminGUIPlugin) : Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	fun onLogin(event: PlayerLoginEvent) {
		if (!GuiManager.maintenanceMode) return

		val player = event.player
		if (player.isOp || player.hasPermission("admingui.maintenance")) {
			return
		}

		val reason = plugin.messageService.getRaw("prefix") + plugin.messageService.getRaw("message_maintenance")
		event.disallow(PlayerLoginEvent.Result.KICK_OTHER, reason)
	}

	@EventHandler
	fun onQuit(event: PlayerQuitEvent) {
		GuiManager.cleanup(event.player)
	}
}
