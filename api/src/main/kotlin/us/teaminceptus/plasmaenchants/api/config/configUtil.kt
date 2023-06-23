package us.teaminceptus.plasmaenchants.api.config

import com.google.common.collect.ImmutableMap
import org.bukkit.Material
import org.bukkit.configuration.MemorySection
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.entity.Villager
import org.bukkit.loot.LootTables
import us.teaminceptus.plasmaenchants.api.PlasmaConfig

private val isNumber: (FileConfiguration, String) -> Boolean = { config, path -> config.isNumber(path) }

internal val CONFIG_MAP = ImmutableMap.builder<String, ConfigData>()
    .put("language", FileConfiguration::isString, "en")
    .put("max-anvil-cost", FileConfiguration::isInt, 100)

    // Enchantment Configuration
    .putSection("enchantments")
    .put("enchantments.disabled-enchants", FileConfiguration::isList, listOf<String>())
    .put("enchantments.ignore-level-restriction", FileConfiguration::isBoolean, false)
    .put("enchantments.ignore-conflict-restriction", FileConfiguration::isBoolean, false)

    .putSection("enchantments.spawn")
    .put("enchantments.spawn.blacklisted-enchants", FileConfiguration::isList, listOf<String>())
    .put("enchantments.spawn.whitelisted-enchants", FileConfiguration::isList, listOf<String>())
    .put("enchantments.spawn.min-level", FileConfiguration::isInt, 1)
    .put("enchantments.spawn.max-level", FileConfiguration::isInt, 2)
    .put("enchantments.spawn.luck-modifier", isNumber, 0.05)

    .putSection("enchantments.spawn.drops")
    .put("enchantments.spawn.drops.blacklisted-mobs", FileConfiguration::isList, listOf<String>(),
        { value -> EntityType.values().map { it.name.lowercase() }.containsAll(value.map { it.lowercase() }) },
        { old -> old.filter { type -> EntityType.values().map { it.name.lowercase() }.contains(type.lowercase()) } }
    )
    .put("enchantments.spawn.drops.whitelisted-mobs", FileConfiguration::isList, listOf<String>(),
        { value -> EntityType.values().map { it.name.lowercase() }.containsAll(value.map { it.lowercase() }) },
        { old -> old.filter { type -> EntityType.values().map { it.name.lowercase() }.contains(type.lowercase()) } }
    )
    .put("enchantments.spawn.drops.min-level", FileConfiguration::isSet, "default",
        { value -> value.isChildLevel() }
    )
    .put("enchantments.spawn.drops.max-level", FileConfiguration::isSet, "default",
        { value -> value.isChildLevel() }
    )

    .putSection("enchantments.spawn.drops.chance")
    .put("enchantments.spawn.drops.chance.global", isNumber, 0.05)
    .put("enchantments.spawn.drops.chance.looting-modifier", isNumber, 0.01)
    .put("enchantments.spawn.drops.chance.config", FileConfiguration::isList, listOf<Map<String, Any>>(),
        { value -> value.all { map -> map.keyNotNull("bukkit") { enchant -> Enchantment.values().map { it.key.key.lowercase() }.contains(enchant.toString().lowercase()) } &&
                map.keyNotNull("plasma") { it.isEnchantment() } &&
                map.keyNotNull("min-bukkit-level") { it.isChildLevel() } &&
                map.keyNotNull("max-bukkit-level") { it.isChildLevel() } && map.keyNotNull("min-plasma-level") { it.isChildLevel() } &&
                map.keyNotNull("max-plasma-level") { it.isChildLevel() }
        }}
    )

    .putSection("enchantments.spawn.loot")
    .put("enchantments.spawn.loot.blacklisted-loottables", FileConfiguration::isList, listOf<String>(),
        { value -> LootTables.values().map { it.name.lowercase() }.containsAll(value.map { it.lowercase() }) })
    { old -> old.filter { type -> LootTables.values().map { it.name.lowercase() }.contains(type.lowercase()) } }
    .put("enchantments.spawn.loot.whitelisted-loottables", FileConfiguration::isList, listOf<String>(),
        { value -> EntityType.values().map { it.name.lowercase() }.containsAll(value.map { it.lowercase() }) },
        { old -> old.filter { type -> EntityType.values().map { it.name.lowercase() }.contains(type.lowercase()) } }
    )
    .put("enchantments.spawn.loot.min-level", FileConfiguration::isSet, "default",
        { value -> value.isChildLevel() }
    )
    .put("enchantments.spawn.loot.max-level", FileConfiguration::isSet, "default",
        { value -> value.isChildLevel() }
    )
    .putSection("enchantments.spawn.loot.chance")
    .put("enchantments.spawn.loot.chance.global", isNumber, 0.3)
    .put("enchantments.spawn.loot.chance.config", FileConfiguration::isList, listOf<Map<String, Any>>(),
        { value -> value.all { map -> map.keyNotNull("table") { table -> LootTables.values().map { it.name }.contains(table.toString().uppercase())}
            map.keyNotNull("chance") { it.isNumber() } &&
                    map.keyNotNull("min-level") { it.isChildLevel() } &&
                    map.keyNotNull("max-level") { it.isChildLevel() } &&
                    map.keyNotNull("blacklisted-enchants") { enchants -> enchants is List<*> && enchants.all { it.isEnchantment() } } &&
                    map.keyNotNull("whitelisted-enchants") { enchants -> enchants is List<*> && enchants.all { it.isEnchantment() } }
        }}
    )

    .putSection("enchantments.spawn.fishing")
    .put("enchantments.spawn.fishing.blacklisted-enchants", FileConfiguration::isList, listOf<String>())
    .put("enchantments.spawn.fishing.whitelisted-enchants", FileConfiguration::isList, listOf<String>(),
        { value -> value.all { it.isEnchantment() } })
    .put("enchantments.spawn.fishing.min-level", FileConfiguration::isSet, "default",
        { value -> value.isChildLevel() }
    )
    .put("enchantments.spawn.fishing.max-level", FileConfiguration::isSet, "default",
        { value -> value.isChildLevel() }
    )
    .putSection("enchantments.spawn.fishing.chance")
    .put("enchantments.spawn.fishing.chance.global", isNumber, 0.04)
    .put("enchantments.spawn.fishing.chance.luck-of-the-sea-modifier", isNumber, 0.03)

    .putSection("enchantments.spawn.mining")
    .put("enchantments.spawn.mining.blacklisted-blocks", FileConfiguration::isList, listOf<String>(),
        { value -> value.all { Material.matchMaterial(it) != null } },
        { old -> old.filter { Material.matchMaterial(it) != null } }
    )
    .put("enchantments.spawn.mining.whitelisted-blocks", FileConfiguration::isList, listOf<String>(),
        { value -> value.all { Material.matchMaterial(it) != null } },
        { old -> old.filter { Material.matchMaterial(it) != null } }
    )
    .put("enchantments.spawn.mining.min-level", FileConfiguration::isSet, "default",
        { value -> value.isChildLevel() }
    )
    .put("enchantments.spawn.mining.max-level", FileConfiguration::isSet, "default",
        { value -> value.isChildLevel() }
    )
    .put("enchantments.spawn.mining.ignore-silk-touch", FileConfiguration::isBoolean, true)
    .putSection("enchantments.spawn.mining.chance")
    .put("enchantments.spawn.mining.chance.global", isNumber, 0.005)
    .put("enchantments.spawn.mining.chance.fortune-modifier", isNumber, 0.01)
    .put("enchantments.spawn.mining.chance.config", FileConfiguration::isList, listOf<Map<String, Any>>(),
        { value -> value.all { map -> map.keyNotNull("block") { block -> Material.matchMaterial(block.toString().uppercase()) != null } &&
                map.keyNotNull("chance") { it.isNumber() } &&
                map.keyNotNull("min-level") { it.isChildLevel() } &&
                map.keyNotNull("max-level") { it.isChildLevel() } &&
                map.keyNotNull("blacklisted-enchants") { enchants -> enchants is List<*> && enchants.all { it.isEnchantment() } } &&
                map.keyNotNull("whitelisted-enchants") { enchants -> enchants is List<*> && enchants.all { it.isEnchantment() } }
        }}
    )


    .putSection("enchantments.trades")
    .put("enchantments.trades.include-wandering-traders", FileConfiguration::isBoolean, true)
    .put("enchantments.trades.professions", FileConfiguration::isList, listOf<String>(),
        { value -> value.all { profession -> Villager.Profession.values().map { it.name.lowercase() }.contains(profession.lowercase()) } },
        { old -> old.filter { profession -> Villager.Profession.values().map { it.name.lowercase() }.contains(profession.lowercase()) } }
    )
    .put("enchantments.trades.blacklisted-types", FileConfiguration::isList, listOf<String>(),
        { value -> value.all { type -> Villager.Type.values().map { it.name.lowercase() }.contains(type.lowercase()) } },
        { old -> old.filter { type -> Villager.Type.values().map { it.name.lowercase() }.contains(type.lowercase()) } }
    )
    .put("enchantments.trades.whitelisted-types", FileConfiguration::isList, listOf<String>(),
        { value -> value.all { type -> Villager.Type.values().map { it.name.lowercase() }.contains(type.lowercase()) } },
        { old -> old.filter { type -> Villager.Type.values().map { it.name.lowercase() }.contains(type.lowercase()) } }
    )
    .put("enchantments.trades.min-villager-level", FileConfiguration::isInt, 1)
    .put("enchantments.trades.max-villager-level", FileConfiguration::isInt, 5)
    .put("enchantments.trades.buy-books", FileConfiguration::isBoolean, false)
    .putSection("enchantments.trades.emerald-price")
    .put("enchantments.trades.emerald-price.normal", FileConfiguration::isInt, 15)
    .put("enchantments.trades.emerald-price.attacking", FileConfiguration::isSet, "default",
        { value -> value.isChildLevel() }
    )
    .put("enchantments.trades.emerald-price.defending", FileConfiguration::isSet, "default",
        { value -> value.isChildLevel() }
    )
    .put("enchantments.trades.emerald-price.mining", FileConfiguration::isSet, "default",
        { value -> value.isChildLevel() }
    )
    .put("enchantments.trades.emerald-price.passive", FileConfiguration::isSet, "default",
        { value -> value.isChildLevel() }
    )
    .put("enchantments.trades.emerald-price.ranged", FileConfiguration::isSet, "default",
        { value -> value.isChildLevel() }
    )
    .put("enchantments.trades.emerald-price.collector", FileConfiguration::isSet, "default",
        { value -> value.isChildLevel() }
    )

    .putSection("enchantments.trades.enchants")
    .put("enchantments.trades.enchants.blacklisted-enchants", FileConfiguration::isList, listOf<String>(),
        { value -> value.all { it.isEnchantment() } },
        { old -> old.filter { it.isEnchantment() } }
    )
    .put("enchantments.trades.enchants.whitelisted-enchants", FileConfiguration::isList, listOf<String>(),
        { value -> value.all { it.isEnchantment() } },
        { old -> old.filter { it.isEnchantment() } }
    )
    .put("enchantments.trades.enchants.min-level", FileConfiguration::isInt, 1)
    .put("enchantments.trades.enchants.max-level", FileConfiguration::isInt, 5)
    .putSection("enchantments.trades.enchants.replace")
    .put("enchantments.trades.enchants.replace.only-enchantment-books", FileConfiguration::isBoolean, false)
    .put("enchantments.trades.enchants.replace.chance", isNumber, 0.25)
    .put("enchantments.trades.enchants.replace.config", FileConfiguration::isSet, listOf<Map<String, Any>>(),
        { value -> value.all { map -> map.keyNotNull("bukkit") { enchant -> Enchantment.values().map { it.key.key.lowercase() }.contains(enchant.toString().lowercase()) } &&
                map.keyNotNull("plasma") { it.isEnchantment() } &&
                map.keyNotNull("min-bukkit-level") { it.isChildLevel() } &&
                map.keyNotNull("max-bukkit-level") { it.isChildLevel() } && map.keyNotNull("min-plasma-level") { it.isChildLevel() } &&
                map.keyNotNull("max-plasma-level") { it.isChildLevel() }
        }}
    )

    // Artifact Configuration
    .putSection("artifacts")
    .put("artifacts.disabled-artifacts", FileConfiguration::isList, listOf<String>(),
        { value -> value.all { it.isArtifact() }},
        { old -> old.filter { it.isArtifact() }}
    )

    .putSection("artifacts.spawn")
    .put("artifacts.spawn.luck-modifier", isNumber, 0.05)

    .putSection("artifacts.spawn.killing")
    .put("artifacts.spawn.killing.looting-modifier", isNumber, 0.01)
    .put("artifacts.spawn.killing.global-chance", isNumber, 0.05)

    .putSection("artifacts.spawn.loot")
    .put("artifacts.spawn.loot.global-chance", isNumber, 0.25)

    .putSection("artifacts.spawn.fishing")
    .put("artifacts.spawn.fishing.luck-of-the-sea-modifier", isNumber, 0.01)
    .put("artifacts.spawn.fishing.global-chance", isNumber, 0.07)

    .putSection("artifacts.spawn.mining")
    .put("artifacts.spawn.mining.fortune-modifier", isNumber, 0.02)
    .put("artifacts.spawn.mining.global-chance", isNumber, 0.03)

    .putSection("artifacts.trades")
    .put("artifacts.trades.include-wandering-traders", FileConfiguration::isBoolean, true)
    .put("artifacts.trades.professions", FileConfiguration::isList, listOf<String>(),
        { value -> value.all { profession -> Villager.Profession.values().map { it.name.lowercase() }.contains(profession.lowercase()) } },
        { old -> old.filter { profession -> Villager.Profession.values().map { it.name.lowercase() }.contains(profession.lowercase()) } }
    )
    .put("artifacts.trades.min-villager-level", FileConfiguration::isInt, 1)
    .put("artifacts.trades.max-villager-level", FileConfiguration::isInt, 5)
    .put("artifacts.trades.max-uses", FileConfiguration::isInt, 10)
    .put("artifacts.trades.chance", isNumber, 0.25)

    .putSection("artifacts.trades.buy-artifacts")
    .put("artifacts.trades.buy-artifacts.enabled", FileConfiguration::isBoolean, true)
    .put("artifacts.trades.buy-artifacts.price", FileConfiguration::isInt, 7)

    .putSection("artifacts.trades.craftable-artifacts")
    .put("artifacts.trades.craftable-artifacts.enabled", FileConfiguration::isBoolean, true)
    .put("artifacts.trades.craftable-artifacts.professions", FileConfiguration::isList, listOf<String>(),
        { value -> value.all { profession -> Villager.Profession.values().map { it.name.lowercase() }.contains(profession.lowercase()) } },
        { old -> old.filter { profession -> Villager.Profession.values().map { it.name.lowercase() }.contains(profession.lowercase()) } }
    )
    .put("artifacts.trades.craftable-artifacts.blacklisted-artifacts", FileConfiguration::isList, listOf<String>(),
        { value -> value.all { it.isArtifact() } },
        { old -> old.filter { it.isArtifact() } }
    )
    .put("artifacts.trades.craftable-artifacts.whitelisted-artifacts", FileConfiguration::isList, listOf<String>(),
        { value -> value.all { it.isArtifact() } },
        { old -> old.filter { it.isArtifact() } }
    )
    .putSection("artifacts.trades.craftable-artifacts.chance")
    .put("artifacts.trades.craftable-artifacts.chance.global", FileConfiguration::isSet, "default",
        { value -> value.isChildLevel() },
    )

    .build()


