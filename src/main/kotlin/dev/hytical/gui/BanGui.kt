package dev.hytical.gui

import com.cryptomorin.xseries.XMaterial
import dev.hytical.AdminGUIPlugin
import dev.hytical.services.MessageService
import dev.triumphteam.gui.builder.item.ItemBuilder
import dev.triumphteam.gui.guis.Gui
import dev.triumphteam.gui.guis.GuiItem
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class BanGui(
	private val plugin: AdminGUIPlugin,
	private val messageService: MessageService
) {

	fun open(viewer: Player, target: Player) {
		if (!target.isOnline) {
			messageService.send(viewer, "message_player_not_found")
			viewer.closeInventory()
			return
		}

		val title = messageService.getRaw("inventory_ban").replace("{player}", target.name)
		val gui = Gui.gui()
			.title(messageService.deserialize(title))
			.rows(4)
			.disableAllInteractions()
			.create()

		GuiManager.setTarget(viewer, target)

		val filler = createItem(XMaterial.LIGHT_BLUE_STAINED_GLASS_PANE, " ")
		for (i in 0 until 36) {
			gui.setItem(i, filler)
		}

		addDurationItem(gui, 11, viewer, target, "years", GuiManager::getBanYears, GuiManager::setBanYears)
		addDurationItem(gui, 12, viewer, target, "months", GuiManager::getBanMonths, GuiManager::setBanMonths)
		addDurationItem(gui, 13, viewer, target, "days", GuiManager::getBanDays, GuiManager::setBanDays)
		addDurationItem(gui, 14, viewer, target, "hours", GuiManager::getBanHours, GuiManager::setBanHours)
		addDurationItem(gui, 15, viewer, target, "minutes", GuiManager::getBanMinutes, GuiManager::setBanMinutes)

		addBanReason(gui, 29, viewer, target, XMaterial.WHITE_TERRACOTTA, "ban_hacking")
		addBanReason(gui, 30, viewer, target, XMaterial.ORANGE_TERRACOTTA, "ban_griefing")
		addBanReason(gui, 31, viewer, target, XMaterial.MAGENTA_TERRACOTTA, "ban_spamming")
		addBanReason(gui, 32, viewer, target, XMaterial.LIGHT_BLUE_TERRACOTTA, "ban_advertising")
		addBanReason(gui, 33, viewer, target, XMaterial.YELLOW_TERRACOTTA, "ban_swearing")

		val backItem = createClickableItem(XMaterial.REDSTONE_BLOCK, messageService.getRaw("ban_back")) {
			GuiManager.clearBanConfig(viewer)
			PlayerSettingsGui(plugin, messageService).open(viewer, target)
		}
		gui.setItem(35, backItem)

		gui.open(viewer)
	}

	private fun addDurationItem(
		gui: Gui,
		slot: Int,
		viewer: Player,
		target: Player,
		type: String,
		getter: (Player) -> Int,
		setter: (Player, Int) -> Unit
	) {
		val currentValue = getter(viewer)
		val nameKey = "ban_$type"

		val material = if (currentValue == 0) XMaterial.RED_STAINED_GLASS_PANE else XMaterial.CLOCK
		val amount = if (currentValue == 0) 1 else currentValue.coerceIn(1, 64)

		val item = ItemBuilder.from(material.parseItem() ?: ItemStack(org.bukkit.Material.STONE))
			.name(messageService.deserialize(messageService.getRaw(nameKey)))
			.amount(amount)
			.asGuiItem {
				it.isCancelled = true
				// Cycle through values
				val maxValue = when (type) {
					"years" -> 10
					"months" -> 12
					"days" -> 31
					"hours" -> 24
					"minutes" -> 60
					else -> 10
				}
				val newValue = (currentValue + 1) % (maxValue + 1)
				setter(viewer, newValue)
				open(viewer, target) // Refresh
			}
		gui.setItem(slot, item)
	}

	private fun addBanReason(
		gui: Gui,
		slot: Int,
		viewer: Player,
		target: Player,
		material: XMaterial,
		reasonKey: String
	) {
		val item = createClickableItem(material, messageService.getRaw(reasonKey)) {
			if (target.hasPermission("admingui.ban.bypass")) {
				messageService.send(viewer, "message_ban_bypass")
				viewer.closeInventory()
				return@createClickableItem
			}

			val banDate = plugin.punishmentService.calculateBanDate(
				years = GuiManager.getBanYears(viewer),
				months = GuiManager.getBanMonths(viewer),
				days = GuiManager.getBanDays(viewer),
				hours = GuiManager.getBanHours(viewer),
				minutes = GuiManager.getBanMinutes(viewer)
			)

			val banReason = plugin.punishmentService.formatBanReason(reasonKey, banDate, messageService)
			val prefix = messageService.getRaw("prefix")

			plugin.punishmentService.ban(target.name, banReason, banDate)
			plugin.punishmentService.kick(target, prefix + banReason)

			messageService.send(viewer, "message_player_ban", messageService.playerPlaceholder("player", target.name))
			GuiManager.clearBanConfig(viewer)
			viewer.closeInventory()
		}
		gui.setItem(slot, item)
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
