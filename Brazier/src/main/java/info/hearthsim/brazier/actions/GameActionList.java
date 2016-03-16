package info.hearthsim.brazier.actions;

import info.hearthsim.brazier.Game;
import info.hearthsim.brazier.Priorities;
import info.hearthsim.brazier.actions.undo.UndoableUnregisterAction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import info.hearthsim.brazier.actions.undo.UndoAction;
import org.jtrim.collections.RefLinkedList;
import org.jtrim.collections.RefList;
import org.jtrim.utils.ExceptionHelper;

/**
 * A {@code GameActionList} is essentially a weighted sequence of {@link GameObjectAction}. Every action element
 * is added to the list via {@link #addAction(int, Predicate, GameObjectAction)} method, where its priority and
 * trigger condition will be designated. The list will organised the added actions in a decreasing order of their
 * priorities. For actions with the same priority, they will be organised based on the sequence they are added.
 * <p>
 * Formally, the indices of element actions in this list follows these patterns:
 * <ul>
 *     <li>
 *         If action {@code a} has a bigger priority than action {@code b}, the index of {@code a} will be smaller
 *         than {@code b}. ({@code a} stands before {@code b}).
 *     </li>
 *     <li>
 *         If action {@code a} and {@code b} has the same priority, but {@code a} is added before {@code b},
 *         {@code a} will have a smaller index.
 *     </li>
 * </ul>
 */
public final class GameActionList <T> {
    private final RefList<ActionWrapper<T>> actions;

    /**
     * Creates an empty {@code GameActionList}.
     */
    public GameActionList() {
        this.actions = new RefLinkedList<>();
    }

    /**
     * Returns a copy of this {@code GameActionList}.
     */
    public GameActionList<T> copy() {
        GameActionList<T> result = new GameActionList<>();
        result.actions.addAll(actions);
        return result;
    }

    /**
     * Adds the given {@link GameObjectAction} to the list with normal priority and no specific condition.
     *
     * @see Priorities#NORMAL_PRIORITY
     */
    public UndoableUnregisterAction addAction(GameObjectAction<T> action) {
        return addAction(Priorities.NORMAL_PRIORITY, (arg) -> true, action);
    }

    /**
     * Returns the priority of the given {@link RefList.ElementRef} of {@link ActionWrapper}.
     */
    private static <T> int getPriority(RefList.ElementRef<ActionWrapper<T>> ref) {
        return ref.getElement().priority;
    }

    /**
     * Inserts the given {@link ActionWrapper} to the list according to its priority and
     * returns the newly created {@link RefList.ElementRef} for the {@code ActionWrapper}.
     */
    private RefList.ElementRef<?> insert(ActionWrapper<T> action) {
        int priority = action.priority;
        RefList.ElementRef<ActionWrapper<T>> previousRef = actions.getLastReference();
        while (previousRef != null && getPriority(previousRef) < priority) {
            previousRef = previousRef.getPrevious(1);
        }

        return previousRef != null
                ? previousRef.addAfter(action)
                : actions.addFirstGetReference(action);
    }

