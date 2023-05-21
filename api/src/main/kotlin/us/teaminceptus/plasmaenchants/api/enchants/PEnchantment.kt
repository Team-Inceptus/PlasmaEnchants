package us.teaminceptus.plasmaenchants.api.enchants

import org.bukkit.Material
import org.bukkit.event.Event
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.player.PlayerInteractEvent
import us.teaminceptus.plasmaenchants.api.events.PlayerTickEvent
import java.util.function.BiConsumer
import java.util.stream.Collectors

/**
 * Represents a PlasmaEnchants Enchantment.
 */
interface PEnchantment : BiConsumer<Event, Int> {

    /**
     * Fetches the name of this PEnchantment.
     * @return Name of Enchantment
     */
    fun getName(): String

    /**
     * Fetches the description of this PEnchantment.
     * @return Description of Enchantment
     */
    fun getDescription(): String

    /**
     * Fetches the maximum level of this PEnchantment.
     * @return Maximum Level
     */
    fun getMaxLevel(): Int = 1

    /**
     * Fetches the Target Type of this PEnchantment.
     * @return Target Type
     */
    fun getTarget(): Target

    /**
     * Fetches the Type of this PEnchantment.
     * @return Type
     */
    fun getType(): Type<*>

    /**
     * Fetches the list of conflicts for this PEnchantment.
     * @return List of Conflicts
     */
    fun getConflicts(): List<PEnchantment>

    /**
     * Whether or not this PEnchantment conflicts with another PEnchantment.
     * @param enchantment PEnchantment to check for conflicts with.
     * @return true if conflicts with enchantment, false otherwise
     */
    fun conflictsWith(enchantment: PEnchantment?): Boolean {
        if (enchantment == null) return false
        return getConflicts().contains(enchantment) || enchantment.getConflicts().contains(this)
    }

    /**
     * PEnchantment Types
     * @param T Event Type
     */
    class Type<T : Event> private constructor(private val clazz: Class<T>) {
        companion object {
            /**
             * Represents the type of Enchantment that will activate when attacking.
             */
            val ATTACKING: Type<EntityDamageByEntityEvent> = Type(EntityDamageByEntityEvent::class.java)
            /**
             * Represents the type of Enchantment that will activate when defending or taking damage.
             */
            val DEFENDING: Type<EntityDamageByEntityEvent> = Type(EntityDamageByEntityEvent::class.java)
            /**
             * Represents the type of Enchantment that will activate when taking general damage.
             */
            val DAMAGE: Type<EntityDamageEvent> = Type(EntityDamageEvent::class.java)
            /**
             * Represents the type of Enchantment that will activate when mining blocks.
             */
            val MINING: Type<BlockBreakEvent> = Type(BlockBreakEvent::class.java)
            /**
             * Represents the type of Enchantment that runs its action every tick.
             */
            val PASSIVE: Type<PlayerTickEvent> = Type(PlayerTickEvent::class.java)
            /**
             * Represents the type of Enchantment that will activate when interacting. Only one enchantment of this type can be applied to an item.
             */
            val INTERACT: Type<PlayerInteractEvent> = Type(PlayerInteractEvent::class.java)
            /**
             * Represents the type of Enchantment that will activate when shooting a bow or crossbow-.
             */
            val SHOOT_BOW: Type<EntityShootBowEvent> = Type(EntityShootBowEvent::class.java)
        }

        fun getEventClass(): Class<T> = clazz
    }

    /**
     * PEnchantment Material Application Targets
     */
    enum class Target {

        /**
         * Represents all Swords
         */
        SWORDS(
            Material.WOODEN_SWORD,
            Material.STONE_SWORD,
            Material.IRON_SWORD,
            Material.GOLDEN_SWORD,
            Material.DIAMOND_SWORD,
            Material.matchMaterial("NETHERITE_SWORD") ?: Material.AIR
        ),

        /**
         * Represents all Axes
         */
        AXES(
            Material.WOODEN_AXE,
            Material.STONE_AXE,
            Material.IRON_AXE,
            Material.GOLDEN_AXE,
            Material.DIAMOND_AXE,
            Material.matchMaterial("NETHERITE_AXE") ?: Material.AIR
        ),

        /**
         * Represents all Pickaxes
         */
        PICKAXES(
            Material.WOODEN_PICKAXE,
            Material.STONE_PICKAXE,
            Material.IRON_PICKAXE,
            Material.GOLDEN_PICKAXE,
            Material.DIAMOND_PICKAXE,
            Material.matchMaterial("NETHERITE_PICKAXE") ?: Material.AIR
        ),

