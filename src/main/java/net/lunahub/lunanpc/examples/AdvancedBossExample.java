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

// A node-based advanced-combat boss showing off most of the toolbox: five attack kinds (melee, homing
// projectile, AoE ring, hitscan beam, raining pattern), a temporary nav-mode switch to flight with a
// direct climb over the target, an "airborne" flag + timer that gates a whole aerial phase, an enrage +
// summon phase, a self-heal, and a telegraphed nova around the boss. HP is just a condition, so the phases
// are emergent — there is no phase construct. This mirrors, one-to-one, what the in-game node editor builds.
public final class AdvancedBossExample {

    private static final String AIRBORNE = "airborne";
    private static final int FLIGHT_TICKS = 300;

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

        NpcCombatAttack cleave = new NpcCombatAttack("cleave", NpcCombatAttackKind.MELEE, 14.0F, 4.5F, 20.0F,
                "minecraft:arrow", false, false, 1, 0.0F, List.of(), NpcCombatPlacement.atTarget());
        NpcCombatAttack ember = new NpcCombatAttack("ember_bolt", NpcCombatAttackKind.PROJECTILE, 9.0F, 32.0F,
                18.0F, "minecraft:fire_charge", true, false, 1, 0.0F,
                List.of(new NpcAttackEffect("flame", 0.0F, 1)), NpcCombatPlacement.atSelf());
        NpcCombatAttack flameRing = new NpcCombatAttack("flame_ring", NpcCombatAttackKind.AOE, 16.0F, 3.0F,
                20.0F, "minecraft:arrow", false, false, 1, 0.0F,
                List.of(new NpcAttackEffect("minecraft:wither", 4.0F, 0)),
                new NpcCombatPlacement(NpcPlacementCentre.TARGET, NpcPlacementShape.RING, 0.0F, 6.0F, 5, 0, 0));
        NpcCombatAttack scorchBeam = new NpcCombatAttack("scorch_beam", NpcCombatAttackKind.BEAM, 12.0F, 2.0F,
                20.0F, "minecraft:arrow", false, false, 1, 0.0F,
                List.of(new NpcAttackEffect("flame", 0.0F, 2)), NpcCombatPlacement.atTarget());
        NpcCombatAttack starFall = new NpcCombatAttack("star_fall", NpcCombatAttackKind.PATTERN, 8.0F, 2.0F,
                22.0F, "minecraft:fire_charge", false, true, 1, 4.0F, List.of(),
                new NpcCombatPlacement(NpcPlacementCentre.TARGET, NpcPlacementShape.RANDOM_IN_AREA, 0.0F, 8.0F, 6, 0, 2));

        NpcValueSource distance = source(NpcValueSourceType.DISTANCE_TO_TARGET);
        NpcValueSource selfHpPercent = source(NpcValueSourceType.SELF_HP_PERCENT);
        NpcValueSource airborne = flag(AIRBORNE);

        // On the ground: bolts at range, cleave up close, and a periodic scorching beam.
        NpcCombatRule ranged = rule(
                trigger(NpcCombatTriggerType.INTERVAL, 2.0F, 0.0F),
                List.of(condition(distance, NpcCombatComparator.GREATER, 5.0F), grounded()),
                List.of(useAttack("ember_bolt", NpcTargetSelector.PRIMARY)),
                0.5F, false, -210, -90);
        NpcCombatRule melee = rule(
                trigger(NpcCombatTriggerType.INTERVAL, 1.2F, 0.0F),
                List.of(condition(distance, NpcCombatComparator.LESS_EQUAL, 5.0F), grounded()),
                List.of(useAttack("cleave", NpcTargetSelector.PRIMARY)),
                0.4F, false, -70, -90);
        NpcCombatRule scorch = rule(
                trigger(NpcCombatTriggerType.INTERVAL, 6.0F, 0.0F),
                List.of(grounded()),
                List.of(sound("minecraft:item.firecharge.use"), useAttack("scorch_beam", NpcTargetSelector.PRIMARY)),
                0.0F, false, 70, -90);

        // At 60% HP, take flight: flag the aerial phase, switch to fly navigation, climb directly above the
        // target, arm a landing timer, and relabel the bar.
        NpcCombatRule takeFlight = rule(
                trigger(NpcCombatTriggerType.ON_HP_CROSSES, 0.0F, 60.0F),
                List.of(),
                List.of(
                        action(NpcCombatActionType.SET_FLAG, NpcTargetSelector.SELF, NpcCombatPlacement.atSelf(),
                                NpcValueSource.constant(1.0F), 0, 0, AIRBORNE),
                        sound("minecraft:entity.ender_dragon.flap"),
                        action(NpcCombatActionType.SWITCH_NAV_MODE, NpcTargetSelector.SELF,
                                NpcCombatPlacement.atSelf(), NpcValueSource.constant(0.0F), FLIGHT_TICKS, 0, "fly"),
                        moveAbove(),
                        action(NpcCombatActionType.START_TIMER, NpcTargetSelector.SELF, NpcCombatPlacement.atSelf(),
                                NpcValueSource.constant(0.0F), FLIGHT_TICKS, 0, "land"),
                        action(NpcCombatActionType.BOSS_BAR, NpcTargetSelector.SELF, NpcCombatPlacement.atSelf(),
                                NpcValueSource.constant(0.0F), 0, 0, "Ember Colossus takes flight!")),
                0.0F, true, -210, 0);

