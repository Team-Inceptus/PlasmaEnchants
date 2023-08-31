package us.teaminceptus.plasmaenchants.api.artifacts

import org.bukkit.*
import org.bukkit.block.Biome
import org.bukkit.block.data.Ageable
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.*
import org.bukkit.event.Event
import org.bukkit.event.block.Action.*
import org.bukkit.event.entity.EntityDamageByEntityEvent
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
import us.teaminceptus.plasmaenchants.api.PType.Companion.DAMAGE
import us.teaminceptus.plasmaenchants.api.PType.Companion.DEFENDING
import us.teaminceptus.plasmaenchants.api.PType.Companion.INTERACT
import us.teaminceptus.plasmaenchants.api.PType.Companion.MINING
import us.teaminceptus.plasmaenchants.api.PType.Companion.PASSIVE
import us.teaminceptus.plasmaenchants.api.PType.Companion.SHOOT_BOW
import us.teaminceptus.plasmaenchants.api.enchants.PEnchantment
import us.teaminceptus.plasmaenchants.api.util.NAMESPACED_KEY
import java.security.SecureRandom

private val r = SecureRandom()

/**
 * Represents a PlasmaEnchants Artifact. Artifacts available in later versions are not available here.
 * <p>The difference between Artifacts and Enchantments is that Enchantments can be combined, whereas items can only have one artifact. Artifacts can be removed via a Gridstone just like a [PEnchantment].</p>
 */
