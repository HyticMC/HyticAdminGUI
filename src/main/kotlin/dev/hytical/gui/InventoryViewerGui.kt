package dev.hytical.gui

import com.cryptomorin.xseries.XMaterial
import dev.hytical.ServiceContext
import dev.hytical.gui.GuiUtils.createClickableItem
import dev.hytical.gui.GuiUtils.createItem
import dev.triumphteam.gui.builder.item.ItemBuilder
import dev.triumphteam.gui.guis.Gui
import org.bukkit.entity.Player

class InventoryViewerGui(private val ctx: ServiceContext) {

	private val messageService get() = ctx.messageService

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

		val contents = target.inventory.contents
		for (i in 0 until minOf(36, contents.size)) {
			contents[i]?.let { item ->
				gui.setItem(i, ItemBuilder.from(item).asGuiItem { it.isCancelled = true })
			}
		}

		target.inventory.armorContents.forEachIndexed { i, item ->
			item?.let {
				gui.setItem(36 + i, ItemBuilder.from(it).asGuiItem { e -> e.isCancelled = true })
			}
		}

		val filler = createItem(XMaterial.LIGHT_BLUE_STAINED_GLASS_PANE, " ", messageService)
		(41 until 54).forEach { gui.setItem(it, filler) }

		val refreshItem = createClickableItem(XMaterial.GREEN_TERRACOTTA, messageService.getRaw("inventory_refresh"), messageService) {
			open(viewer, target)
		}
		gui.setItem(45, refreshItem)

		val clearItem = createClickableItem(XMaterial.BLUE_TERRACOTTA, messageService.getRaw("inventory_clear"), messageService) {
			target.inventory.clear()
			open(viewer, target)
		}
		gui.setItem(49, clearItem)

		val backItem = createClickableItem(XMaterial.REDSTONE_BLOCK, messageService.getRaw("inventory_back"), messageService) {
			ctx.createActionsGui().open(viewer, target)
		}
		gui.setItem(53, backItem)

		gui.open(viewer)
	}
}
