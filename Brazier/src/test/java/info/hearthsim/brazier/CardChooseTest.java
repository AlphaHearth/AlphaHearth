package info.hearthsim.brazier;

import info.hearthsim.brazier.utils.BrazierTest;
import info.hearthsim.brazier.utils.TestCards;
import org.junit.Ignore;
import org.junit.Test;

public final class CardChooseTest extends BrazierTest {
    @Test
    @Ignore("TestAgent#addCardChoice is not supported for dynamic testing.")
    public void testTracking() {
        agent.setMana("p1", 10);
        agent.deck("p1", TestCards.YETI, TestCards.CULT_MASTER, TestCards.BLACKWING_CORRUPTOR, TestCards.BLUEGILL_WARRIOR, TestCards.ABUSIVE_SERGEANT);

        agent.addCardChoice(1, TestCards.ABUSIVE_SERGEANT, TestCards.BLUEGILL_WARRIOR, TestCards.BLACKWING_CORRUPTOR);
        agent.playCard("p1", TestCards.TRACKING);

        agent.expectHand("p1", TestCards.BLUEGILL_WARRIOR);
        agent.expectDeck("p1", TestCards.YETI, TestCards.CULT_MASTER);
    }

    @Test
    @Ignore("TestAgent#addCardChoice is not supported for dynamic testing.")
    public void testTrackingWith2Cards() {
        agent.setMana("p1", 10);
        agent.deck("p1", TestCards.YETI, TestCards.CULT_MASTER);

        agent.addCardChoice(0, TestCards.CULT_MASTER, TestCards.YETI);
        agent.playCard("p1", TestCards.TRACKING);

        agent.expectHand("p1", TestCards.CULT_MASTER);
        agent.expectDeck("p1");
    }

    @Test
    public void testTrackingWith1Card() {
        agent.setMana("p1", 10);
        agent.deck("p1", TestCards.YETI);

        agent.playCard("p1", TestCards.TRACKING);

        agent.expectHand("p1", TestCards.YETI);
        agent.expectDeck("p1");
    }

    @Test
    public void testTrackingWithNoCards() {
        agent.setMana("p1", 10);

        agent.playCard("p1", TestCards.TRACKING);

        agent.expectHand("p1");
        agent.expectDeck("p1");
    }
}
