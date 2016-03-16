package info.hearthsim.brazier.events;

import info.hearthsim.brazier.actions.undo.UndoAction;

public interface GameActionEvents <T> extends GameActionEventsRegistry<T> {
    /**
     * Triggers event actions for the given {@code object}.
     *
     * @param object the given {@code object}. Only actions that are applicable for the {@code object} will be executed.
     */
    public default UndoAction triggerEvent(T object) {
        return triggerEvent(true, object);
    }

    /**
     * Triggers event actions for the given {@code object}.
     *
     * @param delayable if the execution of the actions can be delayed.
     * @param object the given {@code object}. Only actions that are applicable for the {@code object} will be executed.
     */
    public UndoAction triggerEvent(boolean delayable, T object);
}
