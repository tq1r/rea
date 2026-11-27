package dev.aetherfall.util;

import dev.aetherfall.AetherfallPlugin;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

/**
 * Builds the pretty (and tagged) items players actually hold: shard caches,
 * GUI buttons, and generic named items.
 */
public final class ItemFactory {
    private final AetherfallPlugin plugin;

    public ItemFactory(AetherfallPlugin plugin) {
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

    public ItemStack shardCache(int amount) {
        ItemStack item = named(Material.AMETHYST_SHARD, "&dShard Cache",
                List.of("&7A fistful of humming shards.", "&7Right-click to spend them: &d+"
                        + plugin.getConfig().getInt("settings.cache-shards", 25) + " shards"));
        item.setAmount(amount);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(plugin.getKeys().cache(), PersistentDataType.STRING, "1");
            item.setItemMeta(meta);
        }
        return item;
    }

    public boolean isShardCache(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.getPersistentDataContainer().has(plugin.getKeys().cache(), PersistentDataType.STRING);
    }

    public ItemStack guiButton(Material material, String displayName, List<String> lore, String action, String arg) {
        ItemStack item = named(material, displayName, lore);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(plugin.getKeys().guiAction(), PersistentDataType.STRING, action);
            if (arg != null) {
                meta.getPersistentDataContainer().set(plugin.getKeys().guiArg(), PersistentDataType.STRING, arg);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    public String guiAction(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        return meta.getPersistentDataContainer().get(plugin.getKeys().guiAction(), PersistentDataType.STRING);
    }

    public String guiArg(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        return meta.getPersistentDataContainer().get(plugin.getKeys().guiArg(), PersistentDataType.STRING);
    }
}
