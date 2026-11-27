package dev.rea.weapon;

import dev.rea.ReaPlugin;
import dev.rea.util.Msg;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cardinal weapons: named, high-grade armaments from Tensura, forged as items
 * and won in the dungeons. Canon sources:
 * <ul>
 *   <li>Demon Slayer (破邪の剣) — Saare's blade.</li>
 *   <li>Dragon Slayer (斬竜剛刀) — the greatsword Rimuru gifted Youm Farmenas.</li>
 *   <li>Gorikimaru (剛力丸) — Shion's ōdachi, forged by Kurobe.</li>
 *   <li>Dragon Buster (竜破聖剣) — the Holy Sword given by the Seven Luminaries to Hinata.</li>
 *   <li>Curse Sword (怨嗟の剣) — Albert's blade that drinks life force.</li>
 *   <li>True Gorikimaru (真・剛力丸) — Shion's blade, revived by her love.</li>
 *   <li>Flame Pillar — Leon Cromwell's blazing sword.</li>
 *   <li>Night Rose — Luminous Valentine's sword.</li>
 *   <li>Truth (真意の長剣) — Granbell Rosso's rapier, lent to Hinata.</li>
 *   <li>Crumbling Fang — Dino's weapon.</li>
 *   <li>Moon Mistress — Chloe Aubert's blade.</li>
 *   <li>Ark — Feldway's weapon.</li>
 *   <li>Deva — Masayuki Rudra's weapon.</li>
 *   <li>Asura (天魔) — Veldanava's scimitar, Milim's treasure.</li>
 * </ul>
 */
public final class WeaponManager {
    public static final String GRADE_UNIQUE = "Unique";
    public static final String GRADE_LEGEND = "Legend";
    public static final String GRADE_MYTHICAL = "Mythical";
    public static final String GRADE_GENESIS = "Genesis";

    private final ReaPlugin plugin;
    private final Map<String, WeaponDef> weapons = new ConcurrentHashMap<>();

    public WeaponManager(ReaPlugin plugin) {
        this.plugin = plugin;
        register(new WeaponDef("demon_slayer", "Demon Slayer", GRADE_UNIQUE, Material.NETHERITE_SWORD,
                8, 0, 0, List.of("&oThe Sword of Evil-Crushing.", "&oSaare's blade, forged to cut wickedness.")));
        register(new WeaponDef("dragon_slayer", "Dragon Slayer", GRADE_UNIQUE, Material.NETHERITE_AXE,
                8, 0, 0, List.of("&oThe Dragon-Rending Greatsword.", "&oGifted by Rimuru to Youm Farmenas.")));
        register(new WeaponDef("gorikimaru", "Gorikimaru", GRADE_UNIQUE, Material.NETHERITE_SWORD,
                8, 0, 0, List.of("&oShion's ōdachi, forged by Kurobe.", "&oImpractically large. Perfectly her.")));
        register(new WeaponDef("dragon_buster", "Dragon Buster", GRADE_UNIQUE, Material.NETHERITE_SWORD,
                8, 2, 0, List.of("&oThe Dragon-Break Holy Sword.", "&oForged by the Seven Luminaries for Hinata Sakaguchi.")));
        register(new WeaponDef("curse_sword", "Curse Sword", GRADE_UNIQUE, Material.NETHERITE_SWORD,
                8, 0, 3, List.of("&oThe Sword of Resentment.", "&oAlbert's blade; it drinks the life of all around it.")));
        register(new WeaponDef("true_gorikimaru", "True Gorikimaru", GRADE_LEGEND, Material.NETHERITE_SWORD,
                12, 0, 0, List.of("&oTrue Gorikimaru — revived by love itself.", "&oShion's blade ascended from Unique to Legend grade.")));
        register(new WeaponDef("flame_pillar", "Flame Pillar", GRADE_LEGEND, Material.NETHERITE_SWORD,
                12, 2, 0, List.of("&oA pillar of living fire in sword form.", "&oLeon Cromwell's blazing edge.")));
        register(new WeaponDef("night_rose", "Night Rose", GRADE_LEGEND, Material.NETHERITE_SWORD,
                12, 0, 0, List.of("&oLuminous Valentine's sword.", "&oElegant, merciless, and radiant.")));
        register(new WeaponDef("truth", "Truth", GRADE_LEGEND, Material.NETHERITE_SWORD,
                12, 0, 0, List.of("&oThe Longsword of Truth.", "&oGranbell Rosso's rapier, carried by Hinata Sakaguchi.")));
        register(new WeaponDef("crumbling_fang", "Crumbling Fang", GRADE_MYTHICAL, Material.NETHERITE_AXE,
                15, 2, 0, List.of("&oDino's weapon.", "&oIt gnaws at whatever it cannot cut.")));
        register(new WeaponDef("moon_mistress", "Moon Mistress", GRADE_MYTHICAL, Material.NETHERITE_SWORD,
                15, 0, 3, List.of("&oChloe Aubert's blade.", "&oIt carves its mistress across the night.")));
        register(new WeaponDef("ark", "Ark", GRADE_MYTHICAL, Material.NETHERITE_SWORD,
                15, 0, 0, List.of("&oFeldway's weapon.", "&oA vessel for the storm that never ends.")));
        register(new WeaponDef("deva", "Deva", GRADE_MYTHICAL, Material.NETHERITE_SWORD,
                15, 0, 0, List.of("&oMasayuki Rudra's weapon.", "&oNo prayer survives it.")));
        register(new WeaponDef("asura", "Asura", GRADE_GENESIS, Material.NETHERITE_SWORD,
                20, 2, 0, List.of("&oAsura — the Heavenly Demon.", "&oOne of the Seven Genesis-grade weapons, forged by Veldanava. Milim's treasure.")));
    }

