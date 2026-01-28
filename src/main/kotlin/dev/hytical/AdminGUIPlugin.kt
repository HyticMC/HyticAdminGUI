package dev.hytical

import dev.hytical.commands.AdminCommand
import dev.hytical.listeners.PlayerDamageListener
import dev.hytical.listeners.PlayerJoinListener
import dev.hytical.listeners.PlayerLoginListener
import dev.hytical.services.EconomyService
import dev.hytical.services.HookService
import dev.hytical.services.MessageService
import dev.hytical.services.PunishmentService
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI

/**
 * AdminGUI Plugin - Easy manage your Minecraft server with Admin GUI
 *
 * Kotlin rewrite with modern architecture using:
 * - Triumph GUI for inventory management
 * - Adventure/MiniMessage for messaging
 * - AdvancedBan for punishments (with Bukkit fallback)
 */
class AdminGUIPlugin : JavaPlugin() {

	lateinit var audiences: BukkitAudiences
		private set

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
		// Initialize Adventure audiences
		audiences = BukkitAudiences.create(this)

		// Initialize services
		hookService = HookService(this)
		hookService.initialize()

		messageService = MessageService(this, hookService, audiences)
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
		// Close Adventure audiences
		if (::audiences.isInitialized) {
			audiences.close()
		}
	}

	private fun printStartupInfo() {
		val message = buildString {
			appendLine()
			appendLine()
			appendLine("§8[]===========[§aEnabling §cAdminGUI§8]===========[]")
			appendLine("§8|")
			appendLine("§8| §cInformation:")
			appendLine("§8|")
			appendLine("§8|   §9Name: §bAdminGUI")
			appendLine("§8|   §9Developer: §bBlack1_TV")
			if (newVersion != null) {
				appendLine("§8|   §9Version: §b${description.version} (FREE) (§6update available§b)")
			} else {
				appendLine("§8|   §9Version: §b${description.version} (FREE)")
			}
			appendLine("§8|   §9Website: §bhttps://rabbit-company.com")
			appendLine("§8|")
			appendLine("§8| §cSponsors:")
			appendLine("§8|")
			appendLine("§8|   §9- §6https://rabbitserverlist.com")
			appendLine("§8|")
			appendLine("§8| §cSupport:")
			appendLine("§8|")
			appendLine("§8|   §9Discord: §bziga.zajc007")
			appendLine("§8|   §9Mail: §bziga.zajc007@gmail.com")
			appendLine("§8|   §9Discord: §bhttps://discord.gg/hUNymXX")
			appendLine("§8|")
			appendLine("§8[]=========================================[]")
		}
		Bukkit.getConsoleSender().sendMessage(message)
	}

	companion object {
		lateinit var instance: AdminGUIPlugin
			private set
	}

	init {
		instance = this
	}
}