        /**
         * Represents all Shovels
         */
        SHOVELS(
            Material.WOODEN_SHOVEL,
            Material.STONE_SHOVEL,
            Material.IRON_SHOVEL,
            Material.GOLDEN_SHOVEL,
            Material.DIAMOND_SHOVEL,
            Material.matchMaterial("NETHERITE_SHOVEL") ?: Material.AIR
        ),

        /**
         * Represents all Hoes
         */
        HOES(
            Material.WOODEN_HOE,
            Material.STONE_HOE,
            Material.IRON_HOE,
            Material.GOLDEN_HOE,
            Material.DIAMOND_HOE,
            Material.matchMaterial("NETHERITE_HOE") ?: Material.AIR
        ),

        /**
         * Represents all Melee Weapons (Swords and Axes)
         */
        MELEE_WEAPONS(SWORDS, AXES),

        /**
         * Represents all mining tools (Shovels, Pickaxes, Axes, and Hoes)
         */
        MINING_TOOLS(SHOVELS, PICKAXES, AXES, HOES),

        /**
         * Represents all tools (Swords, Axes, Pickaxes, Shovels, and Hoes)
         */
        TOOLS(SWORDS, AXES, PICKAXES, SHOVELS, HOES),

        /**
         * Represents all Helmets
         */
        HELMETS(
            Material.LEATHER_HELMET,
            Material.CHAINMAIL_HELMET,
            Material.IRON_HELMET,
            Material.GOLDEN_HELMET,
            Material.DIAMOND_HELMET,
            Material.matchMaterial("NETHERITE_HELMET") ?: Material.AIR
        ),

        /**
         * Represents all Chestplates
         */
        CHESTPLATES(
            Material.LEATHER_CHESTPLATE,
            Material.CHAINMAIL_CHESTPLATE,
            Material.IRON_CHESTPLATE,
            Material.GOLDEN_CHESTPLATE,
            Material.DIAMOND_CHESTPLATE,
            Material.matchMaterial("NETHERITE_CHESTPLATE") ?: Material.AIR
        ),

        /**
         * Represents all Leggings
         */
        LEGGINGS(
            Material.LEATHER_LEGGINGS,
            Material.CHAINMAIL_LEGGINGS,
            Material.IRON_LEGGINGS,
            Material.GOLDEN_LEGGINGS,
            Material.DIAMOND_LEGGINGS,
            Material.matchMaterial("NETHERITE_LEGGINGS") ?: Material.AIR
        ),

        /**
         * Represents all Boots
         */
        BOOTS(
            Material.LEATHER_BOOTS,
            Material.CHAINMAIL_BOOTS,
            Material.IRON_BOOTS,
            Material.GOLDEN_BOOTS,
            Material.DIAMOND_BOOTS,
            Material.matchMaterial("NETHERITE_BOOTS") ?: Material.AIR
        ),

        /**
         * Represents all Armor (Helmets, Chestplates, Leggings, and Boots)
         */
        ARMOR(HELMETS, CHESTPLATES, LEGGINGS, BOOTS),

        /**
         * Target that applies to all materials.
         */
        ALL(TOOLS, ARMOR)

        ;

        private val validMaterials: List<Material>

        constructor(vararg validMaterials: Material) {
            this.validMaterials = validMaterials.toList()
                .stream()
                .filter { it != Material.AIR }
                .collect(Collectors.toList())
        }

        constructor(vararg targets: Target) {
            this.validMaterials = targets.toList()
                .stream()
                .map { it.validMaterials }
                .flatMap { it.stream() }
                .filter { it != Material.AIR}
                .collect(Collectors.toList())
        }

        private fun mapTargets(targets: Array<Target>): ArrayList<Material> {
            val materials = ArrayList<Material>()

            targets.forEach { materials.addAll(it.getValidMaterials()) }

            return materials
        }

        /**
         * Fetches all of the valid materiasl this Target supports.
         * @return Array of Valid Materials
         */
        fun getValidMaterials(): List<Material> {
            return validMaterials
        }

        /**
         * Checks if the given Material is valid for this Target.
         * @param material Material to check
         * @return True if the Material is valid, false otherwise
         */
        fun isValid(material: Material): Boolean {
            return validMaterials.contains(material)
        }
    }

}