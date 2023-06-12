package us.teaminceptus.plasmaenchants

import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDamageEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import us.teaminceptus.plasmaenchants.api.PType
import us.teaminceptus.plasmaenchants.api.getArtifact
import us.teaminceptus.plasmaenchants.api.getPlasmaEnchants

internal class PlasmaEvents(plugin: PlasmaEnchants) : Listener {

    init {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    private fun execute(player: Player?, event: Event, type: PType<*>) {
        if (player == null) return
        if (event is Cancellable && event.isCancelled) return

        val items = listOf(
            player.inventory.armorContents.toList(),
            listOf(player.inventory.itemInMainHand),
            listOf(player.inventory.itemInOffHand)
        ).flatten()

        items.forEach { item ->
            val meta: ItemMeta = item.itemMeta ?: return

            val enchants = meta.getPlasmaEnchants().filter { it.key.type == type }
            enchants.forEach { it.key.accept(event, it.value) }

            val artifact = meta.getArtifact().takeIf { it != null && it.type == type } ?: return
            artifact.accept(event)
        }
    }

    @EventHandler
    fun onAttack(event: EntityDamageByEntityEvent) = execute(event.entity as? Player, event, PType.ATTACKING)

    @EventHandler
    fun onDefend(event: EntityDamageByEntityEvent) = execute(event.entity as? Player, event, PType.DEFENDING)

    @EventHandler
    fun onDamage(event: EntityDamageEvent) = execute(event.entity as? Player, event, PType.DAMAGE )

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) = execute(event.player, event, PType.BLOCK_BREAK)

    @EventHandler
    fun onBlockDamage(event: BlockDamageEvent) = execute(event.player, event, PType.MINING)

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) = execute(event.player, event, PType.INTERACT)

    @EventHandler
    fun onBowShoot(event: EntityShootBowEvent) = execute(event.entity as? Player, event, PType.SHOOT_BOW)

}