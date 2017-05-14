package info.hearthsim.brazier.abilities;

import info.hearthsim.brazier.game.Entity;
import info.hearthsim.brazier.util.UndoAction;
import org.jtrim.utils.ExceptionHelper;

import java.util.ArrayList;
import java.util.List;

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
     *
     * @param other the new ability actor for the new {@code AbilityList}.
     * @param copyAbilities whether to copy all the abilities added to this {@code AbilityList},
     *                      regardless of their {@code toCopy} field.
     *
     * @see #addAndActivateAbility(Ability, boolean, boolean)
     */
    public AbilityList<Self> copyFor(Self other, boolean copyAbilities) {
        AbilityList<Self> list = new AbilityList<>(other);
        for (AbilityRef ability : abilities) {
            if (ability.toCopy) {
                if (!ability.needsReactivate)
                    list.abilities.add(ability);
                else
                    list.addAndActivateAbility(ability.ability);
            }
            else if (copyAbilities)
                list.addAndActivateAbility(ability.ability);
        }

        return list;
    }

    /**
     * Adds the given ability to the {@code AbilityList} and activates it for the underlying object
     * by calling its {@link Ability#activate(Object)} method.
     * <p>
     * For a finer control on the copying of this registered ability, see
     * {@link #addAndActivateAbility(Ability, boolean, boolean)}.
     *
     * @param ability the given ability.
     *
     * @see #addAndActivateAbility(Ability, boolean, boolean)
     */
    public UndoAction<AbilityList> addAndActivateAbility(Ability<? super Self> ability) {
        return addAndActivateAbility(ability, false, false);
    }

    /**
     * Adds the given ability to the {@code AbilityList} and activates it for the underlying object
     * by calling its {@link Ability#activate(Object)} method.
     * <p>
     * The other two boolean parameters control how this ability will be copied when {@link #copyFor(Entity, boolean)}
     * is invoked.
     *
     * @param ability the given ability to be added and activated.
     * @param toCopy whether this ability should be copied when {@link #copyFor(Entity, boolean)} is invoked.
     * @param needsReactivate whether this ability should be reactivated for the new {@code AbilityList} when it is
     *                        copied.
     *
     * @see #copyFor(Entity, boolean)
     */
    public UndoAction<AbilityList> addAndActivateAbility(Ability<? super Self> ability,
                                                         boolean toCopy, boolean needsReactivate) {
        ExceptionHelper.checkNotNullArgument(ability, "ability");

        UndoAction<? super Self> registerRef = ability.activate(self);
        AbilityRef toBeAdded = new AbilityRef(ability, registerRef, toCopy, needsReactivate);
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
        public final UndoAction<? super Self> registerRef;
        private final boolean toCopy;
        private final boolean needsReactivate;

        public AbilityRef(
            Ability<? super Self> ability,
            UndoAction<? super Self> registerRef,
            boolean toCopy,
            boolean needsReactivate) {
            ExceptionHelper.checkNotNullArgument(ability, "ability");
            ExceptionHelper.checkNotNullArgument(registerRef, "registerRef");

            this.ability = ability;
            this.registerRef = registerRef;
            this.toCopy = toCopy;
            this.needsReactivate = needsReactivate;
        }

        public void deactivate(Self self) {
            registerRef.undo(self);
        }
    }
}
