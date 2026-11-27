package dev.rea.listener;

import dev.rea.ReaPlugin;
import dev.rea.command.SkillsGui;
import dev.rea.command.DungeonGui;
import dev.rea.data.PlayerData;
import dev.rea.dungeon.DungeonDef;
import dev.rea.util.Msg;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

/**
 * Handles clicks in the Talents and Ruins GUIs.
 */
public final class GuiListener implements Listener {

    private final ReaPlugin plugin;
    private final SkillsGui skillsGui;
    private final DungeonGui dungeonGui;

    public GuiListener(ReaPlugin plugin, SkillsGui skillsGui, DungeonGui dungeonGui) {
        this.plugin = plugin;
        this.skillsGui = skillsGui;
        this.dungeonGui = dungeonGui;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        String title = event.getView().getTitle();
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getItemMeta() == null) {
            if (title.contains("Talents") || title.contains("Ruins")) {
                event.setCancelled(true);
            }
            return;
        }
        if (title.contains("Talents")) {
            event.setCancelled(true);
            Byte evolve = item.getItemMeta().getPersistentDataContainer().get(skillsGui.evolveKey(), PersistentDataType.BYTE);
            if (evolve != null) {
                PlayerData data = plugin.getDataManager().get(player);
                if (plugin.getRaceManager().canEvolve(data)) {
                    plugin.getRaceManager().evolve(player);
                    skillsGui.open(player); // refresh the menu
                } else {
                    Msg.send(player, "&cRequirements not met yet.");
                }
                return;
            }
            String skillId = item.getItemMeta().getPersistentDataContainer().get(skillsGui.skillKey(), PersistentDataType.STRING);
            if (skillId != null) {
                plugin.getSkillManager().useSkill(player, skillId);
            }
            return;
        }
        if (title.contains("Ruins")) {
            event.setCancelled(true);
            String dungeonId = item.getItemMeta().getPersistentDataContainer().get(dungeonGui.dungeonKey(), PersistentDataType.STRING);
            if (dungeonId != null) {
                DungeonDef def = plugin.getDungeonManager().get(dungeonId);
                if (def != null) {
                    player.closeInventory();
                    plugin.getDungeonManager().enter(player, def);
                }
            }
        }
    }
}
