package info.hearthsim.brazier.actions;

import info.hearthsim.brazier.game.cards.Card;

/**
 * Mana cost adjuster for a certain card. It's a functional interface with its sole un-implemented method
 * {@link #adjustCost(Card, int)}, which returns the adjusted mana cost of the given {@link Card}.
 * <p>
 * Instances of {@code ManaCostAdjuster} must be <b>immutable</b>: no state can be stored. Using the interface as
 * a functional interface and implementing it with lambda expression is highly recommended.
 * <p>
 * For predefined {@code ManaCostAdjuster}s, see {@link ManaCostAdjusters}.
 *
 * @see ManaCostAdjusters
 */
@FunctionalInterface
public interface ManaCostAdjuster {
    /**
     * Returns the adjusted mana cost of the given {@link Card} according to the providing current mana cost.
     */
    public int adjustCost(Card card, int currentManaCost);
}
