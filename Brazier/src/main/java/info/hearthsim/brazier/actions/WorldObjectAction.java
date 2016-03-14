package info.hearthsim.brazier.actions;

import info.hearthsim.brazier.World;
import info.hearthsim.brazier.actions.undo.UndoAction;
import org.jtrim.utils.ExceptionHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Actions that can be used to alter a given {@code World} and a given object. Usually used
 * as a functional interface with its sole un-implemented method {@link #alterWorld(World, Object)}.
 */
public interface WorldObjectAction <T> {
    // TODO check what the 2nd parameter stands for
    public UndoAction alterWorld(World world, T object);

    public default WorldAction toWorldAction(T object) {
        return (world) -> alterWorld(world, object);
    }

    /**
     * Executes the given collection of {@code WorldObjectAction} and returns the corresponding
     * {@code WorldObjectAction} which can be used to undo.
     *
     * @param actions the collection of {@code WorldObjectAction}
     * @return {@code WorldObjectAction} which can undo the given actions.
     */
    public static <T> WorldObjectAction<T> merge(Collection<? extends WorldObjectAction<T>> actions) {
        List<WorldObjectAction<T>> actionsCopy = new ArrayList<>(actions);
        ExceptionHelper.checkNotNullElements(actionsCopy, "actions");

        int count = actionsCopy.size();
        if (count == 0) {
            return (world, object) -> UndoAction.DO_NOTHING;
        }
        if (count == 1) {
            return actionsCopy.get(0);
        }

        return (World world, T self) -> {
            UndoAction.Builder result = new UndoAction.Builder(actionsCopy.size());
            for (WorldObjectAction<T> action : actionsCopy) {
                result.addUndo(action.alterWorld(world, self));
            }
            return result;
        };
    }
}
