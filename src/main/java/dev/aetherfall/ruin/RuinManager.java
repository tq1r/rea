package dev.aetherfall.ruin;

import dev.aetherfall.AetherfallPlugin;
import dev.aetherfall.data.PlayerData;
import dev.aetherfall.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Ruins grant a Codex Page on first visit and can be revisited by teleport.
 * Defaults live in ruins.yml; admins can add more in-game with
 * /aetherfall ruin add &lt;name&gt; &lt;codexId&gt;.
 */
public final class RuinManager {
    private final AetherfallPlugin plugin;
    private final File file;
    private final List<Ruin> ruins = new ArrayList<>();

    public RuinManager(AetherfallPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "ruins.yml");
        load();
    }

    private void load() {
        if (!file.exists()) {
            plugin.saveResource("ruins.yml", false);
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (Map<?, ?> map : config.getMapList("ruins")) {
            ruins.add(new Ruin(
                    str(map, "name"),
                    str(map, "display"),
                    str(map, "world"),
                    num(map, "x"),
                    num(map, "y"),
                    num(map, "z"),
                    str(map, "codex"),
                    str(map, "hint")));
        }
        plugin.getLogger().info("Loaded " + ruins.size() + " ruins.");
    }

    public void reload() {
        ruins.clear();
        load();
    }

    private static String str(Map<?, ?> map, String key) {
        Object value = map.get(key);
        return value == null ? "" : value.toString();
    }

    private static int num(Map<?, ?> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(str(map, key));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public List<Ruin> allRuins() {
        return ruins;
    }

    public Ruin getRuin(String name) {
        for (Ruin ruin : ruins) {
            if (ruin.name().equalsIgnoreCase(name)) {
                return ruin;
            }
        }
        return null;
    }

    public void checkVisit(Player player) {
        PlayerData data = plugin.getDataManager().get(player);
        Location loc = player.getLocation();
        for (Ruin ruin : ruins) {
            if (!ruin.world().equals(loc.getWorld().getName())) {
                continue;
            }
            double dx = loc.getX() - ruin.x();
            double dy = loc.getY() - ruin.y();
            double dz = loc.getZ() - ruin.z();
            if (dx * dx + dy * dy + dz * dz > 64) {
                continue;
            }
            if (data.getRuins().contains(ruin.name())) {
                continue;
            }
            data.getRuins().add(ruin.name());
            Msg.send(player, Msg.bar());
            Msg.send(player, "&d&lYou found the " + ruin.display() + "!");
            Msg.send(player, "&7The old stones approve. Something is left for you here.");
            plugin.getCodexManager().givePage(player, ruin.codex());
            plugin.getDataManager().addShards(player, 30L);
            plugin.getDataManager().addXp(player, 25);
            player.playSound(loc, Sound.BLOCK_BEACON_ACTIVATE, 1f, 1f);
            player.getWorld().spawnParticle(Particle.END_ROD, loc, 40, 0.6, 1.0, 0.6);
            Msg.send(player, Msg.bar());
            plugin.getQuestManager().checkCompletion(player);
            plugin.getTitleManager().checkAll(player);
            plugin.getDataManager().save(data);
        }
    }

    public void list(Player player) {
        PlayerData data = plugin.getDataManager().get(player);
        Msg.send(player, Msg.bar());
        Msg.send(player, "&d&lRuins of the Vale &7(" + data.getRuins().size() + "/" + ruins.size() + ")");
        for (Ruin ruin : ruins) {
            if (data.getRuins().contains(ruin.name())) {
                Msg.send(player, "&a[visited] &f" + ruin.display() + " &7- &e/aetherfall ruin tp " + ruin.name());
            } else {
                Msg.send(player, "&7[???] &7" + ruin.hint());
            }
        }
        Msg.send(player, Msg.bar());
    }

    public void teleport(Player player, String name) {
        Ruin ruin = getRuin(name);
        if (ruin == null) {
            Msg.send(player, "&cNo ruin called '" + name + "'. See &e/aetherfall ruins");
            return;
        }
        PlayerData data = plugin.getDataManager().get(player);
        if (!data.getRuins().contains(ruin.name())) {
            Msg.send(player, "&cYou have not found this ruin yet. &7" + ruin.hint());
            return;
        }
        World world = Bukkit.getWorld(ruin.world());
        if (world == null) {
            Msg.send(player, "&cThat ruin's world is not loaded.");
            return;
        }
        long cost = plugin.getConfig().getLong("settings.ruin-tp-cost", 10L);
        if (!plugin.getDataManager().takeShards(player, cost)) {
            return;
        }
        player.teleport(new Location(world, ruin.x() + 0.5, ruin.y(), ruin.z() + 0.5));
        Msg.send(player, "&7The shards carry you to the &d" + ruin.display() + "&7. &d-" + cost + " shards");
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
    }

    public void addRuin(Player admin, String name, String codexId) {
        if (getRuin(name) != null) {
            Msg.send(admin, "&cA ruin with that name already exists.");
            return;
        }
        if (plugin.getCodexManager().getEntry(codexId) == null) {
            Msg.send(admin, "&cUnknown Codex entry '" + codexId + "'. See codex.yml for valid ids.");
            return;
        }
        Location loc = admin.getLocation();
        Ruin ruin = new Ruin(name, name, loc.getWorld().getName(),
                loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), codexId, "Somewhere in the Vale.");
        ruins.add(ruin);
        save();
        Msg.send(admin, "&aRuin '" + name + "' added at " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
    }

    private void save() {
        YamlConfiguration config = new YamlConfiguration();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Ruin ruin : ruins) {
            Map<String, Object> map = new java.util.LinkedHashMap<>();
            map.put("name", ruin.name());
            map.put("display", ruin.display());
            map.put("world", ruin.world());
            map.put("x", ruin.x());
            map.put("y", ruin.y());
            map.put("z", ruin.z());
            map.put("codex", ruin.codex());
            map.put("hint", ruin.hint());
            list.add(map);
        }
        config.set("ruins", list);
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save ruins: " + e.getMessage());
        }
    }
}
