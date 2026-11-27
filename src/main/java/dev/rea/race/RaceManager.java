package dev.rea.race;

import dev.rea.ReaPlugin;
import dev.rea.data.PlayerData;
import dev.rea.skill.SkillDef;
import dev.rea.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Random;

/**
 * Reincarnates wanderers (/rea start), applies racials, and evolves forms.
 * Passives are potion effects (11 seconds each, re-applied on join/respawn and
 * every 10 seconds), so they work identically for Java and Bedrock players.
 * Active skills live in SkillManager.
 *
 * Canon (Tensura): otherworlders receive a Unique Skill on reincarnation
 * randomly, born of desire. At the Demon Lord awakening, the Unique Skill
 * evolves into an Ultimate Skill (Predator -> Beelzebuth, etc.).
 */
public final class RaceManager {
    // Racial passives are applied as "infinite" effects the Bedrock-friendly
    // way: an 11-second PotionEffect re-applied every 10 seconds. Vanilla
    // never runs dry, Geyser renders it correctly, and players can always
    // tell the truth by hovering the icon (or type /effect).
    private static final int PASSIVE_TICKS = 220;   // 11 seconds
    private static final long LONG = Integer.MAX_VALUE;

    // Evolution thresholds per stage (1 Evolved, 2 Apex, 3 Demon Lord).
    private static final long[] EVOLVE_EP = {0, 500, 2000, 8000};
    private static final int[] EVOLVE_LEVEL = {0, 3, 7, 12};
    private static final long[] EVOLVE_RUNES = {0, 100, 300, 1000};

    private final ReaPlugin plugin;
    private final Random random = new Random();

    public RaceManager(ReaPlugin plugin) {
        this.plugin = plugin;
    }

    public int maxStage() {
        return 3;
    }

    public long evolveEp(int stage) {
        return EVOLVE_EP[Math.max(0, Math.min(EVOLVE_EP.length - 1, stage))];
    }

    public int evolveLevel(int stage) {
        return EVOLVE_LEVEL[Math.max(0, Math.min(EVOLVE_LEVEL.length - 1, stage))];
    }

    public long evolveRunes(int stage) {
        return EVOLVE_RUNES[Math.max(0, Math.min(EVOLVE_RUNES.length - 1, stage))];
    }

    public boolean canEvolve(PlayerData data) {
        int next = data.getForm() + 1;
        if (next > maxStage()) {
            return false;
        }
        return data.getEp() >= EVOLVE_EP[next]
                && data.getLevel() >= EVOLVE_LEVEL[next]
                && data.getRunes() >= EVOLVE_RUNES[next];
    }

