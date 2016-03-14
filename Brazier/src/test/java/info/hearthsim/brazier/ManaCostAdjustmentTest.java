package info.hearthsim.brazier;

import org.junit.Test;

public final class ManaCostAdjustmentTest {
    @Test
    public void testDreadCorsairCost4ByDefault() {
        PlayScript.testScript((script) -> {
            script.setMana("p1", 10);
            script.playMinionCard("p1", TestCards.DREAD_CORSAIR, 0);
            script.expectedMana("p1", 6);

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.DREAD_CORSAIR, 3, 3));
            script.expectBoard("p2");
        });
    }

    @Test
    public void testDreadCorsairCostIsReducedCorrectly() {
        PlayScript.testScript((script) -> {
            script.setMana("p1", 10);
            script.playCard("p1", TestCards.FIERY_WAR_AXE);
            script.expectedMana("p1", 8);
            script.playMinionCard("p1", TestCards.DREAD_CORSAIR, 0);
            script.expectedMana("p1", 7);

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.DREAD_CORSAIR, 3, 3));
            script.expectBoard("p2");
        });
    }
}
