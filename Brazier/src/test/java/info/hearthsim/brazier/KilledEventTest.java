package info.hearthsim.brazier;

import jdk.nashorn.internal.ir.annotations.Ignore;
import org.junit.Test;

public final class KilledEventTest {
    @Test
    public void testSoulOfTheForest() {
        PlayScript.testScript((script) -> {
            script.setMana("p2", 10);
            script.playMinionCard("p2", TestCards.BLUEGILL_WARRIOR, 0);
            script.playMinionCard("p2", TestCards.HAUNTED_CREEPER, 1);

            script.expectBoard("p2",
                    TestCards.expectedMinion(TestCards.BLUEGILL_WARRIOR, 2, 1),
                    TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2));

            script.setMana("p2", 10);
            script.playCard("p2", TestCards.SOUL_OF_THE_FOREST);

            script.playMinionCard("p2", TestCards.BLUEGILL_WARRIOR, 2);

            script.expectBoard("p2",
                    TestCards.expectedMinion(TestCards.BLUEGILL_WARRIOR, 2, 1),
                    TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
                    TestCards.expectedMinion(TestCards.BLUEGILL_WARRIOR, 2, 1));

            script.setMana("p1", 10);
            script.playCard("p1", TestCards.FLAMESTRIKE);

            script.expectBoard("p1");
            script.expectBoard("p2",
                    TestCards.expectedMinion(TestCards.SPECTRAL_SPIDER, 1, 1),
                    TestCards.expectedMinion(TestCards.SPECTRAL_SPIDER, 1, 1),
                    TestCards.expectedMinion(TestCards.TREANT, 2, 2),
                    TestCards.expectedMinion(TestCards.TREANT, 2, 2));
        });
    }

    @Test
    public void testHauntedCreeper() {
        PlayScript.testScript((script) -> {
            script.setMana("p2", 10);
            script.playMinionCard("p2", TestCards.HAUNTED_CREEPER, 0);

            script.expectBoard("p2",
                    TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2));

            script.setMana("p1", 10);
            script.playNonMinionCard("p1", TestCards.MOONFIRE, "p2:0");
            script.playNonMinionCard("p1", TestCards.MOONFIRE, "p2:0");

            script.expectBoard("p2",
                    TestCards.expectedMinion(TestCards.SPECTRAL_SPIDER, 1, 1),
                    TestCards.expectedMinion(TestCards.SPECTRAL_SPIDER, 1, 1));
        });
    }

    @Test
    public void testHauntedCreeperWithFullBoard() {
        PlayScript.testScript((script) -> {
            script.setMana("p2", 10);
            script.playMinionCard("p2", TestCards.HAUNTED_CREEPER, 0);
            script.playMinionCard("p2", TestCards.HAUNTED_CREEPER, 1);
            script.playMinionCard("p2", TestCards.HAUNTED_CREEPER, 2);
            script.playMinionCard("p2", TestCards.HAUNTED_CREEPER, 3);
            script.setMana("p2", 10);
            script.playMinionCard("p2", TestCards.HAUNTED_CREEPER, 4);
            script.playMinionCard("p2", TestCards.HAUNTED_CREEPER, 5);
            script.playMinionCard("p2", TestCards.HAUNTED_CREEPER, 6);

            script.expectBoard("p2",
                    TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
                    TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
                    TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
                    TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
                    TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
                    TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
                    TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2));

            script.setMana("p1", 10);
            script.playNonMinionCard("p1", TestCards.MOONFIRE, "p2:0");
            script.playNonMinionCard("p1", TestCards.MOONFIRE, "p2:0");

            script.expectBoard("p2",
                    TestCards.expectedMinion(TestCards.SPECTRAL_SPIDER, 1, 1),
                    TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
                    TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
                    TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
                    TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
                    TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
                    TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2));
        });
    }

    @Test
    public void testHauntedCreeperWithFullBoardBoardClear() {
        PlayScript.testScript((script) -> {
            script.setMana("p2", 10);
            script.playMinionCard("p2", TestCards.HAUNTED_CREEPER, 0);
            script.playMinionCard("p2", TestCards.HAUNTED_CREEPER, 1);
            script.playMinionCard("p2", TestCards.HAUNTED_CREEPER, 2);
            script.playMinionCard("p2", TestCards.HAUNTED_CREEPER, 3);
            script.setMana("p2", 10);
            script.playMinionCard("p2", TestCards.HAUNTED_CREEPER, 4);
            script.playMinionCard("p2", TestCards.HAUNTED_CREEPER, 5);
            script.playMinionCard("p2", TestCards.HAUNTED_CREEPER, 6);

            script.expectBoard("p2",
                    TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
                    TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
                    TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
                    TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
                    TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
                    TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
                    TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2));

            script.setMana("p1", 10);
            script.playCard("p1", TestCards.FLAMESTRIKE);

            script.expectBoard("p2",
                    TestCards.expectedMinion(TestCards.SPECTRAL_SPIDER, 1, 1),
                    TestCards.expectedMinion(TestCards.SPECTRAL_SPIDER, 1, 1),
                    TestCards.expectedMinion(TestCards.SPECTRAL_SPIDER, 1, 1),
                    TestCards.expectedMinion(TestCards.SPECTRAL_SPIDER, 1, 1),
                    TestCards.expectedMinion(TestCards.SPECTRAL_SPIDER, 1, 1),
                    TestCards.expectedMinion(TestCards.SPECTRAL_SPIDER, 1, 1),
                    TestCards.expectedMinion(TestCards.SPECTRAL_SPIDER, 1, 1));
        });
    }

    @Test
    public void testSludgeBelcherDeathRattleWorksWithFullBoard() {
        PlayScript.testScript((script) -> {
            script.setMana("p2", 10);
            script.playMinionCard("p2", TestCards.SLUDGE_BELCHER, 0);
            script.playMinionCard("p2", TestCards.SLUDGE_BELCHER, 1);
            script.setMana("p2", 10);
            script.playMinionCard("p2", TestCards.SLUDGE_BELCHER, 2);
            script.playMinionCard("p2", TestCards.SLUDGE_BELCHER, 3);
            script.setMana("p2", 10);
            script.playMinionCard("p2", TestCards.SLUDGE_BELCHER, 4);
            script.playMinionCard("p2", TestCards.SLUDGE_BELCHER, 5);
            script.setMana("p2", 10);
            script.playMinionCard("p2", TestCards.SLUDGE_BELCHER, 6);

            script.expectBoard("p2",
                    TestCards.expectedMinion(TestCards.SLUDGE_BELCHER, 3, 5),
                    TestCards.expectedMinion(TestCards.SLUDGE_BELCHER, 3, 5),
                    TestCards.expectedMinion(TestCards.SLUDGE_BELCHER, 3, 5),
                    TestCards.expectedMinion(TestCards.SLUDGE_BELCHER, 3, 5),
                    TestCards.expectedMinion(TestCards.SLUDGE_BELCHER, 3, 5),
                    TestCards.expectedMinion(TestCards.SLUDGE_BELCHER, 3, 5),
                    TestCards.expectedMinion(TestCards.SLUDGE_BELCHER, 3, 5));

            script.setMana("p1", 10);
            script.playNonMinionCard("p1", TestCards.MOONFIRE, "p2:0");
            script.playNonMinionCard("p1", TestCards.MOONFIRE, "p2:0");
            script.playNonMinionCard("p1", TestCards.MOONFIRE, "p2:0");
            script.playNonMinionCard("p1", TestCards.MOONFIRE, "p2:0");
            script.playNonMinionCard("p1", TestCards.MOONFIRE, "p2:0");

            script.expectBoard("p2",
                    TestCards.expectedMinion(TestCards.SLIME, 1, 2),
                    TestCards.expectedMinion(TestCards.SLUDGE_BELCHER, 3, 5),
                    TestCards.expectedMinion(TestCards.SLUDGE_BELCHER, 3, 5),
                    TestCards.expectedMinion(TestCards.SLUDGE_BELCHER, 3, 5),
                    TestCards.expectedMinion(TestCards.SLUDGE_BELCHER, 3, 5),
                    TestCards.expectedMinion(TestCards.SLUDGE_BELCHER, 3, 5),
                    TestCards.expectedMinion(TestCards.SLUDGE_BELCHER, 3, 5));
        });
    }

    @Test
    public void testSludgeBelcher() {
        PlayScript.testScript((script) -> {
            script.setMana("p2", 10);
            script.playMinionCard("p2", TestCards.SLUDGE_BELCHER, 0);
            script.setMana("p2", 10);
            script.playMinionCard("p2", TestCards.YETI, 0);
            script.playMinionCard("p2", TestCards.YETI, 2);

            script.setMana("p1", 10);
            script.playMinionCard("p1", TestCards.YETI, 0);
            script.playMinionCard("p1", TestCards.YETI, 1);

            script.refreshAttacks();

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.YETI, 4, 5),
                    TestCards.expectedMinion(TestCards.YETI, 4, 5));

            script.expectBoard("p2",
                    TestCards.expectedMinion(TestCards.YETI, 4, 5),
                    TestCards.expectedMinion(TestCards.SLUDGE_BELCHER, 3, 5),
                    TestCards.expectedMinion(TestCards.YETI, 4, 5));

            script.attack("p1:0", "p2:1"); // YETI 1 -> SLUDGE

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.YETI, 4, 2),
                    TestCards.expectedMinion(TestCards.YETI, 4, 5));

            script.expectBoard("p2",
                    TestCards.expectedMinion(TestCards.YETI, 4, 5),
                    TestCards.expectedMinion(TestCards.SLUDGE_BELCHER, 3, 1),
                    TestCards.expectedMinion(TestCards.YETI, 4, 5));

            script.attack("p1:1", "p2:1"); // YETI 2 -> SLUDGE

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.YETI, 4, 2),
                    TestCards.expectedMinion(TestCards.YETI, 4, 2));

            script.expectBoard("p2",
                    TestCards.expectedMinion(TestCards.YETI, 4, 5),
                    TestCards.expectedMinion(TestCards.SLIME, 1, 2),
                    TestCards.expectedMinion(TestCards.YETI, 4, 5));

            script.refreshAttacks();

            script.attack("p1:0", "p2:1"); // YETI 1 -> SLIME

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.YETI, 4, 1),
                    TestCards.expectedMinion(TestCards.YETI, 4, 2));

            script.expectBoard("p2",
                    TestCards.expectedMinion(TestCards.YETI, 4, 5),
                    TestCards.expectedMinion(TestCards.YETI, 4, 5));
        });
    }

    @Test
    public void testDeathsBite() {
        PlayScript.testScript((script) -> {
            script.setMana("p1", 10);
            script.setMana("p2", 10);

            script.playCard("p1", TestCards.DEATHS_BITE);
            script.playMinionCard("p1", TestCards.YETI, 0);
            script.playMinionCard("p2", TestCards.FROTHING_BERSERKER, 0);
            script.playMinionCard("p2", TestCards.YETI, 0);

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.YETI, 4, 5));
            script.expectBoard("p2",
                    TestCards.expectedMinion(TestCards.YETI, 4, 5),
                    TestCards.expectedMinion(TestCards.FROTHING_BERSERKER, 2, 4));

            script.attack("p1:hero", "p2:hero");

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.YETI, 4, 5));
            script.expectBoard("p2",
                    TestCards.expectedMinion(TestCards.YETI, 4, 5),
                    TestCards.expectedMinion(TestCards.FROTHING_BERSERKER, 2, 4));

            script.expectHeroHp("p1", 30, 0);
            script.expectHeroHp("p2", 26, 0);

            script.refreshAttacks();

            script.attack("p1:hero", "p2:0");

            script.expectHeroHp("p1", 26, 0);
            script.expectHeroHp("p2", 26, 0);

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.YETI, 4, 4));
            script.expectBoard("p2",
                    TestCards.expectedMinion(TestCards.FROTHING_BERSERKER, 6, 3));
        });
    }

    @Test
    public void testDeathsBiteKilledByJaraxxus() {
        PlayScript.testScript((script) -> {
            script.setMana("p1", 10);

            script.playCard("p1", TestCards.DEATHS_BITE);
            script.playMinionCard("p1", TestCards.YETI, 0);

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.YETI, 4, 5));

            script.setMana("p1", 10);
            script.playMinionCard("p1", TestCards.JARAXXUS, 0);

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.YETI, 4, 4));

            script.expectWeapon("p1", 3, 8);
        });
    }
}
