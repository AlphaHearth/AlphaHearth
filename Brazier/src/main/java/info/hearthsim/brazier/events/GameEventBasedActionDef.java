package info.hearthsim.brazier.events;

import info.hearthsim.brazier.Game;
import info.hearthsim.brazier.PlayerProperty;
import info.hearthsim.brazier.actions.undo.UndoAction;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;

import info.hearthsim.brazier.actions.undo.UndoableUnregisterAction;
import org.jtrim.utils.ExceptionHelper;

/**
 * The definition of a game-event-based action, which will be triggered when certain game event happens.
 */
public final class GameEventBasedActionDef <Self extends PlayerProperty, T> {
    private final boolean lazyFilter;
    private final boolean triggerOnce;
    private final int priority;
    private final Function<GameEvents, ? extends GameActionEventsRegistry<T>> actionEventListenersGetter;
    private final GameEventFilter<? super Self, ? super T> sourceFilter;
    private final GameEventAction<? super Self, ? super T> eventAction;

    public GameEventBasedActionDef(
        boolean lazyFilter,
        boolean triggerOnce,
        int priority,
        Function<GameEvents, ? extends GameActionEventsRegistry<T>> actionEventListenersGetter,
        GameEventFilter<? super Self, ? super T> condition,
        GameEventAction<? super Self, ? super T> eventAction) {
        ExceptionHelper.checkNotNullArgument(actionEventListenersGetter, "actionEventListenersGetter");
        ExceptionHelper.checkNotNullArgument(condition, "condition");
        ExceptionHelper.checkNotNullArgument(eventAction, "eventAction");

        this.lazyFilter = lazyFilter;
        this.triggerOnce = triggerOnce;
        this.priority = priority;
        this.actionEventListenersGetter = actionEventListenersGetter;
        this.sourceFilter = condition;
        this.eventAction = eventAction;
    }

    public static <Self extends PlayerProperty, T> UndoableUnregisterAction registerAll(
            List<GameEventBasedActionDef<Self, T>> actionDefs,
            GameEvents gameEvents,
            Self self) {
        if (actionDefs.isEmpty()) {
            return UndoableUnregisterAction.DO_NOTHING;
        }

        UndoableUnregisterAction.Builder result = new UndoableUnregisterAction.Builder(actionDefs.size());
        for (GameEventBasedActionDef<Self, T> actionDef: actionDefs) {
            result.addRef(actionDef.registerForEvent(gameEvents, self));
        }
        return result;
    }

    private GameEventAction<Self, T> getActionWithFilter() {
        return (Game game, Self self, T eventSource) -> {
            if (sourceFilter.applies(game, self, eventSource)) {
                return eventAction.alterGame(game, self, eventSource);
            }
            else {
                return UndoAction.DO_NOTHING;
            }
        };
    }

    public CompletableGameEventBasedActionDef<Self, T> toStartEventDef(
            Function<GameEvents, ? extends CompletableGameActionEventsRegistry<T>> actionEventListenersGetter) {
        GameEventAction<Self, T> filteredAction = getActionWithFilter();
        return new CompletableGameEventBasedActionDef<>(triggerOnce, priority, actionEventListenersGetter, (Game game, Self self, T eventSource) -> {
            UndoAction actionUndo = filteredAction.alterGame(game, self, eventSource);
            return CompleteGameEventAction.doNothing(actionUndo);
        });
    }

    public CompletableGameEventBasedActionDef<Self, T> toDoneEventDef(
            Function<GameEvents, ? extends CompletableGameActionEventsRegistry<T>> actionEventListenersGetter) {
        GameEventAction<Self, T> filteredAction = getActionWithFilter();
        return new CompletableGameEventBasedActionDef<>(triggerOnce, priority, actionEventListenersGetter, (Game game, Self self, T eventSource) -> {
            return CompleteGameEventAction.nothingToUndo(filteredAction);
        });
    }

    private UndoableUnregisterAction registerForEvents(
            GameActionEventsRegistry<T> actionEvents,
            Self self,
            GameEventAction<? super Self, ? super T> appliedEventAction) {

        if (lazyFilter) {
            return actionEvents.addAction(priority, (Game game, T object) -> {
                if (sourceFilter.applies(game, self, object)) {
                    return appliedEventAction.alterGame(game, self, object);
                }
                else {
                    return UndoAction.DO_NOTHING;
                }
            });
        }
        else {
            Predicate<T> condition = (T object) -> sourceFilter.applies(self.getGame(), self, object);
            return actionEvents.addAction(priority, condition, (Game game, T object) -> {
                return appliedEventAction.alterGame(game, self, object);
            });
        }
    }

    public UndoableUnregisterAction registerForEvent(GameEvents gameEvents, Self self) {
        GameActionEventsRegistry<T> actionEvents = actionEventListenersGetter.apply(gameEvents);
        if (!triggerOnce) {
            return registerForEvents(actionEvents, self, eventAction);
        }

        AtomicReference<UndoableUnregisterAction> refRef = new AtomicReference<>();
        UndoableUnregisterAction ref = registerForEvents(actionEvents, self, (Game game, Self eventSelf, T eventSource) -> {
            UndoAction unregisterUndo = refRef.get().unregister();
            UndoAction actionUndo = eventAction.alterGame(game, eventSelf, eventSource);
            return () -> {
                actionUndo.undo();
                unregisterUndo.undo();
            };
        });
        refRef.set(ref);
        return ref;
    }
}
