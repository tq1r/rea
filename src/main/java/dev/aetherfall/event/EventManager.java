package dev.aetherfall.event;

import dev.aetherfall.AetherfallPlugin;
import dev.aetherfall.codex.CodexEntry;
import dev.aetherfall.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Random;

/**
 * Rotating world events. Announced with chat + boss bar + sound, so they
 * work the same for Java and Bedrock players. Effects are read by
 * GameListener (shard chances, mob damage) and DataManager (XP multiplier).
 */
public final class EventManager {
    private final AetherfallPlugin plugin;
    private final Random random = new Random();

    private EventType active;
    private long endsAtMillis;
    private long durationMillis;
    private BossBar bossBar;
    private int meteorTaskId = -1;

    public EventManager(AetherfallPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        long intervalTicks = plugin.getConfig().getLong("settings.event-interval-minutes", 10L) * 1200L;
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (active == null) {
                startEvent(EventType.values()[random.nextInt(EventType.values().length)]);
            }
        }, intervalTicks, intervalTicks);
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::tick, 20L, 20L);
    }

    public void stop() {
        stopEvent(false);
    }

    private void tick() {
        if (active == null) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now >= endsAtMillis) {
            stopEvent(true);
            return;
        }
        if (bossBar != null) {
            double progress = durationMillis <= 0 ? 0 : Math.max(0.0, Math.min(1.0, (endsAtMillis - now) / (double) durationMillis));
            bossBar.setProgress(progress);
        }
        if (active == EventType.VEIL && random.nextInt(30) == 0) {
            List<String> whispers = plugin.getConfig().getStringList("messages.veil-whispers");
            if (!whispers.isEmpty()) {
                Bukkit.broadcastMessage(Msg.c(whispers.get(random.nextInt(whispers.size()))));
            }
        }
    }

    public void startEvent(EventType type) {
        stopEvent(false);
        active = type;
        durationMillis = plugin.getConfig().getLong("settings.event-duration-minutes", 5L) * 60_000L;
        endsAtMillis = System.currentTimeMillis() + durationMillis;
        bossBar = Bukkit.createBossBar(Msg.c(type.getColor() + "&l" + type.getDisplayName()), BarColor.PURPLE, BarStyle.SOLID);
        for (Player player : Bukkit.getOnlinePlayers()) {
            bossBar.addPlayer(player);
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1f);
            plugin.getDataManager().get(player).incrementCounter("event", 1);
        }
        List<String> lines = plugin.getConfig().getStringList("messages.event-start." + type.getId());
        if (lines.isEmpty()) {
            lines = List.of(type.getColor() + "&l" + type.getDisplayName());
        }
        for (String line : lines) {
            Bukkit.broadcastMessage(Msg.c(line));
        }
        if (type == EventType.METEOR) {
            spawnMeteor();
        }
        plugin.getLogger().info("World event started: " + type.getDisplayName());
    }

    public void stopEvent(boolean announce) {
        if (active == null) {
            return;
        }
        if (announce) {
            Bukkit.broadcastMessage(Msg.c("&7The " + active.getDisplayName() + " passes. The Vale settles."));
        }
        if (bossBar != null) {
            bossBar.removeAll();
            bossBar = null;
        }
        if (meteorTaskId != -1) {
            Bukkit.getScheduler().cancelTask(meteorTaskId);
            meteorTaskId = -1;
        }
        active = null;
    }

    public void addToBossBar(Player player) {
        if (bossBar != null && active != null) {
            bossBar.addPlayer(player);
        }
    }

    public EventType getActive() {
        return active;
    }

    public boolean isActive(EventType type) {
        return active == type;
    }

    public int xpMultiplier() {
        return active == EventType.VEIL ? plugin.getConfig().getInt("settings.veil-xp-multiplier", 2) : 1;
    }

    public double shardChanceMultiplier() {
        return active == EventType.SHARD_STORM ? 3.0 : 1.0;
    }

    private void spawnMeteor() {
        World world = Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().get(0);
        if (world == null) {
            return;
        }
        Location spawn = world.getSpawnLocation();
        int x = spawn.getBlockX() + random.nextInt(601) - 300;
        int z = spawn.getBlockZ() + random.nextInt(601) - 300;
        int y = world.getHighestBlockYAt(x, z);
        Location loc = new Location(world, x, y, z);
        loc.getBlock().setType(Material.CHEST);
        if (loc.getBlock().getState() instanceof org.bukkit.block.Chest chest) {
            chest.getInventory().addItem(plugin.getItemFactory().shardCache(2));
            List<CodexEntry> entries = plugin.getCodexManager().allEntries().stream().toList();
            if (!entries.isEmpty()) {
                chest.getInventory().addItem(plugin.getCodexManager().createBook(entries.get(random.nextInt(entries.size()))));
            }
            chest.getInventory().addItem(new ItemStack(Material.EXPERIENCE_BOTTLE, 8));
            chest.update();
        }
        Bukkit.broadcastMessage(Msg.c("&6A meteor fell at &e" + x + ", " + z + "&6. Be first. Take what it carries."));
        meteorTaskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (active != EventType.METEOR) {
                return;
            }
            world.spawnParticle(Particle.END_ROD, loc.clone().add(0.5, 1, 0.5), 5, 0.4, 2.0, 0.4);
        }, 20L, 20L).getTaskId();
    }
}
