package net.lunacore.lunanpc.examples;

import java.util.List;
import net.minecraft.server.MinecraftServer;
import net.lunacore.luna_npc.api.LunaNpcApi;
import net.lunacore.luna_npc.api.NpcAccuracyAnchor;
import net.lunacore.luna_npc.api.NpcAdvancedCombat;
import net.lunacore.luna_npc.api.NpcAirSpawn;
import net.lunacore.luna_npc.api.NpcAttackEffect;
import net.lunacore.luna_npc.api.NpcCombatAction;
import net.lunacore.luna_npc.api.NpcCombatActionType;
import net.lunacore.luna_npc.api.NpcCombatAttack;
import net.lunacore.luna_npc.api.NpcCombatAttackKind;
import net.lunacore.luna_npc.api.NpcCombatComparator;
import net.lunacore.luna_npc.api.NpcCombatCondition;
import net.lunacore.luna_npc.api.NpcCombatPlacement;
import net.lunacore.luna_npc.api.NpcCombatRule;
import net.lunacore.luna_npc.api.NpcCombatTrigger;
import net.lunacore.luna_npc.api.NpcCombatTriggerType;
import net.lunacore.luna_npc.api.NpcDefinition;
import net.lunacore.luna_npc.api.NpcReactionSettings;
import net.lunacore.luna_npc.api.NpcRegistry;
import net.lunacore.luna_npc.api.NpcTargetSelector;
import net.lunacore.luna_npc.api.NpcValueSource;
import net.lunacore.luna_npc.api.NpcValueSourceType;

// A flying nether blaze that shoots reddish fireballs on a timed rhythm, built with the node-based
// advanced-combat system. The rhythm loops: a 2s-cadence volley for 5 seconds, a 5-second pause, then
// a 0.5s-cadence burst for 1.5 seconds. Two boolean flags ("resting"/"bursting") plus timers drive the
// phases; both clear = steady volley. Advanced combat suppresses default movement, so it holds flight
// with an explicit SWITCH_NAV_MODE at combat start.
public final class CrimsonBlazeExample {

    private static final String RESTING = "resting";
    private static final String BURSTING = "bursting";
    private static final int FLIGHT_HEIGHT = 10;

    private CrimsonBlazeExample() {
    }

    public static void build(MinecraftServer server) {
        NpcRegistry npcs = LunaNpcApi.npcs(server);
        NpcDefinition blaze = npcs.getOrCreate("CrimsonBlaze");

        blaze.setHealth(10);
        blaze.setAllianceId("creatures");
        blaze.setModelSettings(blaze.modelSettings().withModel("blaze", "blaze"));

        blaze.setHurtSoundId("minecraft:entity.blaze.hurt");
        blaze.setDieSoundId("minecraft:entity.blaze.death");
        blaze.setAmbientSoundId("minecraft:entity.blaze.ambient");

        blaze.setMovement(blaze.movement().withMovementType("air").withFlightHeight(FLIGHT_HEIGHT));

        blaze.setTraits(blaze.traits().asCreature()
                .withFireImmune(true)
                .withReaction(NpcReactionSettings.defaults().withMode(NpcReactionSettings.AGGRESSIVE)));

        // 40-block aggro, prefers 15 blocks. The shot does not home, so its distance accuracy curve
        // applies: ~80% within 10 blocks, falling to ~40% past 25.
        blaze.setCombat(blaze.combat()
                .withAggroRange(40.0F)
                .withDistancePreference(15.0F)
                .withProjectile(blaze.combat().projectile()
                        .withItem("luna_npc:red_fire")
                        .withHoming(false)
                        .withDynamicAccuracy(true)
                        .withAccuracyAnchors(List.of(
                                new NpcAccuracyAnchor(0, 80),
                                new NpcAccuracyAnchor(10, 80),
                                new NpcAccuracyAnchor(25, 40)))));

        blaze.setSpawn(blaze.spawn()
                .withBiomeChance("minecraft:nether_wastes", 10)
                .withBiomeChance("minecraft:crimson_forest", 10)
                .withBiomeChance("minecraft:warped_forest", 10)
                .withBiomeChance("minecraft:soul_sand_valley", 10)
                .withBiomeChance("minecraft:basalt_deltas", 10)
                .withAirSpawn(NpcAirSpawn.defaults().withWorldRange(128, 256).withAboveGround(34, 64)));

        blaze.setDropSettings(blaze.dropSettings()
                .withXp(10, 10)
                .withDrop(0, "minecraft:blaze_powder", 50.0F)
                .withDrop(1, "minecraft:blaze_rod", 8.0F));

        NpcCombatAttack bolt = new NpcCombatAttack("red_bolt", NpcCombatAttackKind.PROJECTILE, 5.0F, 15.0F,
                8.0F, "luna_npc:red_fire", false, false, 1, 0.0F,
                List.of(new NpcAttackEffect("flame", 0.0F, 1)), NpcCombatPlacement.atSelf());

        // Rule graph positions are spread out so the editor shows six distinct nodes, not one stack.
        List<NpcCombatRule> rules = List.of(
                // Combat starts: hold flight, clear the flags, open the 5s steady window (100 ticks).
                rule(trigger(NpcCombatTriggerType.ON_COMBAT_START), List.of(),
                        List.of(holdFlight(), clearFlag(RESTING), clearFlag(BURSTING), startTimer("toRest", 100)),
                        -240, -120),
                // Steady volley: a shot every 2s while neither resting nor bursting.
                rule(interval(2.0F), List.of(flagClear(RESTING), flagClear(BURSTING)),
                        List.of(shootSound(), shoot()), -240, 40),
                // 5s up -> rest for 5s.
                rule(onTimer("toRest"), List.of(),
                        List.of(setFlag(RESTING), startTimer("toBurst", 100)), 0, -120),
                // rest up -> burst for 1.5s (30 ticks).
                rule(onTimer("toBurst"), List.of(),
                        List.of(clearFlag(RESTING), setFlag(BURSTING), startTimer("toSteady", 30)), 0, 40),
                // Burst: a shot every 0.5s while bursting (~3 shots).
                rule(interval(0.5F), List.of(flagSet(BURSTING)),
                        List.of(shootSound(), shoot()), 240, 40),
                // burst up -> back to the steady window.
                rule(onTimer("toSteady"), List.of(),
                        List.of(clearFlag(BURSTING), startTimer("toRest", 100)), 240, -120));

        blaze.setAdvancedCombat(NpcAdvancedCombat.defaults()
                .withEnabled(true)
                .withEngagementRadius(40.0F)
                .withAttackLibrary(List.of(bolt))
                .withRules(rules));
    }

