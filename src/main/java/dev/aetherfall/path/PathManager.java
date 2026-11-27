package dev.aetherfall.path;

import dev.aetherfall.AetherfallPlugin;
import dev.aetherfall.data.PlayerData;
import dev.aetherfall.util.Msg;
import dev.aetherfall.util.Util;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Path choosing, passive effects, and active abilities.
 * Passives are potion effects (re-applied on join/respawn and every minute),
 * so they work identically for Java and Bedrock players.
 */
public final class PathManager {
    private static final int LONG = Integer.MAX_VALUE;

    private final AetherfallPlugin plugin;
    private final Map<UUID, Long> abilityCooldowns = new ConcurrentHashMap<>();

    public PathManager(AetherfallPlugin plugin) {
        this.plugin = plugin;
    }

    public void choosePath(Player player, PathType type) {
        PlayerData data = plugin.getDataManager().get(player);
        if (type.getId().equals(data.getPath())) {
            Msg.send(player, "&7You already walk the " + type.getDisplayName() + ".");
            return;
        }
        if (data.getPath() != null) {
            long cost = plugin.getConfig().getLong("settings.path-change-cost", 50L);
            Msg.send(player, "&7The shards remember your first choice. Re-tuning costs &d" + cost + " shards&7.");
            if (!plugin.getDataManager().takeShards(player, cost)) {
                return;
            }
        }
        data.setPath(type.getId());
        applyPassives(player);
        Msg.send(player, Msg.bar());
        Msg.send(player, type.getColor() + "&lYou walk the " + type.getDisplayName() + ".");
        Msg.send(player, "&7" + type.getAbilityDescription());
        Msg.send(player, "&7Use &e/ability &7to call on your Path. The shards approve.");
        Msg.send(player, Msg.bar());
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1f);
        player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation(), 40, 0.6, 1.0, 0.6);
        data.incrementCounter("path", 1);
        plugin.getQuestManager().checkCompletion(player);
        plugin.getTitleManager().checkAll(player);
        plugin.getDataManager().save(data);
    }

    public void applyPassives(Player player) {
        PlayerData data = plugin.getDataManager().get(player);
        // Clear all Path effects first so switching Paths never stacks.
        player.removePotionEffect(PotionEffectType.FIRE_RESISTANCE);
        player.removePotionEffect(PotionEffectType.WATER_BREATHING);
        player.removePotionEffect(PotionEffectType.DOLPHINS_GRACE);
        player.removePotionEffect(PotionEffectType.REGENERATION);
        player.removePotionEffect(PotionEffectType.RESISTANCE);
        PathType type = PathType.fromId(data.getPath());
        if (type == null) {
            return;
        }
        switch (type) {
            case EMBER -> player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, LONG, 0, false, false, false));
            case TIDE -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, LONG, 0, false, false, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, LONG, 0, false, false, false));
            }
            case VERDANT -> player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, LONG, 0, false, false, false));
            case STONE -> player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, LONG, 0, false, false, false));
        }
    }

    public void startPassiveTask() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                applyPassives(player);
            }
        }, 1200L, 1200L);
    }

    public void useAbility(Player player) {
        PlayerData data = plugin.getDataManager().get(player);
        PathType type = PathType.fromId(data.getPath());
        if (type == null) {
            Msg.send(player, "&cYou have not chosen a Path yet. &e/aetherfall path");
            return;
        }
        long cooldownMs = plugin.getConfig().getLong("settings.ability-cooldown-seconds", 30L) * 1000L;
        long now = System.currentTimeMillis();
        Long last = abilityCooldowns.get(player.getUniqueId());
        if (last != null && now - last < cooldownMs) {
            long seconds = (cooldownMs - (now - last) + 999) / 1000;
            Msg.send(player, "&cYour Path is still catching its breath. &7(" + seconds + "s)");
            return;
        }
        abilityCooldowns.put(player.getUniqueId(), now);
        Location loc = player.getLocation();
        switch (type) {
            case EMBER -> {
                Vector dir = loc.getDirection().setY(0).normalize().multiply(1.5);
                dir.setY(0.7);
                player.setVelocity(dir);
                for (Entity entity : player.getNearbyEntities(4, 3, 4)) {
                    if (entity instanceof LivingEntity living && !entity.equals(player)) {
                        living.damage(3.0, player);
                        living.setFireTicks(60);
                    }
                }
                player.getWorld().spawnParticle(Particle.FLAME, loc, 40, 0.6, 0.6, 0.6);
                player.playSound(loc, Sound.ENTITY_BLAZE_SHOOT, 1f, 1f);
            }
            case TIDE -> {
                for (Entity entity : player.getNearbyEntities(6, 4, 6)) {
                    if (entity instanceof LivingEntity living && !entity.equals(player)) {
                        Vector to = loc.toVector().subtract(entity.getLocation().toVector()).normalize().multiply(1.2);
                        to.setY(0.4);
                        living.setVelocity(to);
                        living.damage(1.0, player);
                    }
                }
                player.getWorld().spawnParticle(Particle.SPLASH, loc, 50, 0.8, 0.8, 0.8);
                player.playSound(loc, Sound.ENTITY_PLAYER_SPLASH, 1f, 1f);
            }
            case VERDANT -> {
                double missing = player.getMaxHealth() - player.getHealth();
                if (missing > 0) {
                    player.heal(Math.min(4.0, missing));
                }
                int grown = Util.fertilize(loc, 5);
                player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, loc, 40, 0.8, 1.0, 0.8);
                player.playSound(loc, Sound.BLOCK_GRASS_BREAK, 1f, 1f);
                Msg.send(player, "&aBloom! &7Healed and " + grown + " crops flourished.");
            }
            case STONE -> {
                for (Entity entity : player.getNearbyEntities(5, 3, 5)) {
                    if (entity instanceof LivingEntity living && !entity.equals(player)) {
                        living.damage(4.0, player);
                        Vector away = entity.getLocation().toVector().subtract(loc.toVector()).normalize().multiply(1.5);
                        away.setY(0.8);
                        living.setVelocity(away);
                    }
                }
                player.getWorld().spawnParticle(Particle.CRIT, loc, 40, 0.8, 0.4, 0.8);
                player.playSound(loc, Sound.BLOCK_ANVIL_LAND, 0.7f, 1f);
            }
        }
        Msg.send(player, type.getColor() + type.getAbilityName() + "!");
    }
}
