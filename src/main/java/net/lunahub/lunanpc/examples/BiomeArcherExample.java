package net.lunahub.lunanpc.examples;

import net.minecraft.server.MinecraftServer;
import net.lunahub.luna_npc.api.LunaNpcApi;
import net.lunahub.luna_npc.api.Npc;
import net.lunahub.luna_npc.api.NpcRegistry;
import net.lunahub.luna_npc.npc.NpcReactionSettings;
import net.lunahub.luna_npc.zone.SurfaceMode;

// A hostile ranged archer: a lean "stray" skeleton that natural-spawns across desert biomes at the
// surface, tuned into a long-range sniper, in the built-in "creatures" (Monsters) faction.
public final class BiomeArcherExample {

    private BiomeArcherExample() {
    }

    public static void build(MinecraftServer server) {
        NpcRegistry npcs = LunaNpcApi.npcs(server);
        Npc archer = npcs.getOrCreate("DesertArcher");

        archer.setHealth(20);
        archer.setAllianceId("creatures");

        // A "stray" skeleton, stretched slightly tall and lean.
        archer.setModelSettings(archer.modelSettings()
                .withModel("skeleton", "stray")
                .withLeftArmScale(1.2F).withRightArmScale(1.2F)
                .withLeftLegScale(1.3F).withRightLegScale(1.3F));

        archer.giveRangedWeapon("minecraft:bow", "minecraft:arrow", 4.0F);

        // A sniper: opens fire from far off, but only with a clear line of sight.
        archer.setCombat(archer.combat()
                .withAggroRange(28.0F)
                .withRequireLineOfSight(true));

        archer.setTraits(archer.traits()
                .withReaction(NpcReactionSettings.defaults().withMode(NpcReactionSettings.AGGRESSIVE)));

        archer.spawnNaturallyIn("minecraft:desert", 5);
        archer.setSurfaceMode(SurfaceMode.SURFACE_ONLY);
        archer.setMaxNearby(4);
    }
}