    // Holds air navigation for the whole fight (5 min ceiling) so advanced combat keeps it flying. The
    // fixed flag holds the flight height even up close, instead of giving way down to the target.
    private static NpcCombatAction holdFlight() {
        return new NpcCombatAction(NpcCombatActionType.SWITCH_NAV_MODE, NpcTargetSelector.SELF,
                NpcCombatPlacement.atSelf(), NpcValueSource.constant(FLIGHT_HEIGHT), 6000, 0, "fly",
                List.of(), true);
    }

    private static NpcCombatAction shoot() {
        return new NpcCombatAction(NpcCombatActionType.USE_ATTACK, NpcTargetSelector.PRIMARY,
                NpcCombatPlacement.atTarget(), NpcValueSource.constant(0.0F), 0, 0, "red_bolt", List.of(), false);
    }

    private static NpcCombatAction shootSound() {
        return new NpcCombatAction(NpcCombatActionType.SOUND, NpcTargetSelector.SELF,
                NpcCombatPlacement.atSelf(), NpcValueSource.constant(0.0F), 0, 0,
                "minecraft:entity.blaze.shoot", List.of(), false);
    }

    private static NpcCombatAction setFlag(String key) {
        return flagAction(NpcCombatActionType.SET_FLAG, key, 1.0F);
    }

    private static NpcCombatAction clearFlag(String key) {
        return flagAction(NpcCombatActionType.CLEAR_FLAG, key, 0.0F);
    }

    private static NpcCombatAction flagAction(NpcCombatActionType type, String key, float value) {
        return new NpcCombatAction(type, NpcTargetSelector.SELF, NpcCombatPlacement.atSelf(),
                NpcValueSource.constant(value), 0, 0, key, List.of(), false);
    }

    // durationTicks is the timer length in ticks (20 per second).
    private static NpcCombatAction startTimer(String key, int ticks) {
        return new NpcCombatAction(NpcCombatActionType.START_TIMER, NpcTargetSelector.SELF,
                NpcCombatPlacement.atSelf(), NpcValueSource.constant(0.0F), ticks, 0, key, List.of(), false);
    }

    private static NpcCombatCondition flagSet(String key) {
        return flagCondition(key, NpcCombatComparator.GREATER_EQUAL);
    }

    private static NpcCombatCondition flagClear(String key) {
        return flagCondition(key, NpcCombatComparator.LESS);
    }

    private static NpcCombatCondition flagCondition(String key, NpcCombatComparator cmp) {
        return new NpcCombatCondition(new NpcValueSource(NpcValueSourceType.FLAG_VALUE, 0.0F, 0.0F, 0, key),
                cmp, NpcValueSource.constant(1.0F), false);
    }

    private static NpcCombatTrigger trigger(NpcCombatTriggerType type) {
        return new NpcCombatTrigger(type, 0.0F, 0.0F, "");
    }

    private static NpcCombatTrigger interval(float seconds) {
        return new NpcCombatTrigger(NpcCombatTriggerType.INTERVAL, seconds, 0.0F, "");
    }

    private static NpcCombatTrigger onTimer(String key) {
        return new NpcCombatTrigger(NpcCombatTriggerType.ON_TIMER_ELAPSED, 0.0F, 0.0F, key);
    }

    private static NpcCombatRule rule(NpcCombatTrigger trigger, List<NpcCombatCondition> conditions,
                                      List<NpcCombatAction> actions, int graphX, int graphY) {
        return new NpcCombatRule(trigger, conditions, actions, 0.0F, false, graphX, graphY);
    }
}
