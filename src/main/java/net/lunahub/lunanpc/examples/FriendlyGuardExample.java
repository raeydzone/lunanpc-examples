package net.lunahub.lunanpc.examples;

import java.util.List;
import net.minecraft.server.MinecraftServer;
import net.lunahub.luna_npc.api.AllianceRegistry;
import net.lunahub.luna_npc.api.LunaNpcApi;
import net.lunahub.luna_npc.api.NpcDefinition;
import net.lunahub.luna_npc.api.NpcRegistry;
import net.lunahub.luna_npc.api.ZoneRegistry;
import net.lunahub.luna_npc.api.NpcReactionSettings;
import net.lunahub.luna_npc.api.NpcZone;

// A friendly town guard: a sturdy custom player skin standing in a zone, in its own "Townsfolk"
// faction (friendly to players and villagers, hostile to the monster factions), that defends the town.
public final class FriendlyGuardExample {

    private FriendlyGuardExample() {
    }

    public static void build(MinecraftServer server) {
        // A town-square zone, reused if it already exists.
        ZoneRegistry zones = LunaNpcApi.zones(server);
        NpcZone square = zones.getOrCreateBox("Town Square", "minecraft:overworld", -20, 63, -20, 20, 80, 20);

        // A "Townsfolk" faction, reused if it exists; protects players/villagers, hostile to monsters.
        AllianceRegistry alliances = LunaNpcApi.alliances(server);
        String townsfolk = alliances.getOrCreate("Townsfolk").id();
        alliances.setMates(townsfolk, List.of("players", "villagers"));
        alliances.setEnemies(townsfolk, List.of("undead", "creatures", "illagers"));

        NpcRegistry npcs = LunaNpcApi.npcs(server);
        NpcDefinition guard = npcs.getOrCreate("TownGuard");

        guard.setHealth(40);
        guard.setNameTagShown(true);
        guard.setAllianceId(townsfolk);

        // A sturdy "kai" player skin, broadened through the chest and arms.
        guard.setModelSettings(guard.modelSettings()
                .withModel("player", "kai")
                .withBodyScale(1.15F)
                .withLeftArmScale(1.25F).withRightArmScale(1.25F));

        guard.giveMeleeWeapon("minecraft:iron_sword", 8.0F);

        // Defensive: defends itself and mates but never hunts, so it stays friendly to players.
        guard.setTraits(guard.traits()
                .withReaction(NpcReactionSettings.defaults().withMode(NpcReactionSettings.DEFENSIVE)));

        guard.spawnInZone(square.id(), 3, 2);
    }
}
