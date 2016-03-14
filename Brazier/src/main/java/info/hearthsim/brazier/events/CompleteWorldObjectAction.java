package info.hearthsim.brazier.events;

import info.hearthsim.brazier.actions.WorldObjectAction;
import info.hearthsim.brazier.World;
import info.hearthsim.brazier.actions.undo.UndoAction;
import org.jtrim.utils.ExceptionHelper;

public interface CompleteWorldObjectAction<T> extends WorldObjectAction<T>, UndoAction {

    public static <T> CompleteWorldObjectAction<T> create(
            WorldObjectAction<? super T> action,
            UndoAction undo) {
        ExceptionHelper.checkNotNullArgument(action, "action");
        ExceptionHelper.checkNotNullArgument(undo, "undo");

        return new CompleteWorldObjectAction<T>() {
            @Override
            public UndoAction alterWorld(World world, T object) {
                return action.alterWorld(world, object);
            }

            @Override
            public void undo() {
                undo.undo();
            }
        };
    }

    public static <T> CompleteWorldObjectAction<T> doNothing(UndoAction undo) {
        return create((world, object) -> UndoAction.DO_NOTHING, undo);
    }

    public static <T> CompleteWorldObjectAction<T> nothingToUndo(WorldObjectAction<? super T> action) {
        return create(action, UndoAction.DO_NOTHING);
    }
}
