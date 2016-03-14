package info.hearthsim.brazier;

/**
 * Interface, works as markers of properties of a {@link Player}, with
 * the sole un-implemented method {@link #getOwner()}, which returns
 * the owner {@code Player} instance of this property.
 */
public interface PlayerProperty extends WorldProperty {
    /**
     * Returns the owner {@code Player} instance of this {@code WorldProperty}
     *
     * @return the owner {@code Player} instance of this {@code WorldProperty}
     */
    public Player getOwner();

    /**
     * {@inheritDoc}
     *
     * @implSpec
     * The default implementation returns the owner {@code World} of the result
     * of {@link #getOwner()}.
     *
     * @return {@inheritDoc}
     */
    @Override
    public default World getWorld() {
        return getOwner().getWorld();
    }
}
