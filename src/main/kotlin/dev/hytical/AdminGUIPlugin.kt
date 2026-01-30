package dev.hytical

import dev.hytical.commands.AdminCommand
import dev.hytical.commands.AdminReloadCommand
import dev.hytical.listeners.PlayerDamageListener
import dev.hytical.listeners.PlayerJoinListener
import dev.hytical.listeners.PlayerLoginListener
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.command.CommandExecutor
import org.bukkit.command.TabCompleter
import org.bukkit.plugin.java.JavaPlugin

class AdminGUIPlugin : JavaPlugin() {

	val serviceContext: ServiceContext by lazy { ServiceContext(this) }

	val adventure get() = serviceContext.adventure
	val hookService get() = serviceContext.hookService
	val messageService get() = serviceContext.messageService
	val punishmentService get() = serviceContext.punishmentService
	val economyService get() = serviceContext.economyService

	var newVersion: String? = null
		private set

	override fun onEnable() {
		serviceContext.hookService
		serviceContext.messageService

		registerAdminCommand()
		registerEvent()
		printStartupInfo()
	}

	override fun onDisable() {
		serviceContext.shutdown()
	}

	private fun registerAdminCommand() {
		registerCommandExecutor("admin") { AdminCommand(serviceContext) }
		registerCommandExecutor("areload") { AdminReloadCommand(this) }
	}

	private inline fun registerCommandExecutor(
		name: String,
		crossinline executorFactory: () -> CommandExecutor
	) {
		getCommand(name)?.let { command ->
			val executor = executorFactory()
			command.setExecutor(executor)
			if (executor is TabCompleter) {
				command.tabCompleter = executor
			}
		}
	}

	private fun registerEvent() {
		val pluginManager = Bukkit.getPluginManager()

		pluginManager.registerEvents(PlayerDamageListener(), this)
		pluginManager.registerEvents(PlayerJoinListener(this), this)
		pluginManager.registerEvents(PlayerLoginListener(this), this)
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
		val component = LegacyComponentSerializer.legacyAmpersand().deserialize(message)
		adventure.console().sendMessage { component }
	}

	companion object {
		lateinit var instance: AdminGUIPlugin
			private set
	}

	init {
		instance = this
	}
}
