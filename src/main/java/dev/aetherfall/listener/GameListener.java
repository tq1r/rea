package dev.aetherfall.listener;

import dev.aetherfall.AetherfallPlugin;
import dev.aetherfall.data.PlayerData;
import dev.aetherfall.event.EventType;
import dev.aetherfall.util.Msg;
import dev.aetherfall.util.Util;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * The connective tissue: mining, killing, crafting, combat, reading, and
 * exploring all feed quests, bounties, shards, and XP through here.
 */
public final class GameListener implements Listener {
    private final AetherfallPlugin plugin;

    public GameListener(AetherfallPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerData data = plugin.getDataManager().get(player);
        data.setName(player.getName());
        plugin.getTitleManager().unlock(player, "shardling");
        plugin.getPathManager().applyPassives(player);
        plugin.getScoreboardManager().update(player);
        plugin.getBountyManager().rollover(player);
        plugin.getEventManager().addToBossBar(player);
        if (data.getPath() == null) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (!player.isOnline()) {
                    return;
                }
                for (String line : plugin.getConfig().getStringList("messages.welcome")) {
                    Msg.send(player, line);
                }
                plugin.getGuiManager().openPathMenu(player);
            }, 40L);
        } else {
            Msg.send(player, "&7The shards hum a little louder today. Welcome back, Shardbearer.");
        }
        plugin.getTitleManager().checkAll(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getDataManager().save(plugin.getDataManager().get(event.getPlayer()));
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> plugin.getPathManager().applyPassives(player), 20L);
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
        double chance = plugin.getConfig().getDouble("settings.shard-mine-chance", 0.15)
                * plugin.getEventManager().shardChanceMultiplier();
        if (Math.random() < chance) {
            int min = plugin.getConfig().getInt("settings.shard-mine-min", 1);
            int max = plugin.getConfig().getInt("settings.shard-mine-max", 3);
            int amount = min + (int) (Math.random() * Math.max(1, max - min + 1));
            plugin.getDataManager().addShards(player, amount);
            player.getWorld().spawnParticle(Particle.END_ROD, event.getBlock().getLocation(), 8, 0.4, 0.4, 0.4);
            if (Math.random() < 0.25) {
                Msg.send(player, "&7The stone hums as it breaks. Something was hiding in it.");
            }
        }
        plugin.getQuestManager().checkCompletion(player);
        plugin.getBountyManager().onProgress(player, "mine:" + type.name());
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
        double chance = plugin.getConfig().getDouble("settings.kill-shard-chance", 0.10)
                * plugin.getEventManager().shardChanceMultiplier();
        if (Math.random() < chance) {
            int min = plugin.getConfig().getInt("settings.kill-shard-min", 1);
            int max = plugin.getConfig().getInt("settings.kill-shard-max", 2);
            int amount = min + (int) (Math.random() * Math.max(1, max - min + 1));
            plugin.getDataManager().addShards(killer, amount);
        }
        if (plugin.getEventManager().isActive(EventType.BLOOD_MOON)) {
            event.getDrops().add(new ItemStack(bloodMoonDrop(entity.getType())));
        }
        plugin.getQuestManager().checkCompletion(killer);
        plugin.getBountyManager().onProgress(killer, "kill:" + entity.getType().name());
    }

    private Material bloodMoonDrop(EntityType type) {
        return switch (type) {
            case SKELETON, STRAY -> Material.BONE;
            case SPIDER, CAVE_SPIDER -> Material.STRING;
            case CREEPER -> Material.GUNPOWDER;
            case ENDERMAN -> Material.ENDER_PEARL;
            default -> Material.ROTTEN_FLESH;
        };
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
        plugin.getBountyManager().onProgress(player, "craft:" + result.name());
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
        String codexId = plugin.getCodexManager().getCodexId(item);
        if (codexId != null) {
            boolean first = plugin.getCodexManager().readBook(player, codexId);
            if (first) {
                consumeOne(player, item);
            }
            event.setCancelled(true);
            return;
        }
        String relicId = plugin.getRelicManager().getRelicId(item);
        if (relicId != null) {
            if (relicId.equals("verdant_staff")) {
                plugin.getRelicManager().useVerdantStaff(player);
                event.setCancelled(true);
            } else if (relicId.equals("crown_fragment")) {
                plugin.getRelicManager().useCrown(player);
                event.setCancelled(true);
            }
            return;
        }
        if (plugin.getItemFactory().isShardCache(item)) {
            int amount = plugin.getConfig().getInt("settings.cache-shards", 25);
            plugin.getDataManager().addShards(player, amount);
            consumeOne(player, item);
            player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1f, 1f);
            player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation(), 20, 0.5, 0.8, 0.5);
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
            double bonus = 0;
            if ("ember".equals(data.getPath())) {
                bonus += 1.5;
            }
            String relicId = plugin.getRelicManager().getRelicId(attacker.getInventory().getItemInMainHand());
            if (relicId != null) {
                switch (relicId) {
                    case "emberheart" -> {
                        bonus += 2.0;
                        event.getEntity().setFireTicks(60);
                    }
                    case "tidecaller" -> {
                        if (attacker.isInWater()) {
                            bonus += 2.0;
                        }
                    }
                    case "warden_edge" -> {
                        if (isUndead(event.getEntity())) {
                            bonus += 2.0;
                        }
                    }
                    default -> {
                    }
                }
            }
            if (bonus > 0) {
                event.setDamage(event.getDamage() + bonus);
            }
        }
        if (event.getEntity() instanceof Player victim) {
            PlayerData victimData = plugin.getDataManager().get(victim);
            if ("stone".equals(victimData.getPath())) {
                event.setDamage(event.getDamage() * 0.8);
            }
        }
        if (plugin.getEventManager().isActive(EventType.BLOOD_MOON) && event.getDamager() instanceof Monster) {
            event.setDamage(event.getDamage() + 2.0);
        }
    }

    private boolean isUndead(Entity entity) {
        return switch (entity.getType()) {
            case ZOMBIE, SKELETON, DROWNED, HUSK, STRAY, ZOMBIE_VILLAGER, PHANTOM -> true;
            default -> false;
        };
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) {
            return;
        }
        if (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ()) {
            return;
        }
        plugin.getRuinManager().checkVisit(event.getPlayer());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        plugin.getGuiManager().handleClick(event);
    }
}
