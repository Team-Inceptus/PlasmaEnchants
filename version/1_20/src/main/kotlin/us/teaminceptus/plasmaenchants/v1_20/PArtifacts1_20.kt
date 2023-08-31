package us.teaminceptus.plasmaenchants.v1_20

import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.LivingEntity
import org.bukkit.event.Event
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.util.Vector
import us.teaminceptus.plasmaenchants.api.*
import us.teaminceptus.plasmaenchants.api.PTarget.AXES
import us.teaminceptus.plasmaenchants.api.PTarget.SWORDS
import us.teaminceptus.plasmaenchants.api.PType.Companion.ATTACKING
import us.teaminceptus.plasmaenchants.api.PType.Companion.INTERACT
import us.teaminceptus.plasmaenchants.api.artifacts.PArtifact

@Suppress("unchecked_cast")
enum class PArtifacts1_20(
    override val target: PTarget,
    private val info: Action<*>,
    override val ringItem: ItemStack,
    override val itemType: Material,
    override val color: ChatColor = ChatColor.YELLOW
) : PArtifact {

    BAMBOO(
        SWORDS, Action(ATTACKING) { event ->
            val target = event.entity as? LivingEntity ?: return@Action
            target.velocity = target.velocity.add(event.damager.location.direction.multiply(1.5))
        }, ItemStack(Material.BAMBOO_BLOCK, 64), Material.BAMBOO
    ),

    BRICK(
        AXES, Action(ATTACKING) { event ->
            val target = event.entity as? LivingEntity ?: return@Action
            target.velocity = target.velocity.add(Vector(0.0, -0.06, 0.0))
        }, ItemStack(Material.BRICK, 64), Material.DECORATED_POT
    )

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