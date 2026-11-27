package dev.rea.hud;

import dev.rea.ReaPlugin;
import dev.rea.data.PlayerData;
import dev.rea.util.Msg;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.UUID;

/**
 * The HUD: magicules and level on the action bar, once per second.
 * Bedrock-safe (action bars render on MCPE via Geyser), near-zero packets.
 */
public final class HudManager {
    private final ReaPlugin plugin;

    public HudManager(ReaPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                update(player);
            }
        }, 20L, 20L);
    }

    public void stop() {
        // The action bar fades on its own; nothing to restore.
    }

    /** Kept for API symmetry; no per-player state is cached anymore. */
    public void remove(UUID uniqueId) {
    }

    public void update(Player player) {
        // Legacy scrub: Rea <= 1.0.0 could leave a per-player sidebar behind
        // ('rea' objective on a private board). If one exists, hand the player
        // back the server main scoreboard — read-only check, zero packets when
        // nothing is wrong.
        ScoreboardManager boards = Bukkit.getScoreboardManager();
        Scoreboard main = boards != null ? boards.getMainScoreboard() : null;
        Scoreboard current = player.getScoreboard();
        if (main != null && current != null && current != main && current.getObjective("rea") != null) {
            player.setScoreboard(main);
        }
        PlayerData data = plugin.getDataManager().get(player);
        int max = plugin.getDataManager().maxMagicules(data);
        String line = Msg.c("&bMagicules &f" + data.getMagicules() + "/" + max
                + " &8| &eLevel &f" + data.getLevel());
        player.sendActionBar(LegacyComponentSerializer.legacySection().deserialize(line));
    }
}
