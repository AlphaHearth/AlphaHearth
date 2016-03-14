package info.hearthsim.brazier;

import org.junit.Test;


public final class ShadowMadnessTest {
    @Test
    public void testShadowMadness() {
        PlayScript.testScript((script) -> {
            script.setMana("p1", 10);
            script.setMana("p2", 10);

            script.playMinionCard("p1", TestCards.DREAD_CORSAIR, 0);
            script.expectBoard("p1", TestCards.expectedMinion(TestCards.DREAD_CORSAIR, 3, 3, false));
            script.refreshAttacks();
            script.expectBoard("p1", TestCards.expectedMinion(TestCards.DREAD_CORSAIR, 3, 3, true));
            script.attack("p1:0", "p2:hero"); // just to spend the attack
            script.expectBoard("p1", TestCards.expectedMinion(TestCards.DREAD_CORSAIR, 3, 3, false));

            script.setCurrentPlayer("p2");

            script.expectBoard("p1", TestCards.expectedMinion(TestCards.DREAD_CORSAIR, 3, 3, false));
            script.expectBoard("p2");

            script.playNonMinionCard("p2", TestCards.SHADOW_MADNESS, "p1:0");

            script.expectBoard("p1");
            script.expectBoard("p2", TestCards.expectedMinion(TestCards.DREAD_CORSAIR, 3, 3, true));

            script.endTurn();

            script.expectBoard("p1", TestCards.expectedMinion(TestCards.DREAD_CORSAIR, 3, 3, true));
            script.expectBoard("p2");
        });
    }

