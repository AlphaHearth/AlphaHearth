package info.hearthsim.brazier;

import info.hearthsim.brazier.utils.BrazierTest;
import info.hearthsim.brazier.utils.TestAgent;
import org.junit.Test;

import static info.hearthsim.brazier.utils.TestCards.*;

public final class SecretTest extends BrazierTest {
    @Test
    public void testSnipeJaraxxus() {
        agent.setMana("p1", 10);
        agent.playCard("p1", SNIPE);

        agent.setHeroHp("p1", 30, 0);
        agent.setHeroHp("p2", 20, 0);

        agent.setMana("p2", 10);
        agent.setCurrentPlayer("p2");
        agent.playMinionCard("p2", JARAXXUS, 0);

        agent.expectBoard("p1");
        agent.expectBoard("p2");

        agent.expectHeroHp("p1", 30, 0);
        agent.expectHeroHp("p2", 11, 0);
    }

    @Test
    public void testRepentanceSnipeJaraxxus() {
        agent.setMana("p1", 10);
        agent.playCard("p1", REPENTANCE);
        agent.playCard("p1", SNIPE);

        agent.setHeroHp("p1", 30, 0);
        agent.setHeroHp("p2", 20, 0);

        agent.setMana("p2", 10);
        agent.setCurrentPlayer("p2");
        agent.playMinionCard("p2", JARAXXUS, 0);

        agent.expectHeroDeath("p2");

        agent.expectHeroHp("p1", 30, 0);
        agent.expectHeroHp("p2", -3, 0);
    }

    @Test
    public void testAvengeDetectsPreviousDeathRattle() {
        agent.setMana("p1", 10);
        agent.playMinionCard("p1", HARVEST_GOLEM, 0);
        agent.playMinionCard("p1", WISP, 1);
        agent.playCard("p1", AVENGE);

        agent.setMana("p2", 10);
        agent.setCurrentPlayer("p2");
        agent.playMinionCard("p2", WISP, 0);

        agent.expectBoard("p1",
            expectedMinion(HARVEST_GOLEM, 2, 3),
            expectedMinion(WISP, 1, 1));
        agent.expectBoard("p2",
            expectedMinion(WISP, 1, 1));

        agent.playCard("p2", FLAMESTRIKE);

        agent.expectBoard("p1",
            expectedMinion(DAMAGED_GOLEM, 5, 3));
        agent.expectBoard("p2",
            expectedMinion(WISP, 1, 1));

        agent.expectSecret("p1");
        agent.expectSecret("p2");
    }

    @Test
    public void testAvengeDoesNotDetectDeathRattleSummon() {
        agent.setMana("p1", 10);
        agent.playMinionCard("p1", WISP, 0);
        agent.playMinionCard("p1", HARVEST_GOLEM, 0);
        agent.playCard("p1", AVENGE);

        agent.setMana("p2", 10);
        agent.setCurrentPlayer("p2");
        agent.playMinionCard("p2", WISP, 0);

        agent.expectBoard("p1",
            expectedMinion(HARVEST_GOLEM, 2, 3),
            expectedMinion(WISP, 1, 1));
        agent.expectBoard("p2",
            expectedMinion(WISP, 1, 1));

        agent.playCard("p2", FLAMESTRIKE);

        agent.expectBoard("p1",
            expectedMinion(DAMAGED_GOLEM, 2, 1));
        agent.expectBoard("p2",
            expectedMinion(WISP, 1, 1));

        agent.expectSecret("p1", AVENGE);
        agent.expectSecret("p2");
    }

