package info.hearthsim.brazier.events;

import info.hearthsim.brazier.Game;
import info.hearthsim.brazier.PlayerProperty;
import info.hearthsim.brazier.actions.undo.UndoAction;
import org.jtrim.utils.ExceptionHelper;

public interface CompleteGameEventAction <Self extends PlayerProperty, EventSource>
extends
    GameEventAction<Self, EventSource>,
    UndoAction {

    public static final CompleteGameEventAction<PlayerProperty, Object> NO_EVENT_ACTION
            = create(GameEventAction.DO_NOTHING, UndoAction.DO_NOTHING);

    public static <Self extends PlayerProperty, EventSource> CompleteGameEventAction<Self, EventSource> create(
            GameEventAction<? super Self, ? super EventSource> action,
            UndoAction undo) {
        ExceptionHelper.checkNotNullArgument(action, "action");
        ExceptionHelper.checkNotNullArgument(undo, "undo");

        return new CompleteGameEventAction<Self, EventSource>() {
            @Override
            public UndoAction alterGame(Game game, Self self, EventSource eventSource) {
                return action.alterGame(game, self, eventSource);
            }

            @Override
            public void undo() {
                undo.undo();
            }
        };
    }

    public static <Self extends PlayerProperty, EventSource> CompleteGameEventAction<Self, EventSource> doNothing(UndoAction undo) {
        return create(GameEventAction.DO_NOTHING, undo);
    }

    public static <Self extends PlayerProperty, EventSource> CompleteGameEventAction<Self, EventSource> nothingToUndo(
            GameEventAction<? super Self, ? super EventSource> action) {
        return create(action, UndoAction.DO_NOTHING);
    }
}
