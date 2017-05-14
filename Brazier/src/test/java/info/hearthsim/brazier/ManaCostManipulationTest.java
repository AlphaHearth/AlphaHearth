package info.hearthsim.brazier;

import info.hearthsim.brazier.game.cards.Card;
import info.hearthsim.brazier.utils.BrazierTest;
import info.hearthsim.brazier.utils.TestAgent;
import info.hearthsim.brazier.utils.TestCards;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.fail;

public final class ManaCostManipulationTest extends BrazierTest {
    public static void expectManaCost(TestAgent script, String playerName, int... manaCosts) {
        int[] manaCostsCopy = manaCosts.clone();
        script.expectPlayer(playerName, (player) -> {
            List<Card> cards = player.getHand().getCards();
            if (cards.size() != manaCostsCopy.length) {
                fail("Unexpected number of cards: " + cards.size() + ". Expected: " + manaCostsCopy.length);
            }

            for (int i = 0; i < manaCostsCopy.length; i++) {
                int manaCost = cards.get(i).getActiveManaCost();
                int expectedCost = manaCostsCopy[i];
                if (manaCost != expectedCost) {
                    String cardName = cards.get(i).getCardDescr().getId().getName();
                    fail("The card " + cardName + " has an unexpected mana cost: " + manaCost + ". Expected: " + expectedCost);
                }
            }
        });
    }

    @Test
    public void testEmperorManaCostReduction() {
        agent.setCurrentPlayer("p1");
        agent.setMana("p1", 10);

        agent.addToHand("p1", TestCards.YETI, TestCards.SLAM, TestCards.WHIRLWIND);
        expectManaCost(agent, "p1", 4, 2, 1);

        agent.playMinionCard("p1", TestCards.EMPEROR_THAURISSAN, 0);
        expectManaCost(agent, "p1", 4, 2, 1);

        agent.endTurn(); // p1
        expectManaCost(agent, "p1", 3, 1, 0);

        agent.endTurn(); // p2
        expectManaCost(agent, "p1", 3, 1, 0);

        agent.endTurn(); // p1
        expectManaCost(agent, "p1", 2, 0, 0);
    }

    @Test
    public void testManaWraith() {
        agent.setCurrentPlayer("p1");
        agent.setMana("p1", 10);

        agent.addToHand("p1", TestCards.YETI, TestCards.SLAM, TestCards.WHIRLWIND);
        expectManaCost(agent, "p1", 4, 2, 1);

        agent.addToHand("p2", TestCards.PYROBLAST, TestCards.DEATHS_BITE, TestCards.BLUEGILL_WARRIOR);
        expectManaCost(agent, "p2", 10, 4, 2);

        agent.playMinionCard("p1", TestCards.MANA_WRAITH, 0);

        expectManaCost(agent, "p1", 5, 2, 1);
        expectManaCost(agent, "p2", 10, 4, 3);

        agent.endTurn();

        expectManaCost(agent, "p1", 5, 2, 1);
        expectManaCost(agent, "p2", 10, 4, 3);

        agent.setMana("p2", 10);
        agent.playNonMinionCard("p2", TestCards.PYROBLAST, "p1:0");

        expectManaCost(agent, "p1", 4, 2, 1);
        expectManaCost(agent, "p2", 10, 4, 2);
    }

    @Test
    public void testPreparationUsed() {
        agent.setCurrentPlayer("p1");
        agent.setMana("p1", 10);

        agent.addToHand("p1", TestCards.YETI, TestCards.SLAM, TestCards.WHIRLWIND, TestCards.PYROBLAST);
        expectManaCost(agent, "p1", 4, 2, 1, 10);

        agent.addToHand("p2", TestCards.PYROBLAST, TestCards.DEATHS_BITE, TestCards.BLUEGILL_WARRIOR);
        expectManaCost(agent, "p2", 10, 4, 2);

        agent.playCard("p1", TestCards.PREPARATION);

        expectManaCost(agent, "p1", 4, 0, 0, 7);
        expectManaCost(agent, "p2", 10, 4, 2);

        agent.playCard("p1", 3, "p2:hero");

        expectManaCost(agent, "p1", 4, 2, 1);
        expectManaCost(agent, "p2", 10, 4, 2);

        agent.endTurn();

        expectManaCost(agent, "p1", 4, 2, 1);
        expectManaCost(agent, "p2", 10, 4, 2);
    }

    @Test
    public void testPreparationWasted() {
        agent.setCurrentPlayer("p1");
        agent.setMana("p1", 10);

        agent.addToHand("p1", TestCards.YETI, TestCards.SLAM, TestCards.WHIRLWIND, TestCards.PYROBLAST);
        expectManaCost(agent, "p1", 4, 2, 1, 10);

        agent.addToHand("p2", TestCards.PYROBLAST, TestCards.DEATHS_BITE, TestCards.BLUEGILL_WARRIOR);
        expectManaCost(agent, "p2", 10, 4, 2);

        agent.playCard("p1", TestCards.PREPARATION);

        expectManaCost(agent, "p1", 4, 0, 0, 7);
        expectManaCost(agent, "p2", 10, 4, 2);

        agent.endTurn();

        expectManaCost(agent, "p1", 4, 2, 1, 10);
        expectManaCost(agent, "p2", 10, 4, 2);
    }
}
