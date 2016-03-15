package info.hearthsim.brazier;

import info.hearthsim.brazier.utils.BrazierTest;
import info.hearthsim.brazier.utils.TestCards;
import org.junit.Test;

public final class FreezeTest extends BrazierTest {
    @Test
    public void testFreezingOpponent() {
        agent.setMana("p1", 10);
        agent.setMana("p2", 10);

        agent.playMinionCard("p1", TestCards.YETI, 0);

        agent.setCurrentPlayer("p2");

        agent.expectBoard("p1",
            TestCards.expectedMinionWithFlags(TestCards.YETI, 4, 5));

        agent.playCard("p2", TestCards.FROST_NOVA);

        agent.expectBoard("p1",
            TestCards.expectedMinionWithFlags(TestCards.YETI, 4, 5, "frozen"));

        agent.endTurn();

        agent.expectBoard("p1",
            TestCards.expectedMinionWithFlags(TestCards.YETI, 4, 5, "frozen"));

        agent.endTurn();

        agent.expectBoard("p1",
            TestCards.expectedMinionWithFlags(TestCards.YETI, 4, 5));
    }

    @Test
    public void testFreezingSelfWithoutAttack() {
        agent.setMana("p1", 10);
        agent.setMana("p2", 10);

        agent.setCurrentPlayer("p1");
        agent.playMinionCard("p1", TestCards.YETI, 0);

        agent.expectBoard("p1",
            TestCards.expectedMinionWithFlags(TestCards.YETI, 4, 5));

        agent.playNonMinionCard("p1", TestCards.CONE_OF_COLD, "p1:0");

        agent.expectBoard("p1",
            TestCards.expectedMinionWithFlags(TestCards.YETI, 4, 4, "frozen"));

        agent.endTurn();

        agent.expectBoard("p1",
            TestCards.expectedMinionWithFlags(TestCards.YETI, 4, 4));
    }

    @Test
    public void testFreezingSelfWithAttack() {
        agent.setMana("p1", 10);
        agent.setMana("p2", 10);

        agent.setCurrentPlayer("p1");
        agent.playMinionCard("p1", TestCards.STORMWIND_KNIGHT, 0);

        agent.expectBoard("p1",
            TestCards.expectedMinionWithFlags(TestCards.STORMWIND_KNIGHT, 2, 5));

        agent.attack("p1:0", "p2:hero");

        agent.playNonMinionCard("p1", TestCards.CONE_OF_COLD, "p1:0");

        agent.expectBoard("p1",
            TestCards.expectedMinionWithFlags(TestCards.STORMWIND_KNIGHT, 2, 4, "frozen"));

        agent.endTurn();

        agent.expectBoard("p1",
            TestCards.expectedMinionWithFlags(TestCards.STORMWIND_KNIGHT, 2, 4, "frozen"));

        agent.endTurn();

        agent.expectBoard("p1",
            TestCards.expectedMinionWithFlags(TestCards.STORMWIND_KNIGHT, 2, 4, "frozen"));

        agent.endTurn();

        agent.expectBoard("p1",
            TestCards.expectedMinionWithFlags(TestCards.STORMWIND_KNIGHT, 2, 4));
    }
}
