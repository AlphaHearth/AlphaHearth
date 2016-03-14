package info.hearthsim.brazier.events;

import info.hearthsim.brazier.PlayerProperty;
import info.hearthsim.brazier.World;
import info.hearthsim.brazier.actions.undo.UndoAction;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import info.hearthsim.brazier.actions.undo.UndoableUnregisterAction;
import org.jtrim.utils.ExceptionHelper;

public final class CompletableWorldEventBasedActionDef<Self extends PlayerProperty, T> {
    private final boolean triggerOnce;
    private final int priority;
    private final Function<WorldEvents, ? extends CompletableWorldActionEventsRegistry<T>> actionEventListenersGetter;
    private final CompletableWorldEventAction<? super Self, ? super T> eventAction;

    public CompletableWorldEventBasedActionDef(
            int priority,
            Function<WorldEvents, ? extends CompletableWorldActionEventsRegistry<T>> actionEventListenersGetter,
            CompletableWorldEventAction<? super Self, ? super T> eventAction) {
        this(false, priority, actionEventListenersGetter, eventAction);
    }

    public CompletableWorldEventBasedActionDef(
            boolean triggerOnce,
            int priority,
            Function<WorldEvents, ? extends CompletableWorldActionEventsRegistry<T>> actionEventListenersGetter,
            CompletableWorldEventAction<? super Self, ? super T> eventAction) {
        ExceptionHelper.checkNotNullArgument(actionEventListenersGetter, "actionEventListenersGetter");
        ExceptionHelper.checkNotNullArgument(eventAction, "eventAction");

        this.triggerOnce = triggerOnce;
        this.priority = priority;
        this.actionEventListenersGetter = actionEventListenersGetter;
        this.eventAction = eventAction;
    }

    public static <Self extends PlayerProperty, T> UndoableUnregisterAction registerAll(
            List<CompletableWorldEventBasedActionDef<Self, T>> actionDefs,
            WorldEvents worldEvents,
            Self self) {
        if (actionDefs.isEmpty()) {
            return UndoableUnregisterAction.DO_NOTHING;
        }

        UndoableUnregisterAction.Builder result = new UndoableUnregisterAction.Builder(actionDefs.size());
        for (CompletableWorldEventBasedActionDef<Self, T> actionDef: actionDefs) {
            result.addRef(actionDef.registerForEvent(worldEvents, self));
        }
        return result;
    }

    private UndoableUnregisterAction registerForEvents(
            CompletableWorldActionEventsRegistry<T> actionEvents,
            Self self,
            CompletableWorldEventAction<? super Self, ? super T> appliedEventAction) {
        return actionEvents.addAction(priority, (World world, T object) -> {
            CompleteWorldEventAction<? super Self, ? super T> result = appliedEventAction.startEvent(world, self, object);
            return new CompleteWorldObjectAction<T>() {
                @Override
                public UndoAction alterWorld(World world, T completeObj) {
                    return result.alterWorld(world, self, completeObj);
                }

                @Override
                public void undo() {
                    result.undo();
                }
            };
        });
    }

    public UndoableUnregisterAction registerForEvent(WorldEvents worldEvents, Self self) {
        CompletableWorldActionEventsRegistry<T> actionEvents = actionEventListenersGetter.apply(worldEvents);
        if (!triggerOnce) {
            return registerForEvents(actionEvents, self, eventAction);
        }

        AtomicReference<UndoableUnregisterAction> refRef = new AtomicReference<>();
        UndoableUnregisterAction ref = registerForEvents(actionEvents, self, (World world, Self eventSelf, T eventSource) -> {
            UndoAction unregisterUndo = refRef.get().unregister();
            CompleteWorldEventAction<? super Self, ? super T> completeEventAction
                    = eventAction.startEvent(world, eventSelf, eventSource);

            return new CompleteWorldEventAction<Self, T>() {
                @Override
                public UndoAction alterWorld(World world, Self self, T eventSource) {
                    return completeEventAction.alterWorld(world, self, eventSource);
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
