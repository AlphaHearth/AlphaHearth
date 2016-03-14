package info.hearthsim.brazier.abilities;

/**
 * Functional interface, representing buffs on an {@code int} property, with its sole un-implemented
 * method {@link #buffProperty(int)}, which returns buffed value of the property according to the given
 * previous value.
 */
public interface IntPropertyBuff {
    /**
     * {@code IntPropertyBuff} that doesn't change the value of the property.
     */
    public static final IntPropertyBuff IDENTITY = (prev) -> prev;

    /**
     * Returns the buffed value of the {@code int} property according to the given previous value.
     */
    public int buffProperty(int prevValue);
}
