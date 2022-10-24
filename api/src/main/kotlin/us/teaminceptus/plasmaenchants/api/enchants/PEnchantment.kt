package us.teaminceptus.plasmaenchants.api.enchants

import org.bukkit.Material
import org.bukkit.entity.Player
import java.util.Objects
import java.util.function.Consumer
import java.util.stream.Collectors

/**
 * Represents a PlasmaEnchants Enchantment.
 */
interface PEnchantment : Consumer<Player> {

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

        // TODO: Add more Material Targets

        /**
         * Target that applies to all materials.
         */
        ALL(SWORDS, AXES, PICKAXES)

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


        fun mapTargets(targets: Array<Target>): ArrayList<Material> {
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
     * Rolls the table chance for this Enchantment if it should spawn in an Enchantment Table.
     * @return result of the roll
     */
    fun rollTableChance(): Boolean

    /**
     * Fetches the Target Type of this PEnchantment.
     * @return Target Type
     */
    fun getTarget(): Target

}