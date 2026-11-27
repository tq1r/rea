package dev.rea.util;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

/**
 * Persistent-data keys used to tag Rea items. Items without these tags
 * are treated as plain vanilla items (so players cannot fake pages or caches).
 */
public final class Keys {
    private final NamespacedKey codex;
    private final NamespacedKey cache;
    private final NamespacedKey cardinal;
    private final NamespacedKey skillOrb;
    private final NamespacedKey mythicArmor;

    public Keys(Plugin plugin) {
        this.codex = new NamespacedKey(plugin, "codex");
        this.cache = new NamespacedKey(plugin, "cache");
        this.cardinal = new NamespacedKey(plugin, "cardinal");
        this.skillOrb = new NamespacedKey(plugin, "skill_orb");
        this.mythicArmor = new NamespacedKey(plugin, "mythic_armor");
    }

    public NamespacedKey codex() {
        return codex;
    }

    public NamespacedKey cache() {
        return cache;
    }

    public NamespacedKey cardinal() {
        return cardinal;
    }

    public NamespacedKey skillOrb() {
        return skillOrb;
    }

    public NamespacedKey mythicArmor() {
        return mythicArmor;
    }
}
