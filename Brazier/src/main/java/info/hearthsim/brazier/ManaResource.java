package info.hearthsim.brazier;

import info.hearthsim.brazier.actions.undo.UndoAction;

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
     * Refreshes the player's mana. Happens when a new turn starts.
     */
    public UndoAction refresh() {
        int origNextTurnOverload = nextTurnOverload;
        int origOverloadedMana = overloadedMana;
        int origManaCrystals = manaCrystals;
        int origMana = mana;

        manaCrystals = Math.min(Player.MAX_MANA, manaCrystals + 1);
        mana = manaCrystals - nextTurnOverload;
        overloadedMana = nextTurnOverload;
        nextTurnOverload = 0;

        return () -> {
            mana = origMana;
            manaCrystals = origManaCrystals;
            overloadedMana = origOverloadedMana;
            nextTurnOverload = origNextTurnOverload;
        };
    }

    /**
     * Spends designated number of mana and overload.
     */
    public UndoAction spendMana(int toSpend, int overload) {
        if (toSpend == 0) {
            return UndoAction.DO_NOTHING;
        }

        if (toSpend > mana) {
            throw new IllegalStateException("Not enough mana.");
        }

        nextTurnOverload += overload;
        mana -= toSpend;
        return () -> {
            mana += toSpend;
            nextTurnOverload -= overload;
        };
    }

    public int getNextTurnOverload() {
        return nextTurnOverload;
    }

    public UndoAction setNextTurnOverload(int nextTurnOverload) {
        int prevValue = this.nextTurnOverload;
        this.nextTurnOverload = nextTurnOverload;
        return () -> this.nextTurnOverload = prevValue;
    }

    public int getOverloadedMana() {
        return overloadedMana;
    }

    public UndoAction setOverloadedMana(int overloadedMana) {
        int prevValue = this.overloadedMana;
        this.overloadedMana = overloadedMana;
        return () -> this.overloadedMana = prevValue;
    }

    public int getManaCrystals() {
        return manaCrystals;
    }

    public UndoAction setManaCrystals(int manaCrystals) {
        int prevValue = this.manaCrystals;
        this.manaCrystals = Math.min(Player.MAX_MANA, manaCrystals);
        return () -> this.manaCrystals = prevValue;
    }

    public int getMana() {
        return mana;
    }

    public UndoAction setMana(int mana) {
        int prevValue = this.mana;
        this.mana = Math.min(Player.MAX_MANA, mana);
        return () -> this.mana = prevValue;
    }
}
