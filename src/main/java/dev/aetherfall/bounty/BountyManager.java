package dev.aetherfall.bounty;

import dev.aetherfall.AetherfallPlugin;
import dev.aetherfall.data.PlayerData;
import dev.aetherfall.util.Msg;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Three daily contracts for the Vale's wanderers. The same three bounties
 * apply to everyone each day (seeded by date), so friends can work together.
 */
public final class BountyManager {
    private static final String[] MOBS = {"ZOMBIE", "SKELETON", "SPIDER", "CREEPER", "DROWNED", "HUSK"};
    private static final String[] ORES = {"DIAMOND_ORE", "IRON_ORE", "GOLD_ORE", "COAL_ORE", "REDSTONE_ORE", "LAPIS_ORE"};

    private final AetherfallPlugin plugin;
    private final File file;
    private final List<Bounty> bounties = new ArrayList<>();
    private String date = "";

    public BountyManager(AetherfallPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "data/bounties.yml");
        load();
    }

    private void load() {
        String today = LocalDate.now().toString();
        if (file.exists()) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            date = config.getString("date", "");
            if (today.equals(date)) {
                for (int i = 0; i < 3; i++) {
                    String prefix = "bounties." + i + ".";
                    bounties.add(new Bounty(
                            config.getString(prefix + "type", "KILL"),
                            config.getString(prefix + "target", "ANY"),
                            config.getInt(prefix + "amount", 10),
                            config.getLong(prefix + "reward", 40L)));
                }
                return;
            }
        }
        generate(today);
    }

    private void generate(String today) {
        date = today;
        bounties.clear();
        Random random = new Random(today.hashCode());
        bounties.add(new Bounty("KILL", MOBS[random.nextInt(MOBS.length)], 10 + random.nextInt(11), 40L + random.nextInt(21)));
        bounties.add(new Bounty("MINE", ORES[random.nextInt(ORES.length)], 16 + random.nextInt(17), 50L + random.nextInt(21)));
        bounties.add(new Bounty("CRAFT", "ANY", 8 + random.nextInt(9), 30L + random.nextInt(16)));
        save();
    }

    private void save() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("date", date);
        for (int i = 0; i < bounties.size(); i++) {
            Bounty bounty = bounties.get(i);
            config.set("bounties." + i + ".type", bounty.type());
            config.set("bounties." + i + ".target", bounty.target());
            config.set("bounties." + i + ".amount", bounty.amount());
            config.set("bounties." + i + ".reward", bounty.reward());
        }
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save bounties: " + e.getMessage());
        }
    }

    public List<Bounty> bounties() {
        return bounties;
    }

    public String date() {
        return date;
    }

    /** Reset a player's bounty progress if the day has rolled over. */
    public void rollover(Player player) {
        PlayerData data = plugin.getDataManager().get(player);
        if (!date.equals(data.getBountyDate())) {
            data.setBountyDate(date);
            data.getBountyProgress().clear();
            data.getBountyClaimed().clear();
            plugin.getDataManager().save(data);
        }
    }

    /**
     * Feed a progress event (e.g. "kill:ZOMBIE") into today's bounties.
     * Bounties with target ANY match every event of their type.
     */
    public void onProgress(Player player, String counterKey) {
        rollover(player);
        PlayerData data = plugin.getDataManager().get(player);
        String type = counterKey.split(":", 2)[0].toUpperCase();
        String target = counterKey.split(":", 2)[1];
        for (int i = 0; i < bounties.size(); i++) {
            Bounty bounty = bounties.get(i);
            if (!bounty.type().equals(type)) {
                continue;
            }
            if (!bounty.target().equals("ANY") && !bounty.target().equals(target)) {
                continue;
            }
            String key = "b" + i;
            data.getBountyProgress().put(key, data.getBountyProgress().getOrDefault(key, 0) + 1);
            checkCompletion(player, i, bounty);
        }
    }

    private void checkCompletion(Player player, int index, Bounty bounty) {
        PlayerData data = plugin.getDataManager().get(player);
        String key = "b" + index;
        if (data.getBountyClaimed().contains(key)) {
            return;
        }
        if (data.getBountyProgress().getOrDefault(key, 0) < bounty.amount()) {
            return;
        }
        data.getBountyClaimed().add(key);
        data.incrementCounter("bounty", 1);
        Msg.send(player, Msg.bar());
        Msg.send(player, "&d&lBounty complete: &l" + bounty.describe());
        plugin.getDataManager().addShards(player, bounty.reward());
        plugin.getDataManager().addXp(player, 20);
        Msg.send(player, Msg.bar());
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1f);
        plugin.getTitleManager().checkAll(player);
        plugin.getDataManager().save(data);
    }

    public void list(Player player) {
        rollover(player);
        PlayerData data = plugin.getDataManager().get(player);
        Msg.send(player, Msg.bar());
        Msg.send(player, "&d&lToday's Bounties &7(" + date + ")");
        for (int i = 0; i < bounties.size(); i++) {
            Bounty bounty = bounties.get(i);
            int progress = data.getBountyProgress().getOrDefault("b" + i, 0);
            boolean claimed = data.getBountyClaimed().contains("b" + i);
            String status = claimed ? "&aDone" : "&e" + Math.min(progress, bounty.amount()) + "/" + bounty.amount();
            Msg.send(player, "&7- &f" + bounty.describe() + " &7(" + status + ") &d" + bounty.reward() + " shards");
        }
        Msg.send(player, "&7New bounties each day. The Vale always needs hands.");
        Msg.send(player, Msg.bar());
    }

    public record Bounty(String type, String target, int amount, long reward) {
        public String describe() {
            return switch (type) {
                case "KILL" -> "Slay " + amount + " " + target.toLowerCase().replace('_', ' ');
                case "MINE" -> "Mine " + amount + " " + target.toLowerCase().replace('_', ' ');
                case "CRAFT" -> "Craft " + amount + " items";
                default -> type;
            };
        }
    }
}
