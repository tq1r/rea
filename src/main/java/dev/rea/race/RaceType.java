package dev.rea.race;

import java.util.List;

/**
 * The ten bloodlines of Rea. Each grants passive effects and one active
 * ability (used with /ability, so Bedrock players are fully included).
 * Races are dealt randomly by /rea start and are permanent.
 */
public enum RaceType {
    HUMAN("human", "Human", "&e",
            List.of("&7Keepers of the Flame's heart.", "&7Adaptable, stubborn, everywhere."),
            "+10% XP from everything.",
            "Second Wind", "Heal 4 HP and sprint (Speed II, 8s)."),
    ELF("elf", "Elf", "&a",
            List.of("&7Keepers of the Flame's voice.", "&7Swift, keen, long-lived."),
            "Regeneration + Speed.",
            "Elven Step", "Blink forward 6 blocks."),
    DWARF("dwarf", "Dwarf", "&6",
            List.of("&7Keepers of the Flame's hands.", "&7Sturdy, tireless, loyal."),
            "Resistance + Haste.",
            "Stonehide", "Resistance II for 8s."),
    ORC("orc", "Orc", "&c",
            List.of("&7Keepers of the Flame's wrath.", "&7Fierce, proud, unbroken."),
            "+2 melee damage.",
            "Bloodrage", "Strength II for 10s."),
    HALFLING("halfling", "Halfling", "&d",
            List.of("&7Keepers of the Flame's luck.", "&7Small, quick, impossible to catch."),
            "Luck + Speed.",
            "Lucky Dodge", "Resistance + Speed II for 6s."),
    TIEFLING("tiefling", "Tiefling", "&5",
            List.of("&7Keepers of the Flame's shadow.", "&7Warm, wry, fire-touched."),
            "Fire resistance.",
            "Hellfire", "Burn and scorch nearby foes."),
    DRAGONBORN("dragonborn", "Dragonborn", "&4",
            List.of("&7Children of the Flame's first fire.", "&7Proud, strong, draconic."),
            "+4 max health.",
            "Dragon's Breath", "Breathe fire in a cone."),
    GNOME("gnome", "Gnome", "&b",
            List.of("&7Keepers of the Flame's making.", "&7Clever, curious, bouncy."),
            "Haste II + Jump Boost.",
            "Mend", "Repair the item in your hand."),
    AASIMAR("aasimar", "Aasimar", "&f",
            List.of("&7Keepers of the Flame's light.", "&7Radiant, calm, watchful."),
            "Night Vision + Slow Falling.",
            "Dawn's Blessing", "Heal 6 HP and gain Resistance."),
    GOLIATH("goliath", "Goliath", "&8",
            List.of("&7Keepers of the Flame's strength.", "&7Towering, gentle, immovable."),
            "Strength I.",
            "Giant's Slam", "Slam the ground, hurting and hurling foes.");

    private final String id;
    private final String displayName;
    private final String color;
    private final List<String> lore;
    private final String passiveDescription;
    private final String abilityName;
    private final String abilityDescription;

    RaceType(String id, String displayName, String color, List<String> lore,
             String passiveDescription, String abilityName, String abilityDescription) {
        this.id = id;
        this.displayName = displayName;
        this.color = color;
        this.lore = lore;
        this.passiveDescription = passiveDescription;
        this.abilityName = abilityName;
        this.abilityDescription = abilityDescription;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColor() {
        return color;
    }

    public List<String> getLore() {
        return lore;
    }

    public String getPassiveDescription() {
        return passiveDescription;
    }

    public String getAbilityName() {
        return abilityName;
    }

    public String getAbilityDescription() {
        return abilityDescription;
    }

    public static RaceType fromId(String id) {
        if (id == null) {
            return null;
        }
        for (RaceType type : values()) {
            if (type.id.equalsIgnoreCase(id)) {
                return type;
            }
        }
        return null;
    }
}
