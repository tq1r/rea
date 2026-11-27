package dev.rea.command;

import dev.rea.ReaPlugin;
import dev.rea.data.PlayerData;
import dev.rea.race.RaceType;
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
 * The skills menu — skills by tier, cast on click, evolve button.
 * Top row: your race and evolution. Middle: your skills, click to cast.
 * Everything is a standard chest GUI — Bedrock-safe.
 */
public final class SkillsGui {

    private final ReaPlugin plugin;
    private final NamespacedKey skillKey;
    private final NamespacedKey evolveKey;

    public SkillsGui(ReaPlugin plugin) {
        this.plugin = plugin;
        this.skillKey = new NamespacedKey(plugin, "skill");
        this.evolveKey = new NamespacedKey(plugin, "evolve");
    }

    public NamespacedKey skillKey() {
        return skillKey;
    }

    public NamespacedKey evolveKey() {
        return evolveKey;
    }

    public void open(Player player) {
        PlayerData data = plugin.getDataManager().get(player);
        RaceType race = RaceType.fromId(data.getRace());
        Inventory inv = Bukkit.createInventory(null, 54, Msg.c("&8Your Skills"));

        // Slot 45: evolution.
        int form = data.getForm();
        if (race != null && form < 3) {
            int next = form + 1;
            ItemStack evolve = new ItemStack(Material.BEACON);
            ItemMeta meta = evolve.getItemMeta();
            meta.setDisplayName(Msg.c("&b&lEvolve: " + race.getForms()[next]));
            List<String> lore = new ArrayList<>();
            lore.add(Msg.c("&8Current form: &7" + race.getForms()[form]));
            lore.add(Msg.c("&7Requirements:"));
            lore.add(Msg.c("&7- &d" + plugin.getRaceManager().evolveEp(next) + " EP &7(you have " + data.getEp() + ")"));
            lore.add(Msg.c("&7- &dLevel " + plugin.getRaceManager().evolveLevel(next) + " &7(you are " + data.getLevel() + ")"));
            lore.add(Msg.c("&7- &d" + plugin.getRaceManager().evolveRunes(next) + " runes &7(you have " + data.getRunes() + ")"));
            if (plugin.getRaceManager().canEvolve(data)) {
                lore.add(Msg.c("&eClick to awaken the next form!"));
            } else {
                lore.add(Msg.c("&7Requirements not yet met."));
            }
            meta.setLore(lore);
            evolve.setItemMeta(meta);
            evolve.editMeta(m -> m.getPersistentDataContainer().set(evolveKey, PersistentDataType.BYTE, (byte) 1));
            inv.setItem(45, evolve);
        } else if (race != null) {
            ItemStack max = new ItemStack(Material.NETHER_STAR);
            ItemMeta meta = max.getItemMeta();
            meta.setDisplayName(Msg.c("&d&lDemon Lord"));
            meta.setLore(List.of(Msg.c("&7You stand at the summit. Nothing above you.")));
            max.setItemMeta(meta);
            inv.setItem(45, max);
        }

        // Slot 53: race overview.
        if (race != null) {
            ItemStack card = new ItemStack(Material.BOOK);
            ItemMeta meta = card.getItemMeta();
            meta.setDisplayName(Msg.c(race.getColor() + "&l" + race.formName(data.getForm())));
            List<String> lore = new ArrayList<>();
            lore.add(Msg.c("&8" + race.getDisplayName()));
            lore.add(Msg.c("&7Rank: &d" + plugin.getDataManager().rank(data) + " &7EP: &d" + data.getEp()));
            lore.add(Msg.c("&7Magicules: &b" + data.getMagicules() + "/" + plugin.getDataManager().maxMagicules(data)));
            lore.add(Msg.c("&7Level: &d" + data.getLevel() + " &7(" + data.getXp() + "/" + plugin.getDataManager().xpForLevel(data.getLevel()) + " XP)"));
            lore.add(Msg.c("&7Runes: &d" + data.getRunes()));
            meta.setLore(lore);
            card.setItemMeta(meta);
            inv.setItem(53, card);
        }

        // Skills by tier order.
        int slot = 0;
        for (String id : data.getSkills()) {
            SkillDef def = plugin.getSkillManager().getSkill(id);
            if (def == null) {
                continue;
            }
            if (slot >= 45) {
                break; // 45 skill slots is more than anyone can hold
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
                lore.add(Msg.c("&eClick to cast"));
            } else {
                lore.add(Msg.c("&7Passive — always with you."));
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
            item.editMeta(m -> m.getPersistentDataContainer().set(skillKey, PersistentDataType.STRING, def.getId()));
            inv.setItem(slot++, item);
        }
        player.openInventory(inv);
    }
}
