package info.hearthsim.brazier;

import org.junit.Test;

public final class CardDrawTest {
    private static void setupCultMasterBoards(PlayScript script) {
        script.deck("p1", TestCards.MOONFIRE, TestCards.FIERY_WAR_AXE, TestCards.FLAME_OF_AZZINOTH);
        script.deck("p2", TestCards.YETI, TestCards.SCARLET_CRUSADER, TestCards.SLAM);

        script.setMana("p1", 10);
        script.setMana("p2", 10);

        script.playMinionCard("p1", TestCards.BLUEGILL_WARRIOR, 0);
        script.playMinionCard("p1", TestCards.CULT_MASTER, 0);
        script.playMinionCard("p1", TestCards.BLUEGILL_WARRIOR, 2);

        script.playMinionCard("p2", TestCards.YETI, 0);

        script.expectBoard("p1",
                TestCards.expectedMinion(TestCards.CULT_MASTER, 4, 2),
                TestCards.expectedMinion(TestCards.BLUEGILL_WARRIOR, 2, 1),
                TestCards.expectedMinion(TestCards.BLUEGILL_WARRIOR, 2, 1));
        script.expectBoard("p2",
                TestCards.expectedMinion(TestCards.YETI, 4, 5));

        script.setMana("p1", 10);
        script.setMana("p2", 10);

        script.refreshAttacks();
    }

