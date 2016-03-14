package info.hearthsim.brazier.actions;

import info.hearthsim.brazier.World;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import org.jtrim.utils.ExceptionHelper;

/**
 * Functional interface with its sole un-implemented method {@link #select(World, Stream)}, which can be used
 * in a {@link Stream} pipeline to achieve customized features.
 * <p>
 * For predefined {@code EntityFilter}s, see {@link EntityFilters}.
 *
 * @see EntityFilters
 */
public interface EntityFilter<Entity> {
    /**
     * Selects from the given {@link Stream} of entities with the given {@link World}
     * and returns the selected results as another {@code Stream} of entities.
     *
     * @param world the given {@code World}.
     * @param entities the given {@code Stream} of entities.
     * @return the {@code Stream} of selected entities.
     */
    public Stream<? extends Entity> select(World world, Stream<? extends Entity> entities);

    /**
     * Merges the given collection of {@link EntityFilter}s to one which executes their selection
     * logic on the given {@link Stream} of entities sequentially.
     *
     * @throws NullPointerException if any of the given {@code EntityFilter}s is {@code null}.
     */
    public static <Entity> EntityFilter<Entity> merge(
            Collection<? extends EntityFilter<Entity>> filters) {
        ExceptionHelper.checkNotNullElements(filters, "filters");

        if (filters.isEmpty()) {
            return EntityFilters.empty();
        }

        List<EntityFilter<Entity>> filtersCopy = new ArrayList<>(filters);

        return (World world, Stream<? extends Entity> entities) -> {
            Stream<? extends Entity> currentTargets = entities;
            for (EntityFilter<Entity> filter: filtersCopy) {
                currentTargets = filter.select(world, currentTargets);
            }
            return currentTargets;
        };
    }
}
