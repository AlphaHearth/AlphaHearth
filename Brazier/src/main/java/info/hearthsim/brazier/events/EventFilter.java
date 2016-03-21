package info.hearthsim.brazier.events;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import info.hearthsim.brazier.GameProperty;
import org.jtrim.utils.ExceptionHelper;

/**
 * Filter for an {@code EventAction}, which determines the {@code EventAction} should be triggered on a
 * certain event. It is usually used as a functional interface with its sole-unimplemented method
 * {@link #applies(Owner, Source)}, which returns {@code true} only if the given source event and
 * the owner of the {@code EventAction} should trigger its partner {@code EventAction}. For the full
 * explanation of these two parameters, see {@link EventAction}.
 * <p>
 * For predefined {@code EventFilter}s, see {@link EventFilters}.
 *
 * @param <Owner> the type of the owner of this triggering action.
 * @param <Source> the type of the triggering source of this triggering action.
 *
 * @see EventAction
 * @see EventFilters
 */
@FunctionalInterface
public interface EventFilter <Owner extends GameProperty, Source> {
    public static final EventFilter ANY = (owner, source) -> true;

    public boolean applies(Owner owner, Source source);

    public static <Self extends GameProperty, T> EventFilter<? super Self, ? super T> merge(
            Collection<? extends EventFilter<? super Self, ? super T>> filters) {

        int filterCount = filters.size();
        if (filterCount == 0) {
            return EventFilters.ANY;
        }
        if (filterCount == 1) {
            return filters.iterator().next();
        }

        List<EventFilter<? super Self, ? super T>> filtersCopy = new ArrayList<>(filters);
        ExceptionHelper.checkNotNullElements(filtersCopy, "filters");

        return (Self owner, T eventSource) -> {
            for (EventFilter<? super Self, ? super T> filter: filtersCopy) {
                if (!filter.applies(owner, eventSource)) {
                    return false;
                }
            }
            return true;
        };
    }
}
