package info.hearthsim.brazier;

import org.junit.Test;

public final class ReturnMinionTest {
    private static void expectManaCost(PlayScript script, String playerName, int... manaCosts) {
        ManaCostManipulationTest.expectManaCost(script, playerName, manaCosts);
    }

    @Test
    public void testFreezingTrapAttackMinion() {
        PlayScript.testScript((script) -> {
            script.setMana("p1", 10);
            script.setMana("p2", 10);

            script.playCard("p1", TestCards.FREEZING_TRAP);
            script.playMinionCard("p1", TestCards.YETI, 0);

            script.setCurrentPlayer("p2");

            script.playMinionCard("p2", TestCards.BLUEGILL_WARRIOR, 0);

            script.expectBoard("p1", TestCards.expectedMinion(TestCards.YETI, 4, 5));
            script.expectBoard("p2", TestCards.expectedMinion(TestCards.BLUEGILL_WARRIOR, 2, 1));
            script.expectHand("p1");
            script.expectHand("p2");

            script.attack("p2:0", "p1:0");

            script.expectHand("p1");
            script.expectHand("p2", TestCards.BLUEGILL_WARRIOR);
            expectManaCost(script, "p2", 4);

            script.expectBoard("p1", TestCards.expectedMinion(TestCards.YETI, 4, 5));
            script.expectBoard("p2");
        });
    }

    @Test
    public void testFreezingTrapAttackHero() {
        PlayScript.testScript((script) -> {
            script.setMana("p1", 10);
            script.setMana("p2", 10);

            script.playCard("p1", TestCards.FREEZING_TRAP);
            script.playMinionCard("p1", TestCards.YETI, 0);

            script.setCurrentPlayer("p2");

            script.playMinionCard("p2", TestCards.BLUEGILL_WARRIOR, 0);

            script.expectBoard("p1", TestCards.expectedMinion(TestCards.YETI, 4, 5));
            script.expectBoard("p2", TestCards.expectedMinion(TestCards.BLUEGILL_WARRIOR, 2, 1));
            script.expectHand("p1");
            script.expectHand("p2");

            script.attack("p2:0", "p1:hero");

            script.expectHand("p1");
            script.expectHand("p2", TestCards.BLUEGILL_WARRIOR);
            expectManaCost(script, "p2", 4);

            script.expectBoard("p1", TestCards.expectedMinion(TestCards.YETI, 4, 5));
            script.expectBoard("p2");
        });
    }

    @Test
    public void testShadowStepDeathRattle() {
        PlayScript.testScript((script) -> {
            script.setMana("p1", 10);
            script.setMana("p2", 10);

            script.playMinionCard("p1", TestCards.SLUDGE_BELCHER, 0);

            script.expectBoard("p1", TestCards.expectedMinion(TestCards.SLUDGE_BELCHER, 3, 5));
            script.expectBoard("p2");
            script.expectHand("p1");
            script.expectHand("p2");

            script.playNonMinionCard("p1", TestCards.SHADOW_STEP, "p1:0");

            script.expectBoard("p1");
            script.expectBoard("p2");
            script.expectHand("p1", TestCards.SLUDGE_BELCHER);
            expectManaCost(script, "p1", 3);
            script.expectHand("p2");
        });
    }
}
