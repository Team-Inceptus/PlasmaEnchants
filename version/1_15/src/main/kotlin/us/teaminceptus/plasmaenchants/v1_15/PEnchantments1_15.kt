package us.teaminceptus.plasmaenchants.v1_15

import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.event.Event
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import us.teaminceptus.plasmaenchants.api.PTarget
import us.teaminceptus.plasmaenchants.api.PTarget.BOOTS
import us.teaminceptus.plasmaenchants.api.PType
import us.teaminceptus.plasmaenchants.api.PType.Companion.PASSIVE
import us.teaminceptus.plasmaenchants.api.PlasmaConfig
import us.teaminceptus.plasmaenchants.api.enchants.PEnchantment
import us.teaminceptus.plasmaenchants.api.enchants.PEnchantments
import java.util.function.Predicate

@Suppress("unchecked_cast")
enum class PEnchantments1_15(
    override val target: PTarget,
    override val maxLevel: Int = 1,
    private val info: Action<*>,
    private val conflictsP: Predicate<PEnchantment>
) : PEnchantment {

    // Passive Enchantments

    UNSTICK(
        BOOTS, 3, Action(PASSIVE) { event, level ->
            val p = event.player
            if (p.location.block.type == Material.HONEY_BLOCK)
                p.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 3, level, true))
        }, PEnchantments.HERMES
    ),

    ;

    constructor(
        target: PTarget,
        maxLevel: Int,
        info: Action<out Event>,
        conflicts: Collection<PEnchantment> = emptyList()
    ) : this(target, maxLevel, info, { conflicts.contains(it) })

    constructor(
        target: PTarget,
        maxLevel: Int,
        info: Action<out Event>,
        conflicts: PEnchantment
    ) : this(target, maxLevel, info, listOf(conflicts))

    override val type
        get() = info.type

    override val conflicts
        get() = PEnchantments.entries.filter { conflictsP.test(it) }.toList()

    override fun accept(e: Event, level: Int) = info.action(e, level)

    override fun getKey(): NamespacedKey = NamespacedKey(PlasmaConfig.plugin, name.lowercase())

    private class Action<T : Event>(val type: PType<T>, action: (T, Int) -> Unit) {
        val action: (Event, Int) -> Unit

        init {
            this.action = { event, level ->
                if (type.eventClass.isAssignableFrom(event::class.java))
                    action(event as T, level)
            }
        }
    }

}