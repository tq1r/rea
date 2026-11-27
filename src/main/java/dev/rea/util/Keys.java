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

    public Keys(Plugin plugin) {
        this.codex = new NamespacedKey(plugin, "codex");
        this.cache = new NamespacedKey(plugin, "cache");
        this.cardinal = new NamespacedKey(plugin, "cardinal");
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
}
