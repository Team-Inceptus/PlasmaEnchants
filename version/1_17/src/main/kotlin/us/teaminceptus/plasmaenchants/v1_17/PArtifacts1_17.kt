package us.teaminceptus.plasmaenchants.v1_17

import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Tag
import org.bukkit.block.data.type.AmethystCluster
import org.bukkit.event.Event
import org.bukkit.event.entity.EntityDamageEvent.DamageCause
import org.bukkit.inventory.ItemStack
import us.teaminceptus.plasmaenchants.api.*
import us.teaminceptus.plasmaenchants.api.PTarget.*
import us.teaminceptus.plasmaenchants.api.PType.Companion.BLOCK_BREAK
import us.teaminceptus.plasmaenchants.api.PType.Companion.DAMAGE
import us.teaminceptus.plasmaenchants.api.artifacts.PArtifact
import us.teaminceptus.plasmaenchants.api.artifacts.PArtifacts

@Suppress("unchecked_cast")
enum class PArtifacts1_17(
    override val target: PTarget,
    private val info: Action<*>,
    override val ringItem: ItemStack,
    override val itemType: Material,
    override val color: ChatColor = ChatColor.YELLOW
) : PArtifact {

    // Armor Artifacts

    BLAST(
        CHESTPLATES, Action(DAMAGE) { event ->
            if (event.cause == DamageCause.BLOCK_EXPLOSION || event.cause == DamageCause.ENTITY_EXPLOSION)
                event.isCancelled = true
        }, ItemStack(Material.DEEPSLATE, 64), Material.TNT, ChatColor.GOLD
    ),

    // Tool Artifacts

    COPPER(
        PICKAXES, Action(BLOCK_BREAK) { event ->
            if (Tag.COPPER_ORES.isTagged(event.block.type) && event.isDropItems)
                event.block.getDrops(event.player.inventory.itemInMainHand)
                    .forEach { event.player.world.dropItemNaturally(event.block.location, it) }
        }, ItemStack(Material.COPPER_INGOT, 64), Material.COPPER_INGOT, ChatColor.AQUA
    ),

    AMETHYST(
        PICKAXES, Action(BLOCK_BREAK) { event ->
            if (event.block.blockData is AmethystCluster && event.isDropItems)
                event.block.getDrops(event.player.inventory.itemInMainHand)
                    .forEach { event.player.world.dropItemNaturally(event.block.location, it) }
        }, ItemStack(Material.AMETHYST_SHARD, 24), Material.AMETHYST_SHARD, ChatColor.AQUA
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