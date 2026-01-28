package dev.hytical

import dev.hytical.commands.AdminCommand
import dev.hytical.listeners.PlayerDamageListener
import dev.hytical.listeners.PlayerJoinListener
import dev.hytical.listeners.PlayerLoginListener
import dev.hytical.services.EconomyService
import dev.hytical.services.HookService
import dev.hytical.services.MessageService
import dev.hytical.services.PunishmentService
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

/**
 * AdminGUI Plugin - Easy manage your Minecraft server with Admin GUI
 *
 * Kotlin rewrite with modern architecture using:
 * - Triumph GUI for inventory management
 * - Paper's native Adventure/MiniMessage for messaging
 * - AdvancedBan for punishments (with Bukkit fallback)
 */
class AdminGUIPlugin : JavaPlugin() {

	lateinit var hookService: HookService
		private set

	lateinit var messageService: MessageService
		private set

	lateinit var punishmentService: PunishmentService
		private set

	lateinit var economyService: EconomyService
		private set

	var newVersion: String? = null
		private set

	override fun onEnable() {
		// Initialize services
		hookService = HookService(this)
		hookService.initialize()

		messageService = MessageService(this, hookService)
		messageService.loadLanguage()

		punishmentService = PunishmentService(this, hookService)
		economyService = EconomyService(hookService)

		// Register command
		getCommand("admin")?.let { command ->
			val executor = AdminCommand(this)
			command.setExecutor(executor)
			command.tabCompleter = executor
		}

		// Register listeners
		val pluginManager = Bukkit.getPluginManager()
		pluginManager.registerEvents(PlayerDamageListener(), this)
		pluginManager.registerEvents(PlayerJoinListener(this), this)
		pluginManager.registerEvents(PlayerLoginListener(this), this)

		// Print startup message
		printStartupInfo()
	}

	override fun onDisable() {
		// Nothing to clean up - Paper manages Adventure lifecycle
	}

	private fun printStartupInfo() {
		val message = buildString {
			appendLine(" ")
			appendLine(" &dAdminGUIReloaded &7ᴠ${pluginMeta.version}")
			appendLine(" &8--------------------------------------")
			appendLine(" &cInformation")
			appendLine("&7   • &fName: &bAdminGUIReloaded")
			appendLine("&7   • &fAuthor: &bRabbit Company")
			appendLine("&7   • &fMaintainer: &bHyticMC")
			appendLine()
			appendLine(" &cContact")
			appendLine("&7   • &fEmail: &bqhuyy.dev@gmail.com")
			appendLine("&7   • &fDiscord: &b@qh_hytical")
			appendLine()
			appendLine(" &cDependencies")
			appendLine("&7   • &fVault: ${if (hookService.hasVault) "&aEnabled &f( Provider: &b${hookService.economy?.name ?: "&cNull"} &r)" else "&cNot found"}")
			appendLine("&7   • &fSuperVanish: ${if (hookService.hasVanish) "&aEnabled" else "&cNot found"}")
			appendLine("&7   • &fAdvancedBan: ${if (hookService.hasAdvancedBan) "&aEnabled" else "&cNot found"}")
			appendLine("&7   • &fPlaceholderAPI: ${if (hookService.hasPapi) "&aEnabled" else "&cNot found"}")
			appendLine(" &8--------------------------------------")
			appendLine(" ")
		}
		// Use Paper's native Adventure API for console
		val component = LegacyComponentSerializer.legacyAmpersand().deserialize(message)
		Bukkit.getConsoleSender().sendMessage(component)
	}

	companion object {
		lateinit var instance: AdminGUIPlugin
			private set
	}

	init {
		instance = this
	}
}
