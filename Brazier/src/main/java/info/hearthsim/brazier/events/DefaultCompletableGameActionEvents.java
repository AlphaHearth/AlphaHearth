package info.hearthsim.brazier.events;

import info.hearthsim.brazier.Game;

import java.util.ArrayList;
import java.util.List;

import info.hearthsim.brazier.actions.undo.UndoAction;
import info.hearthsim.brazier.actions.undo.UndoableAction;
import info.hearthsim.brazier.actions.undo.UndoableResult;
import info.hearthsim.brazier.actions.undo.UndoableUnregisterAction;
import org.jtrim.collections.RefLinkedList;
import org.jtrim.collections.RefList;
import org.jtrim.utils.ExceptionHelper;

public final class DefaultCompletableGameActionEvents <T>
    implements CompletableGameActionEvents<T> {

    private final Game game;
    private final RefList<ActionWrapper<? super T>> actions;

    public DefaultCompletableGameActionEvents(Game game) {
        ExceptionHelper.checkNotNullArgument(game, "game");
        this.game = game;
        this.actions = new RefLinkedList<>();
    }

    /**
     * Returns a copy of this {@code DefaultCompletableGameActionEvents} with the given new {@code Game}.
     */
    public DefaultCompletableGameActionEvents<T> copyFor(Game game) {
        DefaultCompletableGameActionEvents<T> result = new DefaultCompletableGameActionEvents<>(game);
        result.actions.addAll(actions);
        return result;
    }

    private static <T> int getPriority(RefList.ElementRef<ActionWrapper<? super T>> ref) {
        return ref.getElement().priority;
    }

    /**
     * Inserts the given {@link ActionWrapper} to the list according to its priority and
     * updates the given {@code ActionWrapper}'s {@code registerId} field.
     */
    private void insert(ActionWrapper<? super T> listener) {
        int priority = listener.priority;
        RefList.ElementRef<ActionWrapper<? super T>> previousRef = actions.getLastReference();
        while (previousRef != null && getPriority(previousRef) < priority) {
            previousRef = previousRef.getPrevious(1);
        }

        if (previousRef != null)
            previousRef.addAfter(listener);
        else
            actions.addFirstGetReference(listener);

        listener.registerId = new RegisterId();
    }

    @Override
    public RegisterId addAction(int priority, CompletableGameObjectAction<? super T> action) {
        ExceptionHelper.checkNotNullArgument(action, "action");

        ActionWrapper<? super T> wrappedAction = new ActionWrapper<>(priority, action);

        insert(wrappedAction);
        assert wrappedAction.registerId != null;
        return wrappedAction.registerId;
    }

    /**
     * Unregisters the game action with the given {@code RegisterId} from this object.
     *
     * @param registerId the given {@code RegisterId}.
     * @return {@code true} if there is such {@code GameObjectAction}; {@code false} otherwise.
     */
    public boolean unregister(RegisterId registerId) {
        for (int i = 0; i < actions.size(); i++) {
            ActionWrapper action = actions.get(i);
            if (action.registerId.equals(registerId)) {
                actions.remove(i);
                return true;
            }
        }
        return false;
    }

    private UndoableAction combineCompleteActions(
        T object,
        List<CompleteGameObjectAction<? super T>> actions) {
        return () -> {
            UndoAction.Builder builder = new UndoAction.Builder(actions.size());
            for (CompleteGameObjectAction<? super T> action : actions) {
                builder.addUndo(action.alterGame(game, object));
            }
            return builder;
        };
    }

    @Override
    public UndoableResult<UndoableAction> triggerEvent(boolean delayable, T object) {
        if (actions.isEmpty()) {
            return new UndoableResult<>(() -> UndoAction.DO_NOTHING);
        }

        List<CompletableGameObjectAction<? super T>> currentActions = new ArrayList<>(actions);
        List<CompleteGameObjectAction<? super T>> result = new ArrayList<>(currentActions.size());

        UndoAction.Builder undoBuilder = new UndoAction.Builder(currentActions.size());
        for (CompletableGameObjectAction<? super T> action : currentActions) {
            CompleteGameObjectAction<? super T> completeAction = action.startAlterGame(game, object);
            undoBuilder.addUndo(completeAction);
            result.add(completeAction);
        }

        return new UndoableResult<>(combineCompleteActions(object, result), undoBuilder);
    }

    @Override
    public UndoableResult<UndoableAction> triggerEvent(T object) {
        return triggerEvent(true, object);
    }

    private static final class ActionWrapper <T> implements CompletableGameObjectAction<T> {
        private RegisterId registerId;
        private final int priority;
        private final CompletableGameObjectAction<T> wrapped;

        public ActionWrapper(int priority, CompletableGameObjectAction<T> wrapped) {
            ExceptionHelper.checkNotNullArgument(wrapped, "wrapped");
            this.priority = priority;
            this.wrapped = wrapped;
        }

        @Override
        public CompleteGameObjectAction<T> startAlterGame(Game game, T object) {
            return wrapped.startAlterGame(game, object);
        }
    }

    private static final class Ref <T> {
        public T obj;

        public Ref(T obj) {
            this.obj = obj;
        }
    }
}
