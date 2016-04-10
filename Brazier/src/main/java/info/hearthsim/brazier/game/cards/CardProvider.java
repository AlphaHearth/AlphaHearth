package info.hearthsim.brazier.game.cards;

import info.hearthsim.brazier.db.CardDescr;

import java.util.function.Supplier;

/**
 * Functional interface with its sole un-implemented method {@link #getCard} which returns a
 * {@link CardDescr}.
 * <p>
 * Instances of {@code CardProvider} must be <b>immutable</b>: no state can be stored. Using the
 * interface as a functional interface and implementing it with lambda expression is highly recommended.
 */
@FunctionalInterface
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