    @Test
    public void testCultMasterKillOneMinion() {
        PlayScript.testScript((script) -> {
            setupCultMasterBoards(script);

            script.attack("p2:0", "p1:2");

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.CULT_MASTER, 4, 2),
                    TestCards.expectedMinion(TestCards.BLUEGILL_WARRIOR, 2, 1));
            script.expectBoard("p2",
                    TestCards.expectedMinion(TestCards.YETI, 4, 3));

            script.expectHand("p1", TestCards.FLAME_OF_AZZINOTH);
            script.expectHand("p2");
        });
    }

    @Test
    public void testCultMasterKillTwoMinions() {
        PlayScript.testScript((script) -> {
            setupCultMasterBoards(script);

            script.playCard("p2", TestCards.WHIRLWIND);

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.CULT_MASTER, 4, 1));
            script.expectBoard("p2",
                    TestCards.expectedMinion(TestCards.YETI, 4, 4));

            script.expectHand("p1", TestCards.FLAME_OF_AZZINOTH, TestCards.FIERY_WAR_AXE);
            script.expectHand("p2");
        });
    }

    @Test
    public void testCultMasterDoesNotDrawForSelf() {
        PlayScript.testScript((script) -> {
            setupCultMasterBoards(script);

            script.attack("p2:0", "p1:0");

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.BLUEGILL_WARRIOR, 2, 1),
                    TestCards.expectedMinion(TestCards.BLUEGILL_WARRIOR, 2, 1));
            script.expectBoard("p2",
                    TestCards.expectedMinion(TestCards.YETI, 4, 1));

            script.expectHand("p1");
            script.expectHand("p2");
        });
    }

    @Test
    public void testCultMasterDoesNotDrawWhenDying() {
        PlayScript.testScript((script) -> {
            setupCultMasterBoards(script);

            script.playCard("p2", TestCards.FLAMESTRIKE);

            script.expectBoard("p1");
            script.expectBoard("p2",
                    TestCards.expectedMinion(TestCards.YETI, 4, 5));

            script.expectHand("p1");
            script.expectHand("p2");
        });
    }

    @Test
    public void testCultMasterDoesNotDrawForOpponentMinions() {
        PlayScript.testScript((script) -> {
            setupCultMasterBoards(script);

            script.playMinionCard("p2", TestCards.BLUEGILL_WARRIOR, 1);

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.CULT_MASTER, 4, 2),
                    TestCards.expectedMinion(TestCards.BLUEGILL_WARRIOR, 2, 1),
                    TestCards.expectedMinion(TestCards.BLUEGILL_WARRIOR, 2, 1));
            script.expectBoard("p2",
                    TestCards.expectedMinion(TestCards.YETI, 4, 5),
                    TestCards.expectedMinion(TestCards.BLUEGILL_WARRIOR, 2, 1));

            script.playNonMinionCard("p2", TestCards.MOONFIRE, "p2:1");

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.CULT_MASTER, 4, 2),
                    TestCards.expectedMinion(TestCards.BLUEGILL_WARRIOR, 2, 1),
                    TestCards.expectedMinion(TestCards.BLUEGILL_WARRIOR, 2, 1));
            script.expectBoard("p2",
                    TestCards.expectedMinion(TestCards.YETI, 4, 5));

            script.expectHand("p1");
            script.expectHand("p2");
        });
    }

    @Test
    public void testStarvingBuzzardDoesNotDrawForOpponentBeast() {
        PlayScript.testScript((script) -> {
            script.deck("p1", TestCards.MOONFIRE, TestCards.FIERY_WAR_AXE, TestCards.FLAME_OF_AZZINOTH);

            script.setMana("p1", 10);

            script.playMinionCard("p1", TestCards.STARVING_BUZZARD, 0);

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.STARVING_BUZZARD, 3, 2));

            script.setMana("p2", 10);
            script.setCurrentPlayer("p2");
            script.playMinionCard("p2", TestCards.STONETUSK_BOAR, 1);

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.STARVING_BUZZARD, 3, 2));
            script.expectBoard("p2",
                    TestCards.expectedMinion(TestCards.STONETUSK_BOAR, 1, 1));

            script.expectHand("p1");
        });
    }

    @Test
    public void testStarvingBuzzardDoesNotDrawForNonBeast() {
        PlayScript.testScript((script) -> {
            script.deck("p1", TestCards.MOONFIRE, TestCards.FIERY_WAR_AXE, TestCards.FLAME_OF_AZZINOTH);

            script.setMana("p1", 10);

            script.playMinionCard("p1", TestCards.STARVING_BUZZARD, 0);

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.STARVING_BUZZARD, 3, 2));

            script.playMinionCard("p1", TestCards.BLUEGILL_WARRIOR, 1);

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.STARVING_BUZZARD, 3, 2),
                    TestCards.expectedMinion(TestCards.BLUEGILL_WARRIOR, 2, 1));

            script.expectHand("p1");
        });
    }

    @Test
    public void testStarvingBuzzardDrawsForBeast() {
        PlayScript.testScript((script) -> {
            script.deck("p1", TestCards.MOONFIRE, TestCards.FIERY_WAR_AXE, TestCards.FLAME_OF_AZZINOTH);

            script.setMana("p1", 10);

            script.playMinionCard("p1", TestCards.STARVING_BUZZARD, 0);

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.STARVING_BUZZARD, 3, 2));

            script.playMinionCard("p1", TestCards.STONETUSK_BOAR, 1);

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.STARVING_BUZZARD, 3, 2),
                    TestCards.expectedMinion(TestCards.STONETUSK_BOAR, 1, 1));

            script.expectHand("p1", TestCards.FLAME_OF_AZZINOTH);
        });
    }

    @Test
    public void testStarvingBuzzardDrawsForCopiedBeast() {
        PlayScript.testScript((script) -> {
            script.deck("p1", TestCards.MOONFIRE, TestCards.FIERY_WAR_AXE, TestCards.FLAME_OF_AZZINOTH);

            script.setMana("p1", 10);
            script.setMana("p2", 10);

            script.playMinionCard("p2", TestCards.HAUNTED_CREEPER, 0);
            script.playMinionCard("p1", TestCards.STARVING_BUZZARD, 0);

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.STARVING_BUZZARD, 3, 2));
            script.expectBoard("p2",
                    TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2));

            script.expectHand("p1");
            script.expectHand("p2");

            script.playMinionCard("p1", TestCards.FACELESS_MANIPULATOR, 1, "p2:0");

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.STARVING_BUZZARD, 3, 2),
                    TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2));
            script.expectBoard("p2",
                    TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2));

            script.expectHand("p1", TestCards.FLAME_OF_AZZINOTH);
            script.expectHand("p2");
        });
    }

    @Test
    public void testBlessingOfWisdom() {
        PlayScript.testScript((script) -> {
            script.deck("p1", TestCards.MOONFIRE, TestCards.FIERY_WAR_AXE, TestCards.FLAME_OF_AZZINOTH);
            script.deck("p2", TestCards.YETI, TestCards.SCARLET_CRUSADER, TestCards.SLAM);

            script.setMana("p1", 10);

            script.playMinionCard("p1", TestCards.STONETUSK_BOAR, 0);

            script.expectBoard("p1",
                    TestCards.expectedMinion(TestCards.STONETUSK_BOAR, 1, 1));

            script.playNonMinionCard("p1", TestCards.BLESSING_OF_WISDOM, "p1:0");

            script.attack("p1:0", "p2:hero");

            script.expectHand("p1", TestCards.FLAME_OF_AZZINOTH);

            script.refreshAttacks();
            script.attack("p1:0", "p2:hero");

            script.expectHand("p1", TestCards.FLAME_OF_AZZINOTH, TestCards.FIERY_WAR_AXE);
            script.expectHand("p2");
        });
    }

    @Test
    public void testBlessingOfWisdomOfOpponent() {
        PlayScript.testScript((script) -> {
            script.deck("p1", TestCards.MOONFIRE, TestCards.FIERY_WAR_AXE, TestCards.FLAME_OF_AZZINOTH);
            script.deck("p2", TestCards.YETI, TestCards.SCARLET_CRUSADER, TestCards.SLAM);

            script.setMana("p1", 10);
            script.setMana("p2", 10);

            script.playMinionCard("p2", TestCards.STONETUSK_BOAR, 0);

            script.expectBoard("p2",
                    TestCards.expectedMinion(TestCards.STONETUSK_BOAR, 1, 1));

            script.playNonMinionCard("p1", TestCards.BLESSING_OF_WISDOM, "p2:0");

            script.attack("p2:0", "p1:hero");

            script.expectHand("p1", TestCards.FLAME_OF_AZZINOTH);

            script.refreshAttacks();
            script.attack("p2:0", "p1:hero");

            script.expectHand("p1", TestCards.FLAME_OF_AZZINOTH, TestCards.FIERY_WAR_AXE);
            script.expectHand("p2");
        });
    }
}
