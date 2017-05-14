package info.hearthsim.brazier;

import info.hearthsim.brazier.utils.BrazierTest;
import org.junit.Test;

import static info.hearthsim.brazier.utils.TestCards.*;

public final class CardReceiveTest extends BrazierTest {
    @Test
    public void testHeadcrackNoCombo() {
        agent.setCurrentPlayer("p1");

        agent.addToHand("p1", YETI, WHIRLWIND);
        agent.addToHand("p2", SHADOW_MADNESS, DEFIAS_RINGLEADER);

        agent.deck("p1", BLUEGILL_WARRIOR, ABUSIVE_SERGEANT);
        agent.deck("p2", ANCIENT_MAGE, CULT_MASTER);

        agent.setHeroHp("p1", 30, 0);
        agent.setHeroHp("p2", 20, 0);

        agent.setMana("p1", 10);
        agent.playCard("p1", HEADCRACK);

        agent.expectHeroHp("p1", 30, 0);
        agent.expectHeroHp("p2", 18, 0);

        agent.endTurn();

        agent.expectHand("p1", YETI, WHIRLWIND);
        agent.expectHand("p2", SHADOW_MADNESS, DEFIAS_RINGLEADER, CULT_MASTER);
    }

    @Test
    public void testHeadcrackCombo() {
        agent.setCurrentPlayer("p1");

        agent.addToHand("p1", YETI, WHIRLWIND);
        agent.addToHand("p2", SHADOW_MADNESS, DEFIAS_RINGLEADER);

        agent.deck("p1", BLUEGILL_WARRIOR, ABUSIVE_SERGEANT);
        agent.deck("p2", ANCIENT_MAGE, CULT_MASTER);

        agent.setHeroHp("p1", 30, 0);
        agent.setHeroHp("p2", 20, 0);

        agent.setMana("p1", 10);
        agent.playCard("p1", THE_COIN);
        agent.playCard("p1", HEADCRACK);

        agent.expectHeroHp("p1", 30, 0);
        agent.expectHeroHp("p2", 18, 0);

        agent.endTurn();

        agent.expectHand("p1", YETI, WHIRLWIND, HEADCRACK);
        agent.expectHand("p2", SHADOW_MADNESS, DEFIAS_RINGLEADER, CULT_MASTER);

        agent.endTurn();

        agent.expectHand("p1", YETI, WHIRLWIND, HEADCRACK, ABUSIVE_SERGEANT);
        agent.expectHand("p2", SHADOW_MADNESS, DEFIAS_RINGLEADER, CULT_MASTER);

        agent.endTurn();

        agent.expectHand("p1", YETI, WHIRLWIND, HEADCRACK, ABUSIVE_SERGEANT);
        agent.expectHand("p2", SHADOW_MADNESS, DEFIAS_RINGLEADER, CULT_MASTER, ANCIENT_MAGE);
    }
}
