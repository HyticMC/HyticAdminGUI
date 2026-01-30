package dev.hytical.gui

import com.cryptomorin.xseries.XMaterial
import dev.hytical.ServiceContext
import dev.hytical.gui.GuiUtils.createClickableItem
import dev.hytical.gui.GuiUtils.fillBackground
import dev.triumphteam.gui.guis.Gui
import org.bukkit.entity.Player

class MoneyGui(private val ctx: ServiceContext) {

	private val messageService get() = ctx.messageService

	fun open(viewer: Player, target: Player) {
		if (!target.isOnline) {
			messageService.send(viewer, "message_player_not_found")
			viewer.closeInventory()
			return
		}

		if (!ctx.hookService.hasVault) {
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

		fillBackground(gui, 27, messageService = messageService)

		val giveItem = createClickableItem(XMaterial.PAPER, messageService.getRaw("money_give"), messageService) {
			ctx.createMoneyAmountGui(MoneyAction.GIVE).open(viewer, target)
		}
		gui.setItem(11, giveItem)

		val setItem = createClickableItem(XMaterial.BOOK, messageService.getRaw("money_set"), messageService) {
			ctx.createMoneyAmountGui(MoneyAction.SET).open(viewer, target)
		}
		gui.setItem(13, setItem)

		val takeItem = createClickableItem(XMaterial.PAPER, messageService.getRaw("money_take"), messageService) {
			ctx.createMoneyAmountGui(MoneyAction.TAKE).open(viewer, target)
		}
		gui.setItem(15, takeItem)

		val backItem = createClickableItem(XMaterial.REDSTONE_BLOCK, messageService.getRaw("money_back"), messageService) {
			if (viewer == target) {
				ctx.createPlayerGui().open(viewer)
			} else {
				ctx.createPlayerSettingsGui().open(viewer, target)
			}
		}
		gui.setItem(26, backItem)

		gui.open(viewer)
	}
}

enum class MoneyAction {
	GIVE, SET, TAKE
}
