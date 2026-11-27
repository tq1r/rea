package dev.rea.command;

import dev.rea.ReaPlugin;
import dev.rea.data.PlayerData;
import dev.rea.race.RaceType;
import dev.rea.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * /rea admin — a chest menu exposing literally everything an admin can do,
 * split into subcategories. Geyser-safe: plain inventories only.
 *
 * Admins act on a selected target (defaults to themselves). Destructive
 * actions require a double-click confirmation rendered in-menu.
 */
public final class AdminGui {

    private final ReaPlugin plugin;
    private final NamespacedKey actionKey;
    private final Map<UUID, UUID> selectedTarget = new HashMap<>();
    private final Map<UUID, String> pendingConfirm = new HashMap<>();

    public AdminGui(ReaPlugin plugin) {
        this.plugin = plugin;
        this.actionKey = new NamespacedKey(plugin, "admin_action");
    }

    public NamespacedKey actionKey() {
        return actionKey;
    }

    // ---- target selection ----

    public Player target(Player admin) {
        UUID id = selectedTarget.getOrDefault(admin.getUniqueId(), admin.getUniqueId());
        Player target = Bukkit.getPlayer(id);
        return target != null ? target : admin;
    }

    public void selectTarget(Player admin, Player target) {
        selectedTarget.put(admin.getUniqueId(), target.getUniqueId());
    }

    public String pending(Player admin) {
        return pendingConfirm.get(admin.getUniqueId());
    }

    public void setPending(Player admin, String action) {
        pendingConfirm.put(admin.getUniqueId(), action);
    }

    public void clearPending(Player admin) {
        pendingConfirm.remove(admin.getUniqueId());
    }

    // ---- main menu ----

    public void open(Player admin) {
        clearPending(admin);
        Inventory inv = Bukkit.createInventory(null, 27, Msg.c("&8Rea Admin"));
        Player target = target(admin);
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = head.getItemMeta();
        if (meta instanceof SkullMeta skull) {
            skull.setOwningPlayer(target);
        }
        meta.setDisplayName(Msg.c("&eTarget: &f" + target.getName()));
        PlayerData data = plugin.getDataManager().get(target);
        RaceType race = RaceType.fromId(data.getRace());
        List<String> lore = new ArrayList<>();
        lore.add(Msg.c("&7Race: &f" + (race == null ? "None" : race.formName(data.getForm()))));
        lore.add(Msg.c("&7Level: &f" + data.getLevel() + " &8| &7Runes: &f" + data.getRunes()));
        lore.add(Msg.c("&eClick to switch target."));
        meta.setLore(lore);
        head.setItemMeta(meta);
        tag(head, "menu:select");
        inv.setItem(4, head);

        inv.setItem(10, item(Material.ENDERMAN_SPAWN_EGG, "&c&lRace",
                List.of("&7Assign any of the 10 races,", "&7reroll the Unique Skill,", "&7or force an evolution."),
                "menu:race"));
        inv.setItem(12, item(Material.BEACON, "&b&lSkills",
                List.of("&7Grant or wipe every skill,", "&7refill magicules."),
                "menu:skills"));
        inv.setItem(14, item(Material.EMERALD, "&a&lResources",
                List.of("&7Runes, XP, levels,", "&7Rune Caches and EP."),
                "menu:resources"));
        inv.setItem(16, item(Material.WRITTEN_BOOK, "&d&lLore & Quests",
                List.of("&7Record the whole Codex,", "&7or finish the story."),
                "menu:lore"));
        inv.setItem(19, item(Material.CLOCK, "&e&lGameplay",
                List.of("&7Reincarnate everyone, save all,", "&7reload, check for updates."),
                "menu:gameplay"));
        inv.setItem(22, item(Material.TNT, "&4&lDanger Zone",
                List.of("&7Total data resets.", "&cRequires double-click confirmation."),
                "menu:danger"));
        inv.setItem(26, item(Material.BARRIER, "&7Close", List.of("&7Close this menu."), "menu:close"));
        admin.openInventory(inv);
    }

