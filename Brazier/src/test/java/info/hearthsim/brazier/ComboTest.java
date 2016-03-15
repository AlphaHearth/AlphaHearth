package info.hearthsim.brazier;

import info.hearthsim.brazier.utils.BrazierTest;
import info.hearthsim.brazier.utils.TestCards;
import org.junit.Test;

public final class ComboTest extends BrazierTest {
    @Test
    public void testEviscerateNoCombo() {
        agent.setMana("p1", 10);
        agent.playMinionCard("p1", TestCards.YETI, 0);
        agent.setCurrentPlayer("p1");

        agent.expectBoard("p1", TestCards.expectedMinion(TestCards.YETI, 4, 5));

        agent.endTurn();

        agent.setMana("p2", 10);
        agent.playNonMinionCard("p2", TestCards.EVISCERATE, "p1:0");

        agent.expectBoard("p1", TestCards.expectedMinion(TestCards.YETI, 4, 3));
    }

    @Test
    public void testEviscerateNoComboNextTurn() {
        agent.setMana("p1", 10);
        agent.playMinionCard("p1", TestCards.YETI, 0);
        agent.setCurrentPlayer("p1");

        agent.expectBoard("p1", TestCards.expectedMinion(TestCards.YETI, 4, 5));

        agent.endTurn();

        agent.setMana("p2", 10);
        agent.playNonMinionCard("p2", TestCards.MOONFIRE, "p1:hero");

        agent.endTurn();
        agent.endTurn();

        agent.playNonMinionCard("p2", TestCards.EVISCERATE, "p1:0");

        agent.expectBoard("p1", TestCards.expectedMinion(TestCards.YETI, 4, 3));
    }

    @Test
    public void testEviscerateCombo() {
        agent.setMana("p1", 10);
        agent.playMinionCard("p1", TestCards.YETI, 0);
        agent.setCurrentPlayer("p1");

        agent.expectBoard("p1", TestCards.expectedMinion(TestCards.YETI, 4, 5));

        agent.endTurn();

        agent.setMana("p2", 10);
        agent.playNonMinionCard("p2", TestCards.MOONFIRE, "p1:hero");
        agent.playNonMinionCard("p2", TestCards.EVISCERATE, "p1:0");

        agent.expectBoard("p1", TestCards.expectedMinion(TestCards.YETI, 4, 1));
    }

    @Test
    public void testDefiasRingLeaderCombo() {
        agent.setMana("p1", 10);
        agent.setCurrentPlayer("p1");

        agent.playMinionCard("p1", TestCards.FLAME_OF_AZZINOTH, 0);
        agent.playMinionCard("p1", TestCards.SLIME, 1);

        agent.playMinionCard("p1", TestCards.DEFIAS_RINGLEADER, 1);

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.FLAME_OF_AZZINOTH, 2, 1),
            TestCards.expectedMinion(TestCards.DEFIAS_RINGLEADER, 2, 2),
            TestCards.expectedMinion(TestCards.DEFIAS_BANDIT, 2, 1),
            TestCards.expectedMinion(TestCards.SLIME, 1, 2));

        agent.playMinionCard("p1", TestCards.BLUEGILL_WARRIOR, 0);

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.BLUEGILL_WARRIOR, 2, 1),
            TestCards.expectedMinion(TestCards.FLAME_OF_AZZINOTH, 2, 1),
            TestCards.expectedMinion(TestCards.DEFIAS_RINGLEADER, 2, 2),
            TestCards.expectedMinion(TestCards.DEFIAS_BANDIT, 2, 1),
            TestCards.expectedMinion(TestCards.SLIME, 1, 2));

        agent.endTurn();

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.BLUEGILL_WARRIOR, 2, 1),
            TestCards.expectedMinion(TestCards.FLAME_OF_AZZINOTH, 2, 1),
            TestCards.expectedMinion(TestCards.DEFIAS_RINGLEADER, 2, 2),
            TestCards.expectedMinion(TestCards.DEFIAS_BANDIT, 2, 1),
            TestCards.expectedMinion(TestCards.SLIME, 1, 2));
    }

    @Test
    public void testDefiasRingLeaderNoCombo() {
        agent.setMana("p1", 10);
        agent.setCurrentPlayer("p1");

        agent.playMinionCard("p1", TestCards.DEFIAS_RINGLEADER, 0);
        agent.playMinionCard("p1", TestCards.FLAME_OF_AZZINOTH, 0);
        agent.playMinionCard("p1", TestCards.SLIME, 2);

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.FLAME_OF_AZZINOTH, 2, 1),
            TestCards.expectedMinion(TestCards.DEFIAS_RINGLEADER, 2, 2),
            TestCards.expectedMinion(TestCards.SLIME, 1, 2));

        agent.playMinionCard("p1", TestCards.BLUEGILL_WARRIOR, 0);

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.BLUEGILL_WARRIOR, 2, 1),
            TestCards.expectedMinion(TestCards.FLAME_OF_AZZINOTH, 2, 1),
            TestCards.expectedMinion(TestCards.DEFIAS_RINGLEADER, 2, 2),
            TestCards.expectedMinion(TestCards.SLIME, 1, 2));

        agent.endTurn();

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.BLUEGILL_WARRIOR, 2, 1),
            TestCards.expectedMinion(TestCards.FLAME_OF_AZZINOTH, 2, 1),
            TestCards.expectedMinion(TestCards.DEFIAS_RINGLEADER, 2, 2),
            TestCards.expectedMinion(TestCards.SLIME, 1, 2));
    }
}
