package dev.hytical.gui

import com.cryptomorin.xseries.XMaterial
import de.myzelyam.api.vanish.VanishAPI
import dev.hytical.AdminGUIPlugin
import dev.hytical.services.MessageService
import dev.triumphteam.gui.builder.item.ItemBuilder
import dev.triumphteam.gui.guis.Gui
import dev.triumphteam.gui.guis.GuiItem
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * Player self-management GUI.
 */
class PlayerGui(
	private val plugin: AdminGUIPlugin,
	private val messageService: MessageService
) {

	fun open(player: Player) {
		val title = messageService.getRaw("inventory_player").replace("{player}", player.name)
		val gui = Gui.gui()
			.title(messageService.deserialize(title))
			.rows(5)
			.disableAllInteractions()
			.create()

		// Fill background
		fillBackground(gui)

		// Player info - slot 5 (0-indexed: 4)
		val infoLore = buildInfoLore(player)
		val playerHead = ItemBuilder.skull()
			.owner(player)
			.name(messageService.deserialize(messageService.getRaw("player_info").replace("{player}", player.name)))
			.lore(infoLore)
			.asGuiItem { it.isCancelled = true }
		gui.setItem(4, playerHead)

		// Heal - slot 11 (0-indexed: 10)
		addPermissionItem(
			gui, 10, player, "admingui.heal",
			XMaterial.GOLDEN_APPLE, "player_heal"
		) {
			player.health = player.maxHealth
			player.fireTicks = 0
			messageService.send(player, "message_heal")
			player.closeInventory()
		}

		// Feed - slot 13 (0-indexed: 12)
		addPermissionItem(
			gui, 12, player, "admingui.feed",
			XMaterial.COOKED_BEEF, "player_feed"
		) {
			player.foodLevel = 20
			messageService.send(player, "message_feed")
			player.closeInventory()
		}

		// Gamemode - slot 15 (0-indexed: 14)
		addGamemodeItem(gui, 14, player)

		// God mode - slot 17 (0-indexed: 16)
		addGodModeItem(gui, 16, player)

		// Potions - slot 19 (0-indexed: 18)
		addPermissionItem(
			gui, 18, player, "admingui.potions",
			XMaterial.POTION, "player_potions"
		) {
			PotionsGui(plugin, messageService).open(player, player)
		}

		// Spawner - slot 21 (0-indexed: 20)
		addPermissionItem(
			gui, 20, player, "admingui.spawner",
			XMaterial.SPAWNER, "player_spawner"
		) {
			SpawnerGui(plugin, messageService).open(player, player)
		}

		// Kill - slot 23 (0-indexed: 22)
		addPermissionItem(
			gui, 22, player, "admingui.kill",
			XMaterial.DIAMOND_SWORD, "player_kill"
		) {
			player.health = 0.0
			messageService.send(player, "message_kill")
		}

		// Burn - slot 25 (0-indexed: 24)
		addPermissionItem(
			gui, 24, player, "admingui.burn",
			XMaterial.FLINT_AND_STEEL, "player_burn"
		) {
			player.fireTicks = 500
			messageService.send(player, "message_burn")
		}

		// Lightning - slot 27 (0-indexed: 26)
		addPermissionItem(
			gui, 26, player, "admingui.lightning",
			XMaterial.TRIDENT, "player_lightning"
		) {
			player.world.strikeLightning(player.location)
		}

		// Firework - slot 29 (0-indexed: 28)
		addPermissionItem(
			gui, 28, player, "admingui.firework",
			XMaterial.FIREWORK_ROCKET, "player_firework"
		) {
			FireworkUtil.createRandom(player)
		}

		// Money - slot 31 (0-indexed: 30)
		addPermissionItem(
			gui, 30, player, "admingui.money",
			XMaterial.PAPER, "player_money"
		) {
			MoneyGui(plugin, messageService).open(player, player)
		}

		// Vanish - slot 33 (0-indexed: 32)
		addVanishItem(gui, 32, player)

		// Back - slot 45 (0-indexed: 44)
		val backItem = createClickableItem(XMaterial.REDSTONE_BLOCK, messageService.getRaw("player_back")) {
			MainGui(plugin, messageService).open(player)
		}
		gui.setItem(44, backItem)

		gui.open(player)
	}

	private fun fillBackground(gui: Gui) {
		val filler = createItem(XMaterial.LIGHT_BLUE_STAINED_GLASS_PANE, " ")
		for (i in 0 until 45) {
			gui.setItem(i, filler)
		}
	}

	private fun buildInfoLore(player: Player): List<net.kyori.adventure.text.Component> {
		val lore = mutableListOf<net.kyori.adventure.text.Component>()
		lore.add(messageService.deserialize("<yellow>Health: ${player.health.toInt()}"))
		lore.add(messageService.deserialize("<gray>Food: ${player.foodLevel}"))
		if (plugin.hookService.hasVault) {
			val balance = plugin.economyService.format(plugin.economyService.getBalance(player))
			lore.add(messageService.deserialize("<green>Money: $balance"))
		}
		lore.add(messageService.deserialize("<green>Gamemode: ${player.gameMode.name}"))
		return lore
	}

	private fun addPermissionItem(
		gui: Gui,
		slot: Int,
		player: Player,
		permission: String,
		material: XMaterial,
		nameKey: String,
		onClick: () -> Unit
	) {
		if (player.hasPermission(permission)) {
			val item = createClickableItem(material, messageService.getRaw(nameKey), onClick)
			gui.setItem(slot, item)
		} else {
			val noPermItem = createItem(XMaterial.RED_STAINED_GLASS_PANE, messageService.getRaw("permission"))
			gui.setItem(slot, noPermItem)
		}
	}

	private fun addGamemodeItem(gui: Gui, slot: Int, player: Player) {
		if (!player.hasPermission("admingui.gamemode")) {
			val noPermItem = createItem(XMaterial.RED_STAINED_GLASS_PANE, messageService.getRaw("permission"))
			gui.setItem(slot, noPermItem)
			return
		}

		val (material, nameKey, nextMode) = when (player.gameMode) {
			GameMode.SURVIVAL -> Triple(XMaterial.DIRT, "player_survival", GameMode.ADVENTURE)
			GameMode.ADVENTURE -> Triple(XMaterial.GRASS_BLOCK, "player_adventure", GameMode.CREATIVE)
			GameMode.CREATIVE -> Triple(XMaterial.BRICKS, "player_creative", GameMode.SPECTATOR)
			GameMode.SPECTATOR -> Triple(XMaterial.SPLASH_POTION, "player_spectator", GameMode.SURVIVAL)
		}

		val item = createClickableItem(material, messageService.getRaw(nameKey)) {
			player.gameMode = nextMode
			open(player) // Refresh
		}
		gui.setItem(slot, item)
	}

	private fun addGodModeItem(gui: Gui, slot: Int, player: Player) {
		if (!player.hasPermission("admingui.god")) {
			val noPermItem = createItem(XMaterial.RED_STAINED_GLASS_PANE, messageService.getRaw("permission"))
			gui.setItem(slot, noPermItem)
			return
		}

		val isGod = player.isInvulnerable || GuiManager.isGodMode(player)
		val (material, nameKey) = if (isGod) {
			Pair(XMaterial.RED_TERRACOTTA, "player_god_disabled")
		} else {
			Pair(XMaterial.LIME_TERRACOTTA, "player_god_enabled")
		}

		val item = createClickableItem(material, messageService.getRaw(nameKey)) {
			val newState = !isGod
			player.isInvulnerable = newState
			GuiManager.setGodMode(player, newState)
			if (newState) {
				messageService.send(player, "message_god_enabled")
			} else {
				messageService.send(player, "message_god_disabled")
			}
			open(player) // Refresh
		}
		gui.setItem(slot, item)
	}

	private fun addVanishItem(gui: Gui, slot: Int, player: Player) {
		if (!player.hasPermission("admingui.vanish")) {
			val noPermItem = createItem(XMaterial.RED_STAINED_GLASS_PANE, messageService.getRaw("permission"))
			gui.setItem(slot, noPermItem)
			return
		}

		val hasVanish = plugin.hookService.hasVanish
		val isVanished = hasVanish && try {
			VanishAPI.isInvisible(player)
		} catch (e: Exception) {
			false
		}

		val nameKey = if (isVanished) "player_vanish_disabled" else "player_vanish_enabled"

		val item = createClickableItem(XMaterial.FEATHER, messageService.getRaw(nameKey)) {
			if (!hasVanish) {
				messageService.send(player, "vanish_required")
				player.closeInventory()
				return@createClickableItem
			}
			try {
				if (isVanished) {
					VanishAPI.showPlayer(player)
					messageService.send(player, "message_visible")
				} else {
					VanishAPI.hidePlayer(player)
					messageService.send(player, "message_hide")
				}
			} catch (e: Exception) {
				messageService.send(player, "vanish_required")
			}
			player.closeInventory()
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

/**
 * Utility for creating random fireworks.
 */
object FireworkUtil {
	fun createRandom(player: Player) {
		val world = player.world
		val firework = world.spawn(player.location, org.bukkit.entity.Firework::class.java)
		val meta = firework.fireworkMeta
		val effect = org.bukkit.FireworkEffect.builder()
			.with(org.bukkit.FireworkEffect.Type.values().random())
			.withColor(
				org.bukkit.Color.fromRGB(
					(0..255).random(),
					(0..255).random(),
					(0..255).random()
				)
			)
			.withFade(
				org.bukkit.Color.fromRGB(
					(0..255).random(),
					(0..255).random(),
					(0..255).random()
				)
			)
			.flicker((0..1).random() == 1)
			.trail((0..1).random() == 1)
			.build()
		meta.addEffect(effect)
		meta.power = (0..2).random()
		firework.fireworkMeta = meta
	}
}
