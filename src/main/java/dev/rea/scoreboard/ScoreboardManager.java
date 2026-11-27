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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The sidebar every wanderer sees: race, form, runes, Codex, quest.
 * Scoreboards render on Bedrock too.
 *
 * Anti-lag: the rendered lines are cached per player, and the board is only
 * touched when something actually changed — no per-second entry wipes, no
 * redundant packets.
 */
public final class ScoreboardManager {
    private final ReaPlugin plugin;
    private final Map<UUID, Scoreboard> boards = new ConcurrentHashMap<>();
    private final Map<UUID, List<String>> lastRender = new ConcurrentHashMap<>();

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
        lastRender.clear();
    }

    /** Drop a player's board (called on quit) so long sessions stay lean. */
    public void remove(UUID uniqueId) {
        boards.remove(uniqueId);
        lastRender.remove(uniqueId);
    }

    public void update(Player player) {
        PlayerData data = plugin.getDataManager().get(player);
        List<String> lines = render(data);
        UUID id = player.getUniqueId();
        if (lines.equals(lastRender.get(id))) {
            return; // Nothing changed — send no packets at all.
        }
        lastRender.put(id, lines);

        Scoreboard board = boards.computeIfAbsent(id,
                uuid -> Bukkit.getScoreboardManager().getNewScoreboard());
        Objective objective = board.getObjective("rea");
        if (objective == null) {
            objective = board.registerNewObjective("rea", "dummy", Msg.c("&d&lRea"));
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        }
        for (String entry : board.getEntries()) {
            board.resetScores(entry);
        }
        int score = lines.size();
        for (String line : lines) {
            objective.getScore(Msg.c(line)).setScore(score);
            score--;
        }
        player.setScoreboard(board);
    }

    private List<String> render(PlayerData data) {
        List<String> lines = new ArrayList<>(8);
        RaceType race = RaceType.fromId(data.getRace());
        String raceLine = race == null ? "&7Sleeping" : race.getColor() + race.formName(data.getForm());
        lines.add("&fRace: " + raceLine);
        lines.add("&fRank: &d" + plugin.getDataManager().rank(data) + " &7EP: &d" + data.getEp());
        lines.add("&fMagicules: &d" + data.getMagicules() + "/" + plugin.getDataManager().maxMagicules(data));
        lines.add("&fLevel: &d" + data.getLevel() + " &7(" + data.getXp() + "/" + plugin.getDataManager().xpForLevel(data.getLevel()) + " XP)");
        lines.add("&fRunes: &d" + data.getRunes());
        lines.add("&fCodex: &d" + data.getCodex().size() + "/" + plugin.getCodexManager().total());
        QuestDef current = plugin.getQuestManager().currentQuest(data);
        if (current != null) {
            int progress = plugin.getQuestManager().progress(data, current);
            lines.add("&fQuest: &d" + current.name() + " &7(" + Math.min(progress, current.amount()) + "/" + current.amount() + ")");
        }
        return lines;
    }
}
