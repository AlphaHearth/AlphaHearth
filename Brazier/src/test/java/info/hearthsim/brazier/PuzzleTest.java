package info.hearthsim.brazier;

import info.hearthsim.brazier.utils.BrazierTest;
import info.hearthsim.brazier.utils.TestAgent;
import info.hearthsim.brazier.utils.TestCards;
import org.junit.Test;

public final class PuzzleTest extends BrazierTest {
    private static void setupInitialBoardOfTrumpsPuzzle(TestAgent script) {
        script.setMana("p2", 7);
        script.playCard("p2", TestCards.FIERY_WAR_AXE);

        script.setMana("p1", 10);
        script.playMinionCard("p1", TestCards.GRIM_PATRON, 0);
        script.playMinionCard("p1", TestCards.GRIM_PATRON, 1);
        script.setMana("p1", 10);
        script.playCard("p1", TestCards.WHIRLWIND);

        script.attack("p2:hero", "p1:3");
        script.refreshAttacks();
        script.attack("p2:hero", "p1:1");

        script.playMinionCard("p2", TestCards.SLUDGE_BELCHER, 0);

        script.expectBoard("p1",
            TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 2),
            TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 2));
        script.expectBoard("p2",
            TestCards.expectedMinion(TestCards.SLUDGE_BELCHER, 3, 5));

        script.setHeroHp("p1", 26, 0);
        script.setHeroHp("p2", 29, 2);

        script.refreshAttacks();
    }

    @Test
    public void testTrumpsPatronPuzzle32Damage() {
        setupInitialBoardOfTrumpsPuzzle(agent);

        agent.setMana("p1", 8);
        agent.addToHand("p1",
            TestCards.SLAM,
            TestCards.FIERY_WAR_AXE,
            TestCards.DEATHS_BITE,
            TestCards.FROTHING_BERSERKER,
            TestCards.DREAD_CORSAIR,
            TestCards.DREAD_CORSAIR,
            TestCards.WARSONG_COMMANDER);
        agent.decreaseManaCostOfHand("p1");
        agent.addToHand("p1", TestCards.WHIRLWIND);

        agent.playCard("p1", 1); // War axe
        agent.expectMana("p1", 7);
        agent.playMinionCard("p1", 5, 0); // Warsong
        agent.expectMana("p1", 5);
        agent.playMinionCard("p1", 2, 1); // Frothing
        agent.expectMana("p1", 3);
        agent.playMinionCard("p1", 2, 2); // Dread
        agent.playMinionCard("p1", 2, 3); // Dread
        agent.expectMana("p1", 3);

        agent.attack("p1:3", "p2:0");
        agent.attack("p1:2", "p2:0");

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.WARSONG_COMMANDER, 2, 3),
            TestCards.expectedMinion(TestCards.FROTHING_BERSERKER, 6, 4),
            TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 2),
            TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 2));
        agent.expectBoard("p2",
            TestCards.expectedMinion(TestCards.SLIME, 1, 2));

        agent.attack("p1:2", "p2:0");

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.WARSONG_COMMANDER, 2, 3),
            TestCards.expectedMinion(TestCards.FROTHING_BERSERKER, 8, 4),
            TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 1),
            TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 3),
            TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 2));
        agent.expectBoard("p2");

        agent.playCard("p1", 2); // Whirlwind
        agent.expectMana("p1", 2);

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.WARSONG_COMMANDER, 2, 2),
            TestCards.expectedMinion(TestCards.FROTHING_BERSERKER, 13, 3),
            TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 2),
            TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 3),
            TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 1),
            TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 3));

        agent.playCard("p1", 0, "p1:5");
        agent.expectMana("p1", 1);

        agent.expectBoard("p1",
            TestCards.expectedMinion(TestCards.WARSONG_COMMANDER, 2, 2),
            TestCards.expectedMinion(TestCards.FROTHING_BERSERKER, 14, 3),
            TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 2),
            TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 3),
            TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 1),
            TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 1),
            TestCards.expectedMinion(TestCards.GRIM_PATRON, 3, 3));

        agent.attack("p1:1", "p2:hero");
        agent.attack("p1:2", "p2:hero");
        agent.attack("p1:3", "p2:hero");
        agent.attack("p1:4", "p2:hero");
        agent.attack("p1:5", "p2:hero");
        agent.attack("p1:6", "p2:hero");
        agent.attack("p1:hero", "p2:hero");

        agent.expectHeroDeath("p2");
        agent.expectHeroHp("p2", -1, 0);
    }
}
