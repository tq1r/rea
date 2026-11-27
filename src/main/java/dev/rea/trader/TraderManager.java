package dev.rea.trader;

import dev.rea.ReaPlugin;
import dev.rea.codex.CodexEntry;
import dev.rea.data.PlayerData;
import dev.rea.util.Msg;
import dev.rea.weapon.WeaponDef;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

/**
 * Trained villagers (Rimuru's naming, inverted): the player spends runes to
 * name an ordinary villager as a specialist — a blacksmith, farmer,
 * alchemist, or scribe — and to train it tier by tier. Each tier unlocks
 * better trades, exactly as named dwarven smiths in Tempest grow into
 * legendary craftsmen.
 *
 * All state lives in the villager's persistent data container, so
 * specialists survive restarts and region unloads.
 */
public final class TraderManager {

    private final ReaPlugin plugin;
    private final NamespacedKey roleKey;
    private final NamespacedKey tierKey;
    private final NamespacedKey nameKey;

    public TraderManager(ReaPlugin plugin) {
        this.plugin = plugin;
        this.roleKey = new NamespacedKey(plugin, "trader_role");
        this.tierKey = new NamespacedKey(plugin, "trader_tier");
        this.nameKey = new NamespacedKey(plugin, "trader_name");
    }

    /** Master specialists shed faint magic — an aura players can spot from afar. */
    public void startAuraTask() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                for (Entity entity : player.getNearbyEntities(24, 16, 24)) {
                    if (entity instanceof Villager villager && isSpecialist(villager) && tier(villager) >= 3) {
                        villager.getWorld().spawnParticle(Particle.ENCHANT,
                                villager.getLocation().add(0, 1.2, 0), 2, 0.25, 0.4, 0.25, 0.02);
                    }
                }
            }
        }, 40L, 40L);
    }

    // ---- identification ----

    public boolean isSpecialist(Villager villager) {
        return villager.getPersistentDataContainer().get(roleKey, PersistentDataType.STRING) != null;
    }

    public TraderRole role(Villager villager) {
        return TraderRole.fromId(villager.getPersistentDataContainer().get(roleKey, PersistentDataType.STRING));
    }

    public int tier(Villager villager) {
        Integer tier = villager.getPersistentDataContainer().get(tierKey, PersistentDataType.INTEGER);
        return tier == null ? 0 : tier;
    }

    public String givenName(Villager villager) {
        String name = villager.getPersistentDataContainer().get(nameKey, PersistentDataType.STRING);
        return name == null ? "Unnamed" : name;
    }

    private void setTag(Villager villager, TraderRole role, int tier, String name) {
        villager.getPersistentDataContainer().set(roleKey, PersistentDataType.STRING, role.id());
        villager.getPersistentDataContainer().set(tierKey, PersistentDataType.INTEGER, tier);
        villager.getPersistentDataContainer().set(nameKey, PersistentDataType.STRING, name);
    }

    /** Villager the player is looking at (cone in front, up to 6 blocks). */
    public Villager findTarget(Player player) {
        Location eye = player.getEyeLocation();
        org.bukkit.util.Vector direction = eye.getDirection();
        Villager best = null;
        double bestScore = Double.MAX_VALUE;
        for (Entity entity : player.getNearbyEntities(6, 6, 6)) {
            if (!(entity instanceof Villager villager)) {
                continue;
            }
            Location villagerCenter = villager.getLocation().add(0, 1, 0);
            org.bukkit.util.Vector to = villagerCenter.toVector().subtract(eye.toVector());
            double distance = to.length();
            if (distance < 0.5 || distance > 6.0) {
                continue;
            }
            to.normalize();
            if (direction.dot(to) < 0.80) {
                continue; // ~37° cone
            }
            double score = distance - direction.dot(to); // nearer + straighter = better
            if (score < bestScore) {
                bestScore = score;
                best = villager;
            }
        }
        return best;
    }

    // ---- actions ----

    /** Claim an ordinary villager as your specialist. */
    public void specialize(Player player, String roleId, String name) {
        TraderRole role = TraderRole.fromId(roleId);
        if (role == null) {
            Msg.send(player, "&cUnknown role. &e/rea trader specialize <blacksmith|farmer|alchemist|scribe> <name>");
            return;
        }
        if (name == null || name.isBlank() || name.length() > 24) {
            Msg.send(player, "&cNames are 1-24 characters.");
            return;
        }
        Villager villager = findTarget(player);
        if (villager == null) {
            Msg.send(player, "&cNo villager in front of you. Look at one and try again.");
            return;
        }
        if (isSpecialist(villager)) {
            Msg.send(player, "&cThat villager already serves another master.");
            return;
        }
        long cost = plugin.getConfig().getLong("settings.trader-specialize-cost", 100L);
        if (!plugin.getDataManager().takeRunes(player, cost)) {
            return;
        }
        setTag(villager, role, 0, name.trim());
        applyStage(villager, role, 0, name.trim());
        PlayerData data = plugin.getDataManager().get(player);
        data.incrementCounter("trader:specialize", 1);
        plugin.getDataManager().save(data);
        Msg.send(player, Msg.bar());
        Msg.send(player, "&d&lNaming! &f" + name.trim() + " &7is now &byour " + role.displayName() + "&7.");
        Msg.send(player, "&7Train them with &e/rea trader train &7to unlock better wares.");
        Msg.send(player, Msg.bar());
        villager.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, villager.getLocation().add(0, 1.5, 0), 30, 0.4, 0.6, 0.4);
        villager.getWorld().playSound(villager.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1.6f);
    }

    /** Rename your specialist. */
    public void rename(Player player, String name) {
        if (name == null || name.isBlank() || name.length() > 24) {
            Msg.send(player, "&cNames are 1-24 characters.");
            return;
        }
        Villager villager = findTarget(player);
        if (villager == null) {
            Msg.send(player, "&cNo targeted specialist. Look at your trader and try again.");
            return;
        }
        if (!isSpecialist(villager)) {
            Msg.send(player, "&cThat villager is not your specialist.");
            return;
        }
        setTag(villager, role(villager), tier(villager), name.trim());
        applyStage(villager, role(villager), tier(villager), name.trim());
        Msg.send(player, "&dYour specialist is now known as &b" + name.trim() + "&d.");
    }

    /** Train your specialist a tier. */
    public void train(Player player) {
        Villager villager = findTarget(player);
        if (villager == null) {
            Msg.send(player, "&cNo targeted specialist. Look at your trader and try again.");
            return;
        }
        if (!isSpecialist(villager)) {
            Msg.send(player, "&cThat villager is not your specialist. Use &e/rea trader specialize&c first.");
            return;
        }
        int tier = tier(villager);
        if (tier >= TraderRole.MAX_TIER) {
            Msg.send(player, "&7" + givenName(villager) + " &7has nothing left to learn. A master of their craft.");
            return;
        }
        long cost = TraderRole.TRAIN_COSTS[tier];
        if (!plugin.getDataManager().takeRunes(player, cost)) {
            return;
        }
        TraderRole role = role(villager);
        String name = givenName(villager);
        int next = tier + 1;
        setTag(villager, role, next, name);
        applyStage(villager, role, next, name);
        Msg.send(player, Msg.bar());
        Msg.send(player, "&d&lTraining! &f" + name + " &7advances to &b" + TraderRole.tierName(next) + " " + role.displayName() + "&7.");
        Msg.send(player, "&7New wares unlocked — trade with them to see.");
        Msg.send(player, Msg.bar());
        villager.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, villager.getLocation().add(0, 1.5, 0), 50, 0.5, 0.8, 0.5);
        villager.getWorld().playSound(villager.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1.3f);
    }

    /** Inspect the targeted specialist. */
    public void info(Player player) {
        Villager villager = findTarget(player);
        if (villager == null || !isSpecialist(villager)) {
            Msg.send(player, "&7Look at your specialist to inspect them.");
            return;
        }
        int tier = tier(villager);
        Msg.send(player, Msg.bar());
        Msg.send(player, "&b" + givenName(villager) + " &7- " + TraderRole.tierName(tier) + " " + role(villager).displayName());
        if (tier < TraderRole.MAX_TIER) {
            Msg.send(player, "&7Next training costs &d" + TraderRole.TRAIN_COSTS[tier] + " runes &7(&e/rea trader train&7).");
        } else {
            Msg.send(player, "&dMaster of the craft. &7Their finest wares await.");
        }
        Msg.send(player, "&7Rename with &e/rea trader rename <name>&7.");
        Msg.send(player, Msg.bar());
    }

    // ---- presentation & trades ----

    private void applyStage(Villager villager, TraderRole role, int tier, String name) {
        // Physical transformation: each role wears a different villager face,
        // profession gear, and trade level badge. Masters glow with restless magic.
        villager.setProfession(role.profession());
        villager.setVillagerType(role.type());
        villager.setVillagerLevel(Math.min(5, tier + 1));
        villager.setRemoveWhenFarAway(false);
        villager.setPersistent(true);
        if (!villager.isAdult()) {
            villager.setAdult();
        }
        String title = "&e" + name + " &6" + role.displayName() + " &7(" + TraderRole.tierName(tier) + ")";
        villager.customName(LegacyComponentSerializer.legacySection().deserialize(Msg.c(title)));
        villager.setCustomNameVisible(true);
        villager.setRecipes(buildRecipes(role, tier));
    }

    private MerchantRecipe recipe(ItemStack result, int maxUses, List<ItemStack> ingredients) {
        MerchantRecipe trade = new MerchantRecipe(result, maxUses);
        trade.setIngredients(ingredients);
        return trade;
    }

    private ItemStack stack(Material material, int amount) {
        return new ItemStack(material, amount);
    }

    private ItemStack emeralds(int amount) {
        return stack(Material.EMERALD, amount);
    }

    private ItemStack runeCache() {
        return plugin.getItemFactory().runeCache(1);
    }

    private ItemStack codexBook(String entryId) {
        CodexEntry entry = plugin.getCodexManager().getEntry(entryId);
        if (entry == null) {
            return stack(Material.BOOK, 1);
        }
        return plugin.getCodexManager().createBook(entry);
    }

    private ItemStack cardinalWeapon(String id) {
        WeaponDef def = plugin.getWeaponManager().get(id);
        if (def == null) {
            return stack(Material.NETHERITE_SWORD, 1);
        }
        return plugin.getWeaponManager().create(def);
    }

    /** Cumulative recipe list for the role at the given tier. */
    private List<MerchantRecipe> buildRecipes(TraderRole role, int tier) {
        List<MerchantRecipe> out = new ArrayList<>();
        int uses = 9999;
        switch (role) {
            case BLACKSMITH -> {
                if (tier >= 0) {
                    out.add(recipe(stack(Material.IRON_INGOT, 8), uses, List.of(emeralds(1))));
                    out.add(recipe(stack(Material.IRON_SWORD, 1), uses, List.of(emeralds(3))));
                    out.add(recipe(stack(Material.SHIELD, 1), uses, List.of(emeralds(2))));
                }
                if (tier >= 1) {
                    out.add(recipe(stack(Material.DIAMOND, 1), uses, List.of(emeralds(5))));
                    out.add(recipe(stack(Material.IRON_INGOT, 16), uses, List.of(emeralds(4))));
                }
                if (tier >= 2) {
                    out.add(recipe(runeCache(), uses, List.of(emeralds(2))));
                    out.add(recipe(stack(Material.NETHERITE_INGOT, 1), uses, List.of(emeralds(16))));
                }
                if (tier >= 3) {
                    out.add(recipe(cardinalWeapon("dragon_slayer"), 3, List.of(emeralds(48), stack(Material.NETHERITE_BLOCK, 1))));
                    out.add(recipe(cardinalWeapon("true_gorikimaru"), 1, List.of(emeralds(96), stack(Material.NETHERITE_BLOCK, 2))));
                }
            }
            case FARMER -> {
                if (tier >= 0) {
                    out.add(recipe(emeralds(1), uses, List.of(stack(Material.WHEAT, 20))));
                    out.add(recipe(stack(Material.BREAD, 16), uses, List.of(emeralds(1))));
                    out.add(recipe(stack(Material.CARROT, 12), uses, List.of(emeralds(1))));
                    out.add(recipe(stack(Material.POTATO, 10), uses, List.of(emeralds(1))));
                }
                if (tier >= 1) {
                    out.add(recipe(stack(Material.GOLDEN_CARROT, 8), uses, List.of(emeralds(3))));
                    out.add(recipe(stack(Material.BEETROOT, 16), uses, List.of(emeralds(2))));
                }
                if (tier >= 2) {
                    out.add(recipe(stack(Material.GOLDEN_APPLE, 1), uses, List.of(emeralds(6))));
                    out.add(recipe(stack(Material.COOKED_BEEF, 16), uses, List.of(emeralds(4))));
                }
                if (tier >= 3) {
                    out.add(recipe(stack(Material.ENCHANTED_GOLDEN_APPLE, 1), 5, List.of(emeralds(40))));
                }
            }
            case ALCHEMIST -> {
                if (tier >= 0) {
                    out.add(recipe(stack(Material.BLAZE_POWDER, 3), uses, List.of(emeralds(1))));
                    out.add(recipe(stack(Material.REDSTONE, 4), uses, List.of(emeralds(1))));
                    out.add(recipe(stack(Material.GLOWSTONE_DUST, 2), uses, List.of(emeralds(1))));
                }
                if (tier >= 1) {
                    out.add(recipe(stack(Material.BLAZE_ROD, 1), uses, List.of(emeralds(2))));
                    out.add(recipe(stack(Material.GHAST_TEAR, 1), uses, List.of(emeralds(3))));
                }
                if (tier >= 2) {
                    out.add(recipe(runeCache(), uses, List.of(emeralds(2))));
                    out.add(recipe(stack(Material.EXPERIENCE_BOTTLE, 8), uses, List.of(emeralds(5))));
                }
                if (tier >= 3) {
                    out.add(recipe(stack(Material.NETHER_STAR, 1), 5, List.of(emeralds(64), stack(Material.BLAZE_ROD, 1))));
                }
            }
            case SCRIBE -> {
                if (tier >= 0) {
                    out.add(recipe(stack(Material.PAPER, 4), uses, List.of(emeralds(1))));
                    out.add(recipe(stack(Material.BOOK, 1), uses, List.of(emeralds(2))));
                }
                if (tier >= 1) {
                    out.add(recipe(codexBook("tempest_federation"), 1, List.of(emeralds(6))));
                    out.add(recipe(runeCache(), uses, List.of(emeralds(2))));
                }
                if (tier >= 2) {
                    out.add(recipe(codexBook("true_dragons"), 1, List.of(emeralds(10))));
                }
                if (tier >= 3) {
                    out.add(recipe(codexBook("cardinal_weapons"), 1, List.of(emeralds(20))));
                }
            }
        }
        return out;
    }
}
