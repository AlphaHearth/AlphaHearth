package info.hearthsim.brazier.events;

import info.hearthsim.brazier.actions.GameObjectAction;
import info.hearthsim.brazier.Priorities;
import info.hearthsim.brazier.actions.undo.UndoableUnregisterAction;

import java.util.function.Predicate;

public interface GameActionEventsRegistry <T> {
    /**
     * Adds the given action to the {@code GameActionEventsRegistry} with normal priority.
     * @param action the given action.
     *
     * @see Priorities#NORMAL_PRIORITY
     */
    public default UndoableUnregisterAction addAction(GameObjectAction<T> action) {
        return addAction(Priorities.NORMAL_PRIORITY, action);
    }

    public default UndoableUnregisterAction addAction(
            int priority,
            GameObjectAction<T> action) {
        return addAction(priority, (arg) -> true, action);
    }

    public UndoableUnregisterAction addAction(
            int priority,
            Predicate<? super T> condition,
            GameObjectAction<? super T> action);
}
