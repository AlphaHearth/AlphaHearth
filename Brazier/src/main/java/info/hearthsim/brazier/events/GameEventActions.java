package info.hearthsim.brazier.events;

import info.hearthsim.brazier.Priorities;
import info.hearthsim.brazier.actions.GameObjectAction;
import info.hearthsim.brazier.game.Game;
import info.hearthsim.brazier.game.GameProperty;
import info.hearthsim.brazier.util.UndoAction;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

public final class GameEventActions <T extends GameProperty> {
    private final boolean greedyEvent;
    private final GameActionList<T> actionList;
    private final AtomicReference<GameActionList<Game>> pauseCollectorRef;

    GameEventActions(boolean greedyEvent, GameActionList<T> actionList,
                     AtomicReference<GameActionList<Game>> pauseCollectorRef) {
        this.greedyEvent = greedyEvent;
        this.actionList = actionList;
        this.pauseCollectorRef = pauseCollectorRef;
    }

    public GameEventActions<T> copyFor(AtomicReference<GameActionList<Game>> pauseCollectorRef) {
        return new GameEventActions<>(greedyEvent, actionList.copy(), pauseCollectorRef);
    }

    /**
     * Adds the given action to the {@code GameActionEventsRegistry} with normal priority.
     *
     * @param action the given action.
     * @see Priorities#NORMAL_PRIORITY
     */
    public UndoAction<GameEventActions> register(GameObjectAction<T> action) {
        return register(action, Priorities.NORMAL_PRIORITY);
    }

    public UndoAction<GameEventActions> register(GameObjectAction<T> action, boolean toCopy) {
        return register(action, (arg) -> true, Priorities.NORMAL_PRIORITY, toCopy);
    }

    /**
     * Adds the given action to the {@code GameActionEventsRegistry} with no condition.
     *
     * @param action the given action.
     * @see Priorities#NORMAL_PRIORITY
     */
    public UndoAction<GameEventActions> register(
        GameObjectAction<T> action,
        int priority) {
        return register(action, (arg) -> true, priority);
    }

    public UndoAction<GameEventActions> register(
        GameObjectAction<? super T> action,
        Predicate<? super T> condition,
        int priority) {
        return register(action, condition, priority, false);
    }

    public UndoAction<GameEventActions> register(
        GameObjectAction<? super T> action,
        Predicate<? super T> condition,
        int priority,
        boolean toCopy) {
        UndoAction<GameActionList> undoRef = actionList.addAction(action, condition, priority, toCopy);
        return (gae) -> undoRef.undo(gae.actionList);
    }

    /**
     * Triggers event actions for the given {@code object}.
     *
     * @param object the given {@code object}. Only actions that are applicable for the {@code object} will be executed.
     */
    public void triggerEvent(T object) {
        triggerEvent(true, object);
    }

    /**
     * Triggers event actions for the given {@code object}.
     *
     * @param delayable if the execution of the actions can be delayed.
     * @param object    the given {@code object}. Only actions that are applicable for the {@code object} will be executed.
     */
    public void triggerEvent(boolean delayable, T object) {
        GameActionList<Game> pauseCollector = pauseCollectorRef.get();
        if (pauseCollector != null && delayable) {
            // We do not support greediness for delayable events.
            pauseCollector.addAction(actionList.snapshotCurrentEvents(object));
        } else {
            actionList.executeActionsNow(object, greedyEvent);
        }
    }

    GameActionList<T> getActionList() {
        return actionList;
    }
}
