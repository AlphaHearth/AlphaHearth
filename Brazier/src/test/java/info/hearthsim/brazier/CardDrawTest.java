package info.hearthsim.brazier;

import info.hearthsim.brazier.utils.BrazierTest;
import info.hearthsim.brazier.utils.TestAgent;
import info.hearthsim.brazier.utils.TestCards;
import org.junit.Test;

public final class CardDrawTest extends BrazierTest {
    private static void setupCultMasterBoards(TestAgent script) {
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
        setupCultMasterBoards(agent);

        agent.attack("p2:0", "p1:2");

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.CULT_MASTER, 4, 2),
            TestCards.expectedMinion(TestCards.BLUEGILL_WARRIOR, 2, 1));
        agent.expectBoard("p2",
            TestCards.expectedMinion(TestCards.YETI, 4, 3));

        agent.expectHand("p1", TestCards.FLAME_OF_AZZINOTH);
        agent.expectHand("p2");
    }

    @Test
    public void testCultMasterKillTwoMinions() {
        setupCultMasterBoards(agent);

        agent.playCard("p2", TestCards.WHIRLWIND);

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.CULT_MASTER, 4, 1));
        agent.expectBoard("p2",
            TestCards.expectedMinion(TestCards.YETI, 4, 4));

        agent.expectHand("p1", TestCards.FLAME_OF_AZZINOTH, TestCards.FIERY_WAR_AXE);
        agent.expectHand("p2");
    }

    @Test
    public void testCultMasterDoesNotDrawForSelf() {
        setupCultMasterBoards(agent);

        agent.attack("p2:0", "p1:0");

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.BLUEGILL_WARRIOR, 2, 1),
            TestCards.expectedMinion(TestCards.BLUEGILL_WARRIOR, 2, 1));
        agent.expectBoard("p2",
            TestCards.expectedMinion(TestCards.YETI, 4, 1));

        agent.expectHand("p1");
        agent.expectHand("p2");
    }

    @Test
    public void testCultMasterDoesNotDrawWhenDying() {
        setupCultMasterBoards(agent);

        agent.playCard("p2", TestCards.FLAMESTRIKE);

        agent.expectBoard("p1");
        agent.expectBoard("p2",
            TestCards.expectedMinion(TestCards.YETI, 4, 5));

        agent.expectHand("p1");
        agent.expectHand("p2");
    }

    @Test
    public void testCultMasterDoesNotDrawForOpponentMinions() {
        setupCultMasterBoards(agent);

        agent.playMinionCard("p2", TestCards.BLUEGILL_WARRIOR, 1);

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.CULT_MASTER, 4, 2),
            TestCards.expectedMinion(TestCards.BLUEGILL_WARRIOR, 2, 1),
            TestCards.expectedMinion(TestCards.BLUEGILL_WARRIOR, 2, 1));
        agent.expectBoard("p2",
            TestCards.expectedMinion(TestCards.YETI, 4, 5),
            TestCards.expectedMinion(TestCards.BLUEGILL_WARRIOR, 2, 1));

        agent.playNonMinionCard("p2", TestCards.MOONFIRE, "p2:1");

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.CULT_MASTER, 4, 2),
            TestCards.expectedMinion(TestCards.BLUEGILL_WARRIOR, 2, 1),
            TestCards.expectedMinion(TestCards.BLUEGILL_WARRIOR, 2, 1));
        agent.expectBoard("p2",
            TestCards.expectedMinion(TestCards.YETI, 4, 5));

        agent.expectHand("p1");
        agent.expectHand("p2");
    }

    @Test
    public void testStarvingBuzzardDoesNotDrawForOpponentBeast() {
        agent.deck("p1", TestCards.MOONFIRE, TestCards.FIERY_WAR_AXE, TestCards.FLAME_OF_AZZINOTH);

        agent.setMana("p1", 10);

        agent.playMinionCard("p1", TestCards.STARVING_BUZZARD, 0);

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.STARVING_BUZZARD, 3, 2));

        agent.setMana("p2", 10);
        agent.setCurrentPlayer("p2");
        agent.playMinionCard("p2", TestCards.STONETUSK_BOAR, 1);

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.STARVING_BUZZARD, 3, 2));
        agent.expectBoard("p2",
            TestCards.expectedMinion(TestCards.STONETUSK_BOAR, 1, 1));

        agent.expectHand("p1");
    }

    @Test
    public void testStarvingBuzzardDoesNotDrawForNonBeast() {
        agent.deck("p1", TestCards.MOONFIRE, TestCards.FIERY_WAR_AXE, TestCards.FLAME_OF_AZZINOTH);

        agent.setMana("p1", 10);

        agent.playMinionCard("p1", TestCards.STARVING_BUZZARD, 0);

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.STARVING_BUZZARD, 3, 2));
        agent.expectHand("p1");

        agent.playMinionCard("p1", TestCards.BLUEGILL_WARRIOR, 1);

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.STARVING_BUZZARD, 3, 2),
            TestCards.expectedMinion(TestCards.BLUEGILL_WARRIOR, 2, 1));

        agent.expectHand("p1");
    }

    @Test
    public void testStarvingBuzzardDrawsForBeast() {
        agent.deck("p1", TestCards.MOONFIRE, TestCards.FIERY_WAR_AXE, TestCards.FLAME_OF_AZZINOTH);

        agent.setMana("p1", 10);

        agent.playMinionCard("p1", TestCards.STARVING_BUZZARD, 0);

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.STARVING_BUZZARD, 3, 2));

        agent.playMinionCard("p1", TestCards.STONETUSK_BOAR, 1);

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.STARVING_BUZZARD, 3, 2),
            TestCards.expectedMinion(TestCards.STONETUSK_BOAR, 1, 1));

        agent.expectHand("p1", TestCards.FLAME_OF_AZZINOTH);
    }

    @Test
    public void testStarvingBuzzardDrawsForCopiedBeast() {
        agent.deck("p1", TestCards.MOONFIRE, TestCards.FIERY_WAR_AXE, TestCards.FLAME_OF_AZZINOTH);

        agent.setMana("p1", 10);
        agent.setMana("p2", 10);

        agent.playMinionCard("p2", TestCards.HAUNTED_CREEPER, 0);
        agent.playMinionCard("p1", TestCards.STARVING_BUZZARD, 0);

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.STARVING_BUZZARD, 3, 2));
        agent.expectBoard("p2",
            TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2));

        agent.expectHand("p1");
        agent.expectHand("p2");

        agent.playMinionCard("p1", TestCards.FACELESS_MANIPULATOR, 1, "p2:0");

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.STARVING_BUZZARD, 3, 2),
            TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2));
        agent.expectBoard("p2",
            TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2));

        agent.expectHand("p1", TestCards.FLAME_OF_AZZINOTH);
        agent.expectHand("p2");
    }

    @Test
    public void testBlessingOfWisdom() {
        agent.deck("p1", TestCards.MOONFIRE, TestCards.FIERY_WAR_AXE, TestCards.FLAME_OF_AZZINOTH);
        agent.deck("p2", TestCards.YETI, TestCards.SCARLET_CRUSADER, TestCards.SLAM);

        agent.setMana("p1", 10);

        agent.playMinionCard("p1", TestCards.STONETUSK_BOAR, 0);

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.STONETUSK_BOAR, 1, 1));

        agent.playNonMinionCard("p1", TestCards.BLESSING_OF_WISDOM, "p1:0");

        agent.attack("p1:0", "p2:hero");

        agent.expectHand("p1", TestCards.FLAME_OF_AZZINOTH);

        agent.refreshAttacks();
        agent.attack("p1:0", "p2:hero");

        agent.expectHand("p1", TestCards.FLAME_OF_AZZINOTH, TestCards.FIERY_WAR_AXE);
        agent.expectHand("p2");
    }

    @Test
    public void testBlessingOfWisdomOfOpponent() {
        agent.deck("p1", TestCards.MOONFIRE, TestCards.FIERY_WAR_AXE, TestCards.FLAME_OF_AZZINOTH);
        agent.deck("p2", TestCards.YETI, TestCards.SCARLET_CRUSADER, TestCards.SLAM);

        agent.setMana("p1", 10);
        agent.setMana("p2", 10);

        agent.playMinionCard("p2", TestCards.STONETUSK_BOAR, 0);

        agent.expectBoard("p2",
            TestCards.expectedMinion(TestCards.STONETUSK_BOAR, 1, 1));

        agent.playNonMinionCard("p1", TestCards.BLESSING_OF_WISDOM, "p2:0");

        agent.attack("p2:0", "p1:hero");

        agent.expectHand("p1", TestCards.FLAME_OF_AZZINOTH);

        agent.refreshAttacks();
        agent.attack("p2:0", "p1:hero");

        agent.expectHand("p1", TestCards.FLAME_OF_AZZINOTH, TestCards.FIERY_WAR_AXE);
        agent.expectHand("p2");
    }
}
