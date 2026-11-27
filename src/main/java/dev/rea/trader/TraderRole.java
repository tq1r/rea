package dev.rea.trader;

import org.bukkit.entity.Villager;

/**
 * A villager role a player can grant a named specialist. Tier:
 * 0 Novice, 1 Apprentice, 2 Journeyman, 3 Master.
 * Master tiers unlock Rea's own treasures as trades.
 */
public enum TraderRole {
    BLACKSMITH("blacksmith", "Blacksmith", Villager.Profession.WEAPONSMITH),
    FARMER("farmer", "Farmer", Villager.Profession.FARMER),
    ALCHEMIST("alchemist", "Alchemist", Villager.Profession.CLERIC),
    SCRIBE("scribe", "Scribe", Villager.Profession.LIBRARIAN);

    private final String id;
    private final String displayName;
    private final Villager.Profession profession;

    TraderRole(String id, String displayName, Villager.Profession profession) {
        this.id = id;
        this.displayName = displayName;
        this.profession = profession;
    }

    public String id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public Villager.Profession profession() {
        return profession;
    }

    public static TraderRole fromId(String id) {
        if (id == null) {
            return null;
        }
        for (TraderRole role : values()) {
            if (role.id.equalsIgnoreCase(id)) {
                return role;
            }
        }
        return null;
    }

    public static String tierName(int tier) {
        return switch (tier) {
            case 0 -> "Novice";
            case 1 -> "Apprentice";
            case 2 -> "Journeyman";
            default -> "Master";
        };
    }

    public static final int MAX_TIER = 3;
    /** Runes required to train from tier n to tier n+1. */
    public static final long[] TRAIN_COSTS = {100, 250, 500};
}
