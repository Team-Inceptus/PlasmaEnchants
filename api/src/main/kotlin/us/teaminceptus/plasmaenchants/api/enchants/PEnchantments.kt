package us.teaminceptus.plasmaenchants.api.enchants

import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.block.Beacon
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.BlockFace.*
import org.bukkit.block.data.Ageable
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.*
import org.bukkit.event.Event
import org.bukkit.event.entity.EntityDamageEvent.DamageCause
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import us.teaminceptus.plasmaenchants.api.*
import us.teaminceptus.plasmaenchants.api.PTarget.*
import us.teaminceptus.plasmaenchants.api.PType.Companion.ATTACKING
import us.teaminceptus.plasmaenchants.api.PType.Companion.BLOCK_BREAK
import us.teaminceptus.plasmaenchants.api.PType.Companion.DAMAGE
import us.teaminceptus.plasmaenchants.api.PType.Companion.DEFENDING
import us.teaminceptus.plasmaenchants.api.PType.Companion.MINING
import us.teaminceptus.plasmaenchants.api.PType.Companion.PASSIVE
import us.teaminceptus.plasmaenchants.api.PType.Companion.SHOOT_BOW
import us.teaminceptus.plasmaenchants.api.enchants.PEnchantments.Util.getBlockFace
import us.teaminceptus.plasmaenchants.api.enchants.PEnchantments.Util.getConnected
import us.teaminceptus.plasmaenchants.api.enchants.PEnchantments.Util.getSquareRotation
import us.teaminceptus.plasmaenchants.api.enchants.PEnchantments.Util.isOre
import us.teaminceptus.plasmaenchants.api.enchants.PEnchantments.Util.matchType
import java.security.SecureRandom
import java.util.*
import java.util.function.Predicate
import kotlin.math.absoluteValue

private val r = SecureRandom()

/**
 * Represents all of the Default PlasmaEnchants Enchantments.
 */