    public void openSelect(Player admin) {
        clearPending(admin);
        Inventory inv = Bukkit.createInventory(null, 54, Msg.c("&8Rea Admin · Choose Target"));
        int slot = 0;
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (slot >= 45) {
                break;
            }
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            ItemMeta meta = head.getItemMeta();
            if (meta instanceof SkullMeta skull) {
                skull.setOwningPlayer(online);
            }
            PlayerData data = plugin.getDataManager().get(online);
            RaceType race = RaceType.fromId(data.getRace());
            meta.setDisplayName(Msg.c("&e" + online.getName()));
            meta.setLore(List.of(
                    Msg.c("&7Race: &f" + (race == null ? "None" : race.formName(data.getForm()))),
                    Msg.c("&7Level: &f" + data.getLevel()),
                    Msg.c("&aClick to target them.")));
            head.setItemMeta(meta);
            tag(head, "select:" + online.getUniqueId());
            inv.setItem(slot++, head);
        }
        inv.setItem(53, back());
        admin.openInventory(inv);
    }

    public void openRace(Player admin) {
        clearPending(admin);
        Inventory inv = Bukkit.createInventory(null, 54, Msg.c("&8Rea Admin · Race"));
        int slot = 0;
        for (RaceType race : RaceType.values()) {
            inv.setItem(slot, item(Material.ENDERMAN_SPAWN_EGG, race.getColor() + "&l" + race.getDisplayName(),
                    List.of("&7Full fresh reincarnation", "&7as this race (wipes old data).",
                            "&8Intrinsic: &7" + race.getIntrinsicName()),
                    "race:set:" + race.getId()));
            slot++;
        }
        inv.setItem(12, item(Material.CHORUS_FRUIT, "&d&lRandom Race",
                List.of("&7Fresh reincarnation as a", "&7random race."), "race:random"));
        inv.setItem(14, item(Material.NETHER_STAR, "&b&lRe-roll Unique Skill",
                List.of("&7Replace the target's Unique Skill", "&7with a fresh random roll."), "skill:reroll_unique"));
        inv.setItem(30, item(Material.BEACON, "&b&lForce Evolve",
                List.of("&7Advance one form.", "&7Ignores EP/level/rune requirements.", "&7Demon Lord awakening works."), "race:evolve"));
        inv.setItem(32, item(Material.DRAGON_EGG, "&5&lAscend to Demon Lord",
                List.of("&7Advance all remaining forms", "&7in one click."), "race:max"));
        inv.setItem(45, back());
        inv.setItem(49, targetEcho(admin));
        admin.openInventory(inv);
    }

    public void openSkills(Player admin) {
        clearPending(admin);
        Inventory inv = Bukkit.createInventory(null, 27, Msg.c("&8Rea Admin · Skills"));
        inv.setItem(10, item(Material.ENCHANTING_TABLE, "&d&lGrant ALL Skills",
                List.of("&7Every Intrinsic, Common,", "&7Unique and Ultimate skill."), "skill:grant_all"));
        inv.setItem(13, item(Material.LAVA_BUCKET, "&c&lWipe All Skills",
                List.of("&7Removes every skill,", "&7including intrinsics."), "skill:clear"));
        inv.setItem(16, item(Material.GOLDEN_APPLE, "&b&lRefill Magicules",
                List.of("&7Set magicules to maximum."), "skill:refill"));
        inv.setItem(18, back());
        inv.setItem(22, targetEcho(admin));
        admin.openInventory(inv);
    }

    public void openResources(Player admin) {
        clearPending(admin);
        Inventory inv = Bukkit.createInventory(null, 27, Msg.c("&8Rea Admin · Resources"));
        inv.setItem(2, item(Material.EMERALD, "&a+1,000 Runes", List.of("&7Add 1,000 runes."), "resources:runes:1000"));
        inv.setItem(3, item(Material.EMERALD_BLOCK, "&a+10,000 Runes", List.of("&7Add 10,000 runes."), "resources:runes:10000"));
        inv.setItem(4, item(Material.DIAMOND_BLOCK, "&a+100,000 Runes", List.of("&7Add 100,000 runes."), "resources:runes:100000"));
        inv.setItem(6, item(Material.EXPERIENCE_BOTTLE, "&b+500 XP", List.of("&7Add 500 XP."), "resources:xp:500"));
        inv.setItem(7, item(Material.EXPERIENCE_BOTTLE, "&b+2,000 XP", List.of("&7Add 2,000 XP."), "resources:xp:2000"));
        inv.setItem(8, item(Material.BLAZE_POWDER, "&e+1 Level", List.of("&7Add 1 level."), "resources:level:1"));
        inv.setItem(9, item(Material.BLAZE_POWDER, "&e+5 Levels", List.of("&7Add 5 levels."), "resources:level:5"));
        inv.setItem(11, item(Material.AMETHYST_SHARD, "&dRune Cache ×1", List.of("&7Give 1 Rune Cache."), "resources:cache:1"));
        inv.setItem(12, item(Material.AMETHYST_CLUSTER, "&dRune Cache ×16", List.of("&7Give 16 Rune Caches."), "resources:cache:16"));
        inv.setItem(13, item(Material.BUDDING_AMETHYST, "&dRune Cache ×64", List.of("&7Give 64 Rune Caches."), "resources:cache:64"));
        inv.setItem(15, item(Material.BARRIER, "&4Wipe EP", List.of("&7Set Existence Points to 0.", "&cRank falls back to F."), "resources:wipe_ep"));
        inv.setItem(18, back());
        inv.setItem(22, targetEcho(admin));
        admin.openInventory(inv);
    }

    public void openLore(Player admin) {
        clearPending(admin);
        Inventory inv = Bukkit.createInventory(null, 27, Msg.c("&8Rea Admin · Lore & Quests"));
        inv.setItem(10, item(Material.KNOWLEDGE_BOOK, "&d&lRecord ALL Codex Entries",
                List.of("&7Mark every one of the 26", "&7entries as recorded."), "lore:grant_codex"));
        inv.setItem(11, item(Material.BOOK, "&7Clear Codex",
                List.of("&7Wipe the target's recorded Codex."), "lore:clear_codex"));
        inv.setItem(13, item(Material.WRITTEN_BOOK, "&a&lComplete ALL Quests",
                List.of("&7Mark the story complete", "&7for the target."), "lore:complete_quests"));
        inv.setItem(15, item(Material.PAPER, "&cReset Quest Story",
                List.of("&7Clear quest completion", "&7and all progress counters."), "lore:clear_quests"));
        inv.setItem(18, back());
        inv.setItem(22, targetEcho(admin));
        admin.openInventory(inv);
    }

    public void openGameplay(Player admin) {
        clearPending(admin);
        Inventory inv = Bukkit.createInventory(null, 27, Msg.c("&8Rea Admin · Gameplay"));
        inv.setItem(10, item(Material.ENDER_EYE, "&d&lReincarnate Everyone",
                List.of("&7Deal random races to every online", "&7wanderer without one (/rea start)."), "gameplay:start"));
        inv.setItem(11, item(Material.BARREL, "&eSave All Data",
                List.of("&7Write every cached player file now."), "gameplay:save"));
        inv.setItem(13, item(Material.REPEATER, "&aReload Config",
                List.of("&7Reload config.yml, codex.yml", "&7and quests.yml."), "gameplay:reload"));
        inv.setItem(15, item(Material.COMPARATOR, "&bCheck for Update",
                List.of("&7Poll GitHub Releases now."), "gameplay:update"));
        inv.setItem(18, back());
        admin.openInventory(inv);
    }

    public void openDanger(Player admin, String confirmAction) {
        Inventory inv = Bukkit.createInventory(null, 27, Msg.c("&8Rea Admin · Danger Zone"));
        ItemStack resetTarget = dangerItem(
                item(Material.SKELETON_SKULL, "&c&lReset Target's Data",
                        List.of("&7Full wipe: race, skills, runes,", "&7codex, quests, everything.", "&cClick once to arm, again to fire."),
                        "danger:reset"), confirmAction, "danger:reset");
        ItemStack resetAll = dangerItem(
                item(Material.WITHER_SKELETON_SKULL, "&4&lReset ALL Online Players",
                        List.of("&7Full wipe for everyone online.", "&cClick once to arm, again to fire."),
                        "danger:reset_all"), confirmAction, "danger:reset_all");
        inv.setItem(10, resetTarget);
        inv.setItem(13, resetAll);
        inv.setItem(16, item(Material.IRON_SWORD, "&eKick Target from Dungeon",
                List.of("&7Abandon their current ruin run."), "danger:kick_dungeon"));
        inv.setItem(18, back());
        inv.setItem(22, targetEcho(admin));
        admin.openInventory(inv);
    }

    // ---- helpers ----

    private ItemStack targetEcho(Player admin) {
        Player target = target(admin);
        return item(Material.PLAYER_HEAD, "&7Target: &f" + target.getName(),
                List.of("&7Switch with the skull in the main menu."), "menu:main");
    }

    private ItemStack back() {
        return item(Material.ARROW, "&7Back", List.of("&7Return to the main menu."), "menu:main");
    }

    private ItemStack dangerItem(ItemStack base, String confirmAction, String action) {
        if (action.equals(confirmAction)) {
            ItemStack confirm = new ItemStack(Material.BARRIER);
            ItemMeta meta = confirm.getItemMeta();
            meta.setDisplayName(Msg.c("&4&lCONFIRM — CLICK AGAIN"));
            meta.setLore(List.of(Msg.c("&cThis cannot be undone.")));
            confirm.setItemMeta(meta);
            tag(confirm, action);
            return confirm;
        }
        return base;
    }

    public ItemStack tag(ItemStack item, String action) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(actionKey, PersistentDataType.STRING, action);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack item(Material material, String name, List<String> lore, String action) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Msg.c(name));
            List<String> colored = new ArrayList<>();
            for (String line : lore) {
                colored.add(Msg.c(line));
            }
            meta.setLore(colored);
            stack.setItemMeta(meta);
        }
        return tag(stack, action);
    }
}
