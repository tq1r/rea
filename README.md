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
4. Optional: **Geyser** for Bedrock players — Rea runs on Bedrock/MCPE through Geyser (see below).

## Bedrock (MCPE) support

Rea is built to work on both editions. Every interactive feature uses only what
Geyser translates cleanly for Bedrock clients:

- **Chests & GUIs** — Talents, Ruins, and the admin menu are plain container inventories (no custom rendering, no packet tricks). Bedrock players open and click them normally.
- **Trading** — trained specialists are ordinary villagers with real professions, types, and trade levels; the Bedrock trading UI just works.
- **Scoreboards, boss bars, chat, action bars, titles** — all standard, all Geyser-safe.
- **No Java-only APIs** — no packets, no custom model data requirements, no NBT the client must parse, no signed chat dependencies.

Out of the box, Bedrock players get the full game. Known limits, honestly:

- Items keep vanilla textures on Bedrock (Rea does not ship resource packs).
- Particle auras (Master-tier specialists, evolution bursts) render as ordinary Bedrock particles.
- Geyser itself decides how container clicks map; if a click seems ignored, check `geyser.yml`'s emulation settings rather than this plugin.

**Not yet load-tested on a live Geyser instance** — the smoke-test list above applies to both editions before you open the world to followers.

## Permissions

| Permission | Default | Grants |
| --- | --- | --- |
| `rea.use` | true | All player features (skills, races, dungeons, quests, codex) |
| `rea.admin` | op | `/rea start`, `/rea admin`, `/rea give`, `/rea reset`, `/rea reload`, `/rea update` |

## What to expect at launch

- The jar compiles cleanly on Paper 26.3 via CI on every push — but **this plugin has not been load-tested on a live server**. Smoke-test on your own server before opening it to your followers: reincarnate, evolve via the Talents menu, clear all three dungeons, name and train a villager to Master, wipe a test account with `/rea admin`, then `/rea start` to confirm the fresh reincarnation, and check skills, quests, codex, the scoreboard, and the admin menus on both Java and Bedrock.
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

## Auto-update

Rea checks GitHub Releases on boot and every 6 hours (configurable):

- If a **newer** release exists, its jar is downloaded to `plugins/update/`.
  **Paper applies it on the next restart** — the running jar is never replaced underneath you.
- Ops are notified in-game once per session when an update has been staged.
- Disable with `settings.auto-update: false` in `config.yml`; change the interval with `settings.auto-update-hours`.
- Manual check any time: `/rea update` (requires `rea.admin`).

Auto-update only works if the latest GitHub **release has a `.jar` asset attached**.

### Publishing a release (maintainers)

1. Push a tag: `git tag v1.1.0 && git push origin v1.1.0`
2. The CI workflow builds `rea-1.1.0.jar` and uploads it as the `rea-jar` artifact.
3. Create a GitHub Release for the tag and **attach that jar** to it.
4. Every server running Rea will pick it up automatically on its next restart.

## Commands

Rea keeps the command list deliberately tiny. Actions live in menus; checks are
commands.

| Command | What it does |
| --- | --- |
| `/rea` | Your profile card: race, form, EP, rank, runes, codex, quests |
| `/rea quests` | Your current quest and progress |
| `/rea codex [read <id>]` | The 26 entries of Tensura's world |
| `/rea dungeon` | Menu of the ruins — click a ruin to dive |
| `/rea dungeon leave` | Abandon the ruin you are inside |
| `/rea trader …` | Name and train villager specialists |
| `/ability` | Your Talents menu: skills by tier, click to cast, evolve button |
| `/rea start` | (admin) reincarnate everyone with random races |
| `/rea admin` | (admin) the everything menu: races, skills, resources, lore, danger zone |
| `/rea give`, `/rea reset`, `/rea reload`, `/rea update` | (admin) tools |

Everything else — casting skills, evolving, diving — happens in menus or on
its own.

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

## Named villagers (Rimuru's Naming, for farmers and smiths alike)

Name an ordinary villager as your specialist — like Rimuru named Kurobe — and mentor them
to greatness. Naming is not a menu: it is done on the villager's own body. Specialists are
physical beings — each role claims a distinct face (savanna blacksmiths, plains farmers,
swamp alchemists, jungle scribes), their profession outfit, a trade level badge that grows
with training (Novice I → Master V), a floating name, and masters shed faint magical
particles. They are persistent (works across restarts, never wander off), cost runes, and
unlock strictly better wares at each tier.

- `/rea trader specialize <blacksmith|farmer|alchemist|scribe> <name...>` — claim a villager you're looking at (100 runes).
- `/rea trader train` — Novice → Apprentice → Journeyman → Master (100 / 250 / 500 runes per step).
- `/rea trader rename <name...>` and `/rea trader info`.

Look at your specialist to inspect them — the wand of training is literally looking at them.

What each master offers:

- **Blacksmith** — irons and diamonds → rune caches and netherite → Master: **Dragon Slayer** (48 emeralds + netherite block) and **True Gorikimaru** (96 emeralds + 2 blocks).
- **Farmer** — wheat and bread → golden carrots → golden apples → Master: **enchanted golden apple**.
- **Alchemist** — brewing reagents → experience bottles → rune caches → Master: **the Nether Star** (64 emeralds + blaze rod).
- **Scribe** — paper and books → rune caches → Master: codex tomes (*Jura Tempest Federation*, *True Dragons*, *Cardinal Weapons*).

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
