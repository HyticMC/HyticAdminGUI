package dev.hytical.gui

import com.cryptomorin.xseries.XMaterial
import dev.hytical.ServiceContext
import dev.hytical.gui.GuiUtils.createClickableItem
import dev.hytical.gui.GuiUtils.createItem
import dev.hytical.gui.GuiUtils.fillBackground
import dev.triumphteam.gui.builder.item.ItemBuilder
import dev.triumphteam.gui.guis.Gui
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class PlayersListGui(private val ctx: ServiceContext) {

	private val messageService get() = ctx.messageService

	fun open(player: Player, page: Int = 0) {
		val gui = Gui.gui()
			.title(messageService.getTitle("inventory_players"))
			.rows(6)
			.disableAllInteractions()
			.create()

		fillBackground(gui, TOTAL_SLOTS, messageService = messageService)

		val onlinePlayers = Bukkit.getOnlinePlayers().toList()
		val totalPages = ((onlinePlayers.size - 1) / PAGE_SIZE).coerceAtLeast(0)
		val currentPage = page.coerceIn(0, totalPages)

		GuiManager.setPage(player, currentPage)

		val startIndex = currentPage * PAGE_SIZE
		val endIndex = minOf(startIndex + PAGE_SIZE, onlinePlayers.size)

		onlinePlayers.subList(startIndex, endIndex).forEachIndexed { index, target ->
			val playerHead = ItemBuilder.skull()
				.owner(target)
				.name(messageService.deserialize(messageService.getRaw("players_color").replace("{player}", target.name)))
				.asGuiItem {
					it.isCancelled = true
					ctx.createPlayerSettingsGui().open(player, target)
				}
			gui.setItem(index, playerHead)
		}

		if (currentPage > 0) {
			val prevItem = createClickableItem(XMaterial.ARROW, messageService.getRaw("players_previous"), messageService) {
				open(player, currentPage - 1)
			}
			gui.setItem(PREV_SLOT, prevItem)
		}

		if (currentPage < totalPages) {
			val nextItem = createClickableItem(XMaterial.ARROW, messageService.getRaw("players_next"), messageService) {
				open(player, currentPage + 1)
			}
			gui.setItem(NEXT_SLOT, nextItem)
		}

		val backItem = createClickableItem(XMaterial.REDSTONE_BLOCK, messageService.getRaw("players_back"), messageService) {
			ctx.createMainGui().open(player)
		}
		gui.setItem(BACK_SLOT, backItem)

		gui.open(player)
	}

	companion object {
		private const val PAGE_SIZE = 45
		private const val TOTAL_SLOTS = 54
		private const val PREV_SLOT = 48
		private const val NEXT_SLOT = 50
		private const val BACK_SLOT = 53
	}
}
