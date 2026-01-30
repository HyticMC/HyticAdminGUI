package dev.hytical.gui

import com.cryptomorin.xseries.XMaterial
import de.myzelyam.api.vanish.VanishAPI
import dev.hytical.ServiceContext
import dev.hytical.gui.GuiUtils.createClickableItem
import dev.hytical.gui.GuiUtils.createNoPermissionItem
import dev.hytical.gui.GuiUtils.fillBackground
import dev.triumphteam.gui.builder.item.ItemBuilder
import dev.triumphteam.gui.guis.Gui
import net.kyori.adventure.text.Component
import org.bukkit.GameMode
import org.bukkit.entity.Player

class PlayerGui(private val ctx: ServiceContext) {

	private val messageService get() = ctx.messageService

	fun open(player: Player) {
		val title = messageService.getRaw("inventory_player").replace("{player}", player.name)
		val gui = Gui.gui()
			.title(messageService.deserialize(title))
			.rows(5)
			.disableAllInteractions()
			.create()

		fillBackground(gui, 45, messageService = messageService)

		val infoLore = buildInfoLore(player)
		val playerHead = ItemBuilder.skull()
			.owner(player)
			.name(messageService.deserialize(messageService.getRaw("player_info").replace("{player}", player.name)))
			.lore(infoLore)
			.asGuiItem { it.isCancelled = true }
		gui.setItem(4, playerHead)

		addPermissionItem(gui, 10, player, "admingui.heal", XMaterial.GOLDEN_APPLE, "player_heal") {
			player.health = player.maxHealth
			player.fireTicks = 0
			messageService.send(player, "message_heal")
			player.closeInventory()
		}

		addPermissionItem(gui, 12, player, "admingui.feed", XMaterial.COOKED_BEEF, "player_feed") {
			player.foodLevel = 20
			messageService.send(player, "message_feed")
			player.closeInventory()
		}

		addGamemodeItem(gui, 14, player)
		addGodModeItem(gui, 16, player)

		addPermissionItem(gui, 18, player, "admingui.potions", XMaterial.POTION, "player_potions") {
			ctx.createPotionsGui().open(player, player)
		}

		addPermissionItem(gui, 20, player, "admingui.spawner", XMaterial.SPAWNER, "player_spawner") {
			ctx.createSpawnerGui().open(player, player)
		}

		addPermissionItem(gui, 22, player, "admingui.kill", XMaterial.DIAMOND_SWORD, "player_kill") {
			player.health = 0.0
			messageService.send(player, "message_kill")
		}

		addPermissionItem(gui, 24, player, "admingui.burn", XMaterial.FLINT_AND_STEEL, "player_burn") {
			player.fireTicks = 500
			messageService.send(player, "message_burn")
		}

		addPermissionItem(gui, 26, player, "admingui.lightning", XMaterial.TRIDENT, "player_lightning") {
			player.world.strikeLightning(player.location)
		}

		addPermissionItem(gui, 28, player, "admingui.firework", XMaterial.FIREWORK_ROCKET, "player_firework") {
			FireworkUtil.createRandom(player)
		}

		addPermissionItem(gui, 30, player, "admingui.money", XMaterial.PAPER, "player_money") {
			ctx.createMoneyGui().open(player, player)
		}

		addVanishItem(gui, 32, player)

		val backItem = createClickableItem(XMaterial.REDSTONE_BLOCK, messageService.getRaw("player_back"), messageService) {
			ctx.createMainGui().open(player)
		}
		gui.setItem(44, backItem)

		gui.open(player)
	}

	private fun buildInfoLore(player: Player): List<Component> = buildList {
		add(messageService.deserialize("<yellow>Health: ${player.health.toInt()}"))
		add(messageService.deserialize("<gray>Food: ${player.foodLevel}"))
		if (ctx.hookService.hasVault) {
			val balance = ctx.economyService.format(ctx.economyService.getBalance(player))
			add(messageService.deserialize("<green>Money: $balance"))
		}
		add(messageService.deserialize("<green>Gamemode: ${player.gameMode.name}"))
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
		val item = if (player.hasPermission(permission)) {
			createClickableItem(material, messageService.getRaw(nameKey), messageService, onClick)
		} else {
			createNoPermissionItem(messageService)
		}
		gui.setItem(slot, item)
	}

