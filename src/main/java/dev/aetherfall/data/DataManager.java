package dev.aetherfall.data;

import dev.aetherfall.AetherfallPlugin;
import dev.aetherfall.util.Msg;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loads, saves, and mutates PlayerData. Shards and XP live here so every
 * system (quests, codex, events, bounties...) grants them the same way.
 */
public final class DataManager {
    private final AetherfallPlugin plugin;
    private final Map<UUID, PlayerData> cache = new ConcurrentHashMap<>();
    private final File playersDir;

    public DataManager(AetherfallPlugin plugin) {
        this.plugin = plugin;
        this.playersDir = new File(plugin.getDataFolder(), "data/players");
        if (!playersDir.exists()) {
            playersDir.mkdirs();
        }
    }

    public PlayerData get(Player player) {
        return get(player.getUniqueId(), player.getName());
    }

    public PlayerData get(UUID uuid, String name) {
        PlayerData data = cache.get(uuid);
        if (data != null) {
            return data;
        }
        data = load(uuid, name);
        cache.put(uuid, data);
        return data;
    }

    public void startAutosave() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::saveAll, 6000L, 6000L);
    }

    public void saveAll() {
        for (PlayerData data : cache.values()) {
            save(data);
        }
    }

    public void save(PlayerData data) {
        File file = new File(playersDir, data.getUuid() + ".yml");
        YamlConfiguration config = new YamlConfiguration();
        config.set("name", data.getName());
        config.set("path", data.getPath());
        config.set("level", data.getLevel());
        config.set("xp", data.getXp());
        config.set("shards", data.getShards());
        config.set("lifetime", data.getLifetimeEarned());
        config.set("codex", data.getCodex().stream().sorted().toList());
        config.set("quests", data.getQuestsDone().stream().sorted().toList());
        config.set("titles", data.getTitles().stream().sorted().toList());
        config.set("selectedTitle", data.getSelectedTitle());
        config.set("ruins", data.getRuins().stream().sorted().toList());
        config.set("kills", data.getKills());
        config.set("mines", data.getMines());
        config.set("crafts", data.getCrafts());
        config.set("streak", data.getDailyStreak());
        config.set("lastDaily", data.getLastDaily());
        config.set("order", data.getOrder());
        for (Map.Entry<String, Integer> entry : data.getCounters().entrySet()) {
            config.set("counters." + entry.getKey(), entry.getValue());
        }
        config.set("bountyDate", data.getBountyDate());
        for (Map.Entry<String, Integer> entry : data.getBountyProgress().entrySet()) {
            config.set("bountyProgress." + entry.getKey(), entry.getValue());
        }
        config.set("bountyClaimed", data.getBountyClaimed().stream().sorted().toList());
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save data for " + data.getName() + ": " + e.getMessage());
        }
    }

    private PlayerData load(UUID uuid, String name) {
        File file = new File(playersDir, uuid + ".yml");
        PlayerData data = new PlayerData(uuid, name);
        if (!file.exists()) {
            return data;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        data.setName(config.getString("name", name));
        data.setPath(config.getString("path", null));
        data.setLevel(config.getInt("level", 1));
        data.setXp(config.getInt("xp", 0));
        data.setShards(config.getLong("shards", 0L));
        data.setLifetimeEarned(config.getLong("lifetime", 0L));
        data.getCodex().addAll(config.getStringList("codex"));
        data.getQuestsDone().addAll(config.getStringList("quests"));
        data.getTitles().addAll(config.getStringList("titles"));
        data.setSelectedTitle(config.getString("selectedTitle", null));
        data.getRuins().addAll(config.getStringList("ruins"));
        data.setKills(config.getInt("kills", 0));
        data.setMines(config.getInt("mines", 0));
        data.setCrafts(config.getInt("crafts", 0));
        data.setDailyStreak(config.getInt("streak", 0));
        data.setLastDaily(config.getString("lastDaily", ""));
        data.setOrder(config.getString("order", null));
        ConfigurationSection counters = config.getConfigurationSection("counters");
        if (counters != null) {
            for (String key : counters.getKeys(false)) {
                data.getCounters().put(key, counters.getInt(key, 0));
            }
        }
        data.setBountyDate(config.getString("bountyDate", ""));
        ConfigurationSection bounty = config.getConfigurationSection("bountyProgress");
        if (bounty != null) {
            for (String key : bounty.getKeys(false)) {
                data.getBountyProgress().put(key, bounty.getInt(key, 0));
            }
        }
        data.getBountyClaimed().addAll(config.getStringList("bountyClaimed"));
        return data;
    }

    public void delete(UUID uuid) {
        cache.remove(uuid);
        File file = new File(playersDir, uuid + ".yml");
        if (file.exists()) {
            file.delete();
        }
    }

    // ---- Shards ----

    public void addShards(Player player, long amount) {
        if (amount <= 0) {
            return;
        }
        PlayerData data = get(player);
        data.setShards(data.getShards() + amount);
        data.setLifetimeEarned(data.getLifetimeEarned() + amount);
        Msg.send(player, "&d+" + amount + " Aether Shards &7(" + data.getShards() + " total)");
        plugin.getQuestManager().checkCompletion(player);
    }

    public boolean takeShards(Player player, long amount) {
        PlayerData data = get(player);
        if (data.getShards() < amount) {
            Msg.send(player, "&cYou need " + amount + " Aether Shards. You carry " + data.getShards() + ".");
            return false;
        }
        data.setShards(data.getShards() - amount);
        return true;
    }

    // ---- XP & levels ----

    public int xpForLevel(int level) {
        return 100 + (level - 1) * 60;
    }

    public void addXp(Player player, int amount) {
        if (amount <= 0) {
            return;
        }
        int multiplied = amount * plugin.getEventManager().xpMultiplier();
        PlayerData data = get(player);
        int cap = plugin.getConfig().getInt("settings.level-cap", 20);
        data.setXp(data.getXp() + multiplied);
        while (data.getLevel() < cap && data.getXp() >= xpForLevel(data.getLevel())) {
            data.setXp(data.getXp() - xpForLevel(data.getLevel()));
            data.setLevel(data.getLevel() + 1);
            int level = data.getLevel();
            long bonus = 10L * level;
            data.setShards(data.getShards() + bonus);
            data.setLifetimeEarned(data.getLifetimeEarned() + bonus);
            Msg.send(player, Msg.bar());
            Msg.send(player, "&d&lLevel up! &7You are now Shardbearer Level &d" + level);
            Msg.send(player, "&7The shards sing a little louder. &d+" + bonus + " shards");
            Msg.send(player, Msg.bar());
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1f);
            plugin.getQuestManager().checkCompletion(player);
            plugin.getTitleManager().checkAll(player);
        }
    }
}
