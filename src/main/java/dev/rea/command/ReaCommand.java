package dev.rea.command;

import dev.rea.ReaPlugin;
import dev.rea.codex.CodexEntry;
import dev.rea.data.PlayerData;
import dev.rea.quest.QuestDef;
import dev.rea.race.RaceType;
import dev.rea.util.Msg;
import dev.rea.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Every Rea command in one place. Plain text throughout, so Bedrock
 * players (Geyser) get the full game with the same commands as Java players.
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
            if (sender instanceof Player player) {
                plugin.getRaceManager().useAbility(player);
            }
            return true;
        }
        if (name.equals("runes")) {
            if (sender instanceof Player player) {
                PlayerData data = plugin.getDataManager().get(player);
                Msg.send(player, "&dRunes: &l" + data.getRunes() + " &7(lifetime: " + data.getLifetimeEarned() + ")");
            }
            return true;
        }
        if (args.length == 0) {
            help(sender);
            return true;
        }
        String sub = args[0].toLowerCase();
        switch (sub) {
            case "help" -> help(sender);
            case "start" -> start(sender, args);
            case "race" -> race(sender);
            case "ability" -> {
                if (sender instanceof Player player) {
                    plugin.getRaceManager().useAbility(player);
                }
            }
            case "profile" -> profile(sender);
            case "quests" -> quests(sender);
            case "codex" -> codex(sender, args);
            case "daily" -> daily(sender);
            case "buy" -> buy(sender, args);
            case "runes" -> {
                if (sender instanceof Player player) {
                    PlayerData data = plugin.getDataManager().get(player);
                    Msg.send(player, "&dRunes: &l" + data.getRunes() + " &7(lifetime: " + data.getLifetimeEarned() + ")");
                }
            }
            case "give" -> give(sender, args);
            case "reset" -> reset(sender, args);
            case "reload" -> reload(sender);
            default -> Msg.send(sender, "&cUnknown subcommand. &e/rea help");
        }
        return true;
    }

    private void help(CommandSender sender) {
        Msg.send(sender, Msg.bar());
        Msg.send(sender, "&d&lRea &7- the bloodlines wake.");
        Msg.send(sender, "&e/rea start &7- (admin) deal bloodlines to everyone");
        Msg.send(sender, "&e/rea race &7- your bloodline and its gifts");
        Msg.send(sender, "&e/rea ability &7- use your bloodline ability");
        Msg.send(sender, "&e/rea profile &7- level, runes, Codex, stats");
        Msg.send(sender, "&e/rea quests &7- the story so far");
        Msg.send(sender, "&e/rea codex [read <id>] &7- lore you have collected");
        Msg.send(sender, "&e/rea daily &7- daily rune gift");
        Msg.send(sender, "&e/rea buy cache [n] &7- trade runes for XP");
        Msg.send(sender, "&e/rea runes &7- your balance");
        Msg.send(sender, Msg.bar());
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
                Msg.send(sender, "&c" + target.getName() + " already has a bloodline (" + data.getRace() + "). Bloodlines are permanent.");
                return;
            }
            RaceType[] races = RaceType.values();
            plugin.getRaceManager().assign(target, races[(int) (Math.random() * races.length)]);
            return;
        }
        plugin.getRaceManager().startSeason();
    }

    private void race(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            return;
        }
        PlayerData data = plugin.getDataManager().get(player);
        RaceType type = RaceType.fromId(data.getRace());
        Msg.send(sender, Msg.bar());
        if (type == null) {
            Msg.send(sender, "&7Your bloodline still sleeps. An admin must run &e/rea start&7.");
            Msg.send(sender, Msg.bar());
            return;
        }
        Msg.send(sender, type.getColor() + "&l" + type.getDisplayName());
        for (String line : type.getLore()) {
            Msg.send(sender, "&7" + line);
        }
        Msg.send(sender, "&7Blood gift: &f" + type.getPassiveDescription());
        Msg.send(sender, "&7Ability: &f" + type.getAbilityName() + " &7- " + type.getAbilityDescription());
        Msg.send(sender, "&7Bloodlines are permanent. This one is yours.");
        Msg.send(sender, Msg.bar());
    }

    private void profile(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            return;
        }
        PlayerData data = plugin.getDataManager().get(player);
        RaceType race = RaceType.fromId(data.getRace());
        Msg.send(sender, Msg.bar());
        Msg.send(sender, "&d&l" + player.getName());
        Msg.send(sender, "&7Bloodline: " + (race == null ? "Sleeping" : race.getColor() + race.getDisplayName()));
        Msg.send(sender, "&7Level: &d" + data.getLevel() + " &7(" + data.getXp() + "/" + plugin.getDataManager().xpForLevel(data.getLevel()) + " XP)");
        Msg.send(sender, "&7Runes: &d" + data.getRunes() + " &7(lifetime: " + data.getLifetimeEarned() + ")");
        Msg.send(sender, "&7Codex: &d" + data.getCodex().size() + "/" + plugin.getCodexManager().total()
                + " &7Quests: &d" + data.getQuestsDone().size() + "/" + plugin.getQuestManager().allQuests().size());
        Msg.send(sender, "&7Kills: &d" + data.getKills() + " &7Mined: &d" + data.getMines() + " &7Crafted: &d" + data.getCrafts()
                + " &7Daily streak: &d" + data.getDailyStreak());
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
            Msg.send(sender, "&7The eleventh spark still sleeps. More chapters are coming.");
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

    private void daily(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            return;
        }
        PlayerData data = plugin.getDataManager().get(player);
        String today = LocalDate.now().toString();
        String yesterday = LocalDate.now().minusDays(1).toString();
        if (today.equals(data.getLastDaily())) {
            Msg.send(player, "&cYou already claimed today's gift. The blood will sing again tomorrow.");
            return;
        }
        int streak = yesterday.equals(data.getLastDaily()) ? data.getDailyStreak() + 1 : 1;
        data.setDailyStreak(streak);
        data.setLastDaily(today);
        Msg.send(player, Msg.bar());
        Msg.send(player, "&d&lDay " + streak + " &7- the blood hums a little louder today.");
        switch ((streak - 1) % 7) {
            case 0 -> plugin.getDataManager().addRunes(player, 20L);
            case 1 -> {
                plugin.getDataManager().addRunes(player, 30L);
                Util.giveItem(player, new ItemStack(Material.BREAD, 4));
            }
            case 2 -> Util.giveItem(player, plugin.getItemFactory().runeCache(1));
            case 3 -> plugin.getDataManager().addRunes(player, 50L);
            case 4 -> {
                plugin.getDataManager().addRunes(player, 60L);
                Util.giveItem(player, new ItemStack(Material.GOLDEN_APPLE, 1));
            }
            case 5 -> Util.giveItem(player, plugin.getItemFactory().runeCache(2));
            case 6 -> {
                plugin.getDataManager().addRunes(player, 100L);
                for (CodexEntry entry : plugin.getCodexManager().allEntries()) {
                    if (!data.getCodex().contains(entry.id())) {
                        plugin.getCodexManager().givePage(player, entry.id());
                        break;
                    }
                }
            }
            default -> {
            }
        }
        Msg.send(player, "&7Come back tomorrow. Rea rewards those who return.");
        Msg.send(player, Msg.bar());
        plugin.getDataManager().save(data);
    }

    private void buy(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            return;
        }
        if (args.length < 2 || !args[1].equalsIgnoreCase("cache")) {
            Msg.send(player, "&e/rea buy cache [count] &7- " + plugin.getConfig().getInt("settings.cache-cost", 25)
                    + " runes each, +" + plugin.getConfig().getInt("settings.cache-xp", 40) + " XP each.");
            return;
        }
        int count = 1;
        if (args.length >= 3) {
            try {
                count = Math.max(1, Math.min(64, Integer.parseInt(args[2])));
            } catch (NumberFormatException e) {
                Msg.send(player, "&cCount must be a number.");
                return;
            }
        }
        long cost = (long) plugin.getConfig().getInt("settings.cache-cost", 25) * count;
        if (!plugin.getDataManager().takeRunes(player, cost)) {
            return;
        }
        Util.giveItem(player, plugin.getItemFactory().runeCache(count));
        Msg.send(player, "&dBought " + count + " Rune Cache" + (count > 1 ? "s" : "") + " for " + cost + " runes.");
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
        Msg.send(sender, "&aReset Rea data for " + target.getName() + ". Their bloodline sleeps again until /rea start.");
    }

    private void reload(CommandSender sender) {
        if (!sender.hasPermission("rea.admin")) {
            Msg.send(sender, "&cNo permission.");
            return;
        }
        plugin.reloadConfig();
        plugin.getCodexManager().reload();
        plugin.getQuestManager().reload();
        Msg.send(sender, "&aRea reloaded. The blood barely noticed.");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (!command.getName().equalsIgnoreCase("rea")) {
            return out;
        }
        if (args.length == 1) {
            for (String sub : List.of("help", "start", "race", "ability", "profile", "quests", "codex", "daily", "buy", "runes", "give", "reset", "reload")) {
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
                case "buy" -> {
                    if ("cache".startsWith(args[1].toLowerCase())) {
                        out.add("cache");
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
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("codex") && args[1].equalsIgnoreCase("read")) {
                for (CodexEntry entry : plugin.getCodexManager().allEntries()) {
                    if (entry.id().startsWith(args[2].toLowerCase())) {
                        out.add(entry.id());
                    }
                }
            }
        }
        return out;
    }
}
