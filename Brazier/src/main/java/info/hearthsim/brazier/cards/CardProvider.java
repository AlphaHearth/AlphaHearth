package info.hearthsim.brazier.cards;

import java.util.function.Supplier;

/**
 * Functional interface with its sole un-implemented method {@link #getCard} which returns a
 * {@link CardDescr}.
 */
public interface CardProvider extends Supplier<CardDescr> {
    /**
     * Returns the {@link CardDescr}.
     */
    public CardDescr getCard();

    /**
     * {@inheritDoc}
     *
     * @implNote
     * Returns the {@link CardDescr}.
     */
    public default CardDescr get() {
        return getCard();
    }
}
