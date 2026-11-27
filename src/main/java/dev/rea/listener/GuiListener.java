package dev.rea.listener;

import dev.rea.ReaPlugin;
import dev.rea.command.EvolveGui;
import dev.rea.command.SkillsGui;
import dev.rea.data.PlayerData;
import dev.rea.util.Msg;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

/**
 * Handles clicks in the skills and evolution GUIs.
 */
public final class GuiListener implements Listener {

    private final ReaPlugin plugin;
    private final SkillsGui skillsGui;
    private final EvolveGui evolveGui;

    public GuiListener(ReaPlugin plugin, SkillsGui skillsGui, EvolveGui evolveGui) {
        this.plugin = plugin;
        this.skillsGui = skillsGui;
        this.evolveGui = evolveGui;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        String title = event.getView().getTitle();
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getItemMeta() == null) {
            return;
        }
        if (title.contains("Skills")) {
            event.setCancelled(true);
            String skillId = item.getItemMeta().getPersistentDataContainer().get(
                    new NamespacedKey(plugin, "skill"), PersistentDataType.STRING);
            if (skillId != null) {
                plugin.getSkillManager().useSkill(player, skillId);
            }
        } else if (title.contains("Evolution")) {
            event.setCancelled(true);
            Byte flag = item.getItemMeta().getPersistentDataContainer().get(
                    new NamespacedKey(plugin, "evolve"), PersistentDataType.BYTE);
            if (flag != null) {
                PlayerData data = plugin.getDataManager().get(player);
                if (plugin.getRaceManager().canEvolve(data)) {
                    plugin.getRaceManager().evolve(player);
                    evolveGui.open(player);
                } else {
                    Msg.send(player, "&cYou cannot evolve yet.");
                }
            }
        }
    }
}
