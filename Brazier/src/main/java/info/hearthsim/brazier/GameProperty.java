package info.hearthsim.brazier;

/**
 * Interface, works as markers of properties of a {@link Game}, with
 * the sole un-implemented method {@link #getGame()}, which returns
 * the owner {@code Game} instance of this property.
 */
public interface GameProperty {
    /**
     * Returns the owner {@code Game} instance of this {@code GameProperty}
     *
     * @return the owner {@code Game} instance of this {@code GameProperty}
     */
    public Game getGame();
}
