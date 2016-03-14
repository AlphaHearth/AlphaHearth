package info.hearthsim.brazier;

import org.junit.Assert;
import org.junit.Test;

public final class StealthTest {
    private static void expectTargetable(PlayScript script, boolean expectation) {
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
        PlayScript.testScript((script) -> {
            script.setMana("p1", 10);
            script.setMana("p2", 10);

            script.playMinionCard("p1", TestCards.STRANGLETHORN_TIGER, 0);
            script.playMinionCard("p2", TestCards.YETI, 0);
            script.playCard("p2", TestCards.FIERY_WAR_AXE);

            script.expectBoard("p1",
                    TestCards.expectedMinionWithFlags(TestCards.STRANGLETHORN_TIGER, 5, 5, "stealth"));
            script.expectBoard("p2",
                    TestCards.expectedMinion(TestCards.YETI, 4, 5));

            script.setCurrentPlayer("p2");

            expectTargetable(script, false);

            script.endTurn();

            script.refreshAttacks();
            script.attack("p1:0", "p2:hero");

            script.endTurn();

            script.expectBoard("p1",
                    TestCards.expectedMinionWithFlags(TestCards.STRANGLETHORN_TIGER, 5, 5));
            script.expectBoard("p2",
                    TestCards.expectedMinion(TestCards.YETI, 4, 5));

            expectTargetable(script, true);
        });
    }

    @Test
    public void testStolenStealth() {
        PlayScript.testScript((script) -> {
            script.setCurrentPlayer("p1");
            script.setMana("p1", 10);
            script.playMinionCard("p1", TestCards.WISP, 0);
            script.playNonMinionCard("p1", TestCards.FINICKY_CLOAKFIELD, "p1:0");

            script.expectBoard("p1",
                    TestCards.expectedMinionWithFlags(TestCards.WISP, 1, 1, "stealth"));
            script.expectBoard("p2");

            script.endTurn();

            script.expectBoard("p1",
                    TestCards.expectedMinionWithFlags(TestCards.WISP, 1, 1, "stealth"));
            script.expectBoard("p2");

            script.setMana("p2", 10);
            script.playMinionCard("p2", TestCards.SYLVANAS_WINDRUNNER, 0);
            script.playNonMinionCard("p2", TestCards.FIREBALL, "p2:0");

            script.expectBoard("p1");
            script.expectBoard("p2",
                    TestCards.expectedMinionWithFlags(TestCards.WISP, 1, 1, "stealth"));

            script.endTurn();

            script.expectBoard("p1");
            script.expectBoard("p2",
                    TestCards.expectedMinionWithFlags(TestCards.WISP, 1, 1, "stealth"));

            script.endTurn();

            script.expectBoard("p1");
            script.expectBoard("p2",
                    TestCards.expectedMinionWithFlags(TestCards.WISP, 1, 1));
        });
    }
}
