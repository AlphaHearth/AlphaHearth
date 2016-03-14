package info.hearthsim.brazier;

import org.junit.Test;

import static info.hearthsim.brazier.TestCards.*;

public final class TmpBuffTest {
    @Test
    public void testAbusiveSergeant() {
        PlayScript.testScript((script) -> {
            script.setMana("p1", 10);
            script.playMinionCard("p1", YETI, 0);
            script.playMinionCard("p1", ABUSIVE_SERGEANT, 1, "p1:0");

            script.expectBoard("p1",
                    expectedMinion(YETI, 6, 5),
                    expectedMinion(ABUSIVE_SERGEANT, 2, 1));

            script.endTurn();

            script.expectBoard("p1",
                    expectedMinion(YETI, 4, 5),
                    expectedMinion(ABUSIVE_SERGEANT, 2, 1));
        });
    }

    @Test
    public void testSilencedAbusiveSergeantBuff() {
        PlayScript.testScript((script) -> {
            script.setMana("p1", 10);
            script.playMinionCard("p1", YETI, 0);
            script.playMinionCard("p1", ABUSIVE_SERGEANT, 1, "p1:0");

            script.expectBoard("p1",
                    expectedMinion(YETI, 6, 5),
                    expectedMinion(ABUSIVE_SERGEANT, 2, 1));

            script.playNonMinionCard("p1", SILENCE, "p1:0");

            script.expectBoard("p1",
                    expectedMinion(YETI, 4, 5),
                    expectedMinion(ABUSIVE_SERGEANT, 2, 1));

            script.endTurn();

            script.expectBoard("p1",
                    expectedMinion(YETI, 4, 5),
                    expectedMinion(ABUSIVE_SERGEANT, 2, 1));
        });
    }
}
