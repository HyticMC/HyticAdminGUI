package dev.hytical.gui

import com.cryptomorin.xseries.XMaterial
import dev.hytical.ServiceContext
import dev.hytical.gui.GuiUtils.createClickableItem
import dev.hytical.gui.GuiUtils.fillBackground
import dev.hytical.gui.GuiUtils.toItemStack
import dev.triumphteam.gui.builder.item.ItemBuilder
import dev.triumphteam.gui.guis.Gui
import org.bukkit.entity.Player

class MoneyAmountGui(
	private val ctx: ServiceContext,
	private val action: MoneyAction
) {

	private val messageService get() = ctx.messageService

	fun open(viewer: Player, target: Player) {
		if (!target.isOnline) {
			messageService.send(viewer, "message_player_not_found")
			viewer.closeInventory()
			return
		}

		val titleKey = when (action) {
			MoneyAction.GIVE -> "inventory_money_give"
			MoneyAction.SET -> "inventory_money_set"
			MoneyAction.TAKE -> "inventory_money_take"
		}
		val title = messageService.getRaw(titleKey).replace("{player}", target.name)
		val gui = Gui.gui()
			.title(messageService.deserialize(title))
			.rows(4)
			.disableAllInteractions()
			.create()

		GuiManager.setTarget(viewer, target)

		fillBackground(gui, 36, messageService = messageService)

		AMOUNTS.take(35).forEachIndexed { index, amount ->
			val formattedAmount = ctx.economyService.format(amount.toDouble())
			val item = ItemBuilder.from(XMaterial.PAPER.toItemStack())
				.name(messageService.deserialize("<green><bold>$formattedAmount"))
				.asGuiItem {
					it.isCancelled = true
					performAction(viewer, target, amount.toDouble())
				}
			gui.setItem(index, item)
		}

		val backItem = createClickableItem(XMaterial.REDSTONE_BLOCK, messageService.getRaw("money_amount_back"), messageService) {
			ctx.createMoneyGui().open(viewer, target)
		}
		gui.setItem(35, backItem)

		gui.open(viewer)
	}

	private fun performAction(viewer: Player, target: Player, amount: Double) {
		val economyService = ctx.economyService
		val formattedAmount = economyService.format(amount)

		when (action) {
			MoneyAction.GIVE -> {
				if (economyService.give(target, amount)) {
					val newBalance = economyService.format(economyService.getBalance(target))
					if (viewer == target) {
						messageService.send(viewer, "message_give",
							messageService.placeholder("amount", formattedAmount),
							messageService.placeholder("balance", newBalance))
					} else {
						messageService.send(viewer, "message_player_give",
							messageService.playerPlaceholder("player", target.name),
							messageService.placeholder("amount", formattedAmount),
							messageService.placeholder("balance", newBalance))
						messageService.send(target, "message_target_player_give",
							messageService.playerPlaceholder("player", viewer.name),
							messageService.placeholder("amount", formattedAmount),
							messageService.placeholder("balance", newBalance))
					}
				} else {
					messageService.send(viewer, "message_transaction_error")
				}
			}

			MoneyAction.SET -> {
				if (economyService.set(target, amount)) {
					if (viewer == target) {
						messageService.send(viewer, "message_set",
							messageService.placeholder("amount", formattedAmount))
					} else {
						messageService.send(viewer, "message_player_set",
							messageService.playerPlaceholder("player", target.name),
							messageService.placeholder("amount", formattedAmount))
						messageService.send(target, "message_target_player_set",
							messageService.playerPlaceholder("player", viewer.name),
							messageService.placeholder("amount", formattedAmount))
					}
				} else {
					messageService.send(viewer, "message_transaction_error")
				}
			}

			MoneyAction.TAKE -> {
				if (economyService.take(target, amount)) {
					val newBalance = economyService.format(economyService.getBalance(target))
					if (viewer == target) {
						messageService.send(viewer, "message_take",
							messageService.placeholder("amount", formattedAmount),
							messageService.placeholder("balance", newBalance))
					} else {
						messageService.send(viewer, "message_player_take",
							messageService.playerPlaceholder("player", target.name),
							messageService.placeholder("amount", formattedAmount),
							messageService.placeholder("balance", newBalance))
						messageService.send(target, "message_target_player_take",
							messageService.playerPlaceholder("player", viewer.name),
							messageService.placeholder("amount", formattedAmount),
							messageService.placeholder("balance", newBalance))
					}
				} else {
					messageService.send(viewer, if (viewer == target) "message_take_error" else "message_player_take_error")
				}
			}
		}
		viewer.closeInventory()
	}

	companion object {
		private val AMOUNTS = listOf(
			100, 200, 300, 400, 500, 600, 700, 800, 900, 1000,
			1500, 3000, 4500, 6000, 7500, 9000, 10500, 12000, 13500,
			15000, 30000, 45000, 60000, 75000, 90000,
			100000, 200000, 300000, 400000, 500000, 600000, 700000, 800000, 900000, 1000000
		)
	}
}
