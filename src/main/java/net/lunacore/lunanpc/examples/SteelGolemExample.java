package net.lunacore.lunanpc.examples;

import net.minecraft.server.MinecraftServer;
import net.lunacore.luna_npc.api.LunaNpcApi;
import net.lunacore.luna_npc.api.NpcAttackSettings;
import net.lunacore.luna_npc.api.NpcDefinition;
import net.lunacore.luna_npc.api.NpcReactionSettings;
import net.lunacore.luna_npc.api.NpcRegistry;
import net.lunacore.luna_npc.api.SurfaceMode;

// A heavy melee construct: a metallic iron golem that natural-spawns at villages, hits hard but slowly,
// and drops scrap. Shows structure spawning, a weaponless melee attack, and per-item chance drops.
public final class SteelGolemExample {

    private SteelGolemExample() {
    }

    public static void build(MinecraftServer server) {
        NpcRegistry npcs = LunaNpcApi.npcs(server);
        NpcDefinition golem = npcs.getOrCreate("SteelGolem");

        golem.setHealth(200);
        golem.setCategory("golems");
        golem.setAllianceId("golems");
        golem.setModelSettings(golem.modelSettings().withModel("iron_golem", "iron_golem_movie"));

        golem.setHurtSoundId("minecraft:entity.iron_golem.hurt");
        golem.setDieSoundId("minecraft:entity.iron_golem.death");
        golem.setCombatCloseSoundId("minecraft:entity.iron_golem.attack");

        // 20 damage, 3-block reach, one swing every 5 seconds — no weapon item, it strikes bare-handed.
        golem.setCombat(golem.combat().withMelee(NpcAttackSettings.melee(20.0F, 3.0F).withInterval(5.0F)));

        // Walks and charges at the same 2 blocks/second.
        golem.setMovement(golem.movement().withWalkSpeed(2.0F).withRunSpeed(2.0F));

        golem.setTraits(golem.traits().asCreature()
                .withReaction(NpcReactionSettings.defaults().withMode(NpcReactionSettings.AGGRESSIVE)));

        // Spawns at any village, at the surface, with a 5% spawn chance.
        golem.setSpawn(golem.spawn()
                .withStructureChance("minecraft:village_plains", 5)
                .withStructureChance("minecraft:village_desert", 5)
                .withStructureChance("minecraft:village_savanna", 5)
                .withStructureChance("minecraft:village_snowy", 5)
                .withStructureChance("minecraft:village_taiga", 5)
                .withSurfaceMode(SurfaceMode.SURFACE_ONLY));

        // Each drop rolls its own chance on death. Drops are one item per slot (no stack counts), so
        // repeated slots stand in for "a few" stone / iron.
        golem.setDropSettings(golem.dropSettings()
                .withDrop(0, "minecraft:dandelion", 8.0F)
                .withDrop(1, "minecraft:poppy", 5.0F)
                .withDrop(2, "minecraft:cornflower", 3.0F)
                .withDrop(3, "minecraft:oxeye_daisy", 2.0F)
                .withDrop(4, "minecraft:flint_and_steel", 5.0F)
                .withDrop(5, "minecraft:stone", 10.0F)
                .withDrop(6, "minecraft:stone", 10.0F)
                .withDrop(7, "minecraft:stone", 10.0F)
                .withDrop(8, "minecraft:iron_ingot", 5.0F)
                .withDrop(9, "minecraft:iron_ingot", 5.0F)
                .withDrop(10, "minecraft:iron_ingot", 5.0F)
                .withDrop(11, "minecraft:iron_ingot", 5.0F));
    }
}
