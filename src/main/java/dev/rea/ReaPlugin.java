package dev.rea;

import dev.rea.codex.CodexManager;
import dev.rea.command.ReaCommand;
import dev.rea.data.DataManager;
import dev.rea.listener.GameListener;
import dev.rea.quest.QuestManager;
import dev.rea.race.RaceManager;
import dev.rea.scoreboard.ScoreboardManager;
import dev.rea.skill.SkillManager;
import dev.rea.util.ItemFactory;
import dev.rea.util.Keys;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Rea — a fantasy bloodline SMP plugin for Paper 26.3 + Geyser.
 * The bloodlines wake. Who will you be?
 */
public final class ReaPlugin extends JavaPlugin {
    private Keys keys;
    private ItemFactory itemFactory;
    private DataManager dataManager;
    private RaceManager raceManager;
    private QuestManager questManager;
    private CodexManager codexManager;
    private SkillManager skillManager;
    private ScoreboardManager scoreboardManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        keys = new Keys(this);
        itemFactory = new ItemFactory(this);
        dataManager = new DataManager(this);
        raceManager = new RaceManager(this);
        questManager = new QuestManager(this);
        codexManager = new CodexManager(this);
        skillManager = new SkillManager(this);
        scoreboardManager = new ScoreboardManager(this);

        ReaCommand command = new ReaCommand(this);
        PluginCommand root = getCommand("rea");
        if (root != null) {
            root.setExecutor(command);
            root.setTabCompleter(command);
        }
        PluginCommand ability = getCommand("ability");
        if (ability != null) {
            ability.setExecutor(command);
        }
        PluginCommand runes = getCommand("runes");
        if (runes != null) {
            runes.setExecutor(command);
        }

        getServer().getPluginManager().registerEvents(new GameListener(this), this);
        scoreboardManager.start();
        raceManager.startPassiveTask();
        dataManager.startAutosave();
        dataManager.startMagiculeRegen();
        getLogger().info("Rea awakened. The bloodlines wake.");
    }

    @Override
    public void onDisable() {
        if (dataManager != null) {
            dataManager.saveAll();
        }
        if (scoreboardManager != null) {
            scoreboardManager.stop();
        }
    }

    public Keys getKeys() {
        return keys;
    }

    public ItemFactory getItemFactory() {
        return itemFactory;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public RaceManager getRaceManager() {
        return raceManager;
    }

    public QuestManager getQuestManager() {
        return questManager;
    }

    public CodexManager getCodexManager() {
        return codexManager;
    }

    public SkillManager getSkillManager() {
        return skillManager;
    }

    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }
}
