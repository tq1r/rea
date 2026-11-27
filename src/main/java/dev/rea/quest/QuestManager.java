package dev.rea.quest;

import dev.rea.ReaPlugin;
import dev.rea.data.PlayerData;
import dev.rea.util.Msg;
import dev.rea.util.Util;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Loads quests.yml and tracks the story. Quests are sequential: only the
 * first incomplete quest counts, so Rea's story unfolds in order.
 * Progress is derived from PlayerData counters, so it survives restarts.
 */
public final class QuestManager {
    private final ReaPlugin plugin;
    private final List<QuestDef> quests = new ArrayList<>();

    public QuestManager(ReaPlugin plugin) {
        this.plugin = plugin;
        load();
    }

    private void load() {
        File file = new File(plugin.getDataFolder(), "quests.yml");
        if (!file.exists()) {
            plugin.saveResource("quests.yml", false);
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (Map<?, ?> map : config.getMapList("quests")) {
            quests.add(new QuestDef(
                    str(map, "id"),
                    num(map, "chapter"),
                    str(map, "name"),
                    str(map, "type").toUpperCase(),
                    str(map, "target"),
                    num(map, "amount"),
                    num(map, "reward-runes"),
                    num(map, "reward-xp"),
                    num(map, "reward-ep"),
                    str(map, "reward-item"),
                    str(map, "description"),
                    str(map, "complete")));
        }
        plugin.getLogger().info("Loaded " + quests.size() + " quests.");
    }

    public void reload() {
        quests.clear();
        load();
    }

    private static String str(Map<?, ?> map, String key) {
        Object value = map.get(key);
        return value == null ? "" : value.toString();
    }

    private static int num(Map<?, ?> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(str(map, key));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public List<QuestDef> allQuests() {
        return quests;
    }

    public QuestDef currentQuest(PlayerData data) {
        for (QuestDef quest : quests) {
            if (!data.getQuestsDone().contains(quest.id())) {
                return quest;
            }
        }
        return null;
    }

    public int progress(PlayerData data, QuestDef quest) {
        return switch (quest.type()) {
            case "RACE" -> data.getRace() != null ? 1 : 0;
            case "MINE" -> data.counter("mine:" + quest.target());
            case "KILL" -> data.counter("kill:" + quest.target());
            case "CRAFT" -> data.counter("craft:" + quest.target());
            case "CODEX" -> data.getCodex().size();
            case "REACH_LEVEL" -> data.getLevel();
            case "DUNGEON" -> data.counter("dungeon:clear");
            default -> 0;
        };
    }

    public void checkCompletion(Player player) {
        PlayerData data = plugin.getDataManager().get(player);
        QuestDef current = currentQuest(data);
        if (current == null) {
            return;
        }
        if (progress(data, current) < current.amount()) {
            return;
        }
        data.getQuestsDone().add(current.id());
        Msg.send(player, "&d&lQuest complete: &l" + current.name());
        if (!current.complete().isEmpty()) {
            Msg.send(player, "&7" + current.complete());
        }
        if (current.rewardRunes() > 0) {
            plugin.getDataManager().addRunes(player, current.rewardRunes());
        }
        if (current.rewardXp() > 0) {
            plugin.getDataManager().addXp(player, current.rewardXp());
        }
        if (current.rewardEp() > 0) {
            plugin.getDataManager().addEp(player, current.rewardEp());
        }
        if (current.id().equals("q1-4")) {
            plugin.getSkillManager().learn(player, "magic_sense");
        }
        grantItemReward(player, current.rewardItem());
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1f);
        plugin.getDataManager().save(data);
        // Completing one quest may complete the next (e.g. counters already satisfied).
        checkCompletion(player);
    }

    private void grantItemReward(Player player, String rewardItem) {
        if (rewardItem == null || rewardItem.isEmpty() || !rewardItem.contains(":")) {
            return;
        }
        String[] parts = rewardItem.split(":", 2);
        switch (parts[0].toLowerCase()) {
            case "codex" -> plugin.getCodexManager().givePage(player, parts[1]);
            case "cache" -> {
                int count = 1;
                try {
                    count = Integer.parseInt(parts[1]);
                } catch (NumberFormatException ignored) {
                }
                Util.giveItem(player, plugin.getItemFactory().runeCache(count));
            }
            default -> {
            }
        }
    }
}
