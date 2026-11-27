package dev.aetherfall.gui;

import dev.aetherfall.AetherfallPlugin;
import dev.aetherfall.data.PlayerData;
import dev.aetherfall.path.PathType;
import dev.aetherfall.relic.RelicType;
import dev.aetherfall.ruin.Ruin;
import dev.aetherfall.title.TitleDef;
import dev.aetherfall.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * All Aetherfall menus. Standard inventories only, so they open identically
 * on Java and Bedrock clients. Every action is also available as a chat
 * command, so nothing depends on hover text or fancy UI.
 */
public final class GuiManager {
    private final AetherfallPlugin plugin;

    public GuiManager(AetherfallPlugin plugin) {
        this.plugin = plugin;
    }

    private static final class GuiHolder implements InventoryHolder {
        private final String type;
        private Inventory inventory;

        GuiHolder(String type) {
            this.type = type;
        }

        @Override
        public Inventory getInventory() {
            return inventory;
        }
    }

    private Inventory createGui(Player player, String type, int size, String title) {
        GuiHolder holder = new GuiHolder(type);
        Inventory inventory = Bukkit.createInventory(holder, size, Msg.c(title));
        holder.inventory = inventory;
        return inventory;
    }

    public void openPathMenu(Player player) {
        Inventory gui = createGui(player, "path", 27, "&dChoose your Path");
        int slot = 10;
        for (PathType type : PathType.values()) {
            gui.setItem(slot++, plugin.getItemFactory().guiButton(type.getIcon(),
                    type.getColor() + "&l" + type.getDisplayName(),
                    type.getLore(), "path", type.getId()));
        }
        player.openInventory(gui);
    }

    public void openTrader(Player player) {
        Inventory gui = createGui(player, "trader", 54, "&dShard Trader");
        int slot = 0;
        for (RelicType type : RelicType.values()) {
            if (type.getPrice() <= 0) {
                continue;
            }
            ItemStack item = plugin.getItemFactory().guiButton(type.getMaterial(),
                    "&d" + type.getDisplayName(),
                    appendPrice(type.getLore(), type.getPrice()),
                    "buy_relic", type.getId());
            gui.setItem(slot++, item);
        }
        gui.setItem(slot++, plugin.getItemFactory().guiButton(Material.AMETHYST_SHARD, "&dShard Cache",
                appendPrice(List.of("&7Right-click to spend it."), 25L), "buy_cache", "1"));
        for (var entry : plugin.getCodexManager().allEntries()) {
            PlayerData data = plugin.getDataManager().get(player);
            if (data.getCodex().contains(entry.id())) {
                continue;
            }
            gui.setItem(slot++, plugin.getItemFactory().guiButton(Material.WRITTEN_BOOK,
                    "&dCodex Page: " + entry.title(),
                    appendPrice(List.of("&7" + entry.subtitle(), "&7A page of the Vale's story."), 40L),
                    "buy_codex", entry.id()));
        }
        gui.setItem(slot++, plugin.getItemFactory().guiButton(Material.FIREWORK_ROCKET, "&dFireworks x16",
                appendPrice(List.of("&7For celebrations. The Vale loves a good sky."), 10L), "buy_fireworks", "16"));
        player.openInventory(gui);
    }

    private List<String> appendPrice(List<String> lore, long price) {
        java.util.ArrayList<String> copy = new java.util.ArrayList<>(lore);
        copy.add("");
        copy.add("&dPrice: " + price + " shards");
        copy.add("&eClick to buy");
        return copy;
    }

    public void openTitles(Player player) {
        Inventory gui = createGui(player, "titles", 27, "&dTitles");
        PlayerData data = plugin.getDataManager().get(player);
        int slot = 0;
        for (TitleDef def : plugin.getTitleManager().allTitles()) {
            boolean owned = data.getTitles().contains(def.id());
            boolean selected = def.id().equals(data.getSelectedTitle());
            String display = (selected ? "&d&l" : owned ? "&a" : "&7") + def.display();
            List<String> lore = selected
                    ? List.of("&7" + def.description(), "", "&dCurrently worn")
                    : owned ? List.of("&7" + def.description(), "", "&eClick to wear")
                    : List.of("&7" + def.description(), "", "&cLocked");
            gui.setItem(slot++, plugin.getItemFactory().guiButton(Material.NAME_TAG, display, lore, "title", def.id()));
        }
        player.openInventory(gui);
    }

    public void openRuins(Player player) {
        Inventory gui = createGui(player, "ruins", 27, "&dRuins of the Vale");
        PlayerData data = plugin.getDataManager().get(player);
        int slot = 0;
        for (Ruin ruin : plugin.getRuinManager().allRuins()) {
            boolean visited = data.getRuins().contains(ruin.name());
            if (visited) {
                gui.setItem(slot++, plugin.getItemFactory().guiButton(Material.ENDER_STONE,
                        "&a" + ruin.display(),
                        List.of("&7" + ruin.hint(), "", "&eClick to travel (" + plugin.getConfig().getLong("settings.ruin-tp-cost", 10L) + " shards)"),
                        "ruin_tp", ruin.name()));
            } else {
                gui.setItem(slot++, plugin.getItemFactory().guiButton(Material.STONE_BRICKS,
                        "&7???",
                        List.of("&7" + ruin.hint()),
                        "none", null));
            }
        }
        player.openInventory(gui);
    }

    public void handleClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof GuiHolder holder)) {
            return;
        }
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null) {
            return;
        }
        String action = plugin.getItemFactory().guiAction(clicked);
        String arg = plugin.getItemFactory().guiArg(clicked);
        if (action == null) {
            return;
        }
        switch (holder.type) {
            case "path" -> {
                if (action.equals("path")) {
                    PathType type = PathType.fromId(arg);
                    if (type != null) {
                        player.closeInventory();
                        plugin.getPathManager().choosePath(player, type);
                    }
                }
            }
            case "trader" -> {
                switch (action) {
                    case "buy_relic" -> {
                        RelicType type = RelicType.fromId(arg);
                        if (type != null && plugin.getDataManager().takeShards(player, type.getPrice())) {
                            plugin.getRelicManager().giveRelic(player, type);
                            player.closeInventory();
                        }
                    }
                    case "buy_cache" -> {
                        if (plugin.getDataManager().takeShards(player, 25L)) {
                            dev.aetherfall.util.Util.giveItem(player, plugin.getItemFactory().shardCache(1));
                            player.closeInventory();
                        }
                    }
                    case "buy_codex" -> {
                        if (plugin.getDataManager().takeShards(player, 40L)) {
                            plugin.getCodexManager().givePage(player, arg);
                            player.closeInventory();
                        }
                    }
                    case "buy_fireworks" -> {
                        if (plugin.getDataManager().takeShards(player, 10L)) {
                            dev.aetherfall.util.Util.giveItem(player, new ItemStack(Material.FIREWORK_ROCKET, 16));
                            player.closeInventory();
                        }
                    }
                    default -> {
                    }
                }
            }
            case "titles" -> {
                if (action.equals("title")) {
                    plugin.getTitleManager().select(player, arg);
                    player.closeInventory();
                }
            }
            case "ruins" -> {
                if (action.equals("ruin_tp")) {
                    player.closeInventory();
                    plugin.getRuinManager().teleport(player, arg);
                }
            }
            default -> {
            }
        }
    }
}
