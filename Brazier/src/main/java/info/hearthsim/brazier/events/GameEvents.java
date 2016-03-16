package info.hearthsim.brazier.events;

import info.hearthsim.brazier.actions.GameActionList;
import info.hearthsim.brazier.actions.GameObjectAction;
import info.hearthsim.brazier.actions.undo.UndoableUnregisterAction;
import info.hearthsim.brazier.minions.Minion;
import info.hearthsim.brazier.Player;
import info.hearthsim.brazier.actions.undo.UndoableResult;
import info.hearthsim.brazier.Game;
import info.hearthsim.brazier.actions.undo.UndoAction;
import info.hearthsim.brazier.actions.undo.UndoableAction;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import org.jtrim.utils.ExceptionHelper;

public final class GameEvents {
    private final Game game;

    private final Map<SimpleEventType, GameActionEvents<?>> simpleListeners;
    private final CompletableGameActionEvents<Minion> summoningListeners;

    private final AtomicReference<GameActionList<Void>> pauseCollectorRef;

    // These listeners containers are just convenience methods to access
    // summoningListeners
    private final GameActionEventsRegistry<Minion> startSummoningListeners;
    private final GameActionEventsRegistry<Minion> doneSummoningListeners;

    public GameEvents(Game game) {
        ExceptionHelper.checkNotNullArgument(game, "game");

        this.game = game;
        this.pauseCollectorRef = new AtomicReference<>(null);

        this.simpleListeners = new EnumMap<>(SimpleEventType.class);
        this.summoningListeners = createCompletableGameActionEvents();

        this.startSummoningListeners = (int priority, Predicate<? super Minion> condition, GameObjectAction<? super Minion> action) -> {
            return summoningListeners.addAction(priority, (Game eventGame, Minion minion) -> {
                if (!condition.test(minion)) {
                    return CompleteGameObjectAction.doNothing(UndoAction.DO_NOTHING);
                }

                UndoAction actionUndo = action.alterGame(game, minion);
                return CompleteGameObjectAction.doNothing(actionUndo);
            });
        };
        this.doneSummoningListeners = (int priority, Predicate<? super Minion> condition, GameObjectAction<? super Minion> action) -> {
            return summoningListeners.addAction(priority, (Game eventGame, Minion minion) -> {
                if (!condition.test(minion)) {
                    return CompleteGameObjectAction.doNothing(UndoAction.DO_NOTHING);
                }
                return CompleteGameObjectAction.nothingToUndo(action);
            });
        };
    }

    private GameEvents(Game game, GameEvents other) {
        ExceptionHelper.checkNotNullArgument(game, "game");
        ExceptionHelper.checkNotNullArgument(other, "other");

        this.game = game;
        this.pauseCollectorRef = new AtomicReference<>(other.pauseCollectorRef.get().copy());

        this.simpleListeners = new EnumMap<>(SimpleEventType.class);
        this.summoningListeners = createCompletableGameActionEvents();

        this.startSummoningListeners = other.startSummoningListeners;
        this.doneSummoningListeners = other.doneSummoningListeners;
    }

    /**
     * Returns a copy of this {@code GameEvents} for the given new {@code Game}.
     */
    public GameEvents copyFor(Game game) {
        return new GameEvents(game, this);
    }

    /**
     * Returns simple listener registered in this {@code GameEvents} which listens to the given type of event;
     * {@code null} if no such listener exists.
     */
    private <T> GameActionEvents<T> tryGetSimpleListeners(SimpleEventType eventType) {
        ExceptionHelper.checkNotNullArgument(eventType, "eventType");

        @SuppressWarnings("unchecked")
        GameActionEvents<T> result = (GameActionEvents<T>) simpleListeners.get(eventType);
        return result;
    }

    /**
     * Returns the existed simple listeners with the given {@link SimpleEventType}; if there is no such listener,
     * creates a new one and returns.
     *
     * @param eventType the given {@code SimpleEventType}.
     * @return simple listeners with the given {@code SimpleEventType}.
     * @throws IllegalArgumentException if the given {@code argType} does not match the argument type of the given
     *                                  {@code SimpleEventType}.
     */
    public <T> GameActionEvents<T> simpleListeners(SimpleEventType eventType) {
        GameActionEvents<T> result = tryGetSimpleListeners(eventType);
        if (result == null) {
            result = createEventContainer(eventType);
            simpleListeners.put(eventType, result);
        }

        return result;
    }

    /**
     * Triggers a given type of event with the given argument instantly.
     */
    public <T> UndoAction triggerEventNow(SimpleEventType eventType, T arg) {
        return triggerEvent(eventType, arg, false);
    }

    public <T> UndoAction triggerEvent(SimpleEventType eventType, T arg) {
        return triggerEvent(eventType, arg, true);
    }

    private <T> UndoAction triggerEvent(SimpleEventType eventType, T arg, boolean delayable) {
        Class<?> expectedArgType = eventType.getArgumentType();
        if (!expectedArgType.isInstance(arg)) {
            throw new IllegalArgumentException("The requested listener has a different argument type."
                + " Requested: " + arg.getClass().getName()
                + ". Expected: " + eventType.getArgumentType());
        }

        @SuppressWarnings("unchecked")
        GameActionEvents<T> listeners = tryGetSimpleListeners(eventType);
        if (listeners == null) {
            return UndoAction.DO_NOTHING;
        }
        return listeners.triggerEvent(delayable, arg);
    }

