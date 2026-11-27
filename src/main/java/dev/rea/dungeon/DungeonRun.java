package dev.rea.dungeon;

import dev.rea.ReaPlugin;
import dev.rea.util.Msg;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

/** One active dungeon attempt (instance). */
public final class DungeonRun {
    private final DungeonDef def;
    private final org.bukkit.Location entry;
    private final List<UUID> summoned;
    private final UUID bossId;

    public DungeonRun(DungeonDef def, org.bukkit.Location entry, List<UUID> summoned, UUID bossId) {
        this.def = def;
        this.entry = entry;
        this.summoned = summoned;
        this.bossId = bossId;
    }

    public DungeonDef def() {
        return def;
    }

    public org.bukkit.Location entry() {
        return entry;
    }

    public List<UUID> summoned() {
        return summoned;
    }

    public UUID bossId() {
        return bossId;
    }
}
