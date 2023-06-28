package us.teaminceptus.plasmaenchants.v1_16

import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Statistic
import org.bukkit.entity.Hoglin
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.PiglinAbstract
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.entity.Zoglin
import org.bukkit.event.Event
import org.bukkit.inventory.ItemStack
import us.teaminceptus.plasmaenchants.api.*
import us.teaminceptus.plasmaenchants.api.PTarget.*
import us.teaminceptus.plasmaenchants.api.PType.Companion.ATTACKING
import us.teaminceptus.plasmaenchants.api.PType.Companion.SHOOT_BOW
import us.teaminceptus.plasmaenchants.api.artifacts.PArtifact
import us.teaminceptus.plasmaenchants.api.artifacts.PArtifacts

@Suppress("unchecked_cast")
enum class PArtifacts1_16(
    override val target: PTarget,
    private val info: Action<*>,
    override val ringItem: ItemStack,
    override val itemType: Material,
    override val color: ChatColor = ChatColor.YELLOW
) : PArtifact {

    // Melee Artifacts

    BLACKSTONE(
        MELEE_WEAPONS, Action(ATTACKING) { event ->
            val target = event.entity as? LivingEntity ?: return@Action
            if (target is PiglinAbstract || target is Hoglin || target is Zoglin)
                event.damage *= 2.0
        }, ItemStack(Material.BLACKSTONE, 64), Material.GILDED_BLACKSTONE, ChatColor.GOLD
    ),

    NETHERITE(
        PICKAXES, Action(PType.BLOCK_BREAK) { event ->
            if (event.block.type == Material.ANCIENT_DEBRIS && event.isDropItems)
                event.block.getDrops(event.player.inventory.itemInMainHand)
                    .forEach { event.player.world.dropItemNaturally(event.block.location, it) }
        }, ItemStack(Material.NETHERITE_INGOT, 4), Material.NETHERITE_INGOT, ChatColor.AQUA
    ),

    // Ranged Artifacts

    WARPED(
        RANGED, Action(SHOOT_BOW) { event ->
            val proj = event.projectile as? Projectile ?: return@Action
            val p = proj.shooter as? Player ?: return@Action
            val count = p.getStatistic(Statistic.PICKUP, Material.WARPED_WART_BLOCK).toString().length

            proj.velocity.multiply(count / 2)
        }, ItemStack(Material.WARPED_WART_BLOCK, 48), Material.WARPED_BUTTON, ChatColor.AQUA
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