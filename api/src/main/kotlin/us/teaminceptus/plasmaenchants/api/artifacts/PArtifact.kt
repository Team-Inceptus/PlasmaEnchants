package us.teaminceptus.plasmaenchants.api.artifacts

import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import org.bukkit.ChatColor
import org.bukkit.Keyed
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import us.teaminceptus.plasmaenchants.api.PTarget
import us.teaminceptus.plasmaenchants.api.PType
import us.teaminceptus.plasmaenchants.api.PlasmaConfig
import java.util.*

/**
 * Represents an artifact that can be applied to an item.
 */
interface PArtifact : Keyed {

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
    fun getName(): String

    /**
     * Fetches the human-readable description of this artifact.
     * @return Description of artifact
     */
    fun getDescription(): String

    /**
     * Fetches the Target Type of this artifact.
     * @return Target Type
     */
    fun getTarget(): PTarget

    /**
     * Fetches the Type of this artifact.
     * @return Type
     */
    fun getType(): PType<*>

    /**
     * Fetches the color of this artifact.
     * @return ChatColor Prefix Color
     */
    fun getColor(): ChatColor
}