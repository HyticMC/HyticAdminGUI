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

	fun getRaw(key: String): String {
		return lang.getString(key) ?: "<red>Missing: $key"
	}

	fun deserialize(text: String, vararg resolvers: TagResolver): Component {
		val combined = TagResolver.resolver(*resolvers)
		return miniMessage.deserialize(text, combined)
	}

	fun get(key: String, vararg resolvers: TagResolver): Component {
		return deserialize(getRaw(key), *resolvers)
	}

	fun send(player: Player, key: String, vararg resolvers: TagResolver) {
		val prefix = getRaw("prefix")
		var message = getRaw(key)

		if (hookService.hasPapi) {
			message = hookService.parsePlaceholders(player, message)
		}

		val combined = TagResolver.resolver(*resolvers)
		val component = miniMessage.deserialize(prefix + message, combined)
		adventure.player(player).sendMessage { component }
	}

	fun sendRaw(player: Player, message: String, vararg resolvers: TagResolver) {
		var text = message
		if (hookService.hasPapi) {
			text = hookService.parsePlaceholders(player, text)
		}
		val combined = TagResolver.resolver(*resolvers)
		val component = miniMessage.deserialize(text, combined)
		adventure.player(player).sendMessage { component }
	}

	fun send(sender: CommandSender, key: String, vararg resolvers: TagResolver) {
		val prefix = getRaw("prefix")
		val message = getRaw(key)
		val combined = TagResolver.resolver(*resolvers)
		val component = miniMessage.deserialize(prefix + message, combined)
		adventure.sender(sender).sendMessage { component }
	}

	fun sendConsole(component: Component) {
		adventure.console().sendMessage { component }
	}

	fun getTitle(key: String, vararg resolvers: TagResolver): Component {
		return deserialize(getRaw(key), *resolvers)
	}

	fun playerPlaceholder(name: String, playerName: String): TagResolver {
		return Placeholder.unparsed(name, playerName)
	}

	fun placeholder(name: String, value: String): TagResolver {
		return Placeholder.unparsed(name, value)
	}
}

