package info.hearthsim.brazier;

import info.hearthsim.brazier.utils.BrazierTest;
import info.hearthsim.brazier.utils.TestAgent;
import info.hearthsim.brazier.utils.TestCards;
import org.junit.Test;


public final class ShadowMadnessTest extends BrazierTest {
    @Test
    public void testShadowMadness() {
        agent.setMana("p1", 10);
        agent.setMana("p2", 10);

        agent.playMinionCard("p1", TestCards.DREAD_CORSAIR, 0);
        agent.expectBoard("p1", TestCards.expectedMinion(TestCards.DREAD_CORSAIR, 3, 3, false));
        agent.refreshAttacks();
        agent.expectBoard("p1", TestCards.expectedMinion(TestCards.DREAD_CORSAIR, 3, 3, true));
        agent.attack("p1:0", "p2:hero"); // just to spend the attack
        agent.expectBoard("p1", TestCards.expectedMinion(TestCards.DREAD_CORSAIR, 3, 3, false));

        agent.setCurrentPlayer("p2");

        agent.expectBoard("p1", TestCards.expectedMinion(TestCards.DREAD_CORSAIR, 3, 3, false));
        agent.expectBoard("p2");

        agent.playNonMinionCard("p2", TestCards.SHADOW_MADNESS, "p1:0");

        agent.expectBoard("p1");
        agent.expectBoard("p2", TestCards.expectedMinion(TestCards.DREAD_CORSAIR, 3, 3, true));

        agent.endTurn();

        agent.expectBoard("p1", TestCards.expectedMinion(TestCards.DREAD_CORSAIR, 3, 3, true));
        agent.expectBoard("p2");
    }

