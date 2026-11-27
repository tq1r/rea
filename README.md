# Rea

**A fantasy bloodline SMP plugin for Paper 26.3 + Geyser.**
*The bloodlines wake. Who will you be?*

---

## The story

At the heart of the world burned the **First Flame** — the world's own warmth,
keeping the seasons turning and the dead resting easy. Ten bloodlines tended it:
elves its voice, dwarves its hands, orcs its wrath, dragonborn its first fire.

Then came **the Sundering**. The Flame split into ten sparks, each bloodline
carried one away, and the realm cracked. The sparks scattered and became **Runes**.
The old records mention *eleven* sparks — but only ten bloodlines ever woke.
The eleventh still sleeps somewhere, still burning, still waiting.

Now the bloodlines wake again, one wanderer at a time. Each bloodline remembers
the Sundering differently, and each memory is true the way a song is true:
completely, and not at all. The Codex holds all of them. Trust none completely.
Trust all of them a little.

## How it works

1. The server starts fresh. Players wander without a bloodline.
2. An op runs **`/rea start`** — every player without a bloodline is **dealt a
   random race** from the ten, with a lore reveal and a server-wide announcement.
3. Bloodlines are **permanent**. One race, one life. The deal matters.
4. From there: level up, gather Runes, read the Codex, and walk the story quests.

## The ten bloodlines

| Bloodline | Gift (passive) | Ability (`/ability`) |
|---|---|---|
| **Human** | +10% XP from everything | **Second Wind** — heal + sprint |
| **Elf** | Regeneration + Speed | **Elven Step** — blink forward 6 blocks |
| **Dwarf** | Resistance + Haste | **Stonehide** — Resistance II for 8s |
| **Orc** | +2 melee damage | **Bloodrage** — Strength II for 10s |
| **Halfling** | Luck + Speed | **Lucky Dodge** — Resistance + Speed II |
| **Tiefling** | Fire resistance | **Hellfire** — burn and scorch nearby foes |
| **Dragonborn** | +4 max health | **Dragon's Breath** — fire in a cone |
| **Gnome** | Haste II + Jump Boost | **Mend** — repair the item in your hand |
| **Aasimar** | Night Vision + Slow Falling | **Dawn's Blessing** — heal 6 + Resistance |
| **Goliath** | Strength I | **Giant's Slam** — hurt and hurl nearby foes |

Abilities grow slightly stronger as you level. All ten bloodlines are balanced
for a friends SMP — no race is strictly best, every race has a moment.

## Features

- **Random bloodline deal** at `/rea start` (op), with lore reveals + broadcast
- **Levels & XP** (1–20) from kills, quests, and Codex — Humans learn faster
- **Runes** — the world's scattered sparks, earned from mining, kills, quests,
  Codex, and daily gifts; spent on Rune Caches (XP)
- **13 Codex entries** — collectible lore books: the world, the Sundering, and
  an origin tale for every bloodline (they disagree — that's the mystery)
- **2 story chapters, 8 quests** — *The Waking* and *The Sundering Truth*,
  ending on the eleventh spark
- **Daily gifts** — 7-day streak cycle
- **Scoreboard** — bloodline, level, runes, Codex, current quest

## Geyser / Bedrock friendly

Everything is chat, boss-bar-free, packet-free, vanilla sounds and particles.
Bedrock players get the full game with the same commands. Books, scoreboards,
and potion effects all render on Bedrock.

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
4. As op, run **`/rea start`** to deal bloodlines to everyone online.
5. Invite your friends. The bloodlines are waiting.

> Upgrading from Aetherfall? Delete the old `plugins/Aetherfall/` folder —
> Rea starts fresh.

## Commands

| Command | What it does |
|---|---|
| `/rea` | Help + all subcommands |
| `/rea start` | (admin) Deal random bloodlines to everyone without one |
| `/rea start <player>` | (admin) Deal a bloodline to one player |
| `/rea race` | Your bloodline and its gifts |
| `/ability` | Use your bloodline ability |
| `/rea profile` | Level, runes, Codex, quests, stats |
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
- `codex.yml` — the 13 lore entries. **Edit the words freely.**
- `quests.yml` — the 8 story quests (types: `RACE`, `MINE`, `KILL`, `CRAFT`,
  `CODEX`, `REACH_LEVEL`).
- Player data: `data/players/<uuid>.yml`.

## Roadmap

- Chapter III: *The Eleventh Spark* — find the sleeping spark.
- Bloodline mastery trees (spend XP to deepen your gift).
- Seasonal bloodline festivals.

---

*Carry the spark far. Trust the loud things. And when the Flame burns again —
sing with all your heart.*
