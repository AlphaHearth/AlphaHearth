package info.hearthsim.brazier;

import org.junit.Test;

public final class ReversingSwitchTest {
    @Test
    public void testReverseWithStormwind() {
        PlayScript.testScript((script) -> {
            script.setMana("p1", 10);
            script.playMinionCard("p1", TestCards.EMPEROR_COBRA, 0);
            script.playMinionCard("p1", TestCards.STORMWIND_CHAMPION, 1);

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.EMPEROR_COBRA, 3, 4),
                    TestCards.expectedMinion(TestCards.STORMWIND_CHAMPION, 6, 6));

            script.setMana("p1", 10);

            script.playNonMinionCard("p1", TestCards.REVERSING_SWITCH, "p1:0");

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.EMPEROR_COBRA, 5, 4),
                    TestCards.expectedMinion(TestCards.STORMWIND_CHAMPION, 6, 6));
        });
    }

    @Test
    public void testReverseWithAbusive() {
        PlayScript.testScript((script) -> {
            script.setCurrentPlayer("p1");
            script.setMana("p1", 10);
            script.playMinionCard("p1", TestCards.EMPEROR_COBRA, 0);
            script.playMinionCard("p1", TestCards.ABUSIVE_SERGEANT, 1, "p1:0");

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.EMPEROR_COBRA, 4, 3),
                    TestCards.expectedMinion(TestCards.ABUSIVE_SERGEANT, 2, 1));

            script.playNonMinionCard("p1", TestCards.REVERSING_SWITCH, "p1:0");

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.EMPEROR_COBRA, 3, 4),
                    TestCards.expectedMinion(TestCards.ABUSIVE_SERGEANT, 2, 1));

            script.endTurn();

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.EMPEROR_COBRA, 3, 4),
                    TestCards.expectedMinion(TestCards.ABUSIVE_SERGEANT, 2, 1));
        });
    }
}