internal data class ConfigData(
    val checker: (FileConfiguration, String) -> Boolean,
    val default: Any?,
    val validator: (Any?) -> Boolean = { true },
    val remapper: (Any?) -> Any? = { it },
    val section: Boolean = false,
)

private fun FileConfiguration.isNumber(path: String): Boolean =
    this.isInt(path) || this.isDouble(path) || this.isLong(path)

private fun Any.isChildLevel() = toString() == "default" || toString().toIntOrNull() != null
private fun Any.isNumber() = toString().toDoubleOrNull() != null
private fun Any?.isEnchantment(): Boolean {
    if (this == null) return false
    return PlasmaConfig.registry.enchantments.map { it.key.key }.contains(toString().lowercase())
}
private fun Any?.isArtifact(): Boolean {
    if (this == null) return false
    return PlasmaConfig.registry.artifacts.map { it.key.key }.contains(toString().lowercase())
}

private inline fun <K, reified CV> ImmutableMap.Builder<K, ConfigData>.put(
    key: K,
    noinline checker: (FileConfiguration, String) -> Boolean,
    default: CV? = null,
    crossinline validator: (CV) -> Boolean = { true },
    crossinline remapper: (CV) -> CV? = { it },
): ImmutableMap.Builder<K, ConfigData> = put(key, ConfigData(
    checker, default,
    { if (it is CV) validator(it) else false },
    { if (it is CV) remapper(it) else it },
    false)
)

private fun <T> ImmutableMap.Builder<T, ConfigData>.putSection(key: T) =
    put(key, ConfigData(FileConfiguration::isConfigurationSection, null, { true }, { it }, true))

private fun <K, V> Map<K, V>.key(key: K, predicate: (V?) -> Boolean): Boolean = predicate(get(key))

private fun <K, V> Map<K, V>.keyNotNull(key: K, predicate: (V) -> Boolean): Boolean {
    return predicate(get(key) ?: return false)
}