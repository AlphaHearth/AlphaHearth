package info.hearthsim.brazier.actions;

import info.hearthsim.brazier.game.Character;

import java.util.function.Predicate;

import info.hearthsim.brazier.game.Player;
import info.hearthsim.brazier.PlayerProperty;
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
    public AttackRequest(Character attacker, Character target) {
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
    public void replaceTarget(Character newTarget) {
        target = newTarget;
    }

    @Override
    public Player getOwner() {
        return attacker.getOwner();
    }
}
