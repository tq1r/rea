package dev.rea.race;

import dev.rea.ReaPlugin;
import dev.rea.data.PlayerData;
import dev.rea.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Deals bloodlines (/rea start), applies passive effects, and runs abilities.
 * Passives are potion effects (re-applied on join/respawn and every minute),
 * so they work identically for Java and Bedrock players.
 */
public final class RaceManager {
    private static final int LONG = Integer.MAX_VALUE;

    private final ReaPlugin plugin;
    private final Random random = new Random();
    private final Map<UUID, Long> abilityCooldowns = new ConcurrentHashMap<>();

    public RaceManager(ReaPlugin plugin) {
        this.plugin = plugin;
    }

    /** Deal a random bloodline to every online player who does not have one. */
    public void startSeason() {
        for (String line : plugin.getConfig().getStringList("messages.start-broadcast")) {
            Bukkit.broadcastMessage(Msg.c(line));
        }
        int dealt = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData data = plugin.getDataManager().get(player);
            if (data.getRace() == null) {
                assign(player, RaceType.values()[random.nextInt(RaceType.values().length)]);
                dealt++;
            }
        }
        Bukkit.broadcastMessage(Msg.c("&7The bloodlines have woken for &d" + dealt + " &7wanderers."));
        plugin.getLogger().info("Dealt bloodlines to " + dealt + " players.");
    }

    public void assign(Player player, RaceType type) {
        PlayerData data = plugin.getDataManager().get(player);
        data.setRace(type.getId());
        applyPassives(player);
        Msg.send(player, Msg.bar());
        Msg.send(player, type.getColor() + "&lThe bloodlines wake! You are a " + type.getDisplayName() + "!");
        for (String line : type.getLore()) {
            Msg.send(player, "&7" + line);
        }
        Msg.send(player, "&7Blood gift: &f" + type.getPassiveDescription());
        Msg.send(player, "&7Ability: &f" + type.getAbilityName() + " &7- " + type.getAbilityDescription());
        Msg.send(player, "&7Use &e/ability &7to call on your blood. It is permanent. It is yours.");
        Msg.send(player, Msg.bar());
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1f);
        player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation(), 40, 0.6, 1.0, 0.6);
        Bukkit.broadcastMessage(Msg.c("&7" + player.getName() + " wakes as a " + type.getColor() + type.getDisplayName() + "&7!"));
        data.incrementCounter("race", 1);
        plugin.getQuestManager().checkCompletion(player);
        plugin.getDataManager().save(data);
    }

    public void applyPassives(Player player) {
        PlayerData data = plugin.getDataManager().get(player);
        // Clear all bloodline effects first so re-applying never stacks.
        player.removePotionEffect(PotionEffectType.REGENERATION);
        player.removePotionEffect(PotionEffectType.SPEED);
        player.removePotionEffect(PotionEffectType.RESISTANCE);
        player.removePotionEffect(PotionEffectType.HASTE);
        player.removePotionEffect(PotionEffectType.LUCK);
        player.removePotionEffect(PotionEffectType.FIRE_RESISTANCE);
        player.removePotionEffect(PotionEffectType.HEALTH_BOOST);
        player.removePotionEffect(PotionEffectType.JUMP_BOOST);
        player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        player.removePotionEffect(PotionEffectType.SLOW_FALLING);
        player.removePotionEffect(PotionEffectType.STRENGTH);
        RaceType type = RaceType.fromId(data.getRace());
        if (type == null) {
            return;
        }
        switch (type) {
            case ELF -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, LONG, 0, false, false, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, LONG, 0, false, false, false));
            }
            case DWARF -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, LONG, 0, false, false, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, LONG, 0, false, false, false));
            }
            case HALFLING -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, LONG, 0, false, false, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, LONG, 0, false, false, false));
            }
            case TIEFLING -> player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, LONG, 0, false, false, false));
            case DRAGONBORN -> player.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, LONG, 0, false, false, false));
            case GNOME -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, LONG, 1, false, false, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, LONG, 0, false, false, false));
            }
            case AASIMAR -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, LONG, 0, false, false, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, LONG, 0, false, false, false));
            }
            case GOLIATH -> player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, LONG, 0, false, false, false));
            case HUMAN, ORC -> {
                // Human: XP bonus (in DataManager). Orc: melee bonus (in GameListener).
            }
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
        RaceType type = RaceType.fromId(data.getRace());
        if (type == null) {
            Msg.send(player, "&cYour bloodline still sleeps. An admin must run &e/rea start&c.");
            return;
        }
        long cooldownMs = plugin.getConfig().getLong("settings.ability-cooldown-seconds", 30L) * 1000L;
        long now = System.currentTimeMillis();
        Long last = abilityCooldowns.get(player.getUniqueId());
        if (last != null && now - last < cooldownMs) {
            long seconds = (cooldownMs - (now - last) + 999) / 1000;
            Msg.send(player, "&cYour blood is still catching its breath. &7(" + seconds + "s)");
            return;
        }
        abilityCooldowns.put(player.getUniqueId(), now);
        Location loc = player.getLocation();
        double power = 1.0 + data.getLevel() * 0.03;
        switch (type) {
            case HUMAN -> {
                heal(player, 4.0 * power);
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 160, 1, false, false, false));
                player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, loc, 30, 0.5, 0.8, 0.5);
                player.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
            }
            case ELF -> {
                Location dest = blink(player, 6);
                player.teleport(dest);
                player.getWorld().spawnParticle(Particle.END_ROD, loc, 30, 0.4, 0.8, 0.4);
                player.getWorld().spawnParticle(Particle.END_ROD, dest, 30, 0.4, 0.8, 0.4);
                player.playSound(dest, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
            }
            case DWARF -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 160, 1, false, false, false));
                player.getWorld().spawnParticle(Particle.CRIT, loc, 30, 0.5, 0.4, 0.5);
                player.playSound(loc, Sound.BLOCK_ANVIL_LAND, 0.7f, 1f);
            }
            case ORC -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 200, 1, false, false, false));
                player.getWorld().spawnParticle(Particle.CRIT, loc, 30, 0.5, 0.8, 0.5);
                player.playSound(loc, Sound.ENTITY_WITHER_SPAWN, 0.5f, 1f);
            }
            case HALFLING -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 120, 0, false, false, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 120, 1, false, false, false));
                player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, loc, 30, 0.5, 0.8, 0.5);
                player.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
            }
            case TIEFLING -> {
                for (Entity entity : player.getNearbyEntities(4, 3, 4)) {
                    if (entity instanceof LivingEntity living && !entity.equals(player)) {
                        living.damage(3.0 * power, player);
                        living.setFireTicks(60);
                    }
                }
                player.getWorld().spawnParticle(Particle.FLAME, loc, 40, 0.6, 0.6, 0.6);
                player.playSound(loc, Sound.ENTITY_BLAZE_SHOOT, 1f, 1f);
            }
            case DRAGONBORN -> {
                Vector forward = loc.getDirection().setY(0).normalize();
                for (Entity entity : player.getNearbyEntities(6, 4, 6)) {
                    if (!(entity instanceof LivingEntity living) || entity.equals(player)) {
                        continue;
                    }
                    Vector to = entity.getLocation().toVector().subtract(loc.toVector()).setY(0).normalize();
                    if (to.dot(forward) > 0.3) {
                        living.damage(4.0 * power, player);
                        living.setFireTicks(60);
                    }
                }
                player.getWorld().spawnParticle(Particle.FLAME, loc, 50, 0.8, 0.6, 0.8);
                player.playSound(loc, Sound.ENTITY_BLAZE_SHOOT, 1f, 1f);
            }
            case GNOME -> {
                ItemStack held = player.getInventory().getItemInMainHand();
                ItemMeta meta = held == null ? null : held.getItemMeta();
                if (meta instanceof Damageable damageable && damageable.getDamage() > 0) {
                    damageable.setDamage(0);
                    held.setItemMeta(meta);
                    player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, loc, 30, 0.5, 0.8, 0.5);
                    player.playSound(loc, Sound.BLOCK_ANVIL_USE, 1f, 1f);
                    Msg.send(player, "&bMend! &7The item is as good as new.");
                } else {
                    Msg.send(player, "&cNothing to mend. Hold a damaged tool or weapon.");
                }
            }
            case AASIMAR -> {
                heal(player, 6.0 * power);
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 160, 0, false, false, false));
                player.getWorld().spawnParticle(Particle.END_ROD, loc, 40, 0.6, 1.0, 0.6);
                player.playSound(loc, Sound.BLOCK_BEACON_ACTIVATE, 1f, 1f);
            }
            case GOLIATH -> {
                for (Entity entity : player.getNearbyEntities(5, 3, 5)) {
                    if (entity instanceof LivingEntity living && !entity.equals(player)) {
                        living.damage(4.0 * power, player);
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

    private void heal(Player player, double amount) {
        double missing = player.getMaxHealth() - player.getHealth();
        if (missing > 0) {
            player.heal(Math.min(amount, missing));
        }
    }

    /** Step forward block by block, stopping before solid ground. */
    private Location blink(Player player, int distance) {
        Location loc = player.getLocation();
        Vector dir = loc.getDirection().setY(0).normalize();
        Location dest = loc.clone();
        for (int i = 0; i < distance; i++) {
            Location next = dest.clone().add(dir);
            Block feet = next.getBlock();
            Block head = next.clone().add(0, 1, 0).getBlock();
            if (feet.isSolid() || head.isSolid()) {
                break;
            }
            dest = next;
        }
        return dest;
    }
}
