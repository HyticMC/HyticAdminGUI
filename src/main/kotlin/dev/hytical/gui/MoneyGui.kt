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
 * Money action selection GUI.
 */
class MoneyGui(
	private val plugin: AdminGUIPlugin,
	private val messageService: MessageService
) {

	fun open(viewer: Player, target: Player) {
		if (!target.isOnline) {
			messageService.send(viewer, "message_player_not_found")
			viewer.closeInventory()
			return
		}

		if (!plugin.hookService.hasVault) {
			messageService.send(viewer, "vault_required")
			viewer.closeInventory()
			return
		}

		val title = messageService.getRaw("inventory_money").replace("{player}", target.name)
		val gui = Gui.gui()
			.title(messageService.deserialize(title))
			.rows(3)
			.disableAllInteractions()
			.create()

		GuiManager.setTarget(viewer, target)

		// Fill background
		val filler = createItem(XMaterial.LIGHT_BLUE_STAINED_GLASS_PANE, " ")
		for (i in 0 until 27) {
			gui.setItem(i, filler)
		}

		// Give - slot 12 (0-indexed: 11)
		val giveItem = createClickableItem(XMaterial.PAPER, messageService.getRaw("money_give")) {
			MoneyAmountGui(plugin, messageService, MoneyAction.GIVE).open(viewer, target)
		}
		gui.setItem(11, giveItem)

		// Set - slot 14 (0-indexed: 13)
		val setItem = createClickableItem(XMaterial.BOOK, messageService.getRaw("money_set")) {
			MoneyAmountGui(plugin, messageService, MoneyAction.SET).open(viewer, target)
		}
		gui.setItem(13, setItem)

		// Take - slot 16 (0-indexed: 15)
		val takeItem = createClickableItem(XMaterial.PAPER, messageService.getRaw("money_take")) {
			MoneyAmountGui(plugin, messageService, MoneyAction.TAKE).open(viewer, target)
		}
		gui.setItem(15, takeItem)

		// Back - slot 27 (0-indexed: 26)
		val backItem = createClickableItem(XMaterial.REDSTONE_BLOCK, messageService.getRaw("money_back")) {
			if (viewer == target) {
				PlayerGui(plugin, messageService).open(viewer)
			} else {
				PlayerSettingsGui(plugin, messageService).open(viewer, target)
			}
		}
		gui.setItem(26, backItem)

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

enum class MoneyAction {
	GIVE, SET, TAKE
}
