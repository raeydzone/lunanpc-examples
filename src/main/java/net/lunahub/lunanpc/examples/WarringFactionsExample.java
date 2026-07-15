package net.lunahub.lunanpc.examples;

import java.util.List;
import net.minecraft.server.MinecraftServer;
import net.raeydzone.luna_npc.alliance.NpcAlliance;
import net.raeydzone.luna_npc.api.AllianceRegistry;
import net.raeydzone.luna_npc.api.LunaNpcApi;
import net.raeydzone.luna_npc.api.Npc;
import net.raeydzone.luna_npc.api.NpcRegistry;
import net.raeydzone.luna_npc.api.ZoneRegistry;
import net.raeydzone.luna_npc.npc.NpcReactionSettings;
import net.raeydzone.luna_npc.zone.NpcZone;

// Faction-war example — two brand-new alliances set against each other, with a soldier on each
// side that spawns into a shared arena and fights the enemy faction on sight (but never its own).
//
// Shows: creating two alliances (reused if they already exist), making them mutual enemies with
// setEnemies, and assigning one NPC to each — the whole point of the faction system, in one file.
public final class WarringFactionsExample {

    private WarringFactionsExample() {
    }

    public static void build(MinecraftServer server) {
        NpcRegistry npcs = LunaNpcApi.npcs(server);
        if (npcs.all().stream().anyMatch(npc -> "CrimsonSoldier".equals(npc.name()))) {
            return;
        }

        // Two factions, created once and reused on later runs.
        AllianceRegistry alliances = LunaNpcApi.alliances(server);
        String crimson = reuseOrCreate(alliances, "Crimson Pact");
        String azure = reuseOrCreate(alliances, "Azure Order");

        // Make them hate each other. One side listing the other as an enemy is enough to make both
        // hostile, but we set it on both for clarity. Members of each side stay friendly among themselves.
        alliances.setEnemies(crimson, List.of(azure));
        alliances.setEnemies(azure, List.of(crimson));

        // A shared arena both sides spawn into, so they actually meet and fight.
        ZoneRegistry zones = LunaNpcApi.zones(server);
        NpcZone arena = zones.all().stream()
                .filter(zone -> "War Arena".equals(zone.name()))
                .findFirst()
                .orElseGet(() -> zones.createBox("War Arena", "minecraft:overworld", -30, 63, -30, 30, 90, 30));

        spawnSoldier(npcs, "CrimsonSoldier", crimson, "minecraft:iron_sword", arena.id());
        spawnSoldier(npcs, "AzureSoldier", azure, "minecraft:golden_sword", arena.id());
    }

    // The id of the alliance with this name, creating it only if none exists yet.
    private static String reuseOrCreate(AllianceRegistry alliances, String name) {
        return alliances.all().stream()
                .filter(alliance -> name.equals(alliance.name()))
                .map(NpcAlliance::id)
                .findFirst()
                .orElseGet(() -> alliances.create(name).id());
    }

    private static void spawnSoldier(NpcRegistry npcs, String name, String allianceId, String weapon, String arenaId) {
        Npc soldier = npcs.create(name);
        soldier.setHealth(24);
        soldier.setNameTagShown(true);
        soldier.setAllianceId(allianceId);
        soldier.giveMeleeWeapon(weapon, 5.0F);
        // Aggressive so each side hunts the other the moment they cross paths.
        soldier.setTraits(soldier.traits()
                .withReaction(NpcReactionSettings.defaults().withMode(NpcReactionSettings.AGGRESSIVE)));
        // Both spawn in the arena (weight 4, up to 3 of each side alive at once).
        soldier.spawnInZone(arenaId, 4, 3);
    }
}
