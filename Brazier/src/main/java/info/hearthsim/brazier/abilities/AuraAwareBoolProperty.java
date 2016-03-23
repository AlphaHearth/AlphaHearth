package info.hearthsim.brazier.abilities;

import info.hearthsim.brazier.Silencable;
import info.hearthsim.brazier.util.UndoAction;

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
        this.impl = other.impl.copy(false);
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
    public UndoAction<AuraAwareBoolProperty> setValueTo(boolean newValue) {
        return addRemovableBuff((prev) -> newValue);
    }

    /**
     * Adds a new removable external buff to this {@code AuraAwareBoolProperty} which sets the buffed value to
     * the given value.
     */
    public UndoAction<AuraAwareBoolProperty> setValueToExternal(boolean newValue) {
        return addRemovableBuff(BuffArg.NORMAL_AURA_BUFF, (prev) -> newValue);
    }

    /**
     * Adds a new removable buff to this {@code AuraAwareBoolProperty}.
     */
    public UndoAction<AuraAwareBoolProperty> addRemovableBuff(BoolPropertyBuff toAdd) {
        return addRemovableBuff(BuffArg.NORMAL_BUFF, toAdd);
    }

    /**
     * Adds a new removable external buff to this {@code AuraAwareBoolProperty}.
     */
    public UndoAction<AuraAwareBoolProperty> addExternalBuff(BoolPropertyBuff toAdd) {
        return addRemovableBuff(BuffArg.NORMAL_AURA_BUFF, toAdd);
    }

    /**
     * Adds a new removable buff to this {@code AuraAwareBoolProperty} which sets the buffed value to the given value.
     */
    public UndoAction<AuraAwareBoolProperty> setValueTo(BuffArg buffArg, boolean newValue) {
        return addRemovableBuff(buffArg, (prev) -> newValue);
    }

    /**
     * Adds a new removable buff to this {@code AuraAwareBoolProperty}.
     */
    public UndoAction<AuraAwareBoolProperty> addRemovableBuff(BuffArg buffArg, BoolPropertyBuff toAdd) {
        UndoAction<AuraAwarePropertyBase> undoRef = impl.addRemovableBuff(buffArg, toAdd);
        return (aabp) -> undoRef.undo(aabp.impl);
    }

    /**
     * Silences the property by removing all added buffs.
     */
    @Override
    public void silence() {
        impl.silence();
    }

    /**
     * Returns the buffed value of this property.
     */
    public boolean getValue() {
        return impl.getCombinedView().buffProperty(baseValue);
    }
}
