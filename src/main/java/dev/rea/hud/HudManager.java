package dev.rea.hud;

import dev.rea.ReaPlugin;
import dev.rea.data.PlayerData;
import dev.rea.util.Msg;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

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
        PlayerData data = plugin.getDataManager().get(player);
        int max = plugin.getDataManager().maxMagicules(data);
        String line = Msg.c("&bMagicules &f" + data.getMagicules() + "/" + max
                + " &8| &eLevel &f" + data.getLevel());
        player.sendActionBar(LegacyComponentSerializer.legacySection().deserialize(line));
    }
}
