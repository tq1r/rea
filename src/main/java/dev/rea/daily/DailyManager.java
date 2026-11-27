package dev.rea.daily;

import dev.rea.ReaPlugin;
import dev.rea.codex.CodexEntry;
import dev.rea.data.PlayerData;
import dev.rea.util.Msg;
import dev.rea.util.Util;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.time.LocalDate;

/**
 * Daily gifts, claimed automatically on join. Rimuru's federation remembers
 * those who return — nobody needs to run a command for it.
 */
public final class DailyManager {
    private final ReaPlugin plugin;

    public DailyManager(ReaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Claim today's gift if the player is eligible. Returns true when a gift
     * was given. Silent when already claimed (safe to call on every join).
     */
    public boolean claimOnJoin(Player player) {
        if (!plugin.getConfig().getBoolean("settings.daily-auto-claim", true)) {
            return false;
        }
        PlayerData data = plugin.getDataManager().get(player);
        String today = LocalDate.now().toString();
        String yesterday = LocalDate.now().minusDays(1).toString();
        if (today.equals(data.getLastDaily())) {
            return false; // already claimed today — say nothing
        }
        int streak = yesterday.equals(data.getLastDaily()) ? data.getDailyStreak() + 1 : 1;
        data.setDailyStreak(streak);
        data.setLastDaily(today);
        Msg.send(player, Msg.bar());
        Msg.send(player, "&d&lDay " + streak + " &7- the magicules stir in greeting.");
        switch ((streak - 1) % 7) {
            case 0 -> plugin.getDataManager().addRunes(player, 20L);
            case 1 -> {
                plugin.getDataManager().addRunes(player, 30L);
                Util.giveItem(player, new ItemStack(Material.BREAD, 4));
            }
            case 2 -> Util.giveItem(player, plugin.getItemFactory().runeCache(1));
            case 3 -> plugin.getDataManager().addRunes(player, 50L);
            case 4 -> {
                plugin.getDataManager().addRunes(player, 60L);
                Util.giveItem(player, new ItemStack(Material.GOLDEN_APPLE, 1));
            }
            case 5 -> Util.giveItem(player, plugin.getItemFactory().runeCache(2));
            case 6 -> {
                plugin.getDataManager().addRunes(player, 100L);
                for (CodexEntry entry : plugin.getCodexManager().allEntries()) {
                    if (!data.getCodex().contains(entry.id())) {
                        plugin.getCodexManager().givePage(player, entry.id());
                        break;
                    }
                }
            }
            default -> {
            }
        }
        Msg.send(player, "&7Return tomorrow. The Federation rewards those who return.");
        Msg.send(player, Msg.bar());
        plugin.getDataManager().save(data);
        return true;
    }
}