    private void register(WeaponDef def) {
        weapons.put(def.id(), def);
    }

    public Map<String, WeaponDef> allWeapons() {
        return Collections.unmodifiableMap(weapons);
    }

    public WeaponDef get(String id) {
        return id == null ? null : weapons.get(id.toLowerCase());
    }

    public List<WeaponDef> poolForTier(int tier) {
        // tier 0 = Ogre Hunt (easy), 1 = Orc Lord's Fortress (normal), 2 = Vál Velmyra (hard)
        List<WeaponDef> pool = new ArrayList<>();
        for (WeaponDef def : weapons.values()) {
            switch (tier) {
                case 0 -> {
                    if (GRADE_UNIQUE.equals(def.grade())) {
                        pool.add(def);
                    }
                }
                case 1 -> {
                    if (GRADE_UNIQUE.equals(def.grade()) || GRADE_LEGEND.equals(def.grade())) {
                        pool.add(def);
                    }
                }
                default -> {
                    if (!GRADE_UNIQUE.equals(def.grade())) {
                        pool.add(def);
                    } else if (Math.random() < 0.25) {
                        pool.add(def);
                    }
                }
            }
        }
        return pool;
    }

    public ItemStack create(WeaponDef def) {
        ItemStack item = new ItemStack(def.material());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String gradeColor = switch (def.grade()) {
                case GRADE_GENESIS -> "&4&l";
                case GRADE_MYTHICAL -> "&d&l";
                case GRADE_LEGEND -> "&6&l";
                default -> "&a&l";
            };
            meta.setDisplayName(Msg.c(gradeColor + def.name()));
            List<String> lore = new ArrayList<>();
            lore.add(Msg.c("&7Grade: " + gradeColor.replace("&l", "") + def.grade()));
            lore.add(Msg.c("&8Cardinal Weapon"));
            for (String line : def.lore()) {
                lore.add(Msg.c(line));
            }
            lore.add(Msg.c("&7Holds " + "&f+10% EP &7when held."));
            meta.setLore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            meta.addEnchant(Enchantment.SHARPNESS, def.sharpness(), true);
            if (def.fireAspect() > 0) {
                meta.addEnchant(Enchantment.FIRE_ASPECT, def.fireAspect(), true);
            }
            if (def.looting() > 0) {
                meta.addEnchant(Enchantment.LOOTING, def.looting(), true);
            }
            meta.getPersistentDataContainer().set(plugin.getKeys().cardinal(), PersistentDataType.STRING, def.id());
            item.setItemMeta(meta);
        }
        return item;
    }

    public WeaponDef weaponOf(ItemStack stack) {
        if (stack == null || stack.getType() == Material.AIR) {
            return null;
        }
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return null;
        }
        String id = meta.getPersistentDataContainer().get(plugin.getKeys().cardinal(), PersistentDataType.STRING);
        return id == null ? null : weapons.get(id);
    }

    public boolean isCardinal(ItemStack stack) {
        return weaponOf(stack) != null;
    }

    /** Random cardinal weapon from the pool for a dungeon tier (0-2), or null. */
    public ItemStack rollWeapon(int tier, double chance) {
        if (Math.random() >= chance) {
            return null;
        }
        List<WeaponDef> pool = poolForTier(tier);
        if (pool.isEmpty()) {
            return null;
        }
        if (tier == 2) {
            // Hard dungeon: Legend 50%, Mythical 40%, Genesis 10%.
            double roll = Math.random();
            List<WeaponDef> legend = new ArrayList<>();
            List<WeaponDef> mythical = new ArrayList<>();
            List<WeaponDef> genesis = new ArrayList<>();
            for (WeaponDef def : pool) {
                if (GRADE_GENESIS.equals(def.grade())) {
                    genesis.add(def);
                } else if (GRADE_MYTHICAL.equals(def.grade())) {
                    mythical.add(def);
                } else {
                    legend.add(def);
                }
            }
            List<WeaponDef> chosen = roll < 0.10 ? genesis : roll < 0.50 ? mythical : legend;
            if (chosen.isEmpty()) {
                chosen = pool;
            }
            return create(chosen.get((int) (Math.random() * chosen.size())));
        }
        return create(pool.get((int) (Math.random() * pool.size())));
    }
}
