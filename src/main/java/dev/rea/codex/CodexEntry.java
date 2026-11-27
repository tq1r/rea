package dev.rea.codex;

import java.util.List;

/** One page of Rea's story, as written in codex.yml. */
public record CodexEntry(String id, String title, String subtitle, List<String> pages, long rewardRunes, int rewardXp, long rewardEp) {
}
