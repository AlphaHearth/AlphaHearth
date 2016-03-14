package info.hearthsim.brazier.events;

import info.hearthsim.brazier.actions.WorldObjectAction;
import info.hearthsim.brazier.Priorities;
import info.hearthsim.brazier.actions.undo.UndoableUnregisterAction;

import java.util.function.Predicate;

public interface WorldActionEventsRegistry<T> {
    /**
     * Adds the given action to the {@code WorldActionEventsRegistry} with normal priority.
     * @param action the given action.
     *
     * @see Priorities#NORMAL_PRIORITY
     */
    public default UndoableUnregisterAction addAction(WorldObjectAction<T> action) {
        return addAction(Priorities.NORMAL_PRIORITY, action);
    }

    public default UndoableUnregisterAction addAction(
            int priority,
            WorldObjectAction<T> action) {
        return addAction(priority, (arg) -> true, action);
    }

    public UndoableUnregisterAction addAction(
            int priority,
            Predicate<? super T> condition,
            WorldObjectAction<? super T> action);
}
