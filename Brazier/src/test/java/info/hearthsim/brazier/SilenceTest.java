package info.hearthsim.brazier;

import info.hearthsim.brazier.utils.BrazierTest;
import info.hearthsim.brazier.utils.TestAgent;
import org.junit.Test;

import static info.hearthsim.brazier.utils.TestCards.*;

public final class SilenceTest extends BrazierTest {
    @Test
    public void testSilenceStatBuff() {
        agent.setMana("p1", 10);
        agent.playMinionCard("p1", YETI, 0);
        agent.playMinionCard("p1", SHATTERED_SUN_CLERIC, 1, "p1:0");

        agent.expectBoard("p1",
            expectedMinion(YETI, 5, 6),
            expectedMinion(SHATTERED_SUN_CLERIC, 3, 2));

        agent.playNonMinionCard("p1", SILENCE, "p1:0");

        agent.expectBoard("p1",
            expectedMinion(YETI, 4, 5),
            expectedMinion(SHATTERED_SUN_CLERIC, 3, 2));
    }

    @Test
    public void testSilenceStatBuffDoesNotKillMinion() {
        agent.setMana("p1", 10);
        agent.playMinionCard("p1", BLUEGILL_WARRIOR, 0);
        agent.playMinionCard("p1", SHATTERED_SUN_CLERIC, 1, "p1:0");

        agent.expectBoard("p1",
            expectedMinion(BLUEGILL_WARRIOR, 3, 2),
            expectedMinion(SHATTERED_SUN_CLERIC, 3, 2));

        agent.playNonMinionCard("p1", MOONFIRE, "p1:0");

        agent.expectBoard("p1",
            expectedMinion(BLUEGILL_WARRIOR, 3, 1),
            expectedMinion(SHATTERED_SUN_CLERIC, 3, 2));

        agent.playNonMinionCard("p1", SILENCE, "p1:0");

        agent.expectBoard("p1",
            expectedMinion(BLUEGILL_WARRIOR, 2, 1),
            expectedMinion(SHATTERED_SUN_CLERIC, 3, 2));
    }

    @Test
    public void testSilenceTriggeredAbility() {
        agent.setMana("p1", 10);
        agent.playMinionCard("p1", FROTHING_BERSERKER, 0);

        agent.expectBoard("p1",
            expectedMinion(FROTHING_BERSERKER, 2, 4));

        agent.playNonMinionCard("p1", MOONFIRE, "p1:0");

        agent.expectBoard("p1",
            expectedMinion(FROTHING_BERSERKER, 3, 3));

        agent.playNonMinionCard("p1", SILENCE, "p1:0");

        agent.expectBoard("p1",
            expectedMinion(FROTHING_BERSERKER, 2, 3));

        agent.playNonMinionCard("p1", MOONFIRE, "p1:0");

        agent.expectBoard("p1",
            expectedMinion(FROTHING_BERSERKER, 2, 2));
    }

    @Test
    public void testSilenceAuraGiver() {
        agent.setMana("p1", 10);

        agent.playMinionCard("p1", YETI, 0);
        agent.playMinionCard("p1", BLUEGILL_WARRIOR, 1);
        agent.setMana("p1", 10);
        agent.playMinionCard("p1", STORMWIND_CHAMPION, 2);
        agent.playMinionCard("p1", SLIME, 3);
        agent.playMinionCard("p1", TREANT, 4);

        expectTestStormwindBoard(agent);

        agent.playNonMinionCard("p2", SILENCE, "p1:2");

        agent.expectBoard("p1",
            expectedMinion(YETI, 4, 5),
            expectedMinion(BLUEGILL_WARRIOR, 2, 1),
            expectedMinion(STORMWIND_CHAMPION, 6, 6),
            expectedMinion(SLIME, 1, 2),
            expectedMinion(TREANT, 2, 2));
    }

    @Test
    public void testSilenceRemovesSpellDamage() {
        agent.setMana("p1", 10);
        agent.playMinionCard("p1", KOBOLD_GEOMANCER, 0);
        agent.playMinionCard("p1", YETI, 1);

        agent.expectBoard("p1",
            expectedMinion(KOBOLD_GEOMANCER, 2, 2),
            expectedMinion(YETI, 4, 5));

        agent.playNonMinionCard("p1", SILENCE, "p1:0");
        agent.playNonMinionCard("p1", MOONFIRE, "p1:1");

        agent.expectBoard("p1",
            expectedMinion(KOBOLD_GEOMANCER, 2, 2),
            expectedMinion(YETI, 4, 4));
    }

    private static void expectTestStormwindBoard(TestAgent script) {
        script.expectBoard("p1",
            expectedMinion(YETI, 5, 6),
            expectedMinion(BLUEGILL_WARRIOR, 3, 2),
            expectedMinion(STORMWIND_CHAMPION, 6, 6),
            expectedMinion(SLIME, 2, 3),
            expectedMinion(TREANT, 3, 3));
    }

    @Test
    public void testCannotSilenceAuraBuffs() {
        agent.setMana("p1", 10);

        agent.playMinionCard("p1", YETI, 0);
        agent.playMinionCard("p1", BLUEGILL_WARRIOR, 1);
        agent.setMana("p1", 10);
        agent.playMinionCard("p1", STORMWIND_CHAMPION, 2);
        agent.playMinionCard("p1", SLIME, 3);
        agent.playMinionCard("p1", TREANT, 4);

        expectTestStormwindBoard(agent);

        agent.playNonMinionCard("p2", SILENCE, "p1:0");
        expectTestStormwindBoard(agent);

        agent.playNonMinionCard("p2", SILENCE, "p1:1");
        expectTestStormwindBoard(agent);

        agent.playNonMinionCard("p2", SILENCE, "p1:3");
        expectTestStormwindBoard(agent);

        agent.playNonMinionCard("p2", SILENCE, "p1:4");
        expectTestStormwindBoard(agent);
    }

    @Test
    public void testSilenceDeathRattle() {
        agent.setMana("p1", 10);
        agent.playMinionCard("p1", SLUDGE_BELCHER, 0);

        agent.expectBoard("p1",
            expectedMinion(SLUDGE_BELCHER, 3, 5));

        agent.playNonMinionCard("p1", SILENCE, "p1:0");

        agent.setMana("p1", 10);
        agent.playNonMinionCard("p1", PYROBLAST, "p1:0");

        agent.expectBoard("p1");
    }

    @Test
    public void testSilenceTaunt() {
        agent.setMana("p1", 10);
        agent.playMinionCard("p1", SLUDGE_BELCHER, 0);

        agent.expectBoard("p1",
            expectedMinionWithFlags(SLUDGE_BELCHER, 3, 5, "taunt"));

        agent.playNonMinionCard("p1", SILENCE, "p1:0");

        agent.expectBoard("p1",
            expectedMinionWithFlags(SLUDGE_BELCHER, 3, 5));
    }
}
