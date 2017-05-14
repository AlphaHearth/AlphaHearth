package info.hearthsim.brazier.actions;

import info.hearthsim.brazier.game.Game;
import info.hearthsim.brazier.game.GameProperty;
import org.jtrim.utils.ExceptionHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Functional interface with sole un-implemented method {@link #select(Actor)} which
 * selects {@link Stream} of {@code Selection} with the given {@code Actor}.
 * <p>
 * For predefined {@code EntitySelector}s, see {@link EntitySelectors}.
 *
 * @see EntitySelectors
 */
public interface EntitySelector<Actor extends GameProperty, Selection> {
    /**
     * Selects {@code Selection} from the given {@link Game} with the given {@code Actor} and
     * returns the selected results as a {@link Stream}.
     */
    public Stream<? extends Selection> select(Actor actor);

    /**
     * Applies the given {@link Function} on the selected {@link Stream} of selections by this selector.
     */
    public default void forEach(Actor actor, Consumer<? super Selection> action) {
        select(actor).forEach(action::accept);
    }

    /**
     * Converts this {@code EntitySelector} to a {@link TargetedEntitySelector}.
     */
    public default TargetedEntitySelector<Actor, Object, Selection> toTargeted() {
        return (Actor actor, Object target) -> select(actor);
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
    public static <Actor extends GameProperty, Selection> EntitySelector<Actor, Selection> merge(
            Collection<? extends EntitySelector<? super Actor, ? extends Selection>> selectors) {
        ExceptionHelper.checkNotNullElements(selectors, "selectors");

        List<EntitySelector<? super Actor, ? extends Selection>> selectorsCopy = new ArrayList<>(selectors);

        return (Actor actor) -> {
            Stream<? extends Selection> result = null;
            for (EntitySelector<? super Actor, ? extends Selection> selector: selectorsCopy) {
                Stream<? extends Selection> selected = selector.select(actor);
                result = result != null
                        ? Stream.concat(result, selected)
                        : selected;
            }
            return result != null ? result : Stream.empty();
        };
    }
}
