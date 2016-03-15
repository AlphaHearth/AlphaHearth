package info.hearthsim.brazier;

import info.hearthsim.brazier.utils.BrazierTest;
import info.hearthsim.brazier.utils.TestCards;
import org.junit.Test;

public final class DivineShieldTest extends BrazierTest {
    @Test
    public void testScarletCrusaderAttacks() {
        agent.setMana("p1", 10);
        agent.setMana("p2", 10);

        agent.playMinionCard("p1", TestCards.SCARLET_CRUSADER, 0);

        agent.playMinionCard("p2", TestCards.YETI, 0);
        agent.playMinionCard("p2", TestCards.YETI, 0);

        agent.refreshAttacks();

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.SCARLET_CRUSADER, 3, 1));
        agent.expectBoard("p2",
            TestCards.expectedMinion(TestCards.YETI, 4, 5),
            TestCards.expectedMinion(TestCards.YETI, 4, 5));

        agent.attack("p1:0", "p2:0");

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.SCARLET_CRUSADER, 3, 1));
        agent.expectBoard("p2",
            TestCards.expectedMinion(TestCards.YETI, 4, 2),
            TestCards.expectedMinion(TestCards.YETI, 4, 5));

        agent.refreshAttacks();

        agent.attack("p1:0", "p2:1");

        agent.expectBoard("p1");
        agent.expectBoard("p2",
            TestCards.expectedMinion(TestCards.YETI, 4, 2),
            TestCards.expectedMinion(TestCards.YETI, 4, 2));
    }

    @Test
    public void testScarletCrusaderAttacked() {
        agent.setMana("p1", 10);
        agent.setMana("p2", 10);

        agent.playMinionCard("p1", TestCards.SCARLET_CRUSADER, 0);

        agent.playMinionCard("p2", TestCards.YETI, 0);
        agent.playMinionCard("p2", TestCards.YETI, 0);

        agent.refreshAttacks();

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.SCARLET_CRUSADER, 3, 1));
        agent.expectBoard("p2",
            TestCards.expectedMinion(TestCards.YETI, 4, 5),
            TestCards.expectedMinion(TestCards.YETI, 4, 5));

        agent.attack("p2:0", "p1:0");

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.SCARLET_CRUSADER, 3, 1));
        agent.expectBoard("p2",
            TestCards.expectedMinion(TestCards.YETI, 4, 2),
            TestCards.expectedMinion(TestCards.YETI, 4, 5));

        agent.attack("p2:1", "p1:0");

        agent.expectBoard("p1");
        agent.expectBoard("p2",
            TestCards.expectedMinion(TestCards.YETI, 4, 2),
            TestCards.expectedMinion(TestCards.YETI, 4, 2));
    }
}
