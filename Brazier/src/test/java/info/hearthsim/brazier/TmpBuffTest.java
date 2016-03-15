package info.hearthsim.brazier;

import info.hearthsim.brazier.utils.BrazierTest;
import org.junit.Test;

import static info.hearthsim.brazier.utils.TestCards.*;

public final class TmpBuffTest extends BrazierTest {
    @Test
    public void testAbusiveSergeant() {
        agent.setMana("p1", 10);
        agent.playMinionCard("p1", YETI, 0);
        agent.playMinionCard("p1", ABUSIVE_SERGEANT, 1, "p1:0");

        agent.expectBoard("p1",
            expectedMinion(YETI, 6, 5),
            expectedMinion(ABUSIVE_SERGEANT, 2, 1));

        agent.endTurn();

        agent.expectBoard("p1",
            expectedMinion(YETI, 4, 5),
            expectedMinion(ABUSIVE_SERGEANT, 2, 1));
    }

    @Test
    public void testSilencedAbusiveSergeantBuff() {
        agent.setMana("p1", 10);
        agent.playMinionCard("p1", YETI, 0);
        agent.playMinionCard("p1", ABUSIVE_SERGEANT, 1, "p1:0");

        agent.expectBoard("p1",
            expectedMinion(YETI, 6, 5),
            expectedMinion(ABUSIVE_SERGEANT, 2, 1));

        agent.playNonMinionCard("p1", SILENCE, "p1:0");

        agent.expectBoard("p1",
            expectedMinion(YETI, 4, 5),
            expectedMinion(ABUSIVE_SERGEANT, 2, 1));

        agent.endTurn();

        agent.expectBoard("p1",
            expectedMinion(YETI, 4, 5),
            expectedMinion(ABUSIVE_SERGEANT, 2, 1));
    }
}
