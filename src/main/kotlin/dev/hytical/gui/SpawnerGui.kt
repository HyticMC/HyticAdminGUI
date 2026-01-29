package dev.hytical.gui

import com.cryptomorin.xseries.XMaterial
import dev.hytical.AdminGUIPlugin
import dev.hytical.services.MessageService
import dev.triumphteam.gui.builder.item.ItemBuilder
import dev.triumphteam.gui.guis.Gui
import dev.triumphteam.gui.guis.GuiItem
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class SpawnerGui(
	private val plugin: AdminGUIPlugin,
	private val messageService: MessageService
) {

	private val entities = listOf(
		Triple("spawner_bat", EntityType.BAT, XMaterial.BAT_SPAWN_EGG),
		Triple("spawner_bee", EntityType.BEE, XMaterial.BEE_SPAWN_EGG),
		Triple("spawner_blaze", EntityType.BLAZE, XMaterial.BLAZE_SPAWN_EGG),
		Triple("spawner_cat", EntityType.CAT, XMaterial.CAT_SPAWN_EGG),
		Triple("spawner_cave_spider", EntityType.CAVE_SPIDER, XMaterial.CAVE_SPIDER_SPAWN_EGG),
		Triple("spawner_chicken", EntityType.CHICKEN, XMaterial.CHICKEN_SPAWN_EGG),
		Triple("spawner_cod", EntityType.COD, XMaterial.COD_SPAWN_EGG),
		Triple("spawner_cow", EntityType.COW, XMaterial.COW_SPAWN_EGG),
		Triple("spawner_creeper", EntityType.CREEPER, XMaterial.CREEPER_SPAWN_EGG),
		Triple("spawner_dolphin", EntityType.DOLPHIN, XMaterial.DOLPHIN_SPAWN_EGG),
		Triple("spawner_donkey", EntityType.DONKEY, XMaterial.DONKEY_SPAWN_EGG),
		Triple("spawner_drowned", EntityType.DROWNED, XMaterial.DROWNED_SPAWN_EGG),
		Triple("spawner_elder_guardian", EntityType.ELDER_GUARDIAN, XMaterial.ELDER_GUARDIAN_SPAWN_EGG),
		Triple("spawner_enderman", EntityType.ENDERMAN, XMaterial.ENDERMAN_SPAWN_EGG),
		Triple("spawner_endermite", EntityType.ENDERMITE, XMaterial.ENDERMITE_SPAWN_EGG),
		Triple("spawner_evoker", EntityType.EVOKER, XMaterial.EVOKER_SPAWN_EGG),
		Triple("spawner_fox", EntityType.FOX, XMaterial.FOX_SPAWN_EGG),
		Triple("spawner_ghast", EntityType.GHAST, XMaterial.GHAST_SPAWN_EGG),
		Triple("spawner_guardian", EntityType.GUARDIAN, XMaterial.GUARDIAN_SPAWN_EGG),
		Triple("spawner_horse", EntityType.HORSE, XMaterial.HORSE_SPAWN_EGG),
		Triple("spawner_husk", EntityType.HUSK, XMaterial.HUSK_SPAWN_EGG),
		Triple("spawner_llama", EntityType.LLAMA, XMaterial.LLAMA_SPAWN_EGG),
		Triple("spawner_magma_cube", EntityType.MAGMA_CUBE, XMaterial.MAGMA_CUBE_SPAWN_EGG),
		Triple("spawner_mooshroom", EntityType.MOOSHROOM, XMaterial.MOOSHROOM_SPAWN_EGG),
		Triple("spawner_mule", EntityType.MULE, XMaterial.MULE_SPAWN_EGG),
		Triple("spawner_ocelot", EntityType.OCELOT, XMaterial.OCELOT_SPAWN_EGG),
		Triple("spawner_panda", EntityType.PANDA, XMaterial.PANDA_SPAWN_EGG),
		Triple("spawner_parrot", EntityType.PARROT, XMaterial.PARROT_SPAWN_EGG),
		Triple("spawner_phantom", EntityType.PHANTOM, XMaterial.PHANTOM_SPAWN_EGG),
		Triple("spawner_pig", EntityType.PIG, XMaterial.PIG_SPAWN_EGG),
		Triple("spawner_pillager", EntityType.PILLAGER, XMaterial.PILLAGER_SPAWN_EGG),
		Triple("spawner_polar_bear", EntityType.POLAR_BEAR, XMaterial.POLAR_BEAR_SPAWN_EGG),
		Triple("spawner_pufferfish", EntityType.PUFFERFISH, XMaterial.PUFFERFISH_SPAWN_EGG),
		Triple("spawner_rabbit", EntityType.RABBIT, XMaterial.RABBIT_SPAWN_EGG),
		Triple("spawner_ravager", EntityType.RAVAGER, XMaterial.RAVAGER_SPAWN_EGG),
		Triple("spawner_salmon", EntityType.SALMON, XMaterial.SALMON_SPAWN_EGG),
		Triple("spawner_sheep", EntityType.SHEEP, XMaterial.SHEEP_SPAWN_EGG),
		Triple("spawner_shulker", EntityType.SHULKER, XMaterial.SHULKER_SPAWN_EGG),
		Triple("spawner_silverfish", EntityType.SILVERFISH, XMaterial.SILVERFISH_SPAWN_EGG),
		Triple("spawner_skeleton", EntityType.SKELETON, XMaterial.SKELETON_SPAWN_EGG),
		Triple("spawner_skeleton_horse", EntityType.SKELETON_HORSE, XMaterial.SKELETON_HORSE_SPAWN_EGG),
		Triple("spawner_slime", EntityType.SLIME, XMaterial.SLIME_SPAWN_EGG),
		Triple("spawner_spider", EntityType.SPIDER, XMaterial.SPIDER_SPAWN_EGG),
		Triple("spawner_squid", EntityType.SQUID, XMaterial.SQUID_SPAWN_EGG),
		Triple("spawner_stray", EntityType.STRAY, XMaterial.STRAY_SPAWN_EGG),
		Triple("spawner_trader_llama", EntityType.TRADER_LLAMA, XMaterial.TRADER_LLAMA_SPAWN_EGG),
		Triple("spawner_tropical_fish", EntityType.TROPICAL_FISH, XMaterial.TROPICAL_FISH_SPAWN_EGG),
		Triple("spawner_turtle", EntityType.TURTLE, XMaterial.TURTLE_SPAWN_EGG),
		Triple("spawner_vex", EntityType.VEX, XMaterial.VEX_SPAWN_EGG),
		Triple("spawner_villager", EntityType.VILLAGER, XMaterial.VILLAGER_SPAWN_EGG),
		Triple("spawner_vindicator", EntityType.VINDICATOR, XMaterial.VINDICATOR_SPAWN_EGG),
		Triple("spawner_wandering_trader", EntityType.WANDERING_TRADER, XMaterial.WANDERING_TRADER_SPAWN_EGG),
		Triple("spawner_witch", EntityType.WITCH, XMaterial.WITCH_SPAWN_EGG)
	)

	fun open(viewer: Player, target: Player) {
		if (!target.isOnline) {
			messageService.send(viewer, "message_player_not_found")
			viewer.closeInventory()
			return
		}

		val title = messageService.getRaw("inventory_spawner").replace("{player}", target.name)
		val gui = Gui.gui()
			.title(messageService.deserialize(title))
			.rows(6)
			.disableAllInteractions()
			.create()

		GuiManager.setTarget(viewer, target)

		entities.take(53).forEachIndexed { index, (nameKey, entityType, material) ->
			val item = ItemBuilder.from(material.parseItem() ?: ItemStack(org.bukkit.Material.STONE))
				.name(messageService.deserialize(messageService.getRaw(nameKey)))
				.asGuiItem {
					it.isCancelled = true
					target.world.spawnEntity(target.location, entityType)
				}
			gui.setItem(index, item)
		}

		val backItem = createClickableItem(XMaterial.REDSTONE_BLOCK, messageService.getRaw("spawner_back")) {
			if (viewer == target) {
				PlayerGui(plugin, messageService).open(viewer)
			} else {
				ActionsGui(plugin, messageService).open(viewer, target)
			}
		}
		gui.setItem(53, backItem)

		gui.open(viewer)
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
