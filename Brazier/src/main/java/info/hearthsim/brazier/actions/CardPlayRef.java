package info.hearthsim.brazier.actions;

import info.hearthsim.brazier.game.cards.Card;

/**
 * A reference of an action of playing a card with a specific mana cost.
 */
public interface CardPlayRef extends CardRef {
    /**
     * Returns the mana used to play this card.
     * @return the mana used to play this card.
     */
    public int getManaCost();

    /**
     * Returns the card that is played.
     * @return the card that is played.
     */
    @Override
    public Card getCard();
}
