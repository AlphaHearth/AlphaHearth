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
 * For predefined {@code Ability}s, see {@link Abilities} and {@link MinionAbilities}.
 *
 * @see Abilities
 * @see MinionAbilities
 */
public interface Ability <Self> {
    public UndoableUnregisterAction activate(Self self);

    /**
     * Merges the given collection of {@code Ability}s to one {@code Ability}.
     *
     * @throws NullPointerException if any of the given {@code Ability}s is {@code null}.
     */
    public static <Self> Ability<Self>
    merge(Collection<? extends Ability<? super Self>> abilities) {
        ExceptionHelper.checkNotNullElements(abilities, "abilities");

        if (abilities.isEmpty()) {
            return (self) -> UndoableUnregisterAction.DO_NOTHING;
        }

        List<Ability<? super Self>> abilitiesCopy = new ArrayList<>(abilities);

        return (Self self) -> {
            UndoableUnregisterAction.Builder result = new UndoableUnregisterAction.Builder(abilitiesCopy.size());
            for (Ability<? super Self> ability : abilitiesCopy) {
                result.addRef(ability.activate(self));
            }
            return result;
        };
    }

    /**
     * Creates an {@code Ability} which does the given {@code action} on the given type of event,
     * if the event instance can satisfy the given filter.
     *
     * @param filter the given filter.
     * @param action the given action.
     * @param eventType the given type of event.
     * @return the created {@code Ability}.
     */
    public static <Self extends WorldProperty, EventArg> Ability<Self> onEventAbility(
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
