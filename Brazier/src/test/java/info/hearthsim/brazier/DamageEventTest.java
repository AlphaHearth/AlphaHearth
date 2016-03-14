package info.hearthsim.brazier;

import org.junit.Test;

public final class DamageEventTest {
    @Test
    public void testWhirlwind() {
        PlayScript.testScript((script) -> {
            script.expectHeroHp("p1", 30, 0);
            script.expectHeroHp("p2", 30, 0);

            script.setMana("p1", 10);
            script.playMinionCard("p1", TestCards.FROTHING_BERSERKER, 0);
            script.playMinionCard("p1", TestCards.YETI, 0);
            script.setMana("p1", 10);
            script.playMinionCard("p1", TestCards.YETI, 0);
            script.playMinionCard("p1", TestCards.YETI, 0);
            script.setMana("p1", 10);
            script.playMinionCard("p1", TestCards.YETI, 0);
            script.playMinionCard("p1", TestCards.YETI, 0);
            script.setMana("p1", 10);
            script.playMinionCard("p1", TestCards.FROTHING_BERSERKER, 0);

            script.setMana("p2", 10);
            script.playMinionCard("p2", TestCards.YETI, 0);
            script.playMinionCard("p2", TestCards.YETI, 0);
            script.setMana("p2", 10);
            script.playMinionCard("p2", TestCards.YETI, 0);
            script.playMinionCard("p2", TestCards.YETI, 0);
            script.setMana("p2", 10);
            script.playMinionCard("p2", TestCards.YETI, 0);
            script.playMinionCard("p2", TestCards.YETI, 0);
            script.setMana("p2", 10);
            script.playMinionCard("p2", TestCards.YETI, 0);

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.FROTHING_BERSERKER, 2, 4),
                    TestCards.expectedMinion(TestCards.YETI, 4, 5),
                    TestCards.expectedMinion(TestCards.YETI, 4, 5),
                    TestCards.expectedMinion(TestCards.YETI, 4, 5),
                    TestCards.expectedMinion(TestCards.YETI, 4, 5),
                    TestCards.expectedMinion(TestCards.YETI, 4, 5),
                    TestCards.expectedMinion(TestCards.FROTHING_BERSERKER, 2, 4));

            script.expectBoard("p2",
                    TestCards.expectedMinion(TestCards.YETI, 4, 5),
                    TestCards.expectedMinion(TestCards.YETI, 4, 5),
                    TestCards.expectedMinion(TestCards.YETI, 4, 5),
                    TestCards.expectedMinion(TestCards.YETI, 4, 5),
                    TestCards.expectedMinion(TestCards.YETI, 4, 5),
                    TestCards.expectedMinion(TestCards.YETI, 4, 5),
                    TestCards.expectedMinion(TestCards.YETI, 4, 5));

            script.playCard("p1", TestCards.WHIRLWIND);

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.FROTHING_BERSERKER, 16, 3),
                    TestCards.expectedMinion(TestCards.YETI, 4, 4),
                    TestCards.expectedMinion(TestCards.YETI, 4, 4),
                    TestCards.expectedMinion(TestCards.YETI, 4, 4),
                    TestCards.expectedMinion(TestCards.YETI, 4, 4),
                    TestCards.expectedMinion(TestCards.YETI, 4, 4),
                    TestCards.expectedMinion(TestCards.FROTHING_BERSERKER, 16, 3));

            script.expectBoard("p2",
                    TestCards.expectedMinion(TestCards.YETI, 4, 4),
                    TestCards.expectedMinion(TestCards.YETI, 4, 4),
                    TestCards.expectedMinion(TestCards.YETI, 4, 4),
                    TestCards.expectedMinion(TestCards.YETI, 4, 4),
                    TestCards.expectedMinion(TestCards.YETI, 4, 4),
                    TestCards.expectedMinion(TestCards.YETI, 4, 4),
                    TestCards.expectedMinion(TestCards.YETI, 4, 4));

            script.expectHeroHp("p1", 30, 0);
            script.expectHeroHp("p2", 30, 0);
        });
    }

    @Test
    public void testFrothingBerserkerVsForthingBerserker() {
        PlayScript.testScript((script) -> {
            script.setMana("p1", 10);
            script.setMana("p2", 10);

            script.playMinionCard("p1", TestCards.FROTHING_BERSERKER, 0);
            script.playMinionCard("p1", TestCards.FROTHING_BERSERKER, 0);
            script.playMinionCard("p2", TestCards.FROTHING_BERSERKER, 0);

            script.refreshAttacks();

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.FROTHING_BERSERKER, 2, 4),
                    TestCards.expectedMinion(TestCards.FROTHING_BERSERKER, 2, 4));

            script.expectBoard("p2",
                    TestCards.expectedMinion(TestCards.FROTHING_BERSERKER, 2, 4));

            script.attack("p1:0", "p2:0");

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.FROTHING_BERSERKER, 4, 2),
                    TestCards.expectedMinion(TestCards.FROTHING_BERSERKER, 4, 4));

            script.expectBoard("p2",
                    TestCards.expectedMinion(TestCards.FROTHING_BERSERKER, 4, 2));
        });
    }

    @Test
    public void testGurubashiBerserker() {
        PlayScript.testScript((script) -> {
            script.setMana("p1", 10);
            script.setMana("p2", 10);

            script.playMinionCard("p1", TestCards.GURUBASHI_BERSERKER, 0);
            script.playMinionCard("p1", TestCards.YETI, 0);
            script.playMinionCard("p2", TestCards.YETI, 0);
            script.playMinionCard("p2", TestCards.YETI, 0);

            script.refreshAttacks();

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.YETI, 4, 5),
                    TestCards.expectedMinion(TestCards.GURUBASHI_BERSERKER, 2, 7));

            script.expectBoard("p2",
                    TestCards.expectedMinion(TestCards.YETI, 4, 5),
                    TestCards.expectedMinion(TestCards.YETI, 4, 5));

            script.attack("p2:0", "p1:0");

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.YETI, 4, 1),
                    TestCards.expectedMinion(TestCards.GURUBASHI_BERSERKER, 2, 7));

            script.expectBoard("p2",
                    TestCards.expectedMinion(TestCards.YETI, 4, 1),
                    TestCards.expectedMinion(TestCards.YETI, 4, 5));

            script.attack("p2:1", "p1:1");

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.YETI, 4, 1),
                    TestCards.expectedMinion(TestCards.GURUBASHI_BERSERKER, 5, 3));

            script.expectBoard("p2",
                    TestCards.expectedMinion(TestCards.YETI, 4, 1),
                    TestCards.expectedMinion(TestCards.YETI, 4, 3));
        });
    }

    @Test
    public void testGrimPatron() {
        PlayScript.testScript((script) -> {
            script.setMana("p1", 10);
            script.setMana("p2", 10);

            script.playMinionCard("p1", TestCards.GRIM_PATRON, 0);
            script.playMinionCard("p1", TestCards.GRIM_PATRON, 0);
            script.playMinionCard("p2", TestCards.GRIM_PATRON, 0);

            script.setMana("p1", 10);
            script.setMana("p2", 10);

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 3),  // 2.
                    TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 3)); // 1.

            script.expectBoard("p2",
                    TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 3)); // 1.

            script.playCard("p2", TestCards.WHIRLWIND);

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 2),  // 2.
                    TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 3),  // 4.
                    TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 2),  // 1.
                    TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 3)); // 3.

            script.expectBoard("p2",
                    TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 2),  // 1.
                    TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 3)); // 2.

            script.playCard("p2", TestCards.WHIRLWIND);

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 1),  // 2.
                    TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 3),  // 6.
                    TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 2),  // 4.
                    TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 1),  // 1.
                    TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 3),  // 5.
                    TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 2),  // 3.
                    TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 3)); // 7.

            script.expectBoard("p2",
                    TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 1),  // 1.
                    TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 3),  // 3.
                    TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 2),  // 2.
                    TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 3)); // 4.

            script.playCard("p2", TestCards.WHIRLWIND);

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 2),  // 6.
                    TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 1),  // 4.
                    TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 2),  // 5.
                    TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 1),  // 3.
                    TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 2)); // 7.

            script.expectBoard("p2",
                    TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 2),  // 3.
                    TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 3),  // 6.
                    TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 1),  // 2.
                    TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 3),  // 5.
                    TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 2),  // 7.
                    TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 3)); // 4.
        });
    }

    @Test
    public void testExplosiveShotAtomic() {
        PlayScript.testScript((script) -> {
            script.setMana("p1", 10);

            script.playMinionCard("p1", TestCards.VOIDWALKER, 0);
            script.playMinionCard("p1", TestCards.GRIM_PATRON, 1);
            script.playMinionCard("p1", TestCards.VOIDWALKER, 2);

            script.setMana("p1", 10);
            script.playNonMinionCard("p1", TestCards.BLESSING_OF_KINGS, "p1:1");

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.VOIDWALKER, 1, 3),
                    TestCards.expectedMinion(TestCards.GRIM_PATRON, 7, 7),
                    TestCards.expectedMinion(TestCards.VOIDWALKER, 1, 3));

            script.setMana("p2", 10);
            script.playNonMinionCard("p2", TestCards.EXPLOSIVE_SHOT, "p1:1");

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.VOIDWALKER, 1, 1),
                    TestCards.expectedMinion(TestCards.GRIM_PATRON, 7, 2),
                    TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 3),
                    TestCards.expectedMinion(TestCards.VOIDWALKER, 1, 1));
        });
    }

    @Test
    public void testEmperorCobra() {
        PlayScript.testScript((script) -> {
            script.setMana("p1", 10);
            script.setMana("p2", 10);

            script.playMinionCard("p1", TestCards.EMPEROR_COBRA, 0);
            script.playMinionCard("p2", TestCards.STORMWIND_CHAMPION, 0);

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.EMPEROR_COBRA, 2, 3));
            script.expectBoard("p2",
                    TestCards.expectedMinion(TestCards.STORMWIND_CHAMPION, 6, 6));

            script.refreshAttacks();
            script.attack("p1:0", "p2:0");

            script.expectBoard("p1");
            script.expectBoard("p2");
        });
    }

    @Test
    public void testWaterElementalAttacks() {
        PlayScript.testScript((script) -> {
            script.setMana("p1", 10);
            script.setMana("p2", 10);

            script.playMinionCard("p1", TestCards.WATER_ELEMENTAL, 0);
            script.playMinionCard("p2", TestCards.SLUDGE_BELCHER, 0);

            script.setCurrentPlayer("p1");
            script.refreshAttacks();

            script.expectBoard("p1",
                    TestCards.expectedMinionWithFlags(TestCards.WATER_ELEMENTAL, 3, 6));
            script.expectBoard("p2",
                    TestCards.expectedMinionWithFlags(TestCards.SLUDGE_BELCHER, 3, 5, "taunt"));

            script.attack("p1:0", "p2:0");

            script.expectBoard("p1",
                    TestCards.expectedMinionWithFlags(TestCards.WATER_ELEMENTAL, 3, 3));
            script.expectBoard("p2",
                    TestCards.expectedMinionWithFlags(TestCards.SLUDGE_BELCHER, 3, 2, "taunt", "frozen"));

            script.endTurn(); // p1

            script.expectBoard("p1",
                    TestCards.expectedMinionWithFlags(TestCards.WATER_ELEMENTAL, 3, 3));
            script.expectBoard("p2",
                    TestCards.expectedMinionWithFlags(TestCards.SLUDGE_BELCHER, 3, 2, "taunt", "frozen"));

            script.endTurn(); // p2
            script.endTurn(); // p1

            script.expectBoard("p1",
                    TestCards.expectedMinionWithFlags(TestCards.WATER_ELEMENTAL, 3, 3));
            script.expectBoard("p2",
                    TestCards.expectedMinionWithFlags(TestCards.SLUDGE_BELCHER, 3, 2, "taunt"));
        });
    }

    @Test
    public void testWaterElementalAttacked() {
        PlayScript.testScript((script) -> {
            script.setMana("p1", 10);
            script.setMana("p2", 10);

            script.playMinionCard("p1", TestCards.WATER_ELEMENTAL, 0);
            script.playMinionCard("p2", TestCards.SLUDGE_BELCHER, 0);

            script.setCurrentPlayer("p2");
            script.refreshAttacks();

            script.expectBoard("p1",
                    TestCards.expectedMinionWithFlags(TestCards.WATER_ELEMENTAL, 3, 6));
            script.expectBoard("p2",
                    TestCards.expectedMinionWithFlags(TestCards.SLUDGE_BELCHER, 3, 5, "taunt"));

            script.attack("p2:0", "p1:0");

            script.expectBoard("p1",
                    TestCards.expectedMinionWithFlags(TestCards.WATER_ELEMENTAL, 3, 3));
            script.expectBoard("p2",
                    TestCards.expectedMinionWithFlags(TestCards.SLUDGE_BELCHER, 3, 2, "taunt", "frozen"));

            script.endTurn(); // p2
            script.endTurn(); // p1

            script.expectBoard("p1",
                    TestCards.expectedMinionWithFlags(TestCards.WATER_ELEMENTAL, 3, 3));
            script.expectBoard("p2",
                    TestCards.expectedMinionWithFlags(TestCards.SLUDGE_BELCHER, 3, 2, "taunt", "frozen"));

            script.endTurn(); // p2
            script.endTurn(); // p1

            script.expectBoard("p1",
                    TestCards.expectedMinionWithFlags(TestCards.WATER_ELEMENTAL, 3, 3));
            script.expectBoard("p2",
                    TestCards.expectedMinionWithFlags(TestCards.SLUDGE_BELCHER, 3, 2, "taunt"));
        });
    }
}
