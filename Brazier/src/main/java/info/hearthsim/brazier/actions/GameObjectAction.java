package info.hearthsim.brazier.actions;

import info.hearthsim.brazier.Game;
import info.hearthsim.brazier.actions.undo.UndoAction;
import org.jtrim.utils.ExceptionHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Actions that can be used to alter a given {@code Game} and a given object. Usually used
 * as a functional interface with its sole un-implemented method {@link #alterGame(Game, Object)}.
 */
public interface GameObjectAction <T> {
    // TODO check what the 2nd parameter stands for
    public UndoAction alterGame(Game game, T object);

    public default GameAction toGameAction(T object) {
        return (game) -> alterGame(game, object);
    }

    /**
     * Executes the given collection of {@code GameObjectAction} and returns the corresponding
     * {@code GameObjectAction} which can be used to undo.
     *
     * @param actions the collection of {@code GameObjectAction}
     * @return {@code GameObjectAction} which can undo the given actions.
     */
    public static <T> GameObjectAction<T> merge(Collection<? extends GameObjectAction<T>> actions) {
        List<GameObjectAction<T>> actionsCopy = new ArrayList<>(actions);
        ExceptionHelper.checkNotNullElements(actionsCopy, "actions");

        int count = actionsCopy.size();
        if (count == 0) {
            return (game, object) -> UndoAction.DO_NOTHING;
        }
        if (count == 1) {
            return actionsCopy.get(0);
        }

        return (Game game, T self) -> {
            UndoAction.Builder result = new UndoAction.Builder(actionsCopy.size());
            for (GameObjectAction<T> action : actionsCopy) {
                result.addUndo(action.alterGame(game, self));
            }
            return result;
        };
    }
}
