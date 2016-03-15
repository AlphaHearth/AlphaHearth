package info.hearthsim.brazier;

import info.hearthsim.brazier.utils.BrazierTest;
import info.hearthsim.brazier.utils.TestAgent;
import info.hearthsim.brazier.utils.TestCards;
import org.junit.Test;

import static org.junit.Assert.*;

public final class SpellPowerTest extends BrazierTest {
    @Test
    public void testKoboldGeomancerWithTargetedSpell() {
        agent.setMana("p1", 10);

        agent.setHeroHp("p1", 30, 0);

        agent.playNonMinionCard("p1", TestCards.MOONFIRE, "p1:hero");
        agent.expectHeroHp("p1", 29, 0);

        agent.playCard("p1", TestCards.KOBOLD_GEOMANCER);
        agent.expectBoard("p1", TestCards.expectedMinion(TestCards.KOBOLD_GEOMANCER, 2, 2));

        agent.playNonMinionCard("p1", TestCards.MOONFIRE, "p1:hero");
        agent.expectHeroHp("p1", 27, 0);

        agent.playNonMinionCard("p1", TestCards.MOONFIRE, "p1:0");
        agent.expectBoard("p1");

        agent.playNonMinionCard("p1", TestCards.MOONFIRE, "p1:hero");
        agent.expectHeroHp("p1", 26, 0);
    }

    @Test
    public void testKoboldGeomancerWithAoe() {
        agent.setMana("p1", 10);
        agent.setMana("p2", 10);

        agent.playCard("p1", TestCards.KOBOLD_GEOMANCER);
        agent.playCard("p2", TestCards.YETI);

        agent.expectBoard("p1", TestCards.expectedMinion(TestCards.KOBOLD_GEOMANCER, 2, 2));
        agent.expectBoard("p2", TestCards.expectedMinion(TestCards.YETI, 4, 5));

        agent.playCard("p1", TestCards.FLAMESTRIKE);

        agent.expectBoard("p1", TestCards.expectedMinion(TestCards.KOBOLD_GEOMANCER, 2, 2));
        agent.expectBoard("p2");

        agent.setMana("p1", 10);
        agent.setMana("p2", 10);

        agent.playCard("p2", TestCards.YETI);
        agent.expectBoard("p1", TestCards.expectedMinion(TestCards.KOBOLD_GEOMANCER, 2, 2));
        agent.expectBoard("p2", TestCards.expectedMinion(TestCards.YETI, 4, 5));

        agent.playNonMinionCard("p1", TestCards.MOONFIRE, "p1:0");
        agent.expectBoard("p1");
        agent.expectBoard("p2", TestCards.expectedMinion(TestCards.YETI, 4, 5));

        agent.playCard("p1", TestCards.FLAMESTRIKE);
        agent.expectBoard("p1");
        agent.expectBoard("p2", TestCards.expectedMinion(TestCards.YETI, 4, 1));
    }

    private static void expectSpellPower(
        TestAgent script,
        int expectedSpellPower1,
        int expectedSpellPower2) {
        script.expectPlayer("p1", (player) -> {
            assertEquals("spellPower[p1]", expectedSpellPower1, player.getSpellPower().getValue());

            Player opponent = player.getOpponent();
            assertEquals("spellPower[p2]", expectedSpellPower2, opponent.getSpellPower().getValue());
        });
    }

