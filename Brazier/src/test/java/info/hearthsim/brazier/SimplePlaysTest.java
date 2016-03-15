package info.hearthsim.brazier;

import info.hearthsim.brazier.utils.BrazierTest;
import org.junit.Test;

import static info.hearthsim.brazier.utils.TestCards.*;

public final class SimplePlaysTest extends BrazierTest {
    @Test
    public void testSlamDrawsCards() {
        agent.setMana("p1", 4);
        agent.setMana("p2", 10);
        agent.deck("p1", YETI, SLUDGE_BELCHER, WHIRLWIND);

        agent.playMinionCard("p2", YETI, 0);
        agent.playMinionCard("p2", YETI, 0);

        agent.playNonMinionCard("p1", SLAM, "p2:0");

        agent.expectBoard("p1");
        agent.expectBoard("p2",
            expectedMinion(YETI, 4, 3),
            expectedMinion(YETI, 4, 5));

        agent.expectDeck("p1", YETI, SLUDGE_BELCHER);
        agent.expectHand("p1", WHIRLWIND);
    }

    @Test
    public void testSlamDoesntDrawCards() {
        agent.setMana("p1", 10);
        agent.setMana("p2", 10);
        agent.deck("p1", YETI, SLUDGE_BELCHER, WHIRLWIND);

        agent.playMinionCard("p1", YETI, 0);

        agent.playMinionCard("p2", YETI, 0);
        agent.playMinionCard("p2", YETI, 0);

        agent.refreshAttacks();

        agent.expectBoard("p1",
            expectedMinion(YETI, 4, 5));
        agent.expectBoard("p2",
            expectedMinion(YETI, 4, 5),
            expectedMinion(YETI, 4, 5));

        agent.attack("p1:0", "p2:1");

        agent.expectBoard("p1",
            expectedMinion(YETI, 4, 1));
        agent.expectBoard("p2",
            expectedMinion(YETI, 4, 5),
            expectedMinion(YETI, 4, 1));

        agent.playNonMinionCard("p1", SLAM, "p2:1");

        agent.expectBoard("p1",
            expectedMinion(YETI, 4, 1));
        agent.expectBoard("p2",
            expectedMinion(YETI, 4, 5));

        agent.expectDeck("p1", YETI, SLUDGE_BELCHER, WHIRLWIND);
        agent.expectHand("p1");
    }

    @Test
    public void testPlaySimpleMinion() {
        agent.setMana("p1", 4);
        agent.playMinionCard("p1", YETI, 0);

        agent.expectBoard("p1", expectedMinion(YETI, 4, 5));
        agent.expectBoard("p2");
    }

    @Test
    public void testPlayMultipleMinions() {
        agent.setMana("p1", 10);
        agent.playMinionCard("p1", FROTHING_BERSERKER, 0);
        agent.playMinionCard("p1", FROTHING_BERSERKER, 1);
        agent.playMinionCard("p1", YETI, 1);

        agent.expectBoard("p1",
            expectedMinion(FROTHING_BERSERKER, 2, 4),
            expectedMinion(YETI, 4, 5),
            expectedMinion(FROTHING_BERSERKER, 2, 4));
    }
}
