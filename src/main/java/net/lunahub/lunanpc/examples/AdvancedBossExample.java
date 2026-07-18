package net.lunahub.lunanpc.examples;

import java.util.List;
import net.minecraft.server.MinecraftServer;
import net.lunahub.luna_npc.api.LunaNpcApi;
import net.lunahub.luna_npc.api.NpcAdvancedCombat;
import net.lunahub.luna_npc.api.NpcAttackEffect;
import net.lunahub.luna_npc.api.NpcCombatAction;
import net.lunahub.luna_npc.api.NpcCombatActionType;
import net.lunahub.luna_npc.api.NpcCombatAttack;
import net.lunahub.luna_npc.api.NpcCombatAttackKind;
import net.lunahub.luna_npc.api.NpcCombatComparator;
import net.lunahub.luna_npc.api.NpcCombatCondition;
import net.lunahub.luna_npc.api.NpcCombatPlacement;
import net.lunahub.luna_npc.api.NpcCombatRule;
import net.lunahub.luna_npc.api.NpcCombatTrigger;
import net.lunahub.luna_npc.api.NpcCombatTriggerType;
import net.lunahub.luna_npc.api.NpcDefinition;
import net.lunahub.luna_npc.api.NpcPlacementCentre;
import net.lunahub.luna_npc.api.NpcPlacementShape;
import net.lunahub.luna_npc.api.NpcReactionSettings;
import net.lunahub.luna_npc.api.NpcRegistry;
import net.lunahub.luna_npc.api.NpcTargetSelector;
import net.lunahub.luna_npc.api.NpcValueSource;
import net.lunahub.luna_npc.api.NpcValueSourceType;

// A node-based advanced-combat boss: the Ember Colossus fights by a flat list of rules, each a trigger
// plus conditions plus actions. HP is just a condition, so the "phases" (poke at range, cleave up close,
// enrage and summon adds at half health, heal when low, telegraphed nova when swarmed) are emergent from
// the rules — there is no phase construct. This is the developer mirror of the in-game node editor.
public final class AdvancedBossExample {

    private AdvancedBossExample() {
    }

