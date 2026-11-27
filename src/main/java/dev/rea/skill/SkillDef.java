package dev.rea.skill;

/** One skill, Tensura-style. Tier decides how it is learned. */
public record SkillDef(String id, String name, String tier, int cost, int cooldownSeconds,
                       String description, String learnHint) {
}
