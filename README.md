# Rea

Rea is a **Tensura** (That Time I Got Reincarnated as a Slime) plugin for **Paper 26.3**.
Players reincarnate as a random Tensura race, receive a **Unique Skill**, gain
**Existence Points (EP)** and **magicules**, devour foes to learn **Common Skills**,
and evolve through their race's chain to a **Demon Lord awakening** — where their
Unique Skill evolves into an **Ultimate Skill**.

Everything is Bedrock-safe: chat, standard GUIs, boss bars, and scoreboards only.

## Canon rules

- **Reincarnation:** `/rea start` deals a random race (Slime, Goblin, Lizardman, Orc,
  Human, Elf, Dwarf, Dire Wolf, Serpent, Demon) and grants one **Unique Skill**
  born of the player's desires. Slimes receive **Predator**.
- **Predation is Slime-only:** only Predator holders devour foes to learn Common
  Skills. Other races fight, gather EP, and evolve.
- **Ultimate Skills evolve from Unique Skills:** at the Demon Lord awakening,
  Predator → Beelzebuth, Great Sage → Raphael, Infinity Prison → Uriel,
  Starved → Azathoth.
- **Evolution:** forms evolve with EP, level, and runes along canon chains
  (Goblin → Hobgoblin → Ogre → Demon Lord, Lizardman → Dragonewt → True Dragonewt,
  Orc → High Orc → Orc Lord, Demon → Arch Demon → Demon Peer).
- **Ranks:** EP ranks F, E, D, C, B, A, S, SS, SSS.

## Commands

- `/rea start` — reincarnate as a random race and receive a Unique Skill.
- `/rea status` — race, form, EP, rank, magicules, runes, skills.
- `/rea evolve` — evolve to the next form (triggers Ultimate evolution at Demon Lord).
- `/rea skill <id>` — cast a skill.
- `/rea skills` — list your skills by tier.
- `/rea admin <give|set|reset> ...` — admin tools (`rea.admin`).
- `/ability` — open the skills GUI.
- `/runes` — view your runes.

## Skills

- **Intrinsic** (born with the race): Dissolve, Coercion, Scale Armor, Strengthen,
  Second Wind, Elven Step, Stonehide, Fang Rush, Poison Breath, Hellfire.
- **Common** (learned by predation): Self-Regeneration, Water Blade, Voice Cannon,
  Telepathy, Farsight, Hydraulic Propulsion.
- **Unique** (granted on reincarnation): Predator, Great Sage, Divine Blessing,
  Infinity Prison, Starved, Godly Craftsman.
- **Ultimate** (evolved at Demon Lord awakening): Beelzebuth, Raphael, Uriel, Azathoth.

## Build

```bash
mvn -B package
```

Requires JDK 25. The jar is produced in `target/`.

## License

MIT.
