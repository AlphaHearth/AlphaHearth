package info.hearthsim.brazier.events;

import info.hearthsim.brazier.actions.undo.UndoAction;
import info.hearthsim.brazier.*;
import org.jtrim.utils.ExceptionHelper;

/**
 * The request to deal a {@link Damage} to a {@link TargetableCharacter}.
 */
public final class DamageRequest implements TargetRef, PlayerProperty {
    private final Damage damage;
    private final TargetableCharacter target;
    private boolean vetoDamage;

    /**
     * Creates a new {@code DamageRequest} with the given {@link Damage} and
     * {@link TargetableCharacter} as target.
     */
    public DamageRequest(Damage damage, TargetableCharacter target) {
        ExceptionHelper.checkNotNullArgument(damage, "damage");
        ExceptionHelper.checkNotNullArgument(target, "target");

        this.damage = damage;
        this.target = target;
        this.vetoDamage = false;
    }

    /**
     * Returns the owner of the {@link DamageSource}.
     */
    @Override
    public Player getOwner() {
        return damage.getSource().getOwner();
    }

    /** Returns the target */
    @Override
    public TargetableCharacter getTarget() {
        return target;
    }

    /** Returns the {@link Damage} */
    public Damage getDamage() {
        return damage;
    }

    public UndoAction vetoDamage() {
        if (vetoDamage) {
            return UndoAction.DO_NOTHING;
        }

        vetoDamage = true;
        return () -> vetoDamage = false;
    }
}
