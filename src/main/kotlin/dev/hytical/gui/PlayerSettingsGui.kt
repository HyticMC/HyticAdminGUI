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
 * Player settings GUI for target player.
 */
class PlayerSettingsGui(
	private val plugin: AdminGUIPlugin,
	private val messageService: MessageService
) {

	fun open(viewer: Player, target: Player) {
		val title = messageService.getRaw("players_color").replace("{player}", target.name)
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

		// Player info - slot 5 (0-indexed: 4)
		val infoLore = buildInfoLore(target)
		val playerHead = ItemBuilder.skull()
			.owner(target)
			.name(messageService.deserialize(messageService.getRaw("players_settings_info").replace("{player}", target.name)))
			.lore(infoLore)
			.asGuiItem { it.isCancelled = true }
		gui.setItem(4, playerHead)

		// Actions - slot 11 (0-indexed: 10)
		val actionsItem = createClickableItem(XMaterial.DIAMOND_SWORD, messageService.getRaw("players_settings_actions")) {
			ActionsGui(plugin, messageService).open(viewer, target)
		}
		gui.setItem(10, actionsItem)

		// Money - slot 13 (0-indexed: 12)
		if (viewer.hasPermission("admingui.money.other")) {
			val moneyItem = createClickableItem(XMaterial.PAPER, messageService.getRaw("players_settings_money")) {
				MoneyGui(plugin, messageService).open(viewer, target)
			}
			gui.setItem(12, moneyItem)
		} else {
			gui.setItem(12, createItem(XMaterial.RED_STAINED_GLASS_PANE, messageService.getRaw("permission")))
		}

		// Kick - slot 15 (0-indexed: 14)
		if (viewer.hasPermission("admingui.kick.other")) {
			val kickItem =
				createClickableItem(XMaterial.BLACK_TERRACOTTA, messageService.getRaw("players_settings_kick_player")) {
					KickGui(plugin, messageService).open(viewer, target)
				}
			gui.setItem(14, kickItem)
		} else {
			gui.setItem(14, createItem(XMaterial.RED_STAINED_GLASS_PANE, messageService.getRaw("permission")))
		}

		// Ban - slot 17 (0-indexed: 16)
		if (viewer.hasPermission("admingui.ban")) {
			val banItem = createClickableItem(XMaterial.BEDROCK, messageService.getRaw("players_settings_ban_player")) {
				BanGui(plugin, messageService).open(viewer, target)
			}
			gui.setItem(16, banItem)
		} else {
			gui.setItem(16, createItem(XMaterial.RED_STAINED_GLASS_PANE, messageService.getRaw("permission")))
		}

		// Back - slot 27 (0-indexed: 26)
		val backItem = createClickableItem(XMaterial.REDSTONE_BLOCK, messageService.getRaw("players_settings_back")) {
			PlayersListGui(plugin, messageService).open(viewer)
		}
		gui.setItem(26, backItem)

		gui.open(viewer)
	}

	private fun buildInfoLore(target: Player): List<net.kyori.adventure.text.Component> {
		val lore = mutableListOf<net.kyori.adventure.text.Component>()
		lore.add(messageService.deserialize("<yellow>Health: ${target.health.toInt()}"))
		lore.add(messageService.deserialize("<gray>Food: ${target.foodLevel}"))
		if (plugin.hookService.hasVault) {
			val balance = plugin.economyService.format(plugin.economyService.getBalance(target))
			lore.add(messageService.deserialize("<green>Money: $balance"))
		}
		lore.add(messageService.deserialize("<green>Gamemode: ${target.gameMode.name}"))
		return lore
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
