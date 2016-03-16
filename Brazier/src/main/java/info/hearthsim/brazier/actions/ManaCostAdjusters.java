package info.hearthsim.brazier.actions;

import info.hearthsim.brazier.Game;
import info.hearthsim.brazier.Hero;
import info.hearthsim.brazier.minions.Minion;
import info.hearthsim.brazier.parsing.NamedArg;
import info.hearthsim.brazier.Player;
import info.hearthsim.brazier.cards.Card;
import info.hearthsim.brazier.weapons.Weapon;

/**
 * Predefined {@link ManaCostAdjuster}s.
 */
public final class ManaCostAdjusters {
    /**
     * {@link ManaCostAdjuster} which makes the card cost {@code (1)} less for each attack point of your weapon.
     * <p>
     * See minion <em>Dread Corsair</em>.
     */
    public static final ManaCostAdjuster REDUCE_BY_WEAPON_ATTACK  = (Card card, int currentManaCost) -> {
        Player player = card.getOwner();
        Weapon weapon = player.tryGetWeapon();
        if (weapon != null) {
            return Math.max(0, currentManaCost - weapon.getAttack());
        }
        else {
            return currentManaCost;
        }
    };

    /**
     * {@link ManaCostAdjuster} which makes the card cost {@code (1)} less for each minion on the board.
     * <p>
     * See minion <em>Sea Giant</em>.
     */
    public static final ManaCostAdjuster REDUCE_BY_MINION  = (Card card, int currentManaCost) -> {
        Game game = card.getOwner().getGame();
        int manaReduction = game.getPlayer1().getBoard().getMinionCount()
                + game.getPlayer2().getBoard().getMinionCount();
        return currentManaCost - manaReduction;
    };

    /**
     * {@link ManaCostAdjuster} which makes the card cost {@code (1)} less for each damage your hero has taken.
     * <p>
     * See minion <em>Molten Giant</em>.
     */
    public static final ManaCostAdjuster REDUCE_BY_HERO_DAMAGE  = (Card card, int currentManaCost) -> {
        Hero hero = card.getOwner().getHero();
        int manaReduction = hero.getMaxHp() - hero.getCurrentHp();
        return currentManaCost - manaReduction;
    };

    /**
     * {@link ManaCostAdjuster} which makes the card cost {@code (1)} less for each other card in your hand.
     * <p>
     * See minion <em>Mountain Giant</em>.
     */
    public static final ManaCostAdjuster REDUCE_BY_HAND_SIZE = (Card card, int currentManaCost) -> {
        Hero hero = card.getOwner().getHero();
        int manaReduction = hero.getOwner().getHand().getCardCount();
        return currentManaCost - manaReduction + 1;
    };

    /**
     * {@link ManaCostAdjuster} which makes the card costs {@code (1)} less for each card in your opponent's hand.
     * <p>
     * See minion <em>Clockwork Giant</em>.
     */
    public static final ManaCostAdjuster REDUCE_BY_OPPONENTS_HAND_SIZE  = (Card card, int currentManaCost) -> {
        Player owner = card.getOwner();
        Player opponent = owner.getGame().getOpponent(owner.getPlayerId());
        Hero hero = opponent.getHero();
        int manaReduction = hero.getOwner().getHand().getCardCount();
        return currentManaCost - manaReduction;
    };

    /**
     * {@link ManaCostAdjuster} which makes the card costs {@code (1)} less for each minion that died this turn.
     * <p>
     * See spell <em>Dragon's Breath</em>.
     */
    public static final ManaCostAdjuster REDUCE_BY_DEATH_THIS_TURN  = (Card card, int currentManaCost) -> {
        Game game = card.getGame();
        int deathCount = game.getPlayer1().getGraveyard().getNumberOfMinionsDiedThisTurn()
                + game.getPlayer2().getGraveyard().getNumberOfMinionsDiedThisTurn();
        return currentManaCost - deathCount;
    };

    /**
     * Returns a {@link ManaCostAdjuster} which reduces the card's cost with the given amount if you have a
     * damaged minion.
     * <p>
     * See spell <em>Crush</em>.
     */
    public static ManaCostAdjuster reduceIfHaveDamaged(@NamedArg("reduction") int reduction) {
        return (Card card, int currentManaCost) -> {
            int damagedCount = card.getOwner().getBoard().countMinions(Minion::isDamaged);
            return damagedCount > 0 ? currentManaCost - reduction : currentManaCost;
        };
    }

    private ManaCostAdjusters() {
        throw new AssertionError();
    }
}
