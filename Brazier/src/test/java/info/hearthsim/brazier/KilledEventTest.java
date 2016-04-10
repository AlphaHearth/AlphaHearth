package info.hearthsim.brazier;

import info.hearthsim.brazier.utils.BrazierTest;
import info.hearthsim.brazier.utils.TestCards;
import org.junit.Ignore;
import org.junit.Test;

public final class KilledEventTest extends BrazierTest {
    @Test
    @Ignore("This test case fails to pass.")
    public void testSI7KillsMadScientist() {
        agent.setMana("p2", 2);
        agent.deck("p2", TestCards.MIRROR_ENTITY);
        agent.setCurrentPlayer("p2");
        agent.playMinionCard("p2", TestCards.MAD_SCIENTIST, 0);
        // FIXME The death rattle effect of Mad Scientist is now considered as
        //       drawing and playing random secret card from the deck, which also
        //       increase the card count played in this turn, resulting triggering
        //       combo effect at inappropriate time.
        agent.endTurn();
        agent.setMana("p1", 2);
        agent.playNonMinionCard("p1", TestCards.THE_COIN, "");
        agent.playMinionCard("p1", TestCards.SI7_AGENT, 0, "p2:0");

        // The battle cry effect of SI:7 kills the Mad Scientist, whose death rattle effect
        // pulls out the Mirror Entity for player2. Then the summoning event of SI:7 is triggered,
        // which also triggers the Mirror Entity to summon a copy of SI:7 for player2.
        //
        // The tricks here is, summoning a minion should do the following steps in the exact order:
        //   1. Adds the minion to the board;
        //   2. Trigger its battle cry effect;
        //   3. Trigger the summoning event.
        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.SI7_AGENT, 3, 3)
        );
        agent.expectBoard("p2",
            TestCards.expectedMinion(TestCards.SI7_AGENT, 3, 3)
        );
        agent.expectSecret("p2");
    }

    @Test
    public void testMekgineerBlockBelcherDeathRattle() {
        agent.setCurrentPlayer("p1");
        agent.setMana("p1", 6);
        agent.playMinionCard("p1", TestCards.RECKLESS_ROCKETEER, 0);
        agent.endTurn();

        agent.setMana("p2", 10);
        agent.playMinionCard("p2", TestCards.MEKGINEER, 0);
        agent.setMana("p2", 10);
        agent.playMinionCard("p2", TestCards.WISP, 1);
        agent.playMinionCard("p2", TestCards.WISP, 2);
        agent.playMinionCard("p2", TestCards.WISP, 3);
        agent.playMinionCard("p2", TestCards.WISP, 4);
        agent.playMinionCard("p2", TestCards.WISP, 5);
        agent.playMinionCard("p2", TestCards.SLUDGE_BELCHER, 6);
        agent.endTurn();

        agent.attack("p1:0", "p2:6");

        // The triggering effect of Mekgineer Thermaplugg will summon a Leper Gnome for player2
        // as Reckless Rocketeer dies. The Gnome fills up the player2's board, preventing the
        // death rattle effect of Sludge Belcher from summoning the Slime.
        //
        // The tricks here is, Reckless Rocketeer born before Sludge Belcher, hence its killed event
        // should be triggered before triggering Sludge Belcher's death rattle.

        agent.expectBoard("p1");
        agent.expectBoard("p2",
            TestCards.expectedMinion(TestCards.MEKGINEER, 9, 7),
            TestCards.expectedMinion(TestCards.LEPER_GNOME, 2, 1),
            TestCards.expectedMinion(TestCards.WISP, 1, 1),
            TestCards.expectedMinion(TestCards.WISP, 1, 1),
            TestCards.expectedMinion(TestCards.WISP, 1, 1),
            TestCards.expectedMinion(TestCards.WISP, 1, 1),
            TestCards.expectedMinion(TestCards.WISP, 1, 1)
        );
    }

    @Test
    public void testSoulOfTheForest() {
        agent.setMana("p2", 10);
        agent.playMinionCard("p2", TestCards.BLUEGILL_WARRIOR, 0);
        agent.playMinionCard("p2", TestCards.HAUNTED_CREEPER, 1);

        agent.expectBoard("p2",
            TestCards.expectedMinion(TestCards.BLUEGILL_WARRIOR, 2, 1),
            TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2));

        agent.setMana("p2", 10);
        agent.playCard("p2", TestCards.SOUL_OF_THE_FOREST);

        agent.playMinionCard("p2", TestCards.BLUEGILL_WARRIOR, 2);

        agent.expectBoard("p2",
            TestCards.expectedMinion(TestCards.BLUEGILL_WARRIOR, 2, 1),
            TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
            TestCards.expectedMinion(TestCards.BLUEGILL_WARRIOR, 2, 1));

        agent.setMana("p1", 10);
        agent.playCard("p1", TestCards.FLAMESTRIKE);

        agent.expectBoard("p1");
        agent.expectBoard("p2",
            TestCards.expectedMinion(TestCards.SPECTRAL_SPIDER, 1, 1),
            TestCards.expectedMinion(TestCards.SPECTRAL_SPIDER, 1, 1),
            TestCards.expectedMinion(TestCards.TREANT, 2, 2),
            TestCards.expectedMinion(TestCards.TREANT, 2, 2));
    }

    @Test
    public void testHauntedCreeper() {
        agent.setMana("p2", 10);
        agent.playMinionCard("p2", TestCards.HAUNTED_CREEPER, 0);

        agent.expectBoard("p2",
            TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2));

        agent.setMana("p1", 10);
        agent.playNonMinionCard("p1", TestCards.MOONFIRE, "p2:0");
        agent.playNonMinionCard("p1", TestCards.MOONFIRE, "p2:0");

        agent.expectBoard("p2",
            TestCards.expectedMinion(TestCards.SPECTRAL_SPIDER, 1, 1),
            TestCards.expectedMinion(TestCards.SPECTRAL_SPIDER, 1, 1));
    }

    @Test
    public void testHauntedCreeperWithFullBoard() {
        agent.setMana("p2", 10);
        agent.playMinionCard("p2", TestCards.HAUNTED_CREEPER, 0);
        agent.playMinionCard("p2", TestCards.HAUNTED_CREEPER, 1);
        agent.playMinionCard("p2", TestCards.HAUNTED_CREEPER, 2);
        agent.playMinionCard("p2", TestCards.HAUNTED_CREEPER, 3);
        agent.setMana("p2", 10);
        agent.playMinionCard("p2", TestCards.HAUNTED_CREEPER, 4);
        agent.playMinionCard("p2", TestCards.HAUNTED_CREEPER, 5);
        agent.playMinionCard("p2", TestCards.HAUNTED_CREEPER, 6);

        agent.expectBoard("p2",
            TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
            TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
            TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
            TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
            TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
            TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
            TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2));

        agent.setMana("p1", 10);
        agent.playNonMinionCard("p1", TestCards.MOONFIRE, "p2:0");
        agent.playNonMinionCard("p1", TestCards.MOONFIRE, "p2:0");

        agent.expectBoard("p2",
            TestCards.expectedMinion(TestCards.SPECTRAL_SPIDER, 1, 1),
            TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
            TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
            TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
            TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
            TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
            TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2));
    }

    @Test
    public void testHauntedCreeperWithFullBoardBoardClear() {
        agent.setMana("p2", 10);
        agent.playMinionCard("p2", TestCards.HAUNTED_CREEPER, 0);
        agent.playMinionCard("p2", TestCards.HAUNTED_CREEPER, 1);
        agent.playMinionCard("p2", TestCards.HAUNTED_CREEPER, 2);
        agent.playMinionCard("p2", TestCards.HAUNTED_CREEPER, 3);
        agent.setMana("p2", 10);
        agent.playMinionCard("p2", TestCards.HAUNTED_CREEPER, 4);
        agent.playMinionCard("p2", TestCards.HAUNTED_CREEPER, 5);
        agent.playMinionCard("p2", TestCards.HAUNTED_CREEPER, 6);

        agent.expectBoard("p2",
            TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
            TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
            TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
            TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
            TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
            TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
            TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2));

        agent.setMana("p1", 10);
        agent.playCard("p1", TestCards.FLAMESTRIKE);

        agent.expectBoard("p2",
            TestCards.expectedMinion(TestCards.SPECTRAL_SPIDER, 1, 1),
            TestCards.expectedMinion(TestCards.SPECTRAL_SPIDER, 1, 1),
            TestCards.expectedMinion(TestCards.SPECTRAL_SPIDER, 1, 1),
            TestCards.expectedMinion(TestCards.SPECTRAL_SPIDER, 1, 1),
            TestCards.expectedMinion(TestCards.SPECTRAL_SPIDER, 1, 1),
            TestCards.expectedMinion(TestCards.SPECTRAL_SPIDER, 1, 1),
            TestCards.expectedMinion(TestCards.SPECTRAL_SPIDER, 1, 1));
    }

    @Test
    public void testSludgeBelcherDeathRattleWorksWithFullBoard() {
        agent.setMana("p2", 10);
        agent.playMinionCard("p2", TestCards.SLUDGE_BELCHER, 0);
        agent.playMinionCard("p2", TestCards.SLUDGE_BELCHER, 1);
        agent.setMana("p2", 10);
        agent.playMinionCard("p2", TestCards.SLUDGE_BELCHER, 2);
        agent.playMinionCard("p2", TestCards.SLUDGE_BELCHER, 3);
        agent.setMana("p2", 10);
        agent.playMinionCard("p2", TestCards.SLUDGE_BELCHER, 4);
        agent.playMinionCard("p2", TestCards.SLUDGE_BELCHER, 5);
        agent.setMana("p2", 10);
        agent.playMinionCard("p2", TestCards.SLUDGE_BELCHER, 6);

        agent.expectBoard("p2",
            TestCards.expectedMinion(TestCards.SLUDGE_BELCHER, 3, 5),
            TestCards.expectedMinion(TestCards.SLUDGE_BELCHER, 3, 5),
            TestCards.expectedMinion(TestCards.SLUDGE_BELCHER, 3, 5),
            TestCards.expectedMinion(TestCards.SLUDGE_BELCHER, 3, 5),
            TestCards.expectedMinion(TestCards.SLUDGE_BELCHER, 3, 5),
            TestCards.expectedMinion(TestCards.SLUDGE_BELCHER, 3, 5),
            TestCards.expectedMinion(TestCards.SLUDGE_BELCHER, 3, 5));

        agent.setMana("p1", 10);
        agent.playNonMinionCard("p1", TestCards.MOONFIRE, "p2:0");
        agent.playNonMinionCard("p1", TestCards.MOONFIRE, "p2:0");
        agent.playNonMinionCard("p1", TestCards.MOONFIRE, "p2:0");
        agent.playNonMinionCard("p1", TestCards.MOONFIRE, "p2:0");
        agent.playNonMinionCard("p1", TestCards.MOONFIRE, "p2:0");

        agent.expectBoard("p2",
            TestCards.expectedMinion(TestCards.SLIME, 1, 2),
            TestCards.expectedMinion(TestCards.SLUDGE_BELCHER, 3, 5),
            TestCards.expectedMinion(TestCards.SLUDGE_BELCHER, 3, 5),
            TestCards.expectedMinion(TestCards.SLUDGE_BELCHER, 3, 5),
            TestCards.expectedMinion(TestCards.SLUDGE_BELCHER, 3, 5),
            TestCards.expectedMinion(TestCards.SLUDGE_BELCHER, 3, 5),
            TestCards.expectedMinion(TestCards.SLUDGE_BELCHER, 3, 5));
    }

    @Test
    public void testSludgeBelcher() {
        agent.setMana("p2", 10);
        agent.playMinionCard("p2", TestCards.SLUDGE_BELCHER, 0);
        agent.setMana("p2", 10);
        agent.playMinionCard("p2", TestCards.YETI, 0);
        agent.playMinionCard("p2", TestCards.YETI, 2);

        agent.setMana("p1", 10);
        agent.playMinionCard("p1", TestCards.YETI, 0);
        agent.playMinionCard("p1", TestCards.YETI, 1);

        agent.refreshAttacks();

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.YETI, 4, 5),
            TestCards.expectedMinion(TestCards.YETI, 4, 5));

        agent.expectBoard("p2",
            TestCards.expectedMinion(TestCards.YETI, 4, 5),
            TestCards.expectedMinion(TestCards.SLUDGE_BELCHER, 3, 5),
            TestCards.expectedMinion(TestCards.YETI, 4, 5));

        agent.attack("p1:0", "p2:1"); // YETI 1 -> SLUDGE

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.YETI, 4, 2),
            TestCards.expectedMinion(TestCards.YETI, 4, 5));

        agent.expectBoard("p2",
            TestCards.expectedMinion(TestCards.YETI, 4, 5),
            TestCards.expectedMinion(TestCards.SLUDGE_BELCHER, 3, 1),
            TestCards.expectedMinion(TestCards.YETI, 4, 5));

        agent.attack("p1:1", "p2:1"); // YETI 2 -> SLUDGE

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.YETI, 4, 2),
            TestCards.expectedMinion(TestCards.YETI, 4, 2));

        agent.expectBoard("p2",
            TestCards.expectedMinion(TestCards.YETI, 4, 5),
            TestCards.expectedMinion(TestCards.SLIME, 1, 2),
            TestCards.expectedMinion(TestCards.YETI, 4, 5));

        agent.refreshAttacks();

        agent.attack("p1:0", "p2:1"); // YETI 1 -> SLIME

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.YETI, 4, 1),
            TestCards.expectedMinion(TestCards.YETI, 4, 2));

        agent.expectBoard("p2",
            TestCards.expectedMinion(TestCards.YETI, 4, 5),
            TestCards.expectedMinion(TestCards.YETI, 4, 5));
    }

    @Test
    public void testDeathsBite() {
        agent.setMana("p1", 10);
        agent.setMana("p2", 10);

        agent.playCard("p1", TestCards.DEATHS_BITE);
        agent.playMinionCard("p1", TestCards.YETI, 0);
        agent.playMinionCard("p2", TestCards.FROTHING_BERSERKER, 0);
        agent.playMinionCard("p2", TestCards.YETI, 0);

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.YETI, 4, 5));
        agent.expectBoard("p2",
            TestCards.expectedMinion(TestCards.YETI, 4, 5),
            TestCards.expectedMinion(TestCards.FROTHING_BERSERKER, 2, 4));

        agent.attack("p1:hero", "p2:hero");

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.YETI, 4, 5));
        agent.expectBoard("p2",
            TestCards.expectedMinion(TestCards.YETI, 4, 5),
            TestCards.expectedMinion(TestCards.FROTHING_BERSERKER, 2, 4));

        agent.expectHeroHp("p1", 30, 0);
        agent.expectHeroHp("p2", 26, 0);

        agent.refreshAttacks();

        agent.attack("p1:hero", "p2:0");

        agent.expectHeroHp("p1", 26, 0);
        agent.expectHeroHp("p2", 26, 0);

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.YETI, 4, 4));
        agent.expectBoard("p2",
            TestCards.expectedMinion(TestCards.FROTHING_BERSERKER, 6, 3));
    }

    @Test
    public void testDeathsBiteKilledByJaraxxus() {
        agent.setMana("p1", 10);

        agent.playCard("p1", TestCards.DEATHS_BITE);
        agent.playMinionCard("p1", TestCards.YETI, 0);

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.YETI, 4, 5));

        agent.setMana("p1", 10);
        agent.playMinionCard("p1", TestCards.JARAXXUS, 0);

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.YETI, 4, 4));

        agent.expectWeapon("p1", 3, 8);
    }
}
