package dev.hytical.gui

import com.cryptomorin.xseries.XMaterial
import dev.hytical.AdminGUIPlugin
import dev.hytical.services.MessageService
import dev.triumphteam.gui.builder.item.ItemBuilder
import dev.triumphteam.gui.guis.Gui
import dev.triumphteam.gui.guis.GuiItem
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

/**
 * Potions effect selection GUI.
 */
class PotionsGui(
	private val plugin: AdminGUIPlugin,
	private val messageService: MessageService
) {

	private val potionEffects = listOf(
		Pair("potions_night_vision", PotionEffectType.NIGHT_VISION),
		Pair("potions_invisibility", PotionEffectType.INVISIBILITY),
		Pair("potions_jump_boost", PotionEffectType.JUMP_BOOST),
		Pair("potions_fire_resistance", PotionEffectType.FIRE_RESISTANCE),
		Pair("potions_speed", PotionEffectType.SPEED),
		Pair("potions_slowness", PotionEffectType.SLOWNESS),
		Pair("potions_water_breathing", PotionEffectType.WATER_BREATHING),
		Pair("potions_instant_health", PotionEffectType.INSTANT_HEALTH),
		Pair("potions_instant_damage", PotionEffectType.INSTANT_DAMAGE),
		Pair("potions_poison", PotionEffectType.POISON),
		Pair("potions_regeneration", PotionEffectType.REGENERATION),
		Pair("potions_strength", PotionEffectType.STRENGTH),
		Pair("potions_weakness", PotionEffectType.WEAKNESS),
		Pair("potions_luck", PotionEffectType.LUCK),
		Pair("potions_slow_falling", PotionEffectType.SLOW_FALLING)
	)

	fun open(viewer: Player, target: Player) {
		if (!target.isOnline) {
			messageService.send(viewer, "message_player_not_found")
			viewer.closeInventory()
			return
		}

		val title = messageService.getRaw("inventory_potions").replace("{player}", target.name)
		val gui = Gui.gui()
			.title(messageService.deserialize(title))
			.rows(4)
			.disableAllInteractions()
			.create()

		GuiManager.setTarget(viewer, target)

		// Fill background
		val filler = createItem(XMaterial.LIGHT_BLUE_STAINED_GLASS_PANE, " ")
		for (i in 0 until 36) {
			gui.setItem(i, filler)
		}

		// Potion effects (slots 0-14)
		potionEffects.forEachIndexed { index, (nameKey, effectType) ->
			if (index < 27) {
				addPotionItem(gui, index, viewer, target, nameKey, effectType)
			}
		}

		// Duration control - slot 31 (0-indexed: 30)
		val duration = GuiManager.getPotionDuration(viewer)
		val durationItem = ItemBuilder.from(XMaterial.CLOCK.parseItem() ?: ItemStack(org.bukkit.Material.CLOCK))
			.name(messageService.deserialize(messageService.getRaw("potions_time")))
			.amount(duration.coerceIn(1, 64))
			.asGuiItem {
				it.isCancelled = true
				val newDuration = if (duration >= 60) 1 else duration + 1
				GuiManager.setPotionDuration(viewer, newDuration)
				open(viewer, target)
			}
		gui.setItem(30, durationItem)

		// Remove all - slot 32 (0-indexed: 31)
		val removeItem =
			createClickableItem(XMaterial.RED_STAINED_GLASS_PANE, messageService.getRaw("potions_remove_all")) {
				for (effect in target.activePotionEffects) {
					target.removePotionEffect(effect.type)
				}
				if (viewer == target) {
					messageService.send(viewer, "message_potions_remove")
				} else {
					messageService.send(
						viewer,
						"message_player_potions_remove",
						messageService.playerPlaceholder("player", target.name)
					)
					messageService.send(
						target,
						"message_target_player_potions_remove",
						messageService.playerPlaceholder("player", viewer.name)
					)
				}
			}
		gui.setItem(31, removeItem)

		// Level control - slot 33 (0-indexed: 32)
		val level = GuiManager.getPotionLevel(viewer)
		val levelItem = ItemBuilder.from(XMaterial.BEACON.parseItem() ?: ItemStack(org.bukkit.Material.BEACON))
			.name(messageService.deserialize(messageService.getRaw("potions_level")))
			.amount(level.coerceIn(1, 64))
			.asGuiItem {
				it.isCancelled = true
				val newLevel = if (level >= 10) 1 else level + 1
				GuiManager.setPotionLevel(viewer, newLevel)
				open(viewer, target)
			}
		gui.setItem(32, levelItem)

		// Back - slot 36 (0-indexed: 35)
		val backItem = createClickableItem(XMaterial.REDSTONE_BLOCK, messageService.getRaw("potions_back")) {
			if (viewer == target) {
				PlayerGui(plugin, messageService).open(viewer)
			} else {
				ActionsGui(plugin, messageService).open(viewer, target)
			}
		}
		gui.setItem(35, backItem)

		gui.open(viewer)
	}

	private fun addPotionItem(
		gui: Gui,
		slot: Int,
		viewer: Player,
		target: Player,
		nameKey: String,
		effectType: PotionEffectType
	) {
		val item = ItemBuilder.from(XMaterial.POTION.parseItem() ?: ItemStack(org.bukkit.Material.POTION))
			.name(messageService.deserialize(messageService.getRaw(nameKey)))
			.asGuiItem {
				it.isCancelled = true
				val duration = GuiManager.getPotionDuration(viewer) * 60 * 20 // minutes to ticks
				val level = GuiManager.getPotionLevel(viewer) - 1 // 0-indexed amplifier

				target.addPotionEffect(PotionEffect(effectType, duration, level))

				val potionName = messageService.getRaw(nameKey)
				val time = GuiManager.getPotionDuration(viewer).toString()

				if (viewer == target) {
					messageService.send(
						viewer, "message_potions",
						messageService.placeholder("potion", potionName),
						messageService.placeholder("time", time)
					)
				} else {
					messageService.send(
						viewer, "message_player_potions",
						messageService.playerPlaceholder("player", target.name),
						messageService.placeholder("potion", potionName),
						messageService.placeholder("time", time)
					)
					messageService.send(
						target, "message_target_player_potions",
						messageService.playerPlaceholder("player", viewer.name),
						messageService.placeholder("potion", potionName),
						messageService.placeholder("time", time)
					)
				}
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
