package info.hearthsim.brazier.abilities;

import info.hearthsim.brazier.World;
import info.hearthsim.brazier.WorldProperty;
import info.hearthsim.brazier.actions.undo.UndoAction;
import info.hearthsim.brazier.actions.undo.UndoableUnregisterAction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import info.hearthsim.brazier.events.*;
import info.hearthsim.brazier.parsing.NamedArg;
import org.jtrim.utils.ExceptionHelper;

/**
 * Activatable ability. Such effects include enrage and spell power, which activates when certain condition is
 * satisfied and deactivates when it's not satisfied. It's different than triggering effect like Gahz'rilla
 * or Gurubashi Berserker has, which will not deactivate unless it is silenced.
 * <p>
 * For predefined {@code ActivatableAbility}s, see {@link ActivatableAbilities} and {@link MinionAbilities}.
 *
 * @see ActivatableAbilities
 * @see MinionAbilities
 */
public interface ActivatableAbility <Self> {
    public UndoableUnregisterAction activate(Self self);

    /**
     * Merges the given collection of {@code ActivatableAbility}s to one {@code ActivatableAbility}.
     *
     * @throws NullPointerException if any of the given {@code ActivatableAbility}s is {@code null}.
     */
    public static <Self> ActivatableAbility<Self>
    merge(Collection<? extends ActivatableAbility<? super Self>> abilities) {
        ExceptionHelper.checkNotNullElements(abilities, "abilities");

        if (abilities.isEmpty()) {
            return (self) -> UndoableUnregisterAction.DO_NOTHING;
        }

        List<ActivatableAbility<? super Self>> abilitiesCopy = new ArrayList<>(abilities);

        return (Self self) -> {
            UndoableUnregisterAction.Builder result = new UndoableUnregisterAction.Builder(abilitiesCopy.size());
            for (ActivatableAbility<? super Self> ability : abilitiesCopy) {
                result.addRef(ability.activate(self));
            }
            return result;
        };
    }

    /**
     * Creates an {@code ActivatableAbility} which does the given {@code action} on the given type of event,
     * if the event instance can satisfy the given filter.
     *
     * @param filter the given filter.
     * @param action the given action.
     * @param eventType the given type of event.
     * @return the created {@code ActivatableAbility}.
     */
    public static <Self extends WorldProperty, EventArg> ActivatableAbility<Self> onEventAbility(
        @NamedArg("filter") WorldEventFilter<? super Self, ? super EventArg> filter,
        @NamedArg("action") WorldEventAction<? super Self, ? super EventArg> action,
        @NamedArg("event") SimpleEventType eventType) {
        ExceptionHelper.checkNotNullArgument(filter, "filter");
        ExceptionHelper.checkNotNullArgument(action, "action");
        ExceptionHelper.checkNotNullArgument(eventType, "eventType");

        return (Self self) -> {
            WorldEvents events = self.getWorld().getEvents();
            WorldActionEventsRegistry<EventArg> listeners = events.simpleListeners(eventType);
            return listeners.addAction((World world, EventArg eventArg) -> {
                if (filter.applies(world, self, eventArg)) {
                    return action.alterWorld(world, self, eventArg);
                } else {
                    return UndoAction.DO_NOTHING;
                }
            });
        };
    }
}
