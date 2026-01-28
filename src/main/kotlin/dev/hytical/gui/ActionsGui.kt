package dev.hytical.gui

import com.cryptomorin.xseries.XMaterial
import de.myzelyam.api.vanish.VanishAPI
import dev.hytical.AdminGUIPlugin
import dev.hytical.services.MessageService
import dev.triumphteam.gui.builder.item.ItemBuilder
import dev.triumphteam.gui.guis.Gui
import dev.triumphteam.gui.guis.GuiItem
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * Actions GUI for target player.
 */
class ActionsGui(
	private val plugin: AdminGUIPlugin,
	private val messageService: MessageService
) {

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
		fillBackground(gui)

		// Player info - slot 5 (0-indexed: 4)
		val playerHead = ItemBuilder.skull()
			.owner(target)
			.name(messageService.deserialize(messageService.getRaw("actions_info").replace("{player}", target.name)))
			.asGuiItem { it.isCancelled = true }
		gui.setItem(4, playerHead)

		// Heal - slot 11 (0-indexed: 10)
		addPermissionItem(
			gui, 10, viewer, "admingui.heal.other",
			XMaterial.GOLDEN_APPLE, "actions_heal"
		) {
			target.health = target.maxHealth
			target.fireTicks = 0
			messageService.send(viewer, "message_player_heal", messageService.playerPlaceholder("player", target.name))
			messageService.send(target, "message_target_player_heal", messageService.playerPlaceholder("player", viewer.name))
		}

		// Feed - slot 13 (0-indexed: 12)
		addPermissionItem(
			gui, 12, viewer, "admingui.feed.other",
			XMaterial.COOKED_BEEF, "actions_feed"
		) {
			target.foodLevel = 20
			messageService.send(viewer, "message_player_feed", messageService.playerPlaceholder("player", target.name))
			messageService.send(target, "message_target_player_feed", messageService.playerPlaceholder("player", viewer.name))
		}

		// Gamemode - slot 15 (0-indexed: 14)
		addGamemodeItem(gui, 14, viewer, target)

		// God mode - slot 17 (0-indexed: 16)
		addGodModeItem(gui, 16, viewer, target)

		// Teleport to player - slot 19 (0-indexed: 18)
		addPermissionItem(
			gui, 18, viewer, "admingui.teleport",
			XMaterial.ENDER_PEARL, "actions_teleport_to_player"
		) {
			viewer.closeInventory()
			viewer.teleport(target.location)
			messageService.send(
				viewer,
				"message_target_player_teleport",
				messageService.playerPlaceholder("player", target.name)
			)
		}

		// Potions - slot 21 (0-indexed: 20)
		addPermissionItem(
			gui, 20, viewer, "admingui.potions.other",
			XMaterial.POTION, "actions_potions"
		) {
			PotionsGui(plugin, messageService).open(viewer, target)
		}

		// Kill - slot 23 (0-indexed: 22)
		addPermissionItem(
			gui, 22, viewer, "admingui.kill.other",
			XMaterial.DIAMOND_SWORD, "actions_kill_player"
		) {
			target.health = 0.0
			messageService.send(viewer, "message_player_kill", messageService.playerPlaceholder("player", target.name))
		}

		// Spawner - slot 25 (0-indexed: 24)
		addPermissionItem(
			gui, 24, viewer, "admingui.spawner.other",
			XMaterial.SPAWNER, "actions_spawner"
		) {
			SpawnerGui(plugin, messageService).open(viewer, target)
		}

		// Teleport player to you - slot 27 (0-indexed: 26)
		addPermissionItem(
			gui, 26, viewer, "admingui.teleport.other",
			XMaterial.END_CRYSTAL, "actions_teleport_player_to_you"
		) {
			viewer.closeInventory()
			target.teleport(viewer.location)
			messageService.send(
				target,
				"message_target_player_teleport",
				messageService.playerPlaceholder("player", viewer.name)
			)
		}

		// Inventory - slot 29 (0-indexed: 28)
		addPermissionItem(
			gui, 28, viewer, "admingui.inventory",
			XMaterial.BOOK, "actions_inventory"
		) {
			InventoryViewerGui(plugin, messageService).open(viewer, target)
		}

		// Burn - slot 31 (0-indexed: 30)
		addPermissionItem(
			gui, 30, viewer, "admingui.burn.other",
			XMaterial.FLINT_AND_STEEL, "actions_burn_player"
		) {
			target.fireTicks = 500
			messageService.send(viewer, "message_player_burn", messageService.playerPlaceholder("player", target.name))
		}

		// Vanish - slot 33 (0-indexed: 32)
		addVanishItem(gui, 32, viewer, target)

		// Lightning - slot 35 (0-indexed: 34)
		addPermissionItem(
			gui, 34, viewer, "admingui.lightning.other",
			XMaterial.TRIDENT, "actions_lightning"
		) {
			target.world.strikeLightning(target.location)
		}

		// Firework - slot 37 (0-indexed: 36)
		addPermissionItem(
			gui, 36, viewer, "admingui.firework.other",
			XMaterial.FIREWORK_ROCKET, "actions_firework"
		) {
			FireworkUtil.createRandom(target)
		}

		// Fake OP - slot 39 (0-indexed: 38)
		addPermissionItem(
			gui, 38, viewer, "admingui.fakeop",
			XMaterial.PAPER, "actions_fakeop"
		) {
			Bukkit.broadcastMessage("§7§o[Server: Made ${target.name} a server operator]")
		}

		// Back - slot 54 (0-indexed: 53)
		val backItem = createClickableItem(XMaterial.REDSTONE_BLOCK, messageService.getRaw("actions_back")) {
			PlayerSettingsGui(plugin, messageService).open(viewer, target)
		}
		gui.setItem(53, backItem)

		gui.open(viewer)
	}

	private fun fillBackground(gui: Gui) {
		val filler = createItem(XMaterial.LIGHT_BLUE_STAINED_GLASS_PANE, " ")
		for (i in 0 until 54) {
			gui.setItem(i, filler)
		}
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
		if (viewer.hasPermission(permission)) {
			val item = createClickableItem(material, messageService.getRaw(nameKey), onClick)
			gui.setItem(slot, item)
		} else {
			val noPermItem = createItem(XMaterial.RED_STAINED_GLASS_PANE, messageService.getRaw("permission"))
			gui.setItem(slot, noPermItem)
		}
	}

	private fun addGamemodeItem(gui: Gui, slot: Int, viewer: Player, target: Player) {
		if (!viewer.hasPermission("admingui.gamemode.other")) {
			gui.setItem(slot, createItem(XMaterial.RED_STAINED_GLASS_PANE, messageService.getRaw("permission")))
			return
		}

		val (material, nameKey, nextMode) = when (target.gameMode) {
			GameMode.SURVIVAL -> Triple(XMaterial.DIRT, "actions_survival", GameMode.ADVENTURE)
			GameMode.ADVENTURE -> Triple(XMaterial.GRASS_BLOCK, "actions_adventure", GameMode.CREATIVE)
			GameMode.CREATIVE -> Triple(XMaterial.BRICKS, "actions_creative", GameMode.SPECTATOR)
			GameMode.SPECTATOR -> Triple(XMaterial.SPLASH_POTION, "actions_spectator", GameMode.SURVIVAL)
		}

		val item = createClickableItem(material, messageService.getRaw(nameKey)) {
			target.gameMode = nextMode
			open(viewer, target)
		}
		gui.setItem(slot, item)
	}

	private fun addGodModeItem(gui: Gui, slot: Int, viewer: Player, target: Player) {
		if (!viewer.hasPermission("admingui.god.other")) {
			gui.setItem(slot, createItem(XMaterial.RED_STAINED_GLASS_PANE, messageService.getRaw("permission")))
			return
		}

		val isGod = target.isInvulnerable || GuiManager.isGodMode(target)
		val (material, nameKey) = if (isGod) {
			Pair(XMaterial.RED_TERRACOTTA, "actions_god_disabled")
		} else {
			Pair(XMaterial.LIME_TERRACOTTA, "actions_god_enabled")
		}

		val item = createClickableItem(material, messageService.getRaw(nameKey)) {
			val newState = !isGod
			target.isInvulnerable = newState
			GuiManager.setGodMode(target, newState)
			if (newState) {
				messageService.send(
					viewer,
					"message_player_god_enabled",
					messageService.playerPlaceholder("player", target.name)
				)
				messageService.send(
					target,
					"message_target_player_god_enabled",
					messageService.playerPlaceholder("player", viewer.name)
				)
			} else {
				messageService.send(
					viewer,
					"message_player_god_disabled",
					messageService.playerPlaceholder("player", target.name)
				)
				messageService.send(
					target,
					"message_target_player_god_disabled",
					messageService.playerPlaceholder("player", viewer.name)
				)
			}
			open(viewer, target)
		}
		gui.setItem(slot, item)
	}

	private fun addVanishItem(gui: Gui, slot: Int, viewer: Player, target: Player) {
		if (!viewer.hasPermission("admingui.vanish.other")) {
			gui.setItem(slot, createItem(XMaterial.RED_STAINED_GLASS_PANE, messageService.getRaw("permission")))
			return
		}

		val hasVanish = plugin.hookService.hasVanish
		val isVanished = hasVanish && try {
			VanishAPI.isInvisible(target)
		} catch (e: Exception) {
			false
		}

		val nameKey = if (isVanished) "actions_vanish_disabled" else "actions_vanish_enabled"

		val item = createClickableItem(XMaterial.FEATHER, messageService.getRaw(nameKey)) {
			if (!hasVanish) {
				messageService.send(viewer, "vanish_required")
				viewer.closeInventory()
				return@createClickableItem
			}
			try {
				if (isVanished) {
					VanishAPI.showPlayer(target)
					messageService.send(viewer, "message_player_visible", messageService.playerPlaceholder("player", target.name))
					messageService.send(target, "message_visible")
				} else {
					VanishAPI.hidePlayer(target)
					messageService.send(viewer, "message_player_hide", messageService.playerPlaceholder("player", target.name))
					messageService.send(target, "message_hide")
				}
			} catch (e: Exception) {
				messageService.send(viewer, "vanish_required")
			}
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
