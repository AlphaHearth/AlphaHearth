package info.hearthsim.brazier;

import info.hearthsim.brazier.utils.BrazierTest;
import info.hearthsim.brazier.utils.TestCards;
import org.junit.Test;

public final class TargetedBattleCryTest extends BrazierTest {
    @Test
    public void testFireElemental() {
        agent.setMana("p1", 10);
        agent.setMana("p2", 10);

        agent.playMinionCard("p2", TestCards.YETI, 0);

        agent.expectBoard("p1");
        agent.expectBoard("p2",
            TestCards.expectedMinion(TestCards.YETI, 4, 5));

        agent.playMinionCard("p1", TestCards.FIRE_ELEMENTAL, 0, "p2:0");

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.FIRE_ELEMENTAL, 6, 5));
        agent.expectBoard("p2",
            TestCards.expectedMinion(TestCards.YETI, 4, 2));
    }
}
