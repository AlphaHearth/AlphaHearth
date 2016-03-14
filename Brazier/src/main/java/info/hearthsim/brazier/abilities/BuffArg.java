package info.hearthsim.brazier.abilities;

import info.hearthsim.brazier.Priorities;

/**
 * Container class with fields {@code priority} and {@code external}.
 */
public final class BuffArg {
    /** Non-external buff with normal priority. */
    public static final BuffArg NORMAL_BUFF = ownedBuff(Priorities.NORMAL_PRIORITY);
    /** External buff with normal priority. */
    public static final BuffArg NORMAL_AURA_BUFF = externalBuff(Priorities.HIGH_PRIORITY);

    private final int priority;
    private final boolean external;

    /**
     * Creates a {@code BuffArg} with the given {@code priority} and {@code external}.
     */
    public BuffArg(int priority, boolean external) {
        this.priority = priority;
        this.external = external;
    }

    /**
     * Returns an external {@code BuffArg} with the given priority.
     */
    public static BuffArg externalBuff(int priority) {
        return new BuffArg(priority, true);
    }

    /**
     * Returns an non-external {@code BuffArg} with the given priority.
     */
    public static BuffArg ownedBuff(int priority) {
        return new BuffArg(priority, false);
    }

    /**
     * Throws {@code UnsupportedOperationException} if the {@code BuffArg} is {@code external} or its
     * priority is not {@link Priorities#NORMAL_PRIORITY}.
     */
    public void checkNormalBuff() {
        if (external || getPriority() != Priorities.NORMAL_PRIORITY) {
            throw new UnsupportedOperationException("Unsupported buff: " + this);
        }
    }

    /**
     * Returns the priority of the related buff.
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Returns if the related buff is external.
     */
    public boolean isExternal() {
        return external;
    }

    @Override
    public String toString() {
        return "BuffArg{" + "priority=" + priority + ", " + (external ? "external" : "owned") + '}';
    }
}
