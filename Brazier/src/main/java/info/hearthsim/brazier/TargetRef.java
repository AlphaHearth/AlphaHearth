package info.hearthsim.brazier;

/**
 * An event of targeting a specific target.
 */
public interface TargetRef {
    /**
     * Returns the target of this event; returns {@code null} if there is no target.
     */
    public Character getTarget();
}
