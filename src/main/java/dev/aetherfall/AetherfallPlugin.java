package dev.aetherfall;

import dev.aetherfall.bounty.BountyManager;
import dev.aetherfall.codex.CodexManager;
import dev.aetherfall.command.AetherfallCommand;
import dev.aetherfall.data.DataManager;
import dev.aetherfall.event.EventManager;
import dev.aetherfall.gui.GuiManager;
import dev.aetherfall.listener.GameListener;
import dev.aetherfall.order.OrderManager;
import dev.aetherfall.path.PathManager;
import dev.aetherfall.quest.QuestManager;
import dev.aetherfall.relic.RelicManager;
import dev.aetherfall.ruin.RuinManager;
import dev.aetherfall.scoreboard.ScoreboardManager;
import dev.aetherfall.title.TitleManager;
import dev.aetherfall.util.ItemFactory;
import dev.aetherfall.util.Keys;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Aetherfall — a lore-rich SMP adventure plugin for Paper 26.3 + Geyser.
 * The shards remember. Will you?
 */
public final class AetherfallPlugin extends JavaPlugin {
    private Keys keys;
    private ItemFactory itemFactory;
    private DataManager dataManager;
    private PathManager pathManager;
    private QuestManager questManager;
    private CodexManager codexManager;
    private RelicManager relicManager;
    private BountyManager bountyManager;
    private EventManager eventManager;
    private RuinManager ruinManager;
    private OrderManager orderManager;
    private TitleManager titleManager;
    private GuiManager guiManager;
    private ScoreboardManager scoreboardManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        keys = new Keys(this);
        itemFactory = new ItemFactory(this);
        dataManager = new DataManager(this);
        pathManager = new PathManager(this);
        questManager = new QuestManager(this);
        codexManager = new CodexManager(this);
        relicManager = new RelicManager(this);
        bountyManager = new BountyManager(this);
        eventManager = new EventManager(this);
        ruinManager = new RuinManager(this);
        orderManager = new OrderManager(this);
        titleManager = new TitleManager(this);
        guiManager = new GuiManager(this);
        scoreboardManager = new ScoreboardManager(this);

        AetherfallCommand command = new AetherfallCommand(this);
        PluginCommand root = getCommand("aetherfall");
        if (root != null) {
            root.setExecutor(command);
            root.setTabCompleter(command);
        }
        PluginCommand ability = getCommand("ability");
        if (ability != null) {
            ability.setExecutor(command);
        }
        PluginCommand shards = getCommand("shards");
        if (shards != null) {
            shards.setExecutor(command);
        }

        getServer().getPluginManager().registerEvents(new GameListener(this), this);
        scoreboardManager.start();
        eventManager.start();
        pathManager.startPassiveTask();
        dataManager.startAutosave();
        getLogger().info("Aetherfall awakened. The shards remember.");
    }

    @Override
    public void onDisable() {
        if (dataManager != null) {
            dataManager.saveAll();
        }
        if (eventManager != null) {
            eventManager.stop();
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

    public PathManager getPathManager() {
        return pathManager;
    }

    public QuestManager getQuestManager() {
        return questManager;
    }

    public CodexManager getCodexManager() {
        return codexManager;
    }

    public RelicManager getRelicManager() {
        return relicManager;
    }

    public BountyManager getBountyManager() {
        return bountyManager;
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    public RuinManager getRuinManager() {
        return ruinManager;
    }

    public OrderManager getOrderManager() {
        return orderManager;
    }

    public TitleManager getTitleManager() {
        return titleManager;
    }

    public GuiManager getGuiManager() {
        return guiManager;
    }

    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }
}
