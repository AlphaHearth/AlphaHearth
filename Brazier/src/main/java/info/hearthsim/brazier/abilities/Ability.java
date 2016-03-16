package info.hearthsim.brazier.abilities;

import info.hearthsim.brazier.Game;
import info.hearthsim.brazier.GameProperty;
import info.hearthsim.brazier.actions.undo.UndoAction;
import info.hearthsim.brazier.actions.undo.UndoableUnregisterAction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import info.hearthsim.brazier.events.*;
import info.hearthsim.brazier.parsing.NamedArg;
import org.jtrim.utils.ExceptionHelper;

// TODO Ability is now only used to add aura to characters or add spell power.
// TODO Consider using Aura in Json files to replace this interface.
/**
 * Activatable ability for minions, which will not take effect unless its {@link #activate(Self)} method
 * is invoked. Abilities for minions include aura-adding and <b>Spell Power</b>, which adds a certain
 * {@link Aura} or buffs the owner's <b>Spell Power</b> when it is activated.
 * <p>
 * Instances of {@code Ability} must be <b>immutable</b>: no state can be stored. Using the interface as
 * a functional interface and implementing it by using lambda expression is highly recommended.
 * <p>
 * For predefined {@code Ability}s, see {@link Abilities} and {@link MinionAbilities}.
 *
 * @see Aura
 * @see Abilities
 * @see MinionAbilities
 */
@FunctionalInterface
public interface Ability <Self> {
    /**
     * Activates the {@code Ability} with the given object.
     */
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
    public static <Self extends GameProperty, EventArg> Ability<Self> onEventAbility(
        @NamedArg("filter") GameEventFilter<? super Self, ? super EventArg> filter,
        @NamedArg("action") GameEventAction<? super Self, ? super EventArg> action,
        @NamedArg("event") SimpleEventType eventType) {
        ExceptionHelper.checkNotNullArgument(filter, "filter");
        ExceptionHelper.checkNotNullArgument(action, "action");
        ExceptionHelper.checkNotNullArgument(eventType, "eventType");

        return (Self self) -> {
            GameEvents events = self.getGame().getEvents();
            GameActionEventsRegistry<EventArg> listeners = events.simpleListeners(eventType);
            return listeners.addAction((Game game, EventArg eventArg) -> {
                if (filter.applies(game, self, eventArg)) {
                    return action.alterGame(game, self, eventArg);
                } else {
                    return UndoAction.DO_NOTHING;
                }
            });
        };
    }
}
