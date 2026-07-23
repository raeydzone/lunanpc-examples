# LunaNPC Examples

Three small, self-contained examples of the [LunaNPC](https://github.com/raeydzone/LunaNPC) developer
API. Each lives in a single Java file, so you can read one on its own and drop it straight into your own
mod. They all use only LunaNPC's built-in models — no custom assets.

Part of **[lunacore.dev](https://lunacore.dev)**.

## The examples

| File | What it shows |
|------|----------------|
| [`SteelGolemExample`](src/main/java/net/lunacore/lunanpc/examples/SteelGolemExample.java) | A 200-HP metallic golem that natural-spawns at **villages** on the surface. A slow, heavy melee attack (no weapon item), and per-item chance drops of flowers, flint & steel, stone and iron. |
| [`ChargerZombieExample`](src/main/java/net/lunacore/lunanpc/examples/ChargerZombieExample.java) | A **cave**-spawning zombie that walks slowly but charges fast. Heavy model stretching, an iron helmet and a diamond sword, a mineshaft spawn bonus, and a wide XP + loot table. |
| [`CrimsonBlazeExample`](src/main/java/net/lunacore/lunanpc/examples/CrimsonBlazeExample.java) | A flying nether blaze that shoots reddish fireballs on a **timed rhythm** — a steady volley, a pause, then a rapid burst — built with the node-based **advanced-combat** system (flags + timers), plus a distance-based accuracy curve. |

Between them they cover model stretching and built-in skins, structure / biome / cave spawning with spawn
chances, equipment and drops, a flying movement type, and both simple and advanced combat.

Each is invoked on server start by
[`LunaExamplesMod`](src/main/java/net/lunacore/lunanpc/examples/LunaExamplesMod.java). They upsert by
name, so editing an example and relaunching re-applies the change without ever creating a duplicate.

## Building

These target Minecraft 26.2 (Fabric, Java 25) — the same toolchain as LunaNPC — and compile against
LunaNPC's API jar:

1. Build LunaNPC (`./gradlew build` in the LunaNPC repo) so `build/libs/LunaNPC.jar` exists.
2. If your LunaNPC repo isn't a sibling folder, adjust the `compileOnly files(...)` path in `build.gradle`.
3. `./gradlew build` here.

To run them, drop this mod's jar and LunaNPC's jar into the same Fabric instance.
