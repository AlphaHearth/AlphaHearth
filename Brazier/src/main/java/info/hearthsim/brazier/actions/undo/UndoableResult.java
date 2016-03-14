package info.hearthsim.brazier.actions.undo;

import org.jtrim.utils.ExceptionHelper;

/**
 * An extension and implementation of {@link UndoAction}, which contains the result
 * of an {@link UndoableAction} and its corresponding {@link UndoAction}. An {@code UndoableResult}
 * can be used as a {@code UndoAction} to undo {@link UndoableAction}.
 * <p>
 * While {@code UndoAction} and {@code UndoableResult} can be used as return types of undoable methods, an
 * {@code UndoAction} can be considered as an {@code UndoableResult<Void>}.
 *
 * @see UndoableAction
 * @see UndoAction
 */
public final class UndoableResult<T> implements UndoAction {
    private final T result;
    private final UndoAction undoAction;

    public UndoableResult(T result) {
        this(result, UndoAction.DO_NOTHING);
    }

    public UndoableResult(T result, UndoAction undoAction) {
        ExceptionHelper.checkNotNullArgument(undoAction, "undoAction");

        this.result = result;
        this.undoAction = undoAction;
    }

    public T getResult() {
        return result;
    }

    public UndoAction getUndoAction() {
        return undoAction;
    }

    @Override
    public void undo() {
        undoAction.undo();
    }
}
