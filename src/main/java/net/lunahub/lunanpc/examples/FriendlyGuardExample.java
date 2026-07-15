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

// Friendly example — a town guard that stands watch in a zone, belongs to its own faction, defends
// the town against monsters, and never turns on players.
//
// Shows: building a spawn zone from plain coordinates, creating an alliance only if it does not
// already exist and wiring its relationships (friendly to players and villagers, hostile to the
// built-in monster factions), showing the name tag, a defensive temperament, and placing the NPC
// as a natural spawn inside the zone.
public final class FriendlyGuardExample {

    private FriendlyGuardExample() {
    }

    public static void build(MinecraftServer server) {
        NpcRegistry npcs = LunaNpcApi.npcs(server);
        if (npcs.all().stream().anyMatch(npc -> "TownGuard".equals(npc.name()))) {
            return;
        }

        // A town-square zone in the overworld (reused if a previous run already made it).
        ZoneRegistry zones = LunaNpcApi.zones(server);
        NpcZone square = zones.all().stream()
                .filter(zone -> "Town Square".equals(zone.name()))
                .findFirst()
                .orElseGet(() -> zones.createBox("Town Square", "minecraft:overworld", -20, 63, -20, 20, 80, 20));

        // A "Townsfolk" faction — created once, then reused. It protects players and villagers and
        // treats the built-in monster factions as enemies, so guards fight off the DungeonLurker and
        // DesertArcher from the other examples while leaving players alone.
        AllianceRegistry alliances = LunaNpcApi.alliances(server);
        String townsfolk = alliances.all().stream()
                .filter(alliance -> "Townsfolk".equals(alliance.name()))
                .map(NpcAlliance::id)
                .findFirst()
                .orElseGet(() -> alliances.create("Townsfolk").id());
        alliances.setMates(townsfolk, List.of("players", "villagers"));
        alliances.setEnemies(townsfolk, List.of("undead", "creatures", "illagers"));

        Npc guard = npcs.create("TownGuard");
        guard.setHealth(40);
        guard.setNameTagShown(true);
        guard.setAllianceId(townsfolk);
        guard.giveMeleeWeapon("minecraft:iron_sword", 8.0F);

        // Defensive: defends itself and nearby mates and fights back when attacked, but never hunts —
        // so it stays friendly to players (who are mates) and only clashes with monsters.
        guard.setTraits(guard.traits()
                .withReaction(NpcReactionSettings.defaults().withMode(NpcReactionSettings.DEFENSIVE)));

        // Spawn inside the town square (weight 3, at most 2 alive there).
        guard.spawnInZone(square.id(), 3, 2);
    }
}
