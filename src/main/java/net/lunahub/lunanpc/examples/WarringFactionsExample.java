package net.lunahub.lunanpc.examples;

import net.minecraft.server.MinecraftServer;
import net.lunahub.luna_npc.api.AllianceRegistry;
import net.lunahub.luna_npc.api.LunaNpcApi;
import net.lunahub.luna_npc.api.NpcDefinition;
import net.lunahub.luna_npc.api.NpcRegistry;
import net.lunahub.luna_npc.api.ZoneRegistry;
import net.lunahub.luna_npc.npc.NpcReactionSettings;
import net.lunahub.luna_npc.zone.NpcZone;

// Two rival factions set as mutual enemies, with a soldier each — a piglin brute vs. an illager
// raider — that spawn into a shared arena and fight on sight (but never their own side).
public final class WarringFactionsExample {

    private WarringFactionsExample() {
    }

    public static void build(MinecraftServer server) {
        AllianceRegistry alliances = LunaNpcApi.alliances(server);
        String crimson = alliances.getOrCreate("Crimson Pact").id();
        String azure = alliances.getOrCreate("Azure Order").id();
        // addEnemy is incremental — it won't wipe relationships another mod added to these factions.
        alliances.addEnemy(crimson, azure);
        alliances.addEnemy(azure, crimson);

        ZoneRegistry zones = LunaNpcApi.zones(server);
        NpcZone arena = zones.getOrCreateBox("War Arena", "minecraft:overworld", -30, 63, -30, 30, 90, 30);

        NpcRegistry npcs = LunaNpcApi.npcs(server);
        spawnSoldier(npcs, "CrimsonSoldier", crimson, "minecraft:iron_sword", "piglin", "piglin_brute", arena.id());
        spawnSoldier(npcs, "AzureSoldier", azure, "minecraft:golden_sword", "illager", "vindicator", arena.id());
    }

    private static void spawnSoldier(NpcRegistry npcs, String name, String allianceId, String weapon,
                                     String modelType, String modelId, String arenaId) {
        NpcDefinition soldier = npcs.getOrCreate(name);
        soldier.setHealth(24);
        soldier.setNameTagShown(true);
        soldier.setAllianceId(allianceId);
        soldier.setModelSettings(soldier.modelSettings().withModel(modelType, modelId));
        soldier.giveMeleeWeapon(weapon, 5.0F);
        soldier.setTraits(soldier.traits()
                .withReaction(NpcReactionSettings.defaults().withMode(NpcReactionSettings.AGGRESSIVE)));
        soldier.spawnInZone(arenaId, 4, 3);
    }
}
