package dev.rea.data;

import dev.rea.ReaPlugin;
import dev.rea.race.RaceType;
import dev.rea.util.Msg;
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
 * Loads, saves, and mutates PlayerData. Runes and XP live here so every
 * system (quests, codex, daily...) grants them the same way.
 * Humans gain +10% XP from everything (their bloodline gift).
 */
public final class DataManager {
    private final ReaPlugin plugin;
    private final Map<UUID, PlayerData> cache = new ConcurrentHashMap<>();
    private final File playersDir;

    public DataManager(ReaPlugin plugin) {
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
        config.set("race", data.getRace());
        config.set("level", data.getLevel());
        config.set("xp", data.getXp());
        config.set("runes", data.getRunes());
        config.set("lifetime", data.getLifetimeEarned());
        config.set("codex", data.getCodex().stream().sorted().toList());
        config.set("quests", data.getQuestsDone().stream().sorted().toList());
        config.set("kills", data.getKills());
        config.set("mines", data.getMines());
        config.set("crafts", data.getCrafts());
        config.set("streak", data.getDailyStreak());
        config.set("lastDaily", data.getLastDaily());
        for (Map.Entry<String, Integer> entry : data.getCounters().entrySet()) {
            config.set("counters." + entry.getKey(), entry.getValue());
        }
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
        data.setRace(config.getString("race", null));
        data.setLevel(config.getInt("level", 1));
        data.setXp(config.getInt("xp", 0));
        data.setRunes(config.getLong("runes", 0L));
        data.setLifetimeEarned(config.getLong("lifetime", 0L));
        data.getCodex().addAll(config.getStringList("codex"));
        data.getQuestsDone().addAll(config.getStringList("quests"));
        data.setKills(config.getInt("kills", 0));
        data.setMines(config.getInt("mines", 0));
        data.setCrafts(config.getInt("crafts", 0));
        data.setDailyStreak(config.getInt("streak", 0));
        data.setLastDaily(config.getString("lastDaily", ""));
        ConfigurationSection counters = config.getConfigurationSection("counters");
        if (counters != null) {
            for (String key : counters.getKeys(false)) {
                data.getCounters().put(key, counters.getInt(key, 0));
            }
        }
        return data;
    }

    public void delete(UUID uuid) {
        cache.remove(uuid);
        File file = new File(playersDir, uuid + ".yml");
        if (file.exists()) {
            file.delete();
        }
    }

    // ---- Runes ----

    public void addRunes(Player player, long amount) {
        if (amount <= 0) {
            return;
        }
        PlayerData data = get(player);
        data.setRunes(data.getRunes() + amount);
        data.setLifetimeEarned(data.getLifetimeEarned() + amount);
        Msg.send(player, "&d+" + amount + " Runes &7(" + data.getRunes() + " total)");
        plugin.getQuestManager().checkCompletion(player);
    }

    public boolean takeRunes(Player player, long amount) {
        PlayerData data = get(player);
        if (data.getRunes() < amount) {
            Msg.send(player, "&cYou need " + amount + " Runes. You carry " + data.getRunes() + ".");
            return false;
        }
        data.setRunes(data.getRunes() - amount);
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
        PlayerData data = get(player);
        if (RaceType.HUMAN.getId().equals(data.getRace())) {
            amount = (int) Math.round(amount * 1.1);
        }
        int cap = plugin.getConfig().getInt("settings.level-cap", 20);
        data.setXp(data.getXp() + amount);
        while (data.getLevel() < cap && data.getXp() >= xpForLevel(data.getLevel())) {
            data.setXp(data.getXp() - xpForLevel(data.getLevel()));
            data.setLevel(data.getLevel() + 1);
            int level = data.getLevel();
            long bonus = 10L * level;
            data.setRunes(data.getRunes() + bonus);
            data.setLifetimeEarned(data.getLifetimeEarned() + bonus);
            Msg.send(player, Msg.bar());
            Msg.send(player, "&d&lLevel up! &7You are now Level &d" + level);
            Msg.send(player, "&7The blood sings a little louder. &d+" + bonus + " runes");
            Msg.send(player, Msg.bar());
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1f);
            plugin.getQuestManager().checkCompletion(player);
        }
    }
}
