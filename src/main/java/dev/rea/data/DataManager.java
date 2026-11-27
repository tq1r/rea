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
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loads, saves, and mutates PlayerData. Runes and XP live here so every
 * system (quests, codex, runes) grants them the same way.
 * Humans gain +10% XP from everything (their racial gift).
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

    /** Magicules regenerate over time (Magic Sense makes them flow faster). */
    public void startMagiculeRegen() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                PlayerData data = get(player);
                int max = maxMagicules(data);
                if (data.getMagicules() >= max) {
                    continue; // Full — skip the write entirely.
                }
                int regen = data.getSkills().contains("magic_sense") ? 3 : 2;
                data.setMagicules(Math.min(max, data.getMagicules() + regen));
            }
        }, 20L, 20L);
    }

    public int maxMagicules(PlayerData data) {
        long max = 100 + data.getEp() / 10 + data.getForm() * 100L;
        return (int) Math.min(1500, max);
    }

    /** Gain Existence Points. Predator and Truth make them flow faster. */
    public void addEp(Player player, long amount) {
        if (amount <= 0) {
            return;
        }
        PlayerData data = get(player);
        double multiplied = amount;
        if (data.getSkills().contains("predator")) {
            multiplied *= 1.25;
        }
        if (data.getSkills().contains("truth")) {
            multiplied *= 1.25;
        }
        if (plugin.getWeaponManager() != null
                && plugin.getWeaponManager().isCardinal(player.getInventory().getItemInMainHand())) {
            multiplied *= plugin.getConfig().getDouble("settings.cardinal-ep-bonus", 1.10);
        }
        long gained = Math.max(1, Math.round(multiplied));
        data.setEp(data.getEp() + gained);
        plugin.getQuestManager().checkCompletion(player);
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
        config.set("form", data.getForm());
        config.set("ep", data.getEp());
        config.set("magicules", data.getMagicules());
        config.set("skills", data.getSkills().stream().sorted().toList());
        config.set("uniqueSkill", data.getUniqueSkillId());
        for (Map.Entry<String, Long> entry : data.getSkillCooldowns().entrySet()) {
            config.set("cooldowns." + entry.getKey(), entry.getValue());
        }
        config.set("level", data.getLevel());
        config.set("xp", data.getXp());
        config.set("runes", data.getRunes());
        config.set("lifetime", data.getLifetimeEarned());
        config.set("codex", data.getCodex().stream().sorted().toList());
        config.set("quests", data.getQuestsDone().stream().sorted().toList());
        config.set("kills", data.getKills());
        config.set("mines", data.getMines());
        config.set("crafts", data.getCrafts());
        for (Map.Entry<String, Integer> entry : data.getCounters().entrySet()) {
            config.set("counters." + entry.getKey(), entry.getValue());
        }
        // Atomic write: save to a temp file, then move it over the real one.
        // A crash mid-save can never leave a half-written player file behind.
        File tmp = new File(playersDir, data.getUuid() + ".yml.tmp");
        try {
            config.save(tmp);
            try {
                Files.move(tmp.toPath(), file.toPath(),
                        StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException atomicUnsupported) {
                Files.move(tmp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            tmp.delete();
            plugin.getLogger().warning("Could not save data for " + data.getName() + ": " + e.getMessage());
        }
    }

    /**
     * One-time safety net for updates: the first time Rea boots on a new
     * version, copy plugins/Rea/data/players aside before anything else can
     * touch it. Keeps the last few backups so a bad update never eats a life.
     */
    public void backupIfVersionChanged() {
        String current;
        try {
            current = plugin.getPluginMeta().getVersion();
        } catch (Exception e) {
            current = "unknown";
        }
        File dataDir = plugin.getDataFolder();
        File marker = new File(dataDir, "data/version.txt");
        try {
            String last = marker.exists() ? Files.readString(marker.toPath()).trim() : null;
            if (current.equals(last)) {
                return; // Same version as last boot — nothing to do.
            }
            File players = new File(dataDir, "data/players");
            if (players.isDirectory()) {
                File backups = new File(dataDir, "data/backups");
                String stamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
                File dest = new File(backups, current + "-" + stamp);
                copyTree(players.toPath(), dest.toPath());
                pruneBackups(backups, 3);
                plugin.getLogger().info("Data backup written to " + dest.getPath()
                        + " (version changed " + (last == null ? "(first run)" : last) + " -> " + current + ")");
            }
            marker.getParentFile().mkdirs();
            Files.writeString(marker.toPath(), current + System.lineSeparator());
        } catch (Exception e) {
            // Never let a backup failure break startup.
            plugin.getLogger().warning("Data backup skipped: " + e.getMessage());
        }
    }

    private void copyTree(java.nio.file.Path from, java.nio.file.Path to) throws IOException {
        Files.walk(from).forEach(source -> {
            try {
                java.nio.file.Path target = to.resolve(from.relativize(source).toString());
                if (Files.isDirectory(source)) {
                    Files.createDirectories(target);
                } else {
                    Files.createDirectories(target.getParent());
                    Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void pruneBackups(File backups, int keep) {
        File[] dirs = backups.listFiles(File::isDirectory);
        if (dirs == null || dirs.length <= keep) {
            return;
        }
        List<File> sorted = java.util.Arrays.stream(dirs)
                .sorted(Comparator.comparing(File::getName))
                .toList();
        for (int i = 0; i < sorted.size() - keep; i++) {
            deleteTree(sorted.get(i));
        }
    }

    private void deleteTree(File root) {
        File[] children = root.listFiles();
        if (children != null) {
            for (File child : children) {
                deleteTree(child);
            }
        }
        root.delete();
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
        data.setForm(config.getInt("form", 0));
        data.setEp(config.getLong("ep", 0L));
        data.setMagicules(config.getInt("magicules", 100));
        data.getSkills().addAll(config.getStringList("skills"));
        data.setUniqueSkillId(config.getString("uniqueSkill", null));
        ConfigurationSection cooldowns = config.getConfigurationSection("cooldowns");
        if (cooldowns != null) {
            for (String key : cooldowns.getKeys(false)) {
                data.getSkillCooldowns().put(key, cooldowns.getLong(key, 0L));
            }
        }
        data.setLevel(config.getInt("level", 1));
        data.setXp(config.getInt("xp", 0));
        data.setRunes(config.getLong("runes", 0L));
        data.setLifetimeEarned(config.getLong("lifetime", 0L));
        data.getCodex().addAll(config.getStringList("codex"));
        data.getQuestsDone().addAll(config.getStringList("quests"));
        data.setKills(config.getInt("kills", 0));
        data.setMines(config.getInt("mines", 0));
        data.setCrafts(config.getInt("crafts", 0));
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
        if (data.getSkills().contains("great_sage")) {
            amount = (int) Math.round(amount * 1.25);
        }
        if (data.getSkills().contains("truth")) {
            amount = (int) Math.round(amount * 1.5);
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
            Msg.send(player, "&d&lLevel up! &7You are now Level &d" + level + " &7(+" + bonus + " runes)");
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1f);
            plugin.getQuestManager().checkCompletion(player);
        }
    }
}
