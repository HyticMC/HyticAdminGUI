package dev.hytical.gui

import com.cryptomorin.xseries.XMaterial
import dev.hytical.ServiceContext
import dev.hytical.gui.GuiUtils.createClickableItem
import dev.hytical.gui.GuiUtils.createNoPermissionItem
import dev.hytical.gui.GuiUtils.fillBackground
import dev.triumphteam.gui.builder.item.ItemBuilder
import dev.triumphteam.gui.guis.Gui
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

class PlayerSettingsGui(private val ctx: ServiceContext) {

	private val messageService get() = ctx.messageService

	fun open(viewer: Player, target: Player) {
		val title = messageService.getRaw("players_color").replace("{player}", target.name)
		val gui = Gui.gui()
			.title(messageService.deserialize(title))
			.rows(3)
			.disableAllInteractions()
			.create()

		GuiManager.setTarget(viewer, target)

		fillBackground(gui, 27, messageService = messageService)

		val infoLore = buildInfoLore(target)
		val playerHead = ItemBuilder.skull()
			.owner(target)
			.name(messageService.deserialize(messageService.getRaw("players_settings_info").replace("{player}", target.name)))
			.lore(infoLore)
			.asGuiItem { it.isCancelled = true }
		gui.setItem(4, playerHead)

		val actionsItem = createClickableItem(XMaterial.DIAMOND_SWORD, messageService.getRaw("players_settings_actions"), messageService) {
			ctx.createActionsGui().open(viewer, target)
		}
		gui.setItem(10, actionsItem)

		gui.setItem(12, createPermissionItem(viewer, "admingui.money.other") {
			createClickableItem(XMaterial.PAPER, messageService.getRaw("players_settings_money"), messageService) {
				ctx.createMoneyGui().open(viewer, target)
			}
		})

		gui.setItem(14, createPermissionItem(viewer, "admingui.kick.other") {
			createClickableItem(XMaterial.BLACK_TERRACOTTA, messageService.getRaw("players_settings_kick_player"), messageService) {
				ctx.createKickGui().open(viewer, target)
			}
		})

		gui.setItem(16, createPermissionItem(viewer, "admingui.ban") {
			createClickableItem(XMaterial.BEDROCK, messageService.getRaw("players_settings_ban_player"), messageService) {
				ctx.createBanGui().open(viewer, target)
			}
		})

		val backItem = createClickableItem(XMaterial.REDSTONE_BLOCK, messageService.getRaw("players_settings_back"), messageService) {
			ctx.createPlayersListGui().open(viewer)
		}
		gui.setItem(26, backItem)

		gui.open(viewer)
	}

	private fun buildInfoLore(target: Player): List<Component> = buildList {
		add(messageService.deserialize("<yellow>Health: ${target.health.toInt()}"))
		add(messageService.deserialize("<gray>Food: ${target.foodLevel}"))
		if (ctx.hookService.hasVault) {
			val balance = ctx.economyService.format(ctx.economyService.getBalance(target))
			add(messageService.deserialize("<green>Money: $balance"))
		}
		add(messageService.deserialize("<green>Gamemode: ${target.gameMode.name}"))
	}

	private inline fun createPermissionItem(
		viewer: Player,
		permission: String,
		itemFactory: () -> dev.triumphteam.gui.guis.GuiItem
	) = if (viewer.hasPermission(permission)) {
		itemFactory()
	} else {
		createNoPermissionItem(messageService)
	}
}
