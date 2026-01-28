package dev.hytical.services

import net.milkbowl.vault.economy.EconomyResponse
import org.bukkit.entity.Player

/**
 * Economy service wrapper for Vault.
 */
class EconomyService(private val hookService: HookService) {

	/**
	 * Check if economy is available.
	 */
	val isAvailable: Boolean
		get() = hookService.hasVault && hookService.economy != null

	/**
	 * Get player balance.
	 */
	fun getBalance(player: Player): Double {
		return hookService.economy?.getBalance(player) ?: 0.0
	}

	/**
	 * Format amount to currency string.
	 */
	fun format(amount: Double): String {
		return hookService.economy?.format(amount) ?: amount.toString()
	}

	/**
	 * Give money to a player.
	 * Returns true if successful.
	 */
	fun give(player: Player, amount: Double): Boolean {
		val economy = hookService.economy ?: return false
		val response = economy.depositPlayer(player, amount)
		return response.transactionSuccess()
	}

	/**
	 * Take money from a player.
	 * Returns true if successful.
	 */
	fun take(player: Player, amount: Double): Boolean {
		val economy = hookService.economy ?: return false
		if (getBalance(player) < amount) {
			return false
		}
		val response = economy.withdrawPlayer(player, amount)
		return response.transactionSuccess()
	}

	/**
	 * Set player balance.
	 * Returns true if successful.
	 */
	fun set(player: Player, amount: Double): Boolean {
		val economy = hookService.economy ?: return false

		val currentBalance = getBalance(player)
		val difference = amount - currentBalance

		val response: EconomyResponse = if (difference > 0) {
			economy.depositPlayer(player, difference)
		} else if (difference < 0) {
			economy.withdrawPlayer(player, -difference)
		} else {
			return true
		}

		return response.transactionSuccess()
	}
}