    @Test
    public void testAuraIsTakenWithShadowMadness() {
        agent.setMana("p1", 10);
        agent.setMana("p2", 10);

        agent.playMinionCard("p1", TestCards.YETI, 0);
        agent.playMinionCard("p1", TestCards.DIRE_WOLF_ALPHA, 1);
        agent.playMinionCard("p1", TestCards.BLUEGILL_WARRIOR, 2);

        agent.playMinionCard("p2", TestCards.DREAD_CORSAIR, 0);
        agent.playMinionCard("p2", TestCards.HAUNTED_CREEPER, 1);

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.YETI, 5, 5),
            TestCards.expectedMinion(TestCards.DIRE_WOLF_ALPHA, 2, 2),
            TestCards.expectedMinion(TestCards.BLUEGILL_WARRIOR, 3, 1));
        agent.expectBoard("p2",
            TestCards.expectedMinion(TestCards.DREAD_CORSAIR, 3, 3),
            TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2));

        agent.setCurrentPlayer("p2");

        agent.playNonMinionCard("p2", TestCards.SHADOW_MADNESS, "p1:1");

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.YETI, 4, 5),
            TestCards.expectedMinion(TestCards.BLUEGILL_WARRIOR, 2, 1));
        agent.expectBoard("p2",
            TestCards.expectedMinion(TestCards.DREAD_CORSAIR, 3, 3),
            TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 2, 2),
            TestCards.expectedMinion(TestCards.DIRE_WOLF_ALPHA, 2, 2, true));

        agent.endTurn();

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.YETI, 4, 5),
            TestCards.expectedMinion(TestCards.BLUEGILL_WARRIOR, 3, 1),
            TestCards.expectedMinion(TestCards.DIRE_WOLF_ALPHA, 2, 2));
        agent.expectBoard("p2",
            TestCards.expectedMinion(TestCards.DREAD_CORSAIR, 3, 3),
            TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2));
    }

    @Test
    public void testDeathRattleIsStolenByShadowMadness() {
        agent.setMana("p1", 10);
        agent.setMana("p2", 10);

        agent.playMinionCard("p1", TestCards.FIRE_ELEMENTAL, 0);
        agent.setMana("p1", 10);
        agent.playMinionCard("p1", TestCards.SLUDGE_BELCHER, 1);

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.FIRE_ELEMENTAL, 6, 5),
            TestCards.expectedMinion(TestCards.SLUDGE_BELCHER, 3, 5));
        agent.expectBoard("p2");

        agent.setCurrentPlayer("p2");

        agent.playNonMinionCard("p2", TestCards.SHADOW_MADNESS, "p1:1");

        agent.expectBoard("p1", TestCards.expectedMinion(TestCards.FIRE_ELEMENTAL, 6, 5));
        agent.expectBoard("p2", TestCards.expectedMinion(TestCards.SLUDGE_BELCHER, 3, 5, true));

        agent.attack("p2:0", "p1:0");

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.FIRE_ELEMENTAL, 6, 2));
        agent.expectBoard("p2", TestCards.expectedMinion(TestCards.SLIME, 1, 2, false));

        agent.endTurn();

        agent.expectBoard("p1", TestCards.expectedMinion(TestCards.FIRE_ELEMENTAL, 6, 2));
        agent.expectBoard("p2", TestCards.expectedMinion(TestCards.SLIME, 1, 2, false));
    }

    @Test
    public void testSilencingShadowMadness() {
        agent.setMana("p1", 10);
        agent.setMana("p2", 10);

        agent.playMinionCard("p1", TestCards.DREAD_CORSAIR, 0);
        agent.refreshAttacks();
        agent.attack("p1:0", "p2:hero"); // just to spend the attack

        agent.setCurrentPlayer("p2");

        agent.expectBoard("p1", TestCards.expectedMinion(TestCards.DREAD_CORSAIR, 3, 3, false));
        agent.expectBoard("p2");

        agent.playNonMinionCard("p2", TestCards.SHADOW_MADNESS, "p1:0");

        agent.expectBoard("p1");
        agent.expectBoard("p2",
            TestCards.expectedMinion(TestCards.DREAD_CORSAIR, 3, 3, true));

        agent.playNonMinionCard("p2", TestCards.SILENCE, "p2:0");

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.DREAD_CORSAIR, 3, 3));
        agent.expectBoard("p2");

        agent.endTurn();

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.DREAD_CORSAIR, 3, 3));
        agent.expectBoard("p2");
    }

    @Test
    public void testShadowMadnessFullBoardAfterReturn() {
        agent.setMana("p1", 10);
        agent.setMana("p2", 10);

        agent.playMinionCard("p1", TestCards.SLUDGE_BELCHER, 0);
        agent.playMinionCard("p1", TestCards.HAUNTED_CREEPER, 1);
        agent.playMinionCard("p1", TestCards.HAUNTED_CREEPER, 2);
        agent.setMana("p1", 10);
        agent.playMinionCard("p1", TestCards.HAUNTED_CREEPER, 3);
        agent.playMinionCard("p1", TestCards.HAUNTED_CREEPER, 4);
        agent.playMinionCard("p1", TestCards.HAUNTED_CREEPER, 5);
        agent.playMinionCard("p1", TestCards.HAUNTED_CREEPER, 6);

        agent.setCurrentPlayer("p2");

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.SLUDGE_BELCHER, 3, 5),
            TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
            TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
            TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
            TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
            TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
            TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2));
        agent.expectBoard("p2");

        agent.playNonMinionCard("p2", TestCards.SHADOW_MADNESS, "p1:0");

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
            TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
            TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
            TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
            TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
            TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2));
        agent.expectBoard("p2",
            TestCards.expectedMinion(TestCards.SLUDGE_BELCHER, 3, 5));

        agent.attack("p2:0", "p1:0");

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.SPECTRAL_SPIDER, 1, 1),
            TestCards.expectedMinion(TestCards.SPECTRAL_SPIDER, 1, 1),
            TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
            TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
            TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
            TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
            TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2));
        agent.expectBoard("p2",
            TestCards.expectedMinion(TestCards.SLUDGE_BELCHER, 3, 4));

        agent.endTurn();

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.SPECTRAL_SPIDER, 1, 1),
            TestCards.expectedMinion(TestCards.SPECTRAL_SPIDER, 1, 1),
            TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
            TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
            TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
            TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
            TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2));
        agent.expectBoard("p2",
            TestCards.expectedMinion(TestCards.SLIME, 1, 2));
    }

    @Test
    public void testShadowMadnessWithIllidan() {
        agent.setMana("p1", 10);
        agent.setMana("p2", 10);

        agent.playMinionCard("p1", TestCards.SLUDGE_BELCHER, 0);
        agent.playMinionCard("p1", TestCards.HAUNTED_CREEPER, 1);
        agent.playMinionCard("p1", TestCards.HAUNTED_CREEPER, 2);
        agent.setMana("p1", 10);
        agent.playMinionCard("p1", TestCards.HAUNTED_CREEPER, 3);
        agent.playMinionCard("p1", TestCards.HAUNTED_CREEPER, 4);
        agent.playMinionCard("p1", TestCards.HAUNTED_CREEPER, 5);
        agent.playMinionCard("p1", TestCards.HAUNTED_CREEPER, 6);

        agent.playMinionCard("p2", TestCards.STONETUSK_BOAR, 0);
        agent.playMinionCard("p2", TestCards.STONETUSK_BOAR, 1);
        agent.playMinionCard("p2", TestCards.STONETUSK_BOAR, 2);
        agent.playMinionCard("p2", TestCards.STONETUSK_BOAR, 3);
        agent.playMinionCard("p2", TestCards.STONETUSK_BOAR, 4);
        agent.setMana("p2", 10);
        agent.playMinionCard("p2", TestCards.ILLIDAN_STORMRAGE, 5);

        agent.setCurrentPlayer("p2");

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.SLUDGE_BELCHER, 3, 5),
            TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
            TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
            TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
            TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
            TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
            TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2));
        agent.expectBoard("p2",
            TestCards.expectedMinion(TestCards.STONETUSK_BOAR, 1, 1),
            TestCards.expectedMinion(TestCards.STONETUSK_BOAR, 1, 1),
            TestCards.expectedMinion(TestCards.STONETUSK_BOAR, 1, 1),
            TestCards.expectedMinion(TestCards.STONETUSK_BOAR, 1, 1),
            TestCards.expectedMinion(TestCards.STONETUSK_BOAR, 1, 1),
            TestCards.expectedMinion(TestCards.ILLIDAN_STORMRAGE, 7, 5));

        agent.playNonMinionCard("p2", TestCards.SHADOW_MADNESS, "p1:0");

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.SLIME, 1, 2),
            TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
            TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
            TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
            TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
            TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
            TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2));
        agent.expectBoard("p2",
            TestCards.expectedMinion(TestCards.STONETUSK_BOAR, 1, 1),
            TestCards.expectedMinion(TestCards.STONETUSK_BOAR, 1, 1),
            TestCards.expectedMinion(TestCards.STONETUSK_BOAR, 1, 1),
            TestCards.expectedMinion(TestCards.STONETUSK_BOAR, 1, 1),
            TestCards.expectedMinion(TestCards.STONETUSK_BOAR, 1, 1),
            TestCards.expectedMinion(TestCards.ILLIDAN_STORMRAGE, 7, 5),
            TestCards.expectedMinion(TestCards.FLAME_OF_AZZINOTH, 2, 1));

        agent.endTurn();

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.SLIME, 1, 2),
            TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
            TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
            TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
            TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
            TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
            TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2));
        agent.expectBoard("p2",
            TestCards.expectedMinion(TestCards.STONETUSK_BOAR, 1, 1),
            TestCards.expectedMinion(TestCards.STONETUSK_BOAR, 1, 1),
            TestCards.expectedMinion(TestCards.STONETUSK_BOAR, 1, 1),
            TestCards.expectedMinion(TestCards.STONETUSK_BOAR, 1, 1),
            TestCards.expectedMinion(TestCards.STONETUSK_BOAR, 1, 1),
            TestCards.expectedMinion(TestCards.ILLIDAN_STORMRAGE, 7, 5),
            TestCards.expectedMinion(TestCards.FLAME_OF_AZZINOTH, 2, 1));
    }


    @Test
    public void testShadowMadnessWithEndOfTurnEffects() {
        agent.setMana("p1", 10);
        agent.setMana("p2", 10);

        agent.addToHand("p1", TestCards.YETI, TestCards.SLAM, TestCards.WHIRLWIND);
        expectManaCost(agent, "p1", 4, 2, 1);

        agent.addToHand("p2", TestCards.SLUDGE_BELCHER, TestCards.PYROBLAST, TestCards.DIRE_WOLF_ALPHA);
        expectManaCost(agent, "p2", 5, 10, 2);

        agent.playMinionCard("p1", TestCards.EMPEROR_THAURISSAN, 0);

        agent.setCurrentPlayer("p2");

        agent.playMinionCard("p2", TestCards.ALDOR_PEACEKEEPER, 0, "p1:0");

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.EMPEROR_THAURISSAN, 1, 5));
        agent.expectBoard("p2",
            TestCards.expectedMinion(TestCards.ALDOR_PEACEKEEPER, 3, 3));

        agent.playNonMinionCard("p2", TestCards.SHADOW_MADNESS, "p1:0");

        agent.expectBoard("p1");
        agent.expectBoard("p2",
            TestCards.expectedMinion(TestCards.ALDOR_PEACEKEEPER, 3, 3),
            TestCards.expectedMinion(TestCards.EMPEROR_THAURISSAN, 1, 5));

        agent.endTurn();

        expectManaCost(agent, "p1", 4, 2, 1);
        expectManaCost(agent, "p2", 4, 9, 1);

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.EMPEROR_THAURISSAN, 1, 5));
        agent.expectBoard("p2",
            TestCards.expectedMinion(TestCards.ALDOR_PEACEKEEPER, 3, 3));

        agent.endTurn();

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.EMPEROR_THAURISSAN, 1, 5));
        agent.expectBoard("p2",
            TestCards.expectedMinion(TestCards.ALDOR_PEACEKEEPER, 3, 3));

        expectManaCost(agent, "p1", 3, 1, 0);
        expectManaCost(agent, "p2", 4, 9, 1);
    }

    private static void expectManaCost(TestAgent script, String playerName, int... manaCosts) {
        ManaCostManipulationTest.expectManaCost(script, playerName, manaCosts);
    }
}
