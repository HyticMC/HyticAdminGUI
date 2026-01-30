package dev.hytical

import dev.hytical.gui.*
import dev.hytical.services.EconomyService
import dev.hytical.services.HookService
import dev.hytical.services.MessageService
import dev.hytical.services.PunishmentService
import net.kyori.adventure.platform.bukkit.BukkitAudiences

class ServiceContext(val plugin: AdminGUIPlugin) {

    private val _adventure = lazy { BukkitAudiences.builder(plugin).build() }
    val adventure: BukkitAudiences by _adventure

    val hookService: HookService by lazy {
        HookService(plugin).apply { initialize() }
    }

    val messageService: MessageService by lazy {
        MessageService(plugin, hookService, adventure).apply { loadLanguage() }
    }

    val economyService: EconomyService by lazy {
        EconomyService(hookService)
    }

    val punishmentService: PunishmentService by lazy {
        PunishmentService(plugin, hookService)
    }

    fun createMainGui() = MainGui(this)
    fun createPlayerGui() = PlayerGui(this)
    fun createPlayerSettingsGui() = PlayerSettingsGui(this)
    fun createPlayersListGui() = PlayersListGui(this)
    fun createActionsGui() = ActionsGui(this)
    fun createWorldGui() = WorldGui(this)
    fun createMoneyGui() = MoneyGui(this)
    fun createMoneyAmountGui(action: MoneyAction) = MoneyAmountGui(this, action)
    fun createKickGui() = KickGui(this)
    fun createBanGui() = BanGui(this)
    fun createPotionsGui() = PotionsGui(this)
    fun createSpawnerGui() = SpawnerGui(this)
    fun createInventoryViewerGui() = InventoryViewerGui(this)

    fun shutdown() {
        if (_adventure.isInitialized()) {
            adventure.close()
        }
    }
}
