package info.hearthsim.brazier;

import info.hearthsim.brazier.utils.BrazierTest;
import org.junit.Test;

import static info.hearthsim.brazier.utils.TestCards.*;

public final class StatBuffTest extends BrazierTest {
    @Test
    public void testAttackAndHpBuff() {
        agent.setMana("p1", 10);

        agent.playMinionCard("p1", YETI, 0);

        agent.expectBoard("p1",
            expectedMinion(YETI, 4, 5));

        agent.playMinionCard("p1", SHATTERED_SUN_CLERIC, 1, "p1:0");

        agent.expectBoard("p1",
            expectedMinion(YETI, 5, 6),
            expectedMinion(SHATTERED_SUN_CLERIC, 3, 2));
    }
}
