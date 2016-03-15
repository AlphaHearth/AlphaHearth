package info.hearthsim.brazier.actions;

import info.hearthsim.brazier.World;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import info.hearthsim.brazier.actions.undo.UndoAction;
import org.jtrim.utils.ExceptionHelper;

/**
 * Functional interface with sole un-implemented method {@link #select(World, Actor)} which
 * selects {@link Stream} of {@code Selection} from the given {@link World} with the given {@code Actor}.
 * <p>
 * For predefined {@code EntitySelector}s, see {@link EntitySelectors}.
 *
 * @see EntitySelectors
 */
public interface EntitySelector<Actor, Selection> {
    /**
     * Selects {@code Selection} from the given {@link World} with the given {@code Actor} and
     * returns the selected results as a {@link Stream}.
     */
    public Stream<? extends Selection> select(World world, Actor actor);

    /**
     * Applies the given {@link Function} on the selected {@link Stream} of selections by this selector.
     */
    public default UndoAction forEach(World world, Actor actor, Function<? super Selection, UndoAction> action) {
        UndoAction.Builder builder = new UndoAction.Builder();
        select(world, actor).forEach((selection) -> {
            builder.addUndo(action.apply(selection));
        });
        return builder;
    }

    /**
     * Converts this {@code EntitySelector} to a {@link TargetedEntitySelector}.
     */
    public default TargetedEntitySelector<Actor, Object, Selection> toTargeted() {
        return (World world, Actor actor, Object target) -> select(world, actor);
    }

    /**
     * Merges a given collection of {@link EntitySelector}s to one which returns all the selected results
     * from the given selectors sequentially.
     * <p>
     * The given selectors can return any type of selection, and the merged selector would use their common
     * base as the selected type.
     *
     * @throws NullPointerException if any of the given selectors are {@code null}.
     */
    public static <Actor, Selection> EntitySelector<Actor, Selection> merge(
            Collection<? extends EntitySelector<? super Actor, ? extends Selection>> selectors) {
        ExceptionHelper.checkNotNullElements(selectors, "selectors");

        List<EntitySelector<? super Actor, ? extends Selection>> selectorsCopy = new ArrayList<>(selectors);

        return (World world, Actor actor) -> {
            Stream<? extends Selection> result = null;
            for (EntitySelector<? super Actor, ? extends Selection> selector: selectorsCopy) {
                Stream<? extends Selection> selected = selector.select(world, actor);
                result = result != null
                        ? Stream.concat(result, selected)
                        : selected;
            }
            return result != null ? result : Stream.empty();
        };
    }
}