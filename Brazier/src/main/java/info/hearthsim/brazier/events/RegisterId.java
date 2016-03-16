package info.hearthsim.brazier.events;

/**
 * A stub class, providing its {@link #hashCode()} method for distinguishing different event notifications
 * registered in a {@link GameEvents}. Such class is necessary for copying self-unregister event
 * notifications.
 */
public class RegisterId {
    private final int hashCode;

    /**
     * Creates a {@code RegisterId} with its {@code hashCode} randomly generated.
     */
    public RegisterId() {
        this(0);
    }

    /**
     * Creates a {@code RegisterId} with its {@code hashCode} being set to the
     * given {@code Object}'s {@code hashCode}.
     */
    public RegisterId(Object object) {
        this(object.hashCode());
    }

    /**
     * Creates a {@code RegisterId} with its {@code hashCode} being set to the
     * given value.
     */
    public RegisterId(int hashCode) {
        this.hashCode = hashCode;
    }

    @Override
    public int hashCode() {
        if (hashCode != 0)
            return hashCode;
        return super.hashCode();
    }

    @Override
    public boolean equals(Object object) {
        return object != null && object instanceof RegisterId && hashCode() == object.hashCode();
    }

}