    @Test
    public void testAuraIsTakenWithShadowMadness() {
        PlayScript.testScript((script) -> {
            script.setMana("p1", 10);
            script.setMana("p2", 10);

            script.playMinionCard("p1", TestCards.YETI, 0);
            script.playMinionCard("p1", TestCards.DIRE_WOLF_ALPHA, 1);
            script.playMinionCard("p1", TestCards.BLUEGILL_WARRIOR, 2);

            script.playMinionCard("p2", TestCards.DREAD_CORSAIR, 0);
            script.playMinionCard("p2", TestCards.HAUNTED_CREEPER, 1);

            script.expectBoard("p1",
                TestCards.expectedMinion(TestCards.YETI, 5, 5),
                TestCards.expectedMinion(TestCards.DIRE_WOLF_ALPHA, 2, 2),
                TestCards.expectedMinion(TestCards.BLUEGILL_WARRIOR, 3, 1));
            script.expectBoard("p2",
                TestCards.expectedMinion(TestCards.DREAD_CORSAIR, 3, 3),
                TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2));

            script.setCurrentPlayer("p2");

            script.playNonMinionCard("p2", TestCards.SHADOW_MADNESS, "p1:1");

            script.expectBoard("p1",
                TestCards.expectedMinion(TestCards.YETI, 4, 5),
                TestCards.expectedMinion(TestCards.BLUEGILL_WARRIOR, 2, 1));
            script.expectBoard("p2",
                TestCards.expectedMinion(TestCards.DREAD_CORSAIR, 3, 3),
                TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 2, 2),
                TestCards.expectedMinion(TestCards.DIRE_WOLF_ALPHA, 2, 2, true));

            script.endTurn();

            script.expectBoard("p1",
                TestCards.expectedMinion(TestCards.YETI, 4, 5),
                TestCards.expectedMinion(TestCards.BLUEGILL_WARRIOR, 3, 1),
                TestCards.expectedMinion(TestCards.DIRE_WOLF_ALPHA, 2, 2));
            script.expectBoard("p2",
                TestCards.expectedMinion(TestCards.DREAD_CORSAIR, 3, 3),
                TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2));
        });
    }

    @Test
    public void testDeathRattleIsStolenByShadowMadness() {
        PlayScript.testScript((script) -> {
            script.setMana("p1", 10);
            script.setMana("p2", 10);

            script.playMinionCard("p1", TestCards.FIRE_ELEMENTAL, 0);
            script.setMana("p1", 10);
            script.playMinionCard("p1", TestCards.SLUDGE_BELCHER, 1);

            script.expectBoard("p1",
                TestCards.expectedMinion(TestCards.FIRE_ELEMENTAL, 6, 5),
                TestCards.expectedMinion(TestCards.SLUDGE_BELCHER, 3, 5));
            script.expectBoard("p2");

            script.setCurrentPlayer("p2");

            script.playNonMinionCard("p2", TestCards.SHADOW_MADNESS, "p1:1");

            script.expectBoard("p1", TestCards.expectedMinion(TestCards.FIRE_ELEMENTAL, 6, 5));
            script.expectBoard("p2", TestCards.expectedMinion(TestCards.SLUDGE_BELCHER, 3, 5, true));

            script.attack("p2:0", "p1:0");

            script.expectBoard("p1",
                TestCards.expectedMinion(TestCards.FIRE_ELEMENTAL, 6, 2));
            script.expectBoard("p2", TestCards.expectedMinion(TestCards.SLIME, 1, 2, false));

            script.endTurn();

            script.expectBoard("p1", TestCards.expectedMinion(TestCards.FIRE_ELEMENTAL, 6, 2));
            script.expectBoard("p2", TestCards.expectedMinion(TestCards.SLIME, 1, 2, false));
        });
    }

    @Test
    public void testSilencingShadowMadness() {
        PlayScript.testScript((script) -> {
            script.setMana("p1", 10);
            script.setMana("p2", 10);

            script.playMinionCard("p1", TestCards.DREAD_CORSAIR, 0);
            script.refreshAttacks();
            script.attack("p1:0", "p2:hero"); // just to spend the attack

            script.setCurrentPlayer("p2");

            script.expectBoard("p1", TestCards.expectedMinion(TestCards.DREAD_CORSAIR, 3, 3, false));
            script.expectBoard("p2");

            script.playNonMinionCard("p2", TestCards.SHADOW_MADNESS, "p1:0");

            script.expectBoard("p1");
            script.expectBoard("p2",
                TestCards.expectedMinion(TestCards.DREAD_CORSAIR, 3, 3, true));

            script.playNonMinionCard("p2", TestCards.SILENCE, "p2:0");

            script.expectBoard("p1",
                TestCards.expectedMinion(TestCards.DREAD_CORSAIR, 3, 3));
            script.expectBoard("p2");

            script.endTurn();

            script.expectBoard("p1",
                TestCards.expectedMinion(TestCards.DREAD_CORSAIR, 3, 3));
            script.expectBoard("p2");
        });
    }

    @Test
    public void testShadowMadnessFullBoardAfterReturn() {
        PlayScript.testScript((script) -> {
            script.setMana("p1", 10);
            script.setMana("p2", 10);

            script.playMinionCard("p1", TestCards.SLUDGE_BELCHER, 0);
            script.playMinionCard("p1", TestCards.HAUNTED_CREEPER, 1);
            script.playMinionCard("p1", TestCards.HAUNTED_CREEPER, 2);
            script.setMana("p1", 10);
            script.playMinionCard("p1", TestCards.HAUNTED_CREEPER, 3);
            script.playMinionCard("p1", TestCards.HAUNTED_CREEPER, 4);
            script.playMinionCard("p1", TestCards.HAUNTED_CREEPER, 5);
            script.playMinionCard("p1", TestCards.HAUNTED_CREEPER, 6);

            script.setCurrentPlayer("p2");

            script.expectBoard("p1",
                TestCards.expectedMinion(TestCards.SLUDGE_BELCHER, 3, 5),
                TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
                TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
                TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
                TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
                TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
                TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2));
            script.expectBoard("p2");

            script.playNonMinionCard("p2", TestCards.SHADOW_MADNESS, "p1:0");

            script.expectBoard("p1",
                TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
                TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
                TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
                TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
                TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
                TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2));
            script.expectBoard("p2",
                TestCards.expectedMinion(TestCards.SLUDGE_BELCHER, 3, 5));

            script.attack("p2:0", "p1:0");

            script.expectBoard("p1",
                TestCards.expectedMinion(TestCards.SPECTRAL_SPIDER, 1, 1),
                TestCards.expectedMinion(TestCards.SPECTRAL_SPIDER, 1, 1),
                TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
                TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
                TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
                TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
                TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2));
            script.expectBoard("p2",
                TestCards.expectedMinion(TestCards.SLUDGE_BELCHER, 3, 4));

            script.endTurn();

            script.expectBoard("p1",
                TestCards.expectedMinion(TestCards.SPECTRAL_SPIDER, 1, 1),
                TestCards.expectedMinion(TestCards.SPECTRAL_SPIDER, 1, 1),
                TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
                TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
                TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
                TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
                TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2));
            script.expectBoard("p2",
                TestCards.expectedMinion(TestCards.SLIME, 1, 2));
        });
    }

    @Test
    public void testShadowMadnessWithIllidan() {
        PlayScript.testScript((script) -> {
            script.setMana("p1", 10);
            script.setMana("p2", 10);

            script.playMinionCard("p1", TestCards.SLUDGE_BELCHER, 0);
            script.playMinionCard("p1", TestCards.HAUNTED_CREEPER, 1);
            script.playMinionCard("p1", TestCards.HAUNTED_CREEPER, 2);
            script.setMana("p1", 10);
            script.playMinionCard("p1", TestCards.HAUNTED_CREEPER, 3);
            script.playMinionCard("p1", TestCards.HAUNTED_CREEPER, 4);
            script.playMinionCard("p1", TestCards.HAUNTED_CREEPER, 5);
            script.playMinionCard("p1", TestCards.HAUNTED_CREEPER, 6);

            script.playMinionCard("p2", TestCards.STONETUSK_BOAR, 0);
            script.playMinionCard("p2", TestCards.STONETUSK_BOAR, 1);
            script.playMinionCard("p2", TestCards.STONETUSK_BOAR, 2);
            script.playMinionCard("p2", TestCards.STONETUSK_BOAR, 3);
            script.playMinionCard("p2", TestCards.STONETUSK_BOAR, 4);
            script.setMana("p2", 10);
            script.playMinionCard("p2", TestCards.ILLIDAN_STORMRAGE, 5);

            script.setCurrentPlayer("p2");

            script.expectBoard("p1",
                TestCards.expectedMinion(TestCards.SLUDGE_BELCHER, 3, 5),
                TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
                TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
                TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
                TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
                TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
                TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2));
            script.expectBoard("p2",
                TestCards.expectedMinion(TestCards.STONETUSK_BOAR, 1, 1),
                TestCards.expectedMinion(TestCards.STONETUSK_BOAR, 1, 1),
                TestCards.expectedMinion(TestCards.STONETUSK_BOAR, 1, 1),
                TestCards.expectedMinion(TestCards.STONETUSK_BOAR, 1, 1),
                TestCards.expectedMinion(TestCards.STONETUSK_BOAR, 1, 1),
                TestCards.expectedMinion(TestCards.ILLIDAN_STORMRAGE, 7, 5));

            script.playNonMinionCard("p2", TestCards.SHADOW_MADNESS, "p1:0");

            script.expectBoard("p1",
                TestCards.expectedMinion(TestCards.SLIME, 1, 2),
                TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
                TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
                TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
                TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
                TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
                TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2));
            script.expectBoard("p2",
                TestCards.expectedMinion(TestCards.STONETUSK_BOAR, 1, 1),
                TestCards.expectedMinion(TestCards.STONETUSK_BOAR, 1, 1),
                TestCards.expectedMinion(TestCards.STONETUSK_BOAR, 1, 1),
                TestCards.expectedMinion(TestCards.STONETUSK_BOAR, 1, 1),
                TestCards.expectedMinion(TestCards.STONETUSK_BOAR, 1, 1),
                TestCards.expectedMinion(TestCards.ILLIDAN_STORMRAGE, 7, 5),
                TestCards.expectedMinion(TestCards.FLAME_OF_AZZINOTH, 2, 1));

            script.endTurn();

            script.expectBoard("p1",
                TestCards.expectedMinion(TestCards.SLIME, 1, 2),
                TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
                TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
                TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
                TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
                TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2),
                TestCards.expectedMinion(TestCards.HAUNTED_CREEPER, 1, 2));
            script.expectBoard("p2",
                TestCards.expectedMinion(TestCards.STONETUSK_BOAR, 1, 1),
                TestCards.expectedMinion(TestCards.STONETUSK_BOAR, 1, 1),
                TestCards.expectedMinion(TestCards.STONETUSK_BOAR, 1, 1),
                TestCards.expectedMinion(TestCards.STONETUSK_BOAR, 1, 1),
                TestCards.expectedMinion(TestCards.STONETUSK_BOAR, 1, 1),
                TestCards.expectedMinion(TestCards.ILLIDAN_STORMRAGE, 7, 5),
                TestCards.expectedMinion(TestCards.FLAME_OF_AZZINOTH, 2, 1));
        });
    }


    @Test
    public void testShadowMadnessWithEndOfTurnEffects() {
        PlayScript.testScript((script) -> {
            script.setMana("p1", 10);
            script.setMana("p2", 10);

            script.addToHand("p1", TestCards.YETI, TestCards.SLAM, TestCards.WHIRLWIND);
            expectManaCost(script, "p1", 4, 2, 1);

            script.addToHand("p2", TestCards.SLUDGE_BELCHER, TestCards.PYROBLAST, TestCards.DIRE_WOLF_ALPHA);
            expectManaCost(script, "p2", 5, 10, 2);

            script.playMinionCard("p1", TestCards.EMPEROR_THAURISSAN, 0);

            script.setCurrentPlayer("p2");

            script.playMinionCard("p2", TestCards.ALDOR_PEACEKEEPER, 0, "p1:0");

            script.expectBoard("p1",
                TestCards.expectedMinion(TestCards.EMPEROR_THAURISSAN, 1, 5));
            script.expectBoard("p2",
                TestCards.expectedMinion(TestCards.ALDOR_PEACEKEEPER, 3, 3));

            script.playNonMinionCard("p2", TestCards.SHADOW_MADNESS, "p1:0");

            script.expectBoard("p1");
            script.expectBoard("p2",
                TestCards.expectedMinion(TestCards.ALDOR_PEACEKEEPER, 3, 3),
                TestCards.expectedMinion(TestCards.EMPEROR_THAURISSAN, 1, 5));

            script.endTurn();

            expectManaCost(script, "p1", 4, 2, 1);
            expectManaCost(script, "p2", 4, 9, 1);

            script.expectBoard("p1",
                TestCards.expectedMinion(TestCards.EMPEROR_THAURISSAN, 1, 5));
            script.expectBoard("p2",
                TestCards.expectedMinion(TestCards.ALDOR_PEACEKEEPER, 3, 3));

            script.endTurn();

            script.expectBoard("p1",
                TestCards.expectedMinion(TestCards.EMPEROR_THAURISSAN, 1, 5));
            script.expectBoard("p2",
                TestCards.expectedMinion(TestCards.ALDOR_PEACEKEEPER, 3, 3));

            expectManaCost(script, "p1", 3, 1, 0);
            expectManaCost(script, "p2", 4, 9, 1);
        });
    }

    private static void expectManaCost(PlayScript script, String playerName, int... manaCosts) {
        ManaCostManipulationTest.expectManaCost(script, playerName, manaCosts);
    }
}
