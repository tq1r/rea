package dev.aetherfall.title;

import dev.aetherfall.AetherfallPlugin;
import dev.aetherfall.data.PlayerData;
import dev.aetherfall.quest.QuestDef;
import dev.aetherfall.util.Msg;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Earned titles, shown on the scoreboard and in /profile.
 * Titles are unlocked by playing the story — never bought.
 */
public final class TitleManager {
    private final AetherfallPlugin plugin;
    private final List<TitleDef> defs = new ArrayList<>();

    public TitleManager(AetherfallPlugin plugin) {
        this.plugin = plugin;
        defs.add(new TitleDef("shardling", "Shardling", "You woke in the Vale."));
        defs.add(new TitleDef("pathwalker", "Pathwalker", "You chose a Path."));
        defs.add(new TitleDef("ruin-seeker", "Ruin Seeker", "You found a ruin."));
        defs.add(new TitleDef("codex-keeper", "Codex Keeper", "You recorded 5 Codex entries."));
        defs.add(new TitleDef("relic-hunter", "Relic Hunter", "You claimed a relic."));
        defs.add(new TitleDef("bounty-hunter", "Bounty Hunter", "You completed a bounty."));
        defs.add(new TitleDef("event-survivor", "Event Survivor", "You lived through a world event."));
        defs.add(new TitleDef("warden", "Warden", "You finished Chapter II: The Silent Gods."));
        defs.add(new TitleDef("shardbearer", "Shardbearer", "You finished Chapter III: The Second Crack."));
        defs.add(new TitleDef("old-soul", "Old Soul", "You reached Level 10."));
    }

    public List<TitleDef> allTitles() {
        return defs;
    }

    public TitleDef getTitle(String id) {
        for (TitleDef def : defs) {
            if (def.id().equalsIgnoreCase(id)) {
                return def;
            }
        }
        return null;
    }

    public void unlock(Player player, String id) {
        PlayerData data = plugin.getDataManager().get(player);
        if (data.getTitles().contains(id)) {
            return;
        }
        TitleDef def = getTitle(id);
        if (def == null) {
            return;
        }
        data.getTitles().add(id);
        if (data.getSelectedTitle() == null) {
            data.setSelectedTitle(id);
        }
        Msg.send(player, "&d&lTitle earned: &l" + def.display() + " &7- " + def.description());
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1f);
        plugin.getDataManager().save(data);
    }

    public void select(Player player, String id) {
        PlayerData data = plugin.getDataManager().get(player);
        TitleDef def = getTitle(id);
        if (def == null) {
            Msg.send(player, "&cUnknown title '" + id + "'. &e/aetherfall titles");
            return;
        }
        if (!data.getTitles().contains(id)) {
            Msg.send(player, "&cYou have not earned the &l" + def.display() + " &ctitle yet. &7" + def.description());
            return;
        }
        data.setSelectedTitle(id);
        Msg.send(player, "&7You are now &d&l" + def.display());
        plugin.getDataManager().save(data);
    }

    public String selectedDisplay(PlayerData data) {
        TitleDef def = getTitle(data.getSelectedTitle());
        return def == null ? "Shardling" : def.display();
    }

    /** Re-check every unlock condition. Cheap enough to call after major events. */
    public void checkAll(Player player) {
        PlayerData data = plugin.getDataManager().get(player);
        unlock(player, "shardling");
        if (data.getPath() != null) {
            unlock(player, "pathwalker");
        }
        if (!data.getRuins().isEmpty()) {
            unlock(player, "ruin-seeker");
        }
        if (data.getCodex().size() >= 5) {
            unlock(player, "codex-keeper");
        }
        if (data.counter("relic") > 0) {
            unlock(player, "relic-hunter");
        }
        if (data.counter("bounty") > 0) {
            unlock(player, "bounty-hunter");
        }
        if (data.counter("event") > 0) {
            unlock(player, "event-survivor");
        }
        if (chapterComplete(data, 2)) {
            unlock(player, "warden");
        }
        if (chapterComplete(data, 3)) {
            unlock(player, "shardbearer");
        }
        if (data.getLevel() >= 10) {
            unlock(player, "old-soul");
        }
    }

    private boolean chapterComplete(PlayerData data, int chapter) {
        boolean any = false;
        for (QuestDef quest : plugin.getQuestManager().allQuests()) {
            if (quest.chapter() != chapter) {
                continue;
            }
            any = true;
            if (!data.getQuestsDone().contains(quest.id())) {
                return false;
            }
        }
        return any;
    }

    public void list(Player player) {
        PlayerData data = plugin.getDataManager().get(player);
        Msg.send(player, Msg.bar());
        Msg.send(player, "&d&lTitles &7(select with &e/aetherfall title <id>&7)");
        for (TitleDef def : defs) {
            boolean owned = data.getTitles().contains(def.id());
            boolean selected = def.id().equals(data.getSelectedTitle());
            String mark = selected ? "&d[using]" : owned ? "&a[earned]" : "&7[locked]";
            Msg.send(player, mark + " &f" + def.display() + " &7- " + def.description());
        }
        Msg.send(player, Msg.bar());
    }
}
