package us.teaminceptus.plasmaenchants.v1_16

import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.block.Biome
import org.bukkit.entity.Player
import org.bukkit.event.Event
import us.teaminceptus.plasmaenchants.api.PTarget
import us.teaminceptus.plasmaenchants.api.PTarget.ARMOR
import us.teaminceptus.plasmaenchants.api.PType
import us.teaminceptus.plasmaenchants.api.PType.Companion.DEFENDING
import us.teaminceptus.plasmaenchants.api.PlasmaConfig
import us.teaminceptus.plasmaenchants.api.enchants.PEnchantment
import us.teaminceptus.plasmaenchants.api.enchants.PEnchantments
import java.util.function.Predicate

@Suppress("unchecked_cast")
enum class PEnchantments1_16(
    override val target: PTarget,
    override val maxLevel: Int = 1,
    private val info: Action<*>,
    private val conflictsP: Predicate<PEnchantments>
) : PEnchantment {

    ASHY(
        ARMOR, 4, Action(DEFENDING) { event, level ->
            val p = event.entity as? Player ?: return@Action
            if (p.world.environment != World.Environment.NETHER) return@Action

            if (p.location.block.biome == Biome.BASALT_DELTAS)
                event.damage *= 1.0 - (0.08 * level)
            else
                event.damage *= 1.0 - (0.05 * level)
        }),

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