package dev.rea.dungeon;

import org.bukkit.entity.EntityType;

import java.util.List;

/**
 * One dungeon arena: where it is built (y/x offsets), what spawns there,
 * and what it pays out.
 */
public record DungeonDef(String id, String name, String description, int tier,
                        long epReward, long runeReward, int xpReward, double weaponChance,
                        List<Mob> trash, EntityType bossType, String bossName, int radius) {
    public record Mob(EntityType type, String name, int count) {
    }
}
