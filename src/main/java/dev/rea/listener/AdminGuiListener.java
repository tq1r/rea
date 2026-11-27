package dev.rea.listener;

import dev.rea.ReaPlugin;
import dev.rea.admin.AdminService;
import dev.rea.command.AdminGui;
import dev.rea.codex.CodexEntry;
import dev.rea.data.PlayerData;
import dev.rea.quest.QuestDef;
import dev.rea.race.RaceType;
import dev.rea.skill.SkillDef;
import dev.rea.util.Msg;
import dev.rea.util.Util;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

/**
 * Routes clicks in the /rea admin menus to plugin logic.
 */
public final class AdminGuiListener implements Listener {

    private final ReaPlugin plugin;
    private final AdminGui gui;

    public AdminGuiListener(ReaPlugin plugin, AdminGui gui) {
        this.plugin = plugin;
        this.gui = gui;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player admin)) {
            return;
        }
        String title = event.getView().getTitle();
        if (!title.contains("Rea Admin")) {
            return;
        }
        event.setCancelled(true);
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getItemMeta() == null) {
            return;
        }
        String action = item.getItemMeta().getPersistentDataContainer()
                .get(gui.actionKey(), PersistentDataType.STRING);
        if (action == null) {
            return;
        }

        // Menu navigation.
        if (action.startsWith("menu:")) {
            switch (action) {
                case "menu:main" -> gui.open(admin);
                case "menu:select" -> gui.openSelect(admin);
                case "menu:race" -> gui.openRace(admin);
                case "menu:skills" -> gui.openSkills(admin);
                case "menu:gear" -> gui.openGear(admin);
                case "menu:resources" -> gui.openResources(admin);
                case "menu:lore" -> gui.openLore(admin);
                case "menu:gameplay" -> gui.openGameplay(admin);
                case "menu:danger" -> gui.openDanger(admin, null);
                case "menu:close" -> admin.closeInventory();
                default -> {
                }
            }
            return;
        }
        if (action.startsWith("select:")) {
            try {
                UUID id = UUID.fromString(action.substring("select:".length()));
                Player target = plugin.getServer().getPlayer(id);
                if (target != null) {
                    gui.selectTarget(admin, target);
                    Msg.send(admin, "&7Target set to &e" + target.getName() + "&7.");
                }
            } catch (IllegalArgumentException ignored) {
                // Malformed id — ignore.
            }
            gui.open(admin);
            return;
        }
        // Danger actions need an armed confirmation (except kick, which is reversible).
        if (action.startsWith("danger:") && !"danger:kick_dungeon".equals(action)) {
            String pending = gui.pending(admin);
            if (!action.equals(pending)) {
                gui.setPending(admin, action);
                gui.openDanger(admin, action);
                Msg.send(admin, "&cArmed. Click the red button to confirm — or anything else to back out.");
                return;
            }
            gui.clearPending(admin);
        }

        Player target = gui.target(admin);
        execute(admin, target, action);
    }

    private void execute(Player admin, Player target, String action) {
        String[] parts = action.split(":");
        PlayerData data = plugin.getDataManager().get(target);
        switch (parts[0]) {
            case "race" -> {
                switch (parts[1]) {
                    case "set" -> {
                        RaceType race = RaceType.fromId(parts[2]);
                        if (race == null) {
                            Msg.send(admin, "&cUnknown race: " + parts[2]);
                            return;
                        }
                        AdminService.resetPlayer(plugin, target);
                        plugin.getRaceManager().assign(target, race);
                        Msg.send(admin, "&aReincarnated &f" + target.getName() + " &7as &f" + race.getDisplayName() + "&7.");
                    }
                    case "random" -> {
                        AdminService.resetPlayer(plugin, target);
                        RaceType[] races = RaceType.values();
                        plugin.getRaceManager().assign(target, races[(int) (Math.random() * races.length)]);
                        Msg.send(admin, "&aReincarnated &f" + target.getName() + " &7randomly.");
                    }
                    case "evolve" -> plugin.getRaceManager().forceEvolve(target);
                    case "max" -> {
                        for (int i = 0; i < 3; i++) {
                            PlayerData fresh = plugin.getDataManager().get(target);
                            if (RaceType.fromId(fresh.getRace()) == null || fresh.getForm() >= plugin.getRaceManager().maxStage()) {
                                break;
                            }
                            plugin.getRaceManager().forceEvolve(target);
                        }
                        Msg.send(admin, "&aAdvanced &f" + target.getName() + " &7to the Demon Lord form.");
                    }
                    default -> Msg.send(admin, "&cUnknown race action.");
                }
                gui.openRace(admin);
            }
            case "gear" -> {
                String piece = parts.length > 2 ? parts[2] : null;
                java.util.List<ItemStack> items = plugin.getArmorManager().grant(piece);
                if (items.isEmpty()) {
                    Msg.send(admin, "&cUnknown gear: " + piece);
                    return;
                }
                for (ItemStack item : items) {
                    Util.giveItem(target, item);
                }
                Msg.send(admin, "&aGranted &d" + items.size() + " &7Tempest Sovereign piece(s) to &f" + target.getName() + "&7.");
                gui.openGear(admin);
            }
            case "skill" -> {
                switch (parts[1]) {
                    case "grant_all" -> {
                        int count = 0;
                        for (SkillDef def : plugin.getSkillManager().allSkills().values()) {
                            if (!data.hasSkill(def.getId())) {
                                data.addSkill(def.getId());
                                count++;
                            }
                        }
                        plugin.getDataManager().save(data);
                        plugin.getSkillManager().reissueOrbs(target);
                        Msg.send(admin, "&aGranted &f" + count + " &7skills to &f" + target.getName() + "&7.");
                    }
                    case "clear" -> {
                        data.getSkills().clear();
                        plugin.getDataManager().save(data);
                        Msg.send(admin, "&aWiped all skills from &f" + target.getName() + "&7.");
                    }
                    case "refill" -> {
                        data.setMagicules(plugin.getDataManager().maxMagicules(data));
                        plugin.getDataManager().save(data);
                        Msg.send(admin, "&aRefilled magicules for &f" + target.getName() + "&7.");
                    }
                    case "reroll_unique" -> {
                        if (data.getUniqueSkillId() != null) {
                            data.getSkills().remove(data.getUniqueSkillId());
                        }
                        SkillDef unique = plugin.getSkillManager().grantUniqueSkill(RaceType.fromId(data.getRace()));
                        if (unique != null) {
                            data.addSkill(unique.getId());
                            data.setUniqueSkillId(unique.getId());
                            plugin.getDataManager().save(data);
                            Msg.send(admin, "&a" + target.getName() + " &7now bears the Unique Skill &f" + unique.getName() + "&7.");
                        }
                    }
                    default -> Msg.send(admin, "&cUnknown skill action.");
                }
                if ("skill:reroll_unique".equals(action)) {
                    gui.openRace(admin);
                } else {
                    gui.openSkills(admin);
                }
            }
            case "resources" -> {
                try {
                    switch (parts[1]) {
                        case "runes" -> plugin.getDataManager().addRunes(target, Long.parseLong(parts[2]));
                        case "xp" -> plugin.getDataManager().addXp(target, Integer.parseInt(parts[2]));
                        case "level" -> {
                            int cap = plugin.getConfig().getInt("settings.level-cap", 20);
                            data.setLevel(Math.min(cap, data.getLevel() + Integer.parseInt(parts[2])));
                            plugin.getDataManager().save(data);
                        }
                        case "cache" -> Util.giveItem(target, plugin.getItemFactory().runeCache(Integer.parseInt(parts[2])));
                        case "wipe_ep" -> {
                            data.setEp(0);
                            plugin.getDataManager().save(data);
                            Msg.send(admin, "&aWiped EP for &f" + target.getName() + "&7.");
                        }
                        default -> Msg.send(admin, "&cUnknown resource.");
                    }
                } catch (NumberFormatException e) {
                    Msg.send(admin, "&cBad amount in action: " + action);
                }
                gui.openResources(admin);
            }
            case "lore" -> {
                switch (parts[1]) {
                    case "grant_codex" -> {
                        int count = 0;
                        for (CodexEntry entry : plugin.getCodexManager().allEntries()) {
                            if (data.getCodex().add(entry.id())) {
                                count++;
                            }
                        }
                        plugin.getDataManager().save(data);
                        Msg.send(admin, "&aRecorded &f" + count + " &7new Codex entries for &f" + target.getName() + "&7.");
                    }
                    case "clear_codex" -> {
                        data.getCodex().clear();
                        plugin.getDataManager().save(data);
                        Msg.send(admin, "&aCleared Codex for &f" + target.getName() + "&7.");
                    }
                    case "complete_quests" -> {
                        int count = 0;
                        for (QuestDef quest : plugin.getQuestManager().allQuests()) {
                            if (data.getQuestsDone().add(quest.id())) {
                                count++;
                            }
                        }
                        plugin.getDataManager().save(data);
                        Msg.send(admin, "&aCompleted &f" + count + " &7quests for &f" + target.getName() + "&7.");
                    }
                    case "clear_quests" -> {
                        data.getQuestsDone().clear();
                        data.getCounters().clear();
                        plugin.getDataManager().save(data);
                        Msg.send(admin, "&aReset the quest story for &f" + target.getName() + "&7.");
                    }
                    default -> Msg.send(admin, "&cUnknown lore action.");
                }
                gui.openLore(admin);
            }
            case "gameplay" -> {
                switch (parts[1]) {
                    case "start" -> plugin.getRaceManager().startSeason();
                    case "save" -> {
                        plugin.getDataManager().saveAll();
                        Msg.send(admin, "&aAll Rea data saved to disk.");
                    }
                    case "reload" -> {
                        plugin.reloadConfig();
                        plugin.getCodexManager().reload();
                        plugin.getQuestManager().reload();
                        Msg.send(admin, "&aRea reloaded. The magicules barely noticed.");
                    }
                    case "update" -> plugin.getUpdateManager().checkNow(admin);
                    default -> Msg.send(admin, "&cUnknown gameplay action.");
                }
                gui.openGameplay(admin);
            }
            case "danger" -> {
                switch (parts[1]) {
                    case "reset" -> {
                        AdminService.resetPlayer(plugin, target);
                        Msg.send(admin, "&aWiped &f" + target.getName() + "&7's Rea data.");
                    }
                    case "reset_all" -> Msg.send(admin, "&aWiped &f" + AdminService.resetAllOnline(plugin) + " &7online players.");
                    case "kick_dungeon" -> {
                        plugin.getDungeonManager().endRun(target, false);
                        Msg.send(admin, "&aKicked &f" + target.getName() + " &7from the ruin.");
                    }
                    default -> Msg.send(admin, "&cUnknown danger action.");
                }
                gui.openDanger(admin, null);
            }
            default -> Msg.send(admin, "&cUnknown action: " + action);
        }
    }
}
