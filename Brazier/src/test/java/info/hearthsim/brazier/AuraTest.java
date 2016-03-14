package info.hearthsim.brazier;

import org.junit.Test;

public final class AuraTest {
    @Test
    public void testDireWolfAlpha() {
        PlayScript.testScript((script) -> {
            script.setMana("p1", 10);
            script.setMana("p2", 10);

            script.playMinionCard("p1", TestCards.YETI, 0);
            script.playMinionCard("p1", TestCards.BLUEGILL_WARRIOR, 1);
            script.playMinionCard("p1", TestCards.SLIME, 2);
            script.playMinionCard("p1", TestCards.TREANT, 3);

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.YETI, 4, 5),
                    TestCards.expectedMinion(TestCards.BLUEGILL_WARRIOR, 2, 1),
                    TestCards.expectedMinion(TestCards.SLIME, 1, 2),
                    TestCards.expectedMinion(TestCards.TREANT, 2, 2));

            script.playMinionCard("p1", TestCards.DIRE_WOLF_ALPHA, 2);

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.YETI, 4, 5),
                    TestCards.expectedMinion(TestCards.BLUEGILL_WARRIOR, 3, 1),
                    TestCards.expectedMinion(TestCards.DIRE_WOLF_ALPHA, 2, 2),
                    TestCards.expectedMinion(TestCards.SLIME, 2, 2),
                    TestCards.expectedMinion(TestCards.TREANT, 2, 2));

            script.setMana("p2", 10);
            script.playNonMinionCard("p2", TestCards.PYROBLAST, "p1:1");

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.YETI, 5, 5),
                    TestCards.expectedMinion(TestCards.DIRE_WOLF_ALPHA, 2, 2),
                    TestCards.expectedMinion(TestCards.SLIME, 2, 2),
                    TestCards.expectedMinion(TestCards.TREANT, 2, 2));

            script.setMana("p2", 10);
            script.playNonMinionCard("p2", TestCards.PYROBLAST, "p1:2");

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.YETI, 5, 5),
                    TestCards.expectedMinion(TestCards.DIRE_WOLF_ALPHA, 2, 2),
                    TestCards.expectedMinion(TestCards.TREANT, 3, 2));

            script.setMana("p2", 10);
            script.playNonMinionCard("p2", TestCards.PYROBLAST, "p1:2");

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.YETI, 5, 5),
                    TestCards.expectedMinion(TestCards.DIRE_WOLF_ALPHA, 2, 2));

            script.setMana("p2", 10);
            script.playNonMinionCard("p2", TestCards.PYROBLAST, "p1:0");

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.DIRE_WOLF_ALPHA, 2, 2));
        });
    }

    @Test
    public void testDireWolfAlphaDies() {
        PlayScript.testScript((script) -> {
            script.setMana("p1", 10);

            script.playMinionCard("p1", TestCards.YETI, 0);
            script.playMinionCard("p1", TestCards.BLUEGILL_WARRIOR, 1);
            script.playMinionCard("p1", TestCards.DIRE_WOLF_ALPHA, 2);
            script.playMinionCard("p1", TestCards.SLIME, 3);
            script.playMinionCard("p1", TestCards.TREANT, 4);

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.YETI, 4, 5),
                    TestCards.expectedMinion(TestCards.BLUEGILL_WARRIOR, 3, 1),
                    TestCards.expectedMinion(TestCards.DIRE_WOLF_ALPHA, 2, 2),
                    TestCards.expectedMinion(TestCards.SLIME, 2, 2),
                    TestCards.expectedMinion(TestCards.TREANT, 2, 2));

            script.setMana("p2", 10);
            script.playNonMinionCard("p2", TestCards.PYROBLAST, "p1:2");

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.YETI, 4, 5),
                    TestCards.expectedMinion(TestCards.BLUEGILL_WARRIOR, 2, 1),
                    TestCards.expectedMinion(TestCards.SLIME, 1, 2),
                    TestCards.expectedMinion(TestCards.TREANT, 2, 2));
        });
    }

    @Test
    public void testWeeSpellstopper() {
        PlayScript.testScript((script) -> {
            script.setMana("p1", 10);
            script.setMana("p2", 10);

            script.playMinionCard("p1", TestCards.YETI, 0);
            script.playMinionCard("p1", TestCards.BLUEGILL_WARRIOR, 1);
            script.playMinionCard("p1", TestCards.SLIME, 2);
            script.playMinionCard("p1", TestCards.TREANT, 3);

            script.expectBoard("p1",
                    TestCards.expectedMinionWithFlags(TestCards.YETI, 4, 5),
                    TestCards.expectedMinionWithFlags(TestCards.BLUEGILL_WARRIOR, 2, 1),
                    TestCards.expectedMinionWithFlags(TestCards.SLIME, 1, 2, "taunt"),
                    TestCards.expectedMinionWithFlags(TestCards.TREANT, 2, 2));

            script.setMana("p1", 10);
            script.playMinionCard("p1", TestCards.WEE_SPELLSTOPPER, 2);

            script.expectBoard("p1",
                    TestCards.expectedMinionWithFlags(TestCards.YETI, 4, 5),
                    TestCards.expectedMinionWithFlags(TestCards.BLUEGILL_WARRIOR, 2, 1, "untargetable"),
                    TestCards.expectedMinionWithFlags(TestCards.WEE_SPELLSTOPPER, 2, 5),
                    TestCards.expectedMinionWithFlags(TestCards.SLIME, 1, 2, "taunt", "untargetable"),
                    TestCards.expectedMinionWithFlags(TestCards.TREANT, 2, 2));

            script.setMana("p2", 10);
            script.playNonMinionCard("p2", TestCards.PYROBLAST, "p1:1");

            script.expectBoard("p1",
                    TestCards.expectedMinionWithFlags(TestCards.YETI, 4, 5, "untargetable"),
                    TestCards.expectedMinionWithFlags(TestCards.WEE_SPELLSTOPPER, 2, 5),
                    TestCards.expectedMinionWithFlags(TestCards.SLIME, 1, 2, "taunt", "untargetable"),
                    TestCards.expectedMinionWithFlags(TestCards.TREANT, 2, 2));

            script.setMana("p2", 10);
            script.playNonMinionCard("p2", TestCards.PYROBLAST, "p1:2");

            script.expectBoard("p1",
                    TestCards.expectedMinionWithFlags(TestCards.YETI, 4, 5, "untargetable"),
                    TestCards.expectedMinionWithFlags(TestCards.WEE_SPELLSTOPPER, 2, 5),
                    TestCards.expectedMinionWithFlags(TestCards.TREANT, 2, 2, "untargetable"));

            script.setMana("p2", 10);
            script.playNonMinionCard("p2", TestCards.PYROBLAST, "p1:2");

            script.expectBoard("p1",
                    TestCards.expectedMinionWithFlags(TestCards.YETI, 4, 5, "untargetable"),
                    TestCards.expectedMinionWithFlags(TestCards.WEE_SPELLSTOPPER, 2, 5));

            script.setMana("p2", 10);
            script.playNonMinionCard("p2", TestCards.PYROBLAST, "p1:0");

            script.expectBoard("p1",
                    TestCards.expectedMinionWithFlags(TestCards.WEE_SPELLSTOPPER, 2, 5));
        });
    }

    @Test
    public void testWeeSpellStopperDies() {
        PlayScript.testScript((script) -> {
            script.setMana("p1", 10);

            script.playMinionCard("p1", TestCards.YETI, 0);
            script.playMinionCard("p1", TestCards.BLUEGILL_WARRIOR, 1);
            script.setMana("p1", 10);
            script.playMinionCard("p1", TestCards.WEE_SPELLSTOPPER, 2);
            script.playMinionCard("p1", TestCards.SLIME, 3);
            script.playMinionCard("p1", TestCards.TREANT, 4);

            script.setMana("p2", 10);
            script.playMinionCard("p2", TestCards.BLUEGILL_WARRIOR, 0);

            script.attack("p2:0", "p1:2");

            script.expectBoard("p1",
                    TestCards.expectedMinionWithFlags(TestCards.YETI, 4, 5),
                    TestCards.expectedMinionWithFlags(TestCards.BLUEGILL_WARRIOR, 2, 1, "untargetable"),
                    TestCards.expectedMinionWithFlags(TestCards.WEE_SPELLSTOPPER, 2, 3),
                    TestCards.expectedMinionWithFlags(TestCards.SLIME, 1, 2, "taunt", "untargetable"),
                    TestCards.expectedMinionWithFlags(TestCards.TREANT, 2, 2));

            script.setMana("p2", 10);
            script.playNonMinionCard("p2", TestCards.PYROBLAST, "p1:2");

            script.expectBoard("p1",
                    TestCards.expectedMinionWithFlags(TestCards.YETI, 4, 5),
                    TestCards.expectedMinionWithFlags(TestCards.BLUEGILL_WARRIOR, 2, 1),
                    TestCards.expectedMinionWithFlags(TestCards.SLIME, 1, 2, "taunt"),
                    TestCards.expectedMinionWithFlags(TestCards.TREANT, 2, 2));
        });
    }

    @Test
    public void testHealWithHuntersMark() {
        PlayScript.testScript((script) -> {
            script.setMana("p1", 10);

            script.playMinionCard("p1", TestCards.WISP, 0);
            script.playMinionCard("p1", TestCards.STORMWIND_CHAMPION, 1);
            script.playNonMinionCard("p1", TestCards.MOONFIRE, "p1:0");

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.WISP, 2, 1),
                    TestCards.expectedMinion(TestCards.STORMWIND_CHAMPION, 6, 6));

            script.playNonMinionCard("p1", TestCards.HUNTERS_MARK, "p1:0");

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.WISP, 2, 2),
                    TestCards.expectedMinion(TestCards.STORMWIND_CHAMPION, 6, 6));
        });
    }

    @Test
    public void testStormwindChampion() {
        PlayScript.testScript((script) -> {
            script.setMana("p1", 10);

            script.playMinionCard("p1", TestCards.YETI, 0);
            script.playMinionCard("p1", TestCards.BLUEGILL_WARRIOR, 1);
            script.setMana("p1", 10);
            script.playMinionCard("p1", TestCards.STORMWIND_CHAMPION, 2);
            script.playMinionCard("p1", TestCards.SLIME, 3);
            script.playMinionCard("p1", TestCards.TREANT, 4);

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.YETI, 5, 6),
                    TestCards.expectedMinion(TestCards.BLUEGILL_WARRIOR, 3, 2),
                    TestCards.expectedMinion(TestCards.STORMWIND_CHAMPION, 6, 6),
                    TestCards.expectedMinion(TestCards.SLIME, 2, 3),
                    TestCards.expectedMinion(TestCards.TREANT, 3, 3));

            script.setMana("p2", 10);
            script.playNonMinionCard("p2", TestCards.PYROBLAST, "p1:2");

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.YETI, 4, 5),
                    TestCards.expectedMinion(TestCards.BLUEGILL_WARRIOR, 2, 1),
                    TestCards.expectedMinion(TestCards.SLIME, 1, 2),
                    TestCards.expectedMinion(TestCards.TREANT, 2, 2));
        });
    }

    @Test
    public void testStormwindChampionUpdatesAuraAfterDeathRattle() {
        PlayScript.testScript((script) -> {
            script.setMana("p1", 10);

            script.playMinionCard("p1", TestCards.SLUDGE_BELCHER, 0);
            script.setMana("p1", 10);
            script.playMinionCard("p1", TestCards.STORMWIND_CHAMPION, 1);

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.SLUDGE_BELCHER, 4, 6),
                    TestCards.expectedMinion(TestCards.STORMWIND_CHAMPION, 6, 6));

            script.setMana("p2", 10);
            script.playNonMinionCard("p2", TestCards.PYROBLAST, "p1:0");

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.SLIME, 2, 3),
                    TestCards.expectedMinion(TestCards.STORMWIND_CHAMPION, 6, 6));
        });
    }

    @Test
    public void testKillingStormwindChampionDoesNotReduceCurrentHealth() {
        PlayScript.testScript((script) -> {
            script.setMana("p1", 10);

            script.playMinionCard("p1", TestCards.YETI, 0);
            script.playMinionCard("p1", TestCards.BLUEGILL_WARRIOR, 1);
            script.setMana("p1", 10);
            script.playMinionCard("p1", TestCards.STORMWIND_CHAMPION, 2);
            script.playMinionCard("p1", TestCards.SLIME, 3);
            script.playMinionCard("p1", TestCards.TREANT, 4);

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.YETI, 5, 6),
                    TestCards.expectedMinion(TestCards.BLUEGILL_WARRIOR, 3, 2),
                    TestCards.expectedMinion(TestCards.STORMWIND_CHAMPION, 6, 6),
                    TestCards.expectedMinion(TestCards.SLIME, 2, 3),
                    TestCards.expectedMinion(TestCards.TREANT, 3, 3));

            script.setMana("p1", 10);
            script.playNonMinionCard("p1", TestCards.MOONFIRE, "p1:0");
            script.playNonMinionCard("p1", TestCards.MOONFIRE, "p1:1");
            script.playNonMinionCard("p1", TestCards.MOONFIRE, "p1:2");
            script.playNonMinionCard("p1", TestCards.MOONFIRE, "p1:3");
            script.playNonMinionCard("p1", TestCards.MOONFIRE, "p1:4");

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.YETI, 5, 5),
                    TestCards.expectedMinion(TestCards.BLUEGILL_WARRIOR, 3, 1),
                    TestCards.expectedMinion(TestCards.STORMWIND_CHAMPION, 6, 5),
                    TestCards.expectedMinion(TestCards.SLIME, 2, 2),
                    TestCards.expectedMinion(TestCards.TREANT, 3, 2));

            script.setMana("p2", 10);
            script.playNonMinionCard("p2", TestCards.PYROBLAST, "p1:2");

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.YETI, 4, 5),
                    TestCards.expectedMinion(TestCards.BLUEGILL_WARRIOR, 2, 1),
                    TestCards.expectedMinion(TestCards.SLIME, 1, 2),
                    TestCards.expectedMinion(TestCards.TREANT, 2, 2));
        });
    }

    @Test
    public void testRedemptionResurrectsHpAuraProvider() {
        PlayScript.testScript((script) -> {
            script.setMana("p1", 10);

            script.playCard("p1", TestCards.REDEMPTION);
            script.playMinionCard("p1", TestCards.STORMWIND_CHAMPION, 0);
            script.setMana("p1", 10);
            script.playMinionCard("p1", TestCards.KORKRON_ELITE, 1);
            script.playNonMinionCard("p1", TestCards.DARKBOMB, "p1:1");

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.STORMWIND_CHAMPION, 6, 6),
                    TestCards.expectedMinion(TestCards.KORKRON_ELITE, 5, 1));

            script.setCurrentPlayer("p2");
            script.setMana("p2", 10);
            script.playNonMinionCard("p2", TestCards.FIREBALL, "p1:0");

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.STORMWIND_CHAMPION, 6, 1),
                    TestCards.expectedMinion(TestCards.KORKRON_ELITE, 5, 1));
        });
    }

    @Test
    public void testAutoPlayedMalganisSavesDeadMinion() {
        PlayScript.testScript((script) -> {
            script.setMana("p1", 10);

            script.addToHand("p1", TestCards.MALGANIS);

            script.playMinionCard("p1", TestCards.VOIDWALKER, 0);
            script.playMinionCard("p1", TestCards.VOIDCALLER, 1);
            script.playMinionCard("p1", TestCards.EXPLOSIVE_SHEEP, 2);
            script.playNonMinionCard("p1", TestCards.DARKBOMB, "p1:1");

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.VOIDWALKER, 1, 3),
                    TestCards.expectedMinion(TestCards.VOIDCALLER, 3, 1),
                    TestCards.expectedMinion(TestCards.EXPLOSIVE_SHEEP, 1, 1));

            script.setCurrentPlayer("p2");
            script.setMana("p2", 10);
            script.playCard("p2", TestCards.CONSECRATION);

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.VOIDWALKER, 3, 1),
                    TestCards.expectedMinion(TestCards.MALGANIS, 9, 5));
        });
    }
}
