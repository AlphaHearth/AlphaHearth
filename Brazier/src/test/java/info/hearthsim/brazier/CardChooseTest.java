package info.hearthsim.brazier;

import org.junit.Test;

public final class CardChooseTest {
    @Test
    public void testTracking() {
        PlayScript.testScript((script) -> {
            script.setMana("p1", 10);
            script.deck("p1", TestCards.YETI, TestCards.CULT_MASTER, TestCards.BLACKWING_CORRUPTOR, TestCards.BLUEGILL_WARRIOR, TestCards.ABUSIVE_SERGEANT);

            script.addCardChoice(1, TestCards.ABUSIVE_SERGEANT, TestCards.BLUEGILL_WARRIOR, TestCards.BLACKWING_CORRUPTOR);
            script.playCard("p1", TestCards.TRACKING);

            script.expectHand("p1", TestCards.BLUEGILL_WARRIOR);
            script.expectDeck("p1", TestCards.YETI, TestCards.CULT_MASTER);
        });
    }

    @Test
    public void testTrackingWith2Cards() {
        PlayScript.testScript((script) -> {
            script.setMana("p1", 10);
            script.deck("p1", TestCards.YETI, TestCards.CULT_MASTER);

            script.addCardChoice(0, TestCards.CULT_MASTER, TestCards.YETI);
            script.playCard("p1", TestCards.TRACKING);

            script.expectHand("p1", TestCards.CULT_MASTER);
            script.expectDeck("p1");
        });
    }

    @Test
    public void testTrackingWith1Card() {
        PlayScript.testScript((script) -> {
            script.setMana("p1", 10);
            script.deck("p1", TestCards.YETI);

            script.playCard("p1", TestCards.TRACKING);

            script.expectHand("p1", TestCards.YETI);
            script.expectDeck("p1");
        });
    }

    @Test
    public void testTrackingWithNoCards() {
        PlayScript.testScript((script) -> {
            script.setMana("p1", 10);

            script.playCard("p1", TestCards.TRACKING);

            script.expectHand("p1");
            script.expectDeck("p1");
        });
    }
}
