package dev.aetherfall.relic;

import dev.aetherfall.AetherfallPlugin;
import dev.aetherfall.data.PlayerData;
import dev.aetherfall.util.Msg;
import dev.aetherfall.util.Util;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Creates relic items and handles their active effects (staff, crown fragment).
 * Passive combat effects are applied in GameListener, which asks getRelicId().
 */
public final class RelicManager {
    private final AetherfallPlugin plugin;
    private final Map<UUID, Long> staffCooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, Long> crownCooldowns = new ConcurrentHashMap<>();

    public RelicManager(AetherfallPlugin plugin) {
        this.plugin = plugin;
    }

    public ItemStack createItem(RelicType type) {
        ItemStack item = new ItemStack(type.getMaterial());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Msg.c("&d" + type.getDisplayName()));
            meta.setLore(type.getLore().stream().map(Msg::c).toList());
            meta.getPersistentDataContainer().set(plugin.getKeys().relic(), PersistentDataType.STRING, type.getId());
            item.setItemMeta(meta);
        }
        try {
            switch (type) {
                case EMBERHEART -> {
                    item.addEnchantment(Enchantment.SHARPNESS, 3);
                    item.addEnchantment(Enchantment.FIRE_ASPECT, 2);
                    item.addEnchantment(Enchantment.UNBREAKING, 3);
                }
                case TIDECALLER -> {
                    item.addEnchantment(Enchantment.LOYALTY, 3);
                    item.addEnchantment(Enchantment.UNBREAKING, 3);
                }
                case WARDEN_EDGE -> {
                    item.addEnchantment(Enchantment.SHARPNESS, 4);
                    item.addEnchantment(Enchantment.UNBREAKING, 3);
                }
                case VERDANT_STAFF, CROWN_FRAGMENT -> item.addEnchantment(Enchantment.UNBREAKING, 3);
            }
        } catch (IllegalArgumentException ignored) {
            // Enchantment does not apply to this item type on this version; the relic still works.
        }
        return item;
    }

    public String getRelicId(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        return meta.getPersistentDataContainer().get(plugin.getKeys().relic(), PersistentDataType.STRING);
    }

    public void giveRelic(Player player, RelicType type) {
        Util.giveItem(player, createItem(type));
        PlayerData data = plugin.getDataManager().get(player);
        data.incrementCounter("relic", 1);
        Msg.send(player, "&dYou received the &l" + type.getDisplayName() + "&d!");
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1f);
        plugin.getTitleManager().checkAll(player);
        plugin.getDataManager().save(data);
    }

    public void useVerdantStaff(Player player) {
        long now = System.currentTimeMillis();
        Long last = staffCooldowns.get(player.getUniqueId());
        if (last != null && now - last < 30_000L) {
            Msg.send(player, "&cThe staff is resting. &7(" + ((30_000L - (now - last)) / 1000) + "s)");
            return;
        }
        staffCooldowns.put(player.getUniqueId(), now);
        int grown = Util.fertilize(player.getLocation(), 5);
        player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, player.getLocation(), 40, 0.8, 1.0, 0.8);
        player.playSound(player.getLocation(), Sound.BLOCK_GRASS_BREAK, 1f, 1f);
        Msg.send(player, "&aThe forest answers. &7" + grown + " crops flourished.");
    }

    public void useCrown(Player player) {
        long now = System.currentTimeMillis();
        Long last = crownCooldowns.get(player.getUniqueId());
        if (last != null && now - last < 600_000L) {
            Msg.send(player, "&cThe fragment is quiet. &7(" + ((600_000L - (now - last)) / 1000 / 60) + " min)");
            return;
        }
        crownCooldowns.put(player.getUniqueId(), now);
        plugin.getDataManager().addXp(player, 40);
        player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation(), 40, 0.6, 1.0, 0.6);
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1f);
        Msg.send(player, "&dThe fragment sings. &7+40 XP");
    }
}
