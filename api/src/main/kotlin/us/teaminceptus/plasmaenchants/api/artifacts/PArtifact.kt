package us.teaminceptus.plasmaenchants.api.artifacts

import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import org.bukkit.ChatColor
import org.bukkit.Keyed
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.Event
import org.bukkit.inventory.*
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.persistence.PersistentDataType
import us.teaminceptus.plasmaenchants.api.*
import us.teaminceptus.plasmaenchants.api.util.NAMESPACED_KEY
import java.util.*
import java.util.function.Consumer

/**
 * Represents an artifact that can be applied to an item.
 */
interface PArtifact : Keyed, Consumer<Event> {

    companion object {

        /**
         * Represents the raw artifact used for crafting into other Artifacts.
         */
        @JvmStatic
        val RAW_ARTIFACT: ItemStack

        init {
            val raw = ItemStack(Material.PLAYER_HEAD)
            val meta = raw.itemMeta as SkullMeta
            meta.setDisplayName("${ChatColor.YELLOW}${PlasmaConfig.config.get("constants.raw_artifact")}")

            val p = Properties()
            p.load(PlasmaConfig.plugin.javaClass.getResourceAsStream("/util/heads.properties"))

            val profile = GameProfile(UUID.nameUUIDFromBytes("plasmaenchants:raw_artifact".toByteArray()), null)
            profile.properties.put("textures", Property("textures", p.getProperty("raw_artifact")))

            val setP = meta.javaClass.getDeclaredMethod("setProfile", GameProfile::class.java)
            setP.isAccessible = true
            setP.invoke(meta, profile)

            raw.itemMeta = meta
            RAW_ARTIFACT = raw
        }

    }

    /**
     * Fetches the human-readable name of this artifact.
     * @return Name of artifact
     */
    val displayName
        get() = key.key.substringBeforeLast('_').uppercase().split("_").joinToString(" ") { it -> it.lowercase().replaceFirstChar { it.uppercase() } }

    /**
     * Fetches the Target Type of this artifact.
     * @return Target Type
     */
    val target: PTarget

    /**
     * Fetches the Type of this artifact.
     * @return Type
     */
    val type: PType<*>

    /**
     * Fetches the color of this artifact.
     * @return ChatColor Prefix Color
     */
    val color: ChatColor

    /**
     * <p>Fetches the ring item used in the crafting of this Artifact.</p>
     * <p>When crafting an artifact, this item (and its count) will surround [PArtifact.RAW_ARTIFACT] to produce this artifact in item form.</p>
     * @return Artifact Ring Item
     */
    val ringItem: ItemStack

    /**
     * Fetches a ShapedRecipe instance clone for this artifact.
     * @return ShapedRecipe
     */
    @Suppress("deprecation")
    val recipe: ShapedRecipe
        get() {
            val recipe = ShapedRecipe(NamespacedKey(PlasmaConfig.plugin, key.key.lowercase()), item)

            recipe.shape("RRR", "RAR", "RRR")
            recipe.setIngredient('R', RecipeChoice.ExactChoice(ringItem))
            recipe.setIngredient('A', RecipeChoice.ExactChoice(RAW_ARTIFACT))
            recipe.group = "PlasmaEnchants Artifact"

            return recipe
        }

    /**
     * Fetches the Material type of this artifact.
     * @return Material Type
     */
    val itemType: Material

    /**
     * Fetches the ItemStack representation of this artifact.
     * @return ItemStack Representation
     */
    val item: ItemStack
        get() = ItemStack(itemType).clone().apply {
            itemMeta = itemMeta!!.apply {
                setDisplayName("$color${asString()}")

                addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true)
                addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_POTION_EFFECTS)

                persistentDataContainer[artifactsKey, NAMESPACED_KEY] = key
                persistentDataContainer[artifactItemKey, PersistentDataType.BYTE] = 1.toByte()
            }
        }

    /**
     * Fetches the trading price multiplier for this artifact, determined by its rarity.
     * @return Price Multiplier
     */
    val priceMultiplier: Int
        get() {
            return when (color) {
                ChatColor.AQUA -> 5
                ChatColor.GOLD -> 7
                ChatColor.LIGHT_PURPLE -> 10
                else -> 3
            }
        }

    /**
     * Fetches the String representation of this artifact.
     * @return String Representation
     */
    fun asString(): String = String.format(PlasmaConfig.config.locale, PlasmaConfig.config.get("constants.artifact") ?: "%s Artifact", "$color$displayName")

    val isDisabled: Boolean
        /**
         * Fetches whether this artifact is disabled.
         * @return true if disabled, false otherwise
         */
        get() = PlasmaConfig.config.disabledArtifacts.contains(this)
}