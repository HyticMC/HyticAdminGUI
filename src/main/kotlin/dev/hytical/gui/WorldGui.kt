package dev.hytical.gui

import com.cryptomorin.xseries.XMaterial
import dev.hytical.ServiceContext
import dev.hytical.gui.GuiUtils.createClickableItem
import dev.hytical.gui.GuiUtils.createNoPermissionItem
import dev.hytical.gui.GuiUtils.fillBackground
import dev.triumphteam.gui.guis.Gui
import org.bukkit.entity.Player

class WorldGui(private val ctx: ServiceContext) {

	private val messageService get() = ctx.messageService

	fun open(player: Player) {
		val gui = Gui.gui()
			.title(messageService.getTitle("inventory_world"))
			.rows(3)
			.disableAllInteractions()
			.create()

		fillBackground(gui, 27, messageService = messageService)

		val world = player.world

		gui.setItem(10, if (player.hasPermission("admingui.time")) {
			val isDay = world.time < 13000
			val (material, nameKey) = if (isDay) {
				XMaterial.GOLD_BLOCK to "world_day"
			} else {
				XMaterial.COAL_BLOCK to "world_night"
			}
			createClickableItem(material, messageService.getRaw(nameKey), messageService) {
				world.time = if (isDay) 13000 else 0
				open(player)
			}
		} else {
			createNoPermissionItem(messageService)
		})

		gui.setItem(12, if (player.hasPermission("admingui.weather")) {
			val (material, nameKey, nextAction) = when {
				world.isThundering -> Triple(XMaterial.BLUE_TERRACOTTA, "world_thunder", WeatherAction.CLEAR)
				world.hasStorm() -> Triple(XMaterial.CYAN_TERRACOTTA, "world_rain", WeatherAction.THUNDER)
				else -> Triple(XMaterial.LIGHT_BLUE_TERRACOTTA, "world_clear", WeatherAction.RAIN)
			}
			createClickableItem(material, messageService.getRaw(nameKey), messageService) {
				when (nextAction) {
					WeatherAction.CLEAR -> {
						world.setStorm(false)
						world.isThundering = false
					}
					WeatherAction.RAIN -> {
						world.setStorm(true)
						world.isThundering = false
					}
					WeatherAction.THUNDER -> {
						world.setStorm(true)
						world.isThundering = true
					}
				}
				open(player)
			}
		} else {
			createNoPermissionItem(messageService)
		})

		val backItem = createClickableItem(XMaterial.REDSTONE_BLOCK, messageService.getRaw("world_back"), messageService) {
			ctx.createMainGui().open(player)
		}
		gui.setItem(26, backItem)

		gui.open(player)
	}

	private enum class WeatherAction {
		CLEAR, RAIN, THUNDER
	}
}


