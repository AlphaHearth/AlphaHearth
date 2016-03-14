package info.hearthsim.brazier.abilities;

/**
 * Buff for an owned int property, which changes the buffed value based on the owner and the previous value.
 */
public interface OwnedIntPropertyBuff<T> {
    public static final OwnedIntPropertyBuff<Object> IDENTITY = (owner, prev) -> prev;

    /**
     * Evaluates the buffed value of the int property based on the given owner and previous value.
     */
    public int buffProperty(T owner, int prevValue);
}
