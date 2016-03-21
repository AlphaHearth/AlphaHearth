package info.hearthsim.brazier;

/**
 * Util class used to manage minion's freezing. Freezing and unfreezing a minion
 * can be achieved by invoking methods of this class.
 */
public final class FreezeManager implements Silencable {
    private boolean frozen;

    /**
     * Creates a {@code FreezeManager} with its {@code frozen} flag setting to {@code false}.
     */
    public FreezeManager() {
        this(false);
    }

    /**
     * Creates a {@code FreezeManager} with its {@code frozen} flag setting to the given value.
     */
    public FreezeManager(boolean frozen) {
        this.frozen = frozen;
    }

    /**
     * Creates a new {@code FreezeManager} with the same {@code frozen} flag value of this one.
     */
    public FreezeManager copy() {
        return new FreezeManager(frozen);
    }

    /**
     * Returns if the minion is frozen.
     */
    public boolean isFrozen() {
        return frozen;
    }

    /**
     * Unfreezes the minion at the end of a turn if the minion is
     * frozen and did not attack in this turn.
     *
     * @param numberOfAttacks the number of attack the minion dealt in this turn.
     */
    public void endTurn(int numberOfAttacks) {
        if (numberOfAttacks > 0 || !frozen)
            return;

        frozen = false;
    }

    /**
     * Freezes the minion.
     */
    public void freeze() {
        frozen = true;
    }

    /**
     * The minion is silenced and unfrozen.
     */
    @Override
    public void silence() {
        frozen = false;
    }
}
