package dev.rea.listener;

import dev.rea.ReaPlugin;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Feeds dungeon events: boss kills end a run, and leaving the server abandons one.
 */
public final class DungeonListener implements Listener {
    private final ReaPlugin plugin;

    public DungeonListener(ReaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }
        Player killer = entity.getKiller();
        if (killer == null) {
            return;
        }
        // Dungeon mobs drop nothing: rewards come from clearing the ruin.
        var run = plugin.getDungeonManager().runOf(killer);
        if (run != null && run.summoned().contains(entity.getUniqueId())) {
            event.getDrops().clear();
            event.setDroppedExp(0);
        }
        plugin.getDungeonManager().onBossDefeated(killer, entity.getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getDungeonManager().onPlayerQuit(event.getPlayer());
    }
}
