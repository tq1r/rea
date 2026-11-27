package dev.rea.skill;

import dev.rea.ReaPlugin;
import dev.rea.data.PlayerData;
import dev.rea.race.RaceType;
import dev.rea.util.Msg;
import dev.rea.util.Util;
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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tensura-style skills. Intrinsic skills come with your race. Common skills
 * are learned by predation (killing the monster that carries them). Unique
 * skills come from deeds and evolution. Ultimate skills evolve from Unique
 * skills at Demon Lord. Actives cost magicules and have cooldowns.
 */
public final class SkillManager {
    private final ReaPlugin plugin;
    private final Map<String, SkillDef> skills = new LinkedHashMap<>();
    private final Map<UUID, Map<String, Long>> cooldowns = new ConcurrentHashMap<>();

    public SkillManager(ReaPlugin plugin) {
        this.plugin = plugin;
        register(new SkillDef("predator", "Predator", "UNIQUE", 0, 0,
                "Passive: +25% EP and +10% skill-learn chance from predation.",
                "Slay 10 monsters."));
        register(new SkillDef("great_sage", "Great Sage", "UNIQUE", 0, 0,
                "Passive: +25% XP from everything.",
                "Record 3 Codex entries."));
        register(new SkillDef("magic_sense", "Magic Sense", "UNIQUE", 0, 0,
                "Passive: magicules regenerate 50% faster.",
                "Complete Chapter I of the story."));
        register(new SkillDef("beelzebuth", "Beelzebuth", "UNIQUE", 80, 30,
                "Devour: massive damage and heal.",
                "Evolve to your Apex form."));
        register(new SkillDef("lord_of_darkness", "Lord of Darkness", "ULTIMATE", 150, 45,
                "Ultimate: a devouring abyss. Massive damage, heal, and fire.",
                "Evolve to Demon Lord with Beelzebuth."));
        register(new SkillDef("truth", "Truth", "ULTIMATE", 0, 0,
                "Passive: +50% XP and +25% EP from everything.",
                "Evolve to Demon Lord with Great Sage."));
        register(new SkillDef("steel_thread", "Steel Thread", "COMMON", 15, 15,
                "Trap: slow nearby foes.",
                "Predate a Spider."));
        register(new SkillDef("bone_arrow", "Bone Arrow", "COMMON", 15, 10,
                "Pierce the nearest foe for heavy damage.",
                "Predate a Skeleton."));
        register(new SkillDef("flesh_shield", "Flesh Shield", "COMMON", 20, 20,
                "Harden: Resistance for 8s.",
                "Predate a Zombie."));
        register(new SkillDef("explosion", "Explosion", "COMMON", 30, 20,
                "Blast: damage and knockback in a radius.",
                "Predate a Creeper."));
        register(new SkillDef("teleport", "Teleport", "COMMON", 25, 15,
                "Blink forward 8 blocks.",
                "Predate an Enderman."));
        register(new SkillDef("flame_breath", "Flame Breath", "COMMON", 30, 20,
                "Breathe fire in a cone.",
                "Predate a Blaze."));
        register(new SkillDef("poison_cloud", "Poison Cloud", "COMMON", 20, 20,
                "Poison nearby foes.",
                "Predate a Witch."));
        register(new SkillDef("absorb", "Absorb", "COMMON", 20, 20,
                "Absorb: heal 4 HP.",
                "Predate a Slime."));
    }

    private void register(SkillDef def) {
        skills.put(def.id(), def);
    }

    public SkillDef getSkill(String id) {
        return skills.get(id);
    }

    public List<SkillDef> allSkills() {
        return new ArrayList<>(skills.values());
    }

    public boolean hasSkill(Player player, String id) {
        return plugin.getDataManager().get(player).getSkills().contains(id);
    }