@Suppress("unchecked_cast")
enum class PArtifacts(
    override val target: PTarget,
    private val info: Action<*>,
    override val ringItem: ItemStack,
    override val itemType: Material,
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

    STORM(
        MELEE_WEAPONS, Action(ATTACKING) { event ->
            val target = event.entity as? LivingEntity ?: return@Action

            target.world.strikeLightning(target.location)
            event.damage += r.nextInt(-10, 10) + 15.0
        }, ItemStack(Material.CREEPER_HEAD), Material.NETHER_STAR, ChatColor.LIGHT_PURPLE
    ),

    STEEL(
        AXES, Action(PASSIVE) { event ->
            event.player.addPotionEffect(PotionEffect(PotionEffectType.INCREASE_DAMAGE, 3, 3, true))
            event.player.addPotionEffect(PotionEffect(PotionEffectType.SLOW, 3, 0, true))
        }, ItemStack(Material.IRON_BLOCK, 48), Material.ANVIL, ChatColor.GOLD
    ),

    SHULKER(
        SWORDS, Action(INTERACT) { event ->
            val bullet = event.player.world.spawn(event.player.eyeLocation, ShulkerBullet::class.java).apply {
                shooter = event.player
            }

            bullet.target = event.player.getNearbyEntities(10.0, 10.0, 10.0).minByOrNull { it.location.distanceSquared(bullet.location) } as? LivingEntity
        }, ItemStack(Material.SHULKER_SHELL, 48), Material.SHULKER_SHELL, ChatColor.GOLD
    ),

    END(
        MELEE_WEAPONS, Action(ATTACKING) { event ->
            val p = event.damager as? Player ?: return@Action

            if (event.entity is Enderman || event.entity is Endermite || event.entity is EnderDragon) {
                event.damage *= 5.0
                return@Action
            }

            if (p.world.environment == World.Environment.THE_END)
                event.damage *= 2.0
        }, ItemStack(Material.CHORUS_FRUIT, 64), Material.END_STONE, ChatColor.LIGHT_PURPLE
    ),

    // Armor Artifacts

    LAVA(
        CHESTPLATES, Action(DEFENDING) { event ->
            event.damager.fireTicks += 120
        }, ItemStack(Material.OBSIDIAN, 60), Material.LAVA_BUCKET, ChatColor.LIGHT_PURPLE
    ),

    KELP(
        BOOTS, Action(PASSIVE) { event ->
            event.player.addPotionEffect(PotionEffect(PotionEffectType.DOLPHINS_GRACE, 3, 2, true))
            event.player.addPotionEffect(PotionEffect(PotionEffectType.WATER_BREATHING, 3, 0, true))
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
        LEGGINGS, Action(DAMAGE) { event ->
            if (event.cause == DamageCause.DROWNING || (event is EntityDamageByEntityEvent && (event.damager is Guardian || event.damager is Dolphin))) event.isCancelled = true
        }, ItemStack(Material.HEART_OF_THE_SEA, 2), Material.HEART_OF_THE_SEA
    ),

    ANVIL(
        CHESTPLATES, Action(DEFENDING) { event ->
            if (event.cause != DamageCause.ENTITY_ATTACK && event.cause != DamageCause.ENTITY_SWEEP_ATTACK) return@Action

            if (r.nextDouble() < 0.1)
                event.isCancelled = true
        }, ItemStack(Material.ANVIL, 48), Material.ANVIL, ChatColor.LIGHT_PURPLE
    ),

    SQUID(
        HELMETS, Action(DEFENDING) { event ->
            val entity = event.damager as? LivingEntity ?: return@Action

            if (r.nextDouble() < 0.25)
                entity.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 20 * 4, 1, true))
        }, ItemStack(Material.INK_SAC, 48), Material.INK_SAC
    ),

    ZOMBIE(
         ARMOR, Action(DEFENDING) { event ->
            val entity = event.damager as? LivingEntity ?: return@Action

            if (r.nextDouble() < 0.25)
                entity.addPotionEffect(PotionEffect(PotionEffectType.WEAKNESS, 20 * r.nextInt(1, 3), r.nextInt(0, 1), true))
        }, ItemStack(Material.ROTTEN_FLESH, 64), Material.ZOMBIE_HEAD, ChatColor.AQUA
    ),

    BONE(
        CHESTPLATES, Action(DEFENDING) { event ->
            val entity = event.damager as? Projectile ?: return@Action
            if (r.nextDouble() < 0.25) {
                event.isCancelled = true
                entity.velocity = entity.velocity.multiply(-0.75)
            }
        }, ItemStack(Material.BONE, 64), Material.BONE_MEAL, ChatColor.GOLD
    ),

    PRISMARINE(
        CHESTPLATES, Action(PASSIVE) { event ->
            val p = event.player
            val start = p.location
            val step = 0.1

            p.getNearbyEntities(2.5, 2.5, 2.5)
                .filterIsInstance<Monster>()
                .minByOrNull { it.location.distanceSquared(p.location) }
                .apply {
                    if (this == null) return@Action

                    val line = this.location.subtract(p.location.toVector()).toVector()
                    var d = 0.0
                    while (d < line.length()) {
                        line.multiply(d)
                        start.add(line)
                        start.world!!.spawnParticle(Particle.WATER_BUBBLE, start, 2, 0.0, 0.0, 0.0, 0.0)
                        start.subtract(line)
                        line.normalize()
                        d += step
                    }

                    if (r.nextDouble() < 0.01)
                        this.damage(1.0, p)

                }

        }, ItemStack(Material.PRISMARINE_SHARD, 64), Material.PRISMARINE_SHARD
    ),

    WART(
        ARMOR, Action(DEFENDING) { event ->
            val p = event.entity as? Player ?: return@Action
            if (p.world.environment != World.Environment.NETHER) return@Action

            event.damage /= 2
        }, ItemStack(Material.NETHER_WART_BLOCK, 48), Material.NETHER_WART, ChatColor.GOLD
    ),

    POISON(
        ARMOR, Action(PASSIVE) { event ->
            event.player.getNearbyEntities(2.5, 2.5, 2.5)
                .minByOrNull { it.location.distanceSquared(event.player.location) }
                .apply {
                    if (this !is LivingEntity) return@Action

                    addPotionEffect(PotionEffect(PotionEffectType.POISON, 3, 0, true))
                }
        }, ItemStack(Material.SPIDER_EYE, 64), Material.POISONOUS_POTATO, ChatColor.AQUA
    ),

    DEATH(
        ARMOR, Action(PASSIVE) { event ->
            event.player.getNearbyEntities(3.5, 3.5, 3.5)
                .minByOrNull { it.location.distanceSquared(event.player.location) }
                .apply {
                    if (this !is LivingEntity) return@Action

                    addPotionEffect(PotionEffect(PotionEffectType.WITHER, 3, 1, true))
                }
        }, ItemStack(Material.WITHER_ROSE, 48), Material.WITHER_SKELETON_SKULL, ChatColor.LIGHT_PURPLE
    ),

    // Ranged Artifacts

    FLINT(
        RANGED, Action(SHOOT_BOW) { event ->
            event.projectile.velocity.multiply(1.35)
        }, ItemStack(Material.FLINT, 64), Material.FLINT
    ),

    FIREWORK(
        BOW, Action(ATTACKING) { event ->
            event.entity.world.createExplosion(event.entity.location, 3F, true, false, event.damager)
        }, ItemStack(Material.FIREWORK_STAR, 64), Material.FIREWORK_STAR
    ),

    BLAZE(
        BOW, Action(INTERACT) { event ->
            if (event.action != LEFT_CLICK_AIR && event.action != LEFT_CLICK_BLOCK) return@Action

            val loc = event.player.eyeLocation
            event.player.world.spawn(loc, SmallFireball::class.java).apply {
                shooter = event.player
            }
        }, ItemStack(Material.BLAZE_ROD, 64), Material.BLAZE_POWDER, ChatColor.GOLD
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

    APPLE(
        SHEARS, Action(BLOCK_BREAK) { event ->
            val type = event.block.type
            val drop: Material = when {
                type.name.endsWith("_LEAVES") -> Material.APPLE
                type == Material.GRASS || type == Material.TALL_GRASS || type == Material.FERN || type == Material.FERN -> Material.WHEAT_SEEDS
                else -> return@Action
            }

            event.block.world.dropItemNaturally(event.block.location, ItemStack(drop))
        }, ItemStack(Material.APPLE, 32), Material.APPLE
    ),

    TNT(
        PICKAXES, Action(BLOCK_BREAK) { event ->
            if (r.nextDouble() < 0.25)
                event.block.world.createExplosion(event.block.location, 2F, false, true, event.player)
        }, ItemStack(Material.TNT, 64), Material.TNT, ChatColor.GOLD
    ),

    FARMING(
        HOES, Action(MINING) { event ->
            val material = event.block.type
            if (event.block.blockData is Ageable || material == Material.HAY_BLOCK || material == Material.PUMPKIN || material == Material.MELON)
                event.instaBreak = true
        }, ItemStack(Material.HAY_BLOCK, 64), Material.HAY_BLOCK, ChatColor.AQUA
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