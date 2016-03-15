package info.hearthsim.brazier;

import info.hearthsim.brazier.utils.BrazierTest;
import info.hearthsim.brazier.utils.TestCards;
import org.junit.Test;

public final class CardPlayedTest extends BrazierTest {
    @Test
    public void testTwoHobgoblins() {
        agent.setMana("p1", 10);
        agent.playMinionCard("p1", TestCards.HOBGOBLIN, 0);
        agent.playMinionCard("p1", TestCards.HOBGOBLIN, 1);
        agent.playMinionCard("p1", TestCards.SLIME, 2);

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.HOBGOBLIN, 2, 3),
            TestCards.expectedMinion(TestCards.HOBGOBLIN, 2, 3),
            TestCards.expectedMinion(TestCards.SLIME, 5, 6));
    }

    @Test
    public void testIllidanPlayToItsRightSide() {
        agent.setMana("p1", 10);
        agent.playMinionCard("p1", TestCards.YETI, 0);
        agent.playMinionCard("p1", TestCards.YETI, 0);
        agent.setMana("p1", 10);
        agent.playMinionCard("p1", TestCards.ILLIDAN_STORMRAGE, 1);

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.YETI, 4, 5),
            TestCards.expectedMinion(TestCards.ILLIDAN_STORMRAGE, 7, 5),
            TestCards.expectedMinion(TestCards.YETI, 4, 5));
        agent.expectBoard("p2");

        agent.playMinionCard("p1", TestCards.SLIME, 2);

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.YETI, 4, 5),
            TestCards.expectedMinion(TestCards.ILLIDAN_STORMRAGE, 7, 5),
            TestCards.expectedMinion(TestCards.FLAME_OF_AZZINOTH, 2, 1),
            TestCards.expectedMinion(TestCards.SLIME, 1, 2),
            TestCards.expectedMinion(TestCards.YETI, 4, 5));
        agent.expectBoard("p2");
    }

    @Test
    public void testIllidanPlayToItsLeftSide() {
        agent.setMana("p1", 10);
        agent.playMinionCard("p1", TestCards.YETI, 0);
        agent.playMinionCard("p1", TestCards.YETI, 0);
        agent.setMana("p1", 10);
        agent.playMinionCard("p1", TestCards.ILLIDAN_STORMRAGE, 1);

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.YETI, 4, 5),
            TestCards.expectedMinion(TestCards.ILLIDAN_STORMRAGE, 7, 5),
            TestCards.expectedMinion(TestCards.YETI, 4, 5));
        agent.expectBoard("p2");

        agent.playMinionCard("p1", TestCards.SLIME, 1);

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.YETI, 4, 5),
            TestCards.expectedMinion(TestCards.SLIME, 1, 2),
            TestCards.expectedMinion(TestCards.ILLIDAN_STORMRAGE, 7, 5),
            TestCards.expectedMinion(TestCards.FLAME_OF_AZZINOTH, 2, 1),
            TestCards.expectedMinion(TestCards.YETI, 4, 5));
        agent.expectBoard("p2");
    }

    @Test
    public void testIllidanIsNotAffectedByTheOpponent() {
        agent.setMana("p1", 10);
        agent.setMana("p2", 10);

        agent.playMinionCard("p1", TestCards.YETI, 0);
        agent.playMinionCard("p1", TestCards.YETI, 0);
        agent.setMana("p1", 10);
        agent.playMinionCard("p1", TestCards.ILLIDAN_STORMRAGE, 1);

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.YETI, 4, 5),
            TestCards.expectedMinion(TestCards.ILLIDAN_STORMRAGE, 7, 5),
            TestCards.expectedMinion(TestCards.YETI, 4, 5));
        agent.expectBoard("p2");

        agent.playMinionCard("p2", TestCards.SLIME, 0);

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.YETI, 4, 5),
            TestCards.expectedMinion(TestCards.ILLIDAN_STORMRAGE, 7, 5),
            TestCards.expectedMinion(TestCards.YETI, 4, 5));
        agent.expectBoard("p2",
            TestCards.expectedMinion(TestCards.SLIME, 1, 2));
    }
}
