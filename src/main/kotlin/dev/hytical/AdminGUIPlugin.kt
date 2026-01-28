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
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

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
			appendLine(" ")
			appendLine(" &dᴀᴅᴍɪɴɢᴜɪʀᴇʟᴏᴀᴅᴇᴅ &7ᴠ${pluginMeta.version}")
			appendLine(" &8--------------------------------------")
			appendLine(" &cɪɴꜰᴏʀᴍᴀᴛɪᴏɴ")
			appendLine("&7   • &fɴᴀᴍᴇ: &bᴀᴅᴍɪɴɢᴜɪʀᴇʟᴏᴀᴅᴇᴅ")
			appendLine("&7   • &fᴀᴜᴛʜᴏʀ: &bRabbit Company")
			appendLine("&7   • &fᴍᴀɪɴᴛᴀɪɴᴇʀ: &bʜʏᴛɪᴄᴀʟᴍᴄ")
			appendLine()
			appendLine(" &cᴄᴏɴᴛᴀᴄᴛ")
			appendLine("&7   • &fᴇᴍᴀɪʟ: &bǫʜᴜʏʏ.ᴅᴇᴠ@ɢᴍᴀɪʟ.ᴄᴏᴍ")
			appendLine("&7   • &fᴅɪѕᴄᴏʀᴅ: &b@qh_hytical")
			appendLine()
			appendLine(" &cᴅᴇᴘᴇɴᴅᴇɴᴄɪᴇѕ")
			appendLine("&7   • &fᴠᴀᴜʟᴛ: ${if (hookService.hasVault) "&aᴇɴᴀʙʟᴇᴅ" else "&cɴᴏᴛ ꜰᴏᴜɴᴅ"}")
			appendLine("&7   - &fᴇᴄᴏɴᴏᴍʏ ᴘʀᴏᴠɪᴅᴇʀ: &a${hookService.economy?.name ?: "&cɴᴜʟʟ"}")
			appendLine("&7   • &fѕᴜᴘᴇʀᴠᴀɴɪѕʜ: ${if (hookService.hasVanish) "&aᴇɴᴀʙʟᴇᴅ" else "&cɴᴏᴛ ꜰᴏᴜɴᴅ"}")
			appendLine("&7   • &fᴀᴅᴠᴀɴᴄᴇᴅʙᴀɴ: ${if (hookService.hasAdvancedBan) "&aᴇɴᴀʙʟᴇᴅ" else "&cɴᴏᴛ ꜰᴏᴜɴᴅ"}")
			appendLine("&7   • &fᴘʟᴀᴄᴇʜᴏʟᴅᴇʀᴀᴘɪ: ${if (hookService.hasPapi) "&aᴇɴᴀʙʟᴇᴅ" else "&cɴᴏᴛ ꜰᴏᴜɴᴅ"}")
			appendLine(" &8--------------------------------------")
			appendLine(" ")
		}
		audiences.console().sendMessage {
			LegacyComponentSerializer.legacyAmpersand().deserialize(message)
		}
	}

	companion object {
		lateinit var instance: AdminGUIPlugin
			private set
	}

	init {
		instance = this
	}
}
