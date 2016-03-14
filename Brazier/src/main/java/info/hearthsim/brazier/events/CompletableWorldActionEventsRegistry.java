package info.hearthsim.brazier.events;

import info.hearthsim.brazier.Priorities;
import info.hearthsim.brazier.actions.undo.UndoableUnregisterAction;

public interface CompletableWorldActionEventsRegistry<T> {
    public default UndoableUnregisterAction addAction(CompletableWorldObjectAction<? super T> action) {
        return addAction(Priorities.NORMAL_PRIORITY, action);
    }

    public UndoableUnregisterAction addAction(int priority, CompletableWorldObjectAction<? super T> action);
}
