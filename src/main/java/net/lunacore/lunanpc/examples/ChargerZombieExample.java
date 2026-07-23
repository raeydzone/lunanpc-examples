package net.lunacore.lunanpc.examples;

import net.minecraft.server.MinecraftServer;
import net.lunacore.luna_npc.api.LunaNpcApi;
import net.lunacore.luna_npc.api.NpcDefinition;
import net.lunacore.luna_npc.api.NpcReactionSettings;
import net.lunacore.luna_npc.api.NpcRegistry;
import net.lunacore.luna_npc.api.SurfaceMode;

// A fast melee zombie: cave-spawning, lightly armored, and far quicker at a charge than a walk. Shows
// heavy model stretching, armor plus a weapon, cave/structure spawning, and a wide loot table.
public final class ChargerZombieExample {

    private ChargerZombieExample() {
    }

    public static void build(MinecraftServer server) {
        NpcRegistry npcs = LunaNpcApi.npcs(server);
        NpcDefinition zombie = npcs.getOrCreate("ChargerZombie");

        zombie.setHealth(40);
        zombie.setCategory("creatures");
        zombie.setAllianceId("undead");

        zombie.setHurtSoundId("minecraft:entity.zombie.hurt");
        zombie.setDieSoundId("minecraft:entity.zombie.death");
        zombie.setAmbientSoundId("minecraft:entity.zombie.ambient");

        // The lopsided stretch of the very first test zombie: big head, one huge right arm, stubby legs.
        zombie.setModelSettings(zombie.modelSettings()
                .withModel("zombie", "zombie")
                .withSize(0.89F)
                .withHeadScale(2.0F).withBodyScale(1.0F)
                .withLeftArmScale(0.5F).withRightArmScale(2.0F)
                .withLeftLegScale(0.5F).withRightLegScale(0.5F));

        zombie.giveMeleeWeapon("minecraft:diamond_sword", 7.0F);
        zombie.setEquipment(zombie.equipment().withHelmet("minecraft:iron_helmet"));

        // Slow walk, fast charge.
        zombie.setMovement(zombie.movement().withWalkSpeed(2.0F).withRunSpeed(4.0F));

        zombie.setTraits(zombie.traits().asCreature()
                .withReaction(NpcReactionSettings.defaults().withMode(NpcReactionSettings.AGGRESSIVE)));

        // Caves everywhere at 2%, plus 4% inside mineshafts.
        zombie.setSpawn(zombie.spawn()
                .withUseGlobalBiome(true).withGlobalBiomeChance(2)
                .withStructureChance("minecraft:mineshaft", 4)
                .withSurfaceMode(SurfaceMode.CAVES_ONLY));

        zombie.setDropSettings(zombie.dropSettings()
                .withXp(10, 40)
                .withDrop(0, "minecraft:rotten_flesh", 80.0F)
                .withDrop(1, "minecraft:iron_ingot", 12.0F)
                .withDrop(2, "minecraft:carrot", 3.0F)
                .withDrop(3, "minecraft:potato", 3.0F)
                .withDrop(4, "minecraft:iron_helmet", 5.0F)
                .withDrop(5, "minecraft:diamond_sword", 3.0F));
    }
}
