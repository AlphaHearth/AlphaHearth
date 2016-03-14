package info.hearthsim.brazier.events;

import info.hearthsim.brazier.World;

import java.util.ArrayList;
import java.util.List;

import info.hearthsim.brazier.actions.undo.UndoAction;
import info.hearthsim.brazier.actions.undo.UndoableAction;
import info.hearthsim.brazier.actions.undo.UndoableResult;
import info.hearthsim.brazier.actions.undo.UndoableUnregisterAction;
import org.jtrim.collections.RefLinkedList;
import org.jtrim.collections.RefList;
import org.jtrim.utils.ExceptionHelper;

public final class DefaultCompletableWorldActionEvents <T>
    implements CompletableWorldActionEvents<T> {

    private final World world;
    private final RefList<ActionWrapper<? super T>> actions;

    public DefaultCompletableWorldActionEvents(World world) {
        ExceptionHelper.checkNotNullArgument(world, "world");
        this.world = world;
        this.actions = new RefLinkedList<>();
    }

    private static <T> int getPriority(RefList.ElementRef<ActionWrapper<? super T>> ref) {
        return ref.getElement().priority;
    }

    private RefList.ElementRef<?> insert(ActionWrapper<? super T> listener) {
        int priority = listener.priority;
        RefList.ElementRef<ActionWrapper<? super T>> previousRef = actions.getLastReference();
        while (previousRef != null && getPriority(previousRef) < priority) {
            previousRef = previousRef.getPrevious(1);
        }

        return previousRef != null
            ? previousRef.addAfter(listener)
            : actions.addFirstGetReference(listener);
    }

    @Override
    public UndoableUnregisterAction addAction(int priority, CompletableWorldObjectAction<? super T> action) {
        ExceptionHelper.checkNotNullArgument(action, "action");

        ActionWrapper<? super T> wrappedAction = new ActionWrapper<>(priority, action);

        RefList.ElementRef<?> actionRef = insert(wrappedAction);
        Ref<RefList.ElementRef<?>> actionRefRef = new Ref<>(actionRef);
        return new UndoableUnregisterAction() {
            @Override
            public UndoAction unregister() {
                if (actionRefRef.obj.isRemoved()) {
                    return UndoAction.DO_NOTHING;
                }

                int index = actionRefRef.obj.getIndex();
                actionRefRef.obj.remove();

                return () -> actionRefRef.obj = actions.addGetReference(index, wrappedAction);
            }

            @Override
            public void undo() {
                actionRefRef.obj.remove();
            }
        };
    }

    private UndoableAction combineCompleteActions(
        T object,
        List<CompleteWorldObjectAction<? super T>> actions) {
        return () -> {
            UndoAction.Builder builder = new UndoAction.Builder(actions.size());
            for (CompleteWorldObjectAction<? super T> action : actions) {
                builder.addUndo(action.alterWorld(world, object));
            }
            return builder;
        };
    }

    @Override
    public UndoableResult<UndoableAction> triggerEvent(boolean delayable, T object) {
        if (actions.isEmpty()) {
            return new UndoableResult<>(() -> UndoAction.DO_NOTHING);
        }

        List<CompletableWorldObjectAction<? super T>> currentActions = new ArrayList<>(actions);
        List<CompleteWorldObjectAction<? super T>> result = new ArrayList<>(currentActions.size());

        UndoAction.Builder undoBuilder = new UndoAction.Builder(currentActions.size());
        for (CompletableWorldObjectAction<? super T> action : currentActions) {
            CompleteWorldObjectAction<? super T> completeAction = action.startAlterWorld(world, object);
            undoBuilder.addUndo(completeAction);
            result.add(completeAction);
        }

        return new UndoableResult<>(combineCompleteActions(object, result), undoBuilder);
    }

    @Override
    public UndoableResult<UndoableAction> triggerEvent(T object) {
        return triggerEvent(true, object);
    }

    private static final class ActionWrapper <T> implements CompletableWorldObjectAction<T> {
        private final int priority;
        private final CompletableWorldObjectAction<T> wrapped;

        public ActionWrapper(int priority, CompletableWorldObjectAction<T> wrapped) {
            ExceptionHelper.checkNotNullArgument(wrapped, "wrapped");
            this.priority = priority;
            this.wrapped = wrapped;
        }

        @Override
        public CompleteWorldObjectAction<T> startAlterWorld(World world, T object) {
            return wrapped.startAlterWorld(world, object);
        }
    }

    private static final class Ref <T> {
        public T obj;

        public Ref(T obj) {
            this.obj = obj;
        }
    }
}
