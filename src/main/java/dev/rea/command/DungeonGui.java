package dev.rea.command;

import dev.rea.ReaPlugin;
import dev.rea.dungeon.DungeonDef;
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
import java.util.Map;

/**
 * The dungeon picker (Tensura ruins), laid out as a chest — Bedrock-safe.
 * Click a ruin to dive into it.
 */
public final class DungeonGui {

    private final ReaPlugin plugin;
    private final NamespacedKey dungeonKey;

    public DungeonGui(ReaPlugin plugin) {
        this.plugin = plugin;
        this.dungeonKey = new NamespacedKey(plugin, "dungeon");
    }

    public NamespacedKey dungeonKey() {
        return dungeonKey;
    }

    public void open(Player player) {
        if (plugin.getDungeonManager().inDungeon(player)) {
            Msg.send(player, "&cYou are already inside a ruin. Use &e/rea dungeon leave&c to abandon it.");
            return;
        }
        Inventory inv = Bukkit.createInventory(null, 27, Msg.c("&8The Ruins"));
        int[] slots = {11, 13, 15};
        int index = 0;
        for (Map.Entry<String, DungeonDef> entry : plugin.getDungeonManager().all().entrySet()) {
            if (index >= slots.length) {
                break;
            }
            DungeonDef def = entry.getValue();
            Material icon = switch (def.tier()) {
                case 0 -> Material.ZOMBIE_SPAWN_EGG;
                case 1 -> Material.RAVAGER_SPAWN_EGG;
                default -> Material.WARDEN_SPAWN_EGG;
            };
            ItemStack item = new ItemStack(icon);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(Msg.c("&d" + def.name()));
            List<String> lore = new ArrayList<>();
            lore.add(Msg.c(stripColors(def.description())));
            lore.add(Msg.c("&7Rewards: &d" + def.epReward() + " EP &8| &d" + def.runeReward() + " runes &8| &d" + def.xpReward() + " XP"));
            lore.add(Msg.c("&7Cardinal weapon chance: &d" + (int) (def.weaponChance() * 100) + "%"));
            lore.add(Msg.c("&eClick to dive."));
            meta.setLore(lore);
            item.setItemMeta(meta);
            item.editMeta(m -> m.getPersistentDataContainer().set(dungeonKey, PersistentDataType.STRING, def.id()));
            inv.setItem(slots[index], item);
            index++;
        }
        player.openInventory(inv);
    }

    private String stripColors(String text) {
        return text == null ? "" : text.replaceAll("&.", "");
    }
}
