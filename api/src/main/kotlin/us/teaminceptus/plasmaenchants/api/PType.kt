package us.teaminceptus.plasmaenchants.api

import org.bukkit.ChatColor
import org.bukkit.event.Event
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDamageEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.player.PlayerInteractEvent
import us.teaminceptus.plasmaenchants.api.artifacts.PArtifact
import us.teaminceptus.plasmaenchants.api.enchants.PEnchantment
import us.teaminceptus.plasmaenchants.api.events.PlayerTickEvent

/**
 * [PEnchantment] and [PArtifact] Types
 * @param T Event Type
 */
class PType<T : Event> private constructor(private val clazz: Class<T>, private val color: ChatColor = ChatColor.GRAY) {
    companion object {
        /**
         * Represents the type of Enchantment that will activate when attacking.
         */
        @JvmStatic
        val ATTACKING: PType<EntityDamageByEntityEvent> = PType(EntityDamageByEntityEvent::class.java)
        /**
         * Represents the type of Enchantment that will activate when defending or taking damage.
         */
        @JvmStatic
        val DEFENDING: PType<EntityDamageByEntityEvent> = PType(EntityDamageByEntityEvent::class.java)
        /**
         * Represents the type of Enchantment that will activate when taking general damage.
         */
        @JvmStatic
        val DAMAGE: PType<EntityDamageEvent> = PType(EntityDamageEvent::class.java)
        /**
         * Represents the type of Enchantment that will activate when breaking blocks.
         */
        @JvmStatic
        val BLOCK_BREAK: PType<BlockBreakEvent> = PType(BlockBreakEvent::class.java)
        /**
         * Represents the type of Enchantment that will activate when mining blocks.
         */
        @JvmStatic
        val MINING: PType<BlockDamageEvent> = PType(BlockDamageEvent::class.java)
        /**
         * Represents the type of Enchantment that runs its action every tick.
         */
        @JvmStatic
        val PASSIVE: PType<PlayerTickEvent> = PType(PlayerTickEvent::class.java, ChatColor.GREEN)
        /**
         * Represents the type of Enchantment that will activate when interacting. Only one enchantment of this type can be applied to an item.
         */
        @JvmStatic
        val INTERACT: PType<PlayerInteractEvent> = PType(PlayerInteractEvent::class.java, ChatColor.GOLD)
        /**
         * Represents the type of Enchantment that will activate when shooting a bow or crossbow-.
         */
        @JvmStatic
        val SHOOT_BOW: PType<EntityShootBowEvent> = PType(EntityShootBowEvent::class.java)
    }

    /**
     * Fetches the class of this Type.
     * @return Type Event Class
     */
    fun getEventClass(): Class<T> = clazz

    /**
     * Fetches the color prefix of this Type.
     * @return Color
     */
    fun getColor(): ChatColor = color

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PType<*>

        return clazz == other.clazz
    }

    override fun hashCode(): Int {
        return clazz.hashCode()
    }

}