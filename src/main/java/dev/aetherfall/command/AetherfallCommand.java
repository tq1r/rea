package dev.aetherfall.command;

import dev.aetherfall.AetherfallPlugin;
import dev.aetherfall.codex.CodexEntry;
import dev.aetherfall.data.PlayerData;
import dev.aetherfall.event.EventType;
import dev.aetherfall.path.PathType;
import dev.aetherfall.quest.QuestDef;
import dev.aetherfall.relic.RelicType;
import dev.aetherfall.ruin.Ruin;
import dev.aetherfall.title.TitleDef;
import dev.aetherfall.util.Msg;
import dev.aetherfall.util.Util;
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
import java.util.Arrays;
import java.util.List;

/**
 * Every Aetherfall command in one place. Plain text throughout, so Bedrock
 * players (Geyser) get the full game with the same commands as Java players.
 */
public final class AetherfallCommand implements CommandExecutor, TabCompleter {
    private final AetherfallPlugin plugin;

    public AetherfallCommand(AetherfallPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String name = command.getName().toLowerCase();
        if (name.equals("ability")) {
            if (sender instanceof Player player) {
                plugin.getPathManager().useAbility(player);
            }
            return true;
        }
        if (name.equals("shards")) {
            if (sender instanceof Player player) {
                PlayerData data = plugin.getDataManager().get(player);
                Msg.send(player, "&dAether Shards: &l" + data.getShards() + " &7(lifetime: " + data.getLifetimeEarned() + ")");
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
            case "path" -> path(sender, args);
            case "ability" -> {
                if (sender instanceof Player player) {
                    plugin.getPathManager().useAbility(player);
                }
            }
            case "profile" -> profile(sender);
            case "quests" -> quests(sender);
            case "codex" -> codex(sender, args);
            case "bounties" -> {
                if (sender instanceof Player player) {
                    plugin.getBountyManager().list(player);
                }
            }
            case "ruins" -> {
                if (sender instanceof Player player) {
                    plugin.getGuiManager().openRuins(player);
                }
            }
            case "ruin" -> ruin(sender, args);
            case "order" -> order(sender, args);
            case "titles" -> {
                if (sender instanceof Player player) {
                    plugin.getGuiManager().openTitles(player);
                }
            }
            case "title" -> title(sender, args);
            case "daily" -> daily(sender);
            case "trader" -> {
                if (sender instanceof Player player) {
                    plugin.getGuiManager().openTrader(player);
                }
            }
            case "shards" -> {
                if (sender instanceof Player player) {
                    PlayerData data = plugin.getDataManager().get(player);
                    Msg.send(player, "&dAether Shards: &l" + data.getShards() + " &7(lifetime: " + data.getLifetimeEarned() + ")");
                }
            }
            case "event" -> event(sender, args);
            case "relic" -> relic(sender, args);
            case "give" -> give(sender, args);
            case "reset" -> reset(sender, args);
            case "reload" -> reload(sender);
            default -> {
                Msg.send(sender, "&cUnknown subcommand. &e/aetherfall help");
            }
        }
        return true;
    }

    private void help(CommandSender sender) {
        Msg.send(sender, Msg.bar());
        Msg.send(sender, "&d&lAetherfall &7- the shards remember.");
        Msg.send(sender, "&e/aetherfall path &7- choose or view your Path");
        Msg.send(sender, "&e/aetherfall ability &7- use your Path ability");
        Msg.send(sender, "&e/aetherfall profile &7- your Shardbearer profile");
        Msg.send(sender, "&e/aetherfall quests &7- the story so far");
        Msg.send(sender, "&e/aetherfall codex [read <id>] &7- lore you have collected");
        Msg.send(sender, "&e/aetherfall bounties &7- today's contracts");
        Msg.send(sender, "&e/aetherfall ruins &7- ruins of the Vale");
        Msg.send(sender, "&e/aetherfall order <sub> &7- Orders (guilds)");
        Msg.send(sender, "&e/aetherfall titles &7- titles you have earned");
        Msg.send(sender, "&e/aetherfall daily &7- daily shard gift");
        Msg.send(sender, "&e/aetherfall trader &7- spend shards");
        Msg.send(sender, "&e/aetherfall shards &7- your balance");
        Msg.send(sender, Msg.bar());
    }

    private void path(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            return;
        }
        if (args.length >= 3 && args[1].equalsIgnoreCase("choose")) {
            PathType type = PathType.fromId(args[2]);
            if (type == null) {
                Msg.send(player, "&cPaths: ember, tide, verdant, stone. &e/aetherfall path choose <path>");
                return;
            }
            plugin.getPathManager().choosePath(player, type);
            return;
        }
        if (args.length >= 2 && args[1].equalsIgnoreCase("ability")) {
            plugin.getPathManager().useAbility(player);
            return;
        }
        PlayerData data = plugin.getDataManager().get(player);
        PathType current = PathType.fromId(data.getPath());
        Msg.send(player, Msg.bar());
        Msg.send(player, "&d&lYour Path: " + (current == null ? "&7None yet" : current.getColor() + current.getDisplayName()));
        for (PathType type : PathType.values()) {
            String mark = type == current ? "&d[walking]" : "&7[ ]";
            Msg.send(player, mark + " " + type.getColor() + type.getDisplayName() + " &7- " + type.getAbilityName());
        }
        Msg.send(player, "&7Change with &e/aetherfall path choose <ember|tide|verdant|stone>");
        Msg.send(player, Msg.bar());
        plugin.getGuiManager().openPathMenu(player);
    }

