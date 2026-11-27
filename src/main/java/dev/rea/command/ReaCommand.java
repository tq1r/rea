package dev.rea.command;

import dev.rea.ReaPlugin;
import dev.rea.codex.CodexEntry;
import dev.rea.data.PlayerData;
import dev.rea.quest.QuestDef;
import dev.rea.race.RaceType;
import dev.rea.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Player-facing commands are deliberately few. Everything actionable lives
 * in menus (Talents, Ruins) or happens automatically (daily gifts on join).
 * What remains: checks (profile, quests, codex) and the trader system.
 * All admin tooling lives here too, behind rea.admin.
 */
public final class ReaCommand implements CommandExecutor, TabCompleter {
    private final ReaPlugin plugin;

    public ReaCommand(ReaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String name = command.getName().toLowerCase();
        if (name.equals("ability")) {
            // One talent menu to rule them all: skills, evolution, race card.
            if (sender instanceof Player player) {
                PlayerData data = plugin.getDataManager().get(player);
                if (RaceType.fromId(data.getRace()) == null) {
                    Msg.send(player, "&cYour reincarnation awaits. An admin must run &e/rea start&c.");
                } else {
                    plugin.getSkillsGui().open(player);
                }
            }
            return true;
        }
        if (args.length == 0) {
            profile(sender); // bare /rea: your profile card
            return true;
        }
        String sub = args[0].toLowerCase();
        switch (sub) {
            case "help" -> help(sender);
            case "start" -> start(sender, args);
            case "quests" -> quests(sender);
            case "codex" -> codex(sender, args);
            case "dungeon" -> dungeon(sender, args);
            case "trader" -> trader(sender, args);
            case "update" -> update(sender);
            case "give" -> give(sender, args);
            case "reset" -> reset(sender, args);
            case "reload" -> reload(sender);
            default -> Msg.send(sender, "&cUnknown subcommand. &e/rea help");
        }
        return true;
    }

    private void help(CommandSender sender) {
        Msg.send(sender, Msg.bar());
        Msg.send(sender, "&d&lRea &7- from Slime to Demon Lord.");
        Msg.send(sender, "&e/rea &7- your profile card");
        Msg.send(sender, "&e/rea quests &7- the story so far");
        Msg.send(sender, "&e/rea codex [read <id>] &7- the lore you have gathered");
        Msg.send(sender, "&e/rea dungeon &7- the ruins (enter a ruin from the menu)");
        Msg.send(sender, "&e/rea dungeon leave &7- abandon a run");
        Msg.send(sender, "&e/rea trader &7- name and train villager specialists");
        Msg.send(sender, "&e/ability &7- your talents: cast skills, evolve");
        Msg.send(sender, "&7(Admin: &e/rea start, give, reset, reload, update&7)");
        Msg.send(sender, Msg.bar());
    }

