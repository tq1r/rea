package dev.aetherfall.codex;

import java.util.List;

/** One page of the Vale's story, as written in codex.yml. */
public record CodexEntry(String id, String title, String subtitle, List<String> pages, long rewardShards, int rewardXp) {
}