    public static void build(MinecraftServer server) {
        NpcRegistry npcs = LunaNpcApi.npcs(server);
        NpcDefinition boss = npcs.getOrCreate("EmberColossus");

        boss.setHealth(400);
        boss.setNameTagShown(true);
        boss.setAllianceId("bosses");
        boss.setBossBar(boss.bossBar().withEnabled(true).withColor("red"));
        boss.setModelSettings(boss.modelSettings().withModel("player", "corbin").withSize(2.4F));
        boss.giveMeleeWeapon("minecraft:netherite_axe", 12.0F);
        boss.setTraits(boss.traits().asCreature()
                .withReaction(NpcReactionSettings.defaults().withMode(NpcReactionSettings.AGGRESSIVE)));
        boss.setSpawn(boss.spawn().withStructureWeight("minecraft:fortress", 1).withMaxNearby(1));

        // The attack library: reusable attacks the rules invoke by name, each independent of the NPC's
        // simple melee/ranged config.
        NpcCombatAttack cleave = new NpcCombatAttack("cleave", NpcCombatAttackKind.MELEE, 14.0F, 4.5F, 20.0F,
                "minecraft:arrow", false, false, 1, 0.0F, List.of(), NpcCombatPlacement.atTarget());
        NpcCombatAttack ember = new NpcCombatAttack("ember_bolt", NpcCombatAttackKind.PROJECTILE, 9.0F, 32.0F,
                18.0F, "minecraft:fire_charge", true, false, 1, 0.0F,
                List.of(new NpcAttackEffect("flame", 0.0F, 1)), NpcCombatPlacement.atSelf());
        NpcCombatAttack flameRing = new NpcCombatAttack("flame_ring", NpcCombatAttackKind.AOE, 16.0F, 3.0F,
                20.0F, "minecraft:arrow", false, false, 1, 0.0F,
                List.of(new NpcAttackEffect("minecraft:wither", 4.0F, 0)),
                new NpcCombatPlacement(NpcPlacementCentre.TARGET, NpcPlacementShape.RING, 0.0F, 6.0F, 5, 0, 0));

        NpcValueSource distance = source(NpcValueSourceType.DISTANCE_TO_TARGET);
        NpcValueSource selfHpPercent = source(NpcValueSourceType.SELF_HP_PERCENT);

        // Poke with ember bolts while the target is out of reach.
        NpcCombatRule ranged = rule(
                trigger(NpcCombatTriggerType.INTERVAL, 2.0F, 0.0F),
                List.of(condition(distance, NpcCombatComparator.GREATER, 5.0F)),
                List.of(useAttack("ember_bolt", NpcTargetSelector.PRIMARY)),
                0.5F, false, -140, -40);

        // Cleave once the target closes in.
        NpcCombatRule melee = rule(
                trigger(NpcCombatTriggerType.INTERVAL, 1.2F, 0.0F),
                List.of(condition(distance, NpcCombatComparator.LESS_EQUAL, 5.0F)),
                List.of(useAttack("cleave", NpcTargetSelector.PRIMARY)),
                0.4F, false, 40, -40);

        // At half health, enrage for ten seconds, summon three zombies around the target, and relabel the bar.
        NpcCombatRule enrage = rule(
                trigger(NpcCombatTriggerType.ON_HP_CROSSES, 0.0F, 50.0F),
                List.of(),
                List.of(
                        action(NpcCombatActionType.ENRAGE, NpcTargetSelector.SELF, NpcCombatPlacement.atSelf(),
                                NpcValueSource.constant(1.5F), 200, 0, ""),
                        action(NpcCombatActionType.SUMMON, NpcTargetSelector.PRIMARY,
                                new NpcCombatPlacement(NpcPlacementCentre.TARGET, NpcPlacementShape.RING, 0.0F,
                                        4.0F, 3, 0, 0),
                                NpcValueSource.constant(0.0F), 0, 0, "minecraft:zombie"),
                        action(NpcCombatActionType.BOSS_BAR, NpcTargetSelector.SELF, NpcCombatPlacement.atSelf(),
                                NpcValueSource.constant(0.0F), 0, 0, "Ember Colossus — Enraged")),
                0.0F, true, -50, 70);

        // Mend when badly hurt, but no more than once every eight seconds.
        NpcCombatRule mend = rule(
                trigger(NpcCombatTriggerType.INTERVAL, 8.0F, 0.0F),
                List.of(condition(selfHpPercent, NpcCombatComparator.LESS, 30.0F)),
                List.of(action(NpcCombatActionType.HEAL, NpcTargetSelector.SELF, NpcCombatPlacement.atSelf(),
                        NpcValueSource.constant(30.0F), 0, 0, "")),
                0.0F, false, -140, 170);

        // When three or more hostiles crowd it, telegraph a ring, wait, then erupt.
        NpcCombatRule nova = rule(
                trigger(NpcCombatTriggerType.ON_SURROUNDED, 0.0F, 3.0F),
                List.of(),
                List.of(
                        action(NpcCombatActionType.TELEGRAPH, NpcTargetSelector.SELF,
                                new NpcCombatPlacement(NpcPlacementCentre.SELF, NpcPlacementShape.AT_CENTRE, 0.0F,
                                        6.0F, 1, 0, 1),
                                NpcValueSource.constant(0.0F), 20, 0, "minecraft:flame"),
                        action(NpcCombatActionType.WAIT, NpcTargetSelector.SELF, NpcCombatPlacement.atSelf(),
                                NpcValueSource.constant(0.0F), 20, 0, ""),
                        action(NpcCombatActionType.EXPLOSION, NpcTargetSelector.ALL_IN_RANGE,
                                new NpcCombatPlacement(NpcPlacementCentre.SELF, NpcPlacementShape.AT_CENTRE, 0.0F,
                                        6.0F, 1, 0, 1),
                                NpcValueSource.constant(12.0F), 0, 0, "")),
                5.0F, false, 40, 170);

        NpcAdvancedCombat advanced = NpcAdvancedCombat.defaults()
                .withEnabled(true)
                .withEngagementRadius(28.0F)
                .withAttackLibrary(List.of(cleave, ember, flameRing))
                .withRules(List.of(ranged, melee, enrage, mend, nova));
        boss.setAdvancedCombat(advanced);
    }

    private static NpcValueSource source(NpcValueSourceType type) {
        return new NpcValueSource(type, 0.0F, 0.0F, 0, "");
    }

    private static NpcCombatCondition condition(NpcValueSource left, NpcCombatComparator comparator, float right) {
        return new NpcCombatCondition(left, comparator, NpcValueSource.constant(right), false);
    }

    private static NpcCombatTrigger trigger(NpcCombatTriggerType type, float seconds, float threshold) {
        return new NpcCombatTrigger(type, seconds, threshold, "");
    }

    private static NpcCombatAction useAttack(String name, NpcTargetSelector target) {
        return action(NpcCombatActionType.USE_ATTACK, target, NpcCombatPlacement.atTarget(),
                NpcValueSource.constant(0.0F), 0, 0, name);
    }

    private static NpcCombatAction action(NpcCombatActionType type, NpcTargetSelector target,
                                          NpcCombatPlacement placement, NpcValueSource amount, int durationTicks,
                                          int delayTicks, String stringArg) {
        return new NpcCombatAction(type, target, placement, amount, durationTicks, delayTicks, stringArg,
                List.of(), false);
    }

    private static NpcCombatRule rule(NpcCombatTrigger trigger, List<NpcCombatCondition> conditions,
                                      List<NpcCombatAction> actions, float cooldownSeconds, boolean once,
                                      int graphX, int graphY) {
        return new NpcCombatRule(trigger, conditions, actions, cooldownSeconds, once, graphX, graphY);
    }
}
