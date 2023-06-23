package us.teaminceptus.plasmaenchants.events

import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.VillagerAcquireTradeEvent
import org.bukkit.event.player.PlayerFishEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.BlockInventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.MerchantInventory
import org.bukkit.inventory.MerchantRecipe
import org.bukkit.loot.LootTables
import org.bukkit.loot.Lootable
import org.bukkit.potion.PotionEffectType
import us.teaminceptus.plasmaenchants.PlasmaEnchants
import us.teaminceptus.plasmaenchants.api.artifacts.PArtifact
import us.teaminceptus.plasmaenchants.api.config.EnchantmentChanceConfiguration
import us.teaminceptus.plasmaenchants.api.enchants.PEnchantment
import us.teaminceptus.plasmaenchants.api.hasArtifact
import us.teaminceptus.plasmaenchants.r

class SpawnEvents(private val plugin: PlasmaEnchants) : Listener {

    lateinit var effectiveArtifactChances: Map<PArtifact, Double>

    private fun loadEffectiveArtifactChances() {
        val chancesSum = plugin.artifacts.sumOf { plugin.getArtifactTradesCraftableChance(it) }
        effectiveArtifactChances = plugin.artifacts.associateWith { plugin.getArtifactTradesCraftableChance(it) / chancesSum }
    }

    init {
        plugin.server.pluginManager.registerEvents(this, plugin)
        loadEffectiveArtifactChances()
    }

    // Extension Util

    private inline val PEnchantment.isBlacklisted: Boolean
        get() {
            if (plugin.whitelistedSpawnEnchantments.isNotEmpty())
                return !plugin.whitelistedSpawnEnchantments.contains(this)

            return plugin.blacklistedSpawnEnchantments.contains(this)
        }

    private inline val PEnchantment.isFishingBlacklisted: Boolean
        get() {
            if (isBlacklisted) return true

            if (plugin.enchantmentSpawnFishingWhitelistedEnchants.isNotEmpty())
                return !plugin.enchantmentSpawnFishingWhitelistedEnchants.contains(this)

            return plugin.enchantmentSpawnFishingBlacklistedEnchants.contains(this)
        }

    private inline val PArtifact.isMerchantBlacklisted: Boolean
        get() {
            if (plugin.artifactTradesCraftableWhitelist.isNotEmpty())
                return !plugin.artifactTradesCraftableWhitelist.contains(this)

            return plugin.artifactTradesCraftableBlacklist.contains(this)
        }

    private inline val EntityType.baseEnchantChance: Double
        get() = plugin.enchantmentSpawnKillingChanceConfiguration.firstOrNull { it.type == this }?.chance ?: plugin.enchantmentSpawnGlobalKillingChance

    private inline val Material.baseEnchantChance: Double
        get() = plugin.enchantmentSpawnMiningChanceConfiguration.firstOrNull { it.type == this }?.chance ?: plugin.enchantmentSpawnGlobalMiningChance

    private inline val LootTables.baseEnchantChance: Double
        get() = plugin.enchantmentSpawnLootChanceConfiguration.firstOrNull { it.type == this }?.chance ?: plugin.enchantmentSpawnGlobalLootChance

    private inline val LootTables.baseArtifactChance: Double
        get() = plugin.getArtifactSpawnGlobalLootChance(this)

    private inline val Villager.Type.isEnchantmentBlacklisted: Boolean
        get() {
            if (plugin.enchantmentTradesWhitelistedTypes.isNotEmpty())
                return !plugin.enchantmentTradesWhitelistedTypes.contains(this)

            return plugin.enchantmentTradesBlacklistedTypes.contains(this)
        }

    // Function Util

    fun <T : Enum<T>> getRandomEnchantment(fishing: Boolean = false, configuration: EnchantmentChanceConfiguration<T>? = null): PEnchantment? =
        plugin.enchantments.filter {
            !it.isDisabled && !it.isBlacklisted && (configuration?.isAllowed(it) ?: true) && !(fishing && it.isFishingBlacklisted)
        }.randomOrNull()

    fun <T: Enum<T>> generateEnchantmentBook(min: Int = plugin.enchantmentSpawnMiningMaxLevel, max: Int = plugin.enchantmentSpawnMaxLevel, fishing: Boolean = false, configuration: EnchantmentChanceConfiguration<T>? = null): ItemStack? {
        var min0 = min
        var max0 = max
        val enchant = getRandomEnchantment(fishing, configuration)

        if (configuration != null) {
            min0 = configuration.minLevel
            max0 = configuration.maxLevel
        }

        return enchant?.generateBook(r.nextInt(min0, max0 + 1))
    }

