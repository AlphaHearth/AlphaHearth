package info.hearthsim.brazier.actions;

import info.hearthsim.brazier.World;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jtrim.utils.ExceptionHelper;

/**
 * Condition for a targeted action, which also acts as a functional interface with its sole un-implemented
 * method {@link #applies(World, Actor, Target)} which checks if the given {@code actor} and {@code target}
 * in the given {@link World} satisfies the condition.
 * <p>
 * For predefined {@code TargetedActionCondition}s, see {@link TargetedActionConditions}.
 *
 * @see TargetedActionConditions
 */
public interface TargetedActionCondition<Actor, Target> {
    /**
     * Returns if the given {@code actor} and the given {@code target} in the given {@link World}
     * satisfy the condition.
     */
    public boolean applies(World world, Actor actor, Target target);

    /**
     * Merges the given collection of {@link TargetedActionCondition} to one which checks if the
     * given {@code actor} and {@code target} satisfy all of them.
     *
     * @throws NullPointerException if any of the given filters is {@code null}.
     */
    public static <Actor, Target> TargetedActionCondition<Actor, Target> merge(
            Collection<? extends TargetedActionCondition<? super Actor, ? super Target>> filters) {
        ExceptionHelper.checkNotNullElements(filters, "filters");

        if (filters.isEmpty()) {
            return (world, actor, target) -> true;
        }

        List<TargetedActionCondition<? super Actor, ? super Target>> filtersCopy = new ArrayList<>(filters);

        return (World world, Actor actor, Target target) -> {
            for (TargetedActionCondition<? super Actor, ? super Target> filter: filtersCopy) {
                if (!filter.applies(world, actor, target)) {
                    return false;
                }
            }
            return true;
        };
    }
}