        // While airborne, hover over the target and rain fire on it.
        NpcCombatRule aerial = rule(
                trigger(NpcCombatTriggerType.INTERVAL, 2.0F, 0.0F),
                List.of(condition(airborne, NpcCombatComparator.GREATER_EQUAL, 1.0F)),
                List.of(moveAbove(), useAttack("star_fall", NpcTargetSelector.PRIMARY)),
                0.0F, false, -70, 0);

        // When the landing timer elapses, drop the flag, return to ground navigation, and restore the bar.
        NpcCombatRule land = rule(
                trigger(NpcCombatTriggerType.ON_TIMER_ELAPSED, 0.0F, 0.0F, "land"),
                List.of(),
                List.of(
                        action(NpcCombatActionType.CLEAR_FLAG, NpcTargetSelector.SELF, NpcCombatPlacement.atSelf(),
                                NpcValueSource.constant(0.0F), 0, 0, AIRBORNE),
                        action(NpcCombatActionType.SWITCH_NAV_MODE, NpcTargetSelector.SELF,
                                NpcCombatPlacement.atSelf(), NpcValueSource.constant(0.0F), 1, 0, "ground"),
                        action(NpcCombatActionType.BOSS_BAR, NpcTargetSelector.SELF, NpcCombatPlacement.atSelf(),
                                NpcValueSource.constant(0.0F), 0, 0, "Ember Colossus")),
                0.0F, false, 70, 0);

        // At 40% HP, enrage for ten seconds and call in three zombies.
        NpcCombatRule enrage = rule(
                trigger(NpcCombatTriggerType.ON_HP_CROSSES, 0.0F, 40.0F),
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
                0.0F, true, -210, 90);

        // Mend when badly hurt, at most once every eight seconds.
        NpcCombatRule mend = rule(
                trigger(NpcCombatTriggerType.INTERVAL, 8.0F, 0.0F),
                List.of(condition(selfHpPercent, NpcCombatComparator.LESS, 30.0F)),
                List.of(action(NpcCombatActionType.HEAL, NpcTargetSelector.SELF, NpcCombatPlacement.atSelf(),
                        NpcValueSource.constant(30.0F), 0, 0, "")),
                0.0F, false, -70, 90);

        // When three or more hostiles crowd it, telegraph a ring, wait, then erupt around itself.
        NpcCombatRule nova = rule(
                trigger(NpcCombatTriggerType.ON_SURROUNDED, 0.0F, 3.0F),
                List.of(),
                List.of(
                        action(NpcCombatActionType.TELEGRAPH, NpcTargetSelector.SELF,
                                selfArea(6.0F), NpcValueSource.constant(0.0F), 20, 0, "minecraft:flame"),
                        sound("minecraft:entity.blaze.shoot"),
                        action(NpcCombatActionType.WAIT, NpcTargetSelector.SELF, NpcCombatPlacement.atSelf(),
                                NpcValueSource.constant(0.0F), 20, 0, ""),
                        action(NpcCombatActionType.EXPLOSION, NpcTargetSelector.ALL_IN_RANGE,
                                selfArea(6.0F), NpcValueSource.constant(12.0F), 0, 0, "")),
                5.0F, false, 70, 90);

        NpcAdvancedCombat advanced = NpcAdvancedCombat.defaults()
                .withEnabled(true)
                .withEngagementRadius(28.0F)
                .withAttackLibrary(List.of(cleave, ember, flameRing, scorchBeam, starFall))
                .withRules(List.of(ranged, melee, scorch, takeFlight, aerial, land, enrage, mend, nova));
        boss.setAdvancedCombat(advanced);
    }

    private static NpcCombatCondition grounded() {
        return condition(flag(AIRBORNE), NpcCombatComparator.LESS, 1.0F);
    }

    private static NpcCombatPlacement selfArea(float radius) {
        return new NpcCombatPlacement(NpcPlacementCentre.SELF, NpcPlacementShape.AT_CENTRE, 0.0F, radius, 1, 0, 1);
    }

    private static NpcCombatPlacement aboveTarget() {
        return new NpcCombatPlacement(NpcPlacementCentre.TARGET, NpcPlacementShape.AT_CENTRE, 0.0F, 0.0F, 1, 0, 2);
    }

    // A sound played at the boss's own position — great layered under an attack for weight.
    private static NpcCombatAction sound(String id) {
        return action(NpcCombatActionType.SOUND, NpcTargetSelector.SELF, NpcCombatPlacement.atSelf(),
                NpcValueSource.constant(0.0F), 0, 0, id);
    }

    private static NpcCombatAction moveAbove() {
        return action(NpcCombatActionType.MOVE_TO, NpcTargetSelector.SELF, aboveTarget(),
                NpcValueSource.constant(0.0F), 0, 0, "");
    }

    private static NpcValueSource source(NpcValueSourceType type) {
        return new NpcValueSource(type, 0.0F, 0.0F, 0, "");
    }

    private static NpcValueSource flag(String key) {
        return new NpcValueSource(NpcValueSourceType.FLAG_VALUE, 0.0F, 0.0F, 0, key);
    }

    private static NpcCombatCondition condition(NpcValueSource left, NpcCombatComparator comparator, float right) {
        return new NpcCombatCondition(left, comparator, NpcValueSource.constant(right), false);
    }

    private static NpcCombatTrigger trigger(NpcCombatTriggerType type, float seconds, float threshold) {
        return new NpcCombatTrigger(type, seconds, threshold, "");
    }

    private static NpcCombatTrigger trigger(NpcCombatTriggerType type, float seconds, float threshold, String key) {
        return new NpcCombatTrigger(type, seconds, threshold, key);
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
