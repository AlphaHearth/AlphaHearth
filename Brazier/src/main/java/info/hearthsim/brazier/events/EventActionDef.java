package info.hearthsim.brazier.events;

import info.hearthsim.brazier.game.Entity;
import info.hearthsim.brazier.game.GameProperty;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;

import info.hearthsim.brazier.util.UndoAction;
import org.jtrim.utils.ExceptionHelper;

/**
 * The definition of a event-based action, which will be triggered when certain game event happens.
 * Comparing to a single {@code EventAction}, which can only be used to define the logic of an action,
 * an {@code EventActionDef} combines all the necessary fields together to represent the triggering effect
 * of a certain Hearthstone entity.
 * <p>
 * Fields of an {@code EventActionDef} include:
 * <ul>
 *     <li>{@code action}: the {@code EventAction} to be triggered;</li>
 *     <li>
 *         {@code listenerGetter}: a {@code Function} from {@code GameEvents} to {@code GameEventActions},
 *         which fetches the appropriate {@code GameEventActions} from the given {@code GamEvents} for this
 *         event-based action.
 *     </li>
 *     <li>
 *         {@code filter}: {@code EventFilter} which determines if this action should be triggered
 *         by certain event;
 *     </li>
 *     <li>
 *         {@code lazyFilter}: {@code boolean} field, designating if the invocation of {@code filter} should
 *         be delayed to the execution of the action;
 *     </li>
 *     <li>
 *         {@code priority}: {@code int} field, the priority of this event-based action;
 *     </li>
 *     <li>
 *         {@code triggerOnce}: {@code boolean} field, designating if the action will unregister itself when
 *         it is executed.
 *     </li>
 * </ul>
 * The sole {@code public} method of an {@code EventActionDef} is {@link #registerForEvent(GameEvents, Owner)},
 * which registers itself to the given {@link GameEvents} for the given owner.
 *
 * @see EventAction
 * @see EventFilter
 * @see GameEvents
 */
public final class EventActionDef <Owner extends Entity, Source extends GameProperty> {
    private final boolean lazyFilter;
    private final boolean triggerOnce;
    private final int priority;
    private final Function<GameEvents, GameEventActions<Source>> listenerGetter;
    private final EventFilter<? super Owner, ? super Source> filter;
    private final EventAction<? super Owner, ? super Source> action;

    public EventActionDef(
        boolean lazyFilter,
        boolean triggerOnce,
        int priority,
        Function<GameEvents, GameEventActions<Source>> listenerGetter,
        EventFilter<? super Owner, ? super Source> condition,
        EventAction<? super Owner, ? super Source> action) {
        ExceptionHelper.checkNotNullArgument(listenerGetter, "listenerGetter");
        ExceptionHelper.checkNotNullArgument(condition, "condition");
        ExceptionHelper.checkNotNullArgument(action, "action");

        this.lazyFilter = lazyFilter;
        this.triggerOnce = triggerOnce;
        this.priority = priority;
        this.listenerGetter = listenerGetter;
        this.filter = condition;
        this.action = action;
    }

    /**
     * Registers the given list of {@code EventActionDef} to the given {@link GameEvents}
     * for the given {@code Owner}.
     */
    public static <Self extends Entity, T extends GameProperty> UndoAction<GameEvents>
    registerAll(
        List<EventActionDef<Self, T>> actionDefs,
        GameEvents gameEvents,
        Self eventOwner) {
        if (actionDefs.isEmpty())
            return UndoAction.DO_NOTHING;

        UndoAction.Builder<GameEvents> result =
            new UndoAction.Builder<>(actionDefs.size());
        for (EventActionDef<Self, T> actionDef : actionDefs) {
            result.add(actionDef.registerForEvent(gameEvents, eventOwner));
        }
        return result;
    }

    /**
     * Registers this {@code EventActionDef} to the given {@link GameEvents} for the given {@code Owner}.
     */
    public UndoAction<GameEvents> registerForEvent(GameEvents gameEvents, Owner owner) {
        GameEventActions<Source> actionEvents = listenerGetter.apply(gameEvents);
        if (!triggerOnce) {
            UndoAction<GameEventActions> undoRef = registerForEvents(actionEvents, owner, action);
            return (ge) -> undoRef.undo(listenerGetter.apply(ge));
        }

        AtomicReference<UndoAction<GameEvents>> refRef = new AtomicReference<>();
        UndoAction<GameEventActions> ref = registerForEvents(actionEvents, owner,
            (Owner eventSelf, Source eventSource) -> {
                refRef.get().undo(owner.getGame().getEvents());
                action.trigger(eventSelf, eventSource);
            });
        refRef.set((ge) -> ref.undo(listenerGetter.apply(ge)));
        return refRef.get();
    }

    private UndoAction<GameEventActions> registerForEvents(
        GameEventActions<Source> actionEvents,
        Owner owner,
        EventAction<? super Owner, ? super Source> appliedEventAction) {

        if (lazyFilter) {
            return actionEvents.register((Source eventSource) -> {
                if (filter.applies(owner, eventSource))
                    appliedEventAction.trigger(owner, eventSource);
            }, priority);
        } else {
            Predicate<Source> condition = (Source eventSource) -> filter.applies(owner, eventSource);
            return actionEvents.register((Source eventSource) -> {
                appliedEventAction.trigger(owner, eventSource);
            }, condition, priority);
        }
    }
}
