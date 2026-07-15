package net.lunahub.lunanpc.examples;

import net.minecraft.server.MinecraftServer;
import net.raeydzone.luna_npc.api.LunaNpcApi;
import net.raeydzone.luna_npc.api.Npc;
import net.raeydzone.luna_npc.api.NpcRegistry;
import net.raeydzone.luna_npc.npc.NpcReactionSettings;
import net.raeydzone.luna_npc.zone.SurfaceMode;

// Dungeon example — a hostile monster that natural-spawns underground inside vanilla structures.
//
// Shows: a custom look (a "husk" skin, stretched into a gaunt long-limbed lurker), slotting into the
// built-in faction ecosystem (the "undead" alliance already hunts players, villagers and golems),
// STRUCTURE-driven spawning, a caves-only placement rule, cosmetic armor, an XP range plus per-item
// chance-based loot, and a nearby cap. Structure ids come from the live registry, so modded
// structures work the same way.
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

        // Look: the "husk" zombie skin, stretched tall — long arms and legs, a shrunken head — into a
        // gaunt cave-dweller. The zombie model is one of the stretchable humanoids (limb scales 0.5-2.0).
        lurker.setModelSettings(lurker.modelSettings()
                .withModel("zombie", "husk")
                .withHeadScale(0.8F)
                .withLeftArmScale(1.4F).withRightArmScale(1.4F)
                .withLeftLegScale(1.6F).withRightLegScale(1.6F));

        // Join the built-in "Undead" faction, which already treats players as enemies — so it hunts
        // on sight without us wiring up any relationships ourselves.
        lurker.setAllianceId("undead");

        // A stone sword in the main hand (melee, 6 damage), plus cosmetic iron armor on top.
        lurker.giveMeleeWeapon("minecraft:stone_sword", 6.0F);
        lurker.setEquipment(lurker.equipment()
                .withArmor("minecraft:iron_helmet", "minecraft:iron_chestplate", "", ""));

        // XP orbs on death: a random amount between these two (like vanilla mob XP).
        lurker.setDropSettings(lurker.dropSettings().withXp(5, 12));

        // Item drops, each rolled independently on death by its own chance: common rotten flesh, an
        // uncommon iron ingot, and a rare emerald.
        lurker.addDrop("minecraft:rotten_flesh", 80.0F);
        lurker.addDrop("minecraft:iron_ingot", 15.0F);
        lurker.addDrop("minecraft:emerald", 4.0F);

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
