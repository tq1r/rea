package dev.aetherfall.scoreboard;

import dev.aetherfall.AetherfallPlugin;
import dev.aetherfall.data.PlayerData;
import dev.aetherfall.event.EventType;
import dev.aetherfall.path.PathType;
import dev.aetherfall.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The sidebar every Shardbearer sees: title, Path, level, shards, Codex,
 * Order, and the current world event. Scoreboards render on Bedrock too.
 */
public final class ScoreboardManager {
    private final AetherfallPlugin plugin;
    private final Map<UUID, Scoreboard> boards = new ConcurrentHashMap<>();

    public ScoreboardManager(AetherfallPlugin plugin) {
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
        Objective objective = board.getObjective("aetherfall");
        if (objective == null) {
            objective = board.registerNewObjective("aetherfall", "dummy", Msg.c("&d&lAetherfall"));
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        }
        // Clear previous lines.
        for (String entry : board.getEntries()) {
            board.resetScores(entry);
        }
        PathType path = PathType.fromId(data.getPath());
        String pathLine = path == null ? "&7None yet" : path.getColor() + path.getDisplayName();
        int score = 10;
        setLine(objective, "&fTitle: &d" + plugin.getTitleManager().selectedDisplay(data), score--);
        setLine(objective, "&fPath: " + pathLine, score--);
        setLine(objective, "&fLevel: &d" + data.getLevel() + " &7(" + data.getXp() + "/" + plugin.getDataManager().xpForLevel(data.getLevel()) + " XP)", score--);
        setLine(objective, "&fShards: &d" + data.getShards(), score--);
        setLine(objective, "&fCodex: &d" + data.getCodex().size() + "/" + plugin.getCodexManager().total(), score--);
        setLine(objective, "&fOrder: &d" + (data.getOrder() == null ? "None" : data.getOrder()), score--);
        EventType event = plugin.getEventManager().getActive();
        if (event != null) {
            setLine(objective, "&fEvent: " + event.getColor() + event.getDisplayName(), score--);
        }
        player.setScoreboard(board);
    }

    private void setLine(Objective objective, String text, int score) {
        objective.getScore(Msg.c(text)).setScore(score);
    }
}
