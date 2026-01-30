package dev.hytical.gui

import com.cryptomorin.xseries.XMaterial
import dev.hytical.ServiceContext
import dev.hytical.gui.GuiUtils.createClickableItem
import dev.hytical.gui.GuiUtils.fillBackground
import dev.triumphteam.gui.builder.item.ItemBuilder
import dev.triumphteam.gui.guis.Gui
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class MainGui(private val ctx: ServiceContext) {

	private val messageService get() = ctx.messageService

	fun open(player: Player) {
		val gui = Gui.gui()
			.title(messageService.getTitle("inventory_main"))
			.rows(3)
			.disableAllInteractions()
			.create()

		fillBackground(gui, 27, messageService = messageService)

		GuiManager.setTarget(player, player)
		val playerTitle = messageService.getRaw("main_player").replace("{player}", player.name)
		val playerHead = ItemBuilder.skull()
			.owner(player)
			.name(messageService.deserialize(playerTitle))
			.asGuiItem { event ->
				event.isCancelled = true
				ctx.createPlayerGui().open(player)
			}
		gui.setItem(11, playerHead)

		val worldItem = createClickableItem(
			XMaterial.GRASS_BLOCK,
			messageService.getRaw("main_world"),
			messageService
		) {
			ctx.createWorldGui().open(player)
		}
		gui.setItem(13, worldItem)

		val randomPlayer = Bukkit.getOnlinePlayers().firstOrNull() ?: player
		val playersHead = ItemBuilder.skull()
			.owner(randomPlayer)
			.name(messageService.deserialize(messageService.getRaw("main_players")))
			.asGuiItem {
				ctx.createPlayersListGui().open(player)
			}
		gui.setItem(15, playersHead)

		val maintenanceMaterial = if (GuiManager.maintenanceMode) {
			XMaterial.GLOWSTONE_DUST
		} else {
			XMaterial.REDSTONE
		}
		val maintenanceItem = createClickableItem(
			maintenanceMaterial,
			messageService.getRaw("main_maintenance_mode"),
			messageService
		) {
			handleMaintenanceToggle(player)
		}
		gui.setItem(18, maintenanceItem)

		val quitItem = createClickableItem(
			XMaterial.REDSTONE_BLOCK,
			messageService.getRaw("main_quit"),
			messageService
		) {
			player.closeInventory()
		}
		gui.setItem(26, quitItem)

		gui.open(player)
	}

	private fun handleMaintenanceToggle(player: Player) {
		if (!player.hasPermission("admingui.maintenance.manage")) {
			messageService.send(player, "permission")
			player.closeInventory()
			return
		}
		
		GuiManager.maintenanceMode = !GuiManager.maintenanceMode
		if (GuiManager.maintenanceMode) {
			messageService.send(player, "message_maintenance_enabled")
			Bukkit.getOnlinePlayers()
				.filter { !it.isOp && !it.hasPermission("admingui.maintenance") }
				.forEach { it.kickPlayer(messageService.getRaw("prefix") + messageService.getRaw("message_maintenance")) }
		} else {
			messageService.send(player, "message_maintenance_disabled")
		}
		player.closeInventory()
	}
}
