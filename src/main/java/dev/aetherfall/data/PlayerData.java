package dev.aetherfall.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Everything Aetherfall remembers about one Shardbearer. Stored as YAML
 * in plugins/Aetherfall/data/players/&lt;uuid&gt;.yml by DataManager.
 */
public final class PlayerData {
    private final UUID uuid;
    private String name;
    private String path; // null until the player chooses a Path
    private int level = 1;
    private int xp = 0;
    private long shards = 0;
    private long lifetimeEarned = 0;
    private final Set<String> codex = new HashSet<>();
    private final Set<String> questsDone = new HashSet<>();
    private final Set<String> titles = new HashSet<>();
    private String selectedTitle;
    private final Set<String> ruins = new HashSet<>();
    private int kills = 0;
    private int mines = 0;
    private int crafts = 0;
    private int dailyStreak = 0;
    private String lastDaily = "";
    private String order;
    private final Map<String, Integer> counters = new HashMap<>();
    private String bountyDate = "";
    private final Map<String, Integer> bountyProgress = new HashMap<>();
    private final Set<String> bountyClaimed = new HashSet<>();

    public PlayerData(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public long getShards() {
        return shards;
    }

    public void setShards(long shards) {
        this.shards = shards;
    }

    public long getLifetimeEarned() {
        return lifetimeEarned;
    }

    public void setLifetimeEarned(long lifetimeEarned) {
        this.lifetimeEarned = lifetimeEarned;
    }

    public Set<String> getCodex() {
        return codex;
    }

    public Set<String> getQuestsDone() {
        return questsDone;
    }

    public Set<String> getTitles() {
        return titles;
    }

    public String getSelectedTitle() {
        return selectedTitle;
    }

    public void setSelectedTitle(String selectedTitle) {
        this.selectedTitle = selectedTitle;
    }

    public Set<String> getRuins() {
        return ruins;
    }

    public int getKills() {
        return kills;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }

    public int getMines() {
        return mines;
    }

    public void setMines(int mines) {
        this.mines = mines;
    }

    public int getCrafts() {
        return crafts;
    }

    public void setCrafts(int crafts) {
        this.crafts = crafts;
    }

    public int getDailyStreak() {
        return dailyStreak;
    }

    public void setDailyStreak(int dailyStreak) {
        this.dailyStreak = dailyStreak;
    }

    public String getLastDaily() {
        return lastDaily;
    }

    public void setLastDaily(String lastDaily) {
        this.lastDaily = lastDaily;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public Map<String, Integer> getCounters() {
        return counters;
    }

    public int counter(String key) {
        return counters.getOrDefault(key, 0);
    }

    public void incrementCounter(String key, int amount) {
        counters.put(key, counter(key) + amount);
    }

    public String getBountyDate() {
        return bountyDate;
    }

    public void setBountyDate(String bountyDate) {
        this.bountyDate = bountyDate;
    }

    public Map<String, Integer> getBountyProgress() {
        return bountyProgress;
    }

    public Set<String> getBountyClaimed() {
        return bountyClaimed;
    }
}
