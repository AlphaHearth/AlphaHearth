package info.hearthsim.brazier;

import org.junit.Test;

public final class CardPlayedTest {
    @Test
    public void testTwoHobgoblins() {
        PlayScript.testScript((script) -> {
            script.setMana("p1", 10);
            script.playMinionCard("p1", TestCards.HOBGOBLIN, 0);
            script.playMinionCard("p1", TestCards.HOBGOBLIN, 1);
            script.playMinionCard("p1", TestCards.SLIME, 2);

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.HOBGOBLIN, 2, 3),
                    TestCards.expectedMinion(TestCards.HOBGOBLIN, 2, 3),
                    TestCards.expectedMinion(TestCards.SLIME, 5, 6));
        });
    }

    @Test
    public void testIllidanPlayToItsRightSide() {
        PlayScript.testScript((script) -> {
            script.setMana("p1", 10);
            script.playMinionCard("p1", TestCards.YETI, 0);
            script.playMinionCard("p1", TestCards.YETI, 0);
            script.setMana("p1", 10);
            script.playMinionCard("p1", TestCards.ILLIDAN_STORMRAGE, 1);

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.YETI, 4, 5),
                    TestCards.expectedMinion(TestCards.ILLIDAN_STORMRAGE, 7, 5),
                    TestCards.expectedMinion(TestCards.YETI, 4, 5));
            script.expectBoard("p2");

            script.playMinionCard("p1", TestCards.SLIME, 2);

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.YETI, 4, 5),
                    TestCards.expectedMinion(TestCards.ILLIDAN_STORMRAGE, 7, 5),
                    TestCards.expectedMinion(TestCards.FLAME_OF_AZZINOTH, 2, 1),
                    TestCards.expectedMinion(TestCards.SLIME, 1, 2),
                    TestCards.expectedMinion(TestCards.YETI, 4, 5));
            script.expectBoard("p2");
        });
    }

    @Test
    public void testIllidanPlayToItsLeftSide() {
        PlayScript.testScript((script) -> {
            script.setMana("p1", 10);
            script.playMinionCard("p1", TestCards.YETI, 0);
            script.playMinionCard("p1", TestCards.YETI, 0);
            script.setMana("p1", 10);
            script.playMinionCard("p1", TestCards.ILLIDAN_STORMRAGE, 1);

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.YETI, 4, 5),
                    TestCards.expectedMinion(TestCards.ILLIDAN_STORMRAGE, 7, 5),
                    TestCards.expectedMinion(TestCards.YETI, 4, 5));
            script.expectBoard("p2");

            script.playMinionCard("p1", TestCards.SLIME, 1);

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.YETI, 4, 5),
                    TestCards.expectedMinion(TestCards.SLIME, 1, 2),
                    TestCards.expectedMinion(TestCards.ILLIDAN_STORMRAGE, 7, 5),
                    TestCards.expectedMinion(TestCards.FLAME_OF_AZZINOTH, 2, 1),
                    TestCards.expectedMinion(TestCards.YETI, 4, 5));
            script.expectBoard("p2");
        });
    }

    @Test
    public void testIllidanIsNotAffectedByTheOpponent() {
        PlayScript.testScript((script) -> {
            script.setMana("p1", 10);
            script.setMana("p2", 10);

            script.playMinionCard("p1", TestCards.YETI, 0);
            script.playMinionCard("p1", TestCards.YETI, 0);
            script.setMana("p1", 10);
            script.playMinionCard("p1", TestCards.ILLIDAN_STORMRAGE, 1);

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.YETI, 4, 5),
                    TestCards.expectedMinion(TestCards.ILLIDAN_STORMRAGE, 7, 5),
                    TestCards.expectedMinion(TestCards.YETI, 4, 5));
            script.expectBoard("p2");

            script.playMinionCard("p2", TestCards.SLIME, 0);

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.YETI, 4, 5),
                    TestCards.expectedMinion(TestCards.ILLIDAN_STORMRAGE, 7, 5),
                    TestCards.expectedMinion(TestCards.YETI, 4, 5));
            script.expectBoard("p2",
                    TestCards.expectedMinion(TestCards.SLIME, 1, 2));
        });
    }
}
