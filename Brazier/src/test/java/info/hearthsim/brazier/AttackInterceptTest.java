package info.hearthsim.brazier;

import info.hearthsim.brazier.utils.BrazierTest;
import info.hearthsim.brazier.utils.TestCards;
import org.junit.Test;

public final class AttackInterceptTest extends BrazierTest {
    @Test
    public void testExplosiveTrapAttackerKillPreventsMinionAttack() {
        agent.setMana("p1", 10);
        agent.setMana("p2", 10);

        agent.playMinionCard("p1", TestCards.EXPLOSIVE_TRAP, 0);
        // FIXME: Failed to deep copy registered Secret in GameEvents

        agent.setCurrentPlayer("p2");
        agent.playMinionCard("p2", TestCards.STONETUSK_BOAR, 0);

        agent.expectBoard("p1");
        agent.expectBoard("p2", TestCards.expectedMinion(TestCards.STONETUSK_BOAR, 1, 1));

        agent.setHeroHp("p1", 18, 0);
        agent.setHeroHp("p2", 28, 0);

        agent.attack("p2:0", "p1:hero");

        agent.expectHeroHp("p1", 18, 0);
        agent.expectHeroHp("p2", 26, 0);

        agent.expectBoard("p1");
        agent.expectBoard("p2");
    }

    @Test
    public void testExplosiveTrapHeroKillPreventsMinionAttack() {
        agent.setMana("p1", 10);
        agent.setMana("p2", 10);

        agent.playMinionCard("p1", TestCards.EXPLOSIVE_TRAP, 0);

        agent.setCurrentPlayer("p2");
        agent.playMinionCard("p2", TestCards.STONETUSK_BOAR, 0);

        agent.expectBoard("p1");
        agent.expectBoard("p2", TestCards.expectedMinion(TestCards.STONETUSK_BOAR, 1, 1));

        agent.setHeroHp("p1", 1, 0);
        agent.setHeroHp("p2", 2, 0);

        agent.attack("p2:0", "p1:hero");
        agent.expectHeroDeath("p2");

        agent.expectHeroHp("p1", 1, 0);
        agent.expectHeroHp("p2", 0, 0);
    }

    @Test
    public void testExplosiveTrapKillPreventsAuraBeforeAttack() {
        agent.setMana("p1", 10);
        agent.setMana("p2", 10);

        agent.playMinionCard("p1", TestCards.EXPLOSIVE_TRAP, 0);

        agent.setCurrentPlayer("p2");
        agent.playMinionCard("p2", TestCards.YETI, 0);
        agent.playMinionCard("p2", TestCards.DIRE_WOLF_ALPHA, 1);

        agent.expectBoard("p1");
        agent.expectBoard("p2",
            TestCards.expectedMinion(TestCards.YETI, 5, 5),
            TestCards.expectedMinion(TestCards.DIRE_WOLF_ALPHA, 2, 2));

        agent.setHeroHp("p1", 18, 0);
        agent.setHeroHp("p2", 28, 0);

        agent.refreshAttacks();
        agent.attack("p2:0", "p1:hero");

        agent.expectHeroHp("p1", 14, 0);
        agent.expectHeroHp("p2", 26, 0);

        agent.expectBoard("p1");
        agent.expectBoard("p2", TestCards.expectedMinion(TestCards.YETI, 4, 3));
    }
}
