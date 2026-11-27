# Rea

**A Tensura-style reincarnation SMP plugin for Paper 26.3 + Geyser.**
*Reincarnate. Predate. Evolve. Become a Demon Lord.*

---

## The loop (Tensura-style)

1. **Reincarnate** — An op runs `/rea start`. Every player without a bloodline is
   dealt a **random race** from the ten: Slime, Goblin, Lizardman, Orc, Human, Elf,
   Dwarf, Dire Wolf, Serpent, Demon. Races are permanent.
2. **Predate** — Kill monsters to devour them: gain **EP** (Existential Points),
   magicules, and a chance to learn the monster's **skill** (Spider → Steel Thread,
   Blaze → Flame Breath, Enderman → Teleport…).
3. **Grow** — EP sets your **Rank** (F → SSS) and fills your **magicules** well.
   Levels, quests, and Codex lore feed the same climb.
4. **Evolve** — With EP + level + runes, `/rea evolve` reshapes you:
   Base → Evolved → Apex → **Demon Lord**. Evolution grants Unique skills
   (Beelzebuth at Apex) and, at Demon Lord, **Ultimate skills**
   (Lord of Darkness, Truth).
5. **Skills** — Intrinsic (race), Common (predation), Unique (deeds/evolution),
   Ultimate (Demon Lord). Actives cost magicules; passives run always.

## The ten bloodlines

| Bloodline | Gift (passive) | Intrinsic skill |
|---|---|---|
| **Slime** | Regeneration | **Dissolve** — acid melts nearby foes |
| **Goblin** | Speed | **Sneak** — vanish for 6s |
| **Lizardman** | Swim faster | **Tail Slash** — sweep foes away |
| **Orc** | +2 melee damage | **Bloodrage** — Strength II for 10s |
| **Human** | +10% XP | **Second Wind** — heal + sprint |
| **Elf** | Speed + Night Vision | **Elven Step** — blink forward 6 blocks |
| **Dwarf** | Resistance + Haste | **Stonehide** — Resistance II for 8s |
| **Dire Wolf** | Speed + Jump Boost | **Fang Rush** — dash and bite |
| **Serpent** | Water Breathing | **Venom Bite** — poison nearby foes |
| **Demon** | Fire resistance | **Hellfire** — burn and scorch foes |

**Evolution chains** (Base → Evolved → Apex → Demon Lord):
Slime→Evolved Slime→Ogre · Goblin→Hobgoblin→Ogre · Lizardman→Dragon Newt→Wyvern ·
Orc→High Orc→Ogre · Human→Enhanced Human→Hero · Elf→High Elf→Fairy Queen ·
Dwarf→Master Dwarf→Rune Lord · Dire Wolf→Evolved Dire Wolf→Alpha Wolf ·
Serpent→Black Serpent→Hydra · Demon→Arch Demon→Demon Peer. All roads end at **Demon Lord**.

**Skills beyond bloodlines:** 8 Common (predation), 4 Unique (Predator, Great Sage,
Magic Sense, Beelzebuth), 2 Ultimate (Lord of Darkness, Truth). See `/rea skills`.

## Features

- **Random reincarnation** at `/rea start` (op), with lore reveals + broadcast
- **Predation** — EP, magicules, and skill learning from every kill
- **EP & Ranks** (F–SSS), **magicules** with regeneration
- **4-stage evolution** with EP/level/rune costs
- **Levels & XP** (1–20), **Runes** currency (→ XP caches)
- **14 Codex entries** — reincarnation, skills, evolution, and an origin tale per bloodline
- **2 story chapters, 8 quests** — *The Waking* and *The Sundering Truth*
- **Daily gifts** — 7-day streak cycle
- **Scoreboard** — bloodline/form, rank, EP, magicules, level, runes, quest

## Geyser / Bedrock friendly

Everything is chat, scoreboards, books, and vanilla effects — no custom resource
packs, no packets. Bedrock players get the full game with the same commands.

## Install

**Requirements:** Paper **26.3** (JDK **25**) + [Geyser](https://geysermc.org/) (optional).

1. Download the jar from the [latest GitHub Actions build](../../actions)
   (Actions → latest run → *rea-jar* artifact), or build it yourself:
   ```bash
   mvn package   # needs JDK 25 + Maven
   ```
2. Drop `rea-1.0.0.jar` into your server's `plugins/` folder.
3. Start the server once. It creates `plugins/Rea/` with `config.yml`,
   `codex.yml`, and `quests.yml`.
4. As op, run **`/rea start`** to reincarnate everyone online.
5. Invite your friends. The Flame is waiting.

> Upgrading? Delete the old `plugins/Aetherfall/` folder — Rea starts fresh.

## Commands

| Command | What it does |
|---|---|
| `/rea` | Help + all subcommands |
| `/rea start` | (admin) Reincarnate everyone without a bloodline |
| `/rea start <player>` | (admin) Reincarnate one player |
| `/rea race` | Your bloodline, form, and evolution chain |
| `/rea evolve` | Evolve to your next form (EP + level + runes) |
| `/rea skill <id>` | Use a skill (costs magicules) |
| `/rea skills` | Skills you know + how to learn the rest |
| `/ability` | Use your intrinsic skill |
| `/rea profile` | Rank, EP, magicules, level, runes, stats |
| `/rea quests` | Current story quest + progress |
| `/rea codex` | List collected lore |
| `/rea codex read <id>` | Re-read an entry in chat |
| `/rea daily` | Daily rune gift (7-day streak) |
| `/rea buy cache [n]` | Trade runes for XP caches |
| `/runes` | Your rune balance |

**Admin** (`rea.admin`, default op): `/rea give runes <player> <amount>`,
`/rea reset <player>`, `/rea reload`.

**Permissions:** `rea.use` (default true), `rea.admin` (default op).

## Configuration

- `config.yml` — rune chances, costs, level cap, welcome/start messages.
- `codex.yml` — the 14 lore entries. **Edit the words freely.**
- `quests.yml` — the 8 story quests (types: `RACE`, `MINE`, `KILL`, `CRAFT`,
  `CODEX`, `REACH_LEVEL`).
- Player data: `data/players/<uuid>.yml`.

## Roadmap

- Chapter III: *The Eleventh Spark* — find the sleeping spark.
- Subordinates: name monsters to gain followers.
- Skill levels and more Ultimate skills.
- Demon Lord trials and seasonal festivals.

---

*Carry the spark far. Trust the loud things. And when the Flame burns again —
sing with all your heart.*
