package info.hearthsim.brazier;

import info.hearthsim.brazier.minions.MinionId;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import info.hearthsim.brazier.utils.BrazierTest;
import info.hearthsim.brazier.utils.RandomTestUtils;
import org.junit.Test;

import static info.hearthsim.brazier.utils.TestCards.*;
import static org.junit.Assert.*;

public final class ResurrectTest extends BrazierTest {
    @Test
    public void testResurrectNoDeaths() {
            agent.setMana("p1", 10);
            agent.setMana("p2", 10);
            agent.deck("p1", YETI, SLUDGE_BELCHER, WHIRLWIND);

            agent.playMinionCard("p1", BLUEGILL_WARRIOR, 0);

            agent.expectBoard("p1",
                expectedMinion(BLUEGILL_WARRIOR, 2, 1));
            agent.expectBoard("p2");

            agent.playCard("p1", RESURRECT);

            agent.expectBoard("p1",
                expectedMinion(BLUEGILL_WARRIOR, 2, 1));
            agent.expectBoard("p2");
    }

    @Test
    public void testResurrectSingleDeath() {
            agent.setMana("p1", 10);
            agent.setMana("p2", 10);
            agent.deck("p1", YETI, SLUDGE_BELCHER, WHIRLWIND);

            agent.playMinionCard("p1", BLUEGILL_WARRIOR, 0);
            agent.playMinionCard("p1", YETI, 1);

            agent.expectBoard("p1",
                expectedMinion(BLUEGILL_WARRIOR, 2, 1),
                expectedMinion(YETI, 4, 5));
            agent.expectBoard("p2");

            agent.playNonMinionCard("p1", MOONFIRE, "p1:0");

            agent.expectBoard("p1",
                expectedMinion(YETI, 4, 5));
            agent.expectBoard("p2");

            agent.playCard("p1", RESURRECT);

            agent.expectBoard("p1",
                expectedMinion(YETI, 4, 5),
                expectedMinion(BLUEGILL_WARRIOR, 2, 1));
            agent.expectBoard("p2");
    }

    @Test
    public void testResurrectTwoDeaths() {
        Set<String> resurrected = new HashSet<>();
        resurrected.add(runTwoMinionsResurrect(0).getName());
        setUp(); // Set up again to reset TestAgent
        resurrected.add(runTwoMinionsResurrect(1).getName());

        assertEquals("Possible minions",
                new HashSet<>(Arrays.asList(BLUEGILL_WARRIOR, FLAME_OF_AZZINOTH)),
                resurrected);
    }

    private MinionId runTwoMinionsResurrect(int roll) {
        List<MinionId> board = RandomTestUtils.boardMinionScript(agent, "p1", (agent) -> {
            agent.setMana("p1", 10);
            agent.setMana("p2", 10);
            agent.deck("p1", YETI, SLUDGE_BELCHER, WHIRLWIND);

            agent.playMinionCard("p1", BLUEGILL_WARRIOR, 0);
            agent.playMinionCard("p1", FLAME_OF_AZZINOTH, 1);

            agent.expectBoard("p1",
                expectedMinion(BLUEGILL_WARRIOR, 2, 1),
                expectedMinion(FLAME_OF_AZZINOTH, 2, 1));
            agent.expectBoard("p2");

            agent.playNonMinionCard("p1", MOONFIRE, "p1:0");
            agent.playNonMinionCard("p1", MOONFIRE, "p1:0");

            agent.expectBoard("p1");
            agent.expectBoard("p2");

            agent.addRoll(2, roll);
            agent.playCard("p1", RESURRECT);
        });

        if (board.size() != 1) {
            fail("Expected exactly one minion to be resurrected but the resulting board was " + board);
        }

        return board.get(0);
    }
}
