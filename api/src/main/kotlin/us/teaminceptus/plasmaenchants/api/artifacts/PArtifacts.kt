package us.teaminceptus.plasmaenchants.api.artifacts

import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.block.Biome
import org.bukkit.event.Event
import org.bukkit.inventory.ItemStack
import us.teaminceptus.plasmaenchants.api.PTarget
import us.teaminceptus.plasmaenchants.api.PTarget.*
import us.teaminceptus.plasmaenchants.api.PType
import us.teaminceptus.plasmaenchants.api.PType.Companion.ATTACKING
import us.teaminceptus.plasmaenchants.api.PlasmaConfig
import us.teaminceptus.plasmaenchants.api.enchants.PEnchantment
import us.teaminceptus.plasmaenchants.api.enchants.PEnchantments

/**
 * Represents a PlasmaEnchants Artifact.
 * <p>The difference between Artifacts and Enchantments is that Enchantments can be combined, whereas items can only have one artifact. Artifacts can be removed via a Gridstone just like a [PEnchantment].</p>
 */
@Suppress("unchecked_cast")
enum class PArtifacts(
    private val target: PTarget,
    private val info: Action<*>,
    private val ringItems: ItemStack,
    private val color: ChatColor = ChatColor.YELLOW
) : PArtifact {

    // Melee Artifacts

    DIRT(
        MELEE_WEAPONS, Action(ATTACKING) { event ->
            if (event.damager.location.block.biome.name.endsWith("PLAINS"))
                event.damage *= 1.25
        }, ItemStack(Material.DIRT, 64)
    ),

    SAND(
        MELEE_WEAPONS, Action(ATTACKING) { event ->
            if (event.damager.location.block.biome == Biome.DESERT)
                event.damage *= 1.25
        }, ItemStack(Material.SAND, 64)
    )

    // Armor Artifacts

    // Ranged Artifacts

    ;

    private val nameKey = "artifact.${name.lowercase()}"

    override fun getName(): String = PlasmaConfig.getConfig().get(nameKey) ?: name.lowercase().replaceFirstChar { it.uppercase() }

    override fun getDescription(): String = PlasmaConfig.getConfig().get("$nameKey.desc") ?: "No description provided."

    override fun getType(): PType<*> = info.type

    override fun getTarget(): PTarget = target

    override fun getKey(): NamespacedKey = NamespacedKey(PlasmaConfig.getPlugin(), name.lowercase())

    override fun getColor(): ChatColor = color

    private class Action<T : Event>(val type: PType<T>, action: (T) -> Unit) {
        val action: (Event) -> Unit

        init {
            this.action = { event ->
                if (type.getEventClass().isAssignableFrom(event::class.java))
                    action(event as T)
            }
        }
    }

}