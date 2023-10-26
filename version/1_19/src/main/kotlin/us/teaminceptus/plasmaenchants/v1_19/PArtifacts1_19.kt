package us.teaminceptus.plasmaenchants.v1_19

import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Warden
import org.bukkit.event.Event
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import us.teaminceptus.plasmaenchants.api.*
import us.teaminceptus.plasmaenchants.api.PTarget.*
import us.teaminceptus.plasmaenchants.api.PType.Companion.ATTACKING
import us.teaminceptus.plasmaenchants.api.PType.Companion.MINING
import us.teaminceptus.plasmaenchants.api.PType.Companion.PASSIVE
import us.teaminceptus.plasmaenchants.api.artifacts.PArtifact
import us.teaminceptus.plasmaenchants.api.artifacts.PArtifacts

@Suppress("unchecked_cast")
enum class PArtifacts1_19(
    override val target: PTarget,
    private val info: Action<*>,
    override val ringItem: ItemStack,
    override val itemType: Material,
    override val color: ChatColor = ChatColor.YELLOW
) : PArtifact {

    // Melee Artifacts

    SCULK(
        MELEE_WEAPONS, Action(ATTACKING) { event ->
            if (event.entity is Warden)
                event.damage *= 3.0
        }, ItemStack(Material.ECHO_SHARD, 2), Material.ECHO_SHARD, ChatColor.AQUA
    ),

    // Armor Artifacts

    WARDEN(
        HELMETS, Action(PASSIVE) { event ->
            event.player.addPotionEffect(PotionEffect(PotionEffectType.DARKNESS, 3, 0, true))
            event.player.addPotionEffect(PotionEffect(PotionEffectType.INCREASE_DAMAGE, 3, 5, true))
        }, ItemStack(Material.SCULK_CATALYST, 16), Material.SCULK_CATALYST, ChatColor.LIGHT_PURPLE
    ),

    // Tool Artifacts

    MANGROVE(
        AXES, Action(MINING) { event ->
            val m = event.block.type
            if (m.name.contains("MANGROVE") && !m.name.contains("MANGROVE_ROOTS"))
                event.instaBreak = true
        }, ItemStack(Material.MANGROVE_LOG, 32), Material.MANGROVE_PROPAGULE
    ),

    ;
    override val type
        get() = info.type

    override fun getKey(): NamespacedKey = NamespacedKey(PlasmaConfig.plugin, "${name.lowercase()}_artifact")

    override fun toString(): String = asString()

    override fun accept(t: Event) = info.action(t)

    private class Action<T : Event>(val type: PType<T>, action: (T) -> Unit) {
        val action: (Event) -> Unit

        init {
            this.action = { event ->
                if (type.eventClass.isAssignableFrom(event::class.java))
                    action(event as T)
            }
        }
    }

}