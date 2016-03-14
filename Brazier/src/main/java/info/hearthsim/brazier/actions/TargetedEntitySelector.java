package info.hearthsim.brazier.actions;

import info.hearthsim.brazier.World;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import org.jtrim.utils.ExceptionHelper;

/**
 * Functional interface with sole un-implemented method {@link #select(World, Actor, Target)} which
 * selects {@link Stream} of {@code Selection} from the given {@link World} with the given {@code Actor}
 * and {@code Target}.
 * <p>
 * For predefined {@code TargetedEntitySelector}s, see {@link TargetedEntitySelectors}.
 *
 * @see TargetedEntitySelectors
 */
public interface TargetedEntitySelector<Actor, Target, Selection> {
    /**
     * Selects {@code Selection} from the given {@link World} with the given {@code Actor} and {@code Target}
     * and returns the selected results as a {@link Stream}.
     */
    public Stream<? extends Selection> select(World world, Actor actor, Target target);

    /**
     * Merges a given collection of {@link TargetedEntitySelector}s to one which returns all the selected results
     * from the given selectors sequentially.
     * <p>
     * The given selectors can return any type of selection, and the merged selector would use their common
     * base as the selected type.
     *
     * @throws NullPointerException if any of the given selectors are {@code null}.
     */
    public static <Actor, Target, Selection> TargetedEntitySelector<Actor, Target, Selection> merge(
            Collection<? extends TargetedEntitySelector<? super Actor, ? super Target, ? extends Selection>> selectors) {
        ExceptionHelper.checkNotNullElements(selectors, "selectors");

        if (selectors.isEmpty()) {
            return TargetedEntitySelectors.empty();
        }

        List<TargetedEntitySelector<? super Actor, ? super Target, ? extends Selection>> selectorsCopy =
            new ArrayList<>(selectors);

        return (World world, Actor actor, Target target) -> {
            Stream<? extends Selection> result = null;
            for (TargetedEntitySelector<? super Actor, ? super Target, ? extends Selection> selector: selectorsCopy) {
                Stream<? extends Selection> selected = selector.select(world, actor, target);
                result = result != null
                        ? Stream.concat(result, selected)
                        : selected;
            }
            return result != null ? result : Stream.empty();
        };
    }
}
