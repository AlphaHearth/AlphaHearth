package info.hearthsim.brazier.actions;

import info.hearthsim.brazier.cards.Card;

/**
 * Mana cost adjuster for a certain card. It's a functional interface with its sole un-implemented method
 * {@link #adjustCost(Card, int)}, which returns the adjusted mana cost of the given {@link Card}.
 * <p>
 * For predefined {@code ManaCostAdjuster}s, see {@link ManaCostAdjusters}.
 *
 * @see ManaCostAdjusters
 */
public interface ManaCostAdjuster {
    /**
     * Returns the adjusted mana cost of the given {@link Card} according to the providing current mana cost.
     */
    public int adjustCost(Card card, int currentManaCost);
}
