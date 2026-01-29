package dev.hytical.gui

import org.bukkit.entity.Player
import java.util.concurrent.ConcurrentHashMap

object GuiManager {

	private val targetPlayers = ConcurrentHashMap<Player, Player>()

	private val pageNumbers = ConcurrentHashMap<Player, Int>()

	private val banYears = ConcurrentHashMap<Player, Int>()
	private val banMonths = ConcurrentHashMap<Player, Int>()
	private val banDays = ConcurrentHashMap<Player, Int>()
	private val banHours = ConcurrentHashMap<Player, Int>()
	private val banMinutes = ConcurrentHashMap<Player, Int>()

	private val potionDuration = ConcurrentHashMap<Player, Int>()
	private val potionLevel = ConcurrentHashMap<Player, Int>()

	private val godMode = ConcurrentHashMap<Player, Boolean>()

	@Volatile
	var maintenanceMode: Boolean = false

	fun getTarget(viewer: Player): Player? = targetPlayers[viewer]
	fun setTarget(viewer: Player, target: Player) {
		targetPlayers[viewer] = target
	}

	fun clearTarget(viewer: Player) = targetPlayers.remove(viewer)

	fun getPage(player: Player): Int = pageNumbers.getOrDefault(player, 1)
	fun setPage(player: Player, page: Int) {
		pageNumbers[player] = page
	}

	fun clearPage(player: Player) = pageNumbers.remove(player)

	fun getBanYears(player: Player): Int = banYears.getOrDefault(player, 0)
	fun setBanYears(player: Player, value: Int) {
		banYears[player] = value
	}

	fun getBanMonths(player: Player): Int = banMonths.getOrDefault(player, 0)
	fun setBanMonths(player: Player, value: Int) {
		banMonths[player] = value
	}

	fun getBanDays(player: Player): Int = banDays.getOrDefault(player, 0)
	fun setBanDays(player: Player, value: Int) {
		banDays[player] = value
	}

	fun getBanHours(player: Player): Int = banHours.getOrDefault(player, 0)
	fun setBanHours(player: Player, value: Int) {
		banHours[player] = value
	}

	fun getBanMinutes(player: Player): Int = banMinutes.getOrDefault(player, 0)
	fun setBanMinutes(player: Player, value: Int) {
		banMinutes[player] = value
	}

	fun clearBanConfig(player: Player) {
		banYears.remove(player)
		banMonths.remove(player)
		banDays.remove(player)
		banHours.remove(player)
		banMinutes.remove(player)
	}

	fun getPotionDuration(player: Player): Int = potionDuration.getOrDefault(player, 1)
	fun setPotionDuration(player: Player, value: Int) {
		potionDuration[player] = value
	}

	fun getPotionLevel(player: Player): Int = potionLevel.getOrDefault(player, 1)
	fun setPotionLevel(player: Player, value: Int) {
		potionLevel[player] = value
	}

	fun isGodMode(player: Player): Boolean = godMode.getOrDefault(player, false)
	fun setGodMode(player: Player, enabled: Boolean) {
		godMode[player] = enabled
	}

	fun cleanup(player: Player) {
		targetPlayers.remove(player)
		pageNumbers.remove(player)
		clearBanConfig(player)
		potionDuration.remove(player)
		potionLevel.remove(player)
		godMode.remove(player)
	}
}