    @Test
    public void testAncientMageNoTarget() {
        agent.setMana("p1", 10);

        agent.playMinionCard("p1", TestCards.ANCIENT_MAGE, 0);
        agent.playMinionCard("p1", TestCards.BLUEGILL_WARRIOR, 0);
        agent.playMinionCard("p1", TestCards.FLAME_OF_AZZINOTH, 2);

        expectSpellPower(agent, 0, 0);

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.BLUEGILL_WARRIOR, 2, 1),
            TestCards.expectedMinion(TestCards.ANCIENT_MAGE, 2, 5),
            TestCards.expectedMinion(TestCards.FLAME_OF_AZZINOTH, 2, 1));

        expectSpellPower(agent, 0, 0);

        agent.setMana("p1", 10);
        agent.playNonMinionCard("p1", TestCards.PYROBLAST, "p1:1");
        expectSpellPower(agent, 0, 0);

        agent.setMana("p1", 10);
        agent.playNonMinionCard("p1", TestCards.PYROBLAST, "p1:0");
        expectSpellPower(agent, 0, 0);

        agent.setMana("p1", 10);
        agent.playNonMinionCard("p1", TestCards.PYROBLAST, "p1:0");
        expectSpellPower(agent, 0, 0);

        agent.endTurn();

        expectSpellPower(agent, 0, 0);
    }

    @Test
    public void testAncientMageLeftTarget() {
        agent.setMana("p1", 10);

        agent.playMinionCard("p1", TestCards.BLUEGILL_WARRIOR, 0);
        agent.playMinionCard("p1", TestCards.ANCIENT_MAGE, 1);
        agent.playMinionCard("p1", TestCards.FLAME_OF_AZZINOTH, 2);

        expectSpellPower(agent, 1, 0);

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.BLUEGILL_WARRIOR, 2, 1),
            TestCards.expectedMinion(TestCards.ANCIENT_MAGE, 2, 5),
            TestCards.expectedMinion(TestCards.FLAME_OF_AZZINOTH, 2, 1));

        expectSpellPower(agent, 1, 0);

        agent.setMana("p1", 10);
        agent.playNonMinionCard("p1", TestCards.PYROBLAST, "p1:1");
        expectSpellPower(agent, 1, 0);

        agent.setMana("p1", 10);
        agent.playNonMinionCard("p1", TestCards.PYROBLAST, "p1:0");
        expectSpellPower(agent, 0, 0);

        agent.setMana("p1", 10);
        agent.playNonMinionCard("p1", TestCards.PYROBLAST, "p1:0");
        expectSpellPower(agent, 0, 0);

        agent.endTurn();

        expectSpellPower(agent, 0, 0);
    }

    @Test
    public void testAncientMageRightTarget() {
        agent.setMana("p1", 10);

        agent.playMinionCard("p1", TestCards.FLAME_OF_AZZINOTH, 0);
        agent.playMinionCard("p1", TestCards.ANCIENT_MAGE, 0);
        agent.playMinionCard("p1", TestCards.BLUEGILL_WARRIOR, 0);

        expectSpellPower(agent, 1, 0);

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.BLUEGILL_WARRIOR, 2, 1),
            TestCards.expectedMinion(TestCards.ANCIENT_MAGE, 2, 5),
            TestCards.expectedMinion(TestCards.FLAME_OF_AZZINOTH, 2, 1));

        expectSpellPower(agent, 1, 0);

        agent.setMana("p1", 10);
        agent.playNonMinionCard("p1", TestCards.PYROBLAST, "p1:1");
        expectSpellPower(agent, 1, 0);

        agent.setMana("p1", 10);
        agent.playNonMinionCard("p1", TestCards.PYROBLAST, "p1:0");
        expectSpellPower(agent, 1, 0);

        agent.setMana("p1", 10);
        agent.playNonMinionCard("p1", TestCards.PYROBLAST, "p1:0");
        expectSpellPower(agent, 0, 0);

        agent.endTurn();

        expectSpellPower(agent, 0, 0);
    }

    @Test
    public void testAncientMageBothTarget() {
        agent.setMana("p1", 10);

        agent.playMinionCard("p1", TestCards.FLAME_OF_AZZINOTH, 0);
        agent.playMinionCard("p1", TestCards.BLUEGILL_WARRIOR, 0);
        agent.playMinionCard("p1", TestCards.ANCIENT_MAGE, 1);

        expectSpellPower(agent, 2, 0);

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.BLUEGILL_WARRIOR, 2, 1),
            TestCards.expectedMinion(TestCards.ANCIENT_MAGE, 2, 5),
            TestCards.expectedMinion(TestCards.FLAME_OF_AZZINOTH, 2, 1));

        expectSpellPower(agent, 2, 0);

        agent.setMana("p1", 10);
        agent.playNonMinionCard("p1", TestCards.PYROBLAST, "p1:1");
        expectSpellPower(agent, 2, 0);

        agent.setMana("p1", 10);
        agent.playNonMinionCard("p1", TestCards.PYROBLAST, "p1:0");
        expectSpellPower(agent, 1, 0);

        agent.setMana("p1", 10);
        agent.playNonMinionCard("p1", TestCards.PYROBLAST, "p1:0");
        expectSpellPower(agent, 0, 0);

        agent.endTurn();

        expectSpellPower(agent, 0, 0);
    }
}
