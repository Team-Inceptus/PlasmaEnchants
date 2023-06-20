package us.teaminceptus.plasmaenchants.api.artifacts

import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Biome
import org.bukkit.entity.*
import org.bukkit.event.Event
import org.bukkit.event.entity.EntityDamageEvent.DamageCause
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import us.teaminceptus.plasmaenchants.api.PTarget
import us.teaminceptus.plasmaenchants.api.PTarget.*
import us.teaminceptus.plasmaenchants.api.PType
import us.teaminceptus.plasmaenchants.api.PType.Companion.ATTACKING
import us.teaminceptus.plasmaenchants.api.PType.Companion.BLOCK_BREAK
import us.teaminceptus.plasmaenchants.api.PType.Companion.DEFENDING
import us.teaminceptus.plasmaenchants.api.PType.Companion.PASSIVE
import us.teaminceptus.plasmaenchants.api.PType.Companion.SHOOT_BOW
import us.teaminceptus.plasmaenchants.api.PlasmaConfig
import us.teaminceptus.plasmaenchants.api.enchants.PEnchantment

/**
 * Represents a PlasmaEnchants Artifact.
 * <p>The difference between Artifacts and Enchantments is that Enchantments can be combined, whereas items can only have one artifact. Artifacts can be removed via a Gridstone just like a [PEnchantment].</p>
 */
@Suppress("unchecked_cast")
enum class PArtifacts(
    override val target: PTarget,
    private val info: Action<*>,
    override val ringItem: ItemStack,
    override val color: ChatColor = ChatColor.YELLOW
) : PArtifact {

    // Melee Artifacts

    DIRT(
        MELEE_WEAPONS, Action(ATTACKING) { event ->
            if (event.damager.location.block.biome.name.contains("PLAINS"))
                event.damage *= 1.05
        }, ItemStack(Material.DIRT, 64)
    ),

    SAND(
        MELEE_WEAPONS, Action(ATTACKING) { event ->
            if (event.damager.location.block.biome == Biome.DESERT)
                event.damage *= 1.1
        }, ItemStack(Material.SAND, 64)
    ),

    RED_SAND(
        MELEE_WEAPONS, Action(ATTACKING) { event ->
            if (event.damager.location.block.biome.name.contains("BADLANDS"))
                event.damage *= 1.1
        }, ItemStack(Material.RED_SAND, 64)
    ),

    WITHER(
        AXES, Action(ATTACKING) { event ->
            val target = event.entity as? LivingEntity ?: return@Action
            target.addPotionEffect(PotionEffect(PotionEffectType.WITHER, 20 * 15, 2, true))

            if (target is Wither || target is WitherSkeleton)
                event.damage *= 3.0
        }, ItemStack(Material.NETHER_STAR, 6)
    ),

    MEAT(
        SWORDS, Action(ATTACKING) { event ->
            val target = event.entity as? LivingEntity ?: return@Action

            when (target.type) {
                EntityType.PIG, EntityType.COW, EntityType.CHICKEN,
                EntityType.SHEEP, EntityType.RABBIT, EntityType.COD, EntityType.SALMON -> event.damage *= 4.0
                else -> return@Action
            }
        }, ItemStack(Material.BEEF, 32)
    ),

    // Armor Artifacts

    LAVA(
        CHESTPLATES, Action(DEFENDING) { event ->
            event.damager.fireTicks += 120
        }, ItemStack(Material.OBSIDIAN, 24)
    ),

    KELP(
        BOOTS, Action(PASSIVE) { event ->
            event.player.addPotionEffect(PotionEffect(PotionEffectType.DOLPHINS_GRACE, 20, 2, true))
        }, ItemStack(Material.KELP, 64)
    ),

    EXPERIENCE(
        LEGGINGS, Action(ATTACKING) { event ->
            if (event.damager !is Player) return@Action
            val player = event.damager as Player

            event.damage *= 1 + (player.level * 0.03).coerceAtMost(2.5)
        }, ItemStack(Material.EXPERIENCE_BOTTLE, 12)
    ),

    PHANTOM(
        HELMETS, Action(DEFENDING) { event ->
            if (event.damager is Phantom || event.cause == DamageCause.FLY_INTO_WALL) event.isCancelled = true
        }, ItemStack(Material.PHANTOM_MEMBRANE, 8)
    ),

    FEATHER(
        BOOTS, Action(PASSIVE) { event ->
            event.player.addPotionEffect(PotionEffect(PotionEffectType.SLOW_FALLING, 2, 0, true))
        }, ItemStack(Material.FEATHER, 32)
    ),

    SEA(
        LEGGINGS, Action(DEFENDING) { event ->
            if (event.cause == DamageCause.DROWNING || event.damager is Guardian) event.isCancelled = true
        }, ItemStack(Material.HEART_OF_THE_SEA)
    ),

    // Ranged Artifacts

    FLINT(
        CROSSBOW, Action(SHOOT_BOW) { event ->
            event.projectile.velocity.multiply(1.2)
        }, ItemStack(Material.FLINT, 64)
    ),

    // Tool Artifacts

    IRON(
        PICKAXES, Action(BLOCK_BREAK) { event ->
            if (event.block.type.name.contains("IRON_ORE") && event.isDropItems)
                event.block.getDrops(event.player.inventory.itemInMainHand).forEach { event.player.world.dropItemNaturally(event.block.location, it) }
        }, ItemStack(Material.IRON_INGOT, 16)
    ),

    GOLD(
        PICKAXES, Action(BLOCK_BREAK) { event ->
            if (event.block.type.name.contains("GOLD_ORE") && event.isDropItems)
                event.block.getDrops(event.player.inventory.itemInMainHand).forEach { event.player.world.dropItemNaturally(event.block.location, it) }
        }, ItemStack(Material.GOLD_INGOT, 16)
    ),

    DIAMOND(
        PICKAXES, Action(BLOCK_BREAK) { event ->
            if (event.block.type.name.contains("DIAMOND_ORE") && event.isDropItems)
                event.block.getDrops(event.player.inventory.itemInMainHand).forEach { event.player.world.dropItemNaturally(event.block.location, it) }
        }, ItemStack(Material.DIAMOND, 16)
    ),

    ;

    override val displayName
        get() = name.lowercase().replaceFirstChar { it.uppercase() }

    override val description
        get() = PlasmaConfig.config.get("artifact.${displayName.lowercase()}.desc") ?: "No description provided."

    override val type
        get() = info.type

    override fun getKey(): NamespacedKey = NamespacedKey(PlasmaConfig.plugin, displayName.lowercase())

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