package info.hearthsim.brazier.actions;

import info.hearthsim.brazier.game.Game;
import info.hearthsim.brazier.PlayerProperty;
import info.hearthsim.brazier.parsing.NamedArg;

import java.util.function.Predicate;

import org.jtrim.utils.ExceptionHelper;

/**
 * Predefined {@link TargetedActionCondition}s.
 */
public final class TargetedActionConditions {

    /**
     * Returns a {@link TargetedActionCondition} which tests if the given {@code actor}
     * satisfy the given {@link Predicate}.
     * <p>
     * For predefined {@code Predicate}s, see {@link EntityFilters}.
     *
     * @see EntityFilters
     */
    public static <Actor, Target> TargetedActionCondition<Actor, Target> forActor(
        @NamedArg("filter") Predicate<? super Actor> filter) {
        ExceptionHelper.checkNotNullArgument(filter, "filter");

        return (Game game, Actor actor, Target target) -> {
            return filter.test(actor);
        };
    }

    /**
     * Returns a {@link TargetedActionCondition} which tests if the given {@code target}
     * satisfy the given {@link Predicate}.
     * <p>
     * For predefined {@code Predicate}s, see {@link EntityFilters}.
     *
     * @see EntityFilters
     */
    public static <Actor, Target> TargetedActionCondition<Actor, Target> forTarget(
        @NamedArg("filter") Predicate<? super Target> filter) {
        ExceptionHelper.checkNotNullArgument(filter, "filter");

        return (Game game, Actor actor, Target target) -> {
            return filter.test(target);
        };
    }

    /**
     * Returns a {@link TargetedActionCondition} which tests if the {@code actor} and {@code target}
     * have same owner.
     */
    public static <Actor extends PlayerProperty, Target extends PlayerProperty>
    TargetedActionCondition<Actor, Target> sameOwner() {
        return (Game game, Actor actor, Target target) -> {
            return actor.getOwner() == target.getOwner();
        };
    }

    private TargetedActionConditions() {
        throw new AssertionError();
    }
}
