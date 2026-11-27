package dev.rea.command;

import dev.rea.ReaPlugin;
import dev.rea.data.PlayerData;
import dev.rea.race.RaceManager;
import dev.rea.race.RaceType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

/**
 * Evolution GUI (Geyser-safe): shows the race's evolution chain and lets the
 * player evolve when requirements are met.
 */
public final class EvolveGui {

    private final ReaPlugin plugin;
    private final RaceManager races;

    public EvolveGui(ReaPlugin plugin, RaceManager races) {
        this.plugin = plugin;
        this.races = races;
    }

    public void open(Player player) {
        PlayerData data = plugin.getDataManager().get(player);
        RaceType race = RaceType.fromId(data.getRaceId());
        if (race == null) {
            return;
        }
        Inventory inv = Bukkit.createInventory(null, 27, plugin.getConfigManager().color("&8Evolution"));
        String[] forms = race.getForms();
        for (int i = 0; i < forms.length; i++) {
            ItemStack item = new ItemStack(i == data.getForm() ? Material.BEACON : Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(plugin.getConfigManager().color(
                    (i == data.getForm() ? "&b" : "&7") + forms[i]));
            List<String> lore = new ArrayList<>();
            if (i == data.getForm()) {
                lore.add(plugin.getConfigManager().color("&8Current form"));
            } else if (i == data.getForm() + 1) {
                lore.add(plugin.getConfigManager().color("&7Next form"));
                lore.add(plugin.getConfigManager().color("&8Requirements:"));
                switch (i - 1) {
                    case 0 -> lore.add(plugin.getConfigManager().color("&7- 100 EP, level 5, 1 rune"));
                    case 1 -> lore.add(plugin.getConfigManager().color("&7- 500 EP, level 10, 3 runes"));
                    case 2 -> lore.add(plugin.getConfigManager().color("&7- 2,000 EP, level 20, 6 runes"));
                    default -> { }
                }
                lore.add(plugin.getConfigManager().color("&eClick to evolve"));
            } else {
                lore.add(plugin.getConfigManager().color("&8Locked"));
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
            if (i == data.getForm() + 1) {
                item.editMeta(m -> m.getPersistentDataContainer().set(
                        new NamespacedKey(plugin, "evolve"), PersistentDataType.BYTE, (byte) 1));
            }
            inv.setItem(i * 3 + 2, item);
        }
        player.openInventory(inv);
    }
}
