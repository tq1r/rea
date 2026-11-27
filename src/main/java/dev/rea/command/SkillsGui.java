package dev.rea.command;

import dev.rea.ReaPlugin;
import dev.rea.data.PlayerData;
import dev.rea.skill.SkillDef;
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
 * Skills GUI (Geyser-safe): shows the player's skills by tier and lets them
 * cast active skills by clicking.
 */
public final class SkillsGui {

    private final ReaPlugin plugin;

    public SkillsGui(ReaPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        PlayerData data = plugin.getDataManager().get(player);
        Inventory inv = Bukkit.createInventory(null, 54, Msg.c("&8Skills"));
        int slot = 0;
        for (String id : data.getSkills()) {
            SkillDef def = plugin.getSkillManager().getSkill(id);
            if (def == null || slot >= 54) {
                continue;
            }
            Material mat = switch (def.getTier()) {
                case INTRINSIC -> Material.SLIME_BALL;
                case COMMON -> Material.WATER_BUCKET;
                case UNIQUE -> Material.NETHER_STAR;
                case ULTIMATE -> Material.BEACON;
            };
            ItemStack item = new ItemStack(mat);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(Msg.c("&f" + def.getName()));
            List<String> lore = new ArrayList<>();
            lore.add(Msg.c("&8Tier: &7" + def.getTier()));
            lore.add(Msg.c("&7" + def.getDescription()));
            if (def.getMagiculeCost() > 0) {
                lore.add(Msg.c("&8Cost: &b" + (int) def.getMagiculeCost() + " magicules"));
            }
            lore.add(Msg.c("&eClick to cast"));
            meta.setLore(lore);
            item.setItemMeta(meta);
            item.editMeta(m -> m.getPersistentDataContainer().set(
                    new NamespacedKey(plugin, "skill"), PersistentDataType.STRING, def.getId()));
            inv.setItem(slot++, item);
        }
        player.openInventory(inv);
    }
}
