package us.teaminceptus.plasmaenchants.v1_17

import org.bukkit.NamespacedKey
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.entity.EntityDamageEvent.DamageCause
import us.teaminceptus.plasmaenchants.api.PTarget
import us.teaminceptus.plasmaenchants.api.PTarget.MELEE_WEAPONS
import us.teaminceptus.plasmaenchants.api.PType
import us.teaminceptus.plasmaenchants.api.PType.Companion.ATTACKING
import us.teaminceptus.plasmaenchants.api.PlasmaConfig
import us.teaminceptus.plasmaenchants.api.enchants.PEnchantment
import us.teaminceptus.plasmaenchants.api.enchants.PEnchantments
import java.util.function.Predicate

@Suppress("unchecked_cast")
enum class PEnchantments1_17(
    override val target: PTarget,
    override val maxLevel: Int = 1,
    private val info: Action<*>,
    private val conflictsP: Predicate<PEnchantment>
) : PEnchantment {

    // Attacking Enchantments

    FROST(
        MELEE_WEAPONS, 3, Action(ATTACKING) { event, level ->
            val target = event.entity as? LivingEntity ?: return@Action
            target.freezeTicks += 40 * level
        }, PEnchantments.FLARE
    ),

    SILICON(
        MELEE_WEAPONS, 4, Action(ATTACKING) { event, level ->
            val p = event.damager as? Player ?: return@Action
            if (p.lastDamageCause == null) return@Action
            if (p.lastDamageCause!!.cause != DamageCause.LIGHTNING) return@Action

            event.damage *= 1.0 + (level * 0.35)
        }, PEnchantments.SIPHONING
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