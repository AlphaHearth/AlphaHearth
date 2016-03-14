package info.hearthsim.brazier.abilities;

/**
 * Functional interface, representing buffs on an {@code boolean} property, with its sole un-implemented
 * method {@link #buffProperty(boolean)}, which returns buffed value of the property according to the given
 * previous value.
 */
public interface BoolPropertyBuff {
    /**
     * Returns the buffed value of the {@code boolean} property according to the given previous value.
     */
    public boolean buffProperty(boolean prevValue);
}