    private void profile(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            Msg.send(sender, "&cOnly players have a profile.");
            return;
        }
        PlayerData data = plugin.getDataManager().get(player);
        RaceType race = RaceType.fromId(data.getRace());
        Msg.send(sender, Msg.bar());
        Msg.send(sender, "&d&l" + player.getName());
        if (race == null) {
            Msg.send(sender, "&7Your reincarnation awaits. An admin must run &e/rea start&7.");
        } else {
            Msg.send(sender, "&7Race: " + race.getColor() + race.formName(data.getForm()));
        }
        Msg.send(sender, "&7Rank: &d" + plugin.getDataManager().rank(data) + " &7EP: &d" + data.getEp()
                + " &7Magicules: &d" + data.getMagicules() + "/" + plugin.getDataManager().maxMagicules(data));
        Msg.send(sender, "&7Level: &d" + data.getLevel() + " &7(" + data.getXp() + "/" + plugin.getDataManager().xpForLevel(data.getLevel()) + " XP)");
        Msg.send(sender, "&7Runes: &d" + data.getRunes() + " &7(lifetime: " + data.getLifetimeEarned() + ")");
        Msg.send(sender, "&7Codex: &d" + data.getCodex().size() + "/" + plugin.getCodexManager().total()
                + " &7Quests: &d" + data.getQuestsDone().size() + "/" + plugin.getQuestManager().allQuests().size());
        Msg.send(sender, "&7Kills: &d" + data.getKills() + " &7Mined: &d" + data.getMines() + " &7Crafted: &d" + data.getCrafts()
                + " &7Streak: &d" + data.getDailyStreak());
        Msg.send(sender, Msg.bar());
    }

    private void quests(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            return;
        }
        PlayerData data = plugin.getDataManager().get(player);
        Msg.send(sender, Msg.bar());
        Msg.send(sender, "&d&lThe Story of Rea &7(" + data.getQuestsDone().size() + "/" + plugin.getQuestManager().allQuests().size() + " quests)");
        QuestDef current = plugin.getQuestManager().currentQuest(data);
        if (current == null) {
            Msg.send(sender, "&7The final spark still sleeps. More chapters are coming.");
        } else {
            int progress = plugin.getQuestManager().progress(data, current);
            Msg.send(sender, "&dChapter " + current.chapter() + ": &l" + current.name());
            Msg.send(sender, "&7" + current.description());
            Msg.send(sender, "&7Progress: &e" + Math.min(progress, current.amount()) + "/" + current.amount());
            Msg.send(sender, "&7Rewards: &d" + current.rewardRunes() + " runes"
                    + (current.rewardXp() > 0 ? " &7+" + current.rewardXp() + " XP" : ""));
        }
        Msg.send(sender, Msg.bar());
    }

    private void codex(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            return;
        }
        PlayerData data = plugin.getDataManager().get(player);
        if (args.length >= 3 && args[1].equalsIgnoreCase("read")) {
            CodexEntry entry = plugin.getCodexManager().getEntry(args[2]);
            if (entry == null) {
                Msg.send(player, "&cNo Codex entry '" + args[2] + "'.");
                return;
            }
            if (!data.getCodex().contains(entry.id())) {
                Msg.send(player, "&cYou have not recorded that entry yet. Find its page first.");
                return;
            }
            plugin.getCodexManager().showEntry(player, entry);
            return;
        }
        Msg.send(player, Msg.bar());
        Msg.send(player, "&d&lCodex of Rea &7(" + data.getCodex().size() + "/" + plugin.getCodexManager().total() + ")");
        for (CodexEntry entry : plugin.getCodexManager().allEntries()) {
            if (data.getCodex().contains(entry.id())) {
                Msg.send(player, "&a[read] &f" + entry.title() + " &7- &e/rea codex read " + entry.id());
            } else {
                Msg.send(player, "&7[???] &7" + entry.subtitle());
            }
        }
        Msg.send(player, Msg.bar());
    }

    private void dungeon(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            Msg.send(sender, "&cOnly players can dive.");
            return;
        }
        if (args.length >= 2 && args[1].equalsIgnoreCase("leave")) {
            if (!plugin.getDungeonManager().inDungeon(player)) {
                Msg.send(player, "&cYou are not in a dungeon.");
                return;
            }
            plugin.getDungeonManager().endRun(player, false);
            return;
        }
        plugin.getDungeonGui().open(player);
    }

    private void trader(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            Msg.send(sender, "&cOnly players can name specialists.");
            return;
        }
        if (args.length < 2) {
            Msg.send(player, "&dNamed villagers: &e/rea trader specialize <role> <name>|rename|train|info");
            Msg.send(player, "&7Roles: &eblacksmith &7(arms & cardinal weapons), &efarmer &7(food & gilded apples), &ealchemist &7(brewing reagents), &escribe &7(codex tomes)");
            return;
        }
        String action = args[1].toLowerCase();
        switch (action) {
            case "specialize" -> {
                if (args.length < 4) {
                    Msg.send(player, "&e/rea trader specialize <blacksmith|farmer|alchemist|scribe> <name...>");
                    return;
                }
                String name = String.join(" ", java.util.Arrays.copyOfRange(args, 3, args.length));
                plugin.getTraderManager().specialize(player, args[2], name);
            }
            case "rename" -> {
                if (args.length < 3) {
                    Msg.send(player, "&e/rea trader rename <name...>");
                    return;
                }
                String name = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));
                plugin.getTraderManager().rename(player, name);
            }
            case "train" -> plugin.getTraderManager().train(player);
            case "info" -> plugin.getTraderManager().info(player);
            default -> Msg.send(player, "&cUnknown trader subcommand. &e/rea trader");
        }
    }

    private void start(CommandSender sender, String[] args) {
        if (!sender.hasPermission("rea.admin")) {
            Msg.send(sender, "&cNo permission.");
            return;
        }
        if (args.length >= 2) {
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                Msg.send(sender, "&cThat player is not online.");
                return;
            }
            PlayerData data = plugin.getDataManager().get(target);
            if (data.getRace() != null) {
                Msg.send(sender, "&c" + target.getName() + " has already reincarnated (" + data.getRace() + "). Races are permanent.");
                return;
            }
            RaceType[] races = RaceType.values();
            plugin.getRaceManager().assign(target, races[(int) (Math.random() * races.length)]);
            return;
        }
        plugin.getRaceManager().startSeason();
    }

    private void give(CommandSender sender, String[] args) {
        if (!sender.hasPermission("rea.admin")) {
            Msg.send(sender, "&cNo permission.");
            return;
        }
        if (args.length < 4 || !args[1].equalsIgnoreCase("runes")) {
            Msg.send(sender, "&e/rea give runes <player> <amount>");
            return;
        }
        Player target = Bukkit.getPlayer(args[2]);
        if (target == null) {
            Msg.send(sender, "&cThat player is not online.");
            return;
        }
        long amount;
        try {
            amount = Long.parseLong(args[3]);
        } catch (NumberFormatException e) {
            Msg.send(sender, "&cAmount must be a number.");
            return;
        }
        plugin.getDataManager().addRunes(target, amount);
        Msg.send(sender, "&aGave " + amount + " runes to " + target.getName());
    }

    private void reset(CommandSender sender, String[] args) {
        if (!sender.hasPermission("rea.admin")) {
            Msg.send(sender, "&cNo permission.");
            return;
        }
        if (args.length < 2) {
            Msg.send(sender, "&e/rea reset <player>");
            return;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            Msg.send(sender, "&cThat player is not online.");
            return;
        }
        plugin.getDataManager().delete(target.getUniqueId());
        Msg.send(sender, "&aReset Rea data for " + target.getName() + ". Their reincarnation awaits again until /rea start.");
    }

    private void update(CommandSender sender) {
        if (!sender.hasPermission("rea.admin")) {
            Msg.send(sender, "&cNo permission.");
            return;
        }
        plugin.getUpdateManager().checkNow(sender);
    }

    private void reload(CommandSender sender) {
        if (!sender.hasPermission("rea.admin")) {
            Msg.send(sender, "&cNo permission.");
            return;
        }
        plugin.reloadConfig();
        plugin.getCodexManager().reload();
        plugin.getQuestManager().reload();
        Msg.send(sender, "&aRea reloaded. The magicules barely noticed.");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (!command.getName().equalsIgnoreCase("rea")) {
            return out;
        }
        if (args.length == 1) {
            for (String sub : List.of("help", "quests", "codex", "dungeon", "trader", "start", "update", "give", "reset", "reload")) {
                if (sub.startsWith(args[0].toLowerCase())) {
                    out.add(sub);
                }
            }
        } else if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "codex" -> {
                    if ("read".startsWith(args[1].toLowerCase())) {
                        out.add("read");
                    }
                }
                case "dungeon" -> {
                    if ("leave".startsWith(args[1].toLowerCase())) {
                        out.add("leave");
                    }
                }
                case "trader" -> {
                    for (String sub : List.of("specialize", "rename", "train", "info")) {
                        if (sub.startsWith(args[1].toLowerCase())) {
                            out.add(sub);
                        }
                    }
                }
                case "give" -> {
                    if ("runes".startsWith(args[1].toLowerCase())) {
                        out.add("runes");
                    }
                }
                default -> {
                }
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("trader") && args[1].equalsIgnoreCase("specialize")) {
            for (String role : List.of("blacksmith", "farmer", "alchemist", "scribe")) {
                if (role.startsWith(args[2].toLowerCase())) {
                    out.add(role);
                }
            }
        }
        return out;
    }
}
