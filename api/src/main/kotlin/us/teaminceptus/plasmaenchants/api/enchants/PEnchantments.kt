package us.teaminceptus.plasmaenchants.api.enchants

import org.bukkit.entity.Player
import java.util.function.Consumer

/**
 * Represents all of the Default PlasmaEnchants Enchantments.
 */
enum class PEnchantments(private val action: Consumer<Player>) : PEnchantment {

    ;

    override fun accept(player: Player) {
        action.accept(player)
    }

}