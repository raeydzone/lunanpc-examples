package net.lunahub.lunanpc.examples;

import net.minecraft.server.MinecraftServer;
import net.raeydzone.luna_npc.api.LunaNpcApi;
import net.raeydzone.luna_npc.api.Npc;
import net.raeydzone.luna_npc.api.NpcRegistry;
import net.raeydzone.luna_npc.npc.NpcReactionSettings;
import net.raeydzone.luna_npc.zone.SurfaceMode;

// Dungeon example — a hostile monster that natural-spawns underground inside vanilla structures.
//
// Shows: slotting into the built-in faction ecosystem (the "undead" alliance already hunts
// players, villagers and golems), STRUCTURE-driven spawning, a caves-only placement rule,
// cosmetic armor, loot drops, and a nearby cap. Structure ids come from the live registry, so
// modded structures work the same way — just pass their id.
public final class DungeonLurkerExample {

    private DungeonLurkerExample() {
    }

    public static void build(MinecraftServer server) {
        NpcRegistry npcs = LunaNpcApi.npcs(server);
        // Create the NPC exactly once: once it is saved it exists for good, so later runs return early.
        if (npcs.all().stream().anyMatch(npc -> "DungeonLurker".equals(npc.name()))) {
            return;
        }

        Npc lurker = npcs.create("DungeonLurker");
        lurker.setHealth(30);

        // Join the built-in "Undead" faction, which already treats players as enemies — so it hunts
        // on sight without us wiring up any relationships ourselves.
        lurker.setAllianceId("undead");

        // A stone sword in the main hand (melee, 6 damage), plus cosmetic iron armor on top.
        lurker.giveMeleeWeapon("minecraft:stone_sword", 6.0F);
        lurker.setEquipment(lurker.equipment()
                .withArmor("minecraft:iron_helmet", "minecraft:iron_chestplate", "", ""));

        // Loot: common rotten flesh, and a rare iron ingot.
        lurker.addDrop("minecraft:rotten_flesh", 80.0F);
        lurker.addDrop("minecraft:iron_ingot", 10.0F);

        // Aggressive temperament (hunts on sight); potions affect it like a mob, not a player.
        lurker.setTraits(lurker.traits()
                .asCreature()
                .withReaction(NpcReactionSettings.defaults().withMode(NpcReactionSettings.AGGRESSIVE)));

        // Spawn only inside these structures, only in the dark underground, capped at 6 alive nearby.
        lurker.setSpawn(lurker.spawn()
                .withStructureWeight("minecraft:mineshaft", 10)
                .withStructureWeight("minecraft:stronghold", 6)
                .withSurfaceMode(SurfaceMode.CAVES_ONLY)
                .withMaxNearby(6));
    }
}
