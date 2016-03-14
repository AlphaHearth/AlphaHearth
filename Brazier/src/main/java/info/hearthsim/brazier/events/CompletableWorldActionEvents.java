package info.hearthsim.brazier.events;

import info.hearthsim.brazier.actions.undo.UndoableResult;
import info.hearthsim.brazier.actions.undo.UndoableAction;

public interface CompletableWorldActionEvents<T> extends CompletableWorldActionEventsRegistry<T> {
    public default UndoableResult<UndoableAction> triggerEvent(T object) {
        return triggerEvent(true, object);
    }

    public UndoableResult<UndoableAction> triggerEvent(boolean delayable, T object);
}
