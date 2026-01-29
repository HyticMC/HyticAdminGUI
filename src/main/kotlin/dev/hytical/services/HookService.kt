package dev.hytical.services

import dev.hytical.AdminGUIPlugin
import me.clip.placeholderapi.PlaceholderAPI
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class HookService(private val plugin: AdminGUIPlugin) {

	var hasVault: Boolean = false
		private set

	var hasPapi: Boolean = false
		private set

	var hasAdvancedBan: Boolean = false
		private set

	var hasVanish: Boolean = false
		private set

	var economy: Economy? = null
		private set

	fun initialize() {
		plugin.logger.info("Using Vault as default Economy API")
		if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
			val rsp = Bukkit.getServicesManager().getRegistration(Economy::class.java)
			if (rsp != null) {
				economy = rsp.provider
				hasVault = true
				plugin.logger.info("Hooked into provider: ${economy?.name}")
			} else {
				plugin.logger.severe("Vault not found, economy features will not work!")
			}
		}

		if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
			hasPapi = true
			plugin.logger.info("PlaceholderAPI hooked successfully.")
		}

		if (Bukkit.getPluginManager().getPlugin("AdvancedBan") != null) {
			hasAdvancedBan = true
			plugin.logger.info("AdvancedBan hooked successfully.")
		}

		if (Bukkit.getPluginManager().getPlugin("SuperVanish") != null ||
			Bukkit.getPluginManager().getPlugin("PremiumVanish") != null
		) {
			hasVanish = true
			plugin.logger.info("Vanish plugin hooked successfully.")
		}
	}

	fun parsePlaceholders(player: Player, text: String): String {
		return if (hasPapi) {
			PlaceholderAPI.setPlaceholders(player, text)
		} else {
			text
		}
	}
}

