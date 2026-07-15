package net.lunahub.lunanpc.examples;

import net.minecraft.server.MinecraftServer;
import net.raeydzone.luna_npc.api.LunaNpcApi;
import net.raeydzone.luna_npc.api.Npc;
import net.raeydzone.luna_npc.api.NpcRegistry;
import net.raeydzone.luna_npc.npc.NpcReactionSettings;

// Boss example — a rare, oversized raid boss with a boss bar that guards the deep dark.
//
// Shows: LunaNPC's "no forced balance" stance (huge health and damage are allowed by design), a
// boss bar, a full model makeover — scaled to more than twice size and stretched into a hulking
// silhouette — strong melee gear that renders on the stretched body, and rare structure spawning
// capped at a single alive. It joins the built-in "Bosses" faction, hostile to essentially everything.
public final class BossExample {

    private BossExample() {
    }

    public static void build(MinecraftServer server) {
        NpcRegistry npcs = LunaNpcApi.npcs(server);
        if (npcs.all().stream().anyMatch(npc -> "DeepDarkTitan".equals(npc.name()))) {
            return;
        }

        Npc titan = npcs.create("DeepDarkTitan");
        titan.setHealth(300); // no forced balance — extreme values are intentional, not clamped
        titan.setNameTagShown(true);

        // The built-in "Bosses" faction attacks nearly every other faction.
        titan.setAllianceId("bosses");

        // A purple boss bar, showing the NPC's name.
        titan.setBossBar(titan.bossBar().withEnabled(true).withColor("purple"));

        // Look: a "corbin" player skin blown up to more than twice size and stretched into a hulking
        // titan — huge arms, long legs, a broad body. The player model renders the armor and weapon
        // below, and part scales stay within the 0.5-2.0 stretch range.
        titan.setModelSettings(titan.modelSettings()
                .withModel("player", "corbin")
                .withSize(2.2F)
                .withHeadScale(1.2F)
                .withBodyScale(1.4F)
                .withLeftArmScale(1.6F).withRightArmScale(1.6F)
                .withLeftLegScale(1.4F).withRightLegScale(1.4F));

        // A netherite axe hitting for 18, plus a full netherite set (cosmetic by default).
        titan.giveMeleeWeapon("minecraft:netherite_axe", 18.0F);
        titan.setEquipment(titan.equipment().withArmor(
                "minecraft:netherite_helmet", "minecraft:netherite_chestplate",
                "minecraft:netherite_leggings", "minecraft:netherite_boots"));

        // Aggressive; potions affect it like a mob, not a player.
        titan.setTraits(titan.traits()
                .asCreature()
                .withReaction(NpcReactionSettings.defaults().withMode(NpcReactionSettings.AGGRESSIVE)));

        // Rare: only inside ancient cities, with at most one alive near any player.
        titan.setSpawn(titan.spawn()
                .withStructureWeight("minecraft:ancient_city", 1)
                .withMaxNearby(1));
    }
}
