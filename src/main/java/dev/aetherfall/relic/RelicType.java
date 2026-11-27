package dev.aetherfall.relic;

import org.bukkit.Material;

import java.util.List;

/**
 * The relics of the Vale. Each one carries a fragment of the old story in its lore.
 */
public enum RelicType {
    EMBERHEART("emberheart", "Emberheart Blade", Material.NETHERITE_SWORD, 150L,
            List.of("&7Forged in the Ember tower's last fire.", "&7It still remembers the heat.", "", "&c+2 damage, sets foes ablaze")),
    TIDECALLER("tidecaller", "Tidecaller's Trident", Material.TRIDENT, 150L,
            List.of("&7It hums when the water is near.", "&7The deep lent it its voice.", "", "&b+2 damage while in water")),
    VERDANT_STAFF("verdant_staff", "Verdant Staff", Material.BLAZE_ROD, 120L,
            List.of("&7Carved from the first tree of the Grove.", "&7The forest answers when it is raised.", "", "&aRight-click: make crops flourish")),
    WARDEN_EDGE("warden_edge", "Warden's Edge", Material.DIAMOND_SWORD, 100L,
            List.of("&7An oath, sharpened.", "&7It bites deepest into the restless dead.", "", "&f+2 damage vs undead")),
    CROWN_FRAGMENT("crown_fragment", "Sky-Crown Fragment", Material.NETHER_STAR, 0L,
            List.of("&7A piece of the Crown itself.", "&7It sings, very quietly, in your hand.", "", "&dRight-click: +40 XP (10 min cooldown)"));

    private final String id;
    private final String displayName;
    private final Material material;
    private final long price;
    private final List<String> lore;

    RelicType(String id, String displayName, Material material, long price, List<String> lore) {
        this.id = id;
        this.displayName = displayName;
        this.material = material;
        this.price = price;
        this.lore = lore;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Material getMaterial() {
        return material;
    }

    public long getPrice() {
        return price;
    }

    public List<String> getLore() {
        return lore;
    }

    public static RelicType fromId(String id) {
        if (id == null) {
            return null;
        }
        for (RelicType type : values()) {
            if (type.id.equalsIgnoreCase(id)) {
                return type;
            }
        }
        return null;
    }
}
