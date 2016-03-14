package info.hearthsim.brazier;

import info.hearthsim.brazier.cards.Card;

import java.util.List;

import org.junit.Test;

import static org.junit.Assert.*;

public final class ManaCostManipulationTest {
    public static void expectManaCost(PlayScript script, String playerName, int... manaCosts) {
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
        PlayScript.testScript((script) -> {
            script.setCurrentPlayer("p1");
            script.setMana("p1", 10);

            script.addToHand("p1", TestCards.YETI, TestCards.SLAM, TestCards.WHIRLWIND);
            expectManaCost(script, "p1", 4, 2, 1);

            script.playMinionCard("p1", TestCards.EMPEROR_THAURISSAN, 0);
            expectManaCost(script, "p1", 4, 2, 1);

            script.endTurn(); // p1
            expectManaCost(script, "p1", 3, 1, 0);

            script.endTurn(); // p2
            expectManaCost(script, "p1", 3, 1, 0);

            script.endTurn(); // p1
            expectManaCost(script, "p1", 2, 0, 0);
        });
    }

    @Test
    public void testManaWraith() {
        PlayScript.testScript((script) -> {
            script.setCurrentPlayer("p1");
            script.setMana("p1", 10);

            script.addToHand("p1", TestCards.YETI, TestCards.SLAM, TestCards.WHIRLWIND);
            expectManaCost(script, "p1", 4, 2, 1);

            script.addToHand("p2", TestCards.PYROBLAST, TestCards.DEATHS_BITE, TestCards.BLUEGILL_WARRIOR);
            expectManaCost(script, "p2", 10, 4, 2);

            script.playMinionCard("p1", TestCards.MANA_WRAITH, 0);

            expectManaCost(script, "p1", 5, 2, 1);
            expectManaCost(script, "p2", 10, 4, 3);

            script.endTurn();

            expectManaCost(script, "p1", 5, 2, 1);
            expectManaCost(script, "p2", 10, 4, 3);

            script.setMana("p2", 10);
            script.playNonMinionCard("p2", TestCards.PYROBLAST, "p1:0");

            expectManaCost(script, "p1", 4, 2, 1);
            expectManaCost(script, "p2", 10, 4, 2);
        });
    }

    @Test
    public void testPreparationUsed() {
        PlayScript.testScript((script) -> {
            script.setCurrentPlayer("p1");
            script.setMana("p1", 10);

            script.addToHand("p1", TestCards.YETI, TestCards.SLAM, TestCards.WHIRLWIND, TestCards.PYROBLAST);
            expectManaCost(script, "p1", 4, 2, 1, 10);

            script.addToHand("p2", TestCards.PYROBLAST, TestCards.DEATHS_BITE, TestCards.BLUEGILL_WARRIOR);
            expectManaCost(script, "p2", 10, 4, 2);

            script.playCard("p1", TestCards.PREPARATION);

            expectManaCost(script, "p1", 4, 0, 0, 7);
            expectManaCost(script, "p2", 10, 4, 2);

            script.playCard("p1", 3, "p2:hero");

            expectManaCost(script, "p1", 4, 2, 1);
            expectManaCost(script, "p2", 10, 4, 2);

            script.endTurn();

            expectManaCost(script, "p1", 4, 2, 1);
            expectManaCost(script, "p2", 10, 4, 2);
        });
    }

    @Test
    public void testPreparationWasted() {
        PlayScript.testScript((script) -> {
            script.setCurrentPlayer("p1");
            script.setMana("p1", 10);

            script.addToHand("p1", TestCards.YETI, TestCards.SLAM, TestCards.WHIRLWIND, TestCards.PYROBLAST);
            expectManaCost(script, "p1", 4, 2, 1, 10);

            script.addToHand("p2", TestCards.PYROBLAST, TestCards.DEATHS_BITE, TestCards.BLUEGILL_WARRIOR);
            expectManaCost(script, "p2", 10, 4, 2);

            script.playCard("p1", TestCards.PREPARATION);

            expectManaCost(script, "p1", 4, 0, 0, 7);
            expectManaCost(script, "p2", 10, 4, 2);

            script.endTurn();

            expectManaCost(script, "p1", 4, 2, 1, 10);
            expectManaCost(script, "p2", 10, 4, 2);
        });
    }
}
