package info.hearthsim.brazier;

import info.hearthsim.brazier.game.Character;

/**
 * An event of targeting a specific target.
 */
public interface TargetRef {
    /**
     * Returns the target of this event; returns {@code null} if there is no target.
     */
    public Character getTarget();
}
