package info.hearthsim.brazier.events;

import info.hearthsim.brazier.*;
import info.hearthsim.brazier.Character;
import org.jtrim.utils.ExceptionHelper;

/**
 * The event of dealing specific damage on a {@link Character} by a {@link DamageSource}.
 */
public final class DamageEvent implements TargetRef {
    private final DamageSource damageSource;
    private final info.hearthsim.brazier.Character target;
    private final int damageDealt;

    public DamageEvent(
            DamageSource damageSource,
            Character target,
            int damageDealt) {
        ExceptionHelper.checkNotNullArgument(damageSource, "damageSource");
        ExceptionHelper.checkNotNullArgument(target, "target");

        this.damageSource = damageSource;
        this.target = target;
        this.damageDealt = damageDealt;
    }

    public DamageSource getDamageSource() {
        return damageSource;
    }

    @Override
    public Character getTarget() {
        return target;
    }

    public int getDamageDealt() {
        return damageDealt;
    }
}
