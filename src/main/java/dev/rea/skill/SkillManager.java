package dev.rea.skill;

import dev.rea.ReaPlugin;
import dev.rea.data.PlayerData;
import dev.rea.race.RaceType;
import dev.rea.util.Msg;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Skill registry and learning (Tensura canon):
 * - INTRINSIC skills come with the race.
 * - COMMON skills are learned by devouring foes (predation, Predator holders).
 * - UNIQUE skills are granted on reincarnation, born of desire.
 * - ULTIMATE skills evolve from a Unique Skill at Demon Lord awakening.
 */
public final class SkillManager {

    private final ReaPlugin plugin;
    private final Map<String, SkillDef> skills = new LinkedHashMap<>();

    public SkillManager(ReaPlugin plugin) {
        this.plugin = plugin;
        register(new SkillDef("dissolve", "Dissolve", SkillDef.Tier.INTRINSIC,
                "Acid melts nearby foes.", 5, 10_000, "slime"));
        register(new SkillDef("absorb", "Absorb", SkillDef.Tier.INTRINSIC,
                "Absorb matter into your body (Slime intrinsic).", 3, 8_000, "slime"));
        register(new SkillDef("coercion", "Coercion", SkillDef.Tier.INTRINSIC,
                "Fear nearby foes (slow + weaken).", 5, 10_000, "goblin"));
        register(new SkillDef("scale_armor", "Scale Armor", SkillDef.Tier.INTRINSIC,
                "Harden: Resistance II for 8s.", 5, 10_000, "lizardman"));
        register(new SkillDef("strengthen", "Strengthen", SkillDef.Tier.INTRINSIC,
                "Strength II for 10s.", 5, 10_000, "orc"));
        register(new SkillDef("second_wind", "Second Wind", SkillDef.Tier.INTRINSIC,
                "Heal 4 HP and sprint (Speed II, 8s).", 5, 10_000, "human"));
        register(new SkillDef("elven_step", "Elven Step", SkillDef.Tier.INTRINSIC,
                "Blink forward 6 blocks.", 5, 10_000, "elf"));
        register(new SkillDef("stonehide", "Stonehide", SkillDef.Tier.INTRINSIC,
                "Resistance II for 8s.", 5, 10_000, "dwarf"));
        register(new SkillDef("fang_rush", "Fang Rush", SkillDef.Tier.INTRINSIC,
                "Dash forward, biting nearby foes.", 5, 10_000, "dire_wolf"));
        register(new SkillDef("poison_breath", "Poison Breath", SkillDef.Tier.INTRINSIC,
                "Poison nearby foes.", 5, 10_000, "serpent"));
        register(new SkillDef("hellfire", "Hellfire", SkillDef.Tier.INTRINSIC,
                "Burn and scorch nearby foes.", 5, 10_000, "demon"));
        register(new SkillDef("self_regeneration", "Self-Regeneration", SkillDef.Tier.COMMON,
                "Regenerate 6 HP over 10s.", 8, 30_000, null));
        register(new SkillDef("water_blade", "Water Blade", SkillDef.Tier.COMMON,
                "Launch a water blade at a foe.", 6, 5_000, null));
        register(new SkillDef("voice_cannon", "Voice Cannon", SkillDef.Tier.COMMON,
                "Blast foes with a sonic wave.", 8, 8_000, null));
        register(new SkillDef("telepathy", "Telepathy", SkillDef.Tier.COMMON,
                "Speed II for 10s.", 4, 20_000, null));
        register(new SkillDef("farsight", "Farsight", SkillDef.Tier.COMMON,
                "Night Vision for 20s.", 4, 20_000, null));
        register(new SkillDef("hydraulic_propulsion", "Hydraulic Propulsion", SkillDef.Tier.COMMON,
                "Launch yourself upward.", 6, 8_000, null));
        register(new SkillDef("magic_sense", "Magic Sense", SkillDef.Tier.COMMON,
                "Magicules flow faster; sense the world.", 0, 0, null));
        register(new SkillDef("predator", "Predator", SkillDef.Tier.UNIQUE,
                "Devour foes to learn their Common Skills and gain EP.", 0, 0, null));
        register(new SkillDef("great_sage", "Great Sage", SkillDef.Tier.UNIQUE,
                "Learn skills faster; devouring teaches twice as often.", 0, 0, null));
        register(new SkillDef("divine_blessing", "Divine Blessing", SkillDef.Tier.UNIQUE,
                "Regenerate 1 HP every 2s.", 0, 0, null));
        register(new SkillDef("infinity_prison", "Infinity Prison", SkillDef.Tier.UNIQUE,
                "Trap foes: slow them and strip their effects.", 10, 15_000, null));
        register(new SkillDef("starved", "Starved", SkillDef.Tier.UNIQUE,
                "Hunger for power: +1 damage per foe slain (stacks).", 0, 0, null));
        register(new SkillDef("godly_craftsman", "Godly Craftsman", SkillDef.Tier.UNIQUE,
                "Crafting and smelting are instant and free.", 0, 0, null));
        register(new SkillDef("truth", "Truth", SkillDef.Tier.UNIQUE,
                "See the world as it is. EP and XP flow faster.", 0, 0, null));
        register(new SkillDef("beelzebuth", "Beelzebuth", SkillDef.Tier.ULTIMATE,
                "Lord of Gluttony: devour everything, heal, and strike hard.", 20, 20_000, null));
        register(new SkillDef("raphael", "Raphael", SkillDef.Tier.ULTIMATE,
                "Lord of Wisdom: everything flows to you.", 20, 20_000, null));
        register(new SkillDef("uriel", "Uriel", SkillDef.Tier.ULTIMATE,
                "Lord of Vows: trap and strip foes, then strike.", 20, 20_000, null));
        register(new SkillDef("azathoth", "Azathoth", SkillDef.Tier.ULTIMATE,
                "God of the Void: blink and cut through foes.", 20, 20_000, null));
        register(new SkillDef("veldora", "Veldora", SkillDef.Tier.ULTIMATE,
                "Lord of Storms: call the storm down on your foes.", 20, 20_000, null));
        register(new SkillDef("faust", "Faust", SkillDef.Tier.ULTIMATE,
                "Lord of Investigation: see everything, know everything.", 20, 20_000, null));
        register(new SkillDef("mammon", "Mammon", SkillDef.Tier.ULTIMATE,
                "Lord of Greed: take everything, want everything.", 20, 20_000, null));
        register(new SkillDef("lucifer", "Lucifer", SkillDef.Tier.ULTIMATE,
                "Lord of Pride: none stand above you.", 20, 20_000, null));
        register(new SkillDef("satanael", "Satanael", SkillDef.Tier.ULTIMATE,
                "Lord of Wrath: burn with righteous fury.", 20, 20_000, null));
        register(new SkillDef("amaterasu", "Amaterasu", SkillDef.Tier.ULTIMATE,
                "Lord of Shimmering Flame: set the world alight.", 20, 20_000, null));
        register(new SkillDef("hastur", "Hastur", SkillDef.Tier.ULTIMATE,
                "Lord of Starwind: ride the starwind.", 20, 20_000, null));
        register(new SkillDef("mephisto", "Mephisto", SkillDef.Tier.ULTIMATE,
                "Lord of Fantasy: weave illusion and dream.", 20, 20_000, null));
        register(new SkillDef("michael", "Michael", SkillDef.Tier.ULTIMATE,
                "Lord of Justice: judgment falls on the wicked.", 20, 20_000, null));
        register(new SkillDef("samael", "Samael", SkillDef.Tier.ULTIMATE,
                "Lord of Deathly Poison: poison that kills gods.", 20, 20_000, null));
        register(new SkillDef("abaddon", "Abaddon", SkillDef.Tier.ULTIMATE,
                "Lord of Destruction: unmake everything.", 20, 20_000, null));
        register(new SkillDef("belial", "Belial", SkillDef.Tier.ULTIMATE,
                "Lord of Underworld: the dead serve you.", 20, 20_000, null));
        register(new SkillDef("asmodeus", "Asmodeus", SkillDef.Tier.ULTIMATE,
                "Lord of Lust: desire made manifest.", 20, 20_000, null));
        register(new SkillDef("sariel", "Sariel", SkillDef.Tier.ULTIMATE,
                "Lord of Hope: light in the darkest hour.", 20, 20_000, null));
        register(new SkillDef("yog_sothoth", "Yog-Sothoth", SkillDef.Tier.ULTIMATE,
                "Lord of Space-Time: bend time and space.", 20, 20_000, null));
        register(new SkillDef("azazel", "Azazel", SkillDef.Tier.ULTIMATE,
                "Lord of Temptation: all kneel before you.", 20, 20_000, null));
        register(new SkillDef("true_hero", "True Hero", SkillDef.Tier.ULTIMATE,
                "Lord of Heroes: the hero's final form.", 20, 20_000, null));
    }