    public void learn(Player player, String id) {
        SkillDef def = getSkill(id);
        if (def == null) {
            return;
        }
        PlayerData data = plugin.getDataManager().get(player);
        if (data.getSkills().contains(id)) {
            return;
        }
        data.getSkills().add(id);
        Msg.send(player, Msg.bar());
        Msg.send(player, "&d&lSkill learned: &l" + def.name() + " &7(" + def.tier() + ")");
        Msg.send(player, "&7" + def.description());
        Msg.send(player, Msg.bar());
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1f);
        plugin.getDataManager().save(data);
    }

    public void useSkill(Player player, String id) {
        PlayerData data = plugin.getDataManager().get(player);
        SkillDef def = getSkill(id);
        if (def == null) {
            Msg.send(player, "&cUnknown skill '" + id + "'. &e/rea skills");
            return;
        }
        if (!data.getSkills().contains(id)) {
            Msg.send(player, "&cYou have not learned &l" + def.name() + "&c yet. &7" + def.learnHint());
            return;
        }
        if (def.cost() > 0) {
            if (data.getMagicules() < def.cost()) {
                Msg.send(player, "&cNot enough magicules. &7(" + data.getMagicules() + "/" + plugin.getDataManager().maxMagicules(data) + ")");
                return;
            }
            long now = System.currentTimeMillis();
            Map<String, Long> mine = cooldowns.computeIfAbsent(player.getUniqueId(), uuid -> new ConcurrentHashMap<>());
            Long last = mine.get(id);
            long cooldownMs = def.cooldownSeconds() * 1000L;
            if (last != null && now - last < cooldownMs) {
                long seconds = (cooldownMs - (now - last) + 999) / 1000;
                Msg.send(player, "&c" + def.name() + " is still gathering. &7(" + seconds + "s)");
                return;
            }
            mine.put(id, now);
            data.setMagicules(data.getMagicules() - def.cost());
        }
        execute(player, id, data);
        Msg.send(player, "&d" + def.name() + "!");
    }

    private void execute(Player player, String id, PlayerData data) {
        Location loc = player.getLocation();
        double power = 1.0 + data.getLevel() * 0.03 + data.getForm() * 0.25;
        switch (id) {
            case "dissolve" -> {
                for (Entity entity : player.getNearbyEntities(3, 3, 3)) {
                    if (entity instanceof LivingEntity living && !entity.equals(player)) {
                        living.damage(3.0 * power, player);
                    }
                }
                player.getWorld().spawnParticle(Particle.WITCH, loc, 30, 0.5, 0.5, 0.5);
                player.playSound(loc, Sound.ENTITY_SLIME_SQUISH, 1f, 1f);
            }
            case "sneak" -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 120, 0, false, false, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 120, 1, false, false, false));
                player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, loc, 20, 0.5, 0.8, 0.5);
                player.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
            }
            case "tail_slash" -> {
                for (Entity entity : player.getNearbyEntities(4, 3, 4)) {
                    if (entity instanceof LivingEntity living && !entity.equals(player)) {
                        living.damage(3.0 * power, player);
                        Vector away = entity.getLocation().toVector().subtract(loc.toVector()).normalize().multiply(1.2);
                        away.setY(0.6);
                        living.setVelocity(away);
                    }
                }
                player.getWorld().spawnParticle(Particle.CRIT, loc, 30, 0.6, 0.4, 0.6);
                player.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 1f);
            }
            case "bloodrage" -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 200, 1, false, false, false));
                player.getWorld().spawnParticle(Particle.CRIT, loc, 30, 0.5, 0.8, 0.5);
                player.playSound(loc, Sound.ENTITY_WITHER_SPAWN, 0.5f, 1f);
            }
            case "second_wind" -> {
                heal(player, 4.0 * power);
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 160, 1, false, false, false));
                player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, loc, 30, 0.5, 0.8, 0.5);
                player.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
            }
            case "elven_step" -> {
                Location dest = blink(player, 6);
                player.teleport(dest);
                player.getWorld().spawnParticle(Particle.END_ROD, loc, 30, 0.4, 0.8, 0.4);
                player.getWorld().spawnParticle(Particle.END_ROD, dest, 30, 0.4, 0.8, 0.4);
                player.playSound(dest, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
            }
            case "stonehide" -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 160, 1, false, false, false));
                player.getWorld().spawnParticle(Particle.CRIT, loc, 30, 0.5, 0.4, 0.5);
                player.playSound(loc, Sound.BLOCK_ANVIL_LAND, 0.7f, 1f);
            }
            case "fang_rush" -> {
                Vector dir = loc.getDirection().setY(0).normalize().multiply(1.6);
                dir.setY(0.6);
                player.setVelocity(dir);
                for (Entity entity : player.getNearbyEntities(4, 3, 4)) {
                    if (entity instanceof LivingEntity living && !entity.equals(player)) {
                        living.damage(3.0 * power, player);
                    }
                }
                player.getWorld().spawnParticle(Particle.CRIT, loc, 30, 0.5, 0.5, 0.5);
                player.playSound(loc, Sound.ENTITY_WOLF_PANT, 1f, 1f);
            }
            case "venom_bite" -> {
                for (Entity entity : player.getNearbyEntities(4, 3, 4)) {
                    if (entity instanceof LivingEntity living && !entity.equals(player)) {
                        living.damage(2.0 * power, player);
                        living.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 0, false, false, false));
                    }
                }
                player.getWorld().spawnParticle(Particle.WITCH, loc, 30, 0.5, 0.5, 0.5);
                player.playSound(loc, Sound.ENTITY_SPIDER_HURT, 1f, 1f);
            }
            case "hellfire" -> {
                for (Entity entity : player.getNearbyEntities(4, 3, 4)) {
                    if (entity instanceof LivingEntity living && !entity.equals(player)) {
                        living.damage(3.0 * power, player);
                        living.setFireTicks(60);
                    }
                }
                player.getWorld().spawnParticle(Particle.FLAME, loc, 40, 0.6, 0.6, 0.6);
                player.playSound(loc, Sound.ENTITY_BLAZE_SHOOT, 1f, 1f);
            }
            case "steel_thread" -> {
                for (Entity entity : player.getNearbyEntities(5, 3, 5)) {
                    if (entity instanceof LivingEntity living && !entity.equals(player)) {
                        living.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 1, false, false, false));
                    }
                }
                player.getWorld().spawnParticle(Particle.CRIT, loc, 30, 0.6, 0.4, 0.6);
                player.playSound(loc, Sound.ENTITY_SPIDER_HURT, 1f, 1f);
            }
            case "bone_arrow" -> {
                Entity nearest = null;
                double best = 36.0;
                for (Entity entity : player.getNearbyEntities(6, 6, 6)) {
                    if (entity instanceof LivingEntity && !entity.equals(player)) {
                        double dist = entity.getLocation().distanceSquared(loc);
                        if (dist < best) {
                            best = dist;
                            nearest = entity;
                        }
                    }
                }
                if (nearest instanceof LivingEntity living) {
                    living.damage(4.0 * power, player);
                    player.getWorld().spawnParticle(Particle.CRIT, living.getLocation(), 20, 0.3, 0.5, 0.3);
                    player.playSound(loc, Sound.ENTITY_SKELETON_SHOOT, 1f, 1f);
                } else {
                    Msg.send(player, "&7No foe in range.");
                }
            }
            case "flesh_shield" -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 160, 0, false, false, false));
                player.getWorld().spawnParticle(Particle.CRIT, loc, 30, 0.5, 0.4, 0.5);
                player.playSound(loc, Sound.BLOCK_ANVIL_LAND, 0.7f, 1f);
            }
            case "explosion" -> {
                for (Entity entity : player.getNearbyEntities(5, 4, 5)) {
                    if (entity instanceof LivingEntity living && !entity.equals(player)) {
                        living.damage(5.0 * power, player);
                        Vector away = entity.getLocation().toVector().subtract(loc.toVector()).normalize().multiply(1.4);
                        away.setY(0.7);
                        living.setVelocity(away);
                    }
                }
                player.getWorld().spawnParticle(Particle.EXPLOSION, loc, 20, 0.8, 0.8, 0.8);
                player.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1f, 1f);
            }
            case "teleport" -> {
                Location dest = blink(player, 8);
                player.teleport(dest);
                player.getWorld().spawnParticle(Particle.PORTAL, loc, 30, 0.4, 0.8, 0.4);
                player.getWorld().spawnParticle(Particle.PORTAL, dest, 30, 0.4, 0.8, 0.4);
                player.playSound(dest, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
            }
            case "flame_breath" -> {
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
            case "poison_cloud" -> {
                for (Entity entity : player.getNearbyEntities(4, 3, 4)) {
                    if (entity instanceof LivingEntity living && !entity.equals(player)) {
                        living.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 0, false, false, false));
                    }
                }
                player.getWorld().spawnParticle(Particle.WITCH, loc, 40, 0.6, 0.6, 0.6);
                player.playSound(loc, Sound.ENTITY_WITCH_DRINK, 1f, 1f);
            }
            case "absorb" -> {
                heal(player, 4.0 * power);
                player.getWorld().spawnParticle(Particle.WITCH, loc, 30, 0.5, 0.8, 0.5);
                player.playSound(loc, Sound.ENTITY_SLIME_SQUISH, 1f, 1f);
            }
            case "beelzebuth" -> {
                for (Entity entity : player.getNearbyEntities(6, 4, 6)) {
                    if (entity instanceof LivingEntity living && !entity.equals(player)) {
                        living.damage(8.0 * power, player);
                    }
                }
                heal(player, 4.0 * power);
                player.getWorld().spawnParticle(Particle.WITCH, loc, 50, 0.8, 0.8, 0.8);
                player.playSound(loc, Sound.ENTITY_WITHER_SPAWN, 0.7f, 1f);
            }
            case "lord_of_darkness" -> {
                for (Entity entity : player.getNearbyEntities(7, 5, 7)) {
                    if (entity instanceof LivingEntity living && !entity.equals(player)) {
                        living.damage(12.0 * power, player);
                        living.setFireTicks(100);
                    }
                }
                heal(player, 8.0 * power);
                player.getWorld().spawnParticle(Particle.WITCH, loc, 80, 1.0, 1.0, 1.0);
                player.playSound(loc, Sound.ENTITY_WITHER_SPAWN, 1f, 1f);
            }
            default -> {
            }
        }
    }

    private void heal(Player player, double amount) {
        double missing = player.getMaxHealth() - player.getHealth();
        if (missing > 0) {
            player.heal(Math.min(amount, missing));
        }
    }

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

    public void list(Player player) {
        PlayerData data = plugin.getDataManager().get(player);
        RaceType race = RaceType.fromId(data.getRace());
        Msg.send(player, Msg.bar());
        Msg.send(player, "&d&lSkills &7(" + data.getSkills().size() + " learned)");
        if (race != null) {
            Msg.send(player, "&7Intrinsic: &f" + race.getIntrinsicName() + " &7- " + race.getIntrinsicDescription());
        }
        for (SkillDef def : skills.values()) {
            boolean owned = data.getSkills().contains(def.id());
            String mark = owned ? "&a[learned]" : "&7[???]";
            Msg.send(player, mark + " &f" + def.name() + " &7(" + def.tier() + ") &7- "
                    + (owned ? def.description() : def.learnHint()));
        }
        Msg.send(player, "&7Use &e/rea skill <id> &7to activate. Magicules: &d" + data.getMagicules()
                + "/" + plugin.getDataManager().maxMagicules(data));
        Msg.send(player, Msg.bar());
    }
}
