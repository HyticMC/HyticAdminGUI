package dev.hytical.gui

import com.cryptomorin.xseries.XMaterial
import dev.hytical.ServiceContext
import dev.hytical.gui.GuiUtils.createClickableItem
import dev.hytical.gui.GuiUtils.fillBackground
import dev.triumphteam.gui.guis.Gui
import org.bukkit.entity.Player

class KickGui(private val ctx: ServiceContext) {

	private val messageService get() = ctx.messageService

	fun open(viewer: Player, target: Player) {
		if (!target.isOnline) {
			messageService.send(viewer, "message_player_not_found")
			viewer.closeInventory()
			return
		}

		val title = messageService.getRaw("inventory_kick").replace("{player}", target.name)
		val gui = Gui.gui()
			.title(messageService.deserialize(title))
			.rows(3)
			.disableAllInteractions()
			.create()

		GuiManager.setTarget(viewer, target)

		fillBackground(gui, 27, messageService = messageService)

		KICK_REASONS.forEach { (slot, material, reasonKey) ->
			addKickReason(gui, slot, viewer, target, material, reasonKey)
		}

		val backItem = createClickableItem(XMaterial.REDSTONE_BLOCK, messageService.getRaw("kick_back"), messageService) {
			ctx.createPlayerSettingsGui().open(viewer, target)
		}
		gui.setItem(26, backItem)

		gui.open(viewer)
	}

	private fun addKickReason(
		gui: Gui,
		slot: Int,
		viewer: Player,
		target: Player,
		material: XMaterial,
		reasonKey: String
	) {
		val item = createClickableItem(material, messageService.getRaw(reasonKey), messageService) {
			if (target.hasPermission("admingui.kick.bypass")) {
				messageService.send(viewer, "message_kick_bypass")
				viewer.closeInventory()
				return@createClickableItem
			}

			val prefix = messageService.getRaw("prefix")
			val kickReason = messageService.getRaw("kick") + messageService.getRaw(reasonKey)
			ctx.punishmentService.kick(target, prefix + kickReason)
			messageService.send(viewer, "message_player_kick", messageService.playerPlaceholder("player", target.name))
			viewer.closeInventory()
		}
		gui.setItem(slot, item)
	}

	private companion object {
		val KICK_REASONS = listOf(
			Triple(9, XMaterial.WHITE_TERRACOTTA, "kick_hacking"),
			Triple(11, XMaterial.ORANGE_TERRACOTTA, "kick_griefing"),
			Triple(13, XMaterial.MAGENTA_TERRACOTTA, "kick_spamming"),
			Triple(15, XMaterial.LIGHT_BLUE_TERRACOTTA, "kick_advertising"),
			Triple(17, XMaterial.YELLOW_TERRACOTTA, "kick_swearing")
		)
	}
}
