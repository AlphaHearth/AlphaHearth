package info.hearthsim.brazier;

import org.junit.Test;

public final class TargetedBattleCryTest {
    @Test
    public void testFireElemental() {
        PlayScript.testScript((script) -> {
            script.setMana("p1", 10);
            script.setMana("p2", 10);

            script.playMinionCard("p2", TestCards.YETI, 0);

            script.expectBoard("p1");
            script.expectBoard("p2",
                    TestCards.expectedMinion(TestCards.YETI, 4, 5));

            script.playMinionCard("p1", TestCards.FIRE_ELEMENTAL, 0, "p2:0");

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.FIRE_ELEMENTAL, 6, 5));
            script.expectBoard("p2",
                    TestCards.expectedMinion(TestCards.YETI, 4, 2));
        });
    }
}
