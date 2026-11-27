# Rea

Rea is a **Tensura** (That Time I Got Reincarnated as a Slime) plugin for **Paper 26.3**.
You wake in the Jura Forest as an otherworlder. The world of magicules tests you:
devour, evolve, and at last awaken as a Demon Lord — your Unique Skill becoming an
Ultimate Skill — while you delve Tensura's sealed ruins for its named, cardinal weapons.

Everything is Bedrock-safe: chat, standard GUIs, boss bars, and scoreboards only.

## Install

1. Download `rea-1.0.0.jar` from the GitHub Actions build (`rea-jar` artifact) or build it with `mvn -B package`.
2. Drop the jar into your server's `plugins/` folder.
3. Requires **Paper 26.3+** (or a compatible Paper build) and **Java 25**.
4. Optional: **Geyser** for Bedrock players — Rea is fully Bedrock-safe (chat, standard GUIs, boss bars, scoreboards).

## Permissions

| Permission | Default | Grants |
| --- | --- | --- |
| `rea.use` | true | All player features (skills, races, dungeons, quests, codex) |
| `rea.admin` | op | `/rea start`, `/rea give`, `/rea reset`, `/rea reload` |

## What to expect at launch

- The jar compiles cleanly on Paper 26.3 via CI on every push — but **this plugin has not been load-tested on a live server**. Smoke-test on your own server before opening it to your followers: reincarnate, evolve, clear all three dungeons, check skills, scoreboard, quests, and codex on both Java and Bedrock.
- Dungeon difficulty is fixed, not scaled to your EP — bring friends or bring potions.
- Balance (EP/rune costs, loot rates) is tuned by feel, not by telemetry. Feedback welcome.

## Canon rules

- **Reincarnation:** `/rea start` deals a random race (Slime, Goblin, Lizardman, Orc,
  Human, Elf, Dwarf, Dire Wolf, Serpent, Demon) and grants one random **Unique Skill**
  born of the player's desires. Predator is Rimuru's personal Unique Skill — it is
  in the pool, not guaranteed to any race. Slimes as a species have Intrinsic Skills:
  Absorb, Dissolve, Self-Regeneration.
- **Predation belongs to Predator holders:** only players who roll the Predator
  Unique Skill devour foes to learn Common Skills. Other races fight, gather EP,
  and evolve.
- **Ultimate Skills evolve from Unique Skills:** at the Demon Lord awakening,
  Predator → Beelzebuth (Lord of Gluttony), Great Sage → Raphael (Lord of Wisdom),
  Infinity Prison → Uriel (Lord of Vows). Other Uniques evolve into a random
  Ultimate (Veldora, Faust, Mammon, Lucifer, Satanael, Amaterasu, Hastur, Mephisto,
  Michael, Samael, Abaddon, Belial, Asmodeus, Sariel, Yog-Sothoth, Azazel,
  True Hero, Azathoth, and more).
- **Evolution:** forms evolve with EP, level, and runes along canon chains
  (Goblin → Hobgoblin → Ogre → Demon Lord, Lizardman → Dragonewt → True Dragonewt,
  Orc → High Orc → Orc Lord, Demon → Arch Demon → Demon Peer).
- **Ranks:** EP ranks F, E, D, C, B, A, S, SS, SSS.
- **Naming (Rimuru-style):** a demon lord names a soul, granting humanization and a
  power spike. Rea's Demon Lord awakening does the same for your skills.
- **Cardinal weapons:** named, high-grade armaments of the Tensura world — won in
  the dungeons, never bought. Hold one for **+10% EP**.

## Commands

- `/rea start` — (admin) reincarnate everyone with random races and Unique Skills.
- `/rea race` — your race, form, EP, and evolution chain.
- `/rea evolve` — evolve to your next form (Ultimate evolution at Demon Lord).
- `/rea skill <id>` — cast a skill (costs magicules).
- `/rea skills` — list your skills by tier.
- `/rea ability` — cast your intrinsic skill.
- `/rea profile` — level, rank, EP, magicules, runes, codex, stats.
- `/rea quests` — the story so far.
- `/rea codex [read <id>]` — the 25 entries of Tensura's world.
- `/rea daily` — daily rune gift.
- `/rea buy cache [n]` — trade runes for XP.
- `/rea runes` — your balance.
- `/rea dungeon` — the Tensura ruins: dive or claim prizes.
- `/rea dungeon enter <id>` — enter a ruin (15-minute cooldown).
- `/rea dungeon leave` — abandon a run (no rewards).
- `/rea weapons` — the cardinal weapons of legend.
- `/ability`, `/runes` — shortcuts.

## Dungeons

Three ruins torn from broken worlds, opened as sky-arenas far above the world:

| Ruin | Difficulty | Rewards | Weapon chance |
| --- | --- | --- | --- |
| `ogre_hunt` | Ogre Hunt | 300 EP · 80 runes · 60 XP | 30% (Unique grade) |
| `orc_fortress` | Orc Lord's Fortress | 750 EP · 200 runes · 150 XP | 45% (Unique/Legend) |
| `val_velmyra` | Vál Velmyra | 1500 EP · 400 runes · 300 XP | 60% (Legend/Mythical/Genesis) |

Clear the boss to win. You drop nothing and gain no XP from the trash — the prize
is the ruin itself. Defeat the boss, claim the loot, return to Jura.

## Cardinal weapons

Fourteen named weapons, from Unique to Genesis grade:

- **Unique:** Demon Slayer (Saare), Dragon Slayer (Youm Farmenas), Gorikimaru (Shion),
  Dragon Buster (the Seven Luminaries → Hinata), Curse Sword (Albert).
- **Legend:** True Gorikimaru (Shion), Flame Pillar (Leon Cromwell),
  Night Rose (Luminous Valentine), Truth (Granbell Rosso → Hinata).
- **Mythical:** Crumbling Fang (Dino), Moon Mistress (Chloe Aubert),
  Ark (Feldway), Deva (Masayuki Rudra).
- **Genesis:** Asura (天魔) — Veldanava's scimitar, Milim's treasure.

Cardinal weapons grant **+10% EP** while held. The dungeons guard them all.

## Skills

- **Intrinsic** (born with the race): Dissolve, Absorb, Coercion, Scale Armor,
  Strengthen, Second Wind, Elven Step, Stonehide, Fang Rush, Poison Breath, Hellfire.
  Slimes get the canon trio: Absorb, Dissolve, Self-Regeneration.
- **Common** (learned by predation): Self-Regeneration, Water Blade, Voice Cannon,
  Telepathy, Farsight, Hydraulic Propulsion, Magic Sense.
- **Unique** (granted on reincarnation): Predator, Great Sage, Divine Blessing,
  Infinity Prison, Starved, Godly Craftsman, Truth.
- **Ultimate** (evolved at the Demon Lord awakening): Beelzebuth, Raphael, Uriel,
  Azathoth, Veldora, Faust, Mammon, Lucifer, Satanael, Amaterasu, Hastur, Mephisto,
  Michael, Samael, Abaddon, Belial, Asmodeus, Sariel, Yog-Sothoth, Azazel, True Hero.

## Build

```bash
mvn -B package
```

Requires JDK 25. The jar is produced in `target/`.

## License

MIT.
