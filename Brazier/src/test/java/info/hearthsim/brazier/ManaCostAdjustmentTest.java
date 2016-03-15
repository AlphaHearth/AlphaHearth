package info.hearthsim.brazier;

import info.hearthsim.brazier.utils.BrazierTest;
import info.hearthsim.brazier.utils.TestCards;
import org.junit.Test;

public final class ManaCostAdjustmentTest extends BrazierTest {
    @Test
    public void testDreadCorsairCost4ByDefault() {
        agent.setMana("p1", 10);
        agent.playMinionCard("p1", TestCards.DREAD_CORSAIR, 0);
        agent.expectMana("p1", 6);

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.DREAD_CORSAIR, 3, 3));
        agent.expectBoard("p2");
    }

    @Test
    public void testDreadCorsairCostIsReducedCorrectly() {
        agent.setMana("p1", 10);
        agent.playCard("p1", TestCards.FIERY_WAR_AXE);
        agent.expectMana("p1", 8);
        agent.playMinionCard("p1", TestCards.DREAD_CORSAIR, 0);
        agent.expectMana("p1", 7);

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.DREAD_CORSAIR, 3, 3));
        agent.expectBoard("p2");
    }
}
