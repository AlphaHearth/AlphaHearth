package info.hearthsim.brazier;

import org.junit.Test;

public final class BattleCryRequirementTest {
    @Test
    public void testBlackwingTechnicianRequirementIsNotMet() {
        PlayScript.testScript((script) -> {
            script.setMana("p1", 10);

            script.playMinionCard("p1", TestCards.BLACKWING_TECHNICIAN, 0);

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.BLACKWING_TECHNICIAN, 2, 4));
        });
    }

    @Test
    public void testBlackwingTechnicianRequirementIsMet() {
        PlayScript.testScript((script) -> {
            script.setMana("p1", 10);
            script.addToHand("p1", TestCards.BLUEGILL_WARRIOR, TestCards.MALYGOS, TestCards.YETI);

            script.playMinionCard("p1", TestCards.BLACKWING_TECHNICIAN, 0);

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.BLACKWING_TECHNICIAN, 3, 5));
        });
    }

    @Test
    public void testBlackwingCorruptorRequirementIsNotMet() {
        PlayScript.testScript((script) -> {
            script.setMana("p1", 10);
            script.setMana("p2", 10);

            script.playMinionCard("p2", TestCards.YETI, 0);
            script.playMinionCard("p1", TestCards.BLACKWING_CORRUPTOR, 0, "p2:0"); // Target must be ignored

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.BLACKWING_CORRUPTOR, 5, 4));
            script.expectBoard("p2",
                    TestCards.expectedMinion(TestCards.YETI, 4, 5));
        });
    }

    @Test
    public void testBlackwingCorruptorRequirementIsMet() {
        PlayScript.testScript((script) -> {
            script.setMana("p1", 10);
            script.setMana("p2", 10);

            script.addToHand("p1", TestCards.BLUEGILL_WARRIOR, TestCards.MALYGOS, TestCards.YETI);

            script.playMinionCard("p2", TestCards.YETI, 0);
            script.playMinionCard("p1", TestCards.BLACKWING_CORRUPTOR, 0, "p2:0");

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.BLACKWING_CORRUPTOR, 5, 4));
            script.expectBoard("p2",
                    TestCards.expectedMinion(TestCards.YETI, 4, 2));
        });
    }
}
