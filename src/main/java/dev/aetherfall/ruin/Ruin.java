package dev.aetherfall.ruin;

/** One ruin of the Vale, as written in ruins.yml. */
public record Ruin(String name, String display, String world, int x, int y, int z, String codex, String hint) {
}
