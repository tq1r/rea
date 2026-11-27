package dev.aetherfall.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockData;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * Small world/inventory helpers shared by several managers.
 */
public final class Util {
    private Util() {
    }

    /** Give an item, dropping whatever does not fit at the player's feet. */
    public static void giveItem(Player player, ItemStack item) {
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(item);
        for (ItemStack rest : leftover.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), rest);
        }
        player.updateInventory();
    }

    /** Fertilize crops in a square radius around the location. Returns how many grew. */
    public static int fertilize(Location center, int radius) {
        int grown = 0;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                Block block = center.getBlock().getRelative(x, 0, z);
                BlockData data = block.getBlockData();
                if (data instanceof Ageable ageable && ageable.getAge() < ageable.getMaximumAge()) {
                    ageable.setAge(ageable.getMaximumAge());
                    block.setBlockData(data);
                    grown++;
                }
            }
        }
        return grown;
    }

    public static boolean isOre(Material type) {
        return type.name().endsWith("_ORE") || type == Material.ANCIENT_DEBRIS;
    }
}
