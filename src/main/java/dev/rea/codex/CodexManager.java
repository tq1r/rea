package dev.rea.codex;

import dev.rea.ReaPlugin;
import dev.rea.data.PlayerData;
import dev.rea.util.Msg;
import dev.rea.util.Util;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Loads codex.yml, builds Codex Page books, and records entries when read.
 * Books are written books (Bedrock-safe) and can be copied and shared.
 */
public final class CodexManager {
    private final ReaPlugin plugin;
    private final Map<String, CodexEntry> entries = new LinkedHashMap<>();

    public CodexManager(ReaPlugin plugin) {
        this.plugin = plugin;
        load();
    }

    private void load() {
        File file = new File(plugin.getDataFolder(), "codex.yml");
        if (!file.exists()) {
            plugin.saveResource("codex.yml", false);
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = config.getConfigurationSection("codex");
        if (section == null) {
            plugin.getLogger().warning("codex.yml has no 'codex' section; no lore loaded.");
            return;
        }
        for (String id : section.getKeys(false)) {
            ConfigurationSection entry = section.getConfigurationSection(id);
            if (entry == null) {
                continue;
            }
            entries.put(id, new CodexEntry(
                    id,
                    entry.getString("title", id),
                    entry.getString("subtitle", ""),
                    entry.getStringList("pages"),
                    entry.getLong("reward-runes", 10L),
                    entry.getInt("reward-xp", 10)));
        }
        plugin.getLogger().info("Loaded " + entries.size() + " Codex entries.");
    }

    public void reload() {
        entries.clear();
        load();
    }

    public CodexEntry getEntry(String id) {
        return entries.get(id);
    }

    public Collection<CodexEntry> allEntries() {
        return entries.values();
    }

    public int total() {
        return entries.size();
    }

    public ItemStack createBook(CodexEntry entry) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        ItemMeta meta = book.getItemMeta();
        if (meta instanceof BookMeta written) {
            written.setTitle("Codex: " + entry.title());
            written.setAuthor("Rea");
            written.setGeneration(BookMeta.Generation.ORIGINAL);
            for (String page : entry.pages()) {
                written.addPages(Component.text(page));
            }
            written.getPersistentDataContainer().set(plugin.getKeys().codex(), PersistentDataType.STRING, entry.id());
            book.setItemMeta(written);
        }
        return book;
    }

    public String getCodexId(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        return meta.getPersistentDataContainer().get(plugin.getKeys().codex(), PersistentDataType.STRING);
    }

    public void givePage(Player player, String entryId) {
        CodexEntry entry = getEntry(entryId);
        if (entry == null) {
            return;
        }
        Util.giveItem(player, createBook(entry));
        Msg.send(player, "&7You received a Codex Page: &d" + entry.title());
    }

    /**
     * Read a page. Returns true if this was the player's first reading
     * (the caller should then consume the book).
     */
    public boolean readBook(Player player, String entryId) {
        CodexEntry entry = getEntry(entryId);
        if (entry == null) {
            return false;
        }
        PlayerData data = plugin.getDataManager().get(player);
        showEntry(player, entry);
        if (data.getCodex().contains(entryId)) {
            Msg.send(player, "&7(Already recorded in your Codex.)");
            return false;
        }
        data.getCodex().add(entryId);
        Msg.send(player, "&dCodex entry recorded: &l" + entry.title() + " &7(" + data.getCodex().size() + "/" + total() + ")");
        plugin.getDataManager().addRunes(player, entry.rewardRunes());
        plugin.getDataManager().addXp(player, entry.rewardXp());
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1f);
        plugin.getQuestManager().checkCompletion(player);
        plugin.getDataManager().save(data);
        return true;
    }

    public void showEntry(Player player, CodexEntry entry) {
        Msg.send(player, Msg.bar());
        Msg.send(player, "&d&l" + entry.title());
        if (!entry.subtitle().isEmpty()) {
            Msg.send(player, "&7" + entry.subtitle());
        }
        for (String page : entry.pages()) {
            Msg.send(player, "&f" + page);
        }
        Msg.send(player, Msg.bar());
    }
}
