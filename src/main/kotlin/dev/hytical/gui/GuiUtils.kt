package dev.hytical.gui

import com.cryptomorin.xseries.XMaterial
import dev.hytical.services.MessageService
import dev.triumphteam.gui.builder.item.ItemBuilder
import dev.triumphteam.gui.guis.Gui
import dev.triumphteam.gui.guis.GuiItem
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object GuiUtils {

    fun XMaterial.toItemStack(): ItemStack = parseItem() ?: ItemStack(Material.STONE)

    fun createItem(
        material: XMaterial,
        name: Component
    ): GuiItem = ItemBuilder.from(material.toItemStack())
        .name(name)
        .asGuiItem { it.isCancelled = true }

    fun createItem(
        material: XMaterial,
        name: String,
        messageService: MessageService
    ): GuiItem = createItem(material, messageService.deserialize(name))

    fun createClickableItem(
        material: XMaterial,
        name: Component,
        onClick: () -> Unit
    ): GuiItem = ItemBuilder.from(material.toItemStack())
        .name(name)
        .asGuiItem {
            it.isCancelled = true
            onClick()
        }

    fun createClickableItem(
        material: XMaterial,
        name: String,
        messageService: MessageService,
        onClick: () -> Unit
    ): GuiItem = createClickableItem(material, messageService.deserialize(name), onClick)

    fun fillBackground(
        gui: Gui,
        slots: Int,
        material: XMaterial = XMaterial.LIGHT_BLUE_STAINED_GLASS_PANE,
        messageService: MessageService
    ) {
        val filler = createItem(material, " ", messageService)
        repeat(slots) { gui.setItem(it, filler) }
    }

    fun createNoPermissionItem(messageService: MessageService): GuiItem =
        createItem(XMaterial.RED_STAINED_GLASS_PANE, messageService.getRaw("permission"), messageService)
}
