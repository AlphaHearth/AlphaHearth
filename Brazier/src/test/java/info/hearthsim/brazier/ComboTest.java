package info.hearthsim.brazier;

import org.junit.Test;

public final class ComboTest {
    @Test
    public void testEviscerateNoCombo() {
        PlayScript.testScript((script) -> {
            script.setMana("p1", 10);
            script.playMinionCard("p1", TestCards.YETI, 0);
            script.setCurrentPlayer("p1");

            script.expectBoard("p1", TestCards.expectedMinion(TestCards.YETI, 4, 5));

            script.endTurn();

            script.setMana("p2", 10);
            script.playNonMinionCard("p2", TestCards.EVISCERATE, "p1:0");

            script.expectBoard("p1", TestCards.expectedMinion(TestCards.YETI, 4, 3));
        });
    }

    @Test
    public void testEviscerateNoComboNextTurn() {
        PlayScript.testScript((script) -> {
            script.setMana("p1", 10);
            script.playMinionCard("p1", TestCards.YETI, 0);
            script.setCurrentPlayer("p1");

            script.expectBoard("p1", TestCards.expectedMinion(TestCards.YETI, 4, 5));

            script.endTurn();

            script.setMana("p2", 10);
            script.playNonMinionCard("p2", TestCards.MOONFIRE, "p1:hero");

            script.endTurn();
            script.endTurn();

            script.playNonMinionCard("p2", TestCards.EVISCERATE, "p1:0");

            script.expectBoard("p1", TestCards.expectedMinion(TestCards.YETI, 4, 3));
        });
    }

    @Test
    public void testEviscerateCombo() {
        PlayScript.testScript((script) -> {
            script.setMana("p1", 10);
            script.playMinionCard("p1", TestCards.YETI, 0);
            script.setCurrentPlayer("p1");

            script.expectBoard("p1", TestCards.expectedMinion(TestCards.YETI, 4, 5));

            script.endTurn();

            script.setMana("p2", 10);
            script.playNonMinionCard("p2", TestCards.MOONFIRE, "p1:hero");
            script.playNonMinionCard("p2", TestCards.EVISCERATE, "p1:0");

            script.expectBoard("p1", TestCards.expectedMinion(TestCards.YETI, 4, 1));
        });
    }

    @Test
    public void testDefiasRingLeaderCombo() {
        PlayScript.testScript((script) -> {
            script.setMana("p1", 10);
            script.setCurrentPlayer("p1");

            script.playMinionCard("p1", TestCards.FLAME_OF_AZZINOTH, 0);
            script.playMinionCard("p1", TestCards.SLIME, 1);

            script.playMinionCard("p1", TestCards.DEFIAS_RINGLEADER, 1);

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.FLAME_OF_AZZINOTH, 2, 1),
                    TestCards.expectedMinion(TestCards.DEFIAS_RINGLEADER, 2, 2),
                    TestCards.expectedMinion(TestCards.DEFIAS_BANDIT, 2, 1),
                    TestCards.expectedMinion(TestCards.SLIME, 1, 2));

            script.playMinionCard("p1", TestCards.BLUEGILL_WARRIOR, 0);

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.BLUEGILL_WARRIOR, 2, 1),
                    TestCards.expectedMinion(TestCards.FLAME_OF_AZZINOTH, 2, 1),
                    TestCards.expectedMinion(TestCards.DEFIAS_RINGLEADER, 2, 2),
                    TestCards.expectedMinion(TestCards.DEFIAS_BANDIT, 2, 1),
                    TestCards.expectedMinion(TestCards.SLIME, 1, 2));

            script.endTurn();

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.BLUEGILL_WARRIOR, 2, 1),
                    TestCards.expectedMinion(TestCards.FLAME_OF_AZZINOTH, 2, 1),
                    TestCards.expectedMinion(TestCards.DEFIAS_RINGLEADER, 2, 2),
                    TestCards.expectedMinion(TestCards.DEFIAS_BANDIT, 2, 1),
                    TestCards.expectedMinion(TestCards.SLIME, 1, 2));
        });
    }

    @Test
    public void testDefiasRingLeaderNoCombo() {
        PlayScript.testScript((script) -> {
            script.setMana("p1", 10);
            script.setCurrentPlayer("p1");

            script.playMinionCard("p1", TestCards.DEFIAS_RINGLEADER, 0);
            script.playMinionCard("p1", TestCards.FLAME_OF_AZZINOTH, 0);
            script.playMinionCard("p1", TestCards.SLIME, 2);

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.FLAME_OF_AZZINOTH, 2, 1),
                    TestCards.expectedMinion(TestCards.DEFIAS_RINGLEADER, 2, 2),
                    TestCards.expectedMinion(TestCards.SLIME, 1, 2));

            script.playMinionCard("p1", TestCards.BLUEGILL_WARRIOR, 0);

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.BLUEGILL_WARRIOR, 2, 1),
                    TestCards.expectedMinion(TestCards.FLAME_OF_AZZINOTH, 2, 1),
                    TestCards.expectedMinion(TestCards.DEFIAS_RINGLEADER, 2, 2),
                    TestCards.expectedMinion(TestCards.SLIME, 1, 2));

            script.endTurn();

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.BLUEGILL_WARRIOR, 2, 1),
                    TestCards.expectedMinion(TestCards.FLAME_OF_AZZINOTH, 2, 1),
                    TestCards.expectedMinion(TestCards.DEFIAS_RINGLEADER, 2, 2),
                    TestCards.expectedMinion(TestCards.SLIME, 1, 2));
        });
    }
}
