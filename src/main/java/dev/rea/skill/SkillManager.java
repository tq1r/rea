package dev.rea.skill;

import dev.rea.ReaPlugin;
import dev.rea.data.PlayerData;
import dev.rea.race.RaceType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Skill registry and learning (Tensura canon):
 * - INTRINSIC skills come with the race.
 * - COMMON skills are learned by devouring foes (predation).
 * - UNIQUE skills are granted on reincarnation (Slimes get Predator).
 * - ULTIMATE skills evolve from a Unique Skill at Demon Lord awakening.
 */
public final class SkillManager {

    private final ReaPlugin plugin;
    private final Map<String, SkillDef> skills = new LinkedHashMap<>();

    public SkillManager(ReaPlugin plugin) {
        this.plugin = plugin;
        register(new SkillDef("dissolve", "Dissolve", SkillDef.Tier.INTRINSIC,
                "Acid melts nearby foes.", 5, 10_000, "slime"));
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
        register(new SkillDef("predator", "Predator", SkillDef.Tier.UNIQUE,
                "Devour foes to learn their Common Skills and gain EP.", 0, 0, "slime"));
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
        register(new SkillDef("beelzebuth", "Beelzebuth", SkillDef.Tier.ULTIMATE,
                "Lord of Gluttony: devour everything, heal, and strike hard.", 20, 20_000, null));
        register(new SkillDef("raphael", "Raphael", SkillDef.Tier.ULTIMATE,
                "Lord of Wisdom: everything flows to you.", 20, 20_000, null));
        register(new SkillDef("uriel", "Uriel", SkillDef.Tier.ULTIMATE,
                "Lord of Vows: trap and strip foes, then strike.", 20, 20_000, null));
        register(new SkillDef("azathoth", "Azathoth", SkillDef.Tier.ULTIMATE,
                "Lord of Space: blink and cut through foes.", 20, 20_000, null));
    }

    private void register(SkillDef def) {
        skills.put(def.getId(), def);
    }

    public SkillDef get(String id) {
        return skills.get(id);
    }

    public Map<String, SkillDef> all() {
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

    /**
     * The Unique Skill granted on reincarnation: Slimes receive Predator;
     * every other race receives a random Unique Skill from the pool.
     */
    public SkillDef grantUniqueSkill(RaceType race) {
        if (race == RaceType.SLIME) {
            return skills.get("predator");
        }
        List<SkillDef> pool = new ArrayList<>();
        for (SkillDef def : byTier(SkillDef.Tier.UNIQUE)) {
            if (!"predator".equals(def.getId())) {
                pool.add(def);
            }
        }
        return pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
    }

    /**
     * The Ultimate Skill a Unique Skill evolves into at Demon Lord awakening.
     * Returns null when the Unique Skill has no known Ultimate evolution.
     */
    public SkillDef ultimateEvolutionOf(SkillDef unique) {
        if (unique == null) {
            return null;
        }
        return switch (unique.getId()) {
            case "predator" -> skills.get("beelzebuth");
            case "great_sage" -> skills.get("raphael");
            case "infinity_prison" -> skills.get("uriel");
            case "starved" -> skills.get("azathoth");
            default -> null;
        };
    }

    /** Common Skills a devoured foe can teach (predation). */
    public List<SkillDef> commonSkills() {
        return byTier(SkillDef.Tier.COMMON);
    }

    public boolean canUse(Player player, PlayerData data, SkillDef def) {
        if (def == null) {
            return false;
        }
        if (def.getRequiredRace() != null && !def.getRequiredRace().equals(data.getRaceId())) {
            return false;
        }
        return data.hasSkill(def.getId());
    }

    public boolean tryUse(Player player, PlayerData data, SkillDef def) {
        if (def == null || !canUse(player, data, def)) {
            return false;
        }
        if (data.getMagicules() < def.getMagiculeCost()) {
            return false;
        }
        long now = System.currentTimeMillis();
        Long last = data.getSkillCooldowns().get(def.getId());
        if (last != null && now - last < def.getCooldownMillis()) {
            return false;
        }
        data.setMagicules(data.getMagicules() - def.getMagiculeCost());
        data.getSkillCooldowns().put(def.getId(), now);
        return true;
    }
}
