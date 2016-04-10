package info.hearthsim.brazier.events;

import info.hearthsim.brazier.PlayerProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jtrim.utils.ExceptionHelper;

/**
 * Event-based action, usually used as a functional interface with its sole un-implemented method
 * {@link #trigger(Owner, Source)}.
 * <p>
 * An event-based action will be triggered if a certain event happens. When it is triggered, its
 * {@link #trigger(Owner, Source)} method will be invoked to execute the designated logic. Two
 * parameters are needed to trigger an {@code EventAction}, including:
 * <ul>
 *     <li>
 *         {@code owner}: the owner of the triggering effect. For example, minion <em>Acolyte of Pain</em>
 *         has a triggering effect which draws a card for its owner when it takes damage. In this case,
 *         the owner of this triggering effect would be the <em>Acolyte of Pain</em> itself, being
 *         a {@link info.hearthsim.brazier.game.minions.Minion Minion};
 *     </li>
 *     <li>
 *         {@code source}: source of the event, or what triggers this triggering effect. For example,
 *         the card-drawing action (being the {@code EventAction}) of <em>Acolyte of Pain</em> will
 *         be triggered by a damaging event (being a {@link DamageEvent}, see
 *         {@link SimpleEventType#MINION_DAMAGED}), this event will be the source, or triggeror, of
 *         this action.
 *     </li>
 * </ul>
 * Moreover, a {@code EventAction} is usually used in pair with a {@link EventFilter}, which determines
 * if a specific type of event should trigger this action. Consider <em>Acolyte of Pain</em> again, his
 * {@code EventAction} of card-drawing shouldn't be triggered by any {@code DamageEvent}, but only those
 * damages the <em>Acolyte</em> himself. In this case, the triggering effect of <em>Acolyte of Pain</em>
 * also comes with a {@link EventFilters#TARGET_SELF}, which returns {@code true} only if the owner
 * is the target of the triggering event.
 * <p>
 * For predefined {@code EventAction}s, see {@link EventActions}.
 *
 * @param <Owner> the type of the owner of this triggering action.
 * @param <Source> the type of the triggering source of this triggering action.
 *
 * @see EventFilter
 * @see EventActions
 */
@FunctionalInterface
public interface EventAction <Owner extends PlayerProperty, Source> {
    public static final EventAction<PlayerProperty, Object> DO_NOTHING
            = (self, eventSource) -> {};

    /**
     * Triggers this {@code EventAction} with the given action owner and the triggering event.
     */
    public void trigger(Owner owner, Source source);

    public static <Self extends PlayerProperty, EventSource> EventAction<? super Self, ? super EventSource> merge(
            Collection<? extends EventAction<? super Self, ? super EventSource>> actions) {

        int filterCount = actions.size();
        if (filterCount == 0) {
            return DO_NOTHING;
        }
        if (filterCount == 1) {
            return actions.iterator().next();
        }

        List<EventAction<? super Self, ? super EventSource>> actionsCopy = new ArrayList<>(actions);
        ExceptionHelper.checkNotNullElements(actionsCopy, "actions");

        return (Self owner, EventSource eventSource) -> {
            for (EventAction<? super Self, ? super EventSource> action: actionsCopy)
                action.trigger(owner, eventSource);
        };
    }
}