@Suppress("unchecked_cast", "deprecation")
enum class PEnchantments(
    override val target: PTarget,
    override val maxLevel: Int = 1,
    private val info: Action<*>,
    private val conflictsP: Predicate<PEnchantments>
) : PEnchantment {

    // Attacking Enchantments

    POISONING(
        SWORDS, 2, Action(ATTACKING) { event, level ->
            if (event.entity is LivingEntity)
                    (event.entity as LivingEntity).addPotionEffect(PotionEffect(PotionEffectType.POISON, 20 * 7, level - 1))
        }),

    WITHERING(
        MELEE_WEAPONS, 1, Action(ATTACKING) { event, _ ->
            if (event.entity is LivingEntity)
                    (event.entity as LivingEntity).addPotionEffect(PotionEffect(PotionEffectType.WITHER, 20 * 7, 0))
        }, POISONING),

    BANE_OF_ILLAGER(
        MELEE_WEAPONS, 4, Action(ATTACKING) { event, level ->
            if (Illager::class.java.isAssignableFrom(event.entity::class.java))
                    event.damage *= 1 + (level * 0.2)
        }),

    THUNDEROUS(
        SWORDS, 1, Action(ATTACKING) { event, _ ->
            if (event.entity is LivingEntity)
                event.damager.world.strikeLightning(event.entity.location)
        }),

    ACIDIC(
        MELEE_WEAPONS, 5, Action(ATTACKING) { event, level ->
            if (event.entity is LivingEntity) {
                val equipment = (event.entity as LivingEntity).equipment ?: return@Action

                equipment.armorContents.forEach {
                    if (it.itemMeta is Damageable) {
                        val meta = it.itemMeta as Damageable
                        meta.damage -= level * 10
                    }
                }
            }
        }),

    VAMPIRISM(
        SWORDS, 3, Action(ATTACKING) { event, level ->
            if (event.entity is LivingEntity) {
                val player = event.damager as Player
                val entity = event.entity as LivingEntity

                player.health += level * 2
                entity.health -= level * 2
            }
        }),

    GRAVITY(
        SWORDS, 6, Action(ATTACKING) { event, level ->
            event.entity.getNearbyEntities(0.5 * level, 0.5 * level, 0.5 * level).forEach {
                val from = it.location
                val to = event.entity.location

                it.velocity = Vector(from.x - to.x, from.y - to.y, from.z - to.z)
                    .normalize()
                    .multiply(-0.5 * level)
            }
        }),

    FLARE(
        SWORDS, 3, Action(ATTACKING) { event, level ->
            if (event.entity is LivingEntity) {
                val time = event.damager.world.time
                val dist = (7000 - time).absoluteValue.coerceAtLeast(1)
                if (dist > 6000) return@Action

                val ticks = (120 / dist.floorDiv(1000)) * (1 + (level.floorDiv(2) * 0.5))
                event.entity.fireTicks += ticks.toInt()
            }
        }),

    BACKSTAB(
        MELEE_WEAPONS, 5, Action(ATTACKING) { event, level ->
            if (event.entity is LivingEntity)
                (event.entity as LivingEntity).addPotionEffect(PotionEffect(PotionEffectType.WEAKNESS, 20 * level, level.floorDiv(3)))
        }),

    BEACON_OF_ATTACKING(
        MELEE_WEAPONS, 5, Action(ATTACKING) { event, level ->
            val p = event.damager as Player
            var damage = event.damage

            listOf(
                p.location.chunk,
                p.world.getChunkAt(p.location.add(16.0, 0.0, 0.0)),
                p.world.getChunkAt(p.location.add(0.0, 0.0, 16.0)),
                p.world.getChunkAt(p.location.add(-16.0, 0.0, 0.0)),
                p.world.getChunkAt(p.location.add(0.0, 0.0, -16.0)),
                p.world.getChunkAt(p.location.add(16.0, 0.0, 16.0)),
                p.world.getChunkAt(p.location.add(-16.0, 0.0, 16.0)),
                p.world.getChunkAt(p.location.add(16.0, 0.0, -16.0 )),
                p.world.getChunkAt(p.location.add(-16.0, 0.0, -16.0))
            )
                .flatMap { it.tileEntities.toList() }
                .filter { it.type == Material.BEACON }
                .map { it as Beacon }
                .filter { it.tier > 0}.forEach {
                    damage *= 1.0 + (level * 0.2) + 0.05 * (it.tier - 1)
                }

            event.damage = damage
        }),

    IRON_FIST(
        CHESTPLATES, 10, Action(ATTACKING) { event, level ->
            if (event.damager !is Player) return@Action

            val p = event.damager as Player
            if (p.equipment?.itemInMainHand != null) return@Action

            event.damage *= 1 + (level * 0.3)
        }),

    BEHEADING(
        MELEE_WEAPONS, 1, Action(ATTACKING) { event, _ ->
            if (event.entity !is LivingEntity) return@Action
            val entity = event.entity as LivingEntity

            if (event.finalDamage < entity.health) {
                val head: ItemStack = when (entity.type) {
                    EntityType.SKELETON -> ItemStack(Material.SKELETON_SKULL)
                    EntityType.WITHER_SKELETON -> ItemStack(Material.WITHER_SKELETON_SKULL)
                    EntityType.ZOMBIE -> ItemStack(Material.ZOMBIE_HEAD)
                    EntityType.CREEPER -> ItemStack(Material.CREEPER_HEAD)
                    EntityType.PLAYER -> {
                        val player = event.entity as Player
                        val item = ItemStack(Material.PLAYER_HEAD)
                        val meta = item.itemMeta as SkullMeta
                        meta.owningPlayer = player
                        item.itemMeta = meta

                        item
                    }
                    else -> {
                        val name = "MHF_${entity.type.name.split("_").joinToString("") { c -> c.lowercase().replaceFirstChar { it.uppercase() } }}"

                        val item = ItemStack(Material.PLAYER_HEAD)
                        val meta = item.itemMeta as SkullMeta
                        meta.owner = name
                        item.itemMeta = meta

                        item
                    }
                }

                entity.world.dropItemNaturally(entity.location, head)
            }
        }),

    DEFENDER(
        MELEE_WEAPONS, 4, Action(ATTACKING) { event, level ->
            val damager = event.damager as? LivingEntity ?: return@Action

            if (damager.health == damager.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.value)
                event.damage *= 1 + (level * 0.75)
        }),

    POSEIDON(
        TRIDENT, 5, Action(ATTACKING) { event, level ->
            val p = event.damager as? Player ?: return@Action
            if (p.location.block.type != Material.WATER) return@Action

            event.damage *= 1 + (level * 0.15)
        }),

    RECOVERY(
        MELEE_WEAPONS, 4, Action(ATTACKING) { event, level ->
            val p = event.damager as? Player ?: return@Action
            val target = event.entity as? Player ?: return@Action

            if (target.health - event.finalDamage <= 0)
                p.addPotionEffect(PotionEffect(PotionEffectType.REGENERATION, 60 * level, level / 2, true))
        }),

    STEALTH(
        AXES, 7, Action(ATTACKING) { event, level ->
            val p = event.damager as? Player ?: return@Action
            if (p.isSneaking)
                event.damage *= 1 + (level * 0.1)
        }, WITHERING),

    BLEEDING(
        MELEE_WEAPONS, 4, Action(ATTACKING) { event, level ->
            val target = event.entity as? LivingEntity ?: return@Action

            if (r.nextDouble() < (0.15 * level)) {
                target.world.playSound(target.location, Sound.BLOCK_METAL_BREAK, 1F, 1F)

                var i = 0
                object : BukkitRunnable() {
                    override fun run() {
                        if (i >= 5 + (level * 2)) {
                            cancel()
                            return
                        }

                        target.damage(1.0, event.damager)
                        i++
                    }
                }.runTaskTimer(PlasmaConfig.plugin, 20, 20)
            }
        }),

    VANISH(
        SWORDS, 3, Action(ATTACKING) { event, level ->
            val p = event.damager as? Player ?: return@Action
            val target = event.entity as? LivingEntity ?: return@Action
            if (target.health - event.finalDamage < 0) return@Action

            p.addPotionEffect(PotionEffect(PotionEffectType.INVISIBILITY, 60 * level, 1, true))
            p.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 40 * level, level - 1, true))
        }),

    STREAK(
        AXES, 2, Action(ATTACKING) { event, level ->
            val p = event.damager as? Player ?: return@Action
            val target = event.entity as? LivingEntity ?: return@Action
            if (target.health - event.finalDamage < 0) return@Action

            p.addPotionEffect(PotionEffect(PotionEffectType.INCREASE_DAMAGE, 40 * level, level - 1, true))
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

    // Attacking Enchantments - Collectors

    PLAYER_COLLECTOR(
        AXES, 3, Action(ATTACKING) { event, level ->
            val p = event.damager as Player
            val kills = p.getStatistic(Statistic.PLAYER_KILLS)
            if (kills < 1) return@Action

            val count = kills.toString().length
            event.damage *= 1 + (level * 0.05 * count)
        }, { it != PLAYER_COLLECTOR && it.name.endsWith("COLLECTOR") }),

    UNDEAD_COLLECTOR(
        MELEE_WEAPONS, 3, Action(ATTACKING) { event, level ->
            val p = event.damager as Player
            var kills = 0

            for (type in listOf(
                EntityType.ZOMBIE,
                EntityType.ZOMBIE_VILLAGER,
                EntityType.HUSK,
                EntityType.DROWNED,
                EntityType.PHANTOM,
                EntityType.SKELETON,
                EntityType.STRAY,
                EntityType.WITHER,
                EntityType.WITHER_SKELETON,
                matchType("ZOGLIN"),
                matchType("ZOMBIFIED_PIGLIN"),
            )) if (type != null) kills += p.getStatistic(Statistic.KILL_ENTITY, type)

            val count = kills.toString().length
            event.damage *= 1 + (level * 0.05 * count)
        }, { it != PLAYER_COLLECTOR && it.name.endsWith("COLLECTOR") }),

    AQUATIC_COLLECTOR(
        SWORDS, 3, Action(ATTACKING) { event, level ->
            val p = event.damager as Player
            var kills = 0

            for (type in listOf(
                EntityType.DROWNED,
                EntityType.COD,
                EntityType.SALMON,
                EntityType.PUFFERFISH,
                EntityType.TROPICAL_FISH,
                EntityType.TURTLE,
                EntityType.GUARDIAN,
                EntityType.ELDER_GUARDIAN,
                EntityType.SQUID,
                matchType("GLOW_SQUID"),
                matchType("AXOLOTL"),
                matchType("TADPOLE"),
            )) if (type != null) kills += p.getStatistic(Statistic.KILL_ENTITY, type)

            val count = kills.toString().length
            event.damage *= 1 + (level * 0.05 * count)
        }, { it != PLAYER_COLLECTOR && it.name.endsWith("COLLECTOR") }),

    NETHER_COLLECTOR(
        MELEE_WEAPONS, 3, Action(ATTACKING) { event, level ->
            val p = event.damager as Player
            var kills = 0

            for (type in listOf(
                EntityType.BLAZE,
                EntityType.ENDERMAN,
                EntityType.GHAST,
                EntityType.MAGMA_CUBE,
                EntityType.SKELETON,
                EntityType.WITHER_SKELETON,
                EntityType.WITHER,
                matchType("PIGLIN"),
                matchType("HOGLIN"),
                matchType("ZOMBIFIED_PIGLIN"),
                matchType("ZOGLIN"),
                matchType("PIGLIN_BRUTE"),
                matchType("STRIDER"),
            )) if (type != null) kills += p.getStatistic(Statistic.KILL_ENTITY, type)

            val count = kills.toString().length
            event.damage *= 1 + (level * 0.05 * count)
        }, { it != PLAYER_COLLECTOR && it.name.endsWith("COLLECTOR") }),

    ORE_COLLECTOR(
        AXES, 4, Action(ATTACKING) { event, level ->
            val p = event.damager as Player
            var mined = 0

            for (m in listOf(
                Material.COAL_ORE,
                Material.matchMaterial("COPPER_ORE"),
                Material.IRON_ORE,
                Material.LAPIS_ORE,
                Material.REDSTONE_ORE,
                Material.GOLD_ORE,
                Material.DIAMOND_ORE,
                Material.EMERALD_ORE,

                Material.matchMaterial("DEEPSLATE_COAL_ORE"),
                Material.matchMaterial("DEEPSLATE_COPPER_ORE"),
                Material.matchMaterial("DEEPSLATE_LAPIS_ORE"),
                Material.matchMaterial("DEEPSLATE_REDSTONE_ORE"),
                Material.matchMaterial("DEEPSLATE_IRON_ORE"),
                Material.matchMaterial("DEEPSLATE_GOLD_ORE"),
                Material.matchMaterial("DEEPSLATE_DIAMOND_ORE"),
                Material.matchMaterial("DEEPSLATE_EMERALD_ORE"),
                
                Material.NETHER_QUARTZ_ORE,
                Material.matchMaterial("NETHER_GOLD_ORE")
            )) if (m != null) mined += p.getStatistic(Statistic.MINE_BLOCK, m)

            val count = mined.toString().length
            event.damage *= 1 + (level * 0.05 * count)
        }, { it != PLAYER_COLLECTOR && it.name.endsWith("COLLECTOR") }),

    END_COLLECTOR(
        MELEE_WEAPONS, 4, Action(ATTACKING) { event, level ->
            val p = event.damager as Player
            var kills = 0

            for (type in listOf(
                EntityType.ENDERMAN,
                EntityType.ENDER_DRAGON,
                EntityType.ENDERMITE
            )) kills += p.getStatistic(Statistic.KILL_ENTITY, type)

            val count = kills.toString().length
            event.damage *= 1 + (level * 0.07 * count)
        }, { it != PLAYER_COLLECTOR && it.name.endsWith("COLLECTOR") }),

    BOSS_COLLECTOR(
        MELEE_WEAPONS, 5, Action(ATTACKING) { event, level ->
            val p = event.damager as Player
            var kills = 0

            for (type in listOf(
                EntityType.ENDER_DRAGON,
                EntityType.WITHER,
                matchType("WARDEN")
            )) if (type != null) kills += p.getStatistic(Statistic.KILL_ENTITY, type)

            val count = kills.toString().length
            event.damage *= 1 + (level * 0.1 * count)
        }, { it != PLAYER_COLLECTOR && it.name.endsWith("COLLECTOR") }),

    // Defending Enchantments

    DEFLECT(
        CHESTPLATES, 1, Action(DEFENDING) { event, _ ->
            if (event.damager is Projectile) {
                event.isCancelled = true
                event.damager.velocity = event.damager.velocity.multiply(-1)
                event.damager.teleport(event.damager.location.apply {
                    direction = direction.multiply(-1)
                })

                event.entity.world.playSound(event.entity.location, Sound.ITEM_SHIELD_BLOCK, 1F, 1F)
            }
        }),

    HARDENING(
        CHESTPLATES, 3, Action(DEFENDING) { event, level ->
            if (event.entity is LivingEntity) {
                if ((event.entity as LivingEntity).equipment == null) return@Action
                for (item in (event.entity as LivingEntity).equipment!!.armorContents)
                    if (item.itemMeta is Damageable) {
                        val meta = item.itemMeta as Damageable
                        item.itemMeta = meta.apply {
                            damage -= level * 2
                        } as ItemMeta
                    }
            }

        }),

    BEACON_OF_DEFENDING(
        CHESTPLATES, 5, Action(DEFENDING) { event, level ->
            val p = event.entity as Player
            var damage = event.damage

            listOf(
                p.location.chunk,
                p.world.getChunkAt(p.location.add(16.0, 0.0, 0.0)),
                p.world.getChunkAt(p.location.add(0.0, 0.0, 16.0)),
                p.world.getChunkAt(p.location.add(-16.0, 0.0, 0.0)),
                p.world.getChunkAt(p.location.add(0.0, 0.0, -16.0)),
                p.world.getChunkAt(p.location.add(16.0, 0.0, 16.0)),
                p.world.getChunkAt(p.location.add(-16.0, 0.0, 16.0)),
                p.world.getChunkAt(p.location.add(16.0, 0.0, -16.0)),
                p.world.getChunkAt(p.location.add(-16.0, 0.0, -16.0))
            )
                .flatMap { it.tileEntities.toList() }
                .filter { it.type == Material.BEACON }
                .map { it as Beacon }
                .filter { it.tier > 0}.forEach {
                    damage *= 1 - (level * 0.1) - (0.025 * (it.tier - 1))
                }

            event.damage = damage
        }),

    REINFORCEMENT(
        ARMOR, 4, Action(DEFENDING) { event, level ->
            val entity = event.damager as? LivingEntity ?: return@Action

            if (r.nextDouble() < 0.25) {
                entity.addPotionEffect(PotionEffect(PotionEffectType.WEAKNESS, 20 * 3, level - 1, true))
                entity.world.playSound(entity.location, Sound.ENCHANT_THORNS_HIT, 1F, 1F)
            }
        }),

    TANK(
        ARMOR, 3, Action(DEFENDING) { event, level ->
            val entity = event.damager as? LivingEntity ?: return@Action
            val item = entity.equipment?.itemInMainHand ?: return@Action

            if (item.type.name.contains("AXE"))
                event.damage *= 1.0 - (level * 0.03)
        }),

    // Damage Enchantments

    LIGHTFOOT(
        BOOTS, 3, Action(DAMAGE) { event, level ->
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

    STRIDING(
        LEGGINGS, 1, Action(DAMAGE) { event, _ ->
            if (event.cause == DamageCause.LAVA) event.isCancelled = true
        }),

    DISINTEGRATION(
        LEGGINGS, 9, Action(DAMAGE) { event, level ->
            if (event.cause == DamageCause.SUFFOCATION || event.cause == DamageCause.FALLING_BLOCK)
                event.damage *= 1 - (level * 0.1)
        }),

    LANDING(
        BOOTS, 9,  Action(DAMAGE) { event, level ->
            if (event.cause != DamageCause.FALL) return@Action
            event.isCancelled = true

            val dmg = event.damage * (1 - (level * 0.1))

            event.entity.getNearbyEntities(level.toDouble(), 1.0, level.toDouble()).forEach {
                if (it is LivingEntity)
                    it.damage(dmg, event.entity)
            }
        }, LIGHTFOOT),

    MOON_PROTECTION(
        ARMOR, 4, Action(DAMAGE) { event, level ->
            if (event.cause == DamageCause.STARVATION || event.cause == DamageCause.VOID) return@Action
            if (event.entity.world.time !in 13000..24000) return@Action

            event.damage *= (1 - (level * 0.05)).coerceAtLeast(0.1)
        }),

    SUN_PROTECTION(
        ARMOR, 4, Action(DAMAGE) { event, level ->
            if (event.cause == DamageCause.STARVATION || event.cause == DamageCause.VOID) return@Action
            if (event.entity.world.time !in 0..13000) return@Action

            event.damage *= (1 - (level * 0.05)).coerceAtLeast(0.1)
        }, MOON_PROTECTION),

    STORM_PROTECTION(
        ARMOR, 4, Action(DAMAGE) { event, level ->
            if (event.cause == DamageCause.STARVATION || event.cause == DamageCause.VOID) return@Action
            if (!event.entity.world.hasStorm()) return@Action

            event.damage *= (1 - (level * 0.05)).coerceAtLeast(0.1)
        }, SUN_PROTECTION),

    SIPHONING(
        HELMETS, 4, Action(DAMAGE) { event, level ->
            if (event.cause != DamageCause.LIGHTNING) return@Action
            event.isCancelled = true

            if (event.entity is Player)
                (event.entity as Player).health += level * 5
        }),

    SQUISHY(
        CHESTPLATES, 4, Action(DAMAGE) { event, level ->
            if (event.cause != DamageCause.THORNS || event.cause != DamageCause.CONTACT) return@Action

            if (level == 4) event.isCancelled = true
            else event.damage *= 1 - (level * 0.25)
        }),

    SNOW_WALKER(
        BOOTS, 1, Action(DAMAGE) { event, _ ->
            if (event.cause == DamageCause.HOT_FLOOR)
                event.isCancelled = true
        }),

    // Mining Enchantments

    TELEPATHY(
        TOOLS, 1, Action(BLOCK_BREAK) { event, _ ->
            event.isDropItems = false

            val drops = event.block.getDrops(event.player.inventory.itemInMainHand)
            event.player.inventory.addItem(*drops.toTypedArray()).values.forEach {
                event.block.world.dropItemNaturally(event.block.location, it)
            }
        }),

    BLAST(
        PICKAXES, 2, Action(BLOCK_BREAK) { event, level ->
            val face = getBlockFace(event.player) ?: return@Action

            getSquareRotation(event.block.location, face, level + 1)
                .filter { !it.type.isAir && it.type != Material.OBSIDIAN && it.type != Material.BEDROCK }
                .forEach {
                    it.breakNaturally(event.player.inventory.itemInMainHand)
                }
        }),

    SMELTING(
        PICKAXES, 1, Action(BLOCK_BREAK) { event, _ ->
            if (event.player.inventory.itemInMainHand.containsEnchantment(Enchantment.SILK_TOUCH)) return@Action

            event.isDropItems = false
            val drops = event.block.getDrops(event.player.inventory.itemInMainHand).onEach {
                when (it.type.name.lowercase()) {
                    "coal_ore" -> it.type = Material.COAL
                    "copper_ore", "deepslate_copper_ore", "raw_copper" -> it.type = Material.matchMaterial("copper_ingot")!!
                    "redstone_ore", "deepslate_redstone_ore" -> it.type = Material.REDSTONE
                    "iron_ore", "deepslate_iron_ore", "raw_iron" -> it.type = Material.IRON_INGOT
                    "lapis_ore", "deepslate_lapis_ore" -> it.type = Material.LAPIS_LAZULI
                    "gold_ore", "deepslate_gold_ore", "raw_gold" -> it.type = Material.GOLD_INGOT
                    "diamond_ore", "deepslate_diamond_ore" -> it.type = Material.DIAMOND
                    "emerald_ore", "deepslate_emerald_ore" -> it.type = Material.EMERALD
                    "ancient_debris" -> it.type = Material.matchMaterial("netherite_scrap")!!
                }
            }

            drops.forEach { event.player.world.dropItemNaturally(event.block.location, it) }
        }),

    VEIN_RIPPER(
        PICKAXES, 1, Action(BLOCK_BREAK) { event, _ ->
            if (!event.block.type.isOre()) return@Action
            getConnected(event.block).forEach { it.breakNaturally(event.player.inventory.itemInMainHand) }
        }),

    BEDROCK_MINER(
        PICKAXES, 1, Action(MINING) { event, _ ->
            if (event.block.type.name.contains("OBSIDIAN"))
                event.instaBreak = true
        }),

    HARVEST(
        HOES, 3, Action(BLOCK_BREAK) { event, level ->
            if (!event.isDropItems) return@Action
            if (event.block.type.createBlockData() !is Ageable) return@Action

            val drops = event.block.getDrops(event.player.inventory.itemInMainHand)

            for (i in 1..level)
                drops.forEach { event.block.world.dropItemNaturally(event.block.location, it) }
        }),

    REPLENISH(
        HOES, 1, Action(BLOCK_BREAK) { event, _ ->
            if (event.block.type.createBlockData() !is Ageable) return@Action
            val type = event.block.type

            object : BukkitRunnable() {
                override fun run() {
                    event.block.type = type
                }
            }.runTaskLater(PlasmaConfig.plugin, 1)
        }),

    LUMBERJACK(
        AXES, 1, Action(BLOCK_BREAK) { event, _ ->
            if (!Tag.LOGS.isTagged(event.block.type)) return@Action

            var i = 0
            getConnected(event.block) { it.type == event.block.type }.forEach {
                if (i >= 100) return@forEach
                if (event.player.gameMode != GameMode.CREATIVE)
                    event.player.inventory.itemInMainHand.apply {
                        val damageable = itemMeta as? Damageable ?: return@forEach

                        if (itemMeta?.hasEnchant(Enchantment.DURABILITY) == true) {
                            if (r.nextInt((itemMeta?.getEnchantLevel(Enchantment.DURABILITY) ?: 0) + 1) == 1)
                                damageable.damage += 1
                        }
                        else
                            damageable.damage += 1
                        itemMeta = damageable as ItemMeta

                        if (damageable.damage >= this.type.maxDurability) return@Action
                    }

                it.breakNaturally(event.player.inventory.itemInMainHand)
                i++
            }
        }),

    // Passive Enchantments

    BRISK(
        MELEE_WEAPONS, 3, Action(PASSIVE) { event, level ->
            event.player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 3, level - 1, true))
        }),

    CLOVER(
        HELMETS, 6, Action(PASSIVE) { event, level ->
            event.player.addPotionEffect(PotionEffect(PotionEffectType.LUCK, 3, level - 1, true))
        }),

    BUOYANT(
        LEGGINGS, 2, Action(PASSIVE) { event, level ->
            if (event.player.location.block.isLiquid)
                event.player.velocity.add(Vector(0.0, 0.15 * level, 0.0))
        }),

    JUMP(
        BOOTS, 5, Action(PASSIVE) { event, level ->
            event.player.addPotionEffect(PotionEffect(PotionEffectType.JUMP, 3, level - 1))
        }),

    HERMES(
        BOOTS, 2, Action(PASSIVE) { event, level ->
            event.player.addPotionEffect(PotionEffect(PotionEffectType.SLOW_FALLING, 3, level - 1))
        }, JUMP),

    HASTE(
        TOOLS, 4, Action(PASSIVE) { event, level ->
            event.player.addPotionEffect(PotionEffect(PotionEffectType.FAST_DIGGING, 3, level - 1))
        }),

    HEALER(
        HELMETS, 2, Action(PASSIVE) { event, level ->
            event.player.addPotionEffect(PotionEffect(PotionEffectType.REGENERATION, 3, level - 1))
        }),

    FLASHLIGHT(
        HELMETS, 1, Action(PASSIVE) { event, _ ->
            event.player.addPotionEffect(PotionEffect(PotionEffectType.NIGHT_VISION, 3, 0))
        }),

    DENSITY(
        CHESTPLATES, 2, Action(PASSIVE) { event, level ->
            if (event.player.health < (event.player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value?.div(2) ?: 10.0))
                event.player.addPotionEffect(PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 3, level - 1))
        }),

    FRIENDSHIP(
        HELMETS, 3, Action(PASSIVE) { event, level ->
            val amplifier = event.player.world.getNearbyEntities(event.player.location, level * 5.0, level * 5.0, level * 5.0)
                    .filterIsInstance<Tameable>()
                    .count { it.owner?.uniqueId == event.player.uniqueId }.coerceAtMost(6)

            event.player.addPotionEffect(PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 3, amplifier, true))
        }),

    // Ranged Enchantments

    SNIPING(
        BOW, 3, Action(SHOOT_BOW) { event, level -> event.projectile.velocity = event.projectile.velocity.multiply(1 + (level * 0.1)) }),

    MITOSIS(
        BOW, 2, Action(SHOOT_BOW) { event, level ->
            if (event.projectile !is Projectile) return@Action
            val projectile = event.projectile as Projectile

            var count = 0

            object : BukkitRunnable() {
                override fun run() {
                    if (projectile.isDead || projectile.isOnGround || count >= (level * 10)) {
                        cancel()
                        return
                    }

                    val duplicated = projectile.world.spawn(projectile.location, projectile.javaClass)
                    duplicated.shooter = projectile.shooter
                    duplicated.velocity = projectile.velocity
                    count++
                }
            }.runTaskTimer(PlasmaConfig.plugin, 100 / level.toLong(), 100 / level.toLong())
        }
    ),

    PRESSURIZED(
        RANGED, 6, Action(SHOOT_BOW) { event, level ->
            val mod = (event.entity.location.y / (event.entity.world.maxHeight * 2)) * level

            event.projectile.velocity = event.projectile.velocity.multiply((1 + mod).coerceAtMost(5.5))
        }),

    EXPLOSIVE_SHOT(
        BOW, 3, Action(ATTACKING) { event, level ->
            if (event.entity !is LivingEntity) return@Action
            if (event.damager !is Projectile) return@Action

            event.entity.world.createExplosion(event.entity.location, level.toFloat(), false, true, (event.damager as Projectile).shooter as? Entity)
        }),

    WEIGHT(
        RANGED, 4, Action(ATTACKING) { event, level ->
            if (event.entity !is LivingEntity) return@Action
            if (event.damager !is Projectile) return@Action

            val entity = event.entity as LivingEntity

            val vel = event.damager.velocity
            vel.multiply(level * 1.05 / ((entity.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE)?.value ?: 0.0) + 1))

            entity.velocity = vel
        }),

    HOMING(
        RANGED, 3, Action(SHOOT_BOW) { event, level ->
            val proj = event.projectile as? Projectile ?: return@Action
            val shooter = proj.shooter as? Player ?: return@Action
            object : BukkitRunnable() {
                override fun run() {
                    if (proj.isOnGround || proj.isDead) {
                        cancel()
                        return
                    }

                    proj.getNearbyEntities(level * 5.0, level * 5.0, level * 5.0).forEach {
                        val target = it as? LivingEntity ?: return@forEach
                        if (target == shooter) return@forEach

                        val dist = level * 2
                        if (proj.location.distanceSquared(target.location) < dist * dist)
                            proj.velocity = target.location.toVector().subtract(proj.location.toVector()).normalize().multiply(level)
                    }
                }
            }.runTaskTimer(PlasmaConfig.plugin, 0, 1)
        }),

    // Misc Enchantments

    GROWTH(
        ALL, 2, Action(ATTACKING) { event, level ->
            val p = event.damager as? Player ?: return@Action
            val tool = p.inventory.itemInMainHand
            val meta = tool.itemMeta ?: return@Action

            if (meta !is Damageable) return@Action

            if (r.nextDouble() < 0.2)
                meta.damage += 1 + level

            tool.itemMeta = meta
        })

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
        get() = entries.filter { it != this && conflictsP.test(it) }.toList()

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

    private object Util {
        fun Material.isOre(): Boolean {
            return name.endsWith("_ORE") || name.equals("ancient_debris", ignoreCase = true)
        }

        fun matchType(name: String): EntityType? {
            for (type in EntityType.entries)
                if (type.name.equals(name, ignoreCase = true)) return type

            return null
        }

        @JvmStatic
        private val connectedFaces = listOf(UP, DOWN, NORTH, EAST, SOUTH, WEST)

        fun getConnected(block: Block, predicate: (Block) -> Boolean = { block.type == it.type}): Set<Block> {
            val results = hashSetOf<Block>()
            val queue = LinkedList<Block>()
            queue.add(block)

            var current = queue.poll()
            while (current != null) {
                for (face in connectedFaces) {
                    val relative = current.getRelative(face)
                    if (predicate(relative) && results.add(relative))
                        queue.add(relative)
                }
                current = queue.poll()
            }
            return results
        }

        @JvmStatic
        fun getSquare(location: Location, radius: Int): List<Block> {
            val blocks: MutableList<Block> = ArrayList()
            for (x in location.blockX - radius..location.blockX + radius)
                for (z in location.blockZ - radius..location.blockZ + radius)
                    blocks.add(location.world!!.getBlockAt(x, location.blockY, z))

            return blocks
        }

        @JvmStatic
        fun getSquareRotation(loc: Location, face: BlockFace, radius: Int): List<Block> {
            val blocks = getSquare(loc, radius)
            if (face == UP || face == DOWN) return blocks

            val rotated = mutableListOf<Block>()
            blocks.forEach {
                val center = loc.clone()
                val v = it.location.toVector().subtract(loc.toVector())

                if (face == NORTH || face == SOUTH) v.rotateAroundX(Math.toRadians(90.0))
                else v.rotateAroundZ(Math.toRadians(90.0))

                rotated.add(center.add(v).block)
            }

            return rotated
        }

        @JvmStatic
        fun getBlockFace(player: Player): BlockFace? {
            val targets = player.getLastTwoTargetBlocks(null, 100)
            if (targets.size != 2 || !targets[1].type.isOccluding) return null

            return targets[1].getFace(targets[0])
        }

    }

}