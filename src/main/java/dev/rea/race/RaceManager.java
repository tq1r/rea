package dev.rea.race;

import dev.rea.ReaPlugin;
import dev.rea.data.PlayerData;
import dev.rea.skill.SkillDef;
import dev.rea.skill.SkillManager;
import org.bukkit.entity.Player;

/**
 * Race evolution (Tensura canon): forms evolve with EP, level, and runes.
 * The final form is the Demon Lord awakening, at which the player's Unique
 * Skill evolves into an Ultimate Skill (Predator -> Beelzebuth, etc.).
 */
public final class RaceManager {

    private final ReaPlugin plugin;
    private final SkillManager skills;

    public RaceManager(ReaPlugin plugin, SkillManager skills) {
        this.plugin = plugin;
        this.skills = skills;
    }

    public int maxStage() {
        return 3;
    }

    public boolean canEvolve(PlayerData data) {
        RaceType race = RaceType.fromId(data.getRaceId());
        if (race == null) {
            return false;
        }
        int stage = data.getForm();
        if (stage >= maxStage()) {
            return false;
        }
        return switch (stage) {
            case 0 -> data.getEp() >= 100 && data.getLevel() >= 5 && data.getRunes() >= 1;
            case 1 -> data.getEp() >= 500 && data.getLevel() >= 10 && data.getRunes() >= 3;
            case 2 -> data.getEp() >= 2_000 && data.getLevel() >= 20 && data.getRunes() >= 6;
            default -> false;
        };
    }

    public boolean evolve(Player player, PlayerData data) {
        RaceType race = RaceType.fromId(data.getRaceId());
        if (race == null || !canEvolve(data)) {
            return false;
        }
        int stage = data.getForm();
        data.setForm(stage + 1);
        data.setRunes(data.getRunes() - (stage + 1) * 2);
        data.setEp(data.getEp() - (stage + 1) * 100);
        if (stage + 1 >= maxStage()) {
            // Demon Lord awakening: the Unique Skill evolves into an Ultimate Skill.
            SkillDef unique = data.getUniqueSkillId() != null ? skills.get(data.getUniqueSkillId()) : null;
            SkillDef ultimate = skills.ultimateEvolutionOf(unique);
            if (ultimate != null) {
                data.addSkill(ultimate.getId());
                data.setUniqueSkillId(ultimate.getId());
            }
        }
        plugin.getDataManager().save(data);
        return true;
    }
}
