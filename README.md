# Aetherfall

**A lore-rich SMP adventure plugin for Paper 26.3 + Geyser.**
*The shards remember. Will you?*

---

## The story

Long ago, the **Sky-Crown** hung in the sky above the Vale of Aetheria, and it sang.
Its Song was the sound of everything holding together: crops grew sweet, storms
turned aside at the borders, and the dead slept quietly under the hill.

Then came **the Shattering**. The Crown cracked. Its shards rained across the land.
The Song stopped. The Four Pillars — Ember, Tide, Verdant, and Stone — fell silent.
One of them never answered the call at all.

You are a **Shardbearer**: a wanderer whose heart resonates with the fallen shards.
Gather shards. Walk a Path. Read what was left behind. Piece together what really
happened on the Night of Cracking — and why the Crown is cracking *again*.

Aetherfall turns your friends SMP into a slow-burn mystery adventure with cozy
building, shared goals, and a story told in fragments. The Codex disagrees with
itself. The children ask better questions than the scholars. And the Song is not
yet sung.

## Features

- **4 Paths (classes)** — Emberpath, Tidepath, Verdantpath, Stonepath. Each has
  passive effects and an active ability (`/ability`). Chosen on first join.
- **14 Codex entries** — collectible lore books that tell the story in fragments.
  Right-click a page to record it. Pages can be copied and shared with friends.
- **3 story chapters, 15 quests** — *Awakening*, *The Silent Gods*, *The Second
  Crack*. Sequential quests with lore, rewards, and a finale that teases more.
- **5 relics** — named gear with story in its lore: Emberheart Blade, Tidecaller's
  Trident, Verdant Staff, Warden's Edge, and the Sky-Crown Fragment.
- **World events** — Shard Storms, Blood Moons, Meteor Falls, and the Veil Thinning,
  rotating every few minutes with boss-bar announcements.
- **Daily bounties** — 3 shared contracts each day (same for everyone, so friends
  can work together).
- **Ruins** — explorable lore sites granting Codex pages. Admins can add more in-game.
- **Orders (guilds)** — found a player Order with a shared home. The new wardens.
- **Titles** — 10 earned titles shown on your scoreboard. Never bought, always earned.
- **Levels, shards, daily gifts** — progression with a 7-day streak cycle.
- **Shard Trader** — spend shards on relics, Codex pages, and celebrations.

## Geyser / Bedrock friendly

Everything is chat, standard inventories, boss bars, and scoreboards — no custom
resource packs, no NMS, no packets. Bedrock players get the full game:

- Same commands (`/aetherfall`, `/ability`, `/shards`).
- GUIs work on Bedrock; every GUI action also exists as a chat command.
- Books, sounds, and particles are all vanilla.

## Install

**Requirements:** Paper **26.3** (JDK **25**) + [Geyser](https://geysermc.org/)
(optional, for Bedrock players) + Floodgate (optional).

1. Download the jar from the [latest GitHub Actions build](../../actions)
   (Actions → latest run → *aetherfall-jar* artifact), or build it yourself:
   ```bash
   mvn package   # needs JDK 25 + Maven
   ```
2. Drop `aetherfall-1.0.0.jar` into your server's `plugins/` folder.
3. Start the server once. It creates `plugins/Aetherfall/` with `config.yml`,
   `codex.yml`, `quests.yml`, and `ruins.yml`.
4. **Move the ruins** to match your world (or use the admin command below), then
   `/aetherfall reload`.
5. Invite your friends. The shards are waiting.

## Commands

| Command | What it does |
|---|---|
| `/aetherfall` (alias `/af`) | Help + all subcommands |
| `/aetherfall path` | View / choose your Path |
| `/aetherfall path choose <ember\|tide\|verdant\|stone>` | Choose (or change, 50 shards) |
| `/ability` | Use your Path ability |
| `/aetherfall profile` | Level, shards, Codex, quests, stats |
| `/aetherfall quests` | Current story quest + progress |
| `/aetherfall codex` | List collected lore |
| `/aetherfall codex read <id>` | Re-read an entry in chat |
| `/aetherfall bounties` | Today's 3 contracts |
| `/aetherfall ruins` | Ruins menu (travel to visited ones) |
| `/aetherfall ruin tp <name>` | Teleport to a visited ruin (10 shards) |
| `/aetherfall order create <name>` | Found an Order (100 shards) |
| `/aetherfall order invite <player>` | Invite a friend |
| `/aetherfall order accept` / `deny` | Answer an invite |
| `/aetherfall order sethome` / `home` | Set / teleport to Order home (10 shards) |
| `/aetherfall order info` / `list` / `leave` / `kick` / `disband` | Manage Orders |
| `/aetherfall titles` | Earned titles menu |
| `/aetherfall title <id>` | Wear a title |
| `/aetherfall daily` | Daily gift (7-day streak cycle) |
| `/aetherfall trader` | Spend shards |
| `/shards` | Your shard balance |
| `/aetherfall event status` | Current world event |

**Admin** (`aetherfall.admin`, default op):

| Command | What it does |
|---|---|
| `/aetherfall ruin add <name> <codexId>` | Add a ruin at your position |
| `/aetherfall event start <type>` / `stop` | Start / stop a world event |
| `/aetherfall relic give <id> <player>` | Give a relic |
| `/aetherfall give shards <player> <amount>` | Give shards |
| `/aetherfall reset <player>` | Wipe a player's Aetherfall data |
| `/aetherfall reload` | Reload config, Codex, quests, ruins |

**Permissions:** `aetherfall.use` (default true), `aetherfall.admin` (default op).

## Configuration

- `config.yml` — shard chances, costs, event timing, level cap, welcome/event text.
- `codex.yml` — the 14 lore entries. **Edit the words freely.**
- `quests.yml` — the 15 story quests (types: `CHOOSE_PATH`, `MINE`, `KILL`,
  `CRAFT`, `VISIT`, `CODEX`, `EARN`, `REACH_LEVEL`).
- `ruins.yml` — ruin locations. Move them, or add ruins in-game as admin.
- Player data: `data/players/<uuid>.yml`. Orders: `data/orders.yml`.

## Roadmap

- Chapter IV: *The Choir* — the finale (the Song needs a choir).
- Order wars / shared Order bank.
- More ruins, more relics, more Codex entries.
- Seasonal events (Winter's Quiet, Ember Week).

---

*Carry the shards far apart. Trust the loud things. And when the Song starts
again — sing with all your heart. — M.*
