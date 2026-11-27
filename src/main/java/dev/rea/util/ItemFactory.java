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

    /** A Skill Orb bound to one player: "skillId:ownerUuid" in the PDC. */
    public ItemStack skillOrb(String skillId, String skillName, String tierLabel, java.util.UUID owner) {
        ItemStack item = named(Material.PRISMARINE_CRYSTALS, "&b" + skillName,
                List.of("&7" + tierLabel + " Skill Orb",
                        "&7Bound to your soul. Hold it out & use it —",
                        "&fRight-click &7(Java) or &fhold and press your screen &7(Bedrock)"));
        item.setAmount(1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(plugin.getKeys().skillOrb(),
                    PersistentDataType.STRING, skillId + ":" + owner);
            item.setItemMeta(meta);
        }
        return item;
    }

    private String orbData(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        return meta.getPersistentDataContainer().get(plugin.getKeys().skillOrb(), PersistentDataType.STRING);
    }

    public boolean isSkillOrb(ItemStack item) {
        return orbData(item) != null;
    }

    /** Skill id inside the orb, or null if the item is not an orb. */
    public String skillOrbSkill(ItemStack item) {
        String data = orbData(item);
        if (data == null) {
            return null;
        }
        int split = data.indexOf(':');
        return split > 0 ? data.substring(0, split) : null;
    }

    /** Owner of the orb, or null if the item is not an orb. */
    public java.util.UUID skillOrbOwner(ItemStack item) {
        String data = orbData(item);
        if (data == null) {
            return null;
        }
        int split = data.indexOf(':');
        if (split <= 0 || split == data.length() - 1) {
            return null;
        }
        try {
            return java.util.UUID.fromString(data.substring(split + 1));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public boolean isRuneCache(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.getPersistentDataContainer().has(plugin.getKeys().cache(), PersistentDataType.STRING);
    }
}
