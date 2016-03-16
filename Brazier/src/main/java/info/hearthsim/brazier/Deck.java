package info.hearthsim.brazier;

import info.hearthsim.brazier.actions.undo.UndoAction;
import info.hearthsim.brazier.cards.Card;
import info.hearthsim.brazier.cards.CardDescr;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import info.hearthsim.brazier.actions.undo.UndoableResult;
import org.jtrim.utils.ExceptionHelper;

public final class Deck implements PlayerProperty {
    private final Player owner;
    private final List<Card> cards;

    /**
     * Creates an empty {@code Deck} which belongs to the given {@code Player}.
     */
    public Deck(Player owner) {
        ExceptionHelper.checkNotNullArgument(owner, "owner");

        this.owner = owner;
        this.cards = new ArrayList<>();
    }

    /**
     * Returns a list of {@link Card}s in this {@code Deck} which satisfies the given
     * {@link Predicate}.
     */
    public List<Card> getCards(Predicate<? super Card> filter) {
        ExceptionHelper.checkNotNullArgument(filter, "filter");
        return cards.stream().filter(filter).collect(Collectors.toList());
    }

    /**
     * Returns the cards left in the deck as a {@link List} of {@link Card}s.
     */
    public List<Card> getCards() {
        return new ArrayList<>(cards);
    }

    /**
     * Clears the deck and adds the given collection of cards to the deck.
     */
    public UndoAction setCards(Collection<? extends CardDescr> newCards) {
        ExceptionHelper.checkNotNullElements(newCards, "newCards");

        List<Card> prevDeck;
        if (!cards.isEmpty()) {
            prevDeck = new ArrayList<>(cards);
        }
        else {
            prevDeck = Collections.emptyList();
        }

        cards.clear();
        for (CardDescr card: newCards) {
            Objects.requireNonNull(card, "newCards[?]");
            cards.add(new Card(owner, card));
        }

        return () -> {
            cards.clear();
            cards.addAll(prevDeck);
        };
    }

    /**
     * Shuffles the deck with the given {@link RandomProvider}.
     */
    public void shuffle(RandomProvider randomProvider) {
        for (int i = cards.size() - 1; i > 0; i--) {
            int otherIndex = randomProvider.roll(i + 1);
            cards.set(i, cards.set(otherIndex, cards.get(i)));
        }
    }

    /**
     * Returns the number of cards left in the deck.
     */
    public int getNumberOfCards() {
        return cards.size();
    }

    /**
     * Tries to draw the card on the top of the deck.
     *
     * @return {@code UndoableResult} with the drawn card and the {@code UndoAction} to undo the action;
     *         {@code null} if there is no card left in the deck.
     */
    public UndoableResult<Card> tryDrawOneCard() {
        if (cards.isEmpty()) {
            return null;
        }
        else {
            Card result = cards.remove(cards.size() - 1);
            return new UndoableResult<>(result, () -> cards.add(result));
        }
    }

    /**
     * Throws {@link IllegalArgumentException} if the given card doesn't belong to the owner of this deck.
     */
    private void checkOwner(Card card) {
        if (card.getOwner() != owner) {
            throw new IllegalArgumentException("Card has the wrong owner player.");
        }
    }

    /**
     * Puts the given card on the top of the deck.
     */
    public UndoAction putOnTop(CardDescr card) {
        return putOnTop(new Card(owner, card));
    }

    /**
     * Puts the given card on the top of the deck.
     */
    public UndoAction putOnTop(Card card) {
        ExceptionHelper.checkNotNullArgument(card, "card");
        checkOwner(card);

        cards.add(card);
        return () -> cards.remove(cards.size() - 1);
    }

    /**
     * Shuffles the given card to the deck with the given {@link RandomProvider}.
     *
     * @param randomProvider the given {@code RandomProvider}.
     * @param card the given card.
     */
    public UndoAction shuffle(RandomProvider randomProvider, CardDescr card) {
        return shuffle(randomProvider, new Card(owner, card));
    }

    /**
     * Shuffles the given card to the deck with the given {@link RandomProvider}.
     *
     * @param randomProvider the given {@code RandomProvider}.
     * @param card the given card.
     */
    public UndoAction shuffle(RandomProvider randomProvider, Card card) {
        ExceptionHelper.checkNotNullArgument(card, "card");
        checkOwner(card);

        int pos = randomProvider.roll(cards.size() + 1);

        cards.add(pos, card);
        return () -> cards.remove(pos);
    }

    /**
     * Tries to draw a random card with the given {@code RandomProvider} from the cards left which
     * satisfy the given predicate.
     *
     * @param randomProvider the given {@code RandomProvider}.
     * @param filter the given predicate.
     * @return {@code UndoableResult} with the drawn card and the {@code UndoAction} to undo the action;
     *         {@code null} if there is no card left in the deck.
     */
    public UndoableResult<Card> tryDrawRandom(RandomProvider randomProvider, Predicate<? super Card> filter) {
        ExceptionHelper.checkNotNullArgument(randomProvider, "randomProvider");
        ExceptionHelper.checkNotNullArgument(filter, "filter");

        int[] indexes = new int[cards.size()];
        int cardCount = 0;

        int index = 0;
        for (Card card: cards) {
            if (filter.test(card)) {
                indexes[cardCount] = index;
                cardCount++;
            }
            index++;
        }

        if (cardCount == 0) {
            return null;
        }

        int selectedIndex = indexes[randomProvider.roll(cardCount)];
        Card removedCard = cards.remove(selectedIndex);
        return new UndoableResult<>(removedCard, () -> cards.add(selectedIndex, removedCard));
    }

    @Override
    public Player getOwner() {
        return owner;
    }

    @Override
    public Game getGame() {
        return owner.getGame();
    }
}
