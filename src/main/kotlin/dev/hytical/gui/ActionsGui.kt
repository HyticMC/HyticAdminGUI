package dev.hytical.gui

import com.cryptomorin.xseries.XMaterial
import de.myzelyam.api.vanish.VanishAPI
import dev.hytical.ServiceContext
import dev.hytical.gui.GuiUtils.createClickableItem
import dev.hytical.gui.GuiUtils.createNoPermissionItem
import dev.hytical.gui.GuiUtils.fillBackground
import dev.triumphteam.gui.builder.item.ItemBuilder
import dev.triumphteam.gui.guis.Gui
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.entity.Player

class ActionsGui(private val ctx: ServiceContext) {

	private val messageService get() = ctx.messageService

	fun open(viewer: Player, target: Player) {
		if (!target.isOnline) {
			messageService.send(viewer, "message_player_not_found")
			viewer.closeInventory()
			return
		}

		val title = messageService.getRaw("inventory_actions").replace("{player}", target.name)
		val gui = Gui.gui()
			.title(messageService.deserialize(title))
			.rows(6)
			.disableAllInteractions()
			.create()

		GuiManager.setTarget(viewer, target)
		fillBackground(gui, 54, messageService = messageService)

		val playerHead = ItemBuilder.skull()
			.owner(target)
			.name(messageService.deserialize(messageService.getRaw("actions_info").replace("{player}", target.name)))
			.asGuiItem { it.isCancelled = true }
		gui.setItem(4, playerHead)

		addPermissionItem(gui, 10, viewer, "admingui.heal.other", XMaterial.GOLDEN_APPLE, "actions_heal") {
			target.health = target.maxHealth
			target.fireTicks = 0
			messageService.send(viewer, "message_player_heal", messageService.playerPlaceholder("player", target.name))
			messageService.send(target, "message_target_player_heal", messageService.playerPlaceholder("player", viewer.name))
		}

		addPermissionItem(gui, 12, viewer, "admingui.feed.other", XMaterial.COOKED_BEEF, "actions_feed") {
			target.foodLevel = 20
			messageService.send(viewer, "message_player_feed", messageService.playerPlaceholder("player", target.name))
			messageService.send(target, "message_target_player_feed", messageService.playerPlaceholder("player", viewer.name))
		}

		addGamemodeItem(gui, 14, viewer, target)
		addGodModeItem(gui, 16, viewer, target)

		addPermissionItem(gui, 18, viewer, "admingui.teleport", XMaterial.ENDER_PEARL, "actions_teleport_to_player") {
			viewer.closeInventory()
			viewer.teleport(target.location)
			messageService.send(viewer, "message_target_player_teleport", messageService.playerPlaceholder("player", target.name))
		}

		addPermissionItem(gui, 20, viewer, "admingui.potions.other", XMaterial.POTION, "actions_potions") {
			ctx.createPotionsGui().open(viewer, target)
		}

		addPermissionItem(gui, 22, viewer, "admingui.kill.other", XMaterial.DIAMOND_SWORD, "actions_kill_player") {
			target.health = 0.0
			messageService.send(viewer, "message_player_kill", messageService.playerPlaceholder("player", target.name))
		}

		addPermissionItem(gui, 24, viewer, "admingui.spawner.other", XMaterial.SPAWNER, "actions_spawner") {
			ctx.createSpawnerGui().open(viewer, target)
		}

		addPermissionItem(gui, 26, viewer, "admingui.teleport.other", XMaterial.END_CRYSTAL, "actions_teleport_player_to_you") {
			viewer.closeInventory()
			target.teleport(viewer.location)
			messageService.send(target, "message_target_player_teleport", messageService.playerPlaceholder("player", viewer.name))
		}

		addPermissionItem(gui, 28, viewer, "admingui.inventory", XMaterial.BOOK, "actions_inventory") {
			ctx.createInventoryViewerGui().open(viewer, target)
		}

		addPermissionItem(gui, 30, viewer, "admingui.burn.other", XMaterial.FLINT_AND_STEEL, "actions_burn_player") {
			target.fireTicks = 500
			messageService.send(viewer, "message_player_burn", messageService.playerPlaceholder("player", target.name))
		}

		addVanishItem(gui, 32, viewer, target)

		addPermissionItem(gui, 34, viewer, "admingui.lightning.other", XMaterial.TRIDENT, "actions_lightning") {
			target.world.strikeLightning(target.location)
		}

		addPermissionItem(gui, 36, viewer, "admingui.firework.other", XMaterial.FIREWORK_ROCKET, "actions_firework") {
			FireworkUtil.createRandom(target)
		}

		addPermissionItem(gui, 38, viewer, "admingui.fakeop", XMaterial.PAPER, "actions_fakeop") {
			Bukkit.broadcastMessage("§7§o[Server: Made ${target.name} a server operator]")
		}

		val backItem = createClickableItem(XMaterial.REDSTONE_BLOCK, messageService.getRaw("actions_back"), messageService) {
			ctx.createPlayerSettingsGui().open(viewer, target)
		}
		gui.setItem(53, backItem)

		gui.open(viewer)
	}

