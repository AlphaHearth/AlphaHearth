package info.hearthsim.brazier.actions.undo;

import java.util.concurrent.atomic.AtomicBoolean;
import org.jtrim.utils.ExceptionHelper;

/**
 * An action of unregister, which can be undone. In this sense, an {@code UndoableUnregisterAction}
 * is also an {@code UndoableAction}. {@code UndoableUnregisterAction} can also act as an
 * {@code UndoAction} of an {@link UndoableRegistry}, whose {@link #undo()} method can be used to
 * unregister the entity registered in the {@code UndoableRegistry}
 *
 * @see UndoableRegistry
 */
public interface UndoableUnregisterAction extends UndoAction, UndoableAction {
    public static final UndoableUnregisterAction DO_NOTHING = () -> UndoAction.DO_NOTHING;

    /**
     * Removes the registered object, so that it no longer does what it
     * was registered for. This method must be idempotent. That is, calling it
     * multiple times should have no additional effects.
     *
     * @return an {@code UndoAction} which can be used to restore the
     *   registration to its previous state. This method never returns
     *   {@code null}.
     */
    public UndoAction unregister();

    public default UndoAction doAction() {
        return unregister();
    }

    /**
     * Undo the corresponding register action of this {@code UndoableUnregisterAction}.
     * It can be different than simply unregister the registered object.
     *
     * @implNote
     * The default implementation has the same effect as calling {@link #unregister()}.
     *
     * @implSpec
     * Do remember to override this method if undoing the register action is different than unregister.
     */
    @Override
    public default void undo() {
        unregister();
    }

    /**
     * Wraps the given {@code UndoableUnregisterAction} to ensure its {@link #unregister()} method
     * only has effect on its first call and does nothing on the sequential calls.
     *
     * @param wrapped the {@code UndoableUnregisterAction} to be wrapped.
     * @return the wrapped idempotent {@code UndoableUnregisterAction}.
     */
    public static UndoableUnregisterAction makeIdempotent(UndoableUnregisterAction wrapped) {
        ExceptionHelper.checkNotNullArgument(wrapped, "wrapped");
        return new UndoableUnregisterAction() {
            private final AtomicBoolean unregistered = new AtomicBoolean(false);

            @Override
            public UndoAction unregister() {
                if (unregistered.compareAndSet(false, true)) {
                    UndoAction unregisterUndo = wrapped.unregister();
                    return () -> {
                        unregisterUndo.undo();
                        unregistered.set(false);
                    };
                }
                else {
                    return UndoAction.DO_NOTHING;
                }
            }

            @Override
            public void undo() {
                if (unregistered.get()) {
                    throw new IllegalStateException("Cannot be undone after unregistering the reference.");
                }
                wrapped.undo();
            }
        };
    }

    final class Builder implements UndoableUnregisterAction {
        private UndoableUnregisterAction[] refs;
        private int count;

        public Builder() {
            this(10);
        }

        public Builder(int expectedSize) {
            this.refs = new UndoableUnregisterAction[expectedSize];
            this.count = 0;
        }

        public void addRef(UndoableUnregisterAction ref) {
            ExceptionHelper.checkNotNullArgument(ref, "ref");

            if (ref == UndoableUnregisterAction.DO_NOTHING) {
                return;
            }

            if (refs.length >= count) {
                int newLength = Math.max(count + 1, 2 * refs.length);
                UndoableUnregisterAction[] newRefs = new UndoableUnregisterAction[newLength];
                System.arraycopy(refs, 0, newRefs, 0, count);
                refs = newRefs;
            }

            refs[count] = ref;
            count++;
        }

        @Override
        public UndoAction unregister() {
            UndoAction.Builder result = new UndoAction.Builder(count);
            for (int i = 0; i < count; i++) {
                result.addUndo(refs[i].unregister());
            }
            return result;
        }

        @Override
        public void undo() {
            for (int i = count - 1; i >= 0; i--) {
                refs[i].undo();
            }
        }
    }
}
