package dev.hytical.listeners

import dev.hytical.AdminGUIPlugin
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

/**
 * Handles update notifications on player join.
 */
class PlayerJoinListener(private val plugin: AdminGUIPlugin) : Listener {

	@EventHandler
	fun onJoin(event: PlayerJoinEvent) {
		val player = event.player

		// Check for updates if player has admin permission
		if (player.hasPermission("admingui.admin") && plugin.newVersion != null) {
			plugin.messageService.sendRaw(
				player,
				"<gray>[<red>AdminGUI<gray>] <yellow>A new version (<green>${plugin.newVersion}<yellow>) is available!"
			)
		}
	}
}
