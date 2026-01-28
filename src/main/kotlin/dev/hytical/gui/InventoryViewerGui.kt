package dev.hytical.gui

import com.cryptomorin.xseries.XMaterial
import dev.hytical.AdminGUIPlugin
import dev.hytical.services.MessageService
import dev.triumphteam.gui.builder.item.ItemBuilder
import dev.triumphteam.gui.guis.Gui
import dev.triumphteam.gui.guis.GuiItem
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * Player inventory viewer GUI.
 */
class InventoryViewerGui(
	private val plugin: AdminGUIPlugin,
	private val messageService: MessageService
) {

	fun open(viewer: Player, target: Player) {
		if (!target.isOnline) {
			messageService.send(viewer, "message_player_not_found")
			viewer.closeInventory()
			return
		}

		val title = messageService.getRaw("inventory_inventory").replace("{player}", target.name)
		val gui = Gui.gui()
			.title(messageService.deserialize(title))
			.rows(6)
			.disableAllInteractions()
			.create()

		GuiManager.setTarget(viewer, target)

		// Copy target's inventory contents (slots 0-35)
		val contents = target.inventory.contents
		for (i in 0 until minOf(36, contents.size)) {
			val item = contents[i]
			if (item != null) {
				gui.setItem(i, ItemBuilder.from(item).asGuiItem { it.isCancelled = true })
			}
		}

		// Copy armor contents (slots 36-39)
		val armor = target.inventory.armorContents
		for (i in 0 until armor.size) {
			val item = armor[i]
			if (item != null) {
				gui.setItem(36 + i, ItemBuilder.from(item).asGuiItem { it.isCancelled = true })
			}
		}

		// Control bar (slots 41-53)
		val filler = createItem(XMaterial.LIGHT_BLUE_STAINED_GLASS_PANE, " ")
		for (i in 41 until 54) {
			gui.setItem(i, filler)
		}

		// Refresh - slot 46 (0-indexed: 45)
		val refreshItem = createClickableItem(XMaterial.GREEN_TERRACOTTA, messageService.getRaw("inventory_refresh")) {
			open(viewer, target) // Refresh
		}
		gui.setItem(45, refreshItem)

		// Clear - slot 50 (0-indexed: 49)
		val clearItem = createClickableItem(XMaterial.BLUE_TERRACOTTA, messageService.getRaw("inventory_clear")) {
			target.inventory.clear()
			open(viewer, target) // Refresh
		}
		gui.setItem(49, clearItem)

		// Back - slot 54 (0-indexed: 53)
		val backItem = createClickableItem(XMaterial.REDSTONE_BLOCK, messageService.getRaw("inventory_back")) {
			ActionsGui(plugin, messageService).open(viewer, target)
		}
		gui.setItem(53, backItem)

		gui.open(viewer)
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
