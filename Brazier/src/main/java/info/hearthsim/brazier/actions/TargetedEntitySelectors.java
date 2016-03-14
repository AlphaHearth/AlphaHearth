package info.hearthsim.brazier.actions;

import info.hearthsim.brazier.*;
import info.hearthsim.brazier.minions.Minion;
import info.hearthsim.brazier.parsing.NamedArg;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.jtrim.utils.ExceptionHelper;

/**
 * Predefined {@link TargetedEntitySelector}s.
 */
public final class TargetedEntitySelectors {

    /**
     * Returns a {@link TargetedEntitySelector} which returns nothing.
     */
    public static <Actor, Target, Selection> TargetedEntitySelector<Actor, Target, Selection> empty() {
        return (World world, Actor actor, Target target) -> Stream.empty();
    }

    /**
     * Returns a {@link TargetedEntitySelector} which returns only the given actor.
     */
    public static <Actor, Target> TargetedEntitySelector<Actor, Target, Actor> self() {
        return (World world, Actor actor, Target target) -> Stream.of(actor);
    }

    /**
     * Returns a {@link TargetedEntitySelector} which returns the results of the given selector after excluding the
     * given target.
     */
    public static <Actor, Target, Selection> TargetedEntitySelector<Actor, Target, Selection> excludeTarget(
            @NamedArg("selector") TargetedEntitySelector<? super Actor, ? super Target, ? extends Selection> selector) {
        ExceptionHelper.checkNotNullArgument(selector, "selector");

        return (World world, Actor actor, Target target) -> {
            Stream<? extends Selection> selection = selector.select(world, actor, target);
            return selection.filter((element) -> element != target);
        };
    }

    /**
     * Returns a {@link TargetedEntitySelector} which applies the given {@link EntityFilter} to the given selector's
     * results and returns the filtered {@code Stream}.
     */
    public static <Actor, Target, Selection> TargetedEntitySelector<Actor, Target, Selection> filtered(
            @NamedArg("filter") EntityFilter<Selection> filter,
            @NamedArg("selector") TargetedEntitySelector<? super Actor, ? super Target, ? extends Selection> selector) {
        ExceptionHelper.checkNotNullArgument(filter, "filter");
        ExceptionHelper.checkNotNullArgument(selector, "selector");

        return (World world, Actor actor, Target target) -> {
            Stream<? extends Selection> selection = selector.select(world, actor, target);
            return filter.select(world, selection);
        };
    }

    /**
     * Returns a {@link TargetedEntitySelector} which returns only the given target.
     */
    public static <Actor, Target> TargetedEntitySelector<Actor, Target, Target> target() {
        return (World world, Actor actor, Target target) -> Stream.of(target);
    }

    /**
     * Returns a {@link TargetedEntitySelector} which returns only the owner of the given target.
     */
    public static <Actor, Target extends PlayerProperty> TargetedEntitySelector<Actor, Target, Player> targetsOwnerPlayer() {
        return (World world, Actor actor, Target target) -> Stream.of(target.getOwner());
    }

    /**
     * Returns a {@link TargetedEntitySelector} which returns only the owner's {@link Hero} of the given target.
     */
    public static <Actor, Target extends PlayerProperty> TargetedEntitySelector<Actor, Target, Hero> targetsHero() {
        return (World world, Actor actor, Target target) -> Stream.of(target.getOwner().getHero());
    }

    /**
     * Returns a {@link TargetedEntitySelector} which returns the neighbouring minions of the target minion.
     */
    public static <Actor, Target extends Minion> TargetedEntitySelector<Actor, Target, Minion> targetsNeighbours() {
        return (World world, Actor actor, Target target) -> {
            BoardSide board = target.getOwner().getBoard();
            int sourceIndex = board.indexOf(target.getTargetId());
            List<Minion> neighbours = new ArrayList<>(2);

            Minion left = board.getMinion(sourceIndex - 1);
            if (left != null)
                neighbours.add(left);

            Minion right = board.getMinion(sourceIndex + 1);
            if (right != null)
                neighbours.add(right);

            return neighbours.stream();
        };
    }

    /**
     * Returns a {@link TargetedEntitySelector} which returns all minions on the same board side of the given target.
     */
    public static <Actor, Target extends PlayerProperty> TargetedEntitySelector<Actor, Target, Minion> targetsBoard() {
        return (World world, Actor actor, Target target) -> {
            return target.getOwner().getBoard().getAllMinions().stream();
        };
    }

    private TargetedEntitySelectors() {
        throw new AssertionError();
    }
}
