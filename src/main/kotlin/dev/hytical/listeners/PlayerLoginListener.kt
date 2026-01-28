package dev.hytical.listeners

import dev.hytical.AdminGUIPlugin
import dev.hytical.gui.GuiManager
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.event.player.PlayerQuitEvent

/**
 * Handles maintenance mode and player cleanup.
 */
class PlayerLoginListener(private val plugin: AdminGUIPlugin) : Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	fun onLogin(event: PlayerLoginEvent) {
		if (!GuiManager.maintenanceMode) return

		val player = event.player

		// Allow operators and players with maintenance permission
		if (player.isOp || player.hasPermission("admingui.maintenance")) {
			return
		}

		// Kick player with maintenance message
		val reason = plugin.messageService.getRaw("prefix") + plugin.messageService.getRaw("message_maintenance")
		event.disallow(PlayerLoginEvent.Result.KICK_OTHER, reason)
	}

	@EventHandler
	fun onQuit(event: PlayerQuitEvent) {
		// Clean up player state
		GuiManager.cleanup(event.player)
	}
}
