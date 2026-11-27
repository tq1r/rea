package dev.rea.race;

import java.util.List;

/**
 * The ten starting races of Rea (Tensura canon). Each race has canon
 * Intrinsic Skills where the source material gives them one; races without
 * a canon intrinsic carry a modest balance gift instead (marked in lore).
 * Races are dealt randomly by /rea start and are permanent; forms evolve
 * with EP, level, and runes. Every otherworlder also receives a Unique
 * Skill on reincarnation (Slimes receive Predator).
 */
public enum RaceType {
    SLIME("slime", "Slime", "&b",
            List.of("&7Intrinsic: Absorption, Dissolve, Self-Regeneration.", "&7Soft, patient, unstoppable."),
            "Self-Regeneration.",
            "dissolve", "Dissolve", "Acid melts nearby foes.",
            new String[]{"Slime", "Evolved Slime", "Ogre", "Demon Lord"}),
    GOBLIN("goblin", "Goblin", "&a",
            List.of("&7No canon intrinsic. Cunning is its own gift.", "&7Small, quick, everywhere."),
            "Speed.",
            "coercion", "Coercion", "Fear nearby foes (slow + weaken).",
            new String[]{"Goblin", "Hobgoblin", "Ogre", "Demon Lord"}),
    LIZARDMAN("lizardman", "Lizardman", "&2",
            List.of("&7Intrinsic: Scale Armor.", "&7Cold blood, warm heart."),
            "Water Breathing.",
            "scale_armor", "Scale Armor", "Harden: Resistance II for 8s.",
            new String[]{"Lizardman", "Dragonewt", "True Dragonewt", "Demon Lord"}),
    ORC("orc", "Orc", "&c",
            List.of("&7Intrinsic: Strengthen.", "&7Fierce, proud, unbroken."),
            "+2 melee damage.",
            "strengthen", "Strengthen", "Strength II for 10s.",
            new String[]{"Orc", "High Orc", "Orc Lord", "Demon Lord"}),
    HUMAN("human", "Human", "&e",
            List.of("&7No canon intrinsic. Adaptability is its own gift.", "&7Stubborn, everywhere."),
            "+10% XP from everything.",
            "second_wind", "Second Wind", "Heal 4 HP and sprint (Speed II, 8s).",
            new String[]{"Human", "Enhanced Human", "Hero", "Demon Lord"}),
    ELF("elf", "Elf", "&a",
            List.of("&7No canon intrinsic. Keen senses are their own gift.", "&7Swift, keen, long-lived."),
            "Speed + Night Vision.",
            "elven_step", "Elven Step", "Blink forward 6 blocks.",
            new String[]{"Elf", "High Elf", "Fairy Queen", "Demon Lord"}),
    DWARF("dwarf", "Dwarf", "&6",
            List.of("&7No canon intrinsic. Sturdy hands are their own gift.", "&7Sturdy, tireless, loyal."),
            "Resistance + Haste.",
            "stonehide", "Stonehide", "Resistance II for 8s.",
            new String[]{"Dwarf", "Master Dwarf", "Rune Lord", "Demon Lord"}),
    DIRE_WOLF("dire_wolf", "Dire Wolf", "&8",
            List.of("&7Intrinsic: Ultra Smell, Thought Communication, Coercion.", "&7Loyal, swift, relentless."),
            "Ultra Smell (Night Vision + Speed).",
            "fang_rush", "Fang Rush", "Dash forward, biting nearby foes.",
            new String[]{"Dire Wolf", "Evolved Dire Wolf", "Alpha Wolf", "Demon Lord"}),
    SERPENT("serpent", "Serpent", "&5",
            List.of("&7Intrinsic: Poison Breath, Heat Source Perception.", "&7Silent, patient, deadly."),
            "Heat Source Perception (Night Vision).",
            "poison_breath", "Poison Breath", "Poison nearby foes.",
            new String[]{"Serpent", "Black Serpent", "Hydra", "Demon Lord"}),
    DEMON("demon", "Demon", "&4",
            List.of("&7Intrinsic: Magic Resistance.", "&7Warm, wry, fire-touched."),
            "Magic Resistance.",
            "hellfire", "Hellfire", "Burn and scorch nearby foes.",
            new String[]{"Demon", "Arch Demon", "Demon Peer", "Demon Lord"});

    private final String id;
    private final String displayName;
    private final String color;
    private final List<String> lore;
    private final String passiveDescription;
    private final String intrinsicId;
    private final String intrinsicName;
    private final String intrinsicDescription;
    private final String[] forms;

    RaceType(String id, String displayName, String color, List<String> lore,
             String passiveDescription, String intrinsicId, String intrinsicName,
             String intrinsicDescription, String[] forms) {
        this.id = id;
        this.displayName = displayName;
        this.color = color;
        this.lore = lore;
        this.passiveDescription = passiveDescription;
        this.intrinsicId = intrinsicId;
        this.intrinsicName = intrinsicName;
        this.intrinsicDescription = intrinsicDescription;
        this.forms = forms;
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

    public String getIntrinsicId() {
        return intrinsicId;
    }

    public String getIntrinsicName() {
        return intrinsicName;
    }

    public String getIntrinsicDescription() {
        return intrinsicDescription;
    }

    public String[] getForms() {
        return forms;
    }

    public String formName(int stage) {
        return forms[Math.max(0, Math.min(forms.length - 1, stage))];
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
