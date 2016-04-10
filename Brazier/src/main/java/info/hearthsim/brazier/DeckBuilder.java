package info.hearthsim.brazier;

import info.hearthsim.brazier.db.CardDescr;
import info.hearthsim.brazier.game.Deck;
import info.hearthsim.brazier.game.Player;
import info.hearthsim.brazier.game.cards.CardRarity;
import info.hearthsim.brazier.game.cards.HeroClass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builder class for {@link Deck}.
 */
public class DeckBuilder {
    private final HeroClass clazz;
    private final Map<CardDescr, Integer> cards = new HashMap<>();
    private int cardNum = 0;

    /**
     * Creates a {@code DeckBuilder} for the given class.
     *
     * @param clazz the given class of the building deck.
     * @throws IllegalArgumentException if the given {@code HeroClass} is {@link HeroClass#Neutral}.
     */
    public DeckBuilder(HeroClass clazz) {
        if (clazz == HeroClass.Neutral)
            throw new IllegalArgumentException("Cannot create deck for Neutral class");
        this.clazz = clazz;
    }

    /**
     * Adds given number of given cards to the {@code DeckBuilder}.
     *
     * @param card the given card
     * @param num the given number
     *
     * @throws IllegalArgumentException if the given number is invalid for this {@code DeckBuilder}.
     */
    public void addCard(CardDescr card, int num) {
        if (num <= 0)
            throw new IllegalArgumentException("The given number cannot be less than 1.");
        if (cardNum + num > 30)
            throw new IllegalArgumentException("There are already " + cardNum + " cards in the deck, "
                + "adding " + num + " more cards is invalid.");
        if (card.getRarity() == CardRarity.LEGENDARY && num > 1)
            throw new IllegalArgumentException("The number of Legendary cards with the same name in the deck cannot be more than 1.");
        if (num > 2)
            throw new IllegalArgumentException("The number of cards with the same name in the deck cannot be more than 2.");
        int orgNum = cards.computeIfAbsent(card, (c) -> 0);
        int newNum = orgNum + num;
        if (card.getRarity() == CardRarity.LEGENDARY && newNum > 1)
            throw new IllegalArgumentException("The number of Legendary cards with the same name in the deck cannot be more than 1, "
                + "there are already 1 of it");
        if (newNum > 2)
            throw new IllegalArgumentException("The number of cards with the same name in the deck cannot be more than 1, "
                                                   + "there are already " + orgNum + " of it");
        cards.put(card, newNum);
    }

    /**
     * Returns if the number of cards in the deck has reached {@code 30}.
     */
    public boolean isFull() {
        return cardNum == 30;
    }

    /**
     * Builds a {@link Deck} with this {@code DeckBuilder} for the given {@link Player owner}.
     *
     * @throws IllegalStateException if the number of cards in the deck has not reached {@code 30}.
     */
    public Deck build(Player owner) {
        if (!isFull())
            throw new IllegalStateException("A deck must consist of 30 cards, while there are only " + cardNum + ".");
        Deck result = new Deck(owner);
        List<CardDescr> cardList = new ArrayList<>(30);
        for (Map.Entry<CardDescr, Integer> entry : cards.entrySet())
            for (int i = 0; i < entry.getValue(); i++)
                cardList.add(entry.getKey());
        assert cardList.size() == 30;
        result.setCards(cardList);
        return result;
    }

}
