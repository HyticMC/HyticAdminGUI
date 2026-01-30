package dev.hytical.gui

import com.cryptomorin.xseries.XMaterial
import dev.hytical.ServiceContext
import dev.hytical.gui.GuiUtils.createClickableItem
import dev.hytical.gui.GuiUtils.fillBackground
import dev.hytical.gui.GuiUtils.toItemStack
import dev.triumphteam.gui.builder.item.ItemBuilder
import dev.triumphteam.gui.guis.Gui
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class PotionsGui(private val ctx: ServiceContext) {

	private val messageService get() = ctx.messageService

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

		fillBackground(gui, 36, messageService = messageService)

		POTION_EFFECTS.take(27).forEachIndexed { index, (nameKey, effectType) ->
			addPotionItem(gui, index, viewer, target, nameKey, effectType)
		}

		val duration = GuiManager.getPotionDuration(viewer)
		val durationItem = ItemBuilder.from(XMaterial.CLOCK.toItemStack())
			.name(messageService.deserialize(messageService.getRaw("potions_time")))
			.amount(duration.coerceIn(1, 64))
			.asGuiItem {
				it.isCancelled = true
				val newDuration = if (duration >= 60) 1 else duration + 1
				GuiManager.setPotionDuration(viewer, newDuration)
				open(viewer, target)
			}
		gui.setItem(30, durationItem)

		val removeItem = createClickableItem(XMaterial.RED_STAINED_GLASS_PANE, messageService.getRaw("potions_remove_all"), messageService) {
			target.activePotionEffects.forEach { target.removePotionEffect(it.type) }
			if (viewer == target) {
				messageService.send(viewer, "message_potions_remove")
			} else {
				messageService.send(viewer, "message_player_potions_remove", messageService.playerPlaceholder("player", target.name))
				messageService.send(target, "message_target_player_potions_remove", messageService.playerPlaceholder("player", viewer.name))
			}
		}
		gui.setItem(31, removeItem)

		val level = GuiManager.getPotionLevel(viewer)
		val levelItem = ItemBuilder.from(XMaterial.BEACON.toItemStack())
			.name(messageService.deserialize(messageService.getRaw("potions_level")))
			.amount(level.coerceIn(1, 64))
			.asGuiItem {
				it.isCancelled = true
				val newLevel = if (level >= 10) 1 else level + 1
				GuiManager.setPotionLevel(viewer, newLevel)
				open(viewer, target)
			}
		gui.setItem(32, levelItem)

		val backItem = createClickableItem(XMaterial.REDSTONE_BLOCK, messageService.getRaw("potions_back"), messageService) {
			if (viewer == target) {
				ctx.createPlayerGui().open(viewer)
			} else {
				ctx.createActionsGui().open(viewer, target)
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
		val item = ItemBuilder.from(XMaterial.POTION.toItemStack())
			.name(messageService.deserialize(messageService.getRaw(nameKey)))
			.asGuiItem {
				it.isCancelled = true
				val duration = GuiManager.getPotionDuration(viewer) * 60 * 20 // minutes to ticks
				val level = GuiManager.getPotionLevel(viewer) - 1 // 0-indexed amplifier

				target.addPotionEffect(PotionEffect(effectType, duration, level))

				val potionName = messageService.getRaw(nameKey)
				val time = GuiManager.getPotionDuration(viewer).toString()

				if (viewer == target) {
					messageService.send(viewer, "message_potions",
						messageService.placeholder("potion", potionName),
						messageService.placeholder("time", time))
				} else {
					messageService.send(viewer, "message_player_potions",
						messageService.playerPlaceholder("player", target.name),
						messageService.placeholder("potion", potionName),
						messageService.placeholder("time", time))
					messageService.send(target, "message_target_player_potions",
						messageService.playerPlaceholder("player", viewer.name),
						messageService.placeholder("potion", potionName),
						messageService.placeholder("time", time))
				}
			}
		gui.setItem(slot, item)
	}

	companion object {
		private val POTION_EFFECTS = listOf(
			"potions_night_vision" to PotionEffectType.NIGHT_VISION,
			"potions_invisibility" to PotionEffectType.INVISIBILITY,
			"potions_jump_boost" to PotionEffectType.JUMP_BOOST,
			"potions_fire_resistance" to PotionEffectType.FIRE_RESISTANCE,
			"potions_speed" to PotionEffectType.SPEED,
			"potions_slowness" to PotionEffectType.SLOWNESS,
			"potions_water_breathing" to PotionEffectType.WATER_BREATHING,
			"potions_instant_health" to PotionEffectType.INSTANT_HEALTH,
			"potions_instant_damage" to PotionEffectType.INSTANT_DAMAGE,
			"potions_poison" to PotionEffectType.POISON,
			"potions_regeneration" to PotionEffectType.REGENERATION,
			"potions_strength" to PotionEffectType.STRENGTH,
			"potions_weakness" to PotionEffectType.WEAKNESS,
			"potions_luck" to PotionEffectType.LUCK,
			"potions_slow_falling" to PotionEffectType.SLOW_FALLING
		)
	}
}