    fun isAllowed(type: Any, blacklist: List<Any>, whitelist: List<Any>): Boolean {
        if (whitelist.isNotEmpty())
            return whitelist.contains(type)

        return !blacklist.contains(type)
    }

    fun generateMerchantRecipe(enchant: PEnchantment, min: Int, max: Int, reverse: Boolean = false): MerchantRecipe {
        val level = r.nextInt(min, max + 1)
        val book = enchant.generateBook(level)
        val emeraldItem = ItemStack(Material.EMERALD, plugin.getEnchantmentTradesEmeraldPrice(enchant) * level)

        val recipe = MerchantRecipe(
            if (reverse) emeraldItem else book,
            0, plugin.enchantmentTradesMaxUses, true, level * 5, 0.0F
        )

        recipe.addIngredient(if (reverse) book else emeraldItem)
        return recipe
    }

    fun getRandomMerchantArtifact(): PArtifact {
        val distribution = arrayOfNulls<Double>(effectiveArtifactChances.size)

        var cumulative = 0.0

        for ((i, percent) in effectiveArtifactChances.values.withIndex()) {
            cumulative += percent
            distribution[i] = cumulative
        }

        val random = r.nextDouble()
        var j = 0
        while (j < distribution.size && random > distribution[j]!!) j++

        return effectiveArtifactChances.keys.elementAt(j)
    }

    fun generateMerchantRecipe(artifact: PArtifact?, reverse: Boolean = false): MerchantRecipe {
        val recipe: MerchantRecipe
        if (reverse) {
            recipe = MerchantRecipe(
                ItemStack(Material.EMERALD, plugin.artifactTradesBuyPrice),
                0, plugin.artifactTradesMaxUses, true, 0, 0.0F
            )

            recipe.addIngredient(PArtifact.RAW_ARTIFACT)
            return recipe
        } else {
            recipe = MerchantRecipe(
                artifact?.item ?: PArtifact.RAW_ARTIFACT,
                0, plugin.artifactTradesMaxUses, true, 0, 0.0F
            )

            var price: Int = plugin.artifactTradesBuyPrice
            if (artifact != null) price *= artifact.priceMultiplier

            val amount = (price + r.nextInt(-2, 3)).coerceAtMost(128)

            if (amount > 64) {
                recipe.addIngredient(ItemStack(Material.EMERALD, 64))
                recipe.addIngredient(ItemStack(Material.EMERALD, amount - 64))
            } else
                recipe.addIngredient(ItemStack(Material.EMERALD, amount))
        }

        return recipe
    }

    // Events

    @EventHandler
    fun kill(event: EntityDamageByEntityEvent) {
        if (event.entity !is LivingEntity) return
        if (event.damager !is Player) return

        val entity = event.entity as LivingEntity
        val p = event.damager as Player

        val luckAmp = (p.getPotionEffect(PotionEffectType.LUCK)?.amplifier ?: -1) + 1
        val lootingAmp = p.inventory.itemInMainHand.getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS)

        if (entity.health - event.finalDamage > 0) return

        val artifactChance = plugin.artifactSpawnGlobalKillChance + (luckAmp * plugin.artifactSpawnLuckModifier) + (lootingAmp * plugin.artifactSpawnKillingLootingModifier)
        if (r.nextDouble() < artifactChance)
            entity.world.dropItemNaturally(entity.location, PArtifact.RAW_ARTIFACT)

