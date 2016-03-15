package info.hearthsim.brazier;

import info.hearthsim.brazier.utils.BrazierTest;
import info.hearthsim.brazier.utils.TestAgent;
import org.junit.Test;

import static info.hearthsim.brazier.utils.TestCards.*;

public final class CopyMinionTest extends BrazierTest {
    @Test
    public void testFacelessMinionKeptAliveByHpAura() {
        agent.setMana("p1", 10);

        agent.playMinionCard("p1", WISP, 0);
        agent.playMinionCard("p1", STORMWIND_CHAMPION, 1);
        agent.playNonMinionCard("p1", MOONFIRE, "p1:0");


        agent.expectBoard("p1",
            expectedMinion(WISP, 2, 1),
            expectedMinion(STORMWIND_CHAMPION, 6, 6));
        agent.expectBoard("p2");

        agent.setMana("p2", 10);
        agent.playMinionCard("p2", FACELESS_MANIPULATOR, 0, "p1:0");

        agent.expectBoard("p1",
            expectedMinion(WISP, 2, 1),
            expectedMinion(STORMWIND_CHAMPION, 6, 6));
        agent.expectBoard("p2");
    }

    @Test
    public void testFacelessMinionKeptAliveByHpAuraThenMirrorEntity() {
        agent.setMana("p1", 10);

        agent.playMinionCard("p1", WISP, 0);
        agent.playMinionCard("p1", STORMWIND_CHAMPION, 1);
        agent.playNonMinionCard("p1", MOONFIRE, "p1:0");
        agent.playCard("p1", MIRROR_ENTITY);

        agent.expectBoard("p1",
            expectedMinion(WISP, 2, 1),
            expectedMinion(STORMWIND_CHAMPION, 6, 6));
        agent.expectBoard("p2");

        agent.setCurrentPlayer("p2");
        agent.setMana("p2", 10);
        agent.playMinionCard("p2", FACELESS_MANIPULATOR, 0, "p1:0");

        agent.expectBoard("p1",
            expectedMinion(WISP, 2, 1),
            expectedMinion(STORMWIND_CHAMPION, 6, 6),
            expectedMinion(WISP, 2, 1));
        agent.expectBoard("p2");
    }

    @Test
    public void testFacelessDamageMinionWithHpAura() {
        agent.setMana("p1", 10);

        agent.playMinionCard("p1", EMPEROR_COBRA, 0);
        agent.playMinionCard("p1", STORMWIND_CHAMPION, 1);
        agent.playNonMinionCard("p1", MOONFIRE, "p1:0");


        agent.expectBoard("p1",
            expectedMinion(EMPEROR_COBRA, 3, 3),
            expectedMinion(STORMWIND_CHAMPION, 6, 6));
        agent.expectBoard("p2");

        agent.setMana("p2", 10);
        agent.playMinionCard("p2", FACELESS_MANIPULATOR, 0, "p1:0");

        agent.expectBoard("p1",
            expectedMinion(EMPEROR_COBRA, 3, 3),
            expectedMinion(STORMWIND_CHAMPION, 6, 6));
        agent.expectBoard("p2",
            expectedMinion(EMPEROR_COBRA, 2, 2));
    }

    @Test
    public void testBasicFaceless() {
        agent.setMana("p1", 10);
        agent.playMinionCard("p1", YETI, 0);

        agent.expectBoard("p1",
            expectedMinion(YETI, 4, 5));
        agent.expectBoard("p2");

        agent.setMana("p2", 10);
        agent.playMinionCard("p2", FACELESS_MANIPULATOR, 0, "p1:0");

        agent.expectBoard("p1",
            expectedMinion(YETI, 4, 5));
        agent.expectBoard("p2",
            expectedMinion(YETI, 4, 5));
    }

    private static void setupAuraProviderCopy(TestAgent script) {
        script.setMana("p1", 10);
        script.playMinionCard("p1", BLUEGILL_WARRIOR, 0);
        script.playMinionCard("p1", STORMWIND_CHAMPION, 1);

        script.setMana("p2", 10);
        script.playMinionCard("p2", YETI, 0);

        script.expectBoard("p1",
            expectedMinion(BLUEGILL_WARRIOR, 3, 2),
            expectedMinion(STORMWIND_CHAMPION, 6, 6));
        script.expectBoard("p2",
            expectedMinion(YETI, 4, 5));

        script.setMana("p2", 10);
        script.playMinionCard("p2", FACELESS_MANIPULATOR, 1, "p1:1");

        script.expectBoard("p1",
            expectedMinion(BLUEGILL_WARRIOR, 3, 2),
            expectedMinion(STORMWIND_CHAMPION, 6, 6));
        script.expectBoard("p2",
            expectedMinion(YETI, 5, 6),
            expectedMinion(STORMWIND_CHAMPION, 6, 6));
    }

    @Test
    public void testCopyAuraProvider() {
        setupAuraProviderCopy(agent);
    }

    @Test
    public void testCopyAuraProviderKillOriginalProvider() {
        setupAuraProviderCopy(agent);

        agent.setMana("p2", 10);
        agent.playNonMinionCard("p2", PYROBLAST, "p1:1");

        agent.expectBoard("p1",
            expectedMinion(BLUEGILL_WARRIOR, 2, 1));
        agent.expectBoard("p2",
            expectedMinion(YETI, 5, 6),
            expectedMinion(STORMWIND_CHAMPION, 6, 6));
    }

    @Test
    public void testCopyAuraProviderKillNewProvider() {
        setupAuraProviderCopy(agent);

        agent.setMana("p2", 10);
        agent.playNonMinionCard("p2", PYROBLAST, "p2:1");

        agent.expectBoard("p1",
            expectedMinion(BLUEGILL_WARRIOR, 3, 2),
            expectedMinion(STORMWIND_CHAMPION, 6, 6));
        agent.expectBoard("p2",
            expectedMinion(YETI, 4, 5));
    }

    @Test
    public void testAuraTargetCopy() {
        agent.setMana("p1", 10);
        agent.playMinionCard("p1", BLUEGILL_WARRIOR, 0);
        agent.playMinionCard("p1", STORMWIND_CHAMPION, 1);

        agent.setMana("p2", 10);
        agent.playMinionCard("p2", YETI, 0);

        agent.expectBoard("p1",
            expectedMinion(BLUEGILL_WARRIOR, 3, 2),
            expectedMinion(STORMWIND_CHAMPION, 6, 6));
        agent.expectBoard("p2",
            expectedMinion(YETI, 4, 5));

        agent.setMana("p2", 10);
        agent.playMinionCard("p2", FACELESS_MANIPULATOR, 1, "p1:0");

        agent.expectBoard("p1",
            expectedMinion(BLUEGILL_WARRIOR, 3, 2),
            expectedMinion(STORMWIND_CHAMPION, 6, 6));
        agent.expectBoard("p2",
            expectedMinion(YETI, 4, 5),
            expectedMinion(BLUEGILL_WARRIOR, 2, 1));
    }
}
