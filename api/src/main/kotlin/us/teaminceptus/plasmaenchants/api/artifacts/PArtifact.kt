package us.teaminceptus.plasmaenchants.api.artifacts

import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import org.bukkit.ChatColor
import org.bukkit.Keyed
import org.bukkit.Material
import org.bukkit.event.Event
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import us.teaminceptus.plasmaenchants.api.PTarget
import us.teaminceptus.plasmaenchants.api.PType
import us.teaminceptus.plasmaenchants.api.PlasmaConfig
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
        val RAW_ARTIFACT = ItemStack(Material.PLAYER_HEAD).apply {
            val meta = itemMeta as SkullMeta

            meta.setDisplayName("${ChatColor.YELLOW}${PlasmaConfig.getConfig().get("artifact.raw")}")

            val p = Properties()
            p.load(PlasmaConfig.getPlugin().javaClass.getResourceAsStream("/util/heads.properties"))

            val profile = GameProfile(UUID.randomUUID(), null)
            profile.properties.put("textures", Property("textures", p.getProperty("raw_artifact")))

            val setP = meta.javaClass.getDeclaredMethod("setProfile", GameProfile::class.java)
            setP.isAccessible = true
            setP.invoke(meta, profile)

            itemMeta = meta
        }
    }

    /**
     * Fetches the human-readable name of this artifact.
     * @return Name of artifact
     */
    val displayName: String

    /**
     * Fetches the human-readable description of this artifact.
     * @return Description of artifact
     */
    val description: String

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
     * Fetches the String representation of this artifact.
     * @return String Representation
     */
    fun asString(): String = String.format(PlasmaConfig.getConfig().locale, PlasmaConfig.getConfig().get("constants.artifact") ?: "%s Artifact", "${color}${displayName}")
}