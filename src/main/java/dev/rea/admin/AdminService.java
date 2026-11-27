package dev.rea.admin;

import dev.rea.ReaPlugin;
import dev.rea.util.Msg;
import org.bukkit.entity.Player;

/**
 * Shared admin operations used by both the /rea commands and the
 * /rea admin GUI, so both paths behave identically and completely.
 */
public final class AdminService {

    private AdminService() {
    }

    /**
     * Total data wipe for one player: dungeon run abandoned, Rea data deleted,
     * Rea racials cleared, HUD refreshed. The next /rea start deals a
     * completely fresh reincarnation.
     */
    public static void resetPlayer(ReaPlugin plugin, Player target) {
        if (plugin.getDungeonManager().inDungeon(target)) {
            plugin.getDungeonManager().endRun(target, false);
        }
        plugin.getDataManager().delete(target.getUniqueId());
        // Race is gone now, so applyPassives wipes Rea's racials instead of re-adding them.
        plugin.getRaceManager().applyPassives(target);
        plugin.getHudManager().update(target);
        Msg.send(target, "&cAn admin wiped your Rea data. Your next reincarnation starts from nothing.");
    }

    /** Reset every online player (admin GUI danger zone — confirmation required). */
    public static int resetAllOnline(ReaPlugin plugin) {
        int count = 0;
        for (Player online : plugin.getServer().getOnlinePlayers()) {
            resetPlayer(plugin, online);
            count++;
        }
        return count;
    }
}
