package info.hearthsim.brazier;

import info.hearthsim.brazier.utils.BrazierTest;
import info.hearthsim.brazier.utils.TestCards;
import org.junit.Ignore;
import org.junit.Test;

public final class AuraTest extends BrazierTest {
    @Test
    public void testDireWolfAlpha() {
        agent.setMana("p1", 10);
        agent.setMana("p2", 10);

        agent.playMinionCard("p1", TestCards.YETI, 0);
        agent.playMinionCard("p1", TestCards.BLUEGILL_WARRIOR, 1);
        agent.playMinionCard("p1", TestCards.SLIME, 2);
        agent.playMinionCard("p1", TestCards.TREANT, 3);

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.YETI, 4, 5),
            TestCards.expectedMinion(TestCards.BLUEGILL_WARRIOR, 2, 1),
            TestCards.expectedMinion(TestCards.SLIME, 1, 2),
            TestCards.expectedMinion(TestCards.TREANT, 2, 2));

        agent.playMinionCard("p1", TestCards.DIRE_WOLF_ALPHA, 2);

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.YETI, 4, 5),
            TestCards.expectedMinion(TestCards.BLUEGILL_WARRIOR, 3, 1),
            TestCards.expectedMinion(TestCards.DIRE_WOLF_ALPHA, 2, 2),
            TestCards.expectedMinion(TestCards.SLIME, 2, 2),
            TestCards.expectedMinion(TestCards.TREANT, 2, 2));

        agent.setMana("p2", 10);
        agent.playNonMinionCard("p2", TestCards.PYROBLAST, "p1:1");

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.YETI, 5, 5),
            TestCards.expectedMinion(TestCards.DIRE_WOLF_ALPHA, 2, 2),
            TestCards.expectedMinion(TestCards.SLIME, 2, 2),
            TestCards.expectedMinion(TestCards.TREANT, 2, 2));

        agent.setMana("p2", 10);
        agent.playNonMinionCard("p2", TestCards.PYROBLAST, "p1:2");

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.YETI, 5, 5),
            TestCards.expectedMinion(TestCards.DIRE_WOLF_ALPHA, 2, 2),
            TestCards.expectedMinion(TestCards.TREANT, 3, 2));

        agent.setMana("p2", 10);
        agent.playNonMinionCard("p2", TestCards.PYROBLAST, "p1:2");

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.YETI, 5, 5),
            TestCards.expectedMinion(TestCards.DIRE_WOLF_ALPHA, 2, 2));

        agent.setMana("p2", 10);
        agent.playNonMinionCard("p2", TestCards.PYROBLAST, "p1:0");

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.DIRE_WOLF_ALPHA, 2, 2));
    }

    @Test
    public void testDireWolfAlphaDies() {
        agent.setMana("p1", 10);

        agent.playMinionCard("p1", TestCards.YETI, 0);
        agent.playMinionCard("p1", TestCards.BLUEGILL_WARRIOR, 1);
        agent.playMinionCard("p1", TestCards.DIRE_WOLF_ALPHA, 2);
        agent.playMinionCard("p1", TestCards.SLIME, 3);
        agent.playMinionCard("p1", TestCards.TREANT, 4);

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.YETI, 4, 5),
            TestCards.expectedMinion(TestCards.BLUEGILL_WARRIOR, 3, 1),
            TestCards.expectedMinion(TestCards.DIRE_WOLF_ALPHA, 2, 2),
            TestCards.expectedMinion(TestCards.SLIME, 2, 2),
            TestCards.expectedMinion(TestCards.TREANT, 2, 2));

        agent.setMana("p2", 10);
        agent.playNonMinionCard("p2", TestCards.PYROBLAST, "p1:2");

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.YETI, 4, 5),
            TestCards.expectedMinion(TestCards.BLUEGILL_WARRIOR, 2, 1),
            TestCards.expectedMinion(TestCards.SLIME, 1, 2),
            TestCards.expectedMinion(TestCards.TREANT, 2, 2));
    }

    @Test
    public void testWeeSpellstopper() {
        agent.setMana("p1", 10);
        agent.setMana("p2", 10);

        agent.playMinionCard("p1", TestCards.YETI, 0);
        agent.playMinionCard("p1", TestCards.BLUEGILL_WARRIOR, 1);
        agent.playMinionCard("p1", TestCards.SLIME, 2);
        agent.playMinionCard("p1", TestCards.TREANT, 3);

        agent.expectBoard("p1",
            TestCards.expectedMinionWithFlags(TestCards.YETI, 4, 5),
            TestCards.expectedMinionWithFlags(TestCards.BLUEGILL_WARRIOR, 2, 1),
            TestCards.expectedMinionWithFlags(TestCards.SLIME, 1, 2, "taunt"),
            TestCards.expectedMinionWithFlags(TestCards.TREANT, 2, 2));

        agent.setMana("p1", 10);
        agent.playMinionCard("p1", TestCards.WEE_SPELLSTOPPER, 2);

        agent.expectBoard("p1",
            TestCards.expectedMinionWithFlags(TestCards.YETI, 4, 5),
            TestCards.expectedMinionWithFlags(TestCards.BLUEGILL_WARRIOR, 2, 1, "untargetable"),
            TestCards.expectedMinionWithFlags(TestCards.WEE_SPELLSTOPPER, 2, 5),
            TestCards.expectedMinionWithFlags(TestCards.SLIME, 1, 2, "taunt", "untargetable"),
            TestCards.expectedMinionWithFlags(TestCards.TREANT, 2, 2));

        agent.setMana("p2", 10);
        agent.playNonMinionCard("p2", TestCards.PYROBLAST, "p1:1");

        agent.expectBoard("p1",
            TestCards.expectedMinionWithFlags(TestCards.YETI, 4, 5, "untargetable"),
            TestCards.expectedMinionWithFlags(TestCards.WEE_SPELLSTOPPER, 2, 5),
            TestCards.expectedMinionWithFlags(TestCards.SLIME, 1, 2, "taunt", "untargetable"),
            TestCards.expectedMinionWithFlags(TestCards.TREANT, 2, 2));

        agent.setMana("p2", 10);
        agent.playNonMinionCard("p2", TestCards.PYROBLAST, "p1:2");

        agent.expectBoard("p1",
            TestCards.expectedMinionWithFlags(TestCards.YETI, 4, 5, "untargetable"),
            TestCards.expectedMinionWithFlags(TestCards.WEE_SPELLSTOPPER, 2, 5),
            TestCards.expectedMinionWithFlags(TestCards.TREANT, 2, 2, "untargetable"));

        agent.setMana("p2", 10);
        agent.playNonMinionCard("p2", TestCards.PYROBLAST, "p1:2");

        agent.expectBoard("p1",
            TestCards.expectedMinionWithFlags(TestCards.YETI, 4, 5, "untargetable"),
            TestCards.expectedMinionWithFlags(TestCards.WEE_SPELLSTOPPER, 2, 5));

        agent.setMana("p2", 10);
        agent.playNonMinionCard("p2", TestCards.PYROBLAST, "p1:0");

        agent.expectBoard("p1",
            TestCards.expectedMinionWithFlags(TestCards.WEE_SPELLSTOPPER, 2, 5));
    }

    @Test
    public void testWeeSpellStopperDies() {
        agent.setMana("p1", 10);

        agent.playMinionCard("p1", TestCards.YETI, 0);
        agent.playMinionCard("p1", TestCards.BLUEGILL_WARRIOR, 1);
        agent.setMana("p1", 10);
        agent.playMinionCard("p1", TestCards.WEE_SPELLSTOPPER, 2);
        agent.playMinionCard("p1", TestCards.SLIME, 3);
        agent.playMinionCard("p1", TestCards.TREANT, 4);

        agent.setMana("p2", 10);
        agent.playMinionCard("p2", TestCards.BLUEGILL_WARRIOR, 0);

        agent.attack("p2:0", "p1:2");

        agent.expectBoard("p1",
            TestCards.expectedMinionWithFlags(TestCards.YETI, 4, 5),
            TestCards.expectedMinionWithFlags(TestCards.BLUEGILL_WARRIOR, 2, 1, "untargetable"),
            TestCards.expectedMinionWithFlags(TestCards.WEE_SPELLSTOPPER, 2, 3),
            TestCards.expectedMinionWithFlags(TestCards.SLIME, 1, 2, "taunt", "untargetable"),
            TestCards.expectedMinionWithFlags(TestCards.TREANT, 2, 2));

        agent.setMana("p2", 10);
        agent.playNonMinionCard("p2", TestCards.PYROBLAST, "p1:2");

        agent.expectBoard("p1",
            TestCards.expectedMinionWithFlags(TestCards.YETI, 4, 5),
            TestCards.expectedMinionWithFlags(TestCards.BLUEGILL_WARRIOR, 2, 1),
            TestCards.expectedMinionWithFlags(TestCards.SLIME, 1, 2, "taunt"),
            TestCards.expectedMinionWithFlags(TestCards.TREANT, 2, 2));
    }

    @Test
    public void testHealWithHuntersMark() {
        agent.setMana("p1", 10);

        agent.playMinionCard("p1", TestCards.WISP, 0);
        agent.playMinionCard("p1", TestCards.STORMWIND_CHAMPION, 1);
        agent.playNonMinionCard("p1", TestCards.MOONFIRE, "p1:0");

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.WISP, 2, 1),
            TestCards.expectedMinion(TestCards.STORMWIND_CHAMPION, 6, 6));

        agent.playNonMinionCard("p1", TestCards.HUNTERS_MARK, "p1:0");

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.WISP, 2, 2),
            TestCards.expectedMinion(TestCards.STORMWIND_CHAMPION, 6, 6));
    }

    @Test
    public void testStormwindChampion() {
        agent.setMana("p1", 10);

        agent.playMinionCard("p1", TestCards.YETI, 0);
        agent.playMinionCard("p1", TestCards.BLUEGILL_WARRIOR, 1);
        agent.setMana("p1", 10);
        agent.playMinionCard("p1", TestCards.STORMWIND_CHAMPION, 2);
        agent.playMinionCard("p1", TestCards.SLIME, 3);
        agent.playMinionCard("p1", TestCards.TREANT, 4);

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.YETI, 5, 6),
            TestCards.expectedMinion(TestCards.BLUEGILL_WARRIOR, 3, 2),
            TestCards.expectedMinion(TestCards.STORMWIND_CHAMPION, 6, 6),
            TestCards.expectedMinion(TestCards.SLIME, 2, 3),
            TestCards.expectedMinion(TestCards.TREANT, 3, 3));

        agent.setMana("p2", 10);
        agent.playNonMinionCard("p2", TestCards.PYROBLAST, "p1:2");

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.YETI, 4, 5),
            TestCards.expectedMinion(TestCards.BLUEGILL_WARRIOR, 2, 1),
            TestCards.expectedMinion(TestCards.SLIME, 1, 2),
            TestCards.expectedMinion(TestCards.TREANT, 2, 2));
    }

    @Test
    public void testStormwindChampionUpdatesAuraAfterDeathRattle() {
        agent.setMana("p1", 10);

        agent.playMinionCard("p1", TestCards.SLUDGE_BELCHER, 0);
        agent.setMana("p1", 10);
        agent.playMinionCard("p1", TestCards.STORMWIND_CHAMPION, 1);

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.SLUDGE_BELCHER, 4, 6),
            TestCards.expectedMinion(TestCards.STORMWIND_CHAMPION, 6, 6));

        agent.setMana("p2", 10);
        agent.playNonMinionCard("p2", TestCards.PYROBLAST, "p1:0");

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.SLIME, 2, 3),
            TestCards.expectedMinion(TestCards.STORMWIND_CHAMPION, 6, 6));
    }

    @Test
    public void testKillingStormwindChampionDoesNotReduceCurrentHealth() {
        agent.setMana("p1", 10);

        agent.playMinionCard("p1", TestCards.YETI, 0);
        agent.playMinionCard("p1", TestCards.BLUEGILL_WARRIOR, 1);
        agent.setMana("p1", 10);
        agent.playMinionCard("p1", TestCards.STORMWIND_CHAMPION, 2);
        agent.playMinionCard("p1", TestCards.SLIME, 3);
        agent.playMinionCard("p1", TestCards.TREANT, 4);

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.YETI, 5, 6),
            TestCards.expectedMinion(TestCards.BLUEGILL_WARRIOR, 3, 2),
            TestCards.expectedMinion(TestCards.STORMWIND_CHAMPION, 6, 6),
            TestCards.expectedMinion(TestCards.SLIME, 2, 3),
            TestCards.expectedMinion(TestCards.TREANT, 3, 3));

        agent.setMana("p1", 10);
        agent.playNonMinionCard("p1", TestCards.MOONFIRE, "p1:0");
        agent.playNonMinionCard("p1", TestCards.MOONFIRE, "p1:1");
        agent.playNonMinionCard("p1", TestCards.MOONFIRE, "p1:2");
        agent.playNonMinionCard("p1", TestCards.MOONFIRE, "p1:3");
        agent.playNonMinionCard("p1", TestCards.MOONFIRE, "p1:4");

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.YETI, 5, 5),
            TestCards.expectedMinion(TestCards.BLUEGILL_WARRIOR, 3, 1),
            TestCards.expectedMinion(TestCards.STORMWIND_CHAMPION, 6, 5),
            TestCards.expectedMinion(TestCards.SLIME, 2, 2),
            TestCards.expectedMinion(TestCards.TREANT, 3, 2));

        agent.setMana("p2", 10);
        agent.playNonMinionCard("p2", TestCards.PYROBLAST, "p1:2");

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.YETI, 4, 5),
            TestCards.expectedMinion(TestCards.BLUEGILL_WARRIOR, 2, 1),
            TestCards.expectedMinion(TestCards.SLIME, 1, 2),
            TestCards.expectedMinion(TestCards.TREANT, 2, 2));
    }

    @Test
    public void testRedemptionResurrectsHpAuraProvider() {
        agent.setMana("p1", 10);

        agent.playCard("p1", TestCards.REDEMPTION);
        agent.playMinionCard("p1", TestCards.STORMWIND_CHAMPION, 0);
        agent.setMana("p1", 10);
        agent.playMinionCard("p1", TestCards.KORKRON_ELITE, 1);
        agent.playNonMinionCard("p1", TestCards.DARKBOMB, "p1:1");

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.STORMWIND_CHAMPION, 6, 6),
            TestCards.expectedMinion(TestCards.KORKRON_ELITE, 5, 1));

        agent.setCurrentPlayer("p2");
        agent.setMana("p2", 10);
        agent.playNonMinionCard("p2", TestCards.FIREBALL, "p1:0");

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.STORMWIND_CHAMPION, 6, 1),
            TestCards.expectedMinion(TestCards.KORKRON_ELITE, 5, 1));
    }

    @Test
    public void testAutoPlayedMalganisSavesDeadMinion() {
        agent.setMana("p1", 10);

        agent.addToHand("p1", TestCards.MALGANIS);

        agent.playMinionCard("p1", TestCards.VOIDWALKER, 0);
        agent.playMinionCard("p1", TestCards.VOIDCALLER, 1);
        agent.playMinionCard("p1", TestCards.EXPLOSIVE_SHEEP, 2);
        agent.playNonMinionCard("p1", TestCards.DARKBOMB, "p1:1");

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.VOIDWALKER, 1, 3),
            TestCards.expectedMinion(TestCards.VOIDCALLER, 3, 1),
            TestCards.expectedMinion(TestCards.EXPLOSIVE_SHEEP, 1, 1));

        agent.setCurrentPlayer("p2");
        agent.setMana("p2", 10);
        agent.playCard("p2", TestCards.CONSECRATION);

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.VOIDWALKER, 3, 1),
            TestCards.expectedMinion(TestCards.MALGANIS, 9, 5));
    }
}
