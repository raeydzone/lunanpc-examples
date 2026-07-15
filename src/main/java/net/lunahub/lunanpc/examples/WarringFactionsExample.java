package net.lunahub.lunanpc.examples;

import java.util.List;
import net.minecraft.server.MinecraftServer;
import net.lunahub.luna_npc.alliance.NpcAlliance;
import net.lunahub.luna_npc.api.AllianceRegistry;
import net.lunahub.luna_npc.api.LunaNpcApi;
import net.lunahub.luna_npc.api.Npc;
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
        String crimson = reuseOrCreate(alliances, "Crimson Pact");
        String azure = reuseOrCreate(alliances, "Azure Order");
        alliances.setEnemies(crimson, List.of(azure));
        alliances.setEnemies(azure, List.of(crimson));

        ZoneRegistry zones = LunaNpcApi.zones(server);
        NpcZone arena = zones.all().stream()
                .filter(zone -> "War Arena".equals(zone.name()))
                .findFirst()
                .orElseGet(() -> zones.createBox("War Arena", "minecraft:overworld", -30, 63, -30, 30, 90, 30));

        NpcRegistry npcs = LunaNpcApi.npcs(server);
        spawnSoldier(npcs, "CrimsonSoldier", crimson, "minecraft:iron_sword", "piglin", "piglin_brute", arena.id());
        spawnSoldier(npcs, "AzureSoldier", azure, "minecraft:golden_sword", "illager", "vindicator", arena.id());
    }

    private static String reuseOrCreate(AllianceRegistry alliances, String name) {
        return alliances.all().stream()
                .filter(alliance -> name.equals(alliance.name()))
                .map(NpcAlliance::id)
                .findFirst()
                .orElseGet(() -> alliances.create(name).id());
    }

    private static void spawnSoldier(NpcRegistry npcs, String name, String allianceId, String weapon,
                                     String modelType, String modelId, String arenaId) {
        Npc soldier = npcs.all().stream()
                .filter(npc -> name.equals(npc.name()))
                .map(Npc.class::cast)
                .findFirst()
                .orElseGet(() -> npcs.create(name));
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
