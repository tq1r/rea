package dev.rea.listener;

import dev.rea.ReaPlugin;
import dev.rea.data.PlayerData;
import dev.rea.race.RaceType;
import dev.rea.skill.SkillDef;
import dev.rea.skill.SkillManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Core gameplay loop (Tensura canon): combat grants EP; devouring foes
 * (Predator holders only) teaches Common Skills; skills cost magicules.
 */
public final class GameListener implements Listener {

    private final ReaPlugin plugin;
    private final SkillManager skills;

    public GameListener(ReaPlugin plugin, SkillManager skills) {
        this.plugin = plugin;
        this.skills = skills;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerData data = plugin.getDataManager().get(player);
        if (data.getRaceId() == null) {
            return;
        }
        RaceType race = RaceType.fromId(data.getRaceId());
        if (race != null) {
            event.joinMessage(null);
            player.sendMessage(plugin.getConfigManager().color("&8[&bRea&8] &7Welcome back, "
                    + race.getColor() + race.formName(data.getForm()) + "&7."));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerData data = plugin.getDataManager().get(player);
        plugin.getDataManager().save(data);
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) {
            return;
        }
        PlayerData data = plugin.getDataManager().get(attacker);
        if (data.getRaceId() == null) {
            return;
        }
        if (data.hasSkill("starved")) {
            event.setDamage(event.getDamage() + 1.0);
        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        LivingEntity victim = event.getEntity();
        Player killer = victim.getKiller();
        if (killer == null) {
            return;
        }
        PlayerData data = plugin.getDataManager().get(killer);
        if (data.getRaceId() == null) {
            return;
        }
        data.addEp(10);
        plugin.getDataManager().save(data);

        // Predation: only Predator holders devour foes to learn Common Skills.
        if (data.hasSkill("predator")) {
            int chance = data.hasSkill("great_sage") ? 40 : 20;
            if (ThreadLocalRandom.current().nextInt(100) < chance) {
                for (SkillDef def : skills.commonSkills()) {
                    if (!data.hasSkill(def.getId())) {
                        data.addSkill(def.getId());
                        killer.sendMessage(plugin.getConfigManager().color(
                                "&b[Predation] &7You learned &f" + def.getName() + "&7."));
                        plugin.getDataManager().save(data);
                        break;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onRegain(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        PlayerData data = plugin.getDataManager().get(player);
        if (data.getRaceId() == null) {
            return;
        }
        if (data.hasSkill("divine_blessing")) {
            event.setAmount(event.getAmount() * 2.0);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        PlayerData data = plugin.getDataManager().get(player);
        plugin.getDataManager().save(data);
    }

    public void useSkill(Player player, String skillId) {
        PlayerData data = plugin.getDataManager().get(player);
        SkillDef def = skills.get(skillId);
        if (def == null || !skills.tryUse(player, data, def)) {
            return;
        }
        Location loc = player.getLocation();
        switch (def.getId()) {
            case "dissolve" -> {
                for (Entity e : player.getNearbyEntities(3, 3, 3)) {
                    if (e instanceof LivingEntity le) {
                        le.damage(3.0, player);
                    }
                }
                player.getWorld().spawnParticle(Particle.SPLASH, loc, 20, 1, 1, 1, 0.2);
            }
            case "coercion" -> {
                for (Entity e : player.getNearbyEntities(4, 4, 4)) {
                    if (e instanceof LivingEntity le) {
                        le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 1));
                        le.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 0));
                    }
                }
                player.getWorld().spawnParticle(Particle.SPLASH, loc, 20, 1, 1, 1, 0.2);
            }
            case "scale_armor", "stonehide" -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 160, 1));
                player.getWorld().spawnParticle(Particle.SPLASH, loc, 20, 1, 1, 1, 0.2);
            }
            case "strengthen" -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 200, 1));
                player.getWorld().spawnParticle(Particle.SPLASH, loc, 20, 1, 1, 1, 0.2);
            }
            case "second_wind" -> {
                player.setHealth(Math.min(20.0, player.getHealth() + 4.0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 160, 1));
                player.getWorld().spawnParticle(Particle.SPLASH, loc, 20, 1, 1, 1, 0.2);
            }
            case "elven_step" -> {
                Location dest = loc.clone().add(loc.getDirection().multiply(6));
                player.teleport(dest);
                player.getWorld().spawnParticle(Particle.SPLASH, dest, 20, 1, 1, 1, 0.2);
            }
            case "fang_rush" -> {
                player.setVelocity(loc.getDirection().multiply(2.0));
                for (Entity e : player.getNearbyEntities(2, 2, 2)) {
                    if (e instanceof LivingEntity le) {
                        le.damage(4.0, player);
                    }
                }
                player.getWorld().spawnParticle(Particle.SPLASH, loc, 20, 1, 1, 1, 0.2);
            }
            case "poison_breath" -> {
                for (Entity e : player.getNearbyEntities(4, 4, 4)) {
                    if (e instanceof LivingEntity le) {
                        le.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 0));
                    }
                }
                player.getWorld().spawnParticle(Particle.SPLASH, loc, 20, 1, 1, 1, 0.2);
            }
            case "hellfire" -> {
                for (Entity e : player.getNearbyEntities(4, 4, 4)) {
                    if (e instanceof LivingEntity le) {
                        le.setFireTicks(100);
                        le.damage(4.0, player);
                    }
                }
                player.getWorld().spawnParticle(Particle.SPLASH, loc, 20, 1, 1, 1, 0.2);
            }
            case "self_regeneration" -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 0));
                player.getWorld().spawnParticle(Particle.SPLASH, loc, 20, 1, 1, 1, 0.2);
            }
            case "water_blade" -> {
                Location target = loc.clone().add(loc.getDirection().multiply(6));
                for (Entity e : player.getNearbyEntities(8, 8, 8)) {
                    if (e instanceof LivingEntity le && le.getLocation().distance(target) < 2.0) {
                        le.damage(5.0, player);
                    }
                }
                player.getWorld().spawnParticle(Particle.SPLASH, target, 20, 1, 1, 1, 0.2);
            }
            case "voice_cannon" -> {
                for (Entity e : player.getNearbyEntities(5, 5, 5)) {
                    if (e instanceof LivingEntity le) {
                        le.damage(4.0, player);
                        le.setVelocity(loc.getDirection().multiply(1.5));
                    }
                }
                player.getWorld().spawnParticle(Particle.SPLASH, loc, 20, 1, 1, 1, 0.2);
            }
            case "telepathy" -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 0));
                player.getWorld().spawnParticle(Particle.SPLASH, loc, 20, 1, 1, 1, 0.2);
            }
            case "farsight" -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 400, 0));
                player.getWorld().spawnParticle(Particle.SPLASH, loc, 20, 1, 1, 1, 0.2);
            }
            case "hydraulic_propulsion" -> {
                player.setVelocity(player.getVelocity().setY(1.5));
                player.getWorld().spawnParticle(Particle.SPLASH, loc, 20, 1, 1, 1, 0.2);
            }
            case "infinity_prison" -> {
                for (Entity e : player.getNearbyEntities(5, 5, 5)) {
                    if (e instanceof LivingEntity le) {
                        le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 2));
                        le.getActivePotionEffects().forEach(pe -> {
                            if (pe.getType() != PotionEffectType.SLOWNESS) {
                                le.removePotionEffect(pe.getType());
                            }
                        });
                    }
                }
                player.getWorld().spawnParticle(Particle.SPLASH, loc, 20, 1, 1, 1, 0.2);
            }
            case "beelzebuth" -> {
                for (Entity e : player.getNearbyEntities(5, 5, 5)) {
                    if (e instanceof LivingEntity le) {
                        le.damage(8.0, player);
                    }
                }
                player.setHealth(Math.min(20.0, player.getHealth() + 6.0));
                player.getWorld().spawnParticle(Particle.SPLASH, loc, 40, 2, 2, 2, 0.3);
            }
            case "raphael" -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 400, 0));
                player.getWorld().spawnParticle(Particle.SPLASH, loc, 40, 2, 2, 2, 0.3);
            }
            case "uriel" -> {
                for (Entity e : player.getNearbyEntities(5, 5, 5)) {
                    if (e instanceof LivingEntity le) {
                        le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 200, 2));
                        le.damage(6.0, player);
                    }
                }
                player.getWorld().spawnParticle(Particle.SPLASH, loc, 40, 2, 2, 2, 0.3);
            }
            case "azathoth" -> {
                Location dest = loc.clone().add(loc.getDirection().multiply(8));
                player.teleport(dest);
                for (Entity e : player.getNearbyEntities(3, 3, 3)) {
                    if (e instanceof LivingEntity le) {
                        le.damage(6.0, player);
                    }
                }
                player.getWorld().spawnParticle(Particle.SPLASH, dest, 40, 2, 2, 2, 0.3);
            }
            default -> {
                player.getWorld().spawnParticle(Particle.SPLASH, loc, 10, 1, 1, 1, 0.1);
            }
        }
        plugin.getDataManager().save(data);
    }

    /** Passive effects applied on a timer (magicules, regen, passive skills). */
    public void tickPassives() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData data = plugin.getDataManager().get(player);
            if (data.getRaceId() == null) {
                continue;
            }
            data.setMagicules(Math.min(data.getMaxMagicules(), data.getMagicules() + 1.0));
            if (data.hasSkill("divine_blessing")) {
                player.setHealth(Math.min(20.0, player.getHealth() + 0.5));
            }
            if (data.hasSkill("godly_craftsman")) {
                // Crafting/smelt is handled by the skill's passive nature.
            }
            plugin.getDataManager().save(data);
        }
    }
}
