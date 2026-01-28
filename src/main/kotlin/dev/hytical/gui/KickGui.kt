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
 * Kick reason selection GUI.
 */
class KickGui(
	private val plugin: AdminGUIPlugin,
	private val messageService: MessageService
) {

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

		// Fill background
		val filler = createItem(XMaterial.LIGHT_BLUE_STAINED_GLASS_PANE, " ")
		for (i in 0 until 27) {
			gui.setItem(i, filler)
		}

		// Kick reasons
		addKickReason(gui, 9, viewer, target, XMaterial.WHITE_TERRACOTTA, "kick_hacking")
		addKickReason(gui, 11, viewer, target, XMaterial.ORANGE_TERRACOTTA, "kick_griefing")
		addKickReason(gui, 13, viewer, target, XMaterial.MAGENTA_TERRACOTTA, "kick_spamming")
		addKickReason(gui, 15, viewer, target, XMaterial.LIGHT_BLUE_TERRACOTTA, "kick_advertising")
		addKickReason(gui, 17, viewer, target, XMaterial.YELLOW_TERRACOTTA, "kick_swearing")

		// Back - slot 27 (0-indexed: 26)
		val backItem = createClickableItem(XMaterial.REDSTONE_BLOCK, messageService.getRaw("kick_back")) {
			PlayerSettingsGui(plugin, messageService).open(viewer, target)
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
		val item = createClickableItem(material, messageService.getRaw(reasonKey)) {
			if (target.hasPermission("admingui.kick.bypass")) {
				messageService.send(viewer, "message_kick_bypass")
				viewer.closeInventory()
				return@createClickableItem
			}

			val prefix = messageService.getRaw("prefix")
			val kickReason = messageService.getRaw("kick") + messageService.getRaw(reasonKey)
			plugin.punishmentService.kick(target, prefix + kickReason)
			messageService.send(viewer, "message_player_kick", messageService.playerPlaceholder("player", target.name))
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