    private void register(SkillDef def) {
        skills.put(def.getId(), def);
    }

    public SkillDef getSkill(String id) {
        return skills.get(id);
    }

    public Map<String, SkillDef> allSkills() {
        return Collections.unmodifiableMap(skills);
    }

    public List<SkillDef> byTier(SkillDef.Tier tier) {
        List<SkillDef> out = new ArrayList<>();
        for (SkillDef def : skills.values()) {
            if (def.getTier() == tier) {
                out.add(def);
            }
        }
        return out;
    }

    public boolean hasSkill(Player player, String id) {
        return plugin.getDataManager().get(player).hasSkill(id);
    }

    /** Active skills (with a cost) deserve an orb; passives just exist with you. */
    public boolean castable(SkillDef def) {
        return def.getMagiculeCost() > 0 || def.getCooldownMillis() > 0;
    }

    /** Grant the player an orb for one skill (no-op for passives). */
    public void grantOrb(Player player, SkillDef def) {
        if (!castable(def)) {
            return;
        }
        String tier = def.getTier().name();
        tier = tier.charAt(0) + tier.substring(1).toLowerCase(java.util.Locale.ROOT);
        dev.rea.util.Util.giveItem(player,
                plugin.getItemFactory().skillOrb(def.getId(), def.getName(), tier, player.getUniqueId()));
    }

