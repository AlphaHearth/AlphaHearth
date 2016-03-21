package info.hearthsim.brazier.actions.undo;

import org.jtrim.utils.ExceptionHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

@FunctionalInterface
public interface UndoObjectAction<T> {
    public static final UndoObjectAction DO_NOTHING = (obj) -> { };

    public default void undo(T obj) {
        if (obj != null)
            undoUnsafe(obj);
    }

    public void undoUnsafe(T obj);

    public static <Root, Field> UndoObjectAction<Root> of(Root root,
                                                         Function<Root, Field> fieldGetter,
                                                         Function<Field, UndoObjectAction<Field>> fieldModifier) {
        Field field = fieldGetter.apply(root);
        UndoObjectAction<Field> undoRef = fieldModifier.apply(field);
        return (r) -> undoRef.undo(fieldGetter.apply(r));
    }

    public static <T> UndoObjectAction<T> toIdempotent(UndoObjectAction<? super T> action) {
        ExceptionHelper.checkNotNullArgument(action, "action");

        AtomicReference<UndoObjectAction<? super T>> actionRef = new AtomicReference<>(action);
        return (obj) -> {
            UndoObjectAction<? super T> currentAction = actionRef.getAndSet(null);
            if (currentAction != null) {
                currentAction.undo(obj);
            }
        };
    }

    public final class Builder<T> implements UndoObjectAction<T> {
        private final List<UndoObjectAction<? super T>> wrapped;

        public Builder() {
            this(5);
        }

        public Builder(int expectSize) {
            wrapped = new ArrayList<>(expectSize);
        }

        public void add(UndoObjectAction<? super T> action) {
            wrapped.add(action);
        }

        public void undoUnsafe(T obj) {
            for (UndoObjectAction<? super T> action : wrapped)
                action.undo(obj);
        }
    }
}
