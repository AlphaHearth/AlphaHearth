package info.hearthsim.brazier.actions;

import info.hearthsim.brazier.Character;

import java.util.function.Predicate;

import info.hearthsim.brazier.GameProperty;
import info.hearthsim.brazier.Player;
import info.hearthsim.brazier.PlayerProperty;
import info.hearthsim.brazier.actions.undo.UndoAction;
import org.jtrim.utils.ExceptionHelper;

/**
 * Container class for a request of attack, which has fields {@code attacker} and {@code target},
 * represented as {@link Character}s.
 */
public final class AttackRequest implements PlayerProperty {
    private final Character attacker;
    private Character target;

    /**
     * Creates a new {@code AttackRequest} with the given {@code attacker} and {@code target}.
     *
     * @param attacker the given {@code attacker}.
     * @param target the given {@code target}.
     *
     * @throws NullPointerException if the given {@code attacker} is {@code null}.
     */
    public AttackRequest(info.hearthsim.brazier.Character attacker, Character target) {
        ExceptionHelper.checkNotNullArgument(attacker, "attacker");

        this.attacker = attacker;
        this.target = target;
    }

    /**
     * Returns the {@code attacker}.
     */
    public Character getAttacker() {
        return attacker;
    }

    /**
     * Returns the {@code target}.
     */
    public Character getTarget() {
        return target;
    }

    /**
     * Returns if the target exists and satisfies the given predicate.
     */
    public boolean testExistingTarget(Predicate<? super Character> check) {
        return target != null && check.test(target);
    }

    /**
     * Sets the target of this {@code AttackRequest}.
     */
    public UndoAction replaceTarget(Character newTarget) {
        Character prevTarget = target;
        target = newTarget;
        return () -> target = prevTarget;
    }

    @Override
    public Player getOwner() {
        return attacker.getOwner();
    }
}
