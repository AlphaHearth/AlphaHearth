package info.hearthsim.brazier.util;

import org.jtrim.utils.ExceptionHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

@FunctionalInterface
public interface UndoAction <T> {
    public static final UndoAction DO_NOTHING = (obj) -> { };

    public default void undo(T obj) {
        if (obj != null)
            undoUnsafe(obj);
    }

    public void undoUnsafe(T obj);

    public static <Root, Field> UndoAction<Root> of(Root root,
                                                    Function<Root, Field> fieldGetter,
                                                    Function<Field, UndoAction<Field>> fieldModifier) {
        Field field = fieldGetter.apply(root);
        UndoAction<Field> undoRef = fieldModifier.apply(field);
        return (r) -> undoRef.undo(fieldGetter.apply(r));
    }

    public static <T> UndoAction<T> toIdempotent(UndoAction<? super T> action) {
        ExceptionHelper.checkNotNullArgument(action, "action");

        AtomicReference<UndoAction<? super T>> actionRef = new AtomicReference<>(action);
        return (obj) -> {
            UndoAction<? super T> currentAction = actionRef.getAndSet(null);
            if (currentAction != null) {
                currentAction.undo(obj);
            }
        };
    }

    public final class Builder<T> implements UndoAction<T> {
        private final List<UndoAction<? super T>> wrapped;

        public Builder() {
            this(5);
        }

        public Builder(int expectSize) {
            wrapped = new ArrayList<>(expectSize);
        }

        public void add(UndoAction<? super T> action) {
            wrapped.add(action);
        }

        public void undoUnsafe(T obj) {
            for (UndoAction<? super T> action : wrapped)
                action.undo(obj);
        }
    }
}
