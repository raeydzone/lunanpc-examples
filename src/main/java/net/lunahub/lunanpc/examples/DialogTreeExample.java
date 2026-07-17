package net.lunahub.lunanpc.examples;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.lunahub.luna_npc.api.LunaNpcApi;
import net.lunahub.luna_npc.api.NpcDefinition;
import net.lunahub.luna_npc.api.NpcDialog;
import net.lunahub.luna_npc.api.NpcDialogAction;
import net.lunahub.luna_npc.api.NpcDialogActionType;
import net.lunahub.luna_npc.api.NpcDialogChoice;
import net.lunahub.luna_npc.api.NpcDialogCondition;
import net.lunahub.luna_npc.api.NpcDialogConditionType;
import net.lunahub.luna_npc.api.NpcDialogNode;
import net.lunahub.luna_npc.api.NpcJobSettings;
import net.lunahub.luna_npc.api.NpcObjectiveType;
import net.lunahub.luna_npc.api.NpcQuest;
import net.lunahub.luna_npc.api.NpcQuestCompletion;
import net.lunahub.luna_npc.api.NpcQuestObjective;
import net.lunahub.luna_npc.api.NpcRegistry;
import net.lunahub.luna_npc.api.NpcTradeStack;
import net.lunahub.luna_npc.api.NpcZone;
import net.lunahub.luna_npc.api.ZoneRegistry;

// A "Wandering Storyteller" whose whole life is one big dialog tree: a 100-node conversation with
// branch points, condition-gated answers, mid-story events (flags, items, XP, a quest, a custom event,
// particles) and three different endings — a stress test for both the graph editor and the runtime.
// The spine is generated in a loop; the interesting nodes are patched in by index afterwards.
public final class DialogTreeExample {

    private static final NpcQuest BLANK_QUEST = new NpcQuest("", "", "", "", List.of(),
            NpcQuestCompletion.TURN_IN, 0, 0, 0, List.of(), false, List.of());
    // A fixed id so the ASSIGN_QUEST action and the later QUEST_COMPLETED gate reference the same quest.
    private static final String DEBT_QUEST_ID = "example_debt_quest";
    private static final NpcQuest DEBT_QUEST = new NpcQuest(DEBT_QUEST_ID, "Settle the Debt", "", "",
            List.of(new NpcQuestObjective(NpcObjectiveType.KILL_MOB, "minecraft:zombie", 3, "")),
            NpcQuestCompletion.AUTO, 0, 20, 20, List.of(new NpcTradeStack("minecraft:emerald", 2)),
            false, List.of());
    private static final int COUNT = 100;

    private DialogTreeExample() {
    }

    public static void build(MinecraftServer server) {
        ZoneRegistry zones = LunaNpcApi.zones(server);
        NpcZone plaza = zones.getOrCreateBox("Storyteller Plaza", "minecraft:overworld",
                -8, 63, -8, 8, 78, 8);

        NpcRegistry npcs = LunaNpcApi.npcs(server);
        NpcDefinition teller = npcs.getOrCreate("Storyteller");
        teller.setHealth(30);
        teller.setNameTagShown(true);
        teller.setJobs(NpcJobSettings.dialog(buildDialog()));

        teller.spawnInZone(plaza.id(), 1, 1);
    }