    public CompletableGameActionEvents<Minion> summoningListeners() {
        return summoningListeners;
    }

    public GameActionEventsRegistry<Minion> startSummoningListeners() {
        return startSummoningListeners;
    }

    public GameActionEventsRegistry<Minion> doneSummoningListeners() {
        return doneSummoningListeners;
    }

    public GameActionEvents<Player> turnStartsListeners() {
        return simpleListeners(SimpleEventType.TURN_STARTS);
    }

    public GameActionEvents<Player> turnEndsListeners() {
        return simpleListeners(SimpleEventType.TURN_ENDS);
    }

    /**
     * Executes the given action, ensuring that it won't be interrupted by event notifications
     * scheduled to any of the event listeners of this {@code GameEvents} object. If the
     * given action would be interrupted by an event notification, the event notification
     * is suspended until the specified action returns.
     * <p>
     * Calls to {@code doAtomic} can be nested in which case event notifications will
     * be executed once the outer most atomic action returns.
     * <p>
     * <B>Warning</B>: This method is <B>not</B> thread-safe.
     *
     * @param action the action to be executed. This argument cannot be {@code null}.
     * @return the action which might be used to undo what this call did including the actions
     * done by the caused events. This method may never return {@code null}.
     */
    public UndoAction doAtomic(UndoableAction action) {
        ExceptionHelper.checkNotNullArgument(action, "action");

        if (pauseCollectorRef.get() != null) {
            // A caller already ensures this call to be atomic
            return action.doAction();
        } else {
            GameActionList<Void> currentCollector = new GameActionList<>();
            UndoAction actionUndo;
            try {
                pauseCollectorRef.compareAndSet(null, currentCollector);
                actionUndo = action.doAction();
            } finally {
                pauseCollectorRef.compareAndSet(currentCollector, null);
            }

            UndoAction eventUndo = currentCollector.executeActionsNow(game, null, false);
            return () -> {
                eventUndo.undo();
                actionUndo.undo();
            };
        }
    }

    /**
     * Creates and returns a new {@link GameActionEvents} for the given type of event.
     * It uses an underlying {@link GameActionList} as its implementation.
     */
    private <T> GameActionEvents<T> createEventContainer(SimpleEventType eventType) {
        boolean greedyEvent = eventType.isGreedyEvent();
        GameActionList<T> actionList = new GameActionList<>();

        return new GameActionEvents<T>() {
            @Override
            public UndoableUnregisterAction addAction(
                int priority,
                Predicate<? super T> condition,
                GameObjectAction<? super T> action) {
                return actionList.addAction(priority, condition, action);
            }

            @Override
            public UndoAction triggerEvent(boolean delayable, T object) {
                GameActionList<Void> pauseCollector = pauseCollectorRef.get();
                if (pauseCollector != null && delayable) {
                    // We do not support greediness for delayable events.
                    UndoableUnregisterAction actionRegRef = pauseCollector.addAction(actionList.snapshotCurrentEvents(object));
                    return actionRegRef::undo;
                } else {
                    return actionList.executeActionsNow(game, object, greedyEvent);
                }
            }
        };
    }

    private <T> CompletableGameActionEvents<T> createCompletableGameActionEvents() {
        CompletableGameActionEvents<T> wrapped = new DefaultCompletableGameActionEvents<>(game);

        return new CompletableGameActionEvents<T>() {
            @Override
            public UndoableUnregisterAction addAction(int priority, CompletableGameObjectAction<? super T> action) {
                return wrapped.addAction(priority, action);
            }

            @Override
            public UndoableResult<UndoableAction> triggerEvent(boolean delayable, T object) {
                GameActionList<Void> pauseCollector = pauseCollectorRef.get();
                if (pauseCollector != null && delayable) {
                    // If the returned finalizer has been called, then
                    // we have to call the finalizer immediately after triggering
                    // the event. Otherwise, we set a reference after triggering the
                    // event and then it is the client's responsiblity to notify
                    // the finalizer.

                    AtomicReference<UndoableAction> finalizerRef = new AtomicReference<>(null);

                    GameObjectAction<Void> delayedAction = (Game actionGame, Void ignored) -> {
                        UndoableResult<UndoableAction> triggerResult = wrapped.triggerEvent(object);

                        UndoAction finalizeUndo;
                        UndoableAction finalizer = triggerResult.getResult();
                        if (!finalizerRef.compareAndSet(null, finalizer)) {
                            finalizeUndo = finalizer.doAction();
                        } else {
                            finalizeUndo = UndoAction.DO_NOTHING;
                        }

                        return () -> {
                            finalizeUndo.undo();
                            triggerResult.undo();
                        };
                    };

                    UndoableAction finalizer = () -> {
                        UndoableAction currentFinalizer = finalizerRef.getAndSet(() -> UndoAction.DO_NOTHING);
                        return currentFinalizer != null
                            ? currentFinalizer.doAction()
                            : UndoAction.DO_NOTHING;
                    };

                    UndoableUnregisterAction actionRegRef = pauseCollector.addAction(delayedAction);
                    return new UndoableResult<>(finalizer, actionRegRef);
                } else {
                    return wrapped.triggerEvent(object);
                }
            }
        };
    }
}
