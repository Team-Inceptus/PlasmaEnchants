package us.teaminceptus.plasmaenchants.api.enchants

import org.bukkit.Material
import org.bukkit.entity.Player
import org.jetbrains.annotations.NotNull
import java.util.Objects
import java.util.function.BiConsumer
import java.util.function.Consumer
import java.util.stream.Collectors

/**
 * Represents a PlasmaEnchants Enchantment.
 */
interface PEnchantment : BiConsumer<Player, Int> {

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
     * Whether this Enchantment will spawn in an Enchantment Table.
     * @return true if in enchant table, false otherwise
     */
    fun isNatural(): Boolean

    /**
     * Rolls the chance for this Enchantment's level in an enchantment table.
     * @param player Player to use
     * @return result of the roll, 0 if not in enchanting table
     */
    fun rollTable(player: Player): Int

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
    fun getType(): Type

    /**
     * PEnchantment Types
     */
    enum class Type {
        /**
         * Represents the type of Enchantment that will activate when attacking.
         */
        ATTACKING,
        /**
         * Represents the type of Enchantment that will activate when defending or taking damage.
         */
        DEFENDING,
        /**
         * Represents the type of Enchantment that will activate when mining blocks.
         */
        MINING
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
        @NotNull
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