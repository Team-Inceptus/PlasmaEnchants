package us.teaminceptus.plasmaenchants.events

import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.entity.Tameable
import org.bukkit.event.*
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDamageEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.inventory.InventoryEvent
import org.bukkit.event.inventory.InventoryInteractEvent
import org.bukkit.event.inventory.InventoryMoveItemEvent
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.AnvilInventory
import org.bukkit.inventory.CraftingInventory
import org.bukkit.inventory.GrindstoneInventory
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.RegisteredListener
import org.bukkit.scheduler.BukkitRunnable
import us.teaminceptus.plasmaenchants.PlasmaCommands.Companion.cancelKey
import us.teaminceptus.plasmaenchants.PlasmaCommands.Companion.urlKey
import us.teaminceptus.plasmaenchants.PlasmaEnchants
import us.teaminceptus.plasmaenchants.api.*
import us.teaminceptus.plasmaenchants.api.artifacts.PArtifact
import us.teaminceptus.plasmaenchants.api.util.NAMESPACEDKEY_INT_MAP
import us.teaminceptus.plasmaenchants.api.util.NAMESPACED_KEY
import us.teaminceptus.plasmaenchants.util.PlasmaUtil

internal class PlasmaEvents(private val plugin: PlasmaEnchants) : Listener {

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
        ).flatten().filterNotNull()

        items.forEach { item ->
            val meta: ItemMeta = item.itemMeta ?: return@forEach

            val enchants = meta.plasmaEnchants.filter { it.key.type == type }
            enchants.forEach { it.key.accept(event, it.value) }

            val artifact = meta.artifact.takeIf { it != null && it.type == type } ?: return@forEach
            artifact.accept(event)
        }
    }

    @EventHandler
    fun onAttack(event: EntityDamageByEntityEvent) {
        @Suppress("IMPLICIT_CAST_TO_ANY")
        val attacker = when (event.damager) {
            is Player -> event.damager as Player
            is Projectile -> (event.damager as Projectile).shooter
            is Tameable -> (event.damager as Tameable).owner
            else -> null
        } as? Player

        execute(attacker, event, PType.ATTACKING)
    }

    @EventHandler
    fun onDefend(event: EntityDamageByEntityEvent) = execute(event.entity as? Player, event, PType.DEFENDING)

    @EventHandler
    fun onDamage(event: EntityDamageEvent) = execute(event.entity as? Player, event, PType.DAMAGE )

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) = execute(event.player, event, PType.BLOCK_BREAK)

    @EventHandler
    fun onBlockDamage(event: BlockDamageEvent) = execute(event.player, event, PType.MINING)

    @EventHandler
    fun onBowShoot(event: EntityShootBowEvent) = execute(event.entity as? Player, event, PType.SHOOT_BOW)

    // Vanilla Functionality Events

    @EventHandler
    fun grindstone(event: InventoryClickEvent) {
        val p = event.whoClicked as? Player ?: return
        val inv = event.view.topInventory as? GrindstoneInventory ?: return

        when (event.rawSlot) {
            2 -> {
                var exp = 0

                if (inv.getItem(0) != null) {
                    val item = inv.getItem(0)!!
                    exp += (item.itemMeta?.plasmaEnchants?.size ?: 0) + (item.itemMeta?.enchants?.size ?: 0)
                }

                if (inv.getItem(1) != null) {
                    val item = inv.getItem(1)!!
                    exp += (item.itemMeta?.plasmaEnchants?.size ?: 0) + (item.itemMeta?.enchants?.size ?: 0)
                }

                if (exp == 0) return
                p.giveExp(exp + 1)
            }
            else -> {
                val item = when {
                    event.rawSlot == 0 || event.rawSlot == 1 ->
                        if (event.currentItem == null || event.currentItem!!.type == Material.AIR) p.itemOnCursor else event.currentItem!!
                    event.currentItem != null && event.isShiftClick -> event.currentItem!!
                    else -> return
                }

                val newItem = item.clone()
                val meta = newItem.itemMeta ?: return

                if (!meta.hasPlasmaEnchants() && !meta.hasEnchants()) return

                meta.enchants.keys.forEach { meta.removeEnchant(it) }
                meta.clearPlasmaEnchants()
                meta.artifact = null
                newItem.itemMeta = meta

                PlasmaUtil.sync({
                    if (inv.getItem(0) == null && inv.getItem(1) == null) {
                        inv.setItem(2, null)
                        return@sync
                    }
                    inv.setItem(2, newItem)
                })
            }
        }
    }

    private fun combineVanillaEnchants(meta: ItemMeta, sMeta: ItemMeta) {
        for ((enchant, level) in sMeta.enchants)
            if (meta.hasEnchant(enchant)) {
                val oldLevel = meta.getEnchantLevel(enchant)
                val newLevel = (when {
                    oldLevel < level -> level
                    oldLevel == level -> level + 1
                    else -> oldLevel
                }).coerceAtMost(enchant.maxLevel)

                meta.addEnchant(enchant, newLevel, false)
            }
            else
                meta.addEnchant(enchant, level, false)


        if (sMeta is EnchantmentStorageMeta)
            for ((enchant, level) in sMeta.storedEnchants)
                if (meta.hasEnchant(enchant)) {
                    val oldLevel = meta.getEnchantLevel(enchant)
                    val newLevel = (when {
                        oldLevel < level -> level
                        oldLevel == level -> level + 1
                        else -> oldLevel
                    }).coerceAtMost(enchant.maxLevel)

                    meta.addEnchant(enchant, newLevel, false)
                }
                else
                    meta.addEnchant(enchant, level, false)
    }

    @EventHandler
    fun anvil(event: PrepareAnvilEvent) {
        val inv = event.inventory

        val first = inv.getItem(0) ?: return
        val second = inv.getItem(1) ?: return

        val fMeta = first.itemMeta ?: return
        val sMeta = second.itemMeta ?: return

        val fArtifact = fMeta.hasArtifact()
        val sArtifact = sMeta.hasArtifact()
        val both = fArtifact && sArtifact

        if (!fArtifact && !sArtifact && fMeta.plasmaEnchants.isEmpty() && sMeta.plasmaEnchants.isEmpty())
            return

        if (sMeta.plasmaEnchants.keys.any { fMeta.hasConflictingEnchant(it) }) {
            event.result = null
            return
        }

        if (fMeta.isArtifactItem) {
            event.result = null
            return
        }

        if (first.type != second.type && !sMeta.isArtifactItem && second.type != Material.ENCHANTED_BOOK) {
            event.result = null
            return
        }

        inv.maximumRepairCost = plugin.maxAnvilCost

        return when {
            (both && (fMeta.artifact != sMeta.artifact)) -> {
                event.result = null
            }
            (both && (fMeta.artifact == sMeta.artifact && sMeta.isArtifactItem)) -> {
                event.result = null
            }
            !fArtifact && sArtifact -> {
                val clone = first.clone()
                if (sMeta.plasmaEnchants.any { !it.key.target.isValid(clone.type)}) {
                    event.result = null
                    return
                }

                val meta = clone.itemMeta!!
                val artifact = sMeta.artifact!!
                if (!artifact.target.isValid(clone.type)) {
                    event.result = null
                    return
                }

                meta.artifact = artifact
                meta.combinePlasmaEnchants(sMeta, plugin.isIgnoreEnchantmentLevelRestriction)
                if (!sMeta.isArtifactItem)
                    combineVanillaEnchants(meta, sMeta)

                if (inv.renameText != null)
                    meta.setDisplayName(inv.renameText)

                clone.itemMeta = meta

                event.result = clone
                inv.repairCost += 10 + ((fMeta.plasmaEnchants.size + 1) * 5)
            }
            else -> {
                val clone = first.clone()
                if (sMeta.plasmaEnchants.any { !it.key.target.isValid(clone.type)}) {
                    event.result = null
                    return
                }

                val meta = clone.itemMeta!!
                meta.combinePlasmaEnchants(sMeta, plugin.isIgnoreEnchantmentLevelRestriction)

                if (!sMeta.isArtifactItem)
                    combineVanillaEnchants(meta, sMeta)

                if (inv.renameText != null)
                    meta.setDisplayName(inv.renameText)

                clone.itemMeta = meta

                if (clone == first && meta.artifact == sMeta.artifact) {
                    event.result = null
                    return
                }

                event.result = clone
                inv.repairCost += (fMeta.plasmaEnchants.size + 1) * 5
            }
        }
    }

    @EventHandler
    fun craft(event: PrepareItemCraftEvent) {
        val inv = event.inventory

        // Remove PlasmaEnchants Data from the result item
        if (event.isRepair) {
            val item = inv.result ?: return
            val meta = item.itemMeta ?: return

            if (meta.isArtifactItem) {
                inv.result = null
                return
            }

            if (meta.persistentDataContainer.has(artifactsKey, NAMESPACED_KEY))
                meta.persistentDataContainer.remove(artifactsKey)

            if (meta.persistentDataContainer.has(enchantKey, NAMESPACEDKEY_INT_MAP))
                meta.persistentDataContainer.remove(enchantKey)

            item.itemMeta = meta
            return
        }

        val artifact = event.recipe?.result?.itemMeta?.artifact ?: return
        val amount = artifact.ringItem.amount

        for (item in inv.matrix) {
            if (item == null || item.type == Material.AIR) continue
            if (item.isSimilar(PArtifact.RAW_ARTIFACT)) continue

            if (item.amount < amount) {
                inv.result = null
                return
            }
        }


    }

    @EventHandler
    fun craft(event: CraftItemEvent) {
        val p = event.whoClicked as? Player ?: return
        val inv = event.inventory
        val result = event.recipe.result
        val artifact = result.itemMeta?.artifact ?: return
        val amount = artifact.ringItem.amount

        val newMatrix = arrayOfNulls<ItemStack>(9)
        for ((i, item) in inv.matrix.withIndex()) {
            if (item == null || item.type == Material.AIR) continue
            if (item.isSimilar(PArtifact.RAW_ARTIFACT)) continue

            if (item.amount == amount)
                newMatrix[i] = null
            else {
                val clone = item.clone()
                clone.amount -= amount
                newMatrix[i] = clone
            }
        }

        inv.matrix = newMatrix
        p.setItemOnCursor(result.clone())
    }

    // Other

    @EventHandler
    fun click(event: InventoryClickEvent) {
        if (event.whoClicked !is Player) return
        val p = event.whoClicked as Player

        if (event.clickedInventory == null) return
        if (event.currentItem == null) return

        val inv = event.clickedInventory!!
        if (inv is PlayerInventory || inv is GrindstoneInventory || inv is AnvilInventory) return

        val item = event.currentItem!!
        val meta = item.itemMeta ?: return

        if (meta.persistentDataContainer[cancelKey, PersistentDataType.BYTE] == 1.toByte())
            event.isCancelled = true

        if (meta.persistentDataContainer.has(urlKey, PersistentDataType.STRING)) {
            p.closeInventory()
            val url = meta.persistentDataContainer[urlKey, PersistentDataType.STRING]

            try {
                val component =
                    TextComponent("${plugin.get("plugin.prefix")}${plugin.get("constants.click_here_to_go")}")
                component.clickEvent = ClickEvent(ClickEvent.Action.OPEN_URL, url)
                component.hoverEvent =
                    HoverEvent(HoverEvent.Action.SHOW_TEXT, arrayOf(TextComponent("${ChatColor.AQUA}$url")))

                p.spigot().sendMessage(component)
            } catch (ignored: UnsupportedOperationException) {
                p.sendMessage("${plugin.get("plugin.prefix")}${ChatColor.AQUA}$url")
            }
        }
    }

    @EventHandler
    fun place(event: BlockPlaceEvent) {
        val item = event.itemInHand

        if (item.isSimilar(PArtifact.RAW_ARTIFACT) || item.itemMeta?.hasArtifact() == true)
            event.isCancelled = true
    }

}