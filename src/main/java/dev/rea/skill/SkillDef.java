package dev.rea.skill;

/**
 * A skill definition (Tensura canon tiers):
 * INTRINSIC — born with the race (canon intrinsic or balance gift);
 * COMMON — learned by predation from devoured foes;
 * UNIQUE — one granted on reincarnation to otherworlders (Slimes: Predator);
 * ULTIMATE — evolved from a Unique Skill at Demon Lord awakening.
 */
public final class SkillDef {

    public enum Tier {
        INTRINSIC, COMMON, UNIQUE, ULTIMATE
    }

    private final String id;
    private final String name;
    private final Tier tier;
    private final String description;
    private final double magiculeCost;
    private final long cooldownMillis;
    private final String requiredRace;

    public SkillDef(String id, String name, Tier tier, String description,
                    double magiculeCost, long cooldownMillis, String requiredRace) {
        this.id = id;
        this.name = name;
        this.tier = tier;
        this.description = description;
        this.magiculeCost = magiculeCost;
        this.cooldownMillis = cooldownMillis;
        this.requiredRace = requiredRace;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Tier getTier() {
        return tier;
    }

    public String getDescription() {
        return description;
    }

    public double getMagiculeCost() {
        return magiculeCost;
    }

    public long getCooldownMillis() {
        return cooldownMillis;
    }

    public String getRequiredRace() {
        return requiredRace;
    }
}