    /** Reincarnate every online wanderer who has not yet arrived. */
    public void startSeason() {
        for (String line : plugin.getConfig().getStringList("messages.start-broadcast")) {
            Bukkit.broadcastMessage(Msg.c(line));
        }
        int dealt = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData data = plugin.getDataManager().get(player);
            if (data.getRace() == null) {
                assign(player, RaceType.values()[random.nextInt(RaceType.values().length)]);
                dealt++;
            }
        }
        Bukkit.broadcastMessage(Msg.c("&7The otherworlders have arrived: &d" + dealt + " &7wanderers reincarnated."));
        plugin.getLogger().info("Reincarnation dealt to " + dealt + " players.");
    }

    /** Reincarnate a player as the given race: intrinsic skill + Unique Skill. */
    public void assign(Player player, RaceType type) {
        PlayerData data = plugin.getDataManager().get(player);
        data.setRace(type.getId());
        data.setForm(0);
        data.setMagicules(plugin.getDataManager().maxMagicules(data));
        data.addSkill(type.getIntrinsicId());
        // Canon: Slimes have three Intrinsic Skills (Absorb, Dissolve,
        // Self-Regeneration) — Predator is Rimuru's personal Unique Skill,
        // not a species skill.
        if (type == RaceType.SLIME) {
            data.addSkill("absorb");
            data.addSkill("self_regeneration");
        }
        // Otherworlders receive one random Unique Skill on reincarnation.
        SkillDef unique = plugin.getSkillManager().grantUniqueSkill(type);
        data.addSkill(unique.getId());
        data.setUniqueSkillId(unique.getId());
        applyPassives(player);
        Msg.send(player, type.getColor() + "&lReincarnation! You are a " + type.getDisplayName() + "!");
        Msg.send(player, "&7Blood gift: &f" + type.getPassiveDescription());
        Msg.send(player, "&7Intrinsic: &f" + type.getIntrinsicName() + "&8 - &7" + type.getIntrinsicDescription());
        Msg.send(player, "&bUnique: &f" + unique.getName() + "&8 - &7" + unique.getDescription());
        Msg.send(player, "&7Skill orb gained — hold it out and use it. &8(Race is permanent; forms evolve.)");
        dev.rea.quest.QuestDef firstQuest = plugin.getQuestManager().currentQuest(data);
        if (firstQuest != null) {
            Msg.send(player, "&eHere's your quest: &f" + firstQuest.name() + " &8- &7" + firstQuest.description());
            Msg.send(player, "&8(track progress with &e/rea quests&8)");
        }
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1f);
        player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation(), 40, 0.6, 1.0, 0.6);
        plugin.getSkillManager().reissueOrbs(player);
        Bukkit.broadcastMessage(Msg.c("&7" + player.getName() + " reincarnates as a " + type.getColor() + type.getDisplayName() + "&7!"));
        data.incrementCounter("race", 1);
        plugin.getQuestManager().checkCompletion(player);
        plugin.getDataManager().save(data);
    }

    /** Evolve to the next form if EP, level, and runes allow. */
    public void evolve(Player player) {
        PlayerData data = plugin.getDataManager().get(player);
        RaceType type = RaceType.fromId(data.getRace());
        if (type == null) {
            Msg.send(player, "&cYour reincarnation awaits. An admin must run &e/rea start&c.");
            return;
        }
        int next = data.getForm() + 1;
        if (next > maxStage()) {
            Msg.send(player, "&cYou are already a &lDemon Lord&c. There is nothing above you.");
            return;
        }
        long needEp = EVOLVE_EP[next];
        int needLevel = EVOLVE_LEVEL[next];
        long needRunes = EVOLVE_RUNES[next];
        if (data.getEp() < needEp) {
            Msg.send(player, "&cEvolution needs &d" + needEp + " EP&c. You carry &d" + data.getEp() + "&c. Predate monsters to grow.");
            return;
        }
        if (data.getLevel() < needLevel) {
            Msg.send(player, "&cEvolution needs &dLevel " + needLevel + "&c. You are Level &d" + data.getLevel() + "&c.");
            return;
        }
        if (!plugin.getDataManager().takeRunes(player, needRunes)) {
            return;
        }
        performEvolution(player, data, type, next);
    }

    /** Admin-only: advance a form ignoring EP/level/rune requirements. */
    public void forceEvolve(Player player) {
        PlayerData data = plugin.getDataManager().get(player);
        RaceType type = RaceType.fromId(data.getRace());
        if (type == null) {
            Msg.send(player, "&cThat wanderer has no race yet.");
            return;
        }
        int next = data.getForm() + 1;
        if (next > maxStage()) {
            Msg.send(player, "&cAlready a &lDemon Lord&c.");
            return;
        }
        performEvolution(player, data, type, next);
    }

    private void performEvolution(Player player, PlayerData data, RaceType type, int next) {
        data.setForm(next);
        data.setMagicules(plugin.getDataManager().maxMagicules(data));
        String formName = type.formName(next);
        Msg.send(player, type.getColor() + "&lEVOLUTION! &lYou are now a " + formName + "!");
        if (next == 1) {
            Msg.send(player, "&7Your blood remembers more of itself. Skills grow stronger.");
        } else if (next == 2) {
            Msg.send(player, "&7The Apex form. Your Unique skill stirs, hungry to grow.");
        } else {
            Msg.send(player, "&7A &lDemon Lord&7. The Harvest Festival begins!");
            // Demon Lord awakening: the Unique Skill evolves into an Ultimate Skill.
            SkillDef unique = data.getUniqueSkillId() != null ? plugin.getSkillManager().getSkill(data.getUniqueSkillId()) : null;
            SkillDef ultimate = plugin.getSkillManager().ultimateEvolutionOf(unique);
            if (ultimate == null) {
                ultimate = plugin.getSkillManager().randomUltimate();
            }
            if (ultimate != null) {
                data.addSkill(ultimate.getId());
                data.setUniqueSkillId(ultimate.getId());
                Msg.send(player, "&bYour Unique Skill evolves into the Ultimate Skill &l" + ultimate.getName() + "&b!");
                Msg.send(player, "&7" + ultimate.getDescription());
                plugin.getSkillManager().reissueOrbs(player);
            }
        }
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1f);
        player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation(), 60, 0.8, 1.2, 0.8);
        Bukkit.broadcastMessage(Msg.c("&d" + player.getName() + " evolves into a " + type.getColor() + formName + "&d!"));
        plugin.getDataManager().save(data);
    }

    public void applyPassives(Player player) {
        PlayerData data = plugin.getDataManager().get(player);
        // One-time upgrade scrub: Rea <= 1.0.0 applied passives with
        // Integer.MAX_VALUE duration. Those leftovers got saved to player NBT
        // and can survive relogs forever — strip them once.
        List<PotionEffectType> reaPassives = List.of(
                PotionEffectType.REGENERATION, PotionEffectType.SPEED,
                PotionEffectType.RESISTANCE, PotionEffectType.HASTE,
                PotionEffectType.FIRE_RESISTANCE, PotionEffectType.DOLPHINS_GRACE,
                PotionEffectType.NIGHT_VISION, PotionEffectType.JUMP_BOOST,
                PotionEffectType.WATER_BREATHING, PotionEffectType.STRENGTH);
        for (PotionEffect effect : player.getActivePotionEffects()) {
            if (effect.getDuration() == LONG && reaPassives.contains(effect.getType())) {
                player.removePotionEffect(effect.getType());
            }
        }
        RaceType type = RaceType.fromId(data.getRace());
        if (type == null) {
            return;
        }
        switch (type) {
            case SLIME -> player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, PASSIVE_TICKS, 0, false, false, false));
            case GOBLIN -> player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, PASSIVE_TICKS, 0, false, false, false));
            case LIZARDMAN -> player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, PASSIVE_TICKS, 0, false, false, false));
            case ELF -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, PASSIVE_TICKS, 0, false, false, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, PASSIVE_TICKS, 0, false, false, false));
            }
            case DWARF -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, PASSIVE_TICKS, 0, false, false, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, PASSIVE_TICKS, 0, false, false, false));
            }
            case DIRE_WOLF -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, PASSIVE_TICKS, 0, false, false, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, PASSIVE_TICKS, 0, false, false, false));
            }
            case SERPENT -> player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, PASSIVE_TICKS, 0, false, false, false));
            case DEMON -> player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, PASSIVE_TICKS, 0, false, false, false));
            case HUMAN, ORC -> {
                // Human: XP bonus (in DataManager). Orc: melee bonus (in GameListener).
            }
        }
    }

    public void startPassiveTask() {
        // Refresh every 10 seconds; each application lasts 11 — a constant 1s overlap.
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                applyPassives(player);
                plugin.getArmorManager().applyWorn(player);
            }
        }, 200L, 200L);
    }
}
