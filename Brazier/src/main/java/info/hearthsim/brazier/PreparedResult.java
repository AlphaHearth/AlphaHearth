package info.hearthsim.brazier;

import info.hearthsim.brazier.actions.undo.UndoAction;
import info.hearthsim.brazier.actions.undo.UndoableAction;
import org.jtrim.utils.ExceptionHelper;

/**
 * Util class with fields {@code result} and {@code activateAction}, which are added when an instance of
 * {@code PreparedResult} is created. Methods {@link #activate()} and {@link #getResult()} can be used
 * to fetch or execute the underlying fields.
 */
// TODO check its common usage
public final class PreparedResult<T> {
    private final T result;
    private final UndoableAction activateAction;

    /**
     * Creates a {@code PreparedResult} with the given result and activate action.
     *
     * @param result the given result.
     * @param activateAction the given activate action.
     */
    public PreparedResult(T result, UndoableAction activateAction) {
        ExceptionHelper.checkNotNullArgument(result, "result");
        ExceptionHelper.checkNotNullArgument(activateAction, "activateAction");

        this.result = result;
        this.activateAction = activateAction;
    }

    /**
     * Activates the {@code PreparedResult} by executing the registered action.
     */
    public UndoAction activate() {
        return activateAction.doAction();
    }

    /**
     * Returns the {@code result}.
     */
    public T getResult() {
        return result;
    }
}
