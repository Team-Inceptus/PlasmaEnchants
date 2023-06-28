package us.teaminceptus.plasmaenchants.v1_15

import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Bee
import org.bukkit.entity.LivingEntity
import org.bukkit.event.Event
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import us.teaminceptus.plasmaenchants.api.*
import us.teaminceptus.plasmaenchants.api.PTarget.ARMOR
import us.teaminceptus.plasmaenchants.api.PType.Companion.DEFENDING
import us.teaminceptus.plasmaenchants.api.artifacts.PArtifact

@Suppress("unchecked_cast")
enum class PArtifacts1_15(
    override val target: PTarget,
    private val info: Action<*>,
    override val ringItem: ItemStack,
    override val itemType: Material,
    override val color: ChatColor = ChatColor.YELLOW
) : PArtifact {

    // Armor Artifacts

    HONEY(
        ARMOR, Action(DEFENDING) { event ->
            val damager = event.damager as? LivingEntity ?: return@Action
            if (damager is Bee) event.isCancelled = true

            damager.addPotionEffect(PotionEffect(PotionEffectType.SLOW, 20 * 3, 1))
        }, ItemStack(Material.HONEYCOMB, 64), Material.HONEYCOMB
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