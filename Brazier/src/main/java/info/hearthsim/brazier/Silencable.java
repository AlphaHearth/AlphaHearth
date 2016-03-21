package info.hearthsim.brazier;

/**
 * Marker interface, stands for a silencable entity, with a sole un-implemented
 * method {@link #silence()}, which silences the entity.
 */
public interface Silencable {
    /**
     * Silences the entity.
     */
    public void silence();
}
