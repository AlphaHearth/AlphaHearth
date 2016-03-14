package info.hearthsim.brazier.events;

import info.hearthsim.brazier.PlayerProperty;
import info.hearthsim.brazier.World;
import info.hearthsim.brazier.actions.undo.UndoAction;
import org.jtrim.utils.ExceptionHelper;

public interface CompleteWorldEventAction<Self extends PlayerProperty, EventSource>
extends
        WorldEventAction<Self, EventSource>,
    UndoAction {

    public static final CompleteWorldEventAction<PlayerProperty, Object> NO_EVENT_ACTION
            = create(WorldEventAction.DO_NOTHING, UndoAction.DO_NOTHING);

    public static <Self extends PlayerProperty, EventSource> CompleteWorldEventAction<Self, EventSource> create(
            WorldEventAction<? super Self, ? super EventSource> action,
            UndoAction undo) {
        ExceptionHelper.checkNotNullArgument(action, "action");
        ExceptionHelper.checkNotNullArgument(undo, "undo");

        return new CompleteWorldEventAction<Self, EventSource>() {
            @Override
            public UndoAction alterWorld(World world, Self self, EventSource eventSource) {
                return action.alterWorld(world, self, eventSource);
            }

            @Override
            public void undo() {
                undo.undo();
            }
        };
    }

    public static <Self extends PlayerProperty, EventSource> CompleteWorldEventAction<Self, EventSource> doNothing(UndoAction undo) {
        return create(WorldEventAction.DO_NOTHING, undo);
    }

    public static <Self extends PlayerProperty, EventSource> CompleteWorldEventAction<Self, EventSource> nothingToUndo(
            WorldEventAction<? super Self, ? super EventSource> action) {
        return create(action, UndoAction.DO_NOTHING);
    }
}
