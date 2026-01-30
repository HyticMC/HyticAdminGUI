package dev.hytical.gui

import com.cryptomorin.xseries.XMaterial
import dev.hytical.ServiceContext
import dev.hytical.gui.GuiUtils.createClickableItem
import dev.hytical.gui.GuiUtils.fillBackground
import dev.hytical.gui.GuiUtils.toItemStack
import dev.triumphteam.gui.builder.item.ItemBuilder
import dev.triumphteam.gui.guis.Gui
import org.bukkit.entity.Player

class BanGui(private val ctx: ServiceContext) {

	private val messageService get() = ctx.messageService

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
		fillBackground(gui, 36, messageService = messageService)

		addDurationItem(gui, 11, viewer, target, "years", MAX_YEARS, GuiManager::getBanYears, GuiManager::setBanYears)
		addDurationItem(gui, 12, viewer, target, "months", MAX_MONTHS, GuiManager::getBanMonths, GuiManager::setBanMonths)
		addDurationItem(gui, 13, viewer, target, "days", MAX_DAYS, GuiManager::getBanDays, GuiManager::setBanDays)
		addDurationItem(gui, 14, viewer, target, "hours", MAX_HOURS, GuiManager::getBanHours, GuiManager::setBanHours)
		addDurationItem(gui, 15, viewer, target, "minutes", MAX_MINUTES, GuiManager::getBanMinutes, GuiManager::setBanMinutes)

		BAN_REASONS.forEachIndexed { index, (material, reasonKey) ->
			addBanReason(gui, 29 + index, viewer, target, material, reasonKey)
		}

		val backItem = createClickableItem(XMaterial.REDSTONE_BLOCK, messageService.getRaw("ban_back"), messageService) {
			GuiManager.clearBanConfig(viewer)
			ctx.createPlayerSettingsGui().open(viewer, target)
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
		maxValue: Int,
		getter: (Player) -> Int,
		setter: (Player, Int) -> Unit
	) {
		val currentValue = getter(viewer)
		val nameKey = "ban_$type"

		val material = if (currentValue == 0) XMaterial.RED_STAINED_GLASS_PANE else XMaterial.CLOCK
		val amount = if (currentValue == 0) 1 else currentValue.coerceIn(1, 64)

		val item = ItemBuilder.from(material.toItemStack())
			.name(messageService.deserialize(messageService.getRaw(nameKey)))
			.amount(amount)
			.asGuiItem {
				it.isCancelled = true
				val newValue = (currentValue + 1) % (maxValue + 1)
				setter(viewer, newValue)
				open(viewer, target)
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
		val item = createClickableItem(material, messageService.getRaw(reasonKey), messageService) {
			if (target.hasPermission("admingui.ban.bypass")) {
				messageService.send(viewer, "message_ban_bypass")
				viewer.closeInventory()
				return@createClickableItem
			}

			val banDate = ctx.punishmentService.calculateBanDate(
				years = GuiManager.getBanYears(viewer),
				months = GuiManager.getBanMonths(viewer),
				days = GuiManager.getBanDays(viewer),
				hours = GuiManager.getBanHours(viewer),
				minutes = GuiManager.getBanMinutes(viewer)
			)

			val banReason = ctx.punishmentService.formatBanReason(reasonKey, banDate, messageService)
			val prefix = messageService.getRaw("prefix")

			ctx.punishmentService.ban(target.name, banReason, banDate)
			ctx.punishmentService.kick(target, prefix + banReason)

			messageService.send(viewer, "message_player_ban", messageService.playerPlaceholder("player", target.name))
			GuiManager.clearBanConfig(viewer)
			viewer.closeInventory()
		}
		gui.setItem(slot, item)
	}

	private companion object {
		const val MAX_YEARS = 10
		const val MAX_MONTHS = 12
		const val MAX_DAYS = 31
		const val MAX_HOURS = 24
		const val MAX_MINUTES = 60

		val BAN_REASONS = listOf(
			XMaterial.WHITE_TERRACOTTA to "ban_hacking",
			XMaterial.ORANGE_TERRACOTTA to "ban_griefing",
			XMaterial.MAGENTA_TERRACOTTA to "ban_spamming",
			XMaterial.LIGHT_BLUE_TERRACOTTA to "ban_advertising",
			XMaterial.YELLOW_TERRACOTTA to "ban_swearing"
		)
	}
}
