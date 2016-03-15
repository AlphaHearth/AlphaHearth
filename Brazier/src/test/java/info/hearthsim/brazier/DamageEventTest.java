package info.hearthsim.brazier;

import info.hearthsim.brazier.utils.BrazierTest;
import info.hearthsim.brazier.utils.TestCards;
import org.junit.Test;

public final class DamageEventTest extends BrazierTest {
    @Test
    public void testWhirlwind() {
        agent.expectHeroHp("p1", 30, 0);
        agent.expectHeroHp("p2", 30, 0);

        agent.setMana("p1", 10);
        agent.playMinionCard("p1", TestCards.FROTHING_BERSERKER, 0);
        agent.playMinionCard("p1", TestCards.YETI, 0);
        agent.setMana("p1", 10);
        agent.playMinionCard("p1", TestCards.YETI, 0);
        agent.playMinionCard("p1", TestCards.YETI, 0);
        agent.setMana("p1", 10);
        agent.playMinionCard("p1", TestCards.YETI, 0);
        agent.playMinionCard("p1", TestCards.YETI, 0);
        agent.setMana("p1", 10);
        agent.playMinionCard("p1", TestCards.FROTHING_BERSERKER, 0);

        agent.setMana("p2", 10);
        agent.playMinionCard("p2", TestCards.YETI, 0);
        agent.playMinionCard("p2", TestCards.YETI, 0);
        agent.setMana("p2", 10);
        agent.playMinionCard("p2", TestCards.YETI, 0);
        agent.playMinionCard("p2", TestCards.YETI, 0);
        agent.setMana("p2", 10);
        agent.playMinionCard("p2", TestCards.YETI, 0);
        agent.playMinionCard("p2", TestCards.YETI, 0);
        agent.setMana("p2", 10);
        agent.playMinionCard("p2", TestCards.YETI, 0);

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.FROTHING_BERSERKER, 2, 4),
            TestCards.expectedMinion(TestCards.YETI, 4, 5),
            TestCards.expectedMinion(TestCards.YETI, 4, 5),
            TestCards.expectedMinion(TestCards.YETI, 4, 5),
            TestCards.expectedMinion(TestCards.YETI, 4, 5),
            TestCards.expectedMinion(TestCards.YETI, 4, 5),
            TestCards.expectedMinion(TestCards.FROTHING_BERSERKER, 2, 4));

        agent.expectBoard("p2",
            TestCards.expectedMinion(TestCards.YETI, 4, 5),
            TestCards.expectedMinion(TestCards.YETI, 4, 5),
            TestCards.expectedMinion(TestCards.YETI, 4, 5),
            TestCards.expectedMinion(TestCards.YETI, 4, 5),
            TestCards.expectedMinion(TestCards.YETI, 4, 5),
            TestCards.expectedMinion(TestCards.YETI, 4, 5),
            TestCards.expectedMinion(TestCards.YETI, 4, 5));

        agent.playCard("p1", TestCards.WHIRLWIND);

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.FROTHING_BERSERKER, 16, 3),
            TestCards.expectedMinion(TestCards.YETI, 4, 4),
            TestCards.expectedMinion(TestCards.YETI, 4, 4),
            TestCards.expectedMinion(TestCards.YETI, 4, 4),
            TestCards.expectedMinion(TestCards.YETI, 4, 4),
            TestCards.expectedMinion(TestCards.YETI, 4, 4),
            TestCards.expectedMinion(TestCards.FROTHING_BERSERKER, 16, 3));

        agent.expectBoard("p2",
            TestCards.expectedMinion(TestCards.YETI, 4, 4),
            TestCards.expectedMinion(TestCards.YETI, 4, 4),
            TestCards.expectedMinion(TestCards.YETI, 4, 4),
            TestCards.expectedMinion(TestCards.YETI, 4, 4),
            TestCards.expectedMinion(TestCards.YETI, 4, 4),
            TestCards.expectedMinion(TestCards.YETI, 4, 4),
            TestCards.expectedMinion(TestCards.YETI, 4, 4));

        agent.expectHeroHp("p1", 30, 0);
        agent.expectHeroHp("p2", 30, 0);
    }

    @Test
    public void testFrothingBerserkerVsForthingBerserker() {
        agent.setMana("p1", 10);
        agent.setMana("p2", 10);

        agent.playMinionCard("p1", TestCards.FROTHING_BERSERKER, 0);
        agent.playMinionCard("p1", TestCards.FROTHING_BERSERKER, 0);
        agent.playMinionCard("p2", TestCards.FROTHING_BERSERKER, 0);

        agent.refreshAttacks();

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.FROTHING_BERSERKER, 2, 4),
            TestCards.expectedMinion(TestCards.FROTHING_BERSERKER, 2, 4));

        agent.expectBoard("p2",
            TestCards.expectedMinion(TestCards.FROTHING_BERSERKER, 2, 4));

        agent.attack("p1:0", "p2:0");

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.FROTHING_BERSERKER, 4, 2),
            TestCards.expectedMinion(TestCards.FROTHING_BERSERKER, 4, 4));

        agent.expectBoard("p2",
            TestCards.expectedMinion(TestCards.FROTHING_BERSERKER, 4, 2));
    }

    @Test
    public void testGurubashiBerserker() {
        agent.setMana("p1", 10);
        agent.setMana("p2", 10);

        agent.playMinionCard("p1", TestCards.GURUBASHI_BERSERKER, 0);
        agent.playMinionCard("p1", TestCards.YETI, 0);
        agent.playMinionCard("p2", TestCards.YETI, 0);
        agent.playMinionCard("p2", TestCards.YETI, 0);

        agent.refreshAttacks();

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.YETI, 4, 5),
            TestCards.expectedMinion(TestCards.GURUBASHI_BERSERKER, 2, 7));

        agent.expectBoard("p2",
            TestCards.expectedMinion(TestCards.YETI, 4, 5),
            TestCards.expectedMinion(TestCards.YETI, 4, 5));

        agent.attack("p2:0", "p1:0");

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.YETI, 4, 1),
            TestCards.expectedMinion(TestCards.GURUBASHI_BERSERKER, 2, 7));

        agent.expectBoard("p2",
            TestCards.expectedMinion(TestCards.YETI, 4, 1),
            TestCards.expectedMinion(TestCards.YETI, 4, 5));

        agent.attack("p2:1", "p1:1");

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.YETI, 4, 1),
            TestCards.expectedMinion(TestCards.GURUBASHI_BERSERKER, 5, 3));

        agent.expectBoard("p2",
            TestCards.expectedMinion(TestCards.YETI, 4, 1),
            TestCards.expectedMinion(TestCards.YETI, 4, 3));
    }

    @Test
    public void testGrimPatron() {
        agent.setMana("p1", 10);
        agent.setMana("p2", 10);

        agent.playMinionCard("p1", TestCards.GRIM_PATRON, 0);
        agent.playMinionCard("p1", TestCards.GRIM_PATRON, 0);
        agent.playMinionCard("p2", TestCards.GRIM_PATRON, 0);

        agent.setMana("p1", 10);
        agent.setMana("p2", 10);

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 3),  // 2.
            TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 3)); // 1.

        agent.expectBoard("p2",
            TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 3)); // 1.

        agent.playCard("p2", TestCards.WHIRLWIND);

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 2),  // 2.
            TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 3),  // 4.
            TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 2),  // 1.
            TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 3)); // 3.

        agent.expectBoard("p2",
            TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 2),  // 1.
            TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 3)); // 2.

        agent.playCard("p2", TestCards.WHIRLWIND);

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 1),  // 2.
            TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 3),  // 6.
            TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 2),  // 4.
            TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 1),  // 1.
            TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 3),  // 5.
            TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 2),  // 3.
            TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 3)); // 7.

        agent.expectBoard("p2",
            TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 1),  // 1.
            TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 3),  // 3.
            TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 2),  // 2.
            TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 3)); // 4.

        agent.playCard("p2", TestCards.WHIRLWIND);

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 2),  // 6.
            TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 1),  // 4.
            TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 2),  // 5.
            TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 1),  // 3.
            TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 2)); // 7.

        agent.expectBoard("p2",
            TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 2),  // 3.
            TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 3),  // 6.
            TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 1),  // 2.
            TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 3),  // 5.
            TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 2),  // 7.
            TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 3)); // 4.
    }

    @Test
    public void testExplosiveShotAtomic() {
        agent.setMana("p1", 10);

        agent.playMinionCard("p1", TestCards.VOIDWALKER, 0);
        agent.playMinionCard("p1", TestCards.GRIM_PATRON, 1);
        agent.playMinionCard("p1", TestCards.VOIDWALKER, 2);

        agent.setMana("p1", 10);
        agent.playNonMinionCard("p1", TestCards.BLESSING_OF_KINGS, "p1:1");

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.VOIDWALKER, 1, 3),
            TestCards.expectedMinion(TestCards.GRIM_PATRON, 7, 7),
            TestCards.expectedMinion(TestCards.VOIDWALKER, 1, 3));

        agent.setMana("p2", 10);
        agent.playNonMinionCard("p2", TestCards.EXPLOSIVE_SHOT, "p1:1");

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.VOIDWALKER, 1, 1),
            TestCards.expectedMinion(TestCards.GRIM_PATRON, 7, 2),
            TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 3),
            TestCards.expectedMinion(TestCards.VOIDWALKER, 1, 1));
    }

    @Test
    public void testEmperorCobra() {
        agent.setMana("p1", 10);
        agent.setMana("p2", 10);

        agent.playMinionCard("p1", TestCards.EMPEROR_COBRA, 0);
        agent.playMinionCard("p2", TestCards.STORMWIND_CHAMPION, 0);

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.EMPEROR_COBRA, 2, 3));
        agent.expectBoard("p2",
            TestCards.expectedMinion(TestCards.STORMWIND_CHAMPION, 6, 6));

        agent.refreshAttacks();
        agent.attack("p1:0", "p2:0");

        agent.expectBoard("p1");
        agent.expectBoard("p2");
    }

    @Test
    public void testWaterElementalAttacks() {
        agent.setMana("p1", 10);
        agent.setMana("p2", 10);

        agent.playMinionCard("p1", TestCards.WATER_ELEMENTAL, 0);
        agent.playMinionCard("p2", TestCards.SLUDGE_BELCHER, 0);

        agent.setCurrentPlayer("p1");
        agent.refreshAttacks();

        agent.expectBoard("p1",
            TestCards.expectedMinionWithFlags(TestCards.WATER_ELEMENTAL, 3, 6));
        agent.expectBoard("p2",
            TestCards.expectedMinionWithFlags(TestCards.SLUDGE_BELCHER, 3, 5, "taunt"));

        agent.attack("p1:0", "p2:0");

        agent.expectBoard("p1",
            TestCards.expectedMinionWithFlags(TestCards.WATER_ELEMENTAL, 3, 3));
        agent.expectBoard("p2",
            TestCards.expectedMinionWithFlags(TestCards.SLUDGE_BELCHER, 3, 2, "taunt", "frozen"));

        agent.endTurn(); // p1

        agent.expectBoard("p1",
            TestCards.expectedMinionWithFlags(TestCards.WATER_ELEMENTAL, 3, 3));
        agent.expectBoard("p2",
            TestCards.expectedMinionWithFlags(TestCards.SLUDGE_BELCHER, 3, 2, "taunt", "frozen"));

        agent.endTurn(); // p2
        agent.endTurn(); // p1

        agent.expectBoard("p1",
            TestCards.expectedMinionWithFlags(TestCards.WATER_ELEMENTAL, 3, 3));
        agent.expectBoard("p2",
            TestCards.expectedMinionWithFlags(TestCards.SLUDGE_BELCHER, 3, 2, "taunt"));
    }

    @Test
    public void testWaterElementalAttacked() {
        agent.setMana("p1", 10);
        agent.setMana("p2", 10);

        agent.playMinionCard("p1", TestCards.WATER_ELEMENTAL, 0);
        agent.playMinionCard("p2", TestCards.SLUDGE_BELCHER, 0);

        agent.setCurrentPlayer("p2");
        agent.refreshAttacks();

        agent.expectBoard("p1",
            TestCards.expectedMinionWithFlags(TestCards.WATER_ELEMENTAL, 3, 6));
        agent.expectBoard("p2",
            TestCards.expectedMinionWithFlags(TestCards.SLUDGE_BELCHER, 3, 5, "taunt"));

        agent.attack("p2:0", "p1:0");

        agent.expectBoard("p1",
            TestCards.expectedMinionWithFlags(TestCards.WATER_ELEMENTAL, 3, 3));
        agent.expectBoard("p2",
            TestCards.expectedMinionWithFlags(TestCards.SLUDGE_BELCHER, 3, 2, "taunt", "frozen"));

        agent.endTurn(); // p2
        agent.endTurn(); // p1

        agent.expectBoard("p1",
            TestCards.expectedMinionWithFlags(TestCards.WATER_ELEMENTAL, 3, 3));
        agent.expectBoard("p2",
            TestCards.expectedMinionWithFlags(TestCards.SLUDGE_BELCHER, 3, 2, "taunt", "frozen"));

        agent.endTurn(); // p2
        agent.endTurn(); // p1

        agent.expectBoard("p1",
            TestCards.expectedMinionWithFlags(TestCards.WATER_ELEMENTAL, 3, 3));
        agent.expectBoard("p2",
            TestCards.expectedMinionWithFlags(TestCards.SLUDGE_BELCHER, 3, 2, "taunt"));
    }
}
