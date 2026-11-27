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
 * in menus (Skills, Ruins) or happens automatically (passives, Regen).
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
                    Msg.send(player, "&8Curious what awaits? &e/rea help");
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
            case "admin" -> admin(sender);
            case "update" -> update(sender);
            case "give" -> give(sender, args);
            case "reset" -> reset(sender, args);
            case "reload" -> reload(sender);
            default -> Msg.send(sender, "&cUnknown subcommand. &e/rea help &7for the tour.");
        }
        return true;
    }

    private void help(CommandSender sender) {
        Msg.send(sender, "&d&lRea &7- your journey from slime to Demon Lord");
        Msg.send(sender, "&71. &fReincarnate: &7an admin runs &e/rea start&7 — you get a random race and a Unique Skill.");
        Msg.send(sender, "&72. &fFight: &7slaying monsters grants &dEP&7 (rank), &eXP&7 (level) and runes; devouring with Predator teaches new skills.");
        Msg.send(sender, "&73. &fCast: &fhold your Skill Orb out and use it &7(right-click Java · hold-and-press Bedrock). Your magicules fuel it.");
        Msg.send(sender, "&74. &fEvolve: &f/ability &7(or &e/talents&7) — meet the EP, level and rune costs, click Evolve, grow a form. Demon Lord = Ultimate Skill.");
        Msg.send(sender, "&7Check yourself: &e/rea&7 profile · &e/rea quests&7 story · &e/rea codex&7 lore");
        Msg.send(sender, "&7Risk your life: &e/rea dungeon&7 ruins hold named cardinal weapons.");
        Msg.send(sender, "&7Community: &e/rea trader&7 name and train villager specialists.");
        if (sender.hasPermission("rea.admin")) {
            Msg.send(sender, "&7Admin: &e/rea admin&7 everything menu · &e/rea start&7 · &e/rea update&7 · &e/rea reload&7");
        }
    }

    private void profile(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            Msg.send(sender, "&cOnly players have a profile.");
            return;
        }
        PlayerData data = plugin.getDataManager().get(player);
        RaceType race = RaceType.fromId(data.getRace());
        Msg.send(sender, "&d&l" + player.getName());
        if (race == null) {
            Msg.send(sender, "&7Your reincarnation awaits. An admin must run &e/rea start&7.");
            Msg.send(sender, "&8New here? &e/rea help &8explains the journey in 7 lines.");
        } else {
            Msg.send(sender, "&7Race: " + race.getColor() + race.formName(data.getForm()));
        }
        Long toNext = plugin.getDataManager().nextRankEp(data);
        String nextRank = toNext != null ? " &8(+" + toNext + " EP to next rank)" : " &8(max rank)";
        Msg.send(sender, "&7Rank: &d" + plugin.getDataManager().rank(data) + " &7EP: &d" + data.getEp() + nextRank
                + " &7Magicules: &d" + data.getMagicules() + "/" + plugin.getDataManager().maxMagicules(data));
        Msg.send(sender, "&7Level: &d" + data.getLevel() + " &7(" + data.getXp() + "/" + plugin.getDataManager().xpForLevel(data.getLevel()) + " XP)");
        Msg.send(sender, "&7Runes: &d" + data.getRunes() + " &7(lifetime: " + data.getLifetimeEarned() + ")");
        Msg.send(sender, "&7Codex: &d" + data.getCodex().size() + "/" + plugin.getCodexManager().total()
                + " &7Quests: &d" + data.getQuestsDone().size() + "/" + plugin.getQuestManager().allQuests().size());
        Msg.send(sender, "&7Kills: &d" + data.getKills() + " &7Mined: &d" + data.getMines() + " &7Crafted: &d" + data.getCrafts()
               );
    }

    private void quests(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            return;
        }
        PlayerData data = plugin.getDataManager().get(player);
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
        Msg.send(player, "&d&lCodex of Rea &7(" + data.getCodex().size() + "/" + plugin.getCodexManager().total() + ")");
        for (CodexEntry entry : plugin.getCodexManager().allEntries()) {
            if (data.getCodex().contains(entry.id())) {
                Msg.send(player, "&a[read] &f" + entry.title() + " &7- &e/rea codex read " + entry.id());
            } else {
                Msg.send(player, "&7[???] &7" + entry.subtitle());
            }
        }
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
        dev.rea.admin.AdminService.resetPlayer(plugin, target);
        Msg.send(sender, "&aReset Rea data for " + target.getName() + ". Reincarnate fresh with /rea start.");
    }

    private void admin(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            Msg.send(sender, "&cOnly players can open the admin menu.");
            return;
        }
        if (!player.hasPermission("rea.admin")) {
            Msg.send(player, "&cNo permission.");
            return;
        }
        plugin.getAdminGui().open(player);
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
            for (String sub : List.of("help", "quests", "codex", "dungeon", "trader", "start", "admin", "update", "give", "reset", "reload")) {
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
