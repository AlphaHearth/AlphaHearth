package info.hearthsim.brazier;

import info.hearthsim.brazier.utils.BrazierTest;
import info.hearthsim.brazier.utils.TestCards;
import org.junit.Test;

public final class ReversingSwitchTest extends BrazierTest {
    @Test
    public void testReverseWithStormwind() {
        agent.setMana("p1", 10);
        agent.playMinionCard("p1", TestCards.EMPEROR_COBRA, 0);
        agent.playMinionCard("p1", TestCards.STORMWIND_CHAMPION, 1);

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.EMPEROR_COBRA, 3, 4),
            TestCards.expectedMinion(TestCards.STORMWIND_CHAMPION, 6, 6));

        agent.setMana("p1", 10);

        agent.playNonMinionCard("p1", TestCards.REVERSING_SWITCH, "p1:0");

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.EMPEROR_COBRA, 5, 4),
            TestCards.expectedMinion(TestCards.STORMWIND_CHAMPION, 6, 6));
    }

    @Test
    public void testReverseWithAbusive() {
        agent.setCurrentPlayer("p1");
        agent.setMana("p1", 10);
        agent.playMinionCard("p1", TestCards.EMPEROR_COBRA, 0);
        agent.playMinionCard("p1", TestCards.ABUSIVE_SERGEANT, 1, "p1:0");

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.EMPEROR_COBRA, 4, 3),
            TestCards.expectedMinion(TestCards.ABUSIVE_SERGEANT, 2, 1));

        agent.playNonMinionCard("p1", TestCards.REVERSING_SWITCH, "p1:0");

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.EMPEROR_COBRA, 3, 4),
            TestCards.expectedMinion(TestCards.ABUSIVE_SERGEANT, 2, 1));

        agent.endTurn();

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.EMPEROR_COBRA, 3, 4),
            TestCards.expectedMinion(TestCards.ABUSIVE_SERGEANT, 2, 1));
    }
}
