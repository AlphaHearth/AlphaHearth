package info.hearthsim.brazier.game;

import info.hearthsim.brazier.PlayerProperty;
import info.hearthsim.brazier.actions.ActionUtils;
import info.hearthsim.brazier.db.CardDescr;
import info.hearthsim.brazier.game.cards.Card;
import info.hearthsim.brazier.util.UndoAction;
import org.jtrim.utils.ExceptionHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class Hand implements PlayerProperty {
    private final Player owner;
    private final int maxSize;

    private List<CardRef> hand;

    /**
     * Creates an empty {@code Hand} with the designated owner and max size.
     */
    public Hand(Player owner, int maxSize) {
        ExceptionHelper.checkNotNullArgument(owner, "owner");
        ExceptionHelper.checkArgumentInRange(maxSize, 0, Integer.MAX_VALUE, "maxSize");

        this.owner = owner;
        this.maxSize = maxSize;
        this.hand = new ArrayList<>(Player.MAX_HAND_SIZE);
    }

    /**
     * Returns a copy of this {@code Hand} with the given new owner.
     */
    public Hand copyFor(Player newOwner) {
        Hand result = new Hand(newOwner, this.maxSize);
        for (CardRef card : this.hand)
            result.addCard(card.card.copyFor(newOwner.getGame(), newOwner));
        return result;
    }

    @Override
    public Player getOwner() {
        return owner;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public int getCardCount() {
        return hand.size();
    }

    public Card getCard(int index) {
        return hand.get(index).card;
    }

    public int indexOf(Card card) {
        return getCards().indexOf(card);
    }

    public List<Card> getCards() {
        List<Card> result = new ArrayList<>(getCardCount());
        collectCards(result);
        return result;
    }

    public List<Card> getCards(Predicate<? super Card> filter) {
        ExceptionHelper.checkNotNullArgument(filter, "filter");
        List<Card> result = new ArrayList<>(getCardCount());
        collectCards(result, filter);
        return result;
    }

    public void collectCards(List<? super Card> result) {
        collectCards(result, (card) -> true);
    }

    public void collectCards(List<? super Card> result, Predicate<? super Card> filter) {
        ExceptionHelper.checkNotNullArgument(result, "result");
        ExceptionHelper.checkNotNullArgument(filter, "filter");

        for (CardRef cardRef: hand) {
            Card card = cardRef.card;
            if (filter.test(card)) {
                result.add(card);
            }
        }
    }

    public void forAllCards(Consumer<? super Card> action) {
        forCards(action, (card) -> true);
    }

    public void forCards(Consumer<? super Card> action, Predicate<? super Card> filter) {
        ExceptionHelper.checkNotNullArgument(action, "action");
        ExceptionHelper.checkNotNullArgument(filter, "filter");

        for (CardRef cardRef: hand) {
            Card card = cardRef.card;
            if (filter.test(card)) {
                action.accept(card);
            }
        }
    }

    public Card getRandomCard() {
        CardRef result = ActionUtils.pickRandom(getGame(), hand);
        return result != null ? result.card : null;
    }

    public Card findCard(Predicate<? super Card> filter) {
        ExceptionHelper.checkNotNullArgument(filter, "filter");

        for (CardRef cardRef: hand) {
            Card card = cardRef.card;
            if (filter.test(card)) {
                return card;
            }
        }
        return null;
    }

    public void discardAll() {
        if (hand.isEmpty())
            return;


        for (CardRef cardRef: hand) {
            cardRef.deactivate();
        }
        // TODO: Show cards to opponent
        hand.clear();
    }

    /**
     * Returns the index of a randomly selected card which satisfies the given {@link Predicate} of {@link Card}.
     */
    public int chooseRandomCardIndex(Predicate<? super Card> cardFilter) {
        ExceptionHelper.checkNotNullArgument(cardFilter, "cardFilter");

        int[] indexes = new int[hand.size()];
        int indexCount = 0;
        int i = 0;
        for (CardRef cardRef: hand) {
            Card card = cardRef.card;
            if (card.getMinion() != null && cardFilter.test(card)) {
                indexes[indexCount] = i;
                indexCount++;
            }
            i++;
        }

        if (indexCount == 0) {
            return -1;
        }

        int chosenIndex = getGame().getRandomProvider().roll(indexCount);
        return indexes[chosenIndex];
    }

    public Card replaceAtIndex(int cardIndex, CardDescr newCard) {
        CardRef result = hand.remove(cardIndex);
        result.deactivate();

        CardRef newCardRef = new CardRef(owner, newCard);
        hand.add(cardIndex, newCardRef);
        newCardRef.activate();

        return result.card;
    }

    public Card removeAtIndex(int cardIndex) {
        CardRef result = hand.remove(cardIndex);
        result.deactivate();
        return result.card;
    }

    public void addCard(CardDescr newCard) {
        addCard(new Card(owner, newCard));
    }

    public void addCard(Card newCard) {
        addCard(newCard, (card) -> {});
    }

    public void addCard(CardDescr newCard, Consumer<Card> onAddEvent) {
        addCard(new Card(owner, newCard), onAddEvent);
    }

    public void addCard(Card newCard, Consumer<Card> onAddEvent) {
        ExceptionHelper.checkNotNullArgument(newCard, "newCard");
        ExceptionHelper.checkNotNullArgument(onAddEvent, "onAddEvent");

        if (newCard.getOwner() != getOwner()) {
            throw new IllegalArgumentException("The owner of the card must be the same as the owner of this hand.");
        }

        if (hand.size() >= maxSize) {
            return;
        }

        CardRef newCardRef = new CardRef(newCard);
        hand.add(newCardRef);
        newCardRef.activate();
        onAddEvent.accept(newCard);
    }

    private static final class CardRef {
        private final Card card;
        private UndoAction<? super Card> unregisterRef;

        public CardRef(Player owner, CardDescr cardDescr) {
            this(new Card(owner, cardDescr));
        }

        public CardRef(Card card) {
            ExceptionHelper.checkNotNullArgument(card, "card");
            this.card = card;
            this.unregisterRef = UndoAction.DO_NOTHING;
        }

        public void activate() {
            unregisterRef = card.getCardDescr().getInHandAbility().activate(card);
        }

        public void deactivate() {
            unregisterRef.undo(card);
        }
    }
}
