package info.hearthsim.brazier;

import info.hearthsim.brazier.utils.BrazierTest;
import info.hearthsim.brazier.utils.TestCards;
import org.junit.Test;

public final class HeroAttackedTest extends BrazierTest {
    @Test
    public void testOneOffLethal() {
        agent.setMana("p1", 10);
        agent.setMana("p2", 10);

        agent.setHeroHp("p2", 5, 0);

        agent.playMinionCard("p1", TestCards.YETI, 0);

        agent.refreshAttacks();

        agent.attack("p1:0", "p2:hero");

        agent.expectGameContinues();
    }

    @Test
    public void testExactLethal() {
        agent.setMana("p1", 10);
        agent.setMana("p2", 10);

        agent.setHeroHp("p2", 4, 0);

        agent.playMinionCard("p1", TestCards.YETI, 0);

        agent.refreshAttacks();

        agent.attack("p1:0", "p2:hero");

        agent.expectHeroDeath("p2");
    }

    @Test
    public void testExactLethalWithArmor() {
        agent.setMana("p1", 10);
        agent.setMana("p2", 10);

        agent.setHeroHp("p2", 3, 1);

        agent.playMinionCard("p1", TestCards.YETI, 0);

        agent.refreshAttacks();

        agent.attack("p1:0", "p2:hero");

        agent.expectHeroDeath("p2");
    }

    @Test
    public void testOverLethal() {
        agent.setMana("p1", 10);
        agent.setMana("p2", 10);

        agent.setHeroHp("p2", 3, 0);

        agent.playMinionCard("p1", TestCards.YETI, 0);
        agent.refreshAttacks();

        agent.attack("p1:0", "p2:hero");

        agent.expectHeroDeath("p2");
    }

    @Test
    public void testAttackHero() {
        agent.setMana("p1", 4);
        agent.playMinionCard("p1", TestCards.YETI, 0);

        agent.refreshAttacks();

        agent.expectBoard("p1", TestCards.expectedMinion(TestCards.YETI, 4, 5));
        agent.expectBoard("p2");

        agent.attack("p1:0", "p2:hero");

        agent.expectHeroHp("p1", 30, 0);
        agent.expectHeroHp("p2", 26, 0);
    }

    @Test
    public void testAttackHeroWithLowArmor() {
        agent.setHeroHp("p2", 30, 3);

        agent.setMana("p1", 10);

        agent.playMinionCard("p1", TestCards.YETI, 0);

        agent.refreshAttacks();

        agent.expectBoard("p1", TestCards.expectedMinion(TestCards.YETI, 4, 5));
        agent.expectBoard("p2");

        agent.attack("p1:0", "p2:hero");

        agent.expectHeroHp("p1", 30, 0);
        agent.expectHeroHp("p2", 29, 0);
    }

    @Test
    public void testAttackHeroWithJustEnoughArmor() {
        agent.setHeroHp("p2", 30, 4);

        agent.setMana("p1", 10);

        agent.playMinionCard("p1", TestCards.YETI, 0);

        agent.refreshAttacks();

        agent.expectBoard("p1", TestCards.expectedMinion(TestCards.YETI, 4, 5));
        agent.expectBoard("p2");

        agent.attack("p1:0", "p2:hero");

        agent.expectHeroHp("p1", 30, 0);
        agent.expectHeroHp("p2", 30, 0);
    }

    @Test
    public void testAttackHeroWithHighArmor() {
        agent.setHeroHp("p2", 30, 10);

        agent.setMana("p1", 10);

        agent.playMinionCard("p1", TestCards.YETI, 0);

        agent.refreshAttacks();

        agent.expectBoard("p1", TestCards.expectedMinion(TestCards.YETI, 4, 5));
        agent.expectBoard("p2");

        agent.attack("p1:0", "p2:hero");

        agent.expectHeroHp("p1", 30, 0);
        agent.expectHeroHp("p2", 30, 6);
    }
}
