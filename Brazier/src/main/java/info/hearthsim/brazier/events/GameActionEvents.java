package info.hearthsim.brazier.events;

import info.hearthsim.brazier.Game;
import info.hearthsim.brazier.GameProperty;
import info.hearthsim.brazier.Priorities;
import info.hearthsim.brazier.actions.GameActionList;
import info.hearthsim.brazier.actions.GameObjectAction;
import info.hearthsim.brazier.actions.undo.UndoObjectAction;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

public final class GameActionEvents <T extends GameProperty> {
    private final boolean greedyEvent;
    private final GameActionList<T> actionList;
    private final AtomicReference<GameActionList<Game>> pauseCollectorRef;

    GameActionEvents(boolean greedyEvent, GameActionList<T> actionList,
                     AtomicReference<GameActionList<Game>> pauseCollectorRef) {
        this.greedyEvent = greedyEvent;
        this.actionList = actionList;
        this.pauseCollectorRef = pauseCollectorRef;
    }

    /**
     * Adds the given action to the {@code GameActionEventsRegistry} with normal priority.
     *
     * @param action the given action.
     * @see Priorities#NORMAL_PRIORITY
     */
    public UndoObjectAction<GameActionEvents> register(GameObjectAction<T> action) {
        return register(Priorities.NORMAL_PRIORITY, action);
    }

    /**
     * Adds the given action to the {@code GameActionEventsRegistry} with no condition.
     *
     * @param action the given action.
     * @see Priorities#NORMAL_PRIORITY
     */
    public UndoObjectAction<GameActionEvents> register(
        int priority,
        GameObjectAction<T> action) {
        return register(priority, (arg) -> true, action);
    }

    public UndoObjectAction<GameActionEvents> register(
        int priority,
        Predicate<? super T> condition,
        GameObjectAction<? super T> action) {
        UndoObjectAction<GameActionList> undoRef = actionList.addAction(priority, condition, action);
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
