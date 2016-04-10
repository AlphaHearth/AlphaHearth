package info.hearthsim.brazier.actions;

import info.hearthsim.brazier.game.cards.Card;

/**
 * A reference to a {@link Card}, with the sole un-implemented method {@link #getCard()},
 * which returns the referring {@link Card}
 */
public interface CardRef {

    /**
     * Returns the referring {@code Card}
     * @return the referring {@code Card}
     */
    public Card getCard();
}
