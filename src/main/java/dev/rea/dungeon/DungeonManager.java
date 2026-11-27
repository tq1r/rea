package dev.rea.dungeon;

import dev.rea.ReaPlugin;
import dev.rea.data.PlayerData;
import dev.rea.util.Msg;
import dev.rea.util.Util;
import dev.rea.weapon.WeaponDef;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Dungeons: sealed ruins torn from Tensura canon, opened as instanced
 * sky-arenas far above the world. Complete a dungeon to earn EP, runes,
 * XP, and a chance at a cardinal weapon.
 */
public final class DungeonManager {

    // Arenas live at fixed coordinates far from spawn (y=200).
    private static final int ARENA_Y = 200;
    private static final int ARENA_X_START = 20000;
    private static final int ARENA_Z = 0;
    private static final int ARENA_SPACING = 120;

    private final ReaPlugin plugin;
    private final Map<String, DungeonDef> dungeons = new HashMap<>();
    private final Map<UUID, DungeonRun> runs = new HashMap<>();
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public DungeonManager(ReaPlugin plugin) {
        this.plugin = plugin;
        register(new DungeonDef("ogre_hunt", "Ogre Hunt",
                "&7The hills east of Jura are thick with ogres. Cull them for the Federation.",
                0, 300L, 80L, 60, 0.30,
                List.of(new DungeonDef.Mob(EntityType.ZOMBIE, "Ogre", 6)),
                EntityType.RAVAGER, "Ogre Chief Daigol", 12));
        register(new DungeonDef("orc_fortress", "Orc Lord's Fortress",
                "&7The horde that hungered for the world gathers behind black walls. Break it.",
                1, 750L, 200L, 150, 0.45,
                List.of(new DungeonDef.Mob(EntityType.HUSK, "Orc Raider", 8),
                        new DungeonDef.Mob(EntityType.SKELETON, "Orc Archer", 4)),
                EntityType.RAVAGER, "Orc Lord Starved", 14));
        register(new DungeonDef("val_velmyra", "Vál Velmyra",
                "&7The ruins of a dead world, where a Demon Lord's shade still lingers. End it.",
                2, 1500L, 400L, 300, 0.60,
                List.of(new DungeonDef.Mob(EntityType.SKELETON, "Hollow Knight", 6),
                        new DungeonDef.Mob(EntityType.BLAZE, "Hellfire Wisp", 4)),
                EntityType.WARDEN, "Shade of the Demon Lord", 16));
    }

    private void register(DungeonDef def) {
        dungeons.put(def.id(), def);
    }

    public Map<String, DungeonDef> all() {
        return Collections.unmodifiableMap(dungeons);
    }

    public DungeonDef get(String id) {
        return id == null ? null : dungeons.get(id.toLowerCase());
    }

    public boolean inDungeon(Player player) {
        return runs.containsKey(player.getUniqueId());
    }

    public DungeonRun runOf(Player player) {
        return runs.get(player.getUniqueId());
    }

    /** Arena center for a dungeon tier. */
    public Location arenaCenter(World world, DungeonDef def) {
        return new Location(world, ARENA_X_START + def.tier() * ARENA_SPACING, ARENA_Y, ARENA_Z);
    }

