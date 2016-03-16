package info.hearthsim.brazier.events;

import info.hearthsim.brazier.Priorities;
import info.hearthsim.brazier.actions.undo.UndoableUnregisterAction;

public interface CompletableGameActionEventsRegistry <T> {
    public default UndoableUnregisterAction addAction(CompletableGameObjectAction<? super T> action) {
        return addAction(Priorities.NORMAL_PRIORITY, action);
    }

    public UndoableUnregisterAction addAction(int priority, CompletableGameObjectAction<? super T> action);
}
