package info.hearthsim.brazier.abilities;

import info.hearthsim.brazier.*;
import info.hearthsim.brazier.cards.Card;
import info.hearthsim.brazier.minions.Minion;
import info.hearthsim.brazier.weapons.Weapon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Predefined {@link AuraTargetProvider}s.
 */
public final class AuraTargetProviders {

    /**
     * {@code AuraTargetProvider} which returns the source of the aura.
     */
    public static <Self> AuraTargetProvider<Self, Self> selfProvider() {
        return (world, source) -> {
            return Collections.singletonList(source);
        };
    }

    /* AuraTargetProviders for Card */

    /**
     * {@code AuraTargetProvider} which returns the cards on the hands of both players.
     */
    public static final AuraTargetProvider<Object, Card> HAND_PROVIDER = (World world, Object source) -> {
        List<Card> result = new ArrayList<>(2 * Player.MAX_HAND_SIZE);
        world.getPlayer1().getHand().collectCards(result);
        world.getPlayer2().getHand().collectCards(result);
        return result;
    };

    /**
     * {@code AuraTargetProvider} which returns the cards on the hands of the aura source's owner.
     */
    public static final AuraTargetProvider<PlayerProperty, Card> OWN_HAND_PROVIDER = (World world, PlayerProperty source) -> {
        return source.getOwner().getHand().getCards();
    };

    /**
     * {@code AuraTargetProvider} which returns the cards on the hands of the opponent of the aura source's owner.
     */
    public static final AuraTargetProvider<PlayerProperty, Card> OPPONENT_HAND_PROVIDER = (World world, PlayerProperty source) -> {
        return source.getOwner().getOpponent().getHand().getCards();
    };

    /* AuraTargetProviders for Minion */

    /**
     * Returns a {@link AuraTargetProvider} which returns the {@link Hero}es of both players.
     */
    public static final AuraTargetProvider<Object, Hero> HERO_PROVIDER = (World world, Object source) -> {
        return Arrays.asList(world.getPlayer1().getHero(), world.getPlayer2().getHero());
    };

    /**
     * Returns a {@link AuraTargetProvider} which returns the {@link Hero} of the aura source's owner.
     */
    public static final AuraTargetProvider<PlayerProperty, Hero> OWN_HERO_PROVIDER = (World world, PlayerProperty source) -> {
        return Collections.singletonList(source.getOwner().getHero());
    };

    /**
     * Returns a {@link AuraTargetProvider} which returns the both {@link Player}s.
     */
    public static final AuraTargetProvider<Object, Player> PLAYER_PROVIDER = (World world, Object source) -> {
        return Arrays.asList(world.getPlayer1(), world.getPlayer2());
    };

    /**
     * Returns a {@link AuraTargetProvider} which returns the owning {@link Player} of the aura source.
     */
    public static final AuraTargetProvider<PlayerProperty, Player> OWN_PLAYER_PROVIDER = (World world, PlayerProperty source) -> {
        return Collections.singletonList(source.getOwner());
    };

    /* AuraTargetProviders for Weapons */

    /**
     * {@link AuraTargetProvider} which returns the {@link Weapon}(s) equipped by both players.
     */
    public static final AuraTargetProvider<Object, Weapon> WEAPON_PROVIDER = (World world, Object source) -> {
        Weapon weapon1 = world.getPlayer1().tryGetWeapon();
        Weapon weapon2 = world.getPlayer2().tryGetWeapon();
        if (weapon1 == null) {
            return weapon2 != null ? Collections.singletonList(weapon2) : Collections.emptyList();
        }
        else {
            return weapon2 != null ? Arrays.asList(weapon1, weapon2) : Collections.singletonList(weapon1);
        }
    };

    /**
     * {@link AuraTargetProvider} which returns the {@link Weapon} equipped by the owner of the aura source.
     */
    public static final AuraTargetProvider<PlayerProperty, Weapon> OWN_WEAPON_PROVIDER = (World world, PlayerProperty source) -> {
        Weapon weapon = source.getOwner().tryGetWeapon();
        return weapon != null ? Collections.singletonList(weapon) : Collections.emptyList();
    };

    /* AuraTargetProviders for Minions */

    /**
     * {@link AuraTargetProvider} which returns all {@link Minion}s on the board.
     */
    public static final AuraTargetProvider<Object, Minion> MINION_PROVIDER = (World world, Object source) -> {
        List<Minion> result = new ArrayList<>(2 * Player.MAX_BOARD_SIZE);
        world.getPlayer1().getBoard().collectMinions(result, Minion::notScheduledToDestroy);
        world.getPlayer2().getBoard().collectMinions(result, Minion::notScheduledToDestroy);
        BornEntity.sortEntities(result);
        return result;
    };

    /**
     * {@link AuraTargetProvider} which returns all {@link Minion}s on the same board side as the aura source.
     */
    public static final AuraTargetProvider<PlayerProperty, Minion> SAME_BOARD_MINION_PROVIDER = (world, source) -> {
        return source.getOwner().getBoard().getAllMinions();
    };

    /**
     * {@link AuraTargetProvider} which returns the {@link Minion}(s) next to the aura source minion.
     */
    public static final AuraTargetProvider<Minion, Minion> NEIGHBOURS_MINION_PROVIDER = (world, source) -> {
        BoardSide board = source.getOwner().getBoard();
        int sourceIndex = board.indexOf(source.getTargetId());
        List<Minion> neighbours = new ArrayList<>(2);

        Minion left = board.getMinion(sourceIndex - 1);
        if (left != null)
            neighbours.add(left);

        Minion right = board.getMinion(sourceIndex + 1);
        if (right != null)
            neighbours.add(right);

        return neighbours;
    };

    private AuraTargetProviders() {
        throw new AssertionError();
    }
}
