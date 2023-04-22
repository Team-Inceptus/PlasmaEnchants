package us.teaminceptus.plasmaenchants.api.enchants

import org.bukkit.entity.Illager
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import us.teaminceptus.plasmaenchants.api.PlasmaConfig
import us.teaminceptus.plasmaenchants.api.enchants.PEnchantment.Target.*
import us.teaminceptus.plasmaenchants.api.enchants.PEnchantment.Type.Companion.ATTACKING
import us.teaminceptus.plasmaenchants.api.enchants.PEnchantment.Type.Companion.PASSIVE
import us.teaminceptus.plasmaenchants.api.events.PlayerTickEvent
import java.util.function.BiConsumer

/**
 * Represents all of the Default PlasmaEnchants Enchantments.
 */
enum class PEnchantments(
    private val nameKey: String,
    private val type: PEnchantment.Type<*>,
    private val target: PEnchantment.Target,
    private val maxLevel: Int = 1,
    action: (Event, Int) -> Unit,
    private vararg val conflicts: PEnchantment
) : PEnchantment {

    // Melee Enchantments

    POISONING("enchantment.poisoning",
        ATTACKING, SWORDS, 2, { event, level ->
            require(event is EntityDamageByEntityEvent)

            if (event.entity is LivingEntity)
                    (event.entity as LivingEntity).addPotionEffect(PotionEffect(PotionEffectType.POISON, 20 * 5, level - 1))
        }),

    WITHERING("enchantment.withering",
        ATTACKING, MELEE_WEAPONS, 1, { event, _ ->
            require(event is EntityDamageByEntityEvent)

            if (event.entity is LivingEntity)
                    (event.entity as LivingEntity).addPotionEffect(PotionEffect(PotionEffectType.WITHER, 20 * 3, 0))
        }, POISONING),

    BANE_OF_ILLAGER("enchantment.bane_of_illager",
        ATTACKING, MELEE_WEAPONS, 4, { event, level ->
            require(event is EntityDamageByEntityEvent)

            if (Illager::class.java.isAssignableFrom(event.entity::class.java))
                    event.damage *= 1 + (level * 0.2)
        }),

    // Armor Enchantments

    JUMP("enchantment.jump",
        PASSIVE, BOOTS, 5, { event, level ->
            require(event is PlayerTickEvent)

            event.player.addPotionEffect(PotionEffect(PotionEffectType.JUMP, 3, level - 1))
        }),

    ;

    private val action: (Event, Int) -> Unit

    init {
        this.action = { event, level -> if (type.getEventClass().isAssignableFrom(event.javaClass)) action(event, level) }
    }

    override fun getName(): String = PlasmaConfig.getConfig()?.get(nameKey) ?: name.lowercase().replaceFirstChar { it.uppercase() }

    override fun getDescription(): String = PlasmaConfig.getConfig()?.get("$nameKey.desc") ?: "No description provided."

    override fun getType(): PEnchantment.Type<*> = type

    override fun getTarget(): PEnchantment.Target = target

    override fun getConflicts(): List<PEnchantment> = listOf(*conflicts)

}