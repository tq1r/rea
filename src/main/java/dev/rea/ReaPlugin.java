package dev.rea;

import dev.rea.codex.CodexManager;
import dev.rea.command.AbilityCommand;
import dev.rea.command.EvolveGui;
import dev.rea.command.ReaCommand;
import dev.rea.command.RunesCommand;
import dev.rea.command.SkillsGui;
import dev.rea.config.ConfigManager;
import dev.rea.data.DataManager;
import dev.rea.listener.GameListener;
import dev.rea.listener.GuiListener;
import dev.rea.quest.QuestManager;
import dev.rea.race.RaceManager;
import dev.rea.scoreboard.ScoreboardManager;
import dev.rea.skill.SkillManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Rea — a Tensura (That Time I Got Reincarnated as a Slime) plugin for Paper 26.3.
 * Players reincarnate as a random Tensura race, receive a Unique Skill,
 * gain EP and magicules, devour foes to learn Common Skills (Predator),
 * and evolve through their race's chain to a Demon Lord awakening where
 * their Unique Skill evolves into an Ultimate Skill.
 */
public final class ReaPlugin extends JavaPlugin {

    private ConfigManager configManager;
    private DataManager dataManager;
    private SkillManager skillManager;
    private RaceManager raceManager;
    private QuestManager questManager;
    private CodexManager codexManager;
    private ScoreboardManager scoreboardManager;
    private GameListener gameListener;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        configManager = new ConfigManager(this);
        dataManager = new DataManager(this);
        skillManager = new SkillManager(this);
        raceManager = new RaceManager(this, skillManager);
        questManager = new QuestManager(this);
        codexManager = new CodexManager(this);
        scoreboardManager = new ScoreboardManager(this);
        gameListener = new GameListener(this, skillManager);

        ReaCommand reaCommand = new ReaCommand(this, raceManager, skillManager);
        if (getCommand("rea") != null) {
            getCommand("rea").setExecutor(reaCommand);
            getCommand("rea").setTabCompleter(reaCommand);
        }
        AbilityCommand abilityCommand = new AbilityCommand(this, skillManager);
        if (getCommand("ability") != null) {
            getCommand("ability").setExecutor(abilityCommand);
        }
        RunesCommand runesCommand = new RunesCommand(this);
        if (getCommand("runes") != null) {
            getCommand("runes").setExecutor(runesCommand);
        }

        SkillsGui skillsGui = new SkillsGui(this, skillManager);
        EvolveGui evolveGui = new EvolveGui(this, raceManager);
        getServer().getPluginManager().registerEvents(gameListener, this);
        getServer().getPluginManager().registerEvents(new GuiListener(this, raceManager, skillsGui, evolveGui), this);
        getServer().getScheduler().runTaskTimer(this, () -> {
            scoreboardManager.updateAll();
            gameListener.tickPassives();
        }, 20L, 20L);

        getLogger().info("Rea enabled. The world turns.");
    }

    @Override
    public void onDisable() {
        if (dataManager != null) {
            dataManager.saveAll();
        }
        getLogger().info("Rea disabled.");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public SkillManager getSkillManager() {
        return skillManager;
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

    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }

    public GameListener getGameListener() {
        return gameListener;
    }
}
