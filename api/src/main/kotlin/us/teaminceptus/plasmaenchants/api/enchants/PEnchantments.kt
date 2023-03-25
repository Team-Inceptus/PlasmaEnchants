package us.teaminceptus.plasmaenchants.api.enchants

import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import us.teaminceptus.plasmaenchants.api.PlasmaConfig
import us.teaminceptus.plasmaenchants.api.enchants.PEnchantment.Target.SWORDS
import us.teaminceptus.plasmaenchants.api.enchants.PEnchantment.Target.MELEE_WEAPONS
import us.teaminceptus.plasmaenchants.api.enchants.PEnchantment.Type.ATTACKING

/**
 * Represents all of the Default PlasmaEnchants Enchantments.
 */
enum class PEnchantments(
    private val nameKey: String,
    private val type: PEnchantment.Type,
    private val target: PEnchantment.Target,
    private val maxLevel: Int = 1,
    private val action: (Player, Int) -> Unit,
    private val natural: Boolean = false,
    private val naturalRoll: (Player) -> Int = { 0 },
) : PEnchantment {

    // Melee Enchantments

    POISONING("enchantment.poisoning",
        ATTACKING, SWORDS, 2, { p, level ->
            p.addPotionEffect(PotionEffect(PotionEffectType.POISON, 20 * 5, level - 1))
        }),

    WITHERING("enchantment.withering",
        ATTACKING, MELEE_WEAPONS, 1, { p, _ ->
            p.addPotionEffect(PotionEffect(PotionEffectType.WITHER, 20 * 3, 0))
        })

    // Armor Enchantments

    ;

    constructor(
        nameKey: String,
        type: PEnchantment.Type,
        target: PEnchantment.Target,
        maxLevel: Int,
        action: (Player, Int) -> Unit,
        naturalRoll: (Player) -> Int,
    ) : this(nameKey, type, target, maxLevel, action, true, naturalRoll)

    override fun getName(): String = PlasmaConfig.getConfig()?.get(nameKey) ?: name.lowercase().replaceFirstChar { it.uppercase() }

    override fun getDescription(): String = PlasmaConfig.getConfig()?.get("$nameKey.description") ?: "No description provided."

    override fun isNatural(): Boolean = natural

    override fun rollTable(player: Player): Int = naturalRoll.invoke(player)

    override fun getType(): PEnchantment.Type = type

    override fun getTarget(): PEnchantment.Target = target

    override fun accept(player: Player, level: Int) {
        action.invoke(player, level)
    }

}