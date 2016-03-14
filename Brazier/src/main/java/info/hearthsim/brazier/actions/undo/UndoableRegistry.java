package info.hearthsim.brazier.actions.undo;

/**
 * Undoable action of registry, usually used as a functional interface with its sole un-implemented method
 * {@link #register()}, which executes the concrete registry logic and returns a respective
 * {@link UndoableUnregisterAction} which can be used to unregister the registered entity.
 *
 * @see UndoableUnregisterAction
 */
public interface UndoableRegistry {
    /**
     * Executes the concrete registry logic, which is normally registering one thing to another, and
     * returns an {@link UndoableUnregisterAction} which can be used to unregister the registered entity.
     */
    public UndoableUnregisterAction register();
}
