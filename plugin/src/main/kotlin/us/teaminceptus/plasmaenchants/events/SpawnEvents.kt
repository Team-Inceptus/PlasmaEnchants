package us.teaminceptus.plasmaenchants.events

import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType
import us.teaminceptus.plasmaenchants.PlasmaEnchants
import us.teaminceptus.plasmaenchants.api.artifacts.PArtifact
import us.teaminceptus.plasmaenchants.api.enchants.PEnchantment
import us.teaminceptus.plasmaenchants.r
import java.security.SecureRandom

class SpawnEvents(private val plugin: PlasmaEnchants) : Listener {

    init {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    fun getRandomEnchantment(): PEnchantment =
        plugin.enchantments.filter { !it.isDisabled }.random()

    fun generateEnchantmentBook(): ItemStack =
        getRandomEnchantment().generateBook(r.nextInt(plugin.enchantmentSpawnMinLevel, plugin.enchantmentSpawnMaxLevel + 1))


    @EventHandler
    fun kill(event: EntityDamageByEntityEvent) {
        if (event.entity !is LivingEntity) return
        if (event.damager !is Player) return

        val entity = event.entity as LivingEntity
        val p = event.damager as Player

        val luckAmp = (p.getPotionEffect(PotionEffectType.LUCK)?.amplifier ?: -1) + 1
        var lootingAmp = 1.0
        if (p.inventory.itemInMainHand.containsEnchantment(Enchantment.LOOT_BONUS_MOBS))
            lootingAmp += p.inventory.itemInMainHand.getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS) * plugin.artifactSpawnLootingModifier

        if (entity.health - event.finalDamage > 0) return

        val artifactChance = plugin.artifactSpawnGlobalKillChance + (luckAmp * plugin.artifactSpawnLuckModifier) + lootingAmp
        if (r.nextDouble() < artifactChance)
            entity.world.dropItemNaturally(entity.location, PArtifact.RAW_ARTIFACT)

        val enchantmentChance = plugin.enchantmentSpawnGlobalKillingChance + (luckAmp * plugin.enchantmentSpawnLuckModifier) + lootingAmp
        if (r.nextDouble() < enchantmentChance)
            entity.world.dropItemNaturally(entity.location, generateEnchantmentBook())
    }


}