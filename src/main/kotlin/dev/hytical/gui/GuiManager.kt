package dev.hytical.gui

import org.bukkit.entity.Player
import java.util.concurrent.ConcurrentHashMap

/**
 * Centralized GUI state management.
 */
object GuiManager {

	// Target player tracking per viewer
	private val targetPlayers = ConcurrentHashMap<Player, Player>()

	// Pagination state
	private val pageNumbers = ConcurrentHashMap<Player, Int>()

	// Ban configuration
	private val banYears = ConcurrentHashMap<Player, Int>()
	private val banMonths = ConcurrentHashMap<Player, Int>()
	private val banDays = ConcurrentHashMap<Player, Int>()
	private val banHours = ConcurrentHashMap<Player, Int>()
	private val banMinutes = ConcurrentHashMap<Player, Int>()

	// Potion configuration
	private val potionDuration = ConcurrentHashMap<Player, Int>()
	private val potionLevel = ConcurrentHashMap<Player, Int>()

	// God mode tracking (for 1.8 compatibility)
	private val godMode = ConcurrentHashMap<Player, Boolean>()

	// Maintenance mode
	@Volatile
	var maintenanceMode: Boolean = false

	// Target player methods
	fun getTarget(viewer: Player): Player? = targetPlayers[viewer]
	fun setTarget(viewer: Player, target: Player) {
		targetPlayers[viewer] = target
	}

	fun clearTarget(viewer: Player) = targetPlayers.remove(viewer)

	// Page methods
	fun getPage(player: Player): Int = pageNumbers.getOrDefault(player, 1)
	fun setPage(player: Player, page: Int) {
		pageNumbers[player] = page
	}

	fun clearPage(player: Player) = pageNumbers.remove(player)

	// Ban configuration methods
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

	// Potion configuration methods
	fun getPotionDuration(player: Player): Int = potionDuration.getOrDefault(player, 1)
	fun setPotionDuration(player: Player, value: Int) {
		potionDuration[player] = value
	}

	fun getPotionLevel(player: Player): Int = potionLevel.getOrDefault(player, 1)
	fun setPotionLevel(player: Player, value: Int) {
		potionLevel[player] = value
	}

	// God mode methods
	fun isGodMode(player: Player): Boolean = godMode.getOrDefault(player, false)
	fun setGodMode(player: Player, enabled: Boolean) {
		godMode[player] = enabled
	}

	/**
	 * Clean up all state for a player (call on quit).
	 */
	fun cleanup(player: Player) {
		targetPlayers.remove(player)
		pageNumbers.remove(player)
		clearBanConfig(player)
		potionDuration.remove(player)
		potionLevel.remove(player)
		godMode.remove(player)
	}
}
