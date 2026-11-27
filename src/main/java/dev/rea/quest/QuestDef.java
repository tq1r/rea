package dev.rea.quest;

/** One quest, as written in quests.yml. */
public record QuestDef(String id, int chapter, String name, String type, String target,
                      int amount, long rewardRunes, int rewardXp, long rewardEp, String rewardItem,
                      String description, String complete) {
}
