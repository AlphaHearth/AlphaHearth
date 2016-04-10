package info.hearthsim.brazier.game;

/**
 * A util agent of managing a player's mana.
 */
public final class ManaResource {
    private int nextTurnOverload;
    private int overloadedMana;
    private int manaCrystals;
    private int mana;

    public ManaResource() {
        this.nextTurnOverload = 0;
        this.overloadedMana = 0;
        this.manaCrystals = 0;
        this.mana = 0;
    }

    /**
     * Returns a copy of this {@code ManaResource}.
     */
    public ManaResource copy() {
        ManaResource result = new ManaResource();
        result.nextTurnOverload = this.nextTurnOverload;
        result.overloadedMana = this.overloadedMana;
        result.manaCrystals = this.manaCrystals;
        result.mana = this.mana;
        return result;
    }

    /**
     * Refreshes the player's mana at start of turn.
     */
    public void refresh() {
        manaCrystals = Math.min(Player.MAX_MANA, manaCrystals + 1);
        mana = Math.max(0, manaCrystals - nextTurnOverload);
        overloadedMana = nextTurnOverload;
        nextTurnOverload = 0;
    }

    /**
     * Spends designated number of mana and overload.
     *
     * @throws IllegalStateException if there is not enough mana.
     */
    public void spendMana(int toSpend, int overload) {
        if (toSpend <= 0 && overload <= 0)
            return;

        if (toSpend > mana)
            throw new IllegalStateException("Not enough mana. Expecting to spend " + toSpend
            + " mana, but there is only " + mana + " mana left.");

        nextTurnOverload += overload;
        mana -= toSpend;
    }

    /**
     * Returns the number of overloaded crystals for the next turn.
     */
    public int getNextTurnOverload() {
        return nextTurnOverload;
    }

    /**
     * Sets the number of overloaded crystals for the next turn to the given value.
     */
    public void setNextTurnOverload(int nextTurnOverload) {
        if (nextTurnOverload < 0)
            return;
        this.nextTurnOverload = nextTurnOverload;
    }

    /**
     * Returns the number of overloaded crystals for this turn.
     */
    public int getOverloadedMana() {
        return overloadedMana;
    }

    /**
     * Sets the number of overloaded crystals for the this turn to the given value.
     */
    public void setOverloadedMana(int overloadedMana) {
        if (overloadedMana < 0)
            return;
        this.overloadedMana = overloadedMana;
    }

    /**
     * Returns the number of mana crystals.
     */
    public int getManaCrystals() {
        return manaCrystals;
    }

    /**
     * Sets the number of mana crystals to the given value.
     */
    public void setManaCrystals(int manaCrystals) {
        this.manaCrystals = Math.min(Player.MAX_MANA, manaCrystals);
    }

    /**
     * Returns the number of available mana crystals.
     */
    public int getMana() {
        return mana;
    }

    /**
     * Sets the number of available mana crystals to the given value for this turn.
     */
    public void setMana(int mana) {
        this.mana = Math.min(Player.MAX_MANA, mana);
    }
}