    @Test
    public void testAvengeDoesNotTriggerForSingleTarget() {
        agent.setMana("p1", 10);
        agent.playMinionCard("p1", WISP, 0);
        agent.playCard("p1", AVENGE);

        agent.setMana("p2", 10);
        agent.setCurrentPlayer("p2");
        agent.playMinionCard("p2", WISP, 0);

        agent.expectBoard("p1",
            expectedMinion(WISP, 1, 1));
        agent.expectBoard("p2",
            expectedMinion(WISP, 1, 1));

        agent.playCard("p2", FLAMESTRIKE);

        agent.expectBoard("p1");
        agent.expectBoard("p2",
            expectedMinion(WISP, 1, 1));

        agent.expectSecret("p1", AVENGE);
        agent.expectSecret("p2");

        agent.setCurrentPlayer("p1");
        agent.playMinionCard("p1", WISP, 0);
        agent.playMinionCard("p1", WISP, 1);

        agent.expectBoard("p1",
            expectedMinion(WISP, 1, 1),
            expectedMinion(WISP, 1, 1));
        agent.expectBoard("p2",
            expectedMinion(WISP, 1, 1));

        agent.setCurrentPlayer("p2");
        agent.playNonMinionCard("p2", MOONFIRE, "p1:0");

        agent.expectBoard("p1",
            expectedMinion(WISP, 4, 3));
        agent.expectBoard("p2",
            expectedMinion(WISP, 1, 1));

        agent.expectSecret("p1");
        agent.expectSecret("p2");
    }

    @Test
    public void testIceBarrierOnEnemyTurn() {
        agent.setMana("p1", 10);
        agent.playCard("p1", ICE_BARRIER);
        agent.expectSecret("p1", ICE_BARRIER);

        agent.setMana("p2", 10);
        agent.setCurrentPlayer("p2");

        agent.setHeroHp("p1", 30, 0);
        agent.setHeroHp("p2", 30, 0);

        agent.playMinionCard("p2", STONETUSK_BOAR, 0);
        agent.attack("p2:0", "p1:hero");

        agent.expectSecret("p1");

        agent.expectHeroHp("p1", 30, 7);
        agent.expectHeroHp("p2", 30, 0);

        agent.playMinionCard("p2", STONETUSK_BOAR, 1);
        agent.attack("p2:1", "p1:hero");

        agent.expectHeroHp("p1", 30, 6);
        agent.expectHeroHp("p2", 30, 0);
    }

    private static void expectManaCost(TestAgent script, String playerName, int... manaCosts) {
        ManaCostManipulationTest.expectManaCost(script, playerName, manaCosts);
    }

    @Test
    public void testFreezingTrapBeforeExplosive() {
        agent.setMana("p1", 10);
        agent.setMana("p2", 10);

        agent.playCard("p1", FREEZING_TRAP);
        agent.playCard("p1", EXPLOSIVE_TRAP);
        agent.playMinionCard("p1", YETI, 0);

        agent.expectSecret("p1", FREEZING_TRAP, EXPLOSIVE_TRAP);

        agent.setCurrentPlayer("p2");

        agent.playMinionCard("p2", BLUEGILL_WARRIOR, 0);

        agent.expectBoard("p1", expectedMinion(YETI, 4, 5));
        agent.expectBoard("p2", expectedMinion(BLUEGILL_WARRIOR, 2, 1));
        agent.expectHand("p1");
        agent.expectHand("p2");

        agent.setHeroHp("p1", 30, 0);
        agent.setHeroHp("p2", 20, 0);
        agent.attack("p2:0", "p1:hero");

        agent.expectSecret("p1");

        agent.expectHand("p1");
        agent.expectHand("p2", BLUEGILL_WARRIOR);
        expectManaCost(agent, "p2", 4);

        agent.expectBoard("p1", expectedMinion(YETI, 4, 5));
        agent.expectBoard("p2");

        agent.expectHeroHp("p1", 30, 0);
        agent.expectHeroHp("p2", 18, 0);
    }

