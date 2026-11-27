package dev.aetherfall.order;

import dev.aetherfall.AetherfallPlugin;
import dev.aetherfall.data.PlayerData;
import dev.aetherfall.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Orders: friend groups with a shared home. All chat-based, so Bedrock
 * players are full members. Stored in data/orders.yml.
 */
public final class OrderManager {
    private static final int MAX_MEMBERS = 8;

    private final AetherfallPlugin plugin;
    private final File file;
    private final Map<String, Order> orders = new HashMap<>();
    private final Map<UUID, String> invites = new HashMap<>();

    public OrderManager(AetherfallPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "data/orders.yml");
        load();
    }

    private void load() {
        if (!file.exists()) {
            return;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = config.getConfigurationSection("orders");
        if (section == null) {
            return;
        }
        for (String name : section.getKeys(false)) {
            ConfigurationSection entry = section.getConfigurationSection(name);
            if (entry == null) {
                continue;
            }
            UUID leader;
            try {
                leader = UUID.fromString(entry.getString("leader", ""));
            } catch (IllegalArgumentException e) {
                continue;
            }
            Order order = new Order(name, leader);
            order.getMembers().clear();
            for (String member : entry.getStringList("members")) {
                try {
                    order.getMembers().add(UUID.fromString(member));
                } catch (IllegalArgumentException ignored) {
                }
            }
            if (!order.isMember(leader)) {
                order.getMembers().add(0, leader);
            }
            String homeWorld = entry.getString("homeWorld", null);
            if (homeWorld != null) {
                order.setHome(homeWorld, entry.getInt("homeX", 0), entry.getInt("homeY", 64), entry.getInt("homeZ", 0));
            }
            orders.put(name, order);
        }
    }

    private void save() {
        YamlConfiguration config = new YamlConfiguration();
        for (Order order : orders.values()) {
            String prefix = "orders." + order.getName() + ".";
            config.set(prefix + "leader", order.getLeader().toString());
            config.set(prefix + "members", order.getMembers().stream().map(UUID::toString).toList());
            if (order.hasHome()) {
                config.set(prefix + "homeWorld", order.getHomeWorld());
                config.set(prefix + "homeX", order.getHomeX());
                config.set(prefix + "homeY", order.getHomeY());
                config.set(prefix + "homeZ", order.getHomeZ());
            }
        }
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save orders: " + e.getMessage());
        }
    }

    public Order getOrder(PlayerData data) {
        return data.getOrder() == null ? null : orders.get(data.getOrder());
    }

    public void create(Player player, String name) {
        PlayerData data = plugin.getDataManager().get(player);
        if (data.getOrder() != null) {
            Msg.send(player, "&cYou are already in the Order &d" + data.getOrder() + "&c. Leave it first.");
            return;
        }
        if (!name.matches("[A-Za-z0-9_]{3,16}")) {
            Msg.send(player, "&cOrder names must be 3-16 letters, numbers, or underscores.");
            return;
        }
        for (String existing : orders.keySet()) {
            if (existing.equalsIgnoreCase(name)) {
                Msg.send(player, "&cThat Order name is taken.");
                return;
            }
        }
        long cost = plugin.getConfig().getLong("settings.order-create-cost", 100L);
        if (!plugin.getDataManager().takeShards(player, cost)) {
            return;
        }
        Order order = new Order(name, player.getUniqueId());
        orders.put(name, order);
        data.setOrder(name);
        Msg.send(player, "&d&lOrder founded: &l" + name + " &7(-" + cost + " shards)");
        Msg.send(player, "&7Invite friends with &e/aetherfall order invite <player>");
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1f);
        save();
        plugin.getDataManager().save(data);
    }

    public void invite(Player player, String targetName) {
        PlayerData data = plugin.getDataManager().get(player);
        Order order = getOrder(data);
        if (order == null) {
            Msg.send(player, "&cYou are not in an Order. &e/aetherfall order create <name>");
            return;
        }
        if (!order.isLeader(player.getUniqueId())) {
            Msg.send(player, "&cOnly the Order leader can invite.");
            return;
        }
        if (order.getMembers().size() >= MAX_MEMBERS) {
            Msg.send(player, "&cYour Order is full (" + MAX_MEMBERS + ").");
            return;
        }
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            Msg.send(player, "&cThat player is not online.");
            return;
        }
        PlayerData targetData = plugin.getDataManager().get(target);
        if (targetData.getOrder() != null) {
            Msg.send(player, "&c" + target.getName() + " is already in an Order.");
            return;
        }
        invites.put(target.getUniqueId(), order.getName());
        Msg.send(player, "&7Invited &f" + target.getName() + " &7to &d" + order.getName());
        Msg.send(target, "&d" + player.getName() + " invites you to join the Order &l" + order.getName());
        Msg.send(target, "&7Accept with &e/aetherfall order accept &7or decline with &e/aetherfall order deny");
    }

    public void accept(Player player) {
        String orderName = invites.remove(player.getUniqueId());
        if (orderName == null) {
            Msg.send(player, "&cYou have no pending Order invite.");
            return;
        }
        Order order = orders.get(orderName);
        PlayerData data = plugin.getDataManager().get(player);
        if (order == null || data.getOrder() != null) {
            Msg.send(player, "&cThat invite is no longer valid.");
            return;
        }
        order.getMembers().add(player.getUniqueId());
        data.setOrder(orderName);
        Msg.send(player, "&dWelcome to the Order &l" + orderName + "!");
        Bukkit.broadcastMessage(Msg.c("&d" + player.getName() + " joined the Order &l" + orderName));
        save();
        plugin.getDataManager().save(data);
    }

    public void deny(Player player) {
        if (invites.remove(player.getUniqueId()) == null) {
            Msg.send(player, "&cYou have no pending Order invite.");
        } else {
            Msg.send(player, "&7Invite declined.");
        }
    }

    public void leave(Player player) {
        PlayerData data = plugin.getDataManager().get(player);
        Order order = getOrder(data);
        if (order == null) {
            Msg.send(player, "&cYou are not in an Order.");
            return;
        }
        if (order.isLeader(player.getUniqueId())) {
            Msg.send(player, "&cYou lead this Order. &e/aetherfall order disband &7to dissolve it.");
            return;
        }
        order.getMembers().remove(player.getUniqueId());
        data.setOrder(null);
        Msg.send(player, "&7You left the Order &d" + order.getName());
        save();
        plugin.getDataManager().save(data);
    }

    public void kick(Player player, String targetName) {
        PlayerData data = plugin.getDataManager().get(player);
        Order order = getOrder(data);
        if (order == null || !order.isLeader(player.getUniqueId())) {
            Msg.send(player, "&cOnly the Order leader can kick members.");
            return;
        }
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            Msg.send(player, "&cThat player is not online.");
            return;
        }
        if (!order.getMembers().remove(target.getUniqueId())) {
            Msg.send(player, "&cThat player is not in your Order.");
            return;
        }
        PlayerData targetData = plugin.getDataManager().get(target);
        targetData.setOrder(null);
        Msg.send(target, "&cYou were removed from the Order &d" + order.getName());
        Msg.send(player, "&7Removed &f" + target.getName());
        save();
        plugin.getDataManager().save(data);
        plugin.getDataManager().save(targetData);
    }

    public void disband(Player player) {
        PlayerData data = plugin.getDataManager().get(player);
        Order order = getOrder(data);
        if (order == null || !order.isLeader(player.getUniqueId())) {
            Msg.send(player, "&cOnly the Order leader can disband it.");
            return;
        }
        for (UUID member : order.getMembers()) {
            Player online = Bukkit.getPlayer(member);
            if (online != null) {
                PlayerData memberData = plugin.getDataManager().get(online);
                memberData.setOrder(null);
                plugin.getDataManager().save(memberData);
                Msg.send(online, "&cThe Order &d" + order.getName() + " &chas been disbanded.");
            }
        }
        orders.remove(order.getName());
        data.setOrder(null);
        save();
        plugin.getDataManager().save(data);
    }

    public void setHome(Player player) {
        PlayerData data = plugin.getDataManager().get(player);
        Order order = getOrder(data);
        if (order == null) {
            Msg.send(player, "&cYou are not in an Order.");
            return;
        }
        Location loc = player.getLocation();
        order.setHome(loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        Msg.send(player, "&aOrder home set at " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
        save();
    }

    public void home(Player player) {
        PlayerData data = plugin.getDataManager().get(player);
        Order order = getOrder(data);
        if (order == null) {
            Msg.send(player, "&cYou are not in an Order.");
            return;
        }
        if (!order.hasHome()) {
            Msg.send(player, "&cYour Order has no home yet. &e/aetherfall order sethome");
            return;
        }
        World world = Bukkit.getWorld(order.getHomeWorld());
        if (world == null) {
            Msg.send(player, "&cYour Order home's world is not loaded.");
            return;
        }
        long cost = plugin.getConfig().getLong("settings.order-home-cost", 10L);
        if (!plugin.getDataManager().takeShards(player, cost)) {
            return;
        }
        player.teleport(new Location(world, order.getHomeX() + 0.5, order.getHomeY(), order.getHomeZ() + 0.5));
        Msg.send(player, "&7The shards carry you home to &d" + order.getName() + "&7. &d-" + cost + " shards");
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
    }

    public void info(Player player) {
        PlayerData data = plugin.getDataManager().get(player);
        Order order = getOrder(data);
        Msg.send(player, Msg.bar());
        if (order == null) {
            Msg.send(player, "&7You are not in an Order. &e/aetherfall order create <name>");
            Msg.send(player, Msg.bar());
            return;
        }
        Msg.send(player, "&d&lOrder: &l" + order.getName());
        for (UUID member : order.getMembers()) {
            String name = member.toString();
            Player online = Bukkit.getPlayer(member);
            if (online != null) {
                name = online.getName();
            } else {
                PlayerData memberData = plugin.getDataManager().get(member, name);
                name = memberData.getName();
            }
            Msg.send(player, "&7- &f" + name + (member.equals(order.getLeader()) ? " &d(leader)" : ""));
        }
        Msg.send(player, "&7Home: " + (order.hasHome() ? order.getHomeX() + ", " + order.getHomeY() + ", " + order.getHomeZ() : "none"));
        Msg.send(player, Msg.bar());
    }

    public void list(Player player) {
        Msg.send(player, Msg.bar());
        Msg.send(player, "&d&lOrders of the Vale");
        if (orders.isEmpty()) {
            Msg.send(player, "&7None yet. Be the first wardens.");
        }
        for (Order order : orders.values()) {
            Msg.send(player, "&7- &d" + order.getName() + " &7(" + order.getMembers().size() + " members)");
        }
        Msg.send(player, Msg.bar());
    }
}
