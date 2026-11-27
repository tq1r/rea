package dev.rea.command;

import dev.rea.ReaPlugin;
import dev.rea.data.PlayerData;
import dev.rea.race.RaceManager;
import dev.rea.race.RaceType;
import dev.rea.skill.SkillDef;
import dev.rea.skill.SkillManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * /rea — main command. Subcommands: start, status, evolve, skill, skills, admin.
 */
public final class ReaCommand implements CommandExecutor, TabCompleter {

    private final ReaPlugin plugin;
    private final RaceManager races;
    private final SkillManager skills;

    public ReaCommand(ReaPlugin plugin, RaceManager races, SkillManager skills) {
        this.plugin = plugin;
        this.races = races;
        this.skills = skills;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(plugin.getConfigManager().color("&8[&bRea&8] &7Commands: /rea start, /rea status, /rea evolve, /rea skill <id>, /rea skills"));
            return true;
        }
        String sub = args[0].toLowerCase();
        switch (sub) {
            case "start" -> start(sender);
            case "status" -> status(sender);
            case "evolve" -> evolve(sender);
            case "skill" -> {
                if (args.length < 2) {
                    sender.sendMessage(plugin.getConfigManager().color("&8[&bRea&8] &7Usage: /rea skill <id>"));
                    return true;
                }
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("Players only.");
                    return true;
                }
                plugin.getGameListener().useSkill(player, args[1]);
            }
            case "skills" -> skills(sender);
            case "admin" -> admin(sender, args);
            default -> sender.sendMessage(plugin.getConfigManager().color("&8[&bRea&8] &7Unknown subcommand."));
        }
        return true;
    }

    private void start(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return;
        }
        PlayerData data = plugin.getDataManager().get(player);
        if (data.getRaceId() != null) {
            sender.sendMessage(plugin.getConfigManager().color("&8[&bRea&8] &7You are already reincarnated as a "
                    + RaceType.fromId(data.getRaceId()).getDisplayName() + "."));
            return;
        }
        RaceType[] types = RaceType.values();
        RaceType race = types[(int) (Math.random() * types.length)];
        data.setRaceId(race.getId());
        data.setForm(0);
        data.setEp(0);
        data.setLevel(1);
        data.setRunes(0);
        data.setMagicules(50);
        data.setMaxMagicules(100);
        data.addSkill(race.getIntrinsicId());
        // Otherworlders receive a Unique Skill on reincarnation (Slimes: Predator).
        SkillDef unique = skills.grantUniqueSkill(race);
        data.addSkill(unique.getId());
        data.setUniqueSkillId(unique.getId());
        plugin.getDataManager().save(data);

        sender.sendMessage(plugin.getConfigManager().color("&8[&bRea&8] &7The world turns. You are reincarnated as a "
                + race.getColor() + race.getDisplayName() + "&7."));
        sender.sendMessage(plugin.getConfigManager().color("&8[&bRea&8] &7Intrinsic Skill: &f" + race.getIntrinsicName()
                + " &8- &7" + race.getIntrinsicDescription()));
        sender.sendMessage(plugin.getConfigManager().color("&8[&bRea&8] &7Unique Skill: &b" + unique.getName()
                + " &8- &7" + unique.getDescription()));
        sender.sendMessage(plugin.getConfigManager().color("&8[&bRea&8] &7Gain EP by fighting. Evolve with /rea evolve."));
    }

    private void status(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return;
        }
        PlayerData data = plugin.getDataManager().get(player);
        if (data.getRaceId() == null) {
            sender.sendMessage(plugin.getConfigManager().color("&8[&bRea&8] &7You have not reincarnated yet. Use /rea start."));
            return;
        }
        RaceType race = RaceType.fromId(data.getRaceId());
        sender.sendMessage(plugin.getConfigManager().color("&8[&bRea&8] &7Race: " + race.getColor() + race.formName(data.getForm())));
        sender.sendMessage(plugin.getConfigManager().color("&8[&bRea&8] &7EP: &f" + data.getEp() + " &8| &7Level: &f" + data.getLevel()
                + " &8| &7Runes: &f" + data.getRunes()));
        sender.sendMessage(plugin.getConfigManager().color("&8[&bRea&8] &7Magicules: &f" + (int) data.getMagicules() + "/" + (int) data.getMaxMagicules()));
        sender.sendMessage(plugin.getConfigManager().color("&8[&bRea&8] &7Skills: &f" + String.join(", ", data.getSkills())));
    }

    private void evolve(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return;
        }
        PlayerData data = plugin.getDataManager().get(player);
        if (data.getRaceId() == null) {
            sender.sendMessage(plugin.getConfigManager().color("&8[&bRea&8] &7You have not reincarnated yet. Use /rea start."));
            return;
        }
        if (!races.canEvolve(data)) {
            sender.sendMessage(plugin.getConfigManager().color("&8[&bRea&8] &7You cannot evolve yet. Need more EP, level, or runes."));
            return;
        }
        RaceType race = RaceType.fromId(data.getRaceId());
        int before = data.getForm();
        if (races.evolve(player, data)) {
            sender.sendMessage(plugin.getConfigManager().color("&8[&bRea&8] &7You evolved into a "
                    + race.getColor() + race.formName(before + 1) + "&7!"));
            if (before + 1 >= races.maxStage()) {
                SkillDef ultimate = skills.get(data.getUniqueSkillId());
                if (ultimate != null && ultimate.getTier() == SkillDef.Tier.ULTIMATE) {
                    sender.sendMessage(plugin.getConfigManager().color("&8[&bRea&8] &7Demon Lord awakening! Your Unique Skill evolved into the Ultimate Skill &b"
                            + ultimate.getName() + "&7!"));
                }
            }
        }
    }

    private void skills(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return;
        }
        PlayerData data = plugin.getDataManager().get(player);
        if (data.getRaceId() == null) {
            sender.sendMessage(plugin.getConfigManager().color("&8[&bRea&8] &7You have not reincarnated yet. Use /rea start."));
            return;
        }
        sender.sendMessage(plugin.getConfigManager().color("&8[&bRea&8] &7Your skills:"));
        for (String id : data.getSkills()) {
            SkillDef def = skills.get(id);
            if (def != null) {
                sender.sendMessage(plugin.getConfigManager().color(" &8- &f" + def.getName() + " &8(&7" + def.getTier() + "&8) &7" + def.getDescription()));
            }
        }
    }

    private void admin(CommandSender sender, String[] args) {
        if (!sender.hasPermission("rea.admin")) {
            sender.sendMessage(plugin.getConfigManager().color("&8[&bRea&8] &7No permission."));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(plugin.getConfigManager().color("&8[&bRea&8] &7Usage: /rea admin <give|set|reset> ..."));
            return;
        }
        String action = args[1].toLowerCase();
        if ("reset".equals(action) && args.length >= 3) {
            Player target = Bukkit.getPlayerExact(args[2]);
            if (target == null) {
                sender.sendMessage(plugin.getConfigManager().color("&8[&bRea&8] &7Player not found."));
                return;
            }
            plugin.getDataManager().reset(target);
            sender.sendMessage(plugin.getConfigManager().color("&8[&bRea&8] &7Reset " + target.getName() + "."));
            return;
        }
        if (args.length < 4) {
            sender.sendMessage(plugin.getConfigManager().color("&8[&bRea&8] &7Usage: /rea admin <give|set> <player> <ep|level|runes|magicules> <amount>"));
            return;
        }
        Player target = Bukkit.getPlayerExact(args[2]);
        if (target == null) {
            sender.sendMessage(plugin.getConfigManager().color("&8[&bRea&8] &7Player not found."));
            return;
        }
        PlayerData data = plugin.getDataManager().get(target);
        int amount;
        try {
            amount = Integer.parseInt(args[4]);
        } catch (NumberFormatException e) {
            sender.sendMessage(plugin.getConfigManager().color("&8[&bRea&8] &7Amount must be a number."));
            return;
        }
        switch (args[3].toLowerCase()) {
            case "ep" -> {
                if ("give".equals(action)) {
                    data.addEp(amount);
                } else {
                    data.setEp(amount);
                }
            }
            case "level" -> {
                if ("give".equals(action)) {
                    data.setLevel(data.getLevel() + amount);
                } else {
                    data.setLevel(amount);
                }
            }
            case "runes" -> {
                if ("give".equals(action)) {
                    data.setRunes(data.getRunes() + amount);
                } else {
                    data.setRunes(amount);
                }
            }
            case "magicules" -> {
                if ("give".equals(action)) {
                    data.setMagicules(data.getMagicules() + amount);
                } else {
                    data.setMagicules(amount);
                }
            }
            default -> sender.sendMessage(plugin.getConfigManager().color("&8[&bRea&8] &7Unknown stat."));
        }
        plugin.getDataManager().save(data);
        sender.sendMessage(plugin.getConfigManager().color("&8[&bRea&8] &7Done."));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("start", "status", "evolve", "skill", "skills", "admin");
        }
        if (args.length == 2 && "skill".equalsIgnoreCase(args[0])) {
            return new ArrayList<>(skills.all().keySet());
        }
        return List.of();
    }
}
