package net.lunahub.lunanpc.examples;

import net.minecraft.server.MinecraftServer;
import net.lunahub.luna_npc.api.LunaNpcApi;
import net.lunahub.luna_npc.api.NpcDefinition;
import net.lunahub.luna_npc.api.NpcRegistry;
import net.lunahub.luna_npc.api.NpcReactionSettings;

// A rare, oversized boss with a boss bar: a hulking, stretched "corbin" titan in full netherite that
// spawns only in ancient cities. The huge stats are intentional — LunaNPC never clamps for "balance".
public final class BossExample {

    private BossExample() {
    }

    public static void build(MinecraftServer server) {
        NpcRegistry npcs = LunaNpcApi.npcs(server);
        NpcDefinition titan = npcs.getOrCreate("DeepDarkTitan");

        titan.setHealth(300);
        titan.setNameTagShown(true);
        titan.setAllianceId("bosses");
        titan.setBossBar(titan.bossBar().withEnabled(true).withColor("purple"));

        // Over twice normal size and stretched into a hulk; the player model renders the gear below.
        titan.setModelSettings(titan.modelSettings()
                .withModel("player", "corbin")
                .withSize(2.2F)
                .withHeadScale(1.2F).withBodyScale(1.4F)
                .withLeftArmScale(1.6F).withRightArmScale(1.6F)
                .withLeftLegScale(1.4F).withRightLegScale(1.4F));

        titan.giveMeleeWeapon("minecraft:netherite_axe", 18.0F);
        titan.setEquipment(titan.equipment().withArmor(
                "minecraft:netherite_helmet", "minecraft:netherite_chestplate",
                "minecraft:netherite_leggings", "minecraft:netherite_boots"));

        titan.setTraits(titan.traits()
                .asCreature()
                .withReaction(NpcReactionSettings.defaults().withMode(NpcReactionSettings.AGGRESSIVE)));

        titan.setSpawn(titan.spawn()
                .withStructureWeight("minecraft:ancient_city", 1)
                .withMaxNearby(1));
    }
}
