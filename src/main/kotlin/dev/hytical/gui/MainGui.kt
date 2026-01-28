package dev.hytical.gui

import com.cryptomorin.xseries.XMaterial
import dev.hytical.AdminGUIPlugin
import dev.hytical.services.MessageService
import dev.triumphteam.gui.builder.item.ItemBuilder
import dev.triumphteam.gui.guis.Gui
import dev.triumphteam.gui.guis.GuiItem
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * Main Admin GUI menu.
 */
class MainGui(
	private val plugin: AdminGUIPlugin,
	private val messageService: MessageService
) {

	fun open(player: Player) {
		val gui = Gui.gui()
			.title(messageService.getTitle("inventory_main"))
			.rows(3)
			.disableAllInteractions()
			.create()

		// Fill background
		val filler = createItem(XMaterial.LIGHT_BLUE_STAINED_GLASS_PANE, " ")
		for (i in 0 until 27) {
			gui.setItem(i, filler)
		}

		// Player head - slot 12 (0-indexed: 11)
		GuiManager.setTarget(player, player)
		val playerTitle = messageService.getRaw("main_player").replace("{player}", player.name)
		val playerHead = ItemBuilder.skull()
			.owner(player)
			.name(messageService.deserialize(playerTitle))
			.asGuiItem { event ->
				event.isCancelled = true
				PlayerGui(plugin, messageService).open(player)
			}
		gui.setItem(11, playerHead)

		// World - slot 14 (0-indexed: 13)
		val worldItem = createClickableItem(
			XMaterial.GRASS_BLOCK,
			messageService.getRaw("main_world")
		) {
			WorldGui(plugin, messageService).open(player)
		}
		gui.setItem(13, worldItem)

		// Players - slot 16 (0-indexed: 15)
		val randomPlayer = Bukkit.getOnlinePlayers().firstOrNull() ?: player
		val playersHead = ItemBuilder.skull()
			.owner(randomPlayer)
			.name(messageService.deserialize(messageService.getRaw("main_players")))
			.asGuiItem {
				PlayersListGui(plugin, messageService).open(player)
			}
		gui.setItem(15, playersHead)

		// Maintenance mode - slot 19 (0-indexed: 18)
		val maintenanceMaterial = if (GuiManager.maintenanceMode) {
			XMaterial.GLOWSTONE_DUST
		} else {
			XMaterial.REDSTONE
		}
		val maintenanceItem = createClickableItem(
			maintenanceMaterial,
			messageService.getRaw("main_maintenance_mode")
		) {
			if (player.hasPermission("admingui.maintenance.manage")) {
				GuiManager.maintenanceMode = !GuiManager.maintenanceMode
				if (GuiManager.maintenanceMode) {
					messageService.send(player, "message_maintenance_enabled")
					// Kick non-exempt players
					Bukkit.getOnlinePlayers()
						.filter { !it.isOp && !it.hasPermission("admingui.maintenance") }
						.forEach { it.kickPlayer(messageService.getRaw("prefix") + messageService.getRaw("message_maintenance")) }
				} else {
					messageService.send(player, "message_maintenance_disabled")
				}
				player.closeInventory()
			} else {
				messageService.send(player, "permission")
				player.closeInventory()
			}
		}
		gui.setItem(18, maintenanceItem)

		// Quit - slot 27 (0-indexed: 26)
		val quitItem = createClickableItem(
			XMaterial.REDSTONE_BLOCK,
			messageService.getRaw("main_quit")
		) {
			player.closeInventory()
		}
		gui.setItem(26, quitItem)

		gui.open(player)
	}

	private fun createItem(material: XMaterial, name: String): GuiItem {
		val item = material.parseItem() ?: ItemStack(org.bukkit.Material.STONE)
		return ItemBuilder.from(item)
			.name(messageService.deserialize(name))
			.asGuiItem { it.isCancelled = true }
	}

	private fun createClickableItem(
		material: XMaterial,
		name: String,
		onClick: () -> Unit
	): GuiItem {
		val item = material.parseItem() ?: ItemStack(org.bukkit.Material.STONE)
		return ItemBuilder.from(item)
			.name(messageService.deserialize(name))
			.asGuiItem {
				it.isCancelled = true
				onClick()
			}
	}
}
