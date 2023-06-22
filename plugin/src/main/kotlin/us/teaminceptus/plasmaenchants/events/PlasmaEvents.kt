package us.teaminceptus.plasmaenchants.events

import org.bukkit.Material
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
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.GrindstoneInventory
import org.bukkit.inventory.meta.ItemMeta
import us.teaminceptus.plasmaenchants.PlasmaEnchants
import us.teaminceptus.plasmaenchants.api.*

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

            val enchants = meta.plasmaEnchants.filter { it.key.type == type }
            enchants.forEach { it.key.accept(event, it.value) }

            val artifact = meta.artifact.takeIf { it != null && it.type == type } ?: return
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

    // Vanilla Functionality Events

    @EventHandler
    fun grindstone(event: InventoryClickEvent) {
        if (event.isCancelled) return
        if (event.clickedInventory !is GrindstoneInventory) return

        val p = event.whoClicked as? Player ?: return
        val inv = event.clickedInventory as GrindstoneInventory

        val item = event.currentItem ?: return
        val meta = item.itemMeta ?: return

        val exp = meta.plasmaEnchants.map { it.value }.sum()
        meta.clearPlasmaEnchants()
        item.itemMeta = meta

        if (event.slot == 2) {
            event.currentItem = item
            p.exp += exp
        }
        else
            inv.setItem(2, item)
    }

    @EventHandler
    fun anvil(event: PrepareAnvilEvent) {
        val inv = event.inventory

        val first = inv.getItem(0) ?: return
        val second = inv.getItem(1) ?: return

        if (first.type != second.type && second.type != Material.ENCHANTED_BOOK) return
        
        val fMeta = first.itemMeta ?: return
        val sMeta = second.itemMeta ?: return

        if (sMeta.plasmaEnchants.keys.any { fMeta.hasConflictingEnchant(it) }) {
            event.result = null
            return
        }

        val fArtifact = fMeta.hasArtifact()
        val sArtifact = sMeta.hasArtifact()

        return when {
            fArtifact && sArtifact && (fMeta.artifact != sMeta.artifact) -> {
                event.result = null
            }
            !fArtifact && sArtifact -> {
                val clone = first.clone()
                val meta = clone.itemMeta!!

                val artifact = sMeta.artifact!!
                meta.artifact = artifact
                meta.combinePlasmaEnchants(sMeta)

                clone.itemMeta = meta
                event.result = clone
            }
            else -> {
                val clone = first.clone()
                val meta = clone.itemMeta!!

                meta.combinePlasmaEnchants(sMeta)
                clone.itemMeta = meta

                event.result = clone
            }
        }
    }

}