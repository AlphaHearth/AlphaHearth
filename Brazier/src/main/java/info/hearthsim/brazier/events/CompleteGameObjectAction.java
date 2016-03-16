package info.hearthsim.brazier.events;

import info.hearthsim.brazier.actions.GameObjectAction;
import info.hearthsim.brazier.Game;
import info.hearthsim.brazier.actions.undo.UndoAction;
import org.jtrim.utils.ExceptionHelper;

public interface CompleteGameObjectAction <T> extends GameObjectAction<T>, UndoAction {

    public static <T> CompleteGameObjectAction<T> create(
            GameObjectAction<? super T> action,
            UndoAction undo) {
        ExceptionHelper.checkNotNullArgument(action, "action");
        ExceptionHelper.checkNotNullArgument(undo, "undo");

        return new CompleteGameObjectAction<T>() {
            @Override
            public UndoAction alterGame(Game game, T object) {
                return action.alterGame(game, object);
            }

            @Override
            public void undo() {
                undo.undo();
            }
        };
    }

    public static <T> CompleteGameObjectAction<T> doNothing(UndoAction undo) {
        return create((game, object) -> UndoAction.DO_NOTHING, undo);
    }

    public static <T> CompleteGameObjectAction<T> nothingToUndo(GameObjectAction<? super T> action) {
        return create(action, UndoAction.DO_NOTHING);
    }
}
