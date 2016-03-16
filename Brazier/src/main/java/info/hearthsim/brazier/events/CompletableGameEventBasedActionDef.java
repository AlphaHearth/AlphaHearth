package info.hearthsim.brazier.events;

import info.hearthsim.brazier.PlayerProperty;
import info.hearthsim.brazier.Game;
import info.hearthsim.brazier.actions.undo.UndoAction;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import info.hearthsim.brazier.actions.undo.UndoableUnregisterAction;
import org.jtrim.utils.ExceptionHelper;

public final class CompletableGameEventBasedActionDef <Self extends PlayerProperty, T> {
    private final boolean triggerOnce;
    private final int priority;
    private final Function<GameEvents, ? extends CompletableGameActionEventsRegistry<T>> actionEventListenersGetter;
    private final CompletableGameEventAction<? super Self, ? super T> eventAction;

    public CompletableGameEventBasedActionDef(
        int priority,
        Function<GameEvents, ? extends CompletableGameActionEventsRegistry<T>> actionEventListenersGetter,
        CompletableGameEventAction<? super Self, ? super T> eventAction) {
        this(false, priority, actionEventListenersGetter, eventAction);
    }

    public CompletableGameEventBasedActionDef(
        boolean triggerOnce,
        int priority,
        Function<GameEvents, ? extends CompletableGameActionEventsRegistry<T>> actionEventListenersGetter,
        CompletableGameEventAction<? super Self, ? super T> eventAction) {
        ExceptionHelper.checkNotNullArgument(actionEventListenersGetter, "actionEventListenersGetter");
        ExceptionHelper.checkNotNullArgument(eventAction, "eventAction");

        this.triggerOnce = triggerOnce;
        this.priority = priority;
        this.actionEventListenersGetter = actionEventListenersGetter;
        this.eventAction = eventAction;
    }

    public static <Self extends PlayerProperty, T> UndoableUnregisterAction registerAll(
            List<CompletableGameEventBasedActionDef<Self, T>> actionDefs,
            GameEvents gameEvents,
            Self self) {
        if (actionDefs.isEmpty()) {
            return UndoableUnregisterAction.DO_NOTHING;
        }

        UndoableUnregisterAction.Builder result = new UndoableUnregisterAction.Builder(actionDefs.size());
        for (CompletableGameEventBasedActionDef<Self, T> actionDef: actionDefs) {
            result.addRef(actionDef.registerForEvent(gameEvents, self));
        }
        return result;
    }

    private UndoableUnregisterAction registerForEvents(
            CompletableGameActionEventsRegistry<T> actionEvents,
            Self self,
            CompletableGameEventAction<? super Self, ? super T> appliedEventAction) {
        RegisterId id = actionEvents.addAction(priority, (Game game, T object) -> {
            CompleteGameEventAction<? super Self, ? super T> result = appliedEventAction.startEvent(game, self, object);
            return new CompleteGameObjectAction<T>() {
                @Override
                public UndoAction alterGame(Game game, T completeObj) {
                    return result.alterGame(game, self, completeObj);
                }

                @Override
                public void undo() {
                    result.undo();
                }
            };
        });

        return () -> {
            actionEvents.unregister(id);
            return UndoAction.DO_NOTHING;
        };
    }

    public UndoableUnregisterAction registerForEvent(GameEvents gameEvents, Self self) {
        CompletableGameActionEventsRegistry<T> actionEvents = actionEventListenersGetter.apply(gameEvents);
        if (!triggerOnce) {
            return registerForEvents(actionEvents, self, eventAction);
        }

        AtomicReference<UndoableUnregisterAction> refRef = new AtomicReference<>();
        UndoableUnregisterAction ref = registerForEvents(actionEvents, self, (Game game, Self eventSelf, T eventSource) -> {
            UndoAction unregisterUndo = refRef.get().unregister();
            CompleteGameEventAction<? super Self, ? super T> completeEventAction
                    = eventAction.startEvent(game, eventSelf, eventSource);

            return new CompleteGameEventAction<Self, T>() {
                @Override
                public UndoAction alterGame(Game game, Self self, T eventSource) {
                    return completeEventAction.alterGame(game, self, eventSource);
                }

                @Override
                public void undo() {
                    completeEventAction.undo();
                    unregisterUndo.undo();
                }
            };
        });
        refRef.set(ref);
        return ref;
    }
}
