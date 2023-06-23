package us.teaminceptus.plasmaenchants.api.artifacts

import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Biome
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.*
import org.bukkit.event.Event
import org.bukkit.event.entity.EntityDamageEvent.DamageCause
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.RecipeChoice
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import us.teaminceptus.plasmaenchants.api.*
import us.teaminceptus.plasmaenchants.api.PTarget.*
import us.teaminceptus.plasmaenchants.api.PType.Companion.ATTACKING
import us.teaminceptus.plasmaenchants.api.PType.Companion.BLOCK_BREAK
import us.teaminceptus.plasmaenchants.api.PType.Companion.DEFENDING
import us.teaminceptus.plasmaenchants.api.PType.Companion.PASSIVE
import us.teaminceptus.plasmaenchants.api.PType.Companion.SHOOT_BOW
import us.teaminceptus.plasmaenchants.api.enchants.PEnchantment
import us.teaminceptus.plasmaenchants.api.util.NAMESPACED_KEY

/**
 * Represents a PlasmaEnchants Artifact.
 * <p>The difference between Artifacts and Enchantments is that Enchantments can be combined, whereas items can only have one artifact. Artifacts can be removed via a Gridstone just like a [PEnchantment].</p>
 */
@Suppress("unchecked_cast", "deprecation")
enum class PArtifacts(
    override val target: PTarget,
    private val info: Action<*>,
    override val ringItem: ItemStack,
    private val base: ItemStack,
    override val color: ChatColor = ChatColor.YELLOW
) : PArtifact {

    // Melee Artifacts

    DIRT(
        MELEE_WEAPONS, Action(ATTACKING) { event ->
            if (event.damager.location.block.biome.name.contains("PLAINS"))
                event.damage *= 1.05
        }, ItemStack(Material.DIRT, 64), Material.COARSE_DIRT
    ),

    SAND(
        MELEE_WEAPONS, Action(ATTACKING) { event ->
            if (event.damager.location.block.biome == Biome.DESERT)
                event.damage *= 1.1
        }, ItemStack(Material.SAND, 64), Material.SAND
    ),

    RED_SAND(
        MELEE_WEAPONS, Action(ATTACKING) { event ->
            if (event.damager.location.block.biome.name.contains("BADLANDS"))
                event.damage *= 1.1
        }, ItemStack(Material.RED_SAND, 64), Material.RED_SAND
    ),

    WITHER(
        AXES, Action(ATTACKING) { event ->
            val target = event.entity as? LivingEntity ?: return@Action
            target.addPotionEffect(PotionEffect(PotionEffectType.WITHER, 20 * 15, 2, true))

            if (target is Wither || target is WitherSkeleton)
                event.damage *= 3.0
        }, ItemStack(Material.NETHER_STAR, 6), Material.WITHER_ROSE, ChatColor.LIGHT_PURPLE
    ),

    MEAT(
        SWORDS, Action(ATTACKING) { event ->
            val target = event.entity as? LivingEntity ?: return@Action

            when (target.type) {
                EntityType.PIG, EntityType.COW, EntityType.CHICKEN,
                EntityType.SHEEP, EntityType.RABBIT, EntityType.COD, EntityType.SALMON -> event.damage *= 4.0
                else -> return@Action
            }
        }, ItemStack(Material.BEEF, 32), Material.COOKED_BEEF
    ),

    // Armor Artifacts

    LAVA(
        CHESTPLATES, Action(DEFENDING) { event ->
            event.damager.fireTicks += 120
        }, ItemStack(Material.OBSIDIAN, 24), Material.LAVA_BUCKET, ChatColor.LIGHT_PURPLE
    ),

    KELP(
        BOOTS, Action(PASSIVE) { event ->
            event.player.addPotionEffect(PotionEffect(PotionEffectType.DOLPHINS_GRACE, 20, 2, true))
        }, ItemStack(Material.KELP, 64), Material.KELP
    ),

    EXPERIENCE(
        LEGGINGS, Action(ATTACKING) { event ->
            if (event.damager !is Player) return@Action
            val player = event.damager as Player

            event.damage *= 1 + (player.level * 0.03).coerceAtMost(2.5)
        }, ItemStack(Material.EXPERIENCE_BOTTLE, 12), Material.EXPERIENCE_BOTTLE, ChatColor.GOLD
    ),

    PHANTOM(
        HELMETS, Action(DEFENDING) { event ->
            if (event.damager is Phantom || event.cause == DamageCause.FLY_INTO_WALL) event.isCancelled = true
        }, ItemStack(Material.PHANTOM_MEMBRANE, 8), Material.PHANTOM_MEMBRANE, ChatColor.GOLD
    ),

    FEATHER(
        BOOTS, Action(PASSIVE) { event ->
            event.player.addPotionEffect(PotionEffect(PotionEffectType.SLOW_FALLING, 2, 0, true))
        }, ItemStack(Material.FEATHER, 32), Material.FEATHER
    ),

    SEA(
        LEGGINGS, Action(DEFENDING) { event ->
            if (event.cause == DamageCause.DROWNING || event.damager is Guardian) event.isCancelled = true
        }, ItemStack(Material.HEART_OF_THE_SEA), Material.HEART_OF_THE_SEA
    ),

    // Ranged Artifacts

    FLINT(
        CROSSBOW, Action(SHOOT_BOW) { event ->
            event.projectile.velocity.multiply(1.2)
        }, ItemStack(Material.FLINT, 64), Material.FLINT
    ),

    // Tool Artifacts

    IRON(
        PICKAXES, Action(BLOCK_BREAK) { event ->
            if (event.block.type.name.contains("IRON_ORE") && event.isDropItems)
                event.block.getDrops(event.player.inventory.itemInMainHand).forEach { event.player.world.dropItemNaturally(event.block.location, it) }
        }, ItemStack(Material.IRON_INGOT, 16), Material.IRON_INGOT
    ),

    GOLD(
        PICKAXES, Action(BLOCK_BREAK) { event ->
            if (event.block.type.name.contains("GOLD_ORE") && event.isDropItems)
                event.block.getDrops(event.player.inventory.itemInMainHand).forEach { event.player.world.dropItemNaturally(event.block.location, it) }
        }, ItemStack(Material.GOLD_INGOT, 16), Material.GOLD_INGOT, ChatColor.AQUA
    ),

    DIAMOND(
        PICKAXES, Action(BLOCK_BREAK) { event ->
            if (event.block.type.name.contains("DIAMOND_ORE") && event.isDropItems)
                event.block.getDrops(event.player.inventory.itemInMainHand).forEach { event.player.world.dropItemNaturally(event.block.location, it) }
        }, ItemStack(Material.DIAMOND, 16), Material.DIAMOND, ChatColor.AQUA
    ),

    ;

    constructor(
        target: PTarget,
        info: Action<*>,
        ringItem: ItemStack,
        base: Material,
        color: ChatColor = ChatColor.YELLOW
    ) : this (target, info, ringItem, ItemStack(base), color)

    override val displayName
        get() = this.name.split("_").joinToString(" ") { it -> it.lowercase().replaceFirstChar { it.uppercase() } }

    override val description
        get() = PlasmaConfig.config.get("artifact.${displayName.lowercase()}.desc") ?: "No description provided."

    override val type
        get() = info.type

    override val recipe: ShapedRecipe
        get() {
            val recipe = ShapedRecipe(NamespacedKey(PlasmaConfig.plugin, name.lowercase()), item)

            recipe.shape("RRR", "RAR", "RRR")
            recipe.setIngredient('R', RecipeChoice.ExactChoice(ringItem))
            recipe.setIngredient('A', RecipeChoice.ExactChoice(PArtifact.RAW_ARTIFACT))
            recipe.group = "PlasmaEnchants Artifact"

            return recipe
        }

    override val item: ItemStack
        get() = base.clone().apply {
            itemMeta = itemMeta!!.apply {
                setDisplayName("$color${asString()}")

                addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true)
                addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_POTION_EFFECTS)

                persistentDataContainer[artifactsKey, NAMESPACED_KEY] = key
            }
        }

    override fun getKey(): NamespacedKey = NamespacedKey(PlasmaConfig.plugin, "${name.lowercase()}_artifact")

    override val priceMultiplier: Int
        get() {
            return when (color) {
                ChatColor.AQUA -> 5
                ChatColor.GOLD -> 7
                ChatColor.LIGHT_PURPLE -> 10
                else -> 3
            }
        }

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