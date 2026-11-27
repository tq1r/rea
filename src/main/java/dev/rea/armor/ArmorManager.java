package dev.rea.armor;

import dev.rea.ReaPlugin;
import dev.rea.util.Msg;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mythic armor: the <b>Tempest Sovereign</b> set — four relic plates from the
 * Jura Tempest Federation's treasury, won only in the ruins (or granted by an
 * admin). Netherite stats, Unbreakable, and a small permanent buff each while
 * worn; the full set grants Strength I as a crowning bonus.
 *
 * <p>Deliberately small numbers: the set amplifies vanilla PvP, never replaces
 * it. Bedrock-safe — plain armor materials with vanilla textures.
 */
public final class ArmorManager {

    public record ArmorDef(String id, String name, Material material, String bonus) {
    }

    private static final List<ArmorDef> SET = List.of(
            new ArmorDef("crown", "Sovereign's Crown", Material.NETHERITE_HELMET,
                    "&fNight Vision &7while worn."),
            new ArmorDef("aegis", "Sovereign's Aegis", Material.NETHERITE_CHESTPLATE,
                    "&fResistance I &7while worn."),
            new ArmorDef("greaves", "Sovereign's Greaves", Material.NETHERITE_LEGGINGS,
                    "&fSpeed I &7while worn."),
            new ArmorDef("striders", "Sovereign's Striders", Material.NETHERITE_BOOTS,
                    "&fJump Boost I &7while worn.")
    );

    private final ReaPlugin plugin;
    private final Map<String, ArmorDef> byId = new ConcurrentHashMap<>();

    public ArmorManager(ReaPlugin plugin) {
        this.plugin = plugin;
        for (ArmorDef def : SET) {
            byId.put(def.id(), def);
        }
    }

    public List<ArmorDef> set() {
        return SET;
    }

    public ArmorDef get(String id) {
        return id == null ? null : byId.get(id.toLowerCase());
    }

    public ItemStack create(String id) {
        ArmorDef def = get(id);
        if (def == null) {
            return null;
        }
        ItemStack item = new ItemStack(def.material());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Msg.c("&5&l" + def.name()));
            List<String> lore = new ArrayList<>();
            lore.add(Msg.c("&8Mythic Armor — Tempest Sovereign"));
            lore.add(Msg.c(def.bonus()));
            lore.add(Msg.c("&7Full set: &fStrength I &7while all four are worn."));
            lore.add(Msg.c("&8Recovered from the ruins, never bought."));
            meta.setLore(lore);
            meta.setUnbreakable(true);
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
            meta.addEnchant(Enchantment.PROTECTION, 4, true);
            meta.getPersistentDataContainer().set(
                    plugin.getKeys().mythicArmor(), PersistentDataType.STRING, def.id());
            item.setItemMeta(meta);
        }
        return item;
    }

    /** The PDC id of a mythic armor piece, or null for anything else. */
    public String pieceOf(ItemStack stack) {
        if (stack == null || stack.getType() == Material.AIR) {
            return null;
        }
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return null;
        }
        String id = meta.getPersistentDataContainer().get(
                plugin.getKeys().mythicArmor(), PersistentDataType.STRING);
        return id != null && byId.containsKey(id) ? id : null;
    }

    public boolean isMythicArmor(ItemStack stack) {
        return pieceOf(stack) != null;
    }

    /** Which Sovereign pieces is the player wearing right now? */
    private List<String> worn(Player player) {
        List<String> worn = new ArrayList<>();
        for (ItemStack piece : player.getInventory().getArmorContents()) {
            String id = pieceOf(piece);
            if (id != null) {
                worn.add(id);
            }
        }
        return worn;
    }

    /**
     * Refresh the worn-set buffs as 11-second effects (the same Bedrock-safe
     * pattern as racial passives; re-applied every 10s by the passive task).
     * Kept deliberately modest — this augments vanilla PvP, not replaces it.
     */
    public void applyWorn(Player player) {
        List<String> worn = worn(player);
        if (worn.isEmpty()) {
            return;
        }
        int ticks = 220; // 11 seconds
        if (worn.contains("crown")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, ticks, 0, false, false, true));
        }
        if (worn.contains("aegis")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, ticks, 0, false, false, true));
        }
        if (worn.contains("greaves")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, ticks, 0, false, false, true));
        }
        if (worn.contains("striders")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, ticks, 0, false, false, true));
        }
        if (worn.containsAll(List.of("crown", "aegis", "greaves", "striders"))) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, ticks, 0, false, false, true));
        }
    }

    /**
     * Roll a mythic drop from a dungeon victory. Chances: tier 0 = 5%,
     * tier 1 = 10%, tier 2 = 18%. Returns the item or null.
     */
    public ItemStack rollDrop(int tier) {
        double chance = switch (tier) {
            case 0 -> 0.05;
            case 1 -> 0.10;
            default -> 0.18;
        };
        if (Math.random() >= chance) {
            return null;
        }
        ArmorDef def = SET.get((int) (Math.random() * SET.size()));
        return create(def.id());
    }

    /** Admin helper: hand over one piece, or the whole set when id == "set". */
    public List<ItemStack> grant(String id) {
        if ("set".equalsIgnoreCase(id)) {
            List<ItemStack> all = new ArrayList<>();
            for (ArmorDef def : SET) {
                all.add(create(def.id()));
            }
            return all;
        }
        ItemStack single = create(id);
        return single == null ? List.of() : List.of(single);
    }
}
