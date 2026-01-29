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

class PlayersListGui(
	private val plugin: AdminGUIPlugin,
	private val messageService: MessageService
) {

	private val pageSize = 45

	fun open(player: Player) {
		val gui = Gui.gui()
			.title(messageService.getTitle("inventory_players"))
			.rows(6)
			.disableAllInteractions()
			.create()

		val onlinePlayers = Bukkit.getOnlinePlayers()
			.filter { it.name != player.name }
			.sortedBy { it.name }

		val currentPage = GuiManager.getPage(player)
		val totalPages = ((onlinePlayers.size - 1) / pageSize) + 1

		val startIndex = (currentPage - 1) * pageSize
		val endIndex = minOf(startIndex + pageSize, onlinePlayers.size)
		val playersOnPage = if (startIndex < onlinePlayers.size) {
			onlinePlayers.subList(startIndex, endIndex)
		} else {
			emptyList()
		}

		val filler = createItem(XMaterial.LIGHT_BLUE_STAINED_GLASS_PANE, " ")
		for (i in 0 until 45) {
			if (i < playersOnPage.size) {
				val target = playersOnPage[i]
				val playerTitle = messageService.getRaw("players_color").replace("{player}", target.name)
				val playerHead = ItemBuilder.skull()
					.owner(target)
					.name(messageService.deserialize(playerTitle))
					.lore(messageService.deserialize(messageService.getRaw("players_lore")))
					.asGuiItem {
						it.isCancelled = true
						GuiManager.setTarget(player, target)
						PlayerSettingsGui(plugin, messageService).open(player, target)
					}
				gui.setItem(i, playerHead)
			} else {
				gui.setItem(i, filler)
			}
		}

		for (i in 45 until 54) {
			gui.setItem(i, filler)
		}

		if (currentPage > 1) {
			val prevItem = createClickableItem(XMaterial.PAPER, messageService.getRaw("players_previous")) {
				GuiManager.setPage(player, currentPage - 1)
				open(player)
			}
			gui.setItem(48, prevItem)
		}

		if (totalPages > 1) {
			val pageTitle = messageService.getRaw("players_page") + " $currentPage"
			val pageItem = ItemBuilder.from(XMaterial.BOOK.parseItem() ?: ItemStack(org.bukkit.Material.BOOK))
				.name(messageService.deserialize(pageTitle))
				.amount(currentPage.coerceIn(1, 64))
				.asGuiItem { it.isCancelled = true }
			gui.setItem(49, pageItem)
		}

		if (currentPage < totalPages) {
			val nextItem = createClickableItem(XMaterial.PAPER, messageService.getRaw("players_next")) {
				GuiManager.setPage(player, currentPage + 1)
				open(player)
			}
			gui.setItem(50, nextItem)
		}

		val backItem = createClickableItem(XMaterial.REDSTONE_BLOCK, messageService.getRaw("players_back")) {
			GuiManager.clearPage(player)
			MainGui(plugin, messageService).open(player)
		}
		gui.setItem(53, backItem)

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
