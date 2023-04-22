package us.teaminceptus.plasmaenchants.api.events

import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent

/**
 * Utility Class for receiving an event every tick.
 */
class PlayerTickEvent(
    player: Player
) : PlayerEvent(player) {

    companion object {
        private val HANDLERS = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = HANDLERS
    }

    override fun getHandlers(): HandlerList = HANDLERS

}