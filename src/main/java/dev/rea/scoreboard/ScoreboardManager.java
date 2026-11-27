package dev.rea.scoreboard;

import dev.rea.ReaPlugin;
import dev.rea.data.PlayerData;
import dev.rea.race.RaceType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.List;

/**
 * Scoreboard display (Geyser-safe): race, form, EP, rank, magicules, runes.
 */
public final class ScoreboardManager {

    private final ReaPlugin plugin;

    public ScoreboardManager(ReaPlugin plugin) {
        this.plugin = plugin;
    }

    public void updateAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData data = plugin.getDataManager().get(player);
            if (data.getRaceId() == null) {
                continue;
            }
            RaceType race = RaceType.fromId(data.getRaceId());
            Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
            Objective objective = board.registerNewObjective("rea", Criteria.DUMMY, "Rea");
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
            List<String> lines = new ArrayList<>();
            lines.add(plugin.getConfigManager().color("&b&lRea"));
            lines.add(plugin.getConfigManager().color("&7Race: " + race.getColor() + race.formName(data.getForm())));
            lines.add(plugin.getConfigManager().color("&7EP: &f" + data.getEp()));
            lines.add(plugin.getConfigManager().color("&7Rank: &f" + rank(data.getEp())));
            lines.add(plugin.getConfigManager().color("&7Magicules: &f" + (int) data.getMagicules() + "/" + (int) data.getMaxMagicules()));
            lines.add(plugin.getConfigManager().color("&7Runes: &f" + data.getRunes()));
            for (int i = 0; i < lines.size(); i++) {
                objective.getScore(lines.get(i)).setScore(lines.size() - i);
            }
            player.setScoreboard(board);
        }
    }

    /** Canon EP rank thresholds (F, E, D, C, B, A, S, SS, SSS). */
    public String rank(int ep) {
        if (ep >= 100_000) {
            return "SSS";
        }
        if (ep >= 50_000) {
            return "SS";
        }
        if (ep >= 10_000) {
            return "S";
        }
        if (ep >= 5_000) {
            return "A";
        }
        if (ep >= 2_000) {
            return "B";
        }
        if (ep >= 1_000) {
            return "C";
        }
        if (ep >= 500) {
            return "D";
        }
        if (ep >= 100) {
            return "E";
        }
        return "F";
    }
}
