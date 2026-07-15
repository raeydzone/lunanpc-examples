# LunaNPC Examples

Five small, self-contained examples of the [LunaNPC](https://github.com/raeydzone/LunaNPC) developer
API — each in a single Java file, so you can read one on its own and copy it straight into your mod.

Part of **[lunahub.net](https://lunahub.net)**.

## The examples

| File | What it shows |
|------|----------------|
| [`DungeonLurkerExample`](src/main/java/net/lunahub/lunanpc/examples/DungeonLurkerExample.java) | A hostile monster that natural-spawns inside **structures**, underground only — a stretched "husk" with armor, an XP range, and chance-based loot. |
| [`BiomeArcherExample`](src/main/java/net/lunahub/lunanpc/examples/BiomeArcherExample.java) | A long-range **archer** that natural-spawns across a **biome**, at the open surface — a lean "stray" skeleton sniper. |
| [`FriendlyGuardExample`](src/main/java/net/lunahub/lunanpc/examples/FriendlyGuardExample.java) | A **friendly** town guard in a named **zone** and its own faction — a sturdy custom player skin; protects players, fights monsters. |
| [`WarringFactionsExample`](src/main/java/net/lunahub/lunanpc/examples/WarringFactionsExample.java) | Two custom **factions** set as mutual enemies — a piglin brute vs. an illager raider — fighting in a shared arena. |
| [`BossExample`](src/main/java/net/lunahub/lunanpc/examples/BossExample.java) | A rare, oversized **boss** with a boss bar and huge stats — a hulking, stretched titan; LunaNPC's "no forced balance" in action. |

Along the way they show off **custom skins and model stretching** (`withModel` + per-part scales),
chance-based item drops and XP ranges — not just the default Steve.

Each is invoked on server start by
[`LunaExamplesMod`](src/main/java/net/lunahub/lunanpc/examples/LunaExamplesMod.java). Every example
reuses its content if it already exists and re-applies the current settings — so editing an example
and relaunching always reflects the change, and nothing is ever duplicated.

## Building

These target Minecraft 26.2 (Fabric, Java 25) — the same toolchain as LunaNPC — and compile against
LunaNPC's API jar:

1. Build LunaNPC (`./gradlew build` in the LunaNPC repo) so `build/libs/lunanpc-<version>.jar` exists.
2. If your LunaNPC repo isn't a sibling folder, adjust the `compileOnly files(...)` path in `build.gradle`.
3. `./gradlew build` here.

To run them, place this mod's jar and LunaNPC's jar in the same Fabric instance.
