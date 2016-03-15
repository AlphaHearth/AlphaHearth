package info.hearthsim.brazier.abilities;

import info.hearthsim.brazier.PreparedResult;
import info.hearthsim.brazier.actions.undo.UndoAction;
import info.hearthsim.brazier.actions.undo.UndoableUnregisterAction;

import java.util.ArrayList;
import java.util.List;

import org.jtrim.utils.ExceptionHelper;

/**
 * {@link List} of {@link Ability}, providing methods {@link #addAndActivateAbility(Ability)}
 * and {@link #deactivate()} to manage a list of {@code Ability}s.
 */
public final class AbilityList <Self> {
    private final Self self;
    private List<AbilityRef<Self>> abilities;

    public AbilityList(Self self) {
        ExceptionHelper.checkNotNullArgument(self, "self");

        this.self = self;
        this.abilities = new ArrayList<>();
    }

    /**
     * Creates a copy of this {@code AbilityList} for the given object.
     *
     * @param other the given object.
     * @return {@code PreparedResult} for the new copied {@code AbilityList},
     *         to which all the abilities are not added until {@link PreparedResult#activate()}
     *         is called.
     */
    public PreparedResult<AbilityList<Self>> copyFor(Self other) {
        AbilityList<Self> list = new AbilityList<>(other);

        List<Ability<? super Self>> initialAbilities = new ArrayList<>(abilities.size());
        for (AbilityRef<Self> ability: abilities) {
            initialAbilities.add(ability.ability);
        }

        return new PreparedResult<>(list, () -> {
            if (initialAbilities.isEmpty()) {
                return UndoAction.DO_NOTHING;
            }

            UndoAction.Builder undos = new UndoAction.Builder(initialAbilities.size());
            for (Ability<? super Self> ability: initialAbilities) {
                undos.addUndo(list.addAndActivateAbility(ability));
            }
            return undos;
        });
    }

    /**
     * Adds the given ability to the {@code AbilityList} and activates it for the underlying object
     * by calling its {@link Ability#activate(Object)} method.
     *
     * @param ability the given ability.
     */
    public UndoAction addAndActivateAbility(Ability<? super Self> ability) {
        ExceptionHelper.checkNotNullArgument(ability, "ability");

        UndoableUnregisterAction registerRef = ability.activate(self);
        AbilityRef<Self> toBeAdded = new AbilityRef<>(ability, registerRef);
        abilities.add(toBeAdded);

        return () -> {
            abilities.remove(toBeAdded);
            registerRef.undo();
        };
    }

    /**
     * Deactivates all the registered abilities.
     */
    public UndoAction deactivate() {
        if (abilities.isEmpty()) {
            return UndoAction.DO_NOTHING;
        }

        List<AbilityRef<Self>> prevAbilities = abilities;
        abilities = new ArrayList<>();

        UndoAction.Builder result = new UndoAction.Builder();
        result.addUndo(() -> abilities = prevAbilities);

        for (AbilityRef<Self> ability: prevAbilities) {
            result.addUndo(ability.deactivate());
        }

        return result;
    }

    /**
     * Reference to a activated {@link Ability} and its respective unregister (deactivate) action.
     */
    private static final class AbilityRef <Self> {
        public final Ability<? super Self> ability;
        public final UndoableUnregisterAction unregisterAction;

        public AbilityRef(
            Ability<? super Self> ability,
            UndoableUnregisterAction unregisterAction) {
            ExceptionHelper.checkNotNullArgument(ability, "ability");
            ExceptionHelper.checkNotNullArgument(unregisterAction, "unregisterAction");

            this.ability = ability;
            this.unregisterAction = unregisterAction;
        }

        public UndoAction deactivate() {
            return unregisterAction.unregister();
        }
    }
}
