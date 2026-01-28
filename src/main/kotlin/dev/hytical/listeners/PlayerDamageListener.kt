package dev.hytical.listeners

import dev.hytical.gui.GuiManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent

/**
 * Handles god mode damage cancellation.
 */
class PlayerDamageListener : Listener {

	@EventHandler
	fun onDamage(event: EntityDamageEvent) {
		val player = event.entity as? org.bukkit.entity.Player ?: return

		// Check god mode status (for legacy servers that don't support isInvulnerable properly)
		if (GuiManager.isGodMode(player)) {
			event.isCancelled = true
		}
	}
}
