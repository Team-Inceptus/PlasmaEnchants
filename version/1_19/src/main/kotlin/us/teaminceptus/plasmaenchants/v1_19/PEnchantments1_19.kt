package us.teaminceptus.plasmaenchants.v1_19

import org.bukkit.NamespacedKey
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Projectile
import org.bukkit.event.Event
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import us.teaminceptus.plasmaenchants.api.PTarget
import us.teaminceptus.plasmaenchants.api.PTarget.RANGED
import us.teaminceptus.plasmaenchants.api.PTarget.SWORDS
import us.teaminceptus.plasmaenchants.api.PType
import us.teaminceptus.plasmaenchants.api.PType.Companion.ATTACKING
import us.teaminceptus.plasmaenchants.api.PlasmaConfig
import us.teaminceptus.plasmaenchants.api.enchants.PEnchantment
import us.teaminceptus.plasmaenchants.api.enchants.PEnchantments
import java.security.SecureRandom
import java.util.function.Predicate

private val r = SecureRandom()

@Suppress("unchecked_cast")
enum class PEnchantments1_19(
    override val target: PTarget,
    override val maxLevel: Int = 1,
    private val info: Action<*>,
    private val conflictsP: Predicate<PEnchantment>
) : PEnchantment {

    // Attacking Enchantments

    NIGHTFALL(
        SWORDS, 2, Action(ATTACKING) { event, level ->
            val target = event.entity as? LivingEntity ?: return@Action

            if (r.nextDouble() < 0.3)
                target.addPotionEffect(PotionEffect(PotionEffectType.DARKNESS, 30 * level, level - 1, true))
        }),

    CHAINING(
        RANGED, 3, Action(ATTACKING) { event, level ->
            val proj = event.damager as? Projectile ?: return@Action
            val target = event.entity as? LivingEntity ?: return@Action

            if (target.health - event.finalDamage > 0) return@Action

            if (r.nextDouble() < 0.4)
                for (i in 0 until level) {
                    val loc = target.eyeLocation
                    loc.pitch = 0F
                    loc.yaw = when (i) {
                        1 -> loc.yaw + 45F
                        2 -> loc.yaw - 45F
                        else -> loc.yaw
                    }

                    val newProj = target.world.spawn(loc, proj.javaClass)
                    newProj.velocity = proj.velocity.multiply(1.25).normalize()
                    newProj.shooter = proj.shooter
                    newProj.setBounce(proj.doesBounce())
                }
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
        get() = PEnchantments.values().filter { conflictsP.test(it) }.toList()

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