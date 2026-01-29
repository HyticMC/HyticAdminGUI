package dev.hytical.listeners

import dev.hytical.gui.GuiManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent

class PlayerDamageListener : Listener {

	@EventHandler
	fun onDamage(event: EntityDamageEvent) {
		val player = event.entity as? org.bukkit.entity.Player ?: return
		if (GuiManager.isGodMode(player)) {
			event.isCancelled = true
		}
	}
}