    private void profile(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            return;
        }
        PlayerData data = plugin.getDataManager().get(player);
        PathType path = PathType.fromId(data.getPath());
        Msg.send(sender, Msg.bar());
        Msg.send(sender, "&d&l" + player.getName() + " &7- &d" + plugin.getTitleManager().selectedDisplay(data));
        Msg.send(sender, "&7Path: " + (path == null ? "None yet" : path.getColor() + path.getDisplayName()));
        Msg.send(sender, "&7Level: &d" + data.getLevel() + " &7(" + data.getXp() + "/" + plugin.getDataManager().xpForLevel(data.getLevel()) + " XP)");
        Msg.send(sender, "&7Shards: &d" + data.getShards() + " &7(lifetime: " + data.getLifetimeEarned() + ")");
        Msg.send(sender, "&7Codex: &d" + data.getCodex().size() + "/" + plugin.getCodexManager().total()
                + " &7Quests: &d" + data.getQuestsDone().size() + "/" + plugin.getQuestManager().allQuests().size()
                + " &7Ruins: &d" + data.getRuins().size() + "/" + plugin.getRuinManager().allRuins().size());
        Msg.send(sender, "&7Kills: &d" + data.getKills() + " &7Mined: &d" + data.getMines() + " &7Crafted: &d" + data.getCrafts());
        Msg.send(sender, "&7Order: &d" + (data.getOrder() == null ? "None" : data.getOrder())
                + " &7Daily streak: &d" + data.getDailyStreak());
        Msg.send(sender, Msg.bar());
    }

    private void quests(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            return;
        }
        PlayerData data = plugin.getDataManager().get(player);
        Msg.send(sender, Msg.bar());
        Msg.send(sender, "&d&lThe Story of the Vale &7(" + data.getQuestsDone().size() + "/" + plugin.getQuestManager().allQuests().size() + " quests)");
        QuestDef current = plugin.getQuestManager().currentQuest(data);
        if (current == null) {
            Msg.send(sender, "&7The Song is almost sung. More chapters are coming.");
        } else {
            int progress = plugin.getQuestManager().progress(data, current);
            Msg.send(sender, "&dChapter " + current.chapter() + ": &l" + current.name());
            Msg.send(sender, "&7" + current.description());
            Msg.send(sender, "&7Progress: &e" + Math.min(progress, current.amount()) + "/" + current.amount());
            Msg.send(sender, "&7Rewards: &d" + current.rewardShards() + " shards"
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
        Msg.send(player, "&d&lCodex of the Vale &7(" + data.getCodex().size() + "/" + plugin.getCodexManager().total() + ")");
        for (CodexEntry entry : plugin.getCodexManager().allEntries()) {
            if (data.getCodex().contains(entry.id())) {
                Msg.send(player, "&a[read] &f" + entry.title() + " &7- &e/aetherfall codex read " + entry.id());
            } else {
                Msg.send(player, "&7[???] &7" + entry.subtitle());
            }
        }
        Msg.send(player, Msg.bar());
    }

    private void ruin(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            return;
        }
        if (args.length >= 3 && args[1].equalsIgnoreCase("tp")) {
            plugin.getRuinManager().teleport(player, args[2]);
            return;
        }
        if (args.length >= 4 && args[1].equalsIgnoreCase("add")) {
            if (!sender.hasPermission("aetherfall.admin")) {
                Msg.send(sender, "&cNo permission.");
                return;
            }
            plugin.getRuinManager().addRuin(player, args[2], args[3]);
            return;
        }
        plugin.getRuinManager().list(player);
    }

    private void order(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            return;
        }
        if (args.length < 2) {
            Msg.send(player, "&e/aetherfall order <create|invite|accept|deny|leave|kick|disband|sethome|home|info|list>");
            return;
        }
        switch (args[1].toLowerCase()) {
            case "create" -> {
                if (args.length < 3) {
                    Msg.send(player, "&e/aetherfall order create <name>");
                } else {
                    plugin.getOrderManager().create(player, args[2]);
                }
            }
            case "invite" -> {
                if (args.length < 3) {
                    Msg.send(player, "&e/aetherfall order invite <player>");
                } else {
                    plugin.getOrderManager().invite(player, args[2]);
                }
            }
            case "accept" -> plugin.getOrderManager().accept(player);
            case "deny" -> plugin.getOrderManager().deny(player);
            case "leave" -> plugin.getOrderManager().leave(player);
            case "kick" -> {
                if (args.length < 3) {
                    Msg.send(player, "&e/aetherfall order kick <player>");
                } else {
                    plugin.getOrderManager().kick(player, args[2]);
                }
            }
            case "disband" -> plugin.getOrderManager().disband(player);
            case "sethome" -> plugin.getOrderManager().setHome(player);
            case "home" -> plugin.getOrderManager().home(player);
            case "info" -> plugin.getOrderManager().info(player);
            case "list" -> plugin.getOrderManager().list(player);
            default -> Msg.send(player, "&e/aetherfall order <create|invite|accept|deny|leave|kick|disband|sethome|home|info|list>");
        }
    }

    private void title(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            return;
        }
        if (args.length < 2) {
            plugin.getTitleManager().list(player);
            return;
        }
        plugin.getTitleManager().select(player, args[1]);
    }

    private void daily(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            return;
        }
        PlayerData data = plugin.getDataManager().get(player);
        String today = LocalDate.now().toString();
        String yesterday = LocalDate.now().minusDays(1).toString();
        if (today.equals(data.getLastDaily())) {
            Msg.send(player, "&cYou already claimed today's gift. The shards will hum again tomorrow.");
            return;
        }
        int streak = yesterday.equals(data.getLastDaily()) ? data.getDailyStreak() + 1 : 1;
        data.setDailyStreak(streak);
        data.setLastDaily(today);
        Msg.send(player, Msg.bar());
        Msg.send(player, "&d&lDay " + streak + " &7- the shards hum a little louder today.");
        switch ((streak - 1) % 7) {
            case 0 -> plugin.getDataManager().addShards(player, 20L);
            case 1 -> {
                plugin.getDataManager().addShards(player, 30L);
                Util.giveItem(player, new ItemStack(Material.BREAD, 4));
            }
            case 2 -> Util.giveItem(player, plugin.getItemFactory().shardCache(1));
            case 3 -> plugin.getDataManager().addShards(player, 50L);
            case 4 -> {
                plugin.getDataManager().addShards(player, 60L);
                Util.giveItem(player, new ItemStack(Material.GOLDEN_APPLE, 1));
            }
            case 5 -> Util.giveItem(player, plugin.getItemFactory().shardCache(2));
            case 6 -> {
                plugin.getDataManager().addShards(player, 100L);
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
        Msg.send(player, "&7Come back tomorrow. The Vale rewards those who return.");
        Msg.send(player, Msg.bar());
        plugin.getDataManager().save(data);
    }

    private void event(CommandSender sender, String[] args) {
        if (args.length >= 2 && args[1].equalsIgnoreCase("start")) {
            if (!sender.hasPermission("aetherfall.admin")) {
                Msg.send(sender, "&cNo permission.");
                return;
            }
            if (args.length < 3) {
                Msg.send(sender, "&e/aetherfall event start <shard_storm|blood_moon|meteor|veil>");
                return;
            }
            EventType type = EventType.fromId(args[2]);
            if (type == null) {
                Msg.send(sender, "&cUnknown event. Try shard_storm, blood_moon, meteor, veil.");
                return;
            }
            plugin.getEventManager().startEvent(type);
            return;
        }
        if (args.length >= 2 && args[1].equalsIgnoreCase("stop")) {
            if (!sender.hasPermission("aetherfall.admin")) {
                Msg.send(sender, "&cNo permission.");
                return;
            }
            plugin.getEventManager().stopEvent(true);
            return;
        }
        EventType active = plugin.getEventManager().getActive();
        if (active == null) {
            Msg.send(sender, "&7The Vale is quiet. No world event right now.");
        } else {
            Msg.send(sender, "&7Current event: " + active.getColor() + "&l" + active.getDisplayName());
        }
    }

    private void relic(CommandSender sender, String[] args) {
        if (!sender.hasPermission("aetherfall.admin")) {
            Msg.send(sender, "&cNo permission.");
            return;
        }
        if (args.length < 4 || !args[1].equalsIgnoreCase("give")) {
            Msg.send(sender, "&e/aetherfall relic give <id> <player>");
            return;
        }
        RelicType type = RelicType.fromId(args[2]);
        Player target = Bukkit.getPlayer(args[3]);
        if (type == null || target == null) {
            Msg.send(sender, "&cRelics: emberheart, tidecaller, verdant_staff, warden_edge, crown_fragment.");
            return;
        }
        plugin.getRelicManager().giveRelic(target, type);
        Msg.send(sender, "&aGave " + type.getDisplayName() + " to " + target.getName());
    }

    private void give(CommandSender sender, String[] args) {
        if (!sender.hasPermission("aetherfall.admin")) {
            Msg.send(sender, "&cNo permission.");
            return;
        }
        if (args.length < 4 || !args[1].equalsIgnoreCase("shards")) {
            Msg.send(sender, "&e/aetherfall give shards <player> <amount>");
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
        plugin.getDataManager().addShards(target, amount);
        Msg.send(sender, "&aGave " + amount + " shards to " + target.getName());
    }

    private void reset(CommandSender sender, String[] args) {
        if (!sender.hasPermission("aetherfall.admin")) {
            Msg.send(sender, "&cNo permission.");
            return;
        }
        if (args.length < 2) {
            Msg.send(sender, "&e/aetherfall reset <player>");
            return;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            Msg.send(sender, "&cThat player is not online.");
            return;
        }
        plugin.getDataManager().delete(target.getUniqueId());
        Msg.send(sender, "&aReset Aetherfall data for " + target.getName() + ". They will start fresh on next join.");
    }

    private void reload(CommandSender sender) {
        if (!sender.hasPermission("aetherfall.admin")) {
            Msg.send(sender, "&cNo permission.");
            return;
        }
        plugin.reloadConfig();
        plugin.getCodexManager().reload();
        plugin.getQuestManager().reload();
        plugin.getRuinManager().reload();
        Msg.send(sender, "&aAetherfall reloaded. The shards barely noticed.");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (!command.getName().equalsIgnoreCase("aetherfall")) {
            return out;
        }
        if (args.length == 1) {
            for (String sub : List.of("help", "path", "ability", "profile", "quests", "codex", "bounties", "ruins", "ruin", "order", "titles", "title", "daily", "trader", "shards", "event", "relic", "give", "reset", "reload")) {
                if (sub.startsWith(args[0].toLowerCase())) {
                    out.add(sub);
                }
            }
        } else if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "path" -> {
                    for (String s : List.of("choose", "ability")) {
                        if (s.startsWith(args[1].toLowerCase())) {
                            out.add(s);
                        }
                    }
                }
                case "codex" -> {
                    if ("read".startsWith(args[1].toLowerCase())) {
                        out.add("read");
                    }
                }
                case "ruin" -> {
                    for (String s : List.of("tp", "add")) {
                        if (s.startsWith(args[1].toLowerCase())) {
                            out.add(s);
                        }
                    }
                }
                case "order" -> {
                    for (String s : List.of("create", "invite", "accept", "deny", "leave", "kick", "disband", "sethome", "home", "info", "list")) {
                        if (s.startsWith(args[1].toLowerCase())) {
                            out.add(s);
                        }
                    }
                }
                case "event" -> {
                    for (String s : List.of("status", "start", "stop")) {
                        if (s.startsWith(args[1].toLowerCase())) {
                            out.add(s);
                        }
                    }
                }
                default -> {
                }
            }
        } else if (args.length == 3) {
            switch (args[0].toLowerCase()) {
                case "path" -> {
                    for (PathType type : PathType.values()) {
                        if (type.getId().startsWith(args[2].toLowerCase())) {
                            out.add(type.getId());
                        }
                    }
                }
                case "codex" -> {
                    if (args[1].equalsIgnoreCase("read")) {
                        for (CodexEntry entry : plugin.getCodexManager().allEntries()) {
                            if (entry.id().startsWith(args[2].toLowerCase())) {
                                out.add(entry.id());
                            }
                        }
                    }
                }
                case "ruin" -> {
                    if (args[1].equalsIgnoreCase("tp")) {
                        for (Ruin ruin : plugin.getRuinManager().allRuins()) {
                            if (ruin.name().startsWith(args[2].toLowerCase())) {
                                out.add(ruin.name());
                            }
                        }
                    }
                }
                case "title" -> {
                    for (TitleDef def : plugin.getTitleManager().allTitles()) {
                        if (def.id().startsWith(args[2].toLowerCase())) {
                            out.add(def.id());
                        }
                    }
                }
                case "event" -> {
                    if (args[1].equalsIgnoreCase("start")) {
                        for (EventType type : EventType.values()) {
                            if (type.getId().startsWith(args[2].toLowerCase())) {
                                out.add(type.getId());
                            }
                        }
                    }
                }
                case "relic" -> {
                    if (args[1].equalsIgnoreCase("give")) {
                        for (RelicType type : RelicType.values()) {
                            if (type.getId().startsWith(args[2].toLowerCase())) {
                                out.add(type.getId());
                            }
                        }
                    }
                }
                default -> {
                }
            }
        }
        return out;
    }
}
