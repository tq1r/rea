package dev.aetherfall.util;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

/**
 * Persistent-data keys used to tag Aetherfall items. Items without these tags
 * are treated as plain vanilla items (so players cannot fake relics or pages).
 */
public final class Keys {
    private final NamespacedKey relic;
    private final NamespacedKey codex;
    private final NamespacedKey cache;
    private final NamespacedKey guiAction;
    private final NamespacedKey guiArg;

    public Keys(Plugin plugin) {
        this.relic = new NamespacedKey(plugin, "relic");
        this.codex = new NamespacedKey(plugin, "codex");
        this.cache = new NamespacedKey(plugin, "cache");
        this.guiAction = new NamespacedKey(plugin, "gui_action");
        this.guiArg = new NamespacedKey(plugin, "gui_arg");
    }

    public NamespacedKey relic() {
        return relic;
    }

    public NamespacedKey codex() {
        return codex;
    }

    public NamespacedKey cache() {
        return cache;
    }

    public NamespacedKey guiAction() {
        return guiAction;
    }

    public NamespacedKey guiArg() {
        return guiArg;
    }
}
