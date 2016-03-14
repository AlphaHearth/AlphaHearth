package info.hearthsim.brazier.minions;

import java.util.function.Supplier;

/**
 * Functional interface with its sole un-implemented method {@link #getMinion()} which returns
 * a {@link MinionDescr}.
 */
public interface MinionProvider extends Supplier<MinionDescr> {
    public MinionDescr getMinion();

    public default MinionDescr get() {
        return getMinion();
    }
}
