package dev.rea.scoreboard;

import dev.rea.ReaPlugin;
import dev.rea.data.PlayerData;
import dev.rea.quest.QuestDef;
import dev.rea.race.RaceType;
import dev.rea.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The sidebar every wanderer sees: race, form, runes, Codex, quest.
 * Scoreboards render on Bedrock too.
 */
public final class ScoreboardManager {
    private final ReaPlugin plugin;
    private final Map<UUID, Scoreboard> boards = new ConcurrentHashMap<>();

    public ScoreboardManager(ReaPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                update(player);
            }
        }, 40L, 40L);
    }

    public void stop() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
        boards.clear();
    }

    public void update(Player player) {
        PlayerData data = plugin.getDataManager().get(player);
        Scoreboard board = boards.computeIfAbsent(player.getUniqueId(),
                uuid -> Bukkit.getScoreboardManager().getNewScoreboard());
        Objective objective = board.getObjective("rea");
        if (objective == null) {
            objective = board.registerNewObjective("rea", "dummy", Msg.c("&d&lRea"));
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        }
        for (String entry : board.getEntries()) {
            board.resetScores(entry);
        }
        RaceType race = RaceType.fromId(data.getRace());
        String raceLine = race == null ? "&7Sleeping" : race.getColor() + race.formName(data.getForm());
        int score = 10;
        setLine(objective, "&fRace: " + raceLine, score--);
        setLine(objective, "&fRank: &d" + plugin.getDataManager().rank(data) + " &7EP: &d" + data.getEp(), score--);
        setLine(objective, "&fMagicules: &d" + data.getMagicules() + "/" + plugin.getDataManager().maxMagicules(data), score--);
        setLine(objective, "&fLevel: &d" + data.getLevel() + " &7(" + data.getXp() + "/" + plugin.getDataManager().xpForLevel(data.getLevel()) + " XP)", score--);
        setLine(objective, "&fRunes: &d" + data.getRunes(), score--);
        setLine(objective, "&fCodex: &d" + data.getCodex().size() + "/" + plugin.getCodexManager().total(), score--);
        QuestDef current = plugin.getQuestManager().currentQuest(data);
        if (current != null) {
            int progress = plugin.getQuestManager().progress(data, current);
            setLine(objective, "&fQuest: &d" + current.name() + " &7(" + Math.min(progress, current.amount()) + "/" + current.amount() + ")", score--);
        }
        player.setScoreboard(board);
    }

    private void setLine(Objective objective, String text, int score) {
        objective.getScore(Msg.c(text)).setScore(score);
    }
}