    public void enter(Player player, DungeonDef def) {
        PlayerData data = plugin.getDataManager().get(player);
        if (inDungeon(player)) {
            Msg.send(player, "&cYou are already inside a dungeon. Use &e/rea dungeon leave&c to abandon it.");
            return;
        }
        if (data.getRace() == null) {
            Msg.send(player, "&cReincarnate first (an admin must run &e/rea start&c).");
            return;
        }
        long cooldown = plugin.getConfig().getLong("settings.dungeon-cooldown-minutes", 15) * 60_000L;
        Long last = cooldowns.get(player.getUniqueId());
        if (last != null && System.currentTimeMillis() - last < cooldown) {
            long remaining = (cooldown - (System.currentTimeMillis() - last)) / 60000L + 1;
            Msg.send(player, "&cThe ruins need time to settle. Try again in &e" + remaining + " minute(s)&c.");
            return;
        }
        World world = player.getWorld();
        Location center = arenaCenter(world, def);
        buildArena(world, def, center);
        Location entry = player.getLocation().clone();
        // Spawn trash mobs.
        List<UUID> summoned = new ArrayList<>();
        for (DungeonDef.Mob mob : def.trash()) {
            for (int i = 0; i < mob.count(); i++) {
                LivingEntity entity = spawn(world, mob.type(), center, mob.name());
                if (entity != null) {
                    summoned.add(entity.getUniqueId());
                }
            }
        }
        LivingEntity boss = spawn(world, def.bossType(), center, def.bossName());
        UUID bossId = boss != null ? boss.getUniqueId() : null;
        if (bossId != null) {
            summoned.add(bossId);
        }
        runs.put(player.getUniqueId(), new DungeonRun(def, entry, summoned, bossId));
        player.teleport(center.clone().add(0, 1, 0));
        player.playSound(player.getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 1f, 1.4f);
        Msg.send(player, Msg.bar());
        Msg.send(player, "&d&l" + def.name());
        Msg.send(player, def.description());
        Msg.send(player, "&7Defeat &f" + def.bossName() + "&7 to claim your prize.");
        Msg.send(player, "&7Abandon with &e/rea dungeon leave &7(no rewards).");
        Msg.send(player, Msg.bar());
    }

