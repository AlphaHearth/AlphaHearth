package info.hearthsim.brazier.events;

import info.hearthsim.brazier.Priorities;
import info.hearthsim.brazier.actions.GameAction;
import info.hearthsim.brazier.actions.GameObjectAction;
import info.hearthsim.brazier.game.Game;
import info.hearthsim.brazier.game.GameProperty;
import info.hearthsim.brazier.util.UndoAction;
import org.jtrim.collections.RefLinkedList;
import org.jtrim.collections.RefList;
import org.jtrim.utils.ExceptionHelper;

import java.util.*;
import java.util.function.Predicate;

/**
 * A {@code GameActionList} is essentially a weighted sequence of {@link GameObjectAction}. Every action element
 * is added to the list via {@link #addAction(GameObjectAction)} method, where its priority and
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
public final class GameActionList <T extends GameProperty> {
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
        for (ActionWrapper<T> action : actions)
            if (action.toCopy)
                result.actions.add(action);
        return result;
    }

    public UndoAction<GameActionList> addAction(GameObjectAction<T> action) {
        return addAction(action, (arg) -> true, Priorities.NORMAL_PRIORITY, false);
    }

    /**
     * Adds the given {@link GameObjectAction} to the list with normal priority and no specific condition.
     *
     * @see Priorities#NORMAL_PRIORITY
     */
    public UndoAction<GameActionList> addAction(GameObjectAction<T> action, boolean toCopy) {
        return addAction(action, (arg) -> true, Priorities.NORMAL_PRIORITY, toCopy);
    }

    /**
     * Returns the priority of the given {@link RefList.ElementRef} of {@link ActionWrapper}.
     */
    private static <T> int getPriority(RefList.ElementRef<ActionWrapper<T>> ref) {
        return ref.getElement().priority;
    }

    /**
     * Inserts the given {@link ActionWrapper} to the list according to its priority.
     */
    private int insert(ActionWrapper<T> action) {
        int priority = action.priority;
        RefList.ElementRef<ActionWrapper<T>> previousRef = actions.getLastReference();
        while (previousRef != null && getPriority(previousRef) < priority) {
            previousRef = previousRef.getPrevious(1);
        }

        if (previousRef != null)
            return previousRef.addAfter(action).getIndex();
        return actions.addFirstGetReference(action).getIndex();
    }

    /**
     * Adds the given {@link GameObjectAction} to the list with given priority and trigger condition.
     *
     * @param action the given {@code GameObjectAction}.
     * @param condition the given trigger condition.
     * @param priority the given priority.
     * @param toCopy if this action should be copied when this {@code GameActionList} is copied.
     *
     * @throws NullPointerException if the given {@code GameObjectAction} is {@code null}.
     */
    public UndoAction<GameActionList> addAction(GameObjectAction<? super T> action,
                                                      Predicate<? super T> condition,
                                                      int priority,
                                                      boolean toCopy) {
        ExceptionHelper.checkNotNullArgument(action, "action");

        ActionWrapper<T> wrappedAction = new ActionWrapper<>(action, condition, priority, toCopy);
        insert(wrappedAction);

        return (gal) -> gal.actions.remove(wrappedAction);
    }

    /**
     * Executes the {@link GameAction}s in this list with the given {@code object}.
     *
     * @param object the given {@code object}.
     * @param greedy whether to execute these actions greedily.
     */
    public void executeActionsNow(T object, boolean greedy) {
        if (actions.isEmpty())
            return;

        if (greedy) {
            executeActionsNowGreedily(object);
        }
        else {
            // We have to first check if the action conditions are met, otherwise
            // two Hobgoblin would be the same as a single hobgoblin (because the first buff
            // would prevent the second to trigger).
            List<GameObjectAction<? super T>> applicableActions = getApplicableActions(object);
            executeActionsNow(object, applicableActions);
        }
    }

    /**
     * Executes the applicable actions in this list with the given {@link Game} and {@code object} greedily.
     */
    private void executeActionsNowGreedily(T object) {
        List<ActionWrapper<T>> remainingAll = new LinkedList<>(actions);
        List<ActionWrapper<T>> remainingQueue = new ArrayList<>(actions.size());
        List<ActionWrapper<T>> skippedActions = new LinkedList<>();
        List<GameObjectAction<? super T>> toExecute = new ArrayList<>();

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

                for (GameObjectAction<? super T> action: toExecute)
                    action.apply(object);

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
    }

    /**
     * Executes the given collection of {@code GameObjectAction}s with the given {@code Game} instance
     * and {@code object}, and returns the undo actions corresponding to these {@code GameObjectAction}s.
     *
     * @param object object.
     * @param actions collection of {@code GameObjectAction}s, which will be executed with the given
     *                {@code Game} instance and {@code object}.
     * @param <T> the type param of {@code object}.
     */
    public static <T> void executeActionsNow(
        T object,
        Collection<? extends GameObjectAction<? super T>> actions) {

        if (actions.isEmpty())
            return;

        for (GameObjectAction<? super T> action: actions)
            action.apply(object);
    }

    /**
     * Returns a {@link GameAction} which can be used to execute the application actions in this list for the given
     * object in some future time point.
     */
    public GameAction snapshotCurrentEvents(T object) {
        List<GameObjectAction<? super T>> snapshot = getApplicableActions(object);
        if (snapshot.isEmpty()) {
            return GameAction.DO_NOTHING;
        }

        return (game) -> executeActionsNow(object, snapshot);
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
        private final GameObjectAction<? super T> wrapped;
        private final Predicate<? super T> condition;
        private final int priority;
        private final boolean toCopy;

        /**
         * Creates a {@code ActionWrapper} with the given {@code priority}, {@code condition} and wrapping
         * {@link GameObjectAction}.
         */
        public ActionWrapper(GameObjectAction<? super T> wrapped, Predicate<? super T> condition,
                             int priority, boolean toCopy) {
            ExceptionHelper.checkNotNullArgument(condition, "condition");
            ExceptionHelper.checkNotNullArgument(wrapped, "wrapped");

            this.wrapped = wrapped;
            this.condition = condition;
            this.priority = priority;
            this.toCopy = toCopy;
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
