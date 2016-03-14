package info.hearthsim.brazier;

/**
 * Interface, works as markers of properties of a {@link World}, with
 * the sole un-implemented method {@link #getWorld()}, which returns
 * the owner {@code World} instance of this property.
 */
public interface WorldProperty {
    /**
     * Returns the owner {@code World} instance of this {@code WorldProperty}
     *
     * @return the owner {@code World} instance of this {@code WorldProperty}
     */
    public World getWorld();
}
