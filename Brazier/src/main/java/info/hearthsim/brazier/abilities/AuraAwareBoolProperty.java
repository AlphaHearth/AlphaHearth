package info.hearthsim.brazier.abilities;

import info.hearthsim.brazier.Silencable;
import info.hearthsim.brazier.actions.undo.UndoAction;
import info.hearthsim.brazier.actions.undo.UndoableUnregisterAction;

/**
 * Aura-aware boolean property, implemented by using an underlying {@link AuraAwarePropertyBase},
 * includes field {@code baseValue} which will be used to computed the buffed value of this property.
 */
public final class AuraAwareBoolProperty implements Silencable {
    private final boolean baseValue;
    private final AuraAwarePropertyBase<BoolPropertyBuff> impl;

    public AuraAwareBoolProperty(boolean baseValue) {
        this.baseValue = baseValue;

        this.impl = new AuraAwarePropertyBase<>((buffs) -> {
            return (prev) -> {
                boolean result = prev;
                for (AuraAwarePropertyBase.BuffRef<BoolPropertyBuff> buffRef: buffs) {
                    result = buffRef.getBuff().buffProperty(result);
                }
                return result;
            };
        });
    }

    private AuraAwareBoolProperty(AuraAwareBoolProperty other) {
        this.baseValue = other.baseValue;
        this.impl = other.impl.copy();
    }

    /**
     * Returns a new copy of this {@code AuraAwareBoolProperty}.
     */
    public AuraAwareBoolProperty copy() {
        return new AuraAwareBoolProperty(this);
    }

    /**
     * Adds a new removable buff to this {@code AuraAwareBoolProperty} which sets the buffed value to the given value.
     */
    public UndoableUnregisterAction setValueTo(boolean newValue) {
        return addRemovableBuff((prev) -> newValue);
    }

    /**
     * Adds a new removable external buff to this {@code AuraAwareBoolProperty} which sets the buffed value to
     * the given value.
     */
    public UndoableUnregisterAction setValueToExternal(boolean newValue) {
        return addRemovableBuff(BuffArg.NORMAL_AURA_BUFF, (prev) -> newValue);
    }

    /**
     * Adds a new removable buff to this {@code AuraAwareBoolProperty}.
     */
    public UndoableUnregisterAction addRemovableBuff(BoolPropertyBuff toAdd) {
        return addRemovableBuff(BuffArg.NORMAL_BUFF, toAdd);
    }

    /**
     * Adds a new removable external buff to this {@code AuraAwareBoolProperty}.
     */
    public UndoableUnregisterAction addExternalBuff(BoolPropertyBuff toAdd) {
        return addRemovableBuff(BuffArg.NORMAL_AURA_BUFF, toAdd);
    }

    /**
     * Adds a new removable buff to this {@code AuraAwareBoolProperty} which sets the buffed value to the given value.
     */
    public UndoableUnregisterAction setValueTo(BuffArg buffArg, boolean newValue) {
        return addRemovableBuff(buffArg, (prev) -> newValue);
    }

    /**
     * Adds a new removable buff to this {@code AuraAwareBoolProperty}.
     */
    public UndoableUnregisterAction addRemovableBuff(BuffArg buffArg, BoolPropertyBuff toAdd) {
        return impl.addRemovableBuff(buffArg, toAdd);
    }

    /**
     * Silences the property by removing all added buffs.
     */
    @Override
    public UndoAction silence() {
        return impl.silence();
    }

    /**
     * Returns the buffed value of this property.
     */
    public boolean getValue() {
        return impl.getCombinedView().buffProperty(baseValue);
    }
}
