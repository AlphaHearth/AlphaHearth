package info.hearthsim.brazier.events;

import info.hearthsim.brazier.Game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jtrim.utils.ExceptionHelper;

public interface GameEventFilter <Self, T> {
    public static final GameEventFilter<Object, Object> ANY = (game, source, target) -> true;

    public boolean applies(Game game, Self owner, T eventSource);

    public static <Self, T> GameEventFilter<? super Self, ? super T> merge(
            Collection<? extends GameEventFilter<? super Self, ? super T>> filters) {

        int filterCount = filters.size();
        if (filterCount == 0) {
            return GameEventFilters.ANY;
        }
        if (filterCount == 1) {
            return filters.iterator().next();
        }

        List<GameEventFilter<? super Self, ? super T>> filtersCopy = new ArrayList<>(filters);
        ExceptionHelper.checkNotNullElements(filtersCopy, "filters");

        return (Game game, Self owner, T eventSource) -> {
            for (GameEventFilter<? super Self, ? super T> filter: filtersCopy) {
                if (!filter.applies(game, owner, eventSource)) {
                    return false;
                }
            }
            return true;
        };
    }
}
