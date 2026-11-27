package dev.rea.listener;

import dev.rea.ReaPlugin;
import dev.rea.data.PlayerData;
import dev.rea.skill.SkillDef;
import dev.rea.util.Msg;
import dev.rea.util.Util;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The connective tissue: mining, killing, crafting, combat, and reading
 * all feed quests, runes, and XP through here.
 *
 * Canon (Tensura): kills grant EP to everyone, but only Predator holders
 * (the Slime path) devour foes to learn Common Skills.
 */
public final class GameListener implements Listener {
    private final ReaPlugin plugin;

    public GameListener(ReaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerData data = plugin.getDataManager().get(player);
        data.setName(player.getName());
        plugin.getRaceManager().applyPassives(player);
        plugin.getSkillManager().reissueOrbs(player);
        plugin.getHudManager().update(player);
        if (data.getRace() == null) {
            for (String line : plugin.getConfig().getStringList("messages.welcome-no-race")) {
                Msg.send(player, line);
            }
        } else {
            for (String line : plugin.getConfig().getStringList("messages.welcome")) {
                Msg.send(player, line);
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getDataManager().save(plugin.getDataManager().get(event.getPlayer()));
        plugin.getHudManager().remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> plugin.getRaceManager().applyPassives(player), 20L);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Material type = event.getBlock().getType();
        if (!Util.isOre(type)) {
            return;
        }
        Player player = event.getPlayer();
        PlayerData data = plugin.getDataManager().get(player);
        data.incrementCounter("mine:ANY", 1);
        data.incrementCounter("mine:" + type.name(), 1);
        data.setMines(data.getMines() + 1);
        double chance = plugin.getConfig().getDouble("settings.rune-mine-chance", 0.15);
        if (Math.random() < chance) {
            int min = plugin.getConfig().getInt("settings.rune-mine-min", 1);
            int max = plugin.getConfig().getInt("settings.rune-mine-max", 3);
            int amount = min + (int) (Math.random() * Math.max(1, max - min + 1));
            plugin.getDataManager().addRunes(player, amount);
            player.getWorld().spawnParticle(Particle.END_ROD, event.getBlock().getLocation(), 8, 0.4, 0.4, 0.4);
            if (Math.random() < 0.25) {
                Msg.send(player, "&7The stone hums as it breaks. Something was hiding in it.");
            }
        }
        plugin.getQuestManager().checkCompletion(player);
    }

    /** EP a monster is worth when slain (Tensura-style). */
    private long predationEp(org.bukkit.entity.EntityType type) {
        return switch (type) {
            case SLIME -> 5L;
            case ZOMBIE -> 8L;
            case SKELETON -> 10L;
            case SPIDER, CAVE_SPIDER -> 12L;
            case CREEPER -> 15L;
            case WITCH -> 20L;
            case BLAZE -> 25L;
            case ENDERMAN -> 30L;
            default -> 10L;
        };
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();
        if (killer == null || !(entity instanceof Monster)) {
            return;
        }
        PlayerData data = plugin.getDataManager().get(killer);
        data.incrementCounter("kill:ANY", 1);
        data.incrementCounter("kill:" + entity.getType().name(), 1);
        data.setKills(data.getKills() + 1);
        plugin.getDataManager().addXp(killer, plugin.getConfig().getInt("settings.xp-per-kill", 3));
        double chance = plugin.getConfig().getDouble("settings.kill-rune-chance", 0.10);
        if (Math.random() < chance) {
            int min = plugin.getConfig().getInt("settings.kill-rune-min", 1);
            int max = plugin.getConfig().getInt("settings.kill-rune-max", 2);
            int amount = min + (int) (Math.random() * Math.max(1, max - min + 1));
            plugin.getDataManager().addRunes(killer, amount);
        }
        // Everyone gains EP from slaying monsters.
        plugin.getDataManager().addEp(killer, predationEp(entity.getType()));
        // Predation: only Predator holders devour foes to learn Common Skills.
        if (data.hasSkill("predator")) {
            data.setMagicules(Math.min(plugin.getDataManager().maxMagicules(data), data.getMagicules() + 10));
            Msg.send(killer, "&7You devoured the " + entity.getType().name().toLowerCase().replace('_', ' ') + ".");
            int learnChance = data.hasSkill("great_sage") ? 40 : 20;
            if (ThreadLocalRandom.current().nextInt(100) < learnChance) {
                for (SkillDef def : plugin.getSkillManager().commonSkills()) {
                    if (!data.hasSkill(def.getId())) {
                        plugin.getSkillManager().learn(killer, def.getId());
                        break;
                    }
                }
            }
        }
        plugin.getQuestManager().checkCompletion(killer);
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        Material result = event.getRecipe().getResult().getType();
        PlayerData data = plugin.getDataManager().get(player);
        data.incrementCounter("craft:ANY", 1);
        data.incrementCounter("craft:" + result.name(), 1);
        data.setCrafts(data.getCrafts() + 1);
        plugin.getQuestManager().checkCompletion(player);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null || item.getType() == Material.AIR) {
            return;
        }
        String orbSkill = plugin.getItemFactory().skillOrbSkill(item);
        if (orbSkill != null) {
            event.setCancelled(true);
            UUID owner = plugin.getItemFactory().skillOrbOwner(item);
            if (owner == null || !owner.equals(player.getUniqueId())) {
                Msg.send(player, "&cThis orb is bound to another soul.");
                return;
            }
            plugin.getSkillManager().useSkill(player, orbSkill);
            return;
        }
        String codexId = plugin.getCodexManager().getCodexId(item);
        if (codexId != null) {
            boolean first = plugin.getCodexManager().readBook(player, codexId);
            if (first) {
                consumeOne(player, item);
            }
            event.setCancelled(true);
            return;
        }
        if (plugin.getItemFactory().isRuneCache(item)) {
            int amount = plugin.getConfig().getInt("settings.cache-xp", 40);
            plugin.getDataManager().addXp(player, amount);
            consumeOne(player, item);
            player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1f, 1f);
            player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation(), 20, 0.5, 0.8, 0.5);
            Msg.send(player, "&dThe runes attune. &7+" + amount + " XP");
            event.setCancelled(true);
        }
    }

    private void consumeOne(Player player, ItemStack item) {
        if (item.getAmount() <= 1) {
            player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
        } else {
            item.setAmount(item.getAmount() - 1);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player attacker) {
            PlayerData data = plugin.getDataManager().get(attacker);
            double bonus = data.getForm() * 0.5 + plugin.getDataManager().rankIndex(data) * 0.3;
            if ("orc".equals(data.getRace())) {
                bonus += 2.0;
            }
            if (data.hasSkill("starved")) {
                bonus += 1.0;
            }
            if (bonus > 0) {
                event.setDamage(event.getDamage() + bonus);
            }
        }
    }

    @EventHandler
    public void onRegain(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        PlayerData data = plugin.getDataManager().get(player);
        if (data.hasSkill("divine_blessing")) {
            event.setAmount(event.getAmount() * 2.0);
        }
    }
}
