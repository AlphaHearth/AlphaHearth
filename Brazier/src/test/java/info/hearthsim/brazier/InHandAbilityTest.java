package info.hearthsim.brazier;

import info.hearthsim.brazier.utils.BrazierTest;
import org.junit.Test;

import static info.hearthsim.brazier.utils.TestCards.*;

public final class InHandAbilityTest extends BrazierTest {
    @Test
    public void testBolvarWithoutDeaths() {
        agent.addToHand("p1", BOLVAR_FORDRAGON);

        agent.setMana("p1", 10);
        agent.setMana("p2", 10);

        agent.playMinionCard("p1", WISP, 0);
        agent.playMinionCard("p2", WISP, 0);
        agent.playMinionCard("p1", 0, 1);

        agent.expectBoard("p1",
            expectedMinion(WISP, 1, 1),
            expectedMinion(BOLVAR_FORDRAGON, 1, 7));
        agent.expectBoard("p2",
            expectedMinion(WISP, 1, 1));
    }

    @Test
    public void testBolvarWithDeaths() {
        agent.addToHand("p1", BOLVAR_FORDRAGON);

        agent.setMana("p1", 10);
        agent.setMana("p2", 10);

        agent.playMinionCard("p1", WISP, 0);
        agent.playMinionCard("p1", WISP, 1);
        agent.playMinionCard("p1", WISP, 2);
        agent.playMinionCard("p2", WISP, 0);

        agent.playCard("p2", FLAMESTRIKE);
        agent.playMinionCard("p1", 0, 0);

        agent.expectBoard("p1",
            expectedMinion(BOLVAR_FORDRAGON, 4, 7));
        agent.expectBoard("p2",
            expectedMinion(WISP, 1, 1));
    }

    @Test
    public void testBolvarWithEnemyDeaths() {
        agent.addToHand("p1", BOLVAR_FORDRAGON);

        agent.setMana("p1", 10);
        agent.setMana("p2", 10);

        agent.playMinionCard("p1", WISP, 0);
        agent.playMinionCard("p2", WISP, 0);
        agent.playMinionCard("p2", WISP, 1);
        agent.playMinionCard("p2", WISP, 2);

        agent.playCard("p1", FLAMESTRIKE);
        agent.setMana("p1", 10);
        agent.playMinionCard("p1", 0, 1);

        agent.expectBoard("p1",
            expectedMinion(WISP, 1, 1),
            expectedMinion(BOLVAR_FORDRAGON, 1, 7));
        agent.expectBoard("p2");
    }

    @Test
    public void testBolvarIsNotBuffedAfterPlay() {
        agent.addToHand("p1", BOLVAR_FORDRAGON);

        agent.setMana("p1", 10);
        agent.setMana("p2", 10);

        agent.playMinionCard("p1", WISP, 0);
        agent.playMinionCard("p2", WISP, 0);
        agent.playMinionCard("p1", 0, 1);

        agent.expectBoard("p1",
            expectedMinion(WISP, 1, 1),
            expectedMinion(BOLVAR_FORDRAGON, 1, 7));
        agent.expectBoard("p2",
            expectedMinion(WISP, 1, 1));

        agent.playNonMinionCard("p2", MOONFIRE, "p1:0");

        agent.expectBoard("p1",
            expectedMinion(BOLVAR_FORDRAGON, 1, 7));
        agent.expectBoard("p2",
            expectedMinion(WISP, 1, 1));
    }
}
