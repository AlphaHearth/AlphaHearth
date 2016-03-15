package info.hearthsim.brazier;

import info.hearthsim.brazier.utils.BrazierTest;
import info.hearthsim.brazier.utils.TestAgent;
import info.hearthsim.brazier.utils.TestCards;
import org.junit.Assert;
import org.junit.Test;

public final class StealthTest extends BrazierTest {
    private static void expectTargetable(TestAgent script, boolean expectation) {
        script.expectMinion("p1:0", (tiger) -> {
            Player opponent = tiger.getWorld().getOpponent(tiger.getOwner().getPlayerId());
            PlayerId playerId = opponent.getPlayerId();

            Assert.assertEquals("targetable by minion", expectation, tiger.isTargetable(new TargeterDef(playerId, false, true)));
            Assert.assertEquals("targetable by hero", expectation, tiger.isTargetable(new TargeterDef(playerId, true, true)));
            Assert.assertEquals("targetable by hero", expectation, tiger.isTargetable(new TargeterDef(playerId, true, false)));
        });
    }

    @Test
    public void testBasicStealthFeatures() {
            agent.setMana("p1", 10);
            agent.setMana("p2", 10);

            agent.playMinionCard("p1", TestCards.STRANGLETHORN_TIGER, 0);
            agent.playMinionCard("p2", TestCards.YETI, 0);
            agent.playCard("p2", TestCards.FIERY_WAR_AXE);

            agent.expectBoard("p1",
                TestCards.expectedMinionWithFlags(TestCards.STRANGLETHORN_TIGER, 5, 5, "stealth"));
            agent.expectBoard("p2",
                TestCards.expectedMinion(TestCards.YETI, 4, 5));

            agent.setCurrentPlayer("p2");

            expectTargetable(agent, false);

            agent.endTurn();

            agent.refreshAttacks();
            agent.attack("p1:0", "p2:hero");

            agent.endTurn();

            agent.expectBoard("p1",
                TestCards.expectedMinionWithFlags(TestCards.STRANGLETHORN_TIGER, 5, 5));
            agent.expectBoard("p2",
                TestCards.expectedMinion(TestCards.YETI, 4, 5));

            expectTargetable(agent, true);
    }

    @Test
    public void testStolenStealth() {
            agent.setCurrentPlayer("p1");
            agent.setMana("p1", 10);
            agent.playMinionCard("p1", TestCards.WISP, 0);
            agent.playNonMinionCard("p1", TestCards.FINICKY_CLOAKFIELD, "p1:0");

            agent.expectBoard("p1",
                TestCards.expectedMinionWithFlags(TestCards.WISP, 1, 1, "stealth"));
            agent.expectBoard("p2");

            agent.endTurn();

            agent.expectBoard("p1",
                TestCards.expectedMinionWithFlags(TestCards.WISP, 1, 1, "stealth"));
            agent.expectBoard("p2");

            agent.setMana("p2", 10);
            agent.playMinionCard("p2", TestCards.SYLVANAS_WINDRUNNER, 0);
            agent.playNonMinionCard("p2", TestCards.FIREBALL, "p2:0");

            agent.expectBoard("p1");
            agent.expectBoard("p2",
                TestCards.expectedMinionWithFlags(TestCards.WISP, 1, 1, "stealth"));

            agent.endTurn();

            agent.expectBoard("p1");
            agent.expectBoard("p2",
                TestCards.expectedMinionWithFlags(TestCards.WISP, 1, 1, "stealth"));

            agent.endTurn();

            agent.expectBoard("p1");
            agent.expectBoard("p2",
                TestCards.expectedMinionWithFlags(TestCards.WISP, 1, 1));
    }
}