    /**
     * Adds the given {@link GameObjectAction} to the list with given priority and trigger condition.
     * @param priority the given priority.
     * @param condition the given trigger condition.
     * @param action the given {@code GameObjectAction}.
     *
     * @throws NullPointerException if the given {@code GameObjectAction} is {@code null}.
     */
    public UndoableUnregisterAction addAction(int priority, Predicate<? super T> condition, GameObjectAction<? super T> action) {
        ExceptionHelper.checkNotNullArgument(action, "action");

        ActionWrapper<T> wrappedAction = new ActionWrapper<>(priority, condition, action);

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

    /**
     * Executes the {@link GameAction}s in this list with the given {@link Game} and {@code object}.
     *
     * @param game the given {@link Game}.
     * @param object the given {@code object}.
     * @param greedy whether to execute these actions greedily.
     */
    public UndoAction executeActionsNow(Game game, T object, boolean greedy) {
        if (actions.isEmpty()) {
            return UndoAction.DO_NOTHING;
        }

        if (greedy) {
            return executeActionsNowGreedily(game, object);
        }
        else {
            // We have to first check if the action conditions are met, otherwise
            // two Hobgoblin would be the same as a single hobgoblin (because the first buff
            // would prevent the second to trigger).
            List<GameObjectAction<? super T>> applicableActions = getApplicableActions(object);
            return executeActionsNow(game, object, applicableActions);
        }
    }

    /**
     * Executes the applicable actions in this list with the given {@link Game} and {@code object} greedily.
     */
    private UndoAction executeActionsNowGreedily(Game game, T object) {
        List<ActionWrapper<T>> remainingAll = new LinkedList<>(actions);
        List<ActionWrapper<T>> remainingQueue = new ArrayList<>(actions.size());
        List<ActionWrapper<T>> skippedActions = new LinkedList<>();
        List<GameObjectAction<? super T>> toExecute = new ArrayList<>();

        UndoAction.Builder undoBuilder = new UndoAction.Builder(remainingAll.size());

        while (!remainingAll.isEmpty()) {
            skippedActions.clear();
            drainMaxPriorityActions(remainingAll, remainingQueue);

            while (!remainingQueue.isEmpty()) {
                toExecute.clear();
                for (ActionWrapper<T> actionRef: remainingQueue) {
                    if (actionRef.isApplicable(object)) {
                        toExecute.add(actionRef.getAction());
                    }
                    else {
                        skippedActions.add(actionRef);
                    }
                }
                remainingQueue.clear();

                for (GameObjectAction<? super T> action: toExecute) {
                    undoBuilder.addUndo(action.alterGame(game, object));
                }

                Iterator<ActionWrapper<T>> skippedActionsItr = skippedActions.iterator();
                while (skippedActionsItr.hasNext()) {
                    ActionWrapper<T> skippedAction = skippedActionsItr.next();
                    // FIXME: isApplicable for the first item will be called again
                    //   needlessly. This - in theory - can cause an infinite loop.
                    //   However, it is reasonable to assume that filters are
                    //   deterministic and stateless. Still it should be fixed.
                    if (skippedAction.isApplicable(object)) {
                        skippedActionsItr.remove();
                        remainingQueue.add(skippedAction);
                    }
                }
            }
        }

        return undoBuilder;
    }

    /**
     * Executes the given collection of {@code GameObjectAction}s with the given {@code Game} instance
     * and {@code object}, and returns the undo actions corresponding to these {@code GameObjectAction}s.
     *
     * @param game the given {@code Game} instance.
     * @param object object.
     * @param actions collection of {@code GameObjectAction}s, which will be executed with the given
     *                {@code Game} instance and {@code object}.
     * @param <T> the type param of {@code object}.
     * @return undo actions corresponding to these {@code GameObjectAction}s.
     */
    public static <T> UndoAction executeActionsNow(
        Game game,
        T object,
        Collection<? extends GameObjectAction<? super T>> actions) {

        if (actions.isEmpty()) {
            return UndoAction.DO_NOTHING;
        }

        UndoAction.Builder result = new UndoAction.Builder();
        for (GameObjectAction<? super T> action: actions) {
            result.addUndo(action.alterGame(game, object));
        }
        return result;
    }

    /**
     * Returns a {@link GameAction} which can be used to execute the application actions in this list for the given
     * object in some future time point.
     */
    public GameAction snapshotCurrentEvents(T object) {
        List<GameObjectAction<? super T>> snapshot = getApplicableActions(object);
        if (snapshot.isEmpty()) {
            return (game) -> UndoAction.DO_NOTHING;
        }

        return (game) -> executeActionsNow(game, object, snapshot);
    }

    /**
     * Drains the {@code ActionWrapper}s with the maximum priority from the source list to the destination list.
     * The method assumes the {@code ActionWrapper}s are organised in a decreasing order of priority in the source code.
     */
    private static <T> void drainMaxPriorityActions(List<ActionWrapper<T>> src, List<ActionWrapper<T>> dest) {
        if (src.isEmpty()) {
            return;
        }

        ActionWrapper<T> first = src.remove(0);
        dest.add(first);
        int priority = first.priority;

        Iterator<ActionWrapper<T>> srcItr = src.iterator();
        while (srcItr.hasNext()) {
            ActionWrapper<T> current = srcItr.next();
            if (current.priority != priority) {
                break;
            }

            srcItr.remove();
            dest.add(current);
        }
    }

    /**
     * Returns {@link GameObjectAction}s in this list that are applicable to the given object.
     */
    private List<GameObjectAction<? super T>> getApplicableActions(T object) {
        if (actions.isEmpty()) {
            return Collections.emptyList();
        }

        List<GameObjectAction<? super T>> result = new ArrayList<>(actions.size());
        for (ActionWrapper<T> action: actions) {
            if (action.isApplicable(object)) {
                result.add(action.getAction());
            }
        }

        return result;
    }

    /**
     * Wrapper for a {@link GameObjectAction}.
     */
    private static final class ActionWrapper<T> {
        private final int priority;
        private final Predicate<? super T> condition;
        private final GameObjectAction<? super T> wrapped;

        /**
         * Creates a {@code ActionWrapper} with the given {@code priority}, {@code condition} and wrapping
         * {@link GameObjectAction}.
         */
        public ActionWrapper(int priority, Predicate<? super T> condition, GameObjectAction<? super T> wrapped) {
            ExceptionHelper.checkNotNullArgument(condition, "condition");
            ExceptionHelper.checkNotNullArgument(wrapped, "wrapped");

            this.priority = priority;
            this.condition = condition;
            this.wrapped = wrapped;
        }

        /**
         * Returns if the given object satisfies the {@code condition} of this {@code ActionWrapper}.
         */
        public boolean isApplicable(T arg) {
            return condition.test(arg);
        }

        public GameObjectAction<? super T> getAction() {
            return wrapped;
        }
    }

    private static final class Ref<T> {
        public T obj;

        public Ref(T obj) {
            this.obj = obj;
        }
    }
}
