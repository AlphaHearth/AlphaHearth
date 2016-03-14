package info.hearthsim.brazier.abilities;

import info.hearthsim.brazier.World;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jtrim.utils.ExceptionHelper;

/**
 * Functional interface with its sole un-implemented method {@link #isApplicable(World, Source, Target)} which
 * returns if the given source and target in the given world is applicable for the related aura.
 * <p>
 * For predefined {@code AuraFilter}s, see {@link AuraFilters}.
 *
 * @see AuraFilters
 */
// TODO combine `AuraFilter` and `AuraTargetProvider`
// TODO Extend `AuraFilter` to use on other scene, e.g. renaming it to `TargetFilter`
public interface AuraFilter<Source, Target> {
    public static final AuraFilter<Object, Object> ANY = (world, source, target) -> true;

    /**
     * Returns if the given source and target in the given world is applicable for the related aura.
     */
    public boolean isApplicable(World world, Source source, Target target);

    /**
     * Combines the two given {@link AuraFilter}s to one which checks if the given {@code source} and
     * {@code target} pass the two filters.
     *
     * @throws NullPointerException if any of the given filters is {@code null}.
     */
    public static <Source, Target> AuraFilter<Source, Target> and(
            AuraFilter<? super Source, ? super Target> filter1,
            AuraFilter<? super Source, ? super Target> filter2) {
        ExceptionHelper.checkNotNullArgument(filter1, "filter1");
        ExceptionHelper.checkNotNullArgument(filter2, "filter2");

        return (World world, Source source, Target target) -> {
            return filter1.isApplicable(world, source, target) && filter2.isApplicable(world, source, target);
        };
    }

    /**
     * Merges the given collection of {@link AuraFilter}s to one which checks if the given {@code source} and
     * {@code target} pass all of the filters.
     *
     * @throws NullPointerException if any of the given filters is {@code null}.
     */
    public static <Self, T> AuraFilter<Self, T> merge(
            Collection<? extends AuraFilter<? super Self, ? super T>> filters) {
        ExceptionHelper.checkNotNullElements(filters, "filters");

        if (filters.isEmpty()) {
            return (world, source, target) -> true;
        }

        List<AuraFilter<? super Self, ? super T>> filtersCopy = new ArrayList<>(filters);
        ExceptionHelper.checkNotNullElements(filtersCopy, "filters");

        return (World world, Self owner, T eventSource) -> {
            for (AuraFilter<? super Self, ? super T> filter: filtersCopy) {
                if (!filter.isApplicable(world, owner, eventSource)) {
                    return false;
                }
            }
            return true;
        };
    }
}
