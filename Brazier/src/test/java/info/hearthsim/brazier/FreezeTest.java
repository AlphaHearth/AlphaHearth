package info.hearthsim.brazier;

import org.junit.Test;

public final class FreezeTest {
    @Test
    public void testFreezingOpponent() {
        PlayScript.testScript((script) -> {
            script.setMana("p1", 10);
            script.setMana("p2", 10);

            script.playMinionCard("p1", TestCards.YETI, 0);

            script.setCurrentPlayer("p2");

            script.expectBoard("p1",
                    TestCards.expectedMinionWithFlags(TestCards.YETI, 4, 5));

            script.playCard("p2", TestCards.FROST_NOVA);

            script.expectBoard("p1",
                    TestCards.expectedMinionWithFlags(TestCards.YETI, 4, 5, "frozen"));

            script.endTurn();

            script.expectBoard("p1",
                    TestCards.expectedMinionWithFlags(TestCards.YETI, 4, 5, "frozen"));

            script.endTurn();

            script.expectBoard("p1",
                    TestCards.expectedMinionWithFlags(TestCards.YETI, 4, 5));
        });
    }

    @Test
    public void testFreezingSelfWithoutAttack() {
        PlayScript.testScript((script) -> {
            script.setMana("p1", 10);
            script.setMana("p2", 10);

            script.setCurrentPlayer("p1");
            script.playMinionCard("p1", TestCards.YETI, 0);

            script.expectBoard("p1",
                    TestCards.expectedMinionWithFlags(TestCards.YETI, 4, 5));

            script.playNonMinionCard("p1", TestCards.CONE_OF_COLD, "p1:0");

            script.expectBoard("p1",
                    TestCards.expectedMinionWithFlags(TestCards.YETI, 4, 4, "frozen"));

            script.endTurn();

            script.expectBoard("p1",
                    TestCards.expectedMinionWithFlags(TestCards.YETI, 4, 4));
        });
    }

    @Test
    public void testFreezingSelfWithAttack() {
        PlayScript.testScript((script) -> {
            script.setMana("p1", 10);
            script.setMana("p2", 10);

            script.setCurrentPlayer("p1");
            script.playMinionCard("p1", TestCards.STORMWIND_KNIGHT, 0);

            script.expectBoard("p1",
                    TestCards.expectedMinionWithFlags(TestCards.STORMWIND_KNIGHT, 2, 5));

            script.attack("p1:0", "p2:hero");

            script.playNonMinionCard("p1", TestCards.CONE_OF_COLD, "p1:0");

            script.expectBoard("p1",
                    TestCards.expectedMinionWithFlags(TestCards.STORMWIND_KNIGHT, 2, 4, "frozen"));

            script.endTurn();

            script.expectBoard("p1",
                    TestCards.expectedMinionWithFlags(TestCards.STORMWIND_KNIGHT, 2, 4, "frozen"));

            script.endTurn();

            script.expectBoard("p1",
                    TestCards.expectedMinionWithFlags(TestCards.STORMWIND_KNIGHT, 2, 4, "frozen"));

            script.endTurn();

            script.expectBoard("p1",
                    TestCards.expectedMinionWithFlags(TestCards.STORMWIND_KNIGHT, 2, 4));
        });
    }
}
