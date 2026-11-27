# Changelog

## v1.1.0 — Vanilla PvP first, plain language (2026-11-27)

- **Removed the Tensura rank ladder (F → SSS)** everywhere — profile, skills menu, EP threshold announcements, PvP damage bonus. EP remains the evolution currency; nothing else changes about progression.
- **PvP philosophy, enforced in numbers**: Rea augments vanilla PvP, never replaces it. Racial damage bonus = form (small) + Orc +2; the rank term is gone. Cardinal weapons keep +10% EP.
- **Reincarnation now explains itself**: after the race intro you always get `Here's your quest: <name> — <description>` with a `/rea quests` pointer, so the first thing you see is a thing to do.
- **Skills menu spells it out**: every skill is labelled `Active — hold your Skill Orb` or `Passive — always with you, no orb needed`. Passives never receive orbs (both the grant path and the lore say so).
- **NEW — Mythic armor: the Tempest Sovereign set.** Crown (Night Vision), Aegis (Resistance I), Greaves (Speed I), Striders (Jump Boost I); all four → Strength I. Unbreakable netherite (Protection IV), won from dungeon victories (Ogre Hunt 5%, Orc Fortress 10%, Vál Velmyra 18%), admin-grantable via `/rea admin` → **Gear & Relics**.
- README documents the design rule and the new set; ranks removed; quickstart updated.

## v1.0.2 — Revamp for readability (2026-11-27)

- **In-game guide**: `/rea help` is now a 7-line tour (reincarnate → fight → cast orbs → evolve), with an admin appendix when you have `rea.admin`.
- **`/talents` alias** for `/ability`; menu renamed "Your Skills" (it was "Your Talents").
- **Profile clarity**: shows EP remaining to your next Tensura rank (F→SSS), so players know what to chase.
- Console prints the host tip: `run '/rea start' in-game to reincarnate your players`.
- Greeting on join points to `/rea help`.
- README: quickstart + "How a life works" loop at the top; player-facing docs rewritten.

## v1.0.1 — Ghost sidebar fix + update safety

- FIX: legacy `rea` sidebar objective is scrubbed at runtime (HUD only from now on).
- Racial passives: 11-second effects, refreshed every 10s (was: permanent NBT effects).
- Chat hushed: ~26 separator lines removed; one-line level up; compacted join/reincarnation output.
- Player data now saves atomically (temp file + move); first boot on a new version backs up `data/players` into `data/backups/` (keeps 3).
- Auto-update stages under the running jar's filename and purges stale staged `rea-*.jar`.

## v1.0.0 — First public release

- Tensura reincarnation: random race + Unique Skill on `/rea start`.
- Skill Orbs: skills materialize as soul-bound prismarine crystals; hold-and-use to cast (right-click Java / hold-and-press Bedrock).
- Action-bar HUD (magicules + level), no sidebar.
- Evolution chains to Demon Lord; Ultimate Skills evolve from Unique Skills.
- Dungeons (3 ruins) with named cardinal weapons; named villager specialists (Naming, 4 roles, tiers).
- EP/XP/runes/codex/quests/kill-feasts; `/rea admin` everything menu; per-player data YAML.
- Auto-update from GitHub Releases; Bedrock-safe via Geyser.