    /** End a run (victory or abandon). Rewards only on victory. */
    public void endRun(Player player, boolean victory) {
        DungeonRun run = runs.remove(player.getUniqueId());
        if (run == null) {
            return;
        }
        DungeonDef def = run.def();
        // Remove remaining summoned mobs.
        World world = player.getWorld();
        for (UUID uuid : run.summoned()) {
            if (uuid.equals(run.bossId()) && victory) {
                continue; // boss already dead
            }
            Entity entity = world.getEntity(uuid);
            if (entity != null) {
                entity.remove();
            }
        }
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        if (!victory) {
            Msg.send(player, "&7You abandon the ruins of &d" + def.name() + "&7. No reward.");
            player.teleport(run.entry());
            return;
        }
        PlayerData data = plugin.getDataManager().get(player);
        data.incrementCounter("dungeon:clear", 1);
        Msg.send(player, Msg.bar());
        Msg.send(player, "&d&l" + def.name() + " cleared!");
        Msg.send(player, "&7Runes: &d" + def.runeReward() + " &7| XP: &d" + def.xpReward() + " &7| EP: &d" + def.epReward());
        plugin.getDataManager().addRunes(player, def.runeReward());
        plugin.getDataManager().addXp(player, def.xpReward());
        plugin.getDataManager().addEp(player, def.epReward());
        ItemStack weapon = plugin.getWeaponManager().rollWeapon(def.tier(), def.weaponChance());
        if (weapon != null) {
            WeaponDef found = plugin.getWeaponManager().weaponOf(weapon);
            Msg.send(player, "&d&lYou recovered a cardinal weapon: &l" + (found != null ? found.name() : weapon.getType().name()) + "&d!");
            Msg.send(player, "&7" + (found != null ? found.grade() : "") + " grade — its name hums in your hands.");
            Util.giveItem(player, weapon);
        } else {
            Msg.send(player, "&7No cardinal weapon this time. The ruins keep their secrets.");
        }
        Msg.send(player, Msg.bar());
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1f);
        player.teleport(run.entry());
        plugin.getQuestManager().checkCompletion(player);
        plugin.getDataManager().save(data);
    }

    /** Called from the entity-death listener. */
    public void onBossDefeated(Player killer, UUID entityId) {
        DungeonRun run = runs.get(killer.getUniqueId());
        if (run == null || run.bossId() == null || !run.bossId().equals(entityId)) {
            return;
        }
        endRun(killer, true);
    }

    /** Called from the quit listener — abandon without reward. */
    public void onPlayerQuit(Player player) {
        if (runs.containsKey(player.getUniqueId())) {
            endRun(player, false);
        }
    }

    private LivingEntity spawn(World world, EntityType type, Location center, String name) {
        for (int attempt = 0; attempt < 8; attempt++) {
            double angle = Math.random() * Math.PI * 2;
            double dist = 4 + Math.random() * 8;
            Location loc = center.clone().add(Math.cos(angle) * dist, 1, Math.sin(angle) * dist);
            Entity entity = world.spawnEntity(loc, type);
            if (entity instanceof LivingEntity living) {
                living.setCustomName(Msg.c("&c" + name));
                living.setCustomNameVisible(true);
                living.setRemoveWhenFarAway(false);
                if (living instanceof Mob mob) {
                    mob.setAware(true);
                }
                if (living instanceof Tameable tameable) {
                    tameable.setTamed(false);
                }
                return living;
            }
            entity.remove();
        }
        return null;
    }

    private void buildArena(World world, DungeonDef def, Location center) {
        int r = def.radius();
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        // Clear the skybox first (in case a previous run left debris).
        for (int x = cx - r - 2; x <= cx + r + 2; x++) {
            for (int y = cy - 5; y <= cy + 10; y++) {
                for (int z = cz - r - 2; z <= cz + r + 2; z++) {
                    world.getBlockAt(x, y, z).setType(Material.AIR, false);
                }
            }
        }
        // Floating ruin chunk: a tapering base of deepslate.
        int[] baseLayers = {r, r - 2, r - 4, r - 7};
        for (int i = 0; i < baseLayers.length; i++) {
            int y = cy - 1 - i;
            int rr = baseLayers[i];
            for (int x = cx - rr; x <= cx + rr; x++) {
                for (int z = cz - rr; z <= cz + rr; z++) {
                    int dx = x - cx;
                    int dz = z - cz;
                    if (dx * dx + dz * dz <= rr * rr) {
                        world.getBlockAt(x, y, z).setType(Material.DEEPSLATE, false);
                    }
                }
            }
        }
        // Floor: cracked bricks outside, stone bricks inside, polished deepslate dais.
        for (int x = cx - r; x <= cx + r; x++) {
            for (int z = cz - r; z <= cz + r; z++) {
                int dx = x - cx;
                int dz = z - cz;
                int distSq = dx * dx + dz * dz;
                if (distSq > r * r) {
                    continue;
                }
                Material floor;
                if (distSq <= 4) {
                    floor = Material.CHISELED_DEEPSLATE;
                } else if (distSq <= 16) {
                    floor = Material.POLISHED_DEEPSLATE;
                } else if (distSq > (r - 4) * (r - 4)) {
                    floor = Material.CRACKED_STONE_BRICKS;
                } else {
                    floor = Material.STONE_BRICKS;
                }
                world.getBlockAt(x, cy - 1, z).setType(floor, false);
            }
        }
        // Walls: deepslate brick ring, with crenellations on top.
        for (int x = cx - r; x <= cx + r; x++) {
            for (int z = cz - r; z <= cz + r; z++) {
                int dx = x - cx;
                int dz = z - cz;
                int distSq = dx * dx + dz * dz;
                if (distSq > r * r) {
                    continue;
                }
                int dist = (int) Math.sqrt(distSq);
                if (dist >= r - 1) {
                    for (int y = cy; y <= cy + 5; y++) {
                        world.getBlockAt(x, y, z).setType(Material.DEEPSLATE_BRICKS, false);
                    }
                    if ((x + z) % 4 == 0) {
                        world.getBlockAt(x, cy + 6, z).setType(Material.DEEPSLATE_TILE_WALL, false);
                    }
                }
            }
        }
        // Corner bastions: taller pillars capped with sea lanterns.
        for (int signX : new int[]{-1, 1}) {
            for (int signZ : new int[]{-1, 1}) {
                int px = cx + signX * (r - 1);
                int pz = cz + signZ * (r - 1);
                for (int y = cy; y <= cy + 7; y++) {
                    world.getBlockAt(px, y, pz).setType(Material.DEEPSLATE_BRICKS, false);
                }
                world.getBlockAt(px, cy + 8, pz).setType(Material.SEA_LANTERN, false);
            }
        }
        // Cardinal braziers on chiseled deepslate plinths.
        int b = r - 3;
        for (int[] off : new int[][]{{b, 0}, {-b, 0}, {0, b}, {0, -b}}) {
            world.getBlockAt(cx + off[0], cy, cz + off[1]).setType(Material.CHISELED_DEEPSLATE, false);
            world.getBlockAt(cx + off[0], cy + 1, cz + off[1]).setType(Material.CAMPFIRE, false);
        }
    }
}
