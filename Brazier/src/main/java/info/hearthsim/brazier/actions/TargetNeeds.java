package info.hearthsim.brazier.actions;

import info.hearthsim.brazier.PlayerPredicate;
import info.hearthsim.brazier.game.Character;
import info.hearthsim.brazier.game.*;
import info.hearthsim.brazier.game.minions.Minion;
import info.hearthsim.brazier.parsing.NamedArg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Predefined {@link TargetNeed}s.
 */
public final class TargetNeeds {
    private static final PlayerPredicate<Character> CHARACTER_DAMAGED = (playerId, character) -> {
        return character.isDamaged();
    };

    private static final PlayerPredicate<Character> CHARACTER_NOT_DAMAGED = (playerId, character) -> {
        return !character.isDamaged();
    };

    /** {@link TargetNeed} which does not need any target. */
    public static final TargetNeed NO_NEED = new TargetNeed(PlayerPredicate.NONE, PlayerPredicate.NONE, false);
    /** {@link TargetNeed} which is valid for any {@link Hero}. */
    public static final TargetNeed ALL_HEROES = new TargetNeed(PlayerPredicate.ANY, PlayerPredicate.NONE);
    /** {@link TargetNeed} which is valid for any {@link Minion}. */
    public static final TargetNeed ALL_MINIONS = new TargetNeed(PlayerPredicate.NONE, PlayerPredicate.ANY);
    /** {@link TargetNeed} which is valid for any friendly {@link Minion}. */
    public static final TargetNeed FRIENDLY_MINIONS = new TargetNeed(PlayerPredicate.NONE, TargetNeeds::allowSelf);
    /** {@link TargetNeed} which is valid for any enemy {@link Minion}. */
    public static final TargetNeed ENEMY_MINIONS = new TargetNeed(PlayerPredicate.NONE, TargetNeeds::allowEnemy);
    /** {@link TargetNeed} which is valid for any {@link Hero} and {@link Minion}. */
    public static final TargetNeed ALL_TARGETS = new TargetNeed(PlayerPredicate.ANY, PlayerPredicate.ANY);
    /** {@link TargetNeed} which is valid for any friendly {@link Hero} and {@link Minion}. */
    public static final TargetNeed FRIENDLY_TARGETS = new TargetNeed(TargetNeeds::allowSelf, TargetNeeds::allowSelf);
    /** {@link TargetNeed} which is valid for any enemy {@link Hero} and {@link Minion}. */
    public static final TargetNeed ENEMY_TARGETS = new TargetNeed(TargetNeeds::allowEnemy, TargetNeeds::allowEnemy);

    /** {@link TargetNeed} which is valid for any damaged {@link Character}. */
    public static final TargetNeed TARGET_DAMAGED = new TargetNeed(CHARACTER_DAMAGED, CHARACTER_DAMAGED);
    /** {@link TargetNeed} which is valid for any not-damaged {@link Character}. */
    public static final TargetNeed TARGET_NOT_DAMAGED = new TargetNeed(CHARACTER_NOT_DAMAGED, CHARACTER_NOT_DAMAGED);

    /** {@link TargetNeed} which is valid for any <b>Taunt</b> {@link Minion}. */
    public static final TargetNeed IS_TAUNT = new TargetNeed(PlayerPredicate.ANY, (PlayerId playerId, Minion arg) -> {
        return arg.getBody().isTaunt();
    });

    /**
     * Returns a {@link TargetNeed} which is valid for any {@link Character} with its attack less
     * than the given amount.
     */
    public static TargetNeed attackIsLessThan(@NamedArg("attack") int attack) {
        PlayerPredicate<Character> filter = (playerId, character) -> {
            return character.getAttackTool().getAttack() < attack;
        };
        return new TargetNeed(filter, filter);
    }

    /**
     * Returns a {@link TargetNeed} which is valid for any {@link Character} with its attack larger
     * than the given amount.
     */
    public static TargetNeed attackIsMoreThan(@NamedArg("attack") int attack) {
        PlayerPredicate<Character> filter = (playerId, character) -> {
            return character.getAttackTool().getAttack() > attack;
        };
        return new TargetNeed(filter, filter);
    }

    /**
     * Returns a {@link TargetNeed} which is valid for any {@link Character} with all of the given
     * {@link Keyword}s.
     */
    public static TargetNeed hasKeyword(@NamedArg("keywords") Keyword[] keywords) {
        List<Keyword> keywordsCopy = new ArrayList<>(Arrays.asList(keywords));
        PlayerPredicate<LabeledEntity> filter = (playerId, target) -> {
            return target.getKeywords().containsAll(keywordsCopy);
        };
        return new TargetNeed(filter, filter);
    }

    private TargetNeeds() {
        throw new AssertionError();
    }

    private static boolean allowEnemy(PlayerId playerId, Minion minion) {
        return !Objects.equals(playerId, minion.getOwner().getPlayerId());
    }

    private static boolean allowEnemy(PlayerId playerId, Hero hero) {
        return !Objects.equals(playerId, hero.getOwner().getPlayerId());
    }

    private static boolean allowSelf(PlayerId playerId, Minion minion) {
        return Objects.equals(playerId, minion.getOwner().getPlayerId());
    }

    private static boolean allowSelf(PlayerId playerId, Hero hero) {
        return Objects.equals(playerId, hero.getOwner().getPlayerId());
    }
}
