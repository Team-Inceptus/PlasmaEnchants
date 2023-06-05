package us.teaminceptus.plasmaenchants

import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDamageEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.plugin.RegisteredListener
import us.teaminceptus.plasmaenchants.api.enchants.PEnchantment
import us.teaminceptus.plasmaenchants.api.events.PlayerTickEvent
import us.teaminceptus.plasmaenchants.api.getPlasmaEnchants

internal class PlasmaEvents(plugin: PlasmaEnchants) : Listener {

    init {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    private fun execute(item: ItemStack, event: Event, type: PEnchantment.Type<*>) {
        val meta: ItemMeta = item.itemMeta ?: return

        val enchants = meta.getPlasmaEnchants().filter { it.key.getType() == type }
        enchants.forEach { it.key.accept(event, it.value) }
    }

    @EventHandler
    fun onAttack(event: EntityDamageByEntityEvent) {
        if (event.damager !is Player) return
        val player = event.damager as Player

        execute(player.inventory.itemInMainHand, event, PEnchantment.Type.ATTACKING)
    }

    @EventHandler
    fun onDefend(event: EntityDamageByEntityEvent) {
        if (event.entity !is Player) return
        val player = event.entity as Player
        val items = player.inventory.armorContents

        items.forEach { item -> execute(item, event, PEnchantment.Type.DEFENDING) }
    }

    @EventHandler
    fun onDamage(event: EntityDamageEvent) {
        if (event.entity !is Player) return
        val player = event.entity as Player
        val items = player.inventory.armorContents

        items.forEach { item -> execute(item, event, PEnchantment.Type.DAMAGE) }
    }

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) = execute(event.player.inventory.itemInMainHand, event, PEnchantment.Type.BLOCK_BREAK)

    @EventHandler
    fun onBlockDamage(event: BlockDamageEvent) = execute(event.player.inventory.itemInMainHand, event, PEnchantment.Type.MINING)

    @EventHandler
    fun passive(event: PlayerTickEvent) {
        val player = event.player
        val items = listOfNotNull(
            player.inventory.armorContents.toList(),
            listOf(player.inventory.itemInMainHand),
            listOf(player.inventory.itemInOffHand)
        ).flatten()

        items.forEach { item -> execute(item, event, PEnchantment.Type.PASSIVE) }
    }

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) { execute(event.item ?: return, event, PEnchantment.Type.INTERACT) }

    @EventHandler
    fun onBowShoot(event: EntityShootBowEvent) { execute(event.bow ?: return, event, PEnchantment.Type.SHOOT_BOW) }

}