	private fun addPermissionItem(
		gui: Gui,
		slot: Int,
		viewer: Player,
		permission: String,
		material: XMaterial,
		nameKey: String,
		onClick: () -> Unit
	) {
		val item = if (viewer.hasPermission(permission)) {
			createClickableItem(material, messageService.getRaw(nameKey), messageService, onClick)
		} else {
			createNoPermissionItem(messageService)
		}
		gui.setItem(slot, item)
	}

	private fun addGamemodeItem(gui: Gui, slot: Int, viewer: Player, target: Player) {
		if (!viewer.hasPermission("admingui.gamemode.other")) {
			gui.setItem(slot, createNoPermissionItem(messageService))
			return
		}

		val (material, nameKey, nextMode) = when (target.gameMode) {
			GameMode.SURVIVAL -> Triple(XMaterial.DIRT, "actions_survival", GameMode.ADVENTURE)
			GameMode.ADVENTURE -> Triple(XMaterial.GRASS_BLOCK, "actions_adventure", GameMode.CREATIVE)
			GameMode.CREATIVE -> Triple(XMaterial.BRICKS, "actions_creative", GameMode.SPECTATOR)
			GameMode.SPECTATOR -> Triple(XMaterial.SPLASH_POTION, "actions_spectator", GameMode.SURVIVAL)
		}

		val item = createClickableItem(material, messageService.getRaw(nameKey), messageService) {
			target.gameMode = nextMode
			open(viewer, target)
		}
		gui.setItem(slot, item)
	}

	private fun addGodModeItem(gui: Gui, slot: Int, viewer: Player, target: Player) {
		if (!viewer.hasPermission("admingui.god.other")) {
			gui.setItem(slot, createNoPermissionItem(messageService))
			return
		}

		val isGod = target.isInvulnerable || GuiManager.isGodMode(target)
		val (material, nameKey) = if (isGod) {
			XMaterial.RED_TERRACOTTA to "actions_god_disabled"
		} else {
			XMaterial.LIME_TERRACOTTA to "actions_god_enabled"
		}

		val item = createClickableItem(material, messageService.getRaw(nameKey), messageService) {
			val newState = !isGod
			target.isInvulnerable = newState
			GuiManager.setGodMode(target, newState)

			val messageKey = if (newState) "message_player_god_enabled" else "message_player_god_disabled"
			val targetMessageKey = if (newState) "message_target_player_god_enabled" else "message_target_player_god_disabled"

			messageService.send(viewer, messageKey, messageService.playerPlaceholder("player", target.name))
			messageService.send(target, targetMessageKey, messageService.playerPlaceholder("player", viewer.name))

			open(viewer, target)
		}
		gui.setItem(slot, item)
	}

	private fun addVanishItem(gui: Gui, slot: Int, viewer: Player, target: Player) {
		if (!viewer.hasPermission("admingui.vanish.other")) {
			gui.setItem(slot, createNoPermissionItem(messageService))
			return
		}

		val hasVanish = ctx.hookService.hasVanish
		val isVanished = hasVanish && runCatching { VanishAPI.isInvisible(target) }.getOrDefault(false)

		val nameKey = if (isVanished) "actions_vanish_disabled" else "actions_vanish_enabled"

		val item = createClickableItem(XMaterial.FEATHER, messageService.getRaw(nameKey), messageService) {
			if (!hasVanish) {
				messageService.send(viewer, "vanish_required")
				viewer.closeInventory()
				return@createClickableItem
			}
			runCatching {
				if (isVanished) {
					VanishAPI.showPlayer(target)
					messageService.send(viewer, "message_player_visible", messageService.playerPlaceholder("player", target.name))
					messageService.send(target, "message_visible")
				} else {
					VanishAPI.hidePlayer(target)
					messageService.send(viewer, "message_player_hide", messageService.playerPlaceholder("player", target.name))
					messageService.send(target, "message_hide")
				}
			}.onFailure {
				messageService.send(viewer, "vanish_required")
			}
			viewer.closeInventory()
		}
		gui.setItem(slot, item)
	}
}
