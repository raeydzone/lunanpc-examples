package net.lunahub.lunanpc.examples;

import net.minecraft.server.MinecraftServer;
import net.lunahub.luna_npc.api.LunaNpcApi;
import net.lunahub.luna_npc.api.NpcDefinition;
import net.lunahub.luna_npc.api.NpcRegistry;
import net.lunahub.luna_npc.api.NpcReactionSettings;
import net.lunahub.luna_npc.api.SurfaceMode;

// A hostile dungeon monster: a stretched "husk" that natural-spawns inside structures (caves only),
// joins the built-in "undead" faction, and drops XP plus chance-based loot.
public final class DungeonLurkerExample {

    private DungeonLurkerExample() {
    }

    public static void build(MinecraftServer server) {
        NpcRegistry npcs = LunaNpcApi.npcs(server);
        // getOrCreate: reuse the definition if it exists, else create it — idempotent, no duplicates.
        NpcDefinition lurker = npcs.getOrCreate("DungeonLurker");

        lurker.setHealth(30);
        lurker.setAllianceId("undead");

        // A gaunt, long-limbed husk (zombie is a stretchable humanoid; part scales are 0.5-2.0).
        lurker.setModelSettings(lurker.modelSettings()
                .withModel("zombie", "husk")
                .withHeadScale(0.8F)
                .withLeftArmScale(1.4F).withRightArmScale(1.4F)
                .withLeftLegScale(1.6F).withRightLegScale(1.6F));

        lurker.giveMeleeWeapon("minecraft:stone_sword", 6.0F);
        lurker.setEquipment(lurker.equipment()
                .withArmor("minecraft:iron_helmet", "minecraft:iron_chestplate", "", ""));

        // A random XP amount, and three drops each rolled by its own chance (common to rare).
        lurker.setDropSettings(lurker.dropSettings()
                .withXp(5, 12)
                .withDrop(0, "minecraft:rotten_flesh", 80.0F)
                .withDrop(1, "minecraft:iron_ingot", 15.0F)
                .withDrop(2, "minecraft:emerald", 4.0F));

        lurker.setTraits(lurker.traits()
                .asCreature()
                .withReaction(NpcReactionSettings.defaults().withMode(NpcReactionSettings.AGGRESSIVE)));

        lurker.setSpawn(lurker.spawn()
                .withStructureWeight("minecraft:mineshaft", 10)
                .withStructureWeight("minecraft:stronghold", 6)
                .withSurfaceMode(SurfaceMode.CAVES_ONLY)
                .withMaxNearby(6));
    }
}
