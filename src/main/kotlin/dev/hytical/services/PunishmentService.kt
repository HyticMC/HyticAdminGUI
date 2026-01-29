package dev.hytical.services

import dev.hytical.AdminGUIPlugin
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

class PunishmentService(
	private val plugin: AdminGUIPlugin,
	private val hookService: HookService
) {

	fun ban(playerName: String, reason: String, until: Date?): Boolean {
		if (hookService.hasAdvancedBan) {
			return banWithAdvancedBan(playerName, reason, until)
		}
		return banWithBukkit(playerName, reason, until)
	}

	private fun banWithAdvancedBan(playerName: String, reason: String, until: Date?): Boolean {
		return try {
			val punishmentManager = Class.forName("me.leoko.advancedban.manager.PunishmentManager")
			val getInstance = punishmentManager.getMethod("get")
			val instance = getInstance.invoke(null)

			val durationMs = if (until != null) {
				until.time - System.currentTimeMillis()
			} else {
				-1L
			}

			val durationStr = if (durationMs > 0) {
				formatDuration(durationMs)
			} else {
				""
			}

			Bukkit.dispatchCommand(
				Bukkit.getConsoleSender(),
				if (durationStr.isNotEmpty()) {
					"tempban $playerName $durationStr $reason"
				} else {
					"ban $playerName $reason"
				}
			)
			true
		} catch (e: Exception) {
			plugin.logger.warning("AdvancedBan integration failed, using Bukkit fallback: ${e.message}")
			banWithBukkit(playerName, reason, until)
		}
	}

	private fun banWithBukkit(playerName: String, reason: String, until: Date?): Boolean {
		return try {
			val command = if (until == null) {
				"ban $playerName $reason"
			} else {
				"ban $playerName $reason"
			}
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command)
			true
		} catch (e: Exception) {
			plugin.logger.severe("Failed to ban player: ${e.message}")
			false
		}
	}

	fun kick(player: Player, reason: String) {
		player.kickPlayer(reason)
	}

	private fun formatDuration(ms: Long): String {
		val seconds = ms / 1000
		val minutes = seconds / 60
		val hours = minutes / 60
		val days = hours / 24
		val years = days / 365

		val sb = StringBuilder()
		if (years > 0) sb.append("${years}y")
		if (days % 365 > 0) sb.append("${days % 365}d")
		if (hours % 24 > 0) sb.append("${hours % 24}h")
		if (minutes % 60 > 0) sb.append("${minutes % 60}m")

		return if (sb.isEmpty()) "1m" else sb.toString()
	}

	fun calculateBanDate(
		years: Int = 0,
		months: Int = 0,
		days: Int = 0,
		hours: Int = 0,
		minutes: Int = 0
	): Date {
		val calendar = Calendar.getInstance()
		calendar.add(Calendar.YEAR, years)
		calendar.add(Calendar.MONTH, months)
		calendar.add(Calendar.DAY_OF_MONTH, days)
		calendar.add(Calendar.HOUR_OF_DAY, hours)
		calendar.add(Calendar.MINUTE, minutes)
		return calendar.time
	}

	fun formatBanReason(reasonKey: String, until: Date, messageService: MessageService): String {
		val reason = messageService.getRaw("ban") + messageService.getRaw(reasonKey)

		val calendar = Calendar.getInstance()
		calendar.time = until

		val dateStr = messageService.getRaw("ban_time")
			.replace("{days}", String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH)))
			.replace("{months}", String.format("%02d", calendar.get(Calendar.MONTH) + 1))
			.replace("{years}", calendar.get(Calendar.YEAR).toString())
			.replace("{hours}", String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)))
			.replace("{minutes}", String.format("%02d", calendar.get(Calendar.MINUTE)))
			.replace("{seconds}", String.format("%02d", calendar.get(Calendar.SECOND)))

		return reason + "\n" + dateStr
	}
}

