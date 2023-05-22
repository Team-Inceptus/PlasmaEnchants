package us.teaminceptus.plasmaenchants.api.enchants

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.Statistic
import org.bukkit.block.Beacon
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Illager
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.Event
import org.bukkit.event.entity.EntityDamageEvent.DamageCause
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector
import us.teaminceptus.plasmaenchants.api.PlasmaConfig
import us.teaminceptus.plasmaenchants.api.enchants.PEnchantment.Target.*
import us.teaminceptus.plasmaenchants.api.enchants.PEnchantment.Type.Companion.ATTACKING
import us.teaminceptus.plasmaenchants.api.enchants.PEnchantment.Type.Companion.DAMAGE
import us.teaminceptus.plasmaenchants.api.enchants.PEnchantment.Type.Companion.DEFENDING
import us.teaminceptus.plasmaenchants.api.enchants.PEnchantment.Type.Companion.MINING
import us.teaminceptus.plasmaenchants.api.enchants.PEnchantment.Type.Companion.PASSIVE
import us.teaminceptus.plasmaenchants.api.enchants.PEnchantments.Util.isOre
import java.util.*
import kotlin.math.absoluteValue

/**
 * Represents all of the Default PlasmaEnchants Enchantments.
 */
@Suppress("unchecked_cast")
enum class PEnchantments(
    private val target: PEnchantment.Target,
    private val maxLevel: Int = 1,
    private val info: Action<*>,
    private val conflicts: Array<PEnchantment>
) : PEnchantment {

    // Attacking Enchantments

    POISONING(
        SWORDS, 2, Action(ATTACKING) { event, level ->
            if (event.entity is LivingEntity)
                    (event.entity as LivingEntity).addPotionEffect(PotionEffect(PotionEffectType.POISON, 20 * 5, level - 1))
        }),

    WITHERING(
        MELEE_WEAPONS, 1, Action(ATTACKING) { event, _ ->
            if (event.entity is LivingEntity)
                    (event.entity as LivingEntity).addPotionEffect(PotionEffect(PotionEffectType.WITHER, 20 * 3, 0))
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
            val p = event.damager as Player
            if (p.equipment?.itemInMainHand != null) return@Action

            event.damage *= 1 + (level * 0.3)
        }),

    // Attacking Enchantments - Collectors

    PLAYER_COLLECTOR(
        MELEE_WEAPONS, 3, Action(ATTACKING) { event, level ->
            val p = event.damager as Player
            val kills = p.getStatistic(Statistic.PLAYER_KILLS)
            if (kills < 1) return@Action

            val count = kills.toString().length
            event.damage *= 1 + (level * 0.05 * count)
        }, PEnchantments.values().filter { it != PLAYER_COLLECTOR && it.name.endsWith("COLLECTOR") }),



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
                .filter { it.type == org.bukkit.Material.BEACON }
                .map { it as org.bukkit.block.Beacon }
                .filter { it.tier > 0}.forEach {
                    damage *= 1 - (level * 0.1) - (0.025 * (it.tier - 1))
                }

            event.damage = damage
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
        }),

    // Mining Enchantments

    SMELTING(
        PICKAXES, 1, Action(MINING) { event, _ ->
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
        PICKAXES, 1, Action(MINING) { event, _ ->
            if (!event.block.type.isOre()) return@Action

            fun findVein(block: Block): Set<Block> {
                val set = mutableSetOf<Block>()
                listOf(
                    BlockFace.UP,
                    BlockFace.DOWN,
                    BlockFace.NORTH,
                    BlockFace.EAST,
                    BlockFace.SOUTH,
                    BlockFace.WEST,
                ).forEach {
                    val relative = block.getRelative(it)
                    if (relative.type == block.type) {
                        set.add(relative)
                        set.addAll(findVein(relative))
                    }
                }

                return set
            }

            val vein = findVein(event.block)
            vein.forEach { it.breakNaturally(event.player.inventory.itemInMainHand) }
        }),

    // Passive Enchantments

    JUMP(
        BOOTS, 5, Action(PASSIVE) { event, level ->
            event.player.addPotionEffect(PotionEffect(PotionEffectType.JUMP, 3, level - 1))
        }),

    ;

    constructor(
        target: PEnchantment.Target,
        maxLevel: Int,
        info: Action<out Event>,
        conflicts: Collection<PEnchantment> = emptyList()
    ) : this(target, maxLevel, info, conflicts.toTypedArray())

    constructor(
        target: PEnchantment.Target,
        maxLevel: Int,
        info: Action<out Event>,
        conflicts: PEnchantment
    ) : this(target, maxLevel, info, listOf(conflicts))

    private val nameKey = "enchantment.${name.lowercase()}"

    override fun getName(): String = PlasmaConfig.getConfig()?.get(nameKey) ?: name.lowercase().replaceFirstChar { it.uppercase() }

    override fun getDescription(): String = PlasmaConfig.getConfig()?.get("$nameKey.desc") ?: "No description provided."

    override fun getType(): PEnchantment.Type<*> = info.type

    override fun getTarget(): PEnchantment.Target = target

    override fun getConflicts(): List<PEnchantment> = listOf(*conflicts)

    override fun getMaxLevel(): Int = maxLevel

    override fun accept(e: Event, level: Int) = info.action(e, level)

    private class Action<T : Event>(val type: PEnchantment.Type<T>, action: (T, Int) -> Unit) {
        val action: (Event, Int) -> Unit

        init {
            this.action = { event, level ->
                if (type.getEventClass().isAssignableFrom(event::class.java))
                    action(event as T, level)
            }
        }
    }

    private object Util {
        fun Material.isOre(): Boolean {
            return name.endsWith("_ORE") || name.equals("ancient_debris", ignoreCase = true)
        }
    }

}