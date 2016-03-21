package info.hearthsim.brazier.abilities;

import java.util.ArrayList;
import java.util.List;

import info.hearthsim.brazier.Entity;
import info.hearthsim.brazier.actions.undo.UndoObjectAction;
import org.jtrim.utils.ExceptionHelper;

/**
 * {@link List} of {@link Ability}, providing methods {@link #addAndActivateAbility(Ability)}
 * and {@link #deactivate()} to manage a list of {@code Ability}s.
 */
public final class AbilityList<Self extends Entity> {
    private final Self self;
    private List<AbilityRef> abilities;

    /**
     * Creates an empty {@code AbilityList} for the given object.
     */
    public AbilityList(Self self) {
        ExceptionHelper.checkNotNullArgument(self, "self");

        this.self = self;
        this.abilities = new ArrayList<>();
    }

    /**
     * Creates a copy of this {@code AbilityList} for the given object.
     */
    public AbilityList<Self> copyFor(Self other) {
        AbilityList<Self> list = new AbilityList<>(other);
        list.abilities.addAll(abilities);

        return list;
    }

    /**
     * Adds the given ability to the {@code AbilityList} and activates it for the underlying object
     * by calling its {@link Ability#activate(Object)} method.
     *
     * @param ability the given ability.
     */
    public UndoObjectAction<AbilityList> addAndActivateAbility(Ability<? super Self> ability) {
        ExceptionHelper.checkNotNullArgument(ability, "ability");

        UndoObjectAction<? super Self> registerRef = ability.activate(self);
        AbilityRef toBeAdded = new AbilityRef(ability, registerRef);
        abilities.add(toBeAdded);

        return (al) -> {
            registerRef.undo(self);
            al.abilities.remove(toBeAdded);
        };
    }

    /**
     * Deactivates all the registered abilities.
     */
    public void deactivate() {
        if (abilities.isEmpty())
            return;

        // Some ability may remove itself from the list when it's deactivated.
        // Using iterator or for-each statement here will result in ConcurrentModificationException.
        // Traversing the list backwards may be safer.
        for (int i = abilities.size() - 1; i >= 0; i--) {
            if (i >= abilities.size())
                continue;
            abilities.get(i).deactivate(self);
        }
        abilities.clear();
    }

    /**
     * Reference to a activated {@link Ability} and its respective unregister (deactivate) action.
     */
    private final class AbilityRef {
        public final Ability<? super Self> ability;
        public final UndoObjectAction<? super Self> registerRef;

        public AbilityRef(
            Ability<? super Self> ability,
            UndoObjectAction<? super Self> registerRef) {
            ExceptionHelper.checkNotNullArgument(ability, "ability");
            ExceptionHelper.checkNotNullArgument(registerRef, "registerRef");

            this.ability = ability;
            this.registerRef = registerRef;
        }

        public void deactivate(Self self) {
            registerRef.undo(self);
        }
    }
}
