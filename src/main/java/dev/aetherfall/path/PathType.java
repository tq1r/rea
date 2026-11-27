package dev.aetherfall.path;

import org.bukkit.Material;

import java.util.List;

/**
 * The four Paths of the Pillars. Each grants passive effects and one active
 * ability (used with /ability, so Bedrock players are fully included).
 */
public enum PathType {
    EMBER("ember", "Emberpath", "&c", Material.FIRE_CHARGE,
            List.of("&7Walk in fire and fear nothing.", "&7+50% melee damage, fire resistance.", "&7Ability: &cEmber Dash"),
            "Ember Dash", "Dash forward in a burst of flame, burning nearby foes."),
    TIDE("tide", "Tidepath", "&b", Material.NAUTILUS_SHELL,
            List.of("&7Speak to the deep and return with answers.", "&7Breathe underwater, swim faster.", "&7Ability: &bTidal Pull"),
            "Tidal Pull", "Drag nearby foes toward you with the weight of the sea."),
    VERDANT("verdant", "Verdantpath", "&a", Material.OAK_SAPLING,
            List.of("&7Make a desert bloom by asking nicely.", "&7Slowly regenerate, crops grow faster.", "&7Ability: &aBloom"),
            "Bloom", "Heal yourself and make nearby crops flourish."),
    STONE("stone", "Stonepath", "&8", Material.STONE,
            List.of("&7Hold the mountain up and never complain.", "&7Take 20% less damage.", "&7Ability: &8Quake Step"),
            "Quake Step", "Slam the ground, knocking nearby foes away.");

    private final String id;
    private final String displayName;
    private final String color;
    private final Material icon;
    private final List<String> lore;
    private final String abilityName;
    private final String abilityDescription;

    PathType(String id, String displayName, String color, Material icon, List<String> lore,
             String abilityName, String abilityDescription) {
        this.id = id;
        this.displayName = displayName;
        this.color = color;
        this.icon = icon;
        this.lore = lore;
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

    public Material getIcon() {
        return icon;
    }

    public List<String> getLore() {
        return lore;
    }

    public String getAbilityName() {
        return abilityName;
    }

    public String getAbilityDescription() {
        return abilityDescription;
    }

    public static PathType fromId(String id) {
        if (id == null) {
            return null;
        }
        for (PathType type : values()) {
            if (type.id.equalsIgnoreCase(id)) {
                return type;
            }
        }
        return null;
    }
}
