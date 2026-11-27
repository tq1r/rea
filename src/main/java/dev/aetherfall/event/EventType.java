package dev.aetherfall.event;

/** The Vale's wandering moods. Each lasts a few minutes and changes the world. */
public enum EventType {
    SHARD_STORM("shard_storm", "Shard Storm", "&d"),
    BLOOD_MOON("blood_moon", "Blood Moon", "&c"),
    METEOR("meteor", "Meteor Fall", "&6"),
    VEIL("veil", "The Veil Thins", "&b");

    private final String id;
    private final String displayName;
    private final String color;

    EventType(String id, String displayName, String color) {
        this.id = id;
        this.displayName = displayName;
        this.color = color;
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

    public static EventType fromId(String id) {
        if (id == null) {
            return null;
        }
        for (EventType type : values()) {
            if (type.id.equalsIgnoreCase(id)) {
                return type;
            }
        }
        return null;
    }
}
