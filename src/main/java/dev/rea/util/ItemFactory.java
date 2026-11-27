package dev.rea.util;

import dev.rea.ReaPlugin;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

/**
 * Builds the pretty (and tagged) items players actually hold: rune caches.
 */
public final class ItemFactory {
    private final ReaPlugin plugin;

    public ItemFactory(ReaPlugin plugin) {
        this.plugin = plugin;
    }

    public ItemStack named(Material material, String displayName, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Msg.c(displayName));
            meta.setLore(lore.stream().map(Msg::c).toList());
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack runeCache(int amount) {
        ItemStack item = named(Material.AMETHYST_SHARD, "&dRune Cache",
                List.of("&7A fistful of humming runes.", "&7Right-click to attune: &d+"
                        + plugin.getConfig().getInt("settings.cache-xp", 40) + " XP"));
        item.setAmount(amount);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(plugin.getKeys().cache(), PersistentDataType.STRING, "1");
            item.setItemMeta(meta);
        }
        return item;
    }

    public boolean isRuneCache(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.getPersistentDataContainer().has(plugin.getKeys().cache(), PersistentDataType.STRING);
    }
}