        if (isAllowed(entity.type, plugin.enchantmentSpawnKillingBlacklistedMobs, plugin.enchantmentSpawnKillingWhitelistedMobs)) {
            val configuration = plugin.enchantmentSpawnKillingChanceConfiguration.firstOrNull { it.type == entity.type }
            val enchantmentChance =
                configuration?.chance ?: (entity.type.baseEnchantChance + (luckAmp * plugin.enchantmentSpawnLuckModifier) + (lootingAmp * plugin.enchantmentSpawnKillingLootingModifier))

            if (r.nextDouble() < enchantmentChance)
                entity.world.dropItemNaturally(
                    entity.location,
                    generateEnchantmentBook(
                        plugin.enchantmentSpawnKillingMinLevel, plugin.enchantmentSpawnKillingMaxLevel, false,
                        plugin.enchantmentSpawnKillingChanceConfiguration.firstOrNull { it.type == entity.type }
                    ) ?: return
                )
        }
    }

    @EventHandler
    fun loot(event: PlayerInteractEvent) {
        if (!event.hasBlock()) return

        val block = event.clickedBlock!!
        val p = event.player

        val luckAmp = (p.getPotionEffect(PotionEffectType.LUCK)?.amplifier ?: -1) + 1

        if (block.state !is Lootable) return
        if (block.state !is BlockInventoryHolder) return

        val lootable = block.state as Lootable
        val inv = (block.state as BlockInventoryHolder).inventory
        val randomSlot = (0 until inv.size).filter { inv.getItem(it) == null }.randomOrNull() ?: return

        if (lootable.lootTable == null) return
        val tableI = lootable.lootTable!!
        val table = LootTables.values().firstOrNull { it.key == tableI.key } ?: return

        val artifactChance = plugin.artifactSpawnGlobalLootChance + (luckAmp * plugin.artifactSpawnLuckModifier)
        if (r.nextDouble() < artifactChance)
            inv.setItem(randomSlot, PArtifact.RAW_ARTIFACT)

        if (isAllowed(table, plugin.enchantmentSpawnLootBlacklistedLootTables, plugin.enchantmentSpawnLootWhitelistedLootTables)) {
            val configuration = plugin.enchantmentSpawnLootChanceConfiguration.firstOrNull { it.type == table }
            val enchantChance = configuration?.chance ?: (table.baseEnchantChance + (luckAmp * plugin.enchantmentSpawnLuckModifier))
            if (r.nextDouble() < enchantChance) {
                inv.setItem(randomSlot, generateEnchantmentBook(
                    plugin.enchantmentSpawnLootMinLevel, plugin.enchantmentSpawnLootMaxLevel, false,
                    plugin.enchantmentSpawnLootChanceConfiguration.firstOrNull { it.type == table }
                ) ?: return)
            }
        }
    }

    @EventHandler
    fun fish(event: PlayerFishEvent) {
        if (event.state != PlayerFishEvent.State.CAUGHT_FISH) return
        if (event.caught !is Item) return

        val p = event.player
        val item = event.caught as Item

        val luckAmp = (p.getPotionEffect(PotionEffectType.LUCK)?.amplifier ?: -1) + 1
        val luckSeaAmp = p.inventory.itemInMainHand.getEnchantmentLevel(Enchantment.LUCK)

        val enchantChance = plugin.enchantmentSpawnGlobalFishingChance + (luckAmp * plugin.enchantmentSpawnLuckModifier) + (luckSeaAmp * plugin.enchantmentSpawnFishingLuckOfTheSeaModifier)
        if (r.nextDouble() < enchantChance) {
            if (enchantChance >= 1.0)
                item.world.dropItemNaturally(item.location, item.itemStack).apply {
                    velocity = item.velocity
                }

            item.setItemStack(generateEnchantmentBook(plugin.enchantmentSpawnFishingMinLevel, plugin.enchantmentSpawnFishingMaxLevel, true))
        }

        val artifactChance = plugin.artifactSpawnGlobalFishingChance + (luckAmp * plugin.artifactSpawnLuckModifier) + (luckSeaAmp * plugin.artifactSpawnFishingLuckOfTheSeaModifier)
        if (r.nextDouble() < artifactChance) {
            if (artifactChance >= 1.0)
                item.world.dropItemNaturally(item.location, item.itemStack).apply {
                    velocity = item.velocity
                }

            item.setItemStack(PArtifact.RAW_ARTIFACT)
        }
    }

    @EventHandler
    fun mine(event: BlockBreakEvent) {
        val p = event.player
        val block = event.block

        val luckAmp = (p.getPotionEffect(PotionEffectType.LUCK)?.amplifier ?: -1) + 1
        val fortuneAmp = p.inventory.itemInMainHand.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS)

        val artifactChance = plugin.artifactSpawnGlobalMiningChance + (luckAmp * plugin.artifactSpawnLuckModifier) + (fortuneAmp * plugin.artifactSpawnMiningFortuneModifier)
        if (r.nextDouble() < artifactChance)
            block.world.dropItemNaturally(block.location, PArtifact.RAW_ARTIFACT)

        if (isAllowed(block.type, plugin.enchantmentSpawnMiningBlacklistedBlocks, plugin.enchantmentSpawnMiningWhitelistedBlocks) &&
            !(plugin.isEnchantmentSpawnMiningIgnoreSilkTouch && p.inventory.itemInMainHand.containsEnchantment(Enchantment.SILK_TOUCH))) {

            val configuration = plugin.enchantmentSpawnMiningChanceConfiguration.firstOrNull { it.type == block.type }
            val enchantChance =
                configuration?.chance ?: (block.type.baseEnchantChance + (luckAmp * plugin.enchantmentSpawnLuckModifier) + (fortuneAmp * plugin.enchantmentSpawnMiningFortuneModifier))

            if (r.nextDouble() < enchantChance)
                block.world.dropItemNaturally(
                    block.location,
                    generateEnchantmentBook(
                        plugin.enchantmentSpawnMiningMinLevel, plugin.enchantmentSpawnMiningMaxLevel, false,
                        plugin.enchantmentSpawnMiningChanceConfiguration.firstOrNull { it.type == block.type }
                    ) ?: return
                )
        }
    }

    private fun replaceArtifactTrade(entity: AbstractVillager, event: VillagerAcquireTradeEvent) {
        val artifact = if (plugin.isArtifactTradesCraftableEnabled || r.nextDouble() < 0.25) getRandomMerchantArtifact() else null
        if (entity.recipes.any { recipe ->
            recipe.result.itemMeta?.hasArtifact() == true || PArtifact.RAW_ARTIFACT.isSimilar(recipe.result) ||
                    recipe.ingredients.any { it != null && PArtifact.RAW_ARTIFACT.isSimilar(it) || it.itemMeta?.hasArtifact() == true }
        }) return

        if (entity is Villager) {
            if (!plugin.artifactTradesProfessions.contains(entity.profession)) return
            if (entity.villagerLevel < plugin.artifactTradesMinVillagerLevel || entity.villagerLevel > plugin.artifactTradesMaxVillagerLevel) return
        }

        if (plugin.isArtifactTradesCraftableEnabled && artifact != null) {
            if (entity is Villager) {
                if (!plugin.artifactTradesCraftableProfessions.contains(entity.profession)) return
                if (artifact.isMerchantBlacklisted) return
            }
        }

        val generated = generateMerchantRecipe(artifact, plugin.isArtifactTradesBuyEnabled && r.nextBoolean())
        if (r.nextDouble() < plugin.artifactTradesChance)
            event.recipe = generated
    }

    private fun replaceEnchantmentTrade(entity: AbstractVillager, event: VillagerAcquireTradeEvent) {
        val recipe = event.recipe
        val enchant = getRandomEnchantment() ?: return

        if (plugin.isEnchantmentTradesReplaceBooksOnly && recipe.result.type != Material.ENCHANTED_BOOK) return

        if (entity is Villager) {
            if (!plugin.enchantmentTradesProfessions.contains(entity.profession)) return
            if (entity.villagerLevel < plugin.enchantmentTradesMinVillagerLevel || entity.villagerLevel > plugin.enchantmentTradesMaxVillagerLevel) return
            if (entity.villagerType.isEnchantmentBlacklisted) return
        }

        val configuration = plugin.enchantmentTradesReplaceConfiguration.firstOrNull { it.plasma == enchant }
        val chance = plugin.enchantmentTradesReplaceChance
        var min = plugin.enchantmentTradesMinEnchantLevel
        var max = plugin.enchantmentTradesMaxEnchantLevel

        if (configuration != null && recipe.result.hasItemMeta() && recipe.result.itemMeta!!.hasEnchants()) {
            val meta = recipe.result.itemMeta!!

            for ((enchantment, level) in meta.enchants) {
                if (configuration.bukkit != enchantment) continue
                if (level < configuration.minBukkitLevel || level > configuration.maxBukkitLevel) return

                min = configuration.minPlasmaLevel
                max = configuration.maxPlasmaLevel
            }
        }

        val generated = generateMerchantRecipe(enchant, min, max, plugin.isEnchantmentTradesBuyBooks && r.nextBoolean())
        if (r.nextDouble() < chance)
            event.recipe = generated
    }

    @EventHandler
    fun unlockTrade(event: VillagerAcquireTradeEvent) {
        val entity = event.entity

        if (entity is WanderingTrader) {
            if (plugin.isEnchantmentTradesIncludeWanderingTrader)
                replaceEnchantmentTrade(entity, event)

            if (plugin.isArtifactTradesIncludeWanderingTrader)
                replaceArtifactTrade(entity, event)

            return
        }

        replaceEnchantmentTrade(entity, event)
        replaceArtifactTrade(entity, event)
    }


}