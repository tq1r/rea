package dev.rea;

import dev.rea.codex.CodexManager;
import dev.rea.command.EvolveGui;
import dev.rea.command.ReaCommand;
import dev.rea.command.SkillsGui;
import dev.rea.data.DataManager;
import dev.rea.dungeon.DungeonManager;
import dev.rea.listener.DungeonListener;
import dev.rea.listener.GameListener;
import dev.rea.listener.GuiListener;
import dev.rea.quest.QuestManager;
import dev.rea.race.RaceManager;
import dev.rea.scoreboard.ScoreboardManager;
import dev.rea.skill.SkillManager;
import dev.rea.update.UpdateManager;
import dev.rea.util.ItemFactory;
import dev.rea.util.Keys;
import dev.rea.weapon.WeaponManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Rea — a Tensura (That Time I Got Reincarnated as a Slime) plugin for Paper 26.3.
 * Players reincarnate as a random Tensura race, receive a Unique Skill,
 * gain EP and magicules, devour foes to learn Common Skills (Predator),
 * and evolve through their race's chain to a Demon Lord awakening where
 * their Unique Skill evolves into an Ultimate Skill.
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
    private SkillsGui skillsGui;
    private EvolveGui evolveGui;
    private WeaponManager weaponManager;
    private DungeonManager dungeonManager;
    private UpdateManager updateManager;

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
        skillsGui = new SkillsGui(this);
        evolveGui = new EvolveGui(this);
        weaponManager = new WeaponManager(this);
        dungeonManager = new DungeonManager(this);
        updateManager = new UpdateManager(this);

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
        getServer().getPluginManager().registerEvents(new GuiListener(this, skillsGui, evolveGui), this);
        getServer().getPluginManager().registerEvents(new DungeonListener(this), this);
        scoreboardManager.start();
        raceManager.startPassiveTask();
        dataManager.startAutosave();
        dataManager.startMagiculeRegen();
        updateManager.start();
        getLogger().info("Rea awakened. The magicules stir.");
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

    public SkillsGui getSkillsGui() {
        return skillsGui;
    }

    public EvolveGui getEvolveGui() {
        return evolveGui;
    }

    public WeaponManager getWeaponManager() {
        return weaponManager;
    }

    public DungeonManager getDungeonManager() {
        return dungeonManager;
    }

    public UpdateManager getUpdateManager() {
        return updateManager;
    }
}