	private fun addGamemodeItem(gui: Gui, slot: Int, player: Player) {
		if (!player.hasPermission("admingui.gamemode")) {
			gui.setItem(slot, createNoPermissionItem(messageService))
			return
		}

		val (material, nameKey, nextMode) = when (player.gameMode) {
			GameMode.SURVIVAL -> Triple(XMaterial.DIRT, "player_survival", GameMode.ADVENTURE)
			GameMode.ADVENTURE -> Triple(XMaterial.GRASS_BLOCK, "player_adventure", GameMode.CREATIVE)
			GameMode.CREATIVE -> Triple(XMaterial.BRICKS, "player_creative", GameMode.SPECTATOR)
			GameMode.SPECTATOR -> Triple(XMaterial.SPLASH_POTION, "player_spectator", GameMode.SURVIVAL)
		}

		val item = createClickableItem(material, messageService.getRaw(nameKey), messageService) {
			player.gameMode = nextMode
			open(player)
		}
		gui.setItem(slot, item)
	}

	private fun addGodModeItem(gui: Gui, slot: Int, player: Player) {
		if (!player.hasPermission("admingui.god")) {
			gui.setItem(slot, createNoPermissionItem(messageService))
			return
		}

		val isGod = player.isInvulnerable || GuiManager.isGodMode(player)
		val (material, nameKey) = if (isGod) {
			XMaterial.RED_TERRACOTTA to "player_god_disabled"
		} else {
			XMaterial.LIME_TERRACOTTA to "player_god_enabled"
		}

		val item = createClickableItem(material, messageService.getRaw(nameKey), messageService) {
			val newState = !isGod
			player.isInvulnerable = newState
			GuiManager.setGodMode(player, newState)
			messageService.send(player, if (newState) "message_god_enabled" else "message_god_disabled")
			open(player)
		}
		gui.setItem(slot, item)
	}

	private fun addVanishItem(gui: Gui, slot: Int, player: Player) {
		if (!player.hasPermission("admingui.vanish")) {
			gui.setItem(slot, createNoPermissionItem(messageService))
			return
		}

		val hasVanish = ctx.hookService.hasVanish
		val isVanished = hasVanish && runCatching { VanishAPI.isInvisible(player) }.getOrDefault(false)

		val nameKey = if (isVanished) "player_vanish_disabled" else "player_vanish_enabled"

		val item = createClickableItem(XMaterial.FEATHER, messageService.getRaw(nameKey), messageService) {
			if (!hasVanish) {
				messageService.send(player, "vanish_required")
				player.closeInventory()
				return@createClickableItem
			}
			runCatching {
				if (isVanished) {
					VanishAPI.showPlayer(player)
					messageService.send(player, "message_visible")
				} else {
					VanishAPI.hidePlayer(player)
					messageService.send(player, "message_hide")
				}
			}.onFailure {
				messageService.send(player, "vanish_required")
			}
			player.closeInventory()
		}
		gui.setItem(slot, item)
	}
}

object FireworkUtil {
	fun createRandom(player: Player) {
		val world = player.world
		val firework = world.spawn(player.location, org.bukkit.entity.Firework::class.java)
		val meta = firework.fireworkMeta
		val effect = org.bukkit.FireworkEffect.builder()
			.with(org.bukkit.FireworkEffect.Type.entries.random())
			.withColor(org.bukkit.Color.fromRGB((0..255).random(), (0..255).random(), (0..255).random()))
			.withFade(org.bukkit.Color.fromRGB((0..255).random(), (0..255).random(), (0..255).random()))
			.flicker((0..1).random() == 1)
			.trail((0..1).random() == 1)
			.build()
		meta.addEffect(effect)
		meta.power = (0..2).random()
		firework.fireworkMeta = meta
	}
}
