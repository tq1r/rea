package dev.aetherfall.quest;

/** One quest, as written in quests.yml. */
public record QuestDef(String id, int chapter, String name, String type, String target,
                      int amount, long rewardShards, int rewardXp, String rewardItem,
                      String description, String complete) {
}