    @Test
    public void testFreezingTrapAfterExplosive() {
        agent.setMana("p1", 10);
        agent.setMana("p2", 10);

        agent.playCard("p1", EXPLOSIVE_TRAP);
        agent.playCard("p1", FREEZING_TRAP);
        agent.playMinionCard("p1", YETI, 0);

        agent.expectSecret("p1", EXPLOSIVE_TRAP, FREEZING_TRAP);

        agent.setCurrentPlayer("p2");

        agent.playMinionCard("p2", BLUEGILL_WARRIOR, 0);

        agent.expectBoard("p1", expectedMinion(YETI, 4, 5));
        agent.expectBoard("p2", expectedMinion(BLUEGILL_WARRIOR, 2, 1));
        agent.expectHand("p1");
        agent.expectHand("p2");

        agent.setHeroHp("p1", 30, 0);
        agent.setHeroHp("p2", 20, 0);
        agent.attack("p2:0", "p1:hero");

        agent.expectSecret("p1", FREEZING_TRAP);

        agent.expectHand("p1");
        agent.expectHand("p2");

        agent.expectBoard("p1", expectedMinion(YETI, 4, 5));
        agent.expectBoard("p2");

        agent.expectHeroHp("p1", 30, 0);
        agent.expectHeroHp("p2", 18, 0);
    }

    private void testFreezingTrapAfterExplosiveOrderDoesNotChangeWithKezan(int secretRoll) {
        agent.setMana("p1", 10);
        agent.setMana("p2", 10);

        agent.playCard("p1", EXPLOSIVE_TRAP);
        agent.playCard("p1", FREEZING_TRAP);
        agent.playMinionCard("p1", YETI, 0);
        agent.setMana("p1", 10);

        agent.expectSecret("p1", EXPLOSIVE_TRAP, FREEZING_TRAP);

        agent.setCurrentPlayer("p2");

        agent.addRoll(2, secretRoll);
        agent.playMinionCard("p2", KEZAN_MYSTIC, 0);
        agent.playMinionCard("p2", BLUEGILL_WARRIOR, 1);

        agent.setCurrentPlayer("p1");
        agent.playMinionCard("p1", KEZAN_MYSTIC, 0);

        agent.setCurrentPlayer("p2");

        agent.expectBoard("p1",
            expectedMinion(KEZAN_MYSTIC, 4, 3),
            expectedMinion(YETI, 4, 5));
        agent.expectBoard("p2",
            expectedMinion(KEZAN_MYSTIC, 4, 3),
            expectedMinion(BLUEGILL_WARRIOR, 2, 1));
        agent.expectHand("p1");
        agent.expectHand("p2");

        agent.setHeroHp("p1", 30, 0);
        agent.setHeroHp("p2", 20, 0);
        agent.attack("p2:1", "p1:hero");

        agent.expectSecret("p1", FREEZING_TRAP);

        agent.expectHand("p1");
        agent.expectHand("p2");

        agent.expectBoard("p1",
            expectedMinion(KEZAN_MYSTIC, 4, 3),
            expectedMinion(YETI, 4, 5));
        agent.expectBoard("p2",
            expectedMinion(KEZAN_MYSTIC, 4, 1));

        agent.expectHeroHp("p1", 30, 0);
        agent.expectHeroHp("p2", 18, 0);
    }

    @Test
    public void testFreezingTrapAfterExplosiveOrderDoesNotChangeWithKezan() {
        for (int secretRoll : new int[] { 0, 1 }) {
            testFreezingTrapAfterExplosiveOrderDoesNotChangeWithKezan(secretRoll);
            setUp(); // Set up again to reset the TestAgent
        }
    }

    @Test
    public void testKezanStealsANewSecret() {
        agent.setMana("p1", 10);
        agent.setMana("p2", 10);

        agent.playCard("p1", EXPLOSIVE_TRAP);
        agent.playCard("p1", FREEZING_TRAP);
        agent.playCard("p2", EXPLOSIVE_TRAP);

        agent.expectSecret("p1", EXPLOSIVE_TRAP, FREEZING_TRAP);
        agent.expectSecret("p2", EXPLOSIVE_TRAP);

        agent.playMinionCard("p2", KEZAN_MYSTIC, 0);

        agent.expectSecret("p1", EXPLOSIVE_TRAP);
        agent.expectSecret("p2", EXPLOSIVE_TRAP, FREEZING_TRAP);
    }

