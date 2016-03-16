package info.hearthsim.brazier.events;

import info.hearthsim.brazier.PlayerProperty;
import info.hearthsim.brazier.Game;
import info.hearthsim.brazier.actions.undo.UndoAction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jtrim.utils.ExceptionHelper;

public interface GameEventAction <Self extends PlayerProperty, EventSource> {
    public static final GameEventAction<PlayerProperty, Object> DO_NOTHING
            = (game, self, eventSource) -> UndoAction.DO_NOTHING;

    public UndoAction alterGame(Game game, Self self, EventSource eventSource);

    public static <Self extends PlayerProperty, EventSource> GameEventAction<? super Self, ? super EventSource> merge(
            Collection<? extends GameEventAction<? super Self, ? super EventSource>> actions) {

        int filterCount = actions.size();
        if (filterCount == 0) {
            return (game, self, eventSource) -> UndoAction.DO_NOTHING;
        }
        if (filterCount == 1) {
            return actions.iterator().next();
        }

        List<GameEventAction<? super Self, ? super EventSource>> actionsCopy = new ArrayList<>(actions);
        ExceptionHelper.checkNotNullElements(actionsCopy, "actions");

        return (Game game, Self owner, EventSource eventSource) -> {
            UndoAction.Builder result = new UndoAction.Builder(actionsCopy.size());
            for (GameEventAction<? super Self, ? super EventSource> action: actionsCopy) {
                result.addUndo(action.alterGame(game, owner, eventSource));
            }
            return result;
        };
    }
}
