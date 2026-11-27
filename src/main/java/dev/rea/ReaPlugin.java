package dev.rea;

import dev.rea.codex.CodexManager;
import dev.rea.command.ReaCommand;
import dev.rea.command.AdminGui;
import dev.rea.command.DungeonGui;
import dev.rea.command.SkillsGui;
import dev.rea.data.DataManager;
import dev.rea.dungeon.DungeonManager;
import dev.rea.listener.AdminGuiListener;
import dev.rea.listener.DungeonListener;
import dev.rea.listener.GameListener;
import dev.rea.listener.GuiListener;
import dev.rea.quest.QuestManager;
import dev.rea.race.RaceManager;
import dev.rea.hud.HudManager;
import dev.rea.armor.ArmorManager;
import dev.rea.skill.SkillManager;
import dev.rea.update.UpdateManager;
import dev.rea.util.ItemFactory;
import dev.rea.util.Keys;
import dev.rea.trader.TraderManager;
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
    private HudManager hudManager;
    private SkillsGui skillsGui;
    private DungeonGui dungeonGui;
    private AdminGui adminGui;
    private WeaponManager weaponManager;
    private DungeonManager dungeonManager;
    private UpdateManager updateManager;
    private TraderManager traderManager;
    private ArmorManager armorManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        keys = new Keys(this);
        itemFactory = new ItemFactory(this);
        dataManager = new DataManager(this);
        dataManager.backupIfVersionChanged();
        raceManager = new RaceManager(this);
        questManager = new QuestManager(this);
        codexManager = new CodexManager(this);
        skillManager = new SkillManager(this);
        hudManager = new HudManager(this);
        skillsGui = new SkillsGui(this);
        dungeonGui = new DungeonGui(this);
        adminGui = new AdminGui(this);
        weaponManager = new WeaponManager(this);
        dungeonManager = new DungeonManager(this);
        updateManager = new UpdateManager(this);
        traderManager = new TraderManager(this);
        armorManager = new ArmorManager(this);

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

        getServer().getPluginManager().registerEvents(new GameListener(this), this);
        getServer().getPluginManager().registerEvents(new GuiListener(this, skillsGui, dungeonGui), this);
        getServer().getPluginManager().registerEvents(new AdminGuiListener(this, adminGui), this);
        getServer().getPluginManager().registerEvents(new DungeonListener(this), this);
        hudManager.start();
        raceManager.startPassiveTask();
        dataManager.startAutosave();
        dataManager.startMagiculeRegen();
        updateManager.start();
        traderManager.startAuraTask();
        getLogger().info("Rea awakened. The magicules stir.");
        getLogger().info("Host tip: run '/rea start' in-game to reincarnate your players.");
    }

    @Override
    public void onDisable() {
        if (dataManager != null) {
            dataManager.saveAll();
        }
        if (hudManager != null) {
            hudManager.stop();
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

    public HudManager getHudManager() {
        return hudManager;
    }

    public SkillsGui getSkillsGui() {
        return skillsGui;
    }

    public DungeonGui getDungeonGui() {
        return dungeonGui;
    }

    public AdminGui getAdminGui() {
        return adminGui;
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

    public TraderManager getTraderManager() {
        return traderManager;
    }

    public ArmorManager getArmorManager() {
        return armorManager;
    }
}
