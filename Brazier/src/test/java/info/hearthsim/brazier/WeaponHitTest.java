package info.hearthsim.brazier;

import info.hearthsim.brazier.utils.BrazierTest;
import info.hearthsim.brazier.utils.TestCards;
import org.junit.Test;

import static org.junit.Assert.*;

public final class WeaponHitTest extends BrazierTest {
    @Test
    public void testCantAttackSecondTimeAfterWeaponReplace() {
        agent.setMana("p1", 10);

        agent.playCard("p1", TestCards.FIERY_WAR_AXE);
        agent.expectPlayer("p1", (player) -> {
            assertTrue(player.getHero().getAttackTool().canAttackWith());
        });

        agent.attack("p1:hero", "p2:hero");
        agent.expectPlayer("p1", (player) -> {
            assertFalse(player.getHero().getAttackTool().canAttackWith());
        });

        agent.playCard("p1", TestCards.FIERY_WAR_AXE);
        agent.expectPlayer("p1", (player) -> {
            assertFalse(player.getHero().getAttackTool().canAttackWith());
        });
    }

    @Test
    public void testLoseWeapon() {
        agent.setMana("p1", 10);
        agent.setMana("p2", 10);

        agent.playCard("p1", TestCards.FIERY_WAR_AXE);

        agent.expectBoard("p1");
        agent.expectBoard("p2");

        agent.expectWeapon("p1", 3, 2);
        agent.attack("p1:hero", "p2:hero");
        agent.expectWeapon("p1", 3, 1);

        agent.refreshAttacks();
        agent.attack("p1:hero", "p2:hero");
        agent.expectNoWeapon("p1", 0);

        agent.expectHeroHp("p1", 30, 0);
        agent.expectHeroHp("p2", 24, 0);
    }

    @Test
    public void testHitWeaponlessHeroWithWeapon() {
        agent.setMana("p1", 10);
        agent.setMana("p2", 10);

        agent.playCard("p1", TestCards.FIERY_WAR_AXE);

        agent.expectBoard("p1");
        agent.expectBoard("p2");

        agent.expectWeapon("p1", 3, 2);
        agent.attack("p1:hero", "p2:hero");
        agent.expectWeapon("p1", 3, 1);

        agent.expectHeroHp("p1", 30, 0);
        agent.expectHeroHp("p2", 27, 0);
    }

    @Test
    public void testHitWeaponedHeroWithWeapon() {
        agent.setMana("p1", 10);
        agent.setMana("p2", 10);

        agent.playCard("p1", TestCards.FIERY_WAR_AXE);
        agent.playCard("p2", TestCards.FIERY_WAR_AXE);

        agent.expectBoard("p1");
        agent.expectBoard("p2");

        agent.expectWeapon("p1", 3, 2);
        agent.attack("p1:hero", "p2:hero");
        agent.expectWeapon("p1", 3, 1);

        agent.expectHeroHp("p1", 30, 0);
        agent.expectHeroHp("p2", 27, 0);

        agent.expectWeapon("p1", 3, 1);
    }

    @Test
    public void testHitMinionWithWeapon() {
        agent.setMana("p1", 10);
        agent.setMana("p2", 10);

        agent.playCard("p1", TestCards.FIERY_WAR_AXE);
        agent.playMinionCard("p2", TestCards.YETI, 0);

        agent.expectBoard("p1");
        agent.expectBoard("p2",
            TestCards.expectedMinion(TestCards.YETI, 4, 5));

        agent.expectWeapon("p1", 3, 2);
        agent.attack("p1:hero", "p2:0");
        agent.expectWeapon("p1", 3, 1);

        agent.expectHeroHp("p1", 26, 0);
        agent.expectHeroHp("p2", 30, 0);
    }
}
