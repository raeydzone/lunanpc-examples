package net.lunahub.lunanpc.examples;

import net.minecraft.server.MinecraftServer;
import net.raeydzone.luna_npc.api.LunaNpcApi;
import net.raeydzone.luna_npc.api.Npc;
import net.raeydzone.luna_npc.api.NpcRegistry;
import net.raeydzone.luna_npc.npc.NpcReactionSettings;
import net.raeydzone.luna_npc.zone.SurfaceMode;

// Biome example — a hostile archer that natural-spawns out in the open across desert biomes.
//
// Shows: a "stray" skeleton look stretched a little tall and lean, biome-driven spawning, a ranged
// weapon (bow + arrow), tuning the combat brain into a long-range sniper, surface-only placement,
// and joining the built-in "Monsters" faction so it hunts players out of the box. Biome ids come
// from the live registry — modded biomes work too.
public final class BiomeArcherExample {

    private BiomeArcherExample() {
    }

    public static void build(MinecraftServer server) {
        NpcRegistry npcs = LunaNpcApi.npcs(server);
        if (npcs.all().stream().anyMatch(npc -> "DesertArcher".equals(npc.name()))) {
            return;
        }

        Npc archer = npcs.create("DesertArcher");
        archer.setHealth(20);

        // Look: the bleached "stray" skeleton skin, stretched slightly tall and lean for a lanky
        // desert sharpshooter. Skeleton is one of the stretchable humanoid models.
        archer.setModelSettings(archer.modelSettings()
                .withModel("skeleton", "stray")
                .withLeftArmScale(1.2F).withRightArmScale(1.2F)
                .withLeftLegScale(1.3F).withRightLegScale(1.3F));

        // "creatures" is the built-in Monsters faction — already hostile to players.
        archer.setAllianceId("creatures");

        // A bow that fires arrows for 4 damage.
        archer.giveRangedWeapon("minecraft:bow", "minecraft:arrow", 4.0F);

        // A sniper: opens fire from far off (aggro range 28), but only with a clear line of sight.
        archer.setCombat(archer.combat()
                .withAggroRange(28.0F)
                .withRequireLineOfSight(true));

        // Aggressive temperament so it engages on sight.
        archer.setTraits(archer.traits()
                .withReaction(NpcReactionSettings.defaults().withMode(NpcReactionSettings.AGGRESSIVE)));

        // Spawn in the desert (weight 5), only at the open surface, capped at 4 alive nearby.
        archer.spawnNaturallyIn("minecraft:desert", 5);
        archer.setSurfaceMode(SurfaceMode.SURFACE_ONLY);
        archer.setMaxNearby(4);
    }
}
