package io.wdsj.asw.bukkit.listener

import io.wdsj.asw.bukkit.AdvancedSensitiveWords.settingsManager
import io.wdsj.asw.bukkit.listener.abstraction.AbstractFakeMessageExecutor
import io.wdsj.asw.bukkit.manage.punish.PlayerAltController
import io.wdsj.asw.bukkit.setting.PluginSettings
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import java.util.concurrent.ConcurrentHashMap

class FakeMessageExecutor : Listener, AbstractFakeMessageExecutor() {
    @EventHandler(priority = EventPriority.LOWEST)
    fun onChatFirst(event: AsyncPlayerChatEvent) {
        val player = event.player
        if (settingsManager.getProperty(PluginSettings.CHAT_FAKE_MESSAGE_ON_CANCEL) && shouldFakeMessage(player)) {
            event.isCancelled = true
        }
    }
    @EventHandler(priority = EventPriority.MONITOR)
    fun onChat(event: AsyncPlayerChatEvent) {
        val player = event.player
        if (shouldFakeMessage(player)) {
            selfDecrement(player) // Decrease even the fake message is disabled
            if (settingsManager.getProperty(PluginSettings.CHAT_FAKE_MESSAGE_ON_CANCEL)) {
                event.isCancelled = false
                val players: MutableCollection<Player> = event.recipients
                players.clear()
                if (settingsManager.getProperty(PluginSettings.ENABLE_ALTS_CHECK) && PlayerAltController.hasAlt(player)) {
                    val alts = PlayerAltController.getAlts(player)
                    for (alt in alts) {
                        val altPlayer = Bukkit.getPlayer(alt)
                        altPlayer?.let { players.add(it) }
                    }
                }
                players.add(player)
            }
        }
    }
}