    /** Re-issue any missing orbs for the player's known skills (join, reincarnation, evolution). */
    public void reissueOrbs(Player player) {
        PlayerData data = plugin.getDataManager().get(player);
        for (String id : new java.util.HashSet<>(data.getSkills())) {
            SkillDef def = skills.get(id);
            if (def == null || !castable(def)) {
                continue;
            }
            boolean found = false;
            for (org.bukkit.inventory.ItemStack item : player.getInventory().getContents()) {
                if (plugin.getItemFactory().isSkillOrb(item)
                        && id.equals(plugin.getItemFactory().skillOrbSkill(item))
                        && player.getUniqueId().equals(plugin.getItemFactory().skillOrbOwner(item))) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                grantOrb(player, def);
            }
        }
    }

    /** Learn a skill (quest/codex rewards, predation, evolution). */
    public void learn(Player player, String id) {
        SkillDef def = skills.get(id);
        if (def == null) {
            return;
        }
        PlayerData data = plugin.getDataManager().get(player);
        if (data.hasSkill(id)) {
            return;
        }
        data.addSkill(id);
        Msg.send(player, "&b&lSkill learned! &f" + def.getName() + " &8(&7" + def.getTier() + "&8)");
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1f);
        grantOrb(player, def);
        if (def.getMagiculeCost() > 0 || def.getCooldownMillis() > 0) {
            Msg.send(player, "&7Active skill — orb gained. Hold it out and use it to cast.");
        } else {
            Msg.send(player, "&aPassive — always with you. No orb needed.");
        }
        plugin.getDataManager().save(data);
    }

    /**
     * The Unique Skill granted on reincarnation. Canon: otherworlders receive
     * one random Unique Skill born of their desires. Predator is Rimuru's
     * personal Unique Skill — it is in the pool, not guaranteed to any race.
     * (Slimes as a species have Intrinsic Skills: Absorb, Dissolve,
     * Self-Regeneration — not Predator.)
     */
    public SkillDef grantUniqueSkill(RaceType race) {
        List<SkillDef> pool = byTier(SkillDef.Tier.UNIQUE);
        return pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
    }

    /**
     * The Ultimate Skill a Unique Skill evolves into at Demon Lord awakening.
     * Only verified canon chains are mapped; other Uniques evolve into a
     * random Ultimate (their final form is unknown).
     */
    public SkillDef ultimateEvolutionOf(SkillDef unique) {
        if (unique == null) {
            return null;
        }
        return switch (unique.getId()) {
            case "predator" -> skills.get("beelzebuth");
            case "great_sage" -> skills.get("raphael");
            case "infinity_prison" -> skills.get("uriel");
            default -> null;
        };
    }

    /** A random Ultimate Skill (for Uniques with no known evolution). */
    public SkillDef randomUltimate() {
        List<SkillDef> pool = byTier(SkillDef.Tier.ULTIMATE);
        return pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
    }

    /** Common Skills a devoured foe can teach (predation). */
    public List<SkillDef> commonSkills() {
        return byTier(SkillDef.Tier.COMMON);
    }

    public void list(Player player) {
        PlayerData data = plugin.getDataManager().get(player);
        Msg.send(player, "&b&lSkills &8(&7" + data.getSkills().size() + "&8)");
        for (String id : data.getSkills()) {
            SkillDef def = skills.get(id);
            if (def != null) {
                Msg.send(player, " &8- &f" + def.getName() + " &8(&7" + def.getTier() + "&8) &7" + def.getDescription());
            }
        }
    }

    /** Area skills only hit hostile targets: monsters and (in PvP) players — never pets, villagers, or the caster. */
    private boolean isHostileTarget(Entity target, Player caster) {
        if (target == caster) {
            return false;
        }
        return target instanceof Monster || target instanceof Player;
    }

    /** Cast a skill: checks ownership, race, magicules, and cooldown, then applies its effect. */
    public void useSkill(Player player, String skillId) {
        SkillDef def = skills.get(skillId);
        if (def == null) {
            Msg.send(player, "&cUnknown skill: " + skillId);
            return;
        }
        PlayerData data = plugin.getDataManager().get(player);
        if (!data.hasSkill(skillId)) {
            Msg.send(player, "&cYou do not know " + def.getName() + " yet.");
            return;
        }
        if (def.getRequiredRace() != null && !def.getRequiredRace().equals(data.getRace())) {
            Msg.send(player, "&c" + def.getName() + " belongs to another race.");
            return;
        }
        if (def.getMagiculeCost() == 0 && def.getCooldownMillis() == 0) {
            Msg.send(player, "&7" + def.getName() + " is a passive skill. It is always with you.");
            return;
        }
        if (data.getMagicules() < def.getMagiculeCost()) {
            Msg.send(player, "&cNot enough magicules (" + def.getMagiculeCost() + " needed).");
            return;
        }
        long now = System.currentTimeMillis();
        Long last = data.getSkillCooldowns().get(skillId);
        if (last != null && now - last < def.getCooldownMillis()) {
            long left = (def.getCooldownMillis() - (now - last)) / 1000;
            Msg.send(player, "&c" + def.getName() + " is cooling down (" + left + "s).");
            return;
        }
        data.setMagicules(data.getMagicules() - (int) def.getMagiculeCost());
        data.getSkillCooldowns().put(skillId, now);
        applyEffect(player, def);
        plugin.getDataManager().save(data);
    }

    private void applyEffect(Player player, SkillDef def) {
        Location loc = player.getLocation();
        switch (def.getId()) {
            case "dissolve" -> {
                for (Entity e : player.getNearbyEntities(3, 3, 3)) {
                    if (e instanceof LivingEntity le && isHostileTarget(le, player)) {
                        le.damage(3.0, player);
                    }
                }
                player.getWorld().spawnParticle(Particle.SPLASH, loc, 20, 1, 1, 1, 0.2);
            }
            case "coercion" -> {
                for (Entity e : player.getNearbyEntities(4, 4, 4)) {
                    if (e instanceof LivingEntity le && isHostileTarget(le, player)) {
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
                    if (e instanceof LivingEntity le && isHostileTarget(le, player)) {
                        le.damage(4.0, player);
                    }
                }
                player.getWorld().spawnParticle(Particle.SPLASH, loc, 20, 1, 1, 1, 0.2);
            }
            case "poison_breath" -> {
                for (Entity e : player.getNearbyEntities(4, 4, 4)) {
                    if (e instanceof LivingEntity le && isHostileTarget(le, player)) {
                        le.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 0));
                    }
                }
                player.getWorld().spawnParticle(Particle.SPLASH, loc, 20, 1, 1, 1, 0.2);
            }
            case "hellfire" -> {
                for (Entity e : player.getNearbyEntities(4, 4, 4)) {
                    if (e instanceof LivingEntity le && isHostileTarget(le, player)) {
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
                    if (e instanceof LivingEntity le && isHostileTarget(le, player) && le.getLocation().distance(target) < 2.0) {
                        le.damage(5.0, player);
                    }
                }
                player.getWorld().spawnParticle(Particle.SPLASH, target, 20, 1, 1, 1, 0.2);
            }
            case "voice_cannon" -> {
                for (Entity e : player.getNearbyEntities(5, 5, 5)) {
                    if (e instanceof LivingEntity le && isHostileTarget(le, player)) {
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
                    if (e instanceof LivingEntity le && isHostileTarget(le, player)) {
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
                    if (e instanceof LivingEntity le && isHostileTarget(le, player)) {
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
                    if (e instanceof LivingEntity le && isHostileTarget(le, player)) {
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
                    if (e instanceof LivingEntity le && isHostileTarget(le, player)) {
                        le.damage(6.0, player);
                    }
                }
                player.getWorld().spawnParticle(Particle.SPLASH, dest, 40, 2, 2, 2, 0.3);
            }
            case "veldora" -> {
                for (Entity e : player.getNearbyEntities(6, 6, 6)) {
                    if (e instanceof LivingEntity le && isHostileTarget(le, player)) {
                        le.damage(7.0, player);
                        le.setVelocity(loc.getDirection().multiply(1.5).setY(0.8));
                    }
                }
                player.getWorld().spawnParticle(Particle.SPLASH, loc, 40, 2, 2, 2, 0.3);
            }
            case "faust" -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 600, 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1));
                player.getWorld().spawnParticle(Particle.SPLASH, loc, 40, 2, 2, 2, 0.3);
            }
            case "mammon" -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 200, 0));
                player.getWorld().spawnParticle(Particle.SPLASH, loc, 40, 2, 2, 2, 0.3);
            }
            case "lucifer" -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 200, 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 200, 1));
                player.getWorld().spawnParticle(Particle.SPLASH, loc, 40, 2, 2, 2, 0.3);
            }
            case "satanael" -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 200, 2));
                player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 200, 0));
                player.getWorld().spawnParticle(Particle.SPLASH, loc, 40, 2, 2, 2, 0.3);
            }
            case "amaterasu" -> {
                for (Entity e : player.getNearbyEntities(5, 5, 5)) {
                    if (e instanceof LivingEntity le && isHostileTarget(le, player)) {
                        le.setFireTicks(200);
                        le.damage(6.0, player);
                    }
                }
                player.getWorld().spawnParticle(Particle.SPLASH, loc, 40, 2, 2, 2, 0.3);
            }
            case "hastur" -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 300, 2));
                player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 300, 1));
                player.getWorld().spawnParticle(Particle.SPLASH, loc, 40, 2, 2, 2, 0.3);
            }
            case "mephisto" -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 600, 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 200, 0));
                player.getWorld().spawnParticle(Particle.SPLASH, loc, 40, 2, 2, 2, 0.3);
            }
            case "michael" -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 200, 2));
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 200, 0));
                player.getWorld().spawnParticle(Particle.SPLASH, loc, 40, 2, 2, 2, 0.3);
            }
            case "samael" -> {
                for (Entity e : player.getNearbyEntities(5, 5, 5)) {
                    if (e instanceof LivingEntity le && isHostileTarget(le, player)) {
                        le.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 200, 1));
                        le.damage(4.0, player);
                    }
                }
                player.getWorld().spawnParticle(Particle.SPLASH, loc, 40, 2, 2, 2, 0.3);
            }
            case "abaddon" -> {
                for (Entity e : player.getNearbyEntities(6, 6, 6)) {
                    if (e instanceof LivingEntity le && isHostileTarget(le, player)) {
                        le.damage(10.0, player);
                    }
                }
                player.getWorld().spawnParticle(Particle.SPLASH, loc, 60, 3, 3, 3, 0.4);
            }
            case "belial" -> {
                for (Entity e : player.getNearbyEntities(5, 5, 5)) {
                    if (e instanceof LivingEntity le && isHostileTarget(le, player)) {
                        le.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 0));
                        le.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 1));
                        le.damage(4.0, player);
                    }
                }
                player.getWorld().spawnParticle(Particle.SPLASH, loc, 40, 2, 2, 2, 0.3);
            }
            case "asmodeus" -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 0));
                player.getWorld().spawnParticle(Particle.SPLASH, loc, 40, 2, 2, 2, 0.3);
            }
            case "sariel" -> {
                player.setHealth(Math.min(20.0, player.getHealth() + 8.0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 200, 1));
                player.getWorld().spawnParticle(Particle.SPLASH, loc, 40, 2, 2, 2, 0.3);
            }
            case "yog_sothoth" -> {
                Location dest = loc.clone().add(loc.getDirection().multiply(10));
                player.teleport(dest);
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 2));
                player.getWorld().spawnParticle(Particle.SPLASH, dest, 40, 2, 2, 2, 0.3);
            }
            case "azazel" -> {
                for (Entity e : player.getNearbyEntities(5, 5, 5)) {
                    if (e instanceof LivingEntity le && isHostileTarget(le, player)) {
                        le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 1));
                        le.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 1));
                    }
                }
                player.getWorld().spawnParticle(Particle.SPLASH, loc, 40, 2, 2, 2, 0.3);
            }
            case "true_hero" -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 200, 2));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 200, 0));
                player.getWorld().spawnParticle(Particle.SPLASH, loc, 40, 2, 2, 2, 0.3);
            }
            case "absorb" -> {
                player.setHealth(Math.min(20.0, player.getHealth() + 2.0));
                player.getWorld().spawnParticle(Particle.SPLASH, loc, 20, 1, 1, 1, 0.2);
            }
            default -> {
                player.getWorld().spawnParticle(Particle.SPLASH, loc, 10, 1, 1, 1, 0.1);
            }
        }
    }
}
