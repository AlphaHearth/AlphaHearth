package info.hearthsim.brazier.actions;

import info.hearthsim.brazier.TargetableCharacter;
import java.util.function.Predicate;

import info.hearthsim.brazier.actions.undo.UndoAction;
import org.jtrim.utils.ExceptionHelper;

/**
 * Container class for a request of attack, which has fields {@code attacker} and {@code target},
 * represented as {@link TargetableCharacter}s.
 */
public final class AttackRequest {
    private final TargetableCharacter attacker;
    private TargetableCharacter target;

    /**
     * Creates a new {@code AttackRequest} with the given {@code attacker} and {@code target}.
     *
     * @param attacker the given {@code attacker}.
     * @param target the given {@code target}.
     *
     * @throws NullPointerException if the given {@code attacker} is {@code null}.
     */
    public AttackRequest(TargetableCharacter attacker, TargetableCharacter target) {
        ExceptionHelper.checkNotNullArgument(attacker, "attacker");

        this.attacker = attacker;
        this.target = target;
    }

    /**
     * Returns the {@code attacker}.
     */
    public TargetableCharacter getAttacker() {
        return attacker;
    }

    /**
     * Returns the {@code target}.
     */
    public TargetableCharacter getTarget() {
        return target;
    }

    /**
     * Returns if the target exists and satisfies the given predicate.
     */
    public boolean testExistingTarget(Predicate<? super TargetableCharacter> check) {
        return target != null && check.test(target);
    }

    /**
     * Sets the target of this {@code AttackRequest}.
     */
    public UndoAction replaceTarget(TargetableCharacter newTarget) {
        TargetableCharacter prevTarget = target;
        target = newTarget;
        return () -> target = prevTarget;
    }
}
