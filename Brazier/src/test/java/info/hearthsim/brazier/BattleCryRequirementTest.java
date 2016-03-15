package info.hearthsim.brazier;

import info.hearthsim.brazier.utils.BrazierTest;
import info.hearthsim.brazier.utils.TestCards;
import org.junit.Test;

public final class BattleCryRequirementTest extends BrazierTest {
    @Test
    public void testBlackwingTechnicianRequirementIsNotMet() {
        agent.setMana("p1", 10);

        agent.playMinionCard("p1", TestCards.BLACKWING_TECHNICIAN, 0);

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.BLACKWING_TECHNICIAN, 2, 4));
    }

    @Test
    public void testBlackwingTechnicianRequirementIsMet() {
        agent.setMana("p1", 10);
        agent.addToHand("p1", TestCards.BLUEGILL_WARRIOR, TestCards.MALYGOS, TestCards.YETI);

        agent.playMinionCard("p1", TestCards.BLACKWING_TECHNICIAN, 0);

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.BLACKWING_TECHNICIAN, 3, 5));
    }

    @Test
    public void testBlackwingCorruptorRequirementIsNotMet() {
        agent.setMana("p1", 10);
        agent.setMana("p2", 10);

        agent.playMinionCard("p2", TestCards.YETI, 0);
        agent.playMinionCard("p1", TestCards.BLACKWING_CORRUPTOR, 0, "p2:0"); // Target must be ignored

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.BLACKWING_CORRUPTOR, 5, 4));
        agent.expectBoard("p2",
            TestCards.expectedMinion(TestCards.YETI, 4, 5));
    }

    @Test
    public void testBlackwingCorruptorRequirementIsMet() {
        agent.setMana("p1", 10);
        agent.setMana("p2", 10);

        agent.addToHand("p1", TestCards.BLUEGILL_WARRIOR, TestCards.MALYGOS, TestCards.YETI);

        agent.playMinionCard("p2", TestCards.YETI, 0);
        agent.playMinionCard("p1", TestCards.BLACKWING_CORRUPTOR, 0, "p2:0");

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.BLACKWING_CORRUPTOR, 5, 4));
        agent.expectBoard("p2",
            TestCards.expectedMinion(TestCards.YETI, 4, 2));
    }
}