    private static NpcDialog buildDialog() {
        List<NpcDialogNode> nodes = new ArrayList<>();
        for (int i = 0; i < COUNT; i++) {
            nodes.add(fillerNode(i));
        }
        // Branch and event nodes overwrite their filler by index.
        nodes.set(0, node(0, "A hooded figure looks up. \"Every road has a story. Hear mine?\"", List.of(
                choice("Tell me everything.", "n1"),
                choice("Who are you?", "n1"),
                end("Not now.")), List.of()));
        nodes.set(5, node(5, "\"You listen well. I'll remember that.\"",
                List.of(choice("Go on.", "n6")), List.of(setFlag("trusted"))));
        nodes.set(15, node(15, "\"Take this, you'll need your strength.\"",
                List.of(choice("Thank you.", "n16")),
                List.of(giveItems("minecraft:bread", 3))));
        nodes.set(25, node(25, "\"So ends the first chapter of the road.\"",
                List.of(choice("And then?", "n26")), List.of(customEvent("chapter_one_done"))));
        nodes.set(30, node(30, "\"Wisdom earned is wisdom kept.\"",
                List.of(choice("I feel it.", "n31")), List.of(giveXp(30))));
        nodes.set(40, node(40, "\"There is a debt on this land: slay three of the restless dead.\"",
                List.of(choice("I accept.", "n41")), List.of(assignQuest())));
        nodes.set(42, node(42, "\"Well? Is the debt settled?\"", List.of(
                choiceIf("It is done.", "n43", questCompleted()),
                choice("Not yet — I'll return.", "n42")), List.of()));
        nodes.set(45, node(45, "\"Only the trusted may hear what comes next.\"", List.of(
                choiceIf("I have earned it.", "n46", flagSet("trusted")),
                choice("Tell me anyway.", "n70")), List.of()));
        nodes.set(60, node(60, "\"The road blooms for those who walk it.\"",
                List.of(choice("Beautiful.", "n61")), List.of(particle("minecraft:happy_villager"))));
        nodes.set(80, node(80, "\"The tale is nearly told. How does yours end?\"", List.of(
                choice("Accept your reward.", "n97"),
                choice("Walk away in peace.", "n98"),
                choice("Betray the storyteller.", "n99")), List.of()));
        nodes.set(97, node(97, "\"Then take what a friend deserves.\"",
                List.of(end("Farewell.")),
                List.of(giveItems("minecraft:diamond", 1), giveXp(100))));
        nodes.set(98, node(98, "\"Peace, then. The road remembers you.\"",
                List.of(end("Goodbye.")), List.of()));
        nodes.set(99, node(99, "\"So that is who you are.\" The figure's eyes harden.",
                List.of(end("...")), List.of(setRelationHostile())));
        return new NpcDialog("example_storyteller", "Storyteller", "n0", nodes, false, true);
    }

    // A plain spine node: one line, one Continue to the next, laid out on a 10-wide grid for the editor.
    private static NpcDialogNode fillerNode(int index) {
        List<NpcDialogChoice> choices = index < COUNT - 1
                ? List.of(choice("Continue.", "n" + (index + 1)))
                : List.of(end("The end."));
        return node(index, "\"...the road went on, past mile " + index + "...\"", choices, List.of());
    }

    private static NpcDialogNode node(int index, String line, List<NpcDialogChoice> choices,
                                      List<NpcDialogAction> actions) {
        return new NpcDialogNode("n" + index, line, choices, actions, "", "",
                (index % 10) * 180, (index / 10) * 130);
    }

    private static NpcDialogChoice choice(String text, String target) {
        return new NpcDialogChoice(text, target, List.of(), true);
    }

    private static NpcDialogChoice choiceIf(String text, String target, NpcDialogCondition condition) {
        return new NpcDialogChoice(text, target, List.of(condition), true);
    }

    private static NpcDialogChoice end(String text) {
        return new NpcDialogChoice(text, "", List.of(), true);
    }

    private static NpcDialogCondition flagSet(String key) {
        return new NpcDialogCondition(NpcDialogConditionType.FLAG_SET, key, 0, false);
    }

    private static NpcDialogCondition questCompleted() {
        return new NpcDialogCondition(NpcDialogConditionType.QUEST_COMPLETED, DEBT_QUEST_ID, 0, false);
    }

    private static NpcDialogAction setFlag(String key) {
        return action(NpcDialogActionType.SET_FLAG, key, 0);
    }

    private static NpcDialogAction customEvent(String tag) {
        return action(NpcDialogActionType.CUSTOM_EVENT, tag, 0);
    }

    private static NpcDialogAction giveXp(int amount) {
        return action(NpcDialogActionType.GIVE_XP, "", amount);
    }

    private static NpcDialogAction particle(String id) {
        return action(NpcDialogActionType.PARTICLE, id, 0);
    }

    private static NpcDialogAction setRelationHostile() {
        return new NpcDialogAction(NpcDialogActionType.SET_RELATION, "", 0, List.of(), BLANK_QUEST,
                BlockPos.ZERO, "", true);
    }

    private static NpcDialogAction giveItems(String itemId, int count) {
        return new NpcDialogAction(NpcDialogActionType.GIVE_ITEMS, "", 0,
                List.of(new NpcTradeStack(itemId, count)), BLANK_QUEST, BlockPos.ZERO, "", false);
    }

    private static NpcDialogAction assignQuest() {
        return new NpcDialogAction(NpcDialogActionType.ASSIGN_QUEST, "", 0, List.of(), DEBT_QUEST,
                BlockPos.ZERO, "", false);
    }

    private static NpcDialogAction action(NpcDialogActionType type, String stringArg, int intArg) {
        return new NpcDialogAction(type, stringArg, intArg, List.of(), BLANK_QUEST, BlockPos.ZERO, "", false);
    }
}
