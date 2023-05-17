package us.teaminceptus.plasmaenchants.api.enchants

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Illager
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.Event
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDamageEvent.DamageCause
import org.bukkit.inventory.meta.Damageable
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector
import us.teaminceptus.plasmaenchants.api.PlasmaConfig
import us.teaminceptus.plasmaenchants.api.enchants.PEnchantment.Target.*
import us.teaminceptus.plasmaenchants.api.enchants.PEnchantment.Type.Companion.ATTACKING
import us.teaminceptus.plasmaenchants.api.enchants.PEnchantment.Type.Companion.DAMAGE
import us.teaminceptus.plasmaenchants.api.enchants.PEnchantment.Type.Companion.DEFENDING
import us.teaminceptus.plasmaenchants.api.enchants.PEnchantment.Type.Companion.PASSIVE
import us.teaminceptus.plasmaenchants.api.events.PlayerTickEvent

/**
 * Represents all of the Default PlasmaEnchants Enchantments.
 */
enum class PEnchantments(
    private val type: PEnchantment.Type<*>,
    private val target: PEnchantment.Target,
    private val maxLevel: Int = 1,
    action: (Event, Int) -> Unit,
    private vararg val conflicts: PEnchantment
) : PEnchantment {

    // Melee Enchantments

    POISONING(
        ATTACKING, SWORDS, 2, { event, level ->
            require(event is EntityDamageByEntityEvent)

            if (event.entity is LivingEntity)
                    (event.entity as LivingEntity).addPotionEffect(PotionEffect(PotionEffectType.POISON, 20 * 5, level - 1))
        }),

    WITHERING(
        ATTACKING, MELEE_WEAPONS, 1, { event, _ ->
            require(event is EntityDamageByEntityEvent)

            if (event.entity is LivingEntity)
                    (event.entity as LivingEntity).addPotionEffect(PotionEffect(PotionEffectType.WITHER, 20 * 3, 0))
        }, POISONING),

    BANE_OF_ILLAGER(
        ATTACKING, MELEE_WEAPONS, 4, { event, level ->
            require(event is EntityDamageByEntityEvent)

            if (Illager::class.java.isAssignableFrom(event.entity::class.java))
                    event.damage *= 1 + (level * 0.2)
        }),

    THUNDEROUS(
        ATTACKING, SWORDS, 1, { event, _ ->
            require(event is EntityDamageByEntityEvent)

            if (event.entity is LivingEntity)
                event.damager.world.strikeLightning(event.entity.location)
        }),

    ACIDIC(
        ATTACKING, MELEE_WEAPONS, 5, func@{ event, level ->
            require(event is EntityDamageByEntityEvent)

            if (event.entity is LivingEntity) {
                val equipment = (event.entity as LivingEntity).equipment ?: return@func

                equipment.armorContents.forEach {
                    if (it.itemMeta is Damageable) {
                        val meta = it.itemMeta as Damageable
                        meta.damage -= level * 10
                    }
                }
            }
        }),

    VAMPIRISM(
        ATTACKING, SWORDS, 3, { event, level ->
            require(event is EntityDamageByEntityEvent)

            if (event.entity is LivingEntity) {
                val player = event.damager as Player
                val entity = event.entity as LivingEntity

                player.health += level * 2
                entity.health -= level * 2
            }
        }),

    GRAVITY(
        ATTACKING, SWORDS, 6, { event, level ->
            require(event is EntityDamageByEntityEvent)

            event.entity.getNearbyEntities(0.5 * level, 0.5 * level, 0.5 * level).forEach {
                val from = it.location
                val to = event.entity.location

                it.velocity = Vector(from.x - to.x, from.y - to.y, from.z - to.z)
                    .normalize()
                    .multiply(-0.5 * level)
            }
        }),

    // Armor Enchantments

    JUMP(
        PASSIVE, BOOTS, 5, { event, level ->
            require(event is PlayerTickEvent)

            event.player.addPotionEffect(PotionEffect(PotionEffectType.JUMP, 3, level - 1))
        }),

    DEFLECT(
        DEFENDING, CHESTPLATES, 1, { event, _ ->
            require(event is EntityDamageByEntityEvent)

            if (event.entity is Player && event.damager is Projectile) {
                event.isCancelled = true
                event.damager.velocity = event.damager.velocity.multiply(-1)

                event.entity.world.playSound(event.entity.location, Sound.ITEM_SHIELD_BLOCK, 1F, 1F)
            }
        }),

    LIGHTFOOT(
        DAMAGE, BOOTS, 3, { event, level ->
            require(event is EntityDamageEvent)

            if (event.cause == DamageCause.FALL) {
                val block = Location(
                    event.entity.location.world,
                    event.entity.location.x,
                    event.entity.location.y - 1,
                    event.entity.location.z
                ).block

                if (block.type == Material.SOUL_SAND || block.type.name.equals("SOUL_SOIL", true))
                    event.damage /= (1 + level)
            }
        }),

    ;

    private val nameKey = "enchantment.${name.lowercase()}"
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