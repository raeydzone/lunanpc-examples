package net.lunahub.lunanpc.examples;

import java.util.List;
import net.minecraft.server.MinecraftServer;
import net.lunahub.luna_npc.api.LunaNpcApi;
import net.lunahub.luna_npc.api.NpcAttackRange;
import net.lunahub.luna_npc.api.NpcDefinition;
import net.lunahub.luna_npc.api.NpcRangeComparator;
import net.lunahub.luna_npc.api.NpcRangeMetric;
import net.lunahub.luna_npc.api.NpcRangeRule;
import net.lunahub.luna_npc.api.NpcRangeSwitch;
import net.lunahub.luna_npc.api.NpcReactionSettings;
import net.lunahub.luna_npc.api.NpcRegistry;

// A skirmisher that fights at whichever range the situation calls for. It carries both a sword and a
// bow, so it visibly swaps weapons as the rules flip it between close and long range: it snipes by
// default, retreats to ranged when badly hurt, keeps a swarm at bay with the bow, and otherwise draws
// its blade once a target closes in. The rules are priority-ordered — the first that passes wins — so a
// low-health check placed first beats the melee-range check that follows it.
public final class RangeSwitchGuardExample {

    private RangeSwitchGuardExample() {
    }

    public static void build(MinecraftServer server) {
        NpcRegistry npcs = LunaNpcApi.npcs(server);
        NpcDefinition skirmisher = npcs.getOrCreate("Skirmisher");

        skirmisher.setHealth(40);
        skirmisher.setAllianceId("creatures");

        // Both attacks armed: a sword in the main hand and a bow in the ranged slot, each shown in hand
        // while that range is active.
        skirmisher.giveMeleeWeapon("minecraft:iron_sword", 6.0F);
        skirmisher.giveRangedWeapon("minecraft:bow", "minecraft:arrow", 4.0F);

        NpcRangeSwitch rangeSwitch = new NpcRangeSwitch(true, List.of(
                new NpcRangeRule(NpcRangeMetric.SELF_HP, NpcRangeComparator.AT_MOST, 12.0F, 0, NpcAttackRange.LONG),
                new NpcRangeRule(NpcRangeMetric.DISTANCE, NpcRangeComparator.AT_MOST, 4.0F, 0, NpcAttackRange.CLOSE),
                new NpcRangeRule(NpcRangeMetric.ENEMIES_IN_RADIUS, NpcRangeComparator.AT_LEAST, 3.0F, 6,
                        NpcAttackRange.LONG),
                new NpcRangeRule(NpcRangeMetric.OTHERWISE, NpcRangeComparator.AT_MOST, 0.0F, 0,
                        NpcAttackRange.LONG)));
        skirmisher.setRangeSwitch(rangeSwitch);

        skirmisher.setCombat(skirmisher.combat().withAggroRange(28.0F).withDistancePreference(12.0F));
        skirmisher.setTraits(skirmisher.traits()
                .withReaction(NpcReactionSettings.defaults().withMode(NpcReactionSettings.AGGRESSIVE)));
    }
}
