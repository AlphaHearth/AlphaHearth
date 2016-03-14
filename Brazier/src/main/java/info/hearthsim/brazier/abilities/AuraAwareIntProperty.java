package info.hearthsim.brazier.abilities;

import info.hearthsim.brazier.Silencable;
import info.hearthsim.brazier.actions.undo.UndoableUnregisterAction;
import info.hearthsim.brazier.actions.undo.UndoAction;

/**
 * Aura-aware int property, implemented by using an underlying {@link AuraAwarePropertyBase}.
 * Fields {@code baseValue} and {@code minValue} are also included in this class, where {@code baseValue}
 * will be used to computed the buffed value of this property and {@code minValue} designates the minimum
 * of buffed value.
 */
public final class AuraAwareIntProperty implements Silencable {
    private final int baseValue;
    private final int minValue;

    private final AuraAwarePropertyBase<IntPropertyBuff> impl;

    /**
     * Creates a {@code AuraAwareIntProperty} with the given {@code baseValue} and setting {@code minValue}
     * to {@link Integer#MIN_VALUE}.
     */
    public AuraAwareIntProperty(int baseValue) {
        this(baseValue, Integer.MIN_VALUE);
    }

    /**
     * Creates a {@code AuraAwareIntProperty} with the given {@code baseValue} and {@code minValue}.
     */
    public AuraAwareIntProperty(int baseValue, int minValue) {
        this.baseValue = baseValue;
        this.minValue = minValue;

        this.impl = new AuraAwarePropertyBase<>((buffs) -> {
            return (prev) -> {
                int result = prev;
                for (AuraAwarePropertyBase.BuffRef<IntPropertyBuff> buffRef: buffs) {
                    result = buffRef.getBuff().buffProperty(result);
                }
                return result;
            };
        });
    }

    private AuraAwareIntProperty(AuraAwareIntProperty other) {
        this.baseValue = other.baseValue;
        this.minValue = other.minValue;
        this.impl = other.impl.copy();
    }

    /**
     * Returns a new copy of this {@code AuraAwareIntProperty}.
     */
    public AuraAwareIntProperty copy() {
        return new AuraAwareIntProperty(this);
    }

    /**
     * Adds a new removable buff to this {@code AuraAwareIntProperty} which sets its buffed value to the
     * given value.
     */
    public UndoableUnregisterAction setValueTo(int newValue) {
        return addBuff((prev) -> newValue);
    }

    /**
     * Adds a new removable buff to this {@code AuraAwareIntProperty} which adds the property with the given value.
     */
    public UndoableUnregisterAction addBuff(int toAdd) {
        return addBuff((prev) -> prev + toAdd);
    }

    /**
     * Adds a new removable buff to this {@code AuraAwareIntProperty} which adds the property with the given value.
     */
    public UndoableUnregisterAction addBuff(IntPropertyBuff toAdd) {
        return addBuff(BuffArg.NORMAL_BUFF, toAdd);
    }

    /**
     * Adds a new removable external buff to this {@code AuraAwareIntProperty} which adds the property
     * with the given value.
     */
    public UndoableUnregisterAction addExternalBuff(int toAdd) {
        return addExternalBuff((prev) -> prev + toAdd);
    }

    /**
     * Adds a new removable external buff to this {@code AuraAwareIntProperty} which adds the property
     * with the given value.
     */
    public UndoableUnregisterAction addExternalBuff(IntPropertyBuff toAdd) {
        return addBuff(BuffArg.NORMAL_AURA_BUFF, toAdd);
    }

    /**
     * Adds a new removable external buff to this {@code AuraAwareIntProperty} which adds the property
     * with the given value.
     */
    public UndoableUnregisterAction setValueTo(BuffArg arg, int newValue) {
        return addBuff(arg, (prev) -> newValue);
    }

    /**
     * Adds a new removable buff to this {@code AuraAwareIntProperty} with the given {@code priority},
     * {@code external} (contained in a {@code BuffArg}) and value to add.
     */
    public UndoableUnregisterAction addBuff(BuffArg arg, int toAdd) {
        return addBuff(arg, (prev) -> prev + toAdd);
    }

    /**
     * Adds a new removable buff to this {@code AuraAwareIntProperty} with the given {@code priority},
     * {@code external} (contained in a {@code BuffArg}) and value to add.
     */
    public UndoableUnregisterAction addBuff(BuffArg arg, IntPropertyBuff toAdd) {
        return impl.addRemovableBuff(arg, toAdd);
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
    public int getValue() {
        int result = impl.getCombinedView().buffProperty(baseValue);
        return result >= minValue ? result : minValue;
    }
}
