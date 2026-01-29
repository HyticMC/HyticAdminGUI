package dev.hytical.services

import dev.hytical.AdminGUIPlugin
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File

/**
 * Modern messaging service using MiniMessage and Paper's native Adventure.
 * Note: Paper 1.16.5+ has native Adventure support, no need for adventure-platform-bukkit.
 */
class MessageService(
	private val plugin: AdminGUIPlugin,
	private val hookService: HookService,
	private val adventure: BukkitAudiences
) {
	private val miniMessage = MiniMessage.miniMessage()
	private lateinit var lang: YamlConfiguration
	private var langFile: File? = null

	fun loadLanguage() {
		langFile = File(plugin.dataFolder, "language.yml")
		if (!langFile!!.exists()) {
			plugin.saveResource("language.yml", false)
		}
		lang = YamlConfiguration.loadConfiguration(langFile!!)
	}

	fun reloadLanguage() {
		langFile?.let {
			lang = YamlConfiguration.loadConfiguration(it)
		}
	}

	/**
	 * Get raw message string from language file.
	 */
	fun getRaw(key: String): String {
		return lang.getString(key) ?: "<red>Missing: $key"
	}

	/**
	 * Deserialize a MiniMessage string to Component.
	 */
	fun deserialize(text: String, vararg resolvers: TagResolver): Component {
		val combined = TagResolver.resolver(*resolvers)
		return miniMessage.deserialize(text, combined)
	}

	/**
	 * Get a Component from language key with optional resolvers.
	 */
	fun get(key: String, vararg resolvers: TagResolver): Component {
		return deserialize(getRaw(key), *resolvers)
	}

	/**
	 * Send a message to a player using language key.
	 */
	fun send(player: Player, key: String, vararg resolvers: TagResolver) {
		val prefix = getRaw("prefix")
		var message = getRaw(key)

		// Parse PlaceholderAPI if available
		if (hookService.hasPapi) {
			message = hookService.parsePlaceholders(player, message)
		}

		val combined = TagResolver.resolver(*resolvers)
		val component = miniMessage.deserialize(prefix + message, combined)
		// Use Paper's native Adventure API (Player implements Audience)
		adventure.player(player).sendMessage { component }
	}

	/**
	 * Send a raw MiniMessage string to a player.
	 */
	fun sendRaw(player: Player, message: String, vararg resolvers: TagResolver) {
		var text = message
		if (hookService.hasPapi) {
			text = hookService.parsePlaceholders(player, text)
		}
		val combined = TagResolver.resolver(*resolvers)
		val component = miniMessage.deserialize(text, combined)
		adventure.player(player).sendMessage { component }
	}

	/**
	 * Send a message to a CommandSender.
	 */
	fun send(sender: CommandSender, key: String, vararg resolvers: TagResolver) {
		val prefix = getRaw("prefix")
		val message = getRaw(key)
		val combined = TagResolver.resolver(*resolvers)
		val component = miniMessage.deserialize(prefix + message, combined)
		adventure.sender(sender).sendMessage { component }
	}

	/**
	 * Send a component to the console.
	 */
	fun sendConsole(component: Component) {
		adventure.console().sendMessage { component }
	}

	/**
	 * Get inventory title as Component.
	 */
	fun getTitle(key: String, vararg resolvers: TagResolver): Component {
		return deserialize(getRaw(key), *resolvers)
	}

	/**
	 * Create a player placeholder resolver.
	 */
	fun playerPlaceholder(name: String, playerName: String): TagResolver {
		return Placeholder.unparsed(name, playerName)
	}

	/**
	 * Create a parsed placeholder resolver.
	 */
	fun placeholder(name: String, value: String): TagResolver {
		return Placeholder.unparsed(name, value)
	}
}
