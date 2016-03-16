package info.hearthsim.brazier;

import info.hearthsim.brazier.actions.ActionUtils;
import info.hearthsim.brazier.actions.undo.UndoAction;
import info.hearthsim.brazier.cards.Card;
import info.hearthsim.brazier.cards.CardDescr;
import info.hearthsim.brazier.actions.undo.UndoableUnregisterAction;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import info.hearthsim.brazier.actions.undo.UndoableResult;
import org.jtrim.utils.ExceptionHelper;

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
            result.hand.add(new CardRef(card.card.copyFor(newOwner)));
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

    public UndoAction forAllCards(Function<? super Card, ? extends UndoAction> action) {
        return forCards(action, (card) -> true);
    }

    public UndoAction forCards(Function<? super Card, ? extends UndoAction> action, Predicate<? super Card> filter) {
        ExceptionHelper.checkNotNullArgument(action, "action");
        ExceptionHelper.checkNotNullArgument(filter, "filter");

        UndoAction.Builder result = new UndoAction.Builder(hand.size());
        for (CardRef cardRef: hand) {
            Card card = cardRef.card;
            if (filter.test(card)) {
                result.addUndo(action.apply(card));
            }
        }
        return result;
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

    public UndoAction discardAll() {
        if (hand.isEmpty()) {
            return UndoAction.DO_NOTHING;
        }

        UndoAction.Builder result = new UndoAction.Builder(hand.size() + 1);
        // TODO: Show cards to opponent
        List<CardRef> prevHand = hand;
        hand = new ArrayList<>(Player.MAX_HAND_SIZE);
        result.addUndo(() -> hand = prevHand);

        for (CardRef cardRef: prevHand) {
            result.addUndo(cardRef.deactivate());
        }
        return result;
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

    public UndoableResult<Card> replaceAtIndex(int cardIndex, CardDescr newCard) {
        CardRef result = hand.remove(cardIndex);
        UndoAction deactivateUndo = result.deactivate();

        CardRef newCardRef = new CardRef(owner, newCard);
        hand.add(cardIndex, newCardRef);
        UndoAction activateUndo = newCardRef.activate();

        return new UndoableResult<>(result.card, () -> {
            activateUndo.undo();
            hand.remove(cardIndex);
            deactivateUndo.undo();
            hand.add(cardIndex, result);
        });
    }

    public UndoableResult<Card> removeAtIndex(int cardIndex) {
        CardRef result = hand.remove(cardIndex);
        UndoAction deactivateUndo = result.deactivate();
        return new UndoableResult<>(result.card, () -> {
            deactivateUndo.undo();
            hand.add(cardIndex, result);
        });
    }

    public UndoAction addCard(CardDescr newCard) {
        return addCard(new Card(owner, newCard));
    }

    public UndoAction addCard(Card newCard) {
        return addCard(newCard, (card) -> UndoAction.DO_NOTHING);
    }

    public UndoAction addCard(CardDescr newCard, Function<Card, UndoAction> onAddEvent) {
        return addCard(new Card(owner, newCard), onAddEvent);
    }

    public UndoAction addCard(Card newCard, Function<Card, UndoAction> onAddEvent) {
        ExceptionHelper.checkNotNullArgument(newCard, "newCard");
        ExceptionHelper.checkNotNullArgument(onAddEvent, "onAddEvent");

        if (newCard.getOwner() != getOwner()) {
            throw new IllegalArgumentException("The owner of the card must be the same as the owner of this hand.");
        }

        if (hand.size() >= maxSize) {
            return UndoAction.DO_NOTHING;
        }

        CardRef newCardRef = new CardRef(newCard);
        hand.add(newCardRef);
        UndoAction activateUndo = newCardRef.activate();
        UndoAction eventUndo = onAddEvent.apply(newCard);
        return () -> {
            eventUndo.undo();
            activateUndo.undo();
            hand.remove(hand.size() - 1);
        };
    }

    private static final class CardRef {
        private final Card card;
        private UndoableUnregisterAction abilityRef;

        public CardRef(Player owner, CardDescr cardDescr) {
            this(new Card(owner, cardDescr));
        }

        public CardRef(Card card) {
            ExceptionHelper.checkNotNullArgument(card, "card");
            this.card = card;
            this.abilityRef = UndoableUnregisterAction.DO_NOTHING;
        }

        public UndoAction activate() {
            abilityRef = card.getCardDescr().getInHandAbility().activate(card);
            return abilityRef;
        }

        public UndoAction deactivate() {
            return abilityRef.unregister();
        }
    }
}
