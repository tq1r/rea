package dev.rea.command;

import dev.rea.ReaPlugin;
import dev.rea.data.PlayerData;
import dev.rea.race.RaceType;
import dev.rea.util.Msg;
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

    public EvolveGui(ReaPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        PlayerData data = plugin.getDataManager().get(player);
        RaceType race = RaceType.fromId(data.getRace());
        if (race == null) {
            return;
        }
        Inventory inv = Bukkit.createInventory(null, 27, Msg.c("&8Evolution"));
        String[] forms = race.getForms();
        for (int i = 0; i < forms.length; i++) {
            ItemStack item = new ItemStack(i == data.getForm() ? Material.BEACON : Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(Msg.c((i == data.getForm() ? "&b" : "&7") + forms[i]));
            List<String> lore = new ArrayList<>();
            if (i == data.getForm()) {
                lore.add(Msg.c("&8Current form"));
            } else if (i == data.getForm() + 1) {
                lore.add(Msg.c("&7Next form"));
                lore.add(Msg.c("&8Requirements:"));
                lore.add(Msg.c("&7- " + plugin.getRaceManager().evolveEp(i) + " EP, level "
                        + plugin.getRaceManager().evolveLevel(i) + ", "
                        + plugin.getRaceManager().evolveRunes(i) + " runes"));
                lore.add(Msg.c("&eClick to evolve"));
            } else {
                lore.add(Msg.c("&8Locked"));
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
