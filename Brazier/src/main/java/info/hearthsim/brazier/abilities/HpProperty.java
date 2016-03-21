package info.hearthsim.brazier.abilities;

import info.hearthsim.brazier.Priorities;
import info.hearthsim.brazier.actions.undo.UndoObjectAction;
import info.hearthsim.brazier.Silencable;

/**
 * A helpful manager class for the health point property of a certain character, which can be nasty in HearthStone.
 * <p>
 * The class provides:
 * <ul>
 *     <li>{@link #addAuraBuff(int)} and {@link #buffHp(int)} to add aura and buffs to the property;</li>
 *     <li>{@link #getCurrentHp()} and {@link #getMaxHp()} to get current values;</li>
 *     <li>{@link #isDamaged()} and {@link #isDead()} to checks the state of the property's owner.</li>
 * </ul>
 */
public final class HpProperty implements Silencable {
    private final int baseMaxValue;

    private int buffedMaxHp;
    private int auraBuff;

    private int currentMaxHp;
    private int currentHp;

    /**
     * Creates a new {@code HpProperty} with the given maximum hp value.
     */
    public HpProperty(int maxValue) {
        this.baseMaxValue = maxValue;
        this.currentMaxHp = maxValue;
        this.buffedMaxHp = maxValue;
        this.auraBuff = 0;
        this.currentHp = maxValue;
    }

    /**
     * Applies the added auras to this {@code HpProperty} by re-evaluating its current and maximum value.
     */
    public UndoObjectAction<HpProperty> applyAura() {
        int newMaxHp = buffedMaxHp + auraBuff;
        if (newMaxHp == currentMaxHp) {
            return null;
        }

        int prevMaxHp = currentMaxHp;
        int prevCurrentHp = currentHp;

        if (newMaxHp > currentMaxHp) {
            currentHp = currentHp + (newMaxHp - currentMaxHp);
        }
        currentHp = Math.min(newMaxHp, currentHp);
        currentMaxHp = newMaxHp;

        return (hp) -> {
            hp.currentHp = prevCurrentHp;
            hp.currentMaxHp = prevMaxHp;
        };
    }

    /**
     * Returns a new copy of this {@code HpProperty}.
     */
    public HpProperty copy() {
        HpProperty result = new HpProperty(baseMaxValue);

        result.buffedMaxHp = buffedMaxHp;
        result.auraBuff = 0;

        result.currentMaxHp = buffedMaxHp;
        result.currentHp = Math.max(1, currentHp - auraBuff);
        return result;
    }

    /**
     * Sets the current hp to the given value. If the given value is larger than the maximum hp,
     * the current hp will be set to the maximum hp.
     */
    public UndoObjectAction<HpProperty> setCurrentHp(int newHp) {
        if (newHp == currentHp)
            return null;

        int prevCurrentHp = currentHp;
        currentHp = Math.min(getMaxHp(), newHp);
        return (hp) -> hp.currentHp = prevCurrentHp;
    }

    /**
     * Buffs the hp with the given amount.
     */
    public UndoObjectAction<HpProperty> buffHp(BuffArg arg, int amount) {
        if (arg.isExternal()) {
            if (arg.getPriority() != Priorities.HIGH_PRIORITY) {
                throw new UnsupportedOperationException("Unsupported aura priority: " + arg.getPriority());
            }
            return addAuraBuff(amount);
        }
        else {
            if (arg.getPriority() != Priorities.NORMAL_PRIORITY) {
                throw new UnsupportedOperationException("Unsupported buff priority: " + arg.getPriority());
            }
            return buffHp(amount);
        }
    }

    /**
     * Buffs the hp with the given amount.
     */
    public UndoObjectAction<HpProperty> buffHp(int amount) {
        if (amount == 0)
            return null;

        currentHp += amount;
        UndoObjectAction<HpProperty> maxHpUndo = setMaxHp(buffedMaxHp + amount);

        return (hp) -> {
            maxHpUndo.undo(hp);
            hp.currentHp -= amount;
        };
    }

    /**
     * Adds an aura buff with the given amount to this property. The added aura will change nothing until the
     * {@link #applyAura()} is called.
     */
    public UndoObjectAction<HpProperty> addAuraBuff(int amount) {
        auraBuff += amount;

        return (hp) -> hp.auraBuff -= amount;
    }

    /**
     * Returns the maximum hp.
     */
    public int getMaxHp() {
        return currentMaxHp;
    }

    /**
     * Returns the current hp.
     */
    public int getCurrentHp() {
        return currentHp;
    }

    /**
     * Returns if the owner of this property is dead (current hp is less than or equals to 0).
     */
    public boolean isDead() {
        return getCurrentHp() <= 0;
    }

    /**
     * Silences the property by setting the maximum hp to the base value.
     */
    @Override
    public void silence() {
        setMaxHp(baseMaxValue);
    }

    /**
     * Sets the max and current hp to the given value.
     */
    public UndoObjectAction<HpProperty> setMaxAndCurrentHp(int newValue) {
        int prevBuffedMaxHp = buffedMaxHp;
        int prevCurrentHp = currentHp;
        int prevCurrentMaxHp = currentMaxHp;

        buffedMaxHp = newValue;
        currentMaxHp = newValue;
        currentHp = newValue;

        return (hp) -> {
            hp.currentMaxHp = prevCurrentMaxHp;
            hp.currentHp = prevCurrentHp;
            hp.buffedMaxHp = prevBuffedMaxHp;
        };
    }

    /**
     * Sets the max hp to the given value. The current value will be set to the given value
     * if the given value is less than the current value.
     */
    public UndoObjectAction<HpProperty> setMaxHp(int newValue) {
        int prevBuffedMaxHp = buffedMaxHp;
        int prevCurrentHp = currentHp;
        int prevCurrentMaxHp = currentMaxHp;

        buffedMaxHp = newValue;
        currentMaxHp = newValue;
        currentHp = Math.min(currentHp, newValue);

        return (hp) -> {
            hp.currentMaxHp = prevCurrentMaxHp;
            hp.currentHp = prevCurrentHp;
            hp.buffedMaxHp = prevBuffedMaxHp;
        };
    }

    /**
     * Returns if the owner of this property is damaged (current hp is less than max hp).
     */
    public boolean isDamaged() {
        return currentHp < getMaxHp();
    }
}
