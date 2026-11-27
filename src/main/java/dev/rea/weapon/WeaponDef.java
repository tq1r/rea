package dev.rea.weapon;

import org.bukkit.Material;

import java.util.List;

/**
 * One cardinal weapon (a named Tensura weapon). Grade follows the series:
 * Unique < Legend < Mythical < Genesis. Stats are expressed through enchantments
 * so the weapons are plain items with canon names forged onto them.
 */
public record WeaponDef(String id, String name, String grade, Material material,
                        int sharpness, int fireAspect, int looting, List<String> lore) {
}
