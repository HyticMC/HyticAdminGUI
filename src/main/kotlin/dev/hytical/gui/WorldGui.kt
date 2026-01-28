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
 * World settings GUI.
 */
class WorldGui(
	private val plugin: AdminGUIPlugin,
	private val messageService: MessageService
) {

	fun open(player: Player) {
		val gui = Gui.gui()
			.title(messageService.getTitle("inventory_world"))
			.rows(3)
			.disableAllInteractions()
			.create()

		// Fill background
		val filler = createItem(XMaterial.LIGHT_BLUE_STAINED_GLASS_PANE, " ")
		for (i in 0 until 27) {
			gui.setItem(i, filler)
		}

		val world = player.world

		// Time toggle - slot 11 (0-indexed: 10)
		if (player.hasPermission("admingui.time")) {
			val isDay = world.time < 13000
			val (material, nameKey) = if (isDay) {
				Pair(XMaterial.GOLD_BLOCK, "world_day")
			} else {
				Pair(XMaterial.COAL_BLOCK, "world_night")
			}
			val timeItem = createClickableItem(material, messageService.getRaw(nameKey)) {
				if (isDay) {
					world.time = 13000
				} else {
					world.time = 0
				}
				open(player) // Refresh
			}
			gui.setItem(10, timeItem)
		} else {
			gui.setItem(10, createItem(XMaterial.RED_STAINED_GLASS_PANE, messageService.getRaw("permission")))
		}

		// Weather - slot 13 (0-indexed: 12)
		if (player.hasPermission("admingui.weather")) {
			val (material, nameKey, nextAction) = when {
				world.isThundering -> Triple(XMaterial.BLUE_TERRACOTTA, "world_thunder", "clear")
				world.hasStorm() -> Triple(XMaterial.CYAN_TERRACOTTA, "world_rain", "thunder")
				else -> Triple(XMaterial.LIGHT_BLUE_TERRACOTTA, "world_clear", "rain")
			}
			val weatherItem = createClickableItem(material, messageService.getRaw(nameKey)) {
				when (nextAction) {
					"clear" -> {
						world.setStorm(false)
						world.isThundering = false
					}

					"rain" -> {
						world.setStorm(true)
						world.isThundering = false
					}

					"thunder" -> {
						world.setStorm(true)
						world.isThundering = true
					}
				}
				open(player) // Refresh
			}
			gui.setItem(12, weatherItem)
		} else {
			gui.setItem(12, createItem(XMaterial.RED_STAINED_GLASS_PANE, messageService.getRaw("permission")))
		}

		// Back - slot 27 (0-indexed: 26)
		val backItem = createClickableItem(XMaterial.REDSTONE_BLOCK, messageService.getRaw("world_back")) {
			MainGui(plugin, messageService).open(player)
		}
		gui.setItem(26, backItem)

		gui.open(player)
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
