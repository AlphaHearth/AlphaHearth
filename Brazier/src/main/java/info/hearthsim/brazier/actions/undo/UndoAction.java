package info.hearthsim.brazier.actions.undo;

import java.util.concurrent.atomic.AtomicReference;

import org.jtrim.utils.ExceptionHelper;

/**
 * The partner class of {@link UndoableAction}, which can be used to undo the effect of a
 * {@code UndoableAction}. It is usually used as a functional interface with its sole un-implemented method
 * {@link #undo()}.
 *
 * @see UndoAction
 */
public interface UndoAction {
    public static final UndoAction DO_NOTHING = () -> { };

    /**
     * The concrete actions of this {@code UndoAction}, which can undo the corresponding
     * {@link UndoableAction} and is usually designated by lambda expression.
     */
    public void undo();

    /**
     * Converts the given {@link UndoAction} to an idempotent {@code UndoAction}, which
     * will only executes once.
     */
    public static UndoAction toIdempotent(UndoAction action) {
        ExceptionHelper.checkNotNullArgument(action, "action");

        AtomicReference<UndoAction> actionRef = new AtomicReference<>(action);
        return () -> {
            UndoAction currentAction = actionRef.getAndSet(null);
            if (currentAction != null) {
                currentAction.undo();
            }
        };
    }

    /**
     * Util class for building an {@link UndoAction} with an designated ordered
     * sequence of {@link UndoAction}s.
     */
    final class Builder implements UndoAction {
        private UndoAction[] undos;
        private int count;

        public Builder() {
            this(10);
        }

        public Builder(int expectedSize) {
            this.undos = new UndoAction[expectedSize];
            this.count = 0;
        }

        public void addUndo(UndoAction undo) {
            ExceptionHelper.checkNotNullArgument(undo, "undo");

            if (undo == UndoAction.DO_NOTHING) {
                // Minor optimization
                return;
            }

            if (undos.length >= count) {
                int newLength = Math.max(count + 1, 2 * count);
                UndoAction[] newUndos = new UndoAction[newLength];
                System.arraycopy(undos, 0, newUndos, 0, count);
                undos = newUndos;
            }

            undos[count] = undo;
            count++;
        }

        @Override
        public void undo() {
            for (int i = count - 1; i >= 0; i--) {
                undos[i].undo();
            }
        }
    }
}