    @Test
    public void testKezanStealsSecret() {
        agent.setMana("p1", 10);
        agent.setMana("p2", 10);

        agent.playCard("p1", FREEZING_TRAP);

        agent.expectSecret("p1", FREEZING_TRAP);
        agent.expectSecret("p2");

        agent.playMinionCard("p2", KEZAN_MYSTIC, 0);

        agent.expectSecret("p1");
        agent.expectSecret("p2", FREEZING_TRAP);

        agent.setCurrentPlayer("p1");
        agent.playMinionCard("p1", BLUEGILL_WARRIOR, 0);

        agent.expectHand("p1");
        agent.expectHand("p2");
        agent.expectBoard("p1", expectedMinion(BLUEGILL_WARRIOR, 2, 1));
        agent.expectBoard("p2", expectedMinion(KEZAN_MYSTIC, 4, 3));

        agent.attack("p1:0", "p2:hero");

        agent.expectHand("p1", BLUEGILL_WARRIOR);
        agent.expectHand("p2");
        agent.expectBoard("p1");
        agent.expectBoard("p2", expectedMinion(KEZAN_MYSTIC, 4, 3));
    }

    @Test
    public void testKezanDestroysSecret() {
        agent.setMana("p1", 10);
        agent.setMana("p2", 10);

        agent.playCard("p1", FREEZING_TRAP);
        agent.playCard("p2", FREEZING_TRAP);

        agent.expectSecret("p1", FREEZING_TRAP);
        agent.expectSecret("p2", FREEZING_TRAP);

        agent.playMinionCard("p2", KEZAN_MYSTIC, 0);

        agent.playMinionCard("p1", BLUEGILL_WARRIOR, 0);
        agent.playMinionCard("p2", BLUEGILL_WARRIOR, 1);

        agent.expectSecret("p1");
        agent.expectSecret("p2", FREEZING_TRAP);

        agent.expectHand("p1");
        agent.expectHand("p2");
        agent.expectBoard("p1",
            expectedMinion(BLUEGILL_WARRIOR, 2, 1));
        agent.expectBoard("p2",
            expectedMinion(KEZAN_MYSTIC, 4, 3),
            expectedMinion(BLUEGILL_WARRIOR, 2, 1));

        agent.setHeroHp("p1", 30, 0);
        agent.setHeroHp("p2", 20, 0);

        agent.setCurrentPlayer("p1");
        agent.attack("p1:0", "p2:hero");

        agent.expectHand("p1", BLUEGILL_WARRIOR);
        agent.expectHand("p2");
        agent.expectBoard("p1");
        agent.expectBoard("p2",
            expectedMinion(KEZAN_MYSTIC, 4, 3),
            expectedMinion(BLUEGILL_WARRIOR, 2, 1));

        agent.setCurrentPlayer("p2");
        agent.attack("p2:1", "p1:hero");

        agent.expectHand("p1", BLUEGILL_WARRIOR);
        agent.expectHand("p2");
        agent.expectBoard("p1");
        agent.expectBoard("p2",
            expectedMinion(KEZAN_MYSTIC, 4, 3),
            expectedMinion(BLUEGILL_WARRIOR, 2, 1));

        agent.refreshAttacks();
        agent.attack("p2:1", "p1:hero");

        agent.expectHand("p1", BLUEGILL_WARRIOR);
        agent.expectHand("p2");
        agent.expectBoard("p1");
        agent.expectBoard("p2",
            expectedMinion(KEZAN_MYSTIC, 4, 3),
            expectedMinion(BLUEGILL_WARRIOR, 2, 1));

        agent.expectHeroHp("p1", 26, 0);
        agent.expectHeroHp("p2", 20, 0);
    }
}
