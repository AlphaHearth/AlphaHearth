package info.hearthsim.brazier;

import info.hearthsim.brazier.utils.BrazierTest;
import info.hearthsim.brazier.utils.TestAgent;
import info.hearthsim.brazier.utils.TestCards;
import org.junit.Test;

public final class ReturnMinionTest extends BrazierTest {
    private static void expectManaCost(TestAgent script, String playerName, int... manaCosts) {
        ManaCostManipulationTest.expectManaCost(script, playerName, manaCosts);
    }

    @Test
    public void testFreezingTrapAttackMinion() {
        agent.setMana("p1", 10);
        agent.setMana("p2", 10);

        agent.playCard("p1", TestCards.FREEZING_TRAP);
        agent.playMinionCard("p1", TestCards.YETI, 0);

        agent.setCurrentPlayer("p2");

        agent.playMinionCard("p2", TestCards.BLUEGILL_WARRIOR, 0);

        agent.expectBoard("p1", TestCards.expectedMinion(TestCards.YETI, 4, 5));
        agent.expectBoard("p2", TestCards.expectedMinion(TestCards.BLUEGILL_WARRIOR, 2, 1));
        agent.expectHand("p1");
        agent.expectHand("p2");

        agent.attack("p2:0", "p1:0");

        agent.expectHand("p1");
        agent.expectHand("p2", TestCards.BLUEGILL_WARRIOR);
        expectManaCost(agent, "p2", 4);

        agent.expectBoard("p1", TestCards.expectedMinion(TestCards.YETI, 4, 5));
        agent.expectBoard("p2");
    }

    @Test
    public void testFreezingTrapAttackHero() {
        agent.setMana("p1", 10);
        agent.setMana("p2", 10);

        agent.playCard("p1", TestCards.FREEZING_TRAP);
        agent.playMinionCard("p1", TestCards.YETI, 0);

        agent.setCurrentPlayer("p2");

        agent.playMinionCard("p2", TestCards.BLUEGILL_WARRIOR, 0);

        agent.expectBoard("p1", TestCards.expectedMinion(TestCards.YETI, 4, 5));
        agent.expectBoard("p2", TestCards.expectedMinion(TestCards.BLUEGILL_WARRIOR, 2, 1));
        agent.expectHand("p1");
        agent.expectHand("p2");

        agent.attack("p2:0", "p1:hero");

        agent.expectHand("p1");
        agent.expectHand("p2", TestCards.BLUEGILL_WARRIOR);
        expectManaCost(agent, "p2", 4);

        agent.expectBoard("p1", TestCards.expectedMinion(TestCards.YETI, 4, 5));
        agent.expectBoard("p2");
    }

    @Test
    public void testShadowStepDeathRattle() {
        agent.setMana("p1", 10);
        agent.setMana("p2", 10);

        agent.playMinionCard("p1", TestCards.SLUDGE_BELCHER, 0);

        agent.expectBoard("p1", TestCards.expectedMinion(TestCards.SLUDGE_BELCHER, 3, 5));
        agent.expectBoard("p2");
        agent.expectHand("p1");
        agent.expectHand("p2");

        agent.playNonMinionCard("p1", TestCards.SHADOW_STEP, "p1:0");

        agent.expectBoard("p1");
        agent.expectBoard("p2");
        agent.expectHand("p1", TestCards.SLUDGE_BELCHER);
        expectManaCost(agent, "p1", 3);
        agent.expectHand("p2");
    }
}
