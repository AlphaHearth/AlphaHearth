package info.hearthsim.brazier.abilities;

import info.hearthsim.brazier.Silencable;
import info.hearthsim.brazier.actions.undo.UndoAction;
import info.hearthsim.brazier.actions.undo.UndoableUnregisterAction;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

import org.jtrim.utils.ExceptionHelper;

/**
 * Buffable boolean property, which can be created with a given {@link BooleanSupplier}, adds buffs
 * via {@link #addBuff(BoolPropertyBuff)} and evaluates buffed value via {@link #getValue()}.
 */
public final class BuffableBoolProperty implements Silencable {
    private final BooleanSupplier baseValue;
    private List<BoolPropertyBuff> buffs;
    private Boolean topBuff;

    /**
     * Creates a {@link BuffableBoolProperty} with the given {@link BooleanSupplier}, whose return value
     * will be used as the base value for buffed value evaluation.
     */
    public BuffableBoolProperty(BooleanSupplier baseValue) {
        ExceptionHelper.checkNotNullArgument(baseValue, "baseValue");

        this.baseValue = baseValue;
        // We do not use RefList because its element references
        // are not serializable.
        this.buffs = new ArrayList<>();
        this.topBuff = null;
    }

    /**
     * Returns a new copy of this {@link BuffableBoolProperty} with the new given base value supplier.
     * This method is useful when you try to copy all the buffs added to this {@link BuffableBoolProperty}.
     */
    public BuffableBoolProperty copy(BooleanSupplier newBaseValue) {
        BuffableBoolProperty result = new BuffableBoolProperty(newBaseValue);
        result.buffs.addAll(buffs);
        return result;
    }

    /**
     * Adds a new buff which set the property to the given value.
     */
    public UndoableUnregisterAction addBuff(boolean value) {
        return addBuff((prevValue) -> value);
    }

    private <T> int removeAndGetIndex(List<T> list, T value) {
        for (int i = list.size() - 1; i >= 0; i--) {
            if (value == list.get(i)) {
                list.remove(i);
                return i;
            }
        }
        return -1;
    }

    /**
     * Adds a new given buff.
     */
    public UndoableUnregisterAction addBuff(BoolPropertyBuff buff) {
        ExceptionHelper.checkNotNullArgument(buff, "buff");

        if (topBuff != null) {
            // Note that remove will not restore the extraBuff property
            // but the visible effect is the same.
            boolean currentTopBuff = topBuff;
            topBuff = null;
            addBuffAlways((prevValue) -> currentTopBuff);
        }

        return addBuffAlways(buff);
    }

    private UndoableUnregisterAction addBuffAlways(BoolPropertyBuff buff) {
        // We wrap the buff to ensure that we remove the
        // appropriate buff when requested so.
        BoolPropertyBuffWrapper wrapper = new BoolPropertyBuffWrapper(buff);
        buffs.add(wrapper);
        return () -> {
            int prevIndex = removeAndGetIndex(buffs, wrapper);
            return prevIndex >= 0
                    ? () -> buffs.add(prevIndex, wrapper)
                    : UndoAction.DO_NOTHING;
        };
    }

    /**
     * Silences this {@code BuffableBoolProperty} by removing all added buffs.
     */
    @Override
    public UndoAction silence() {
        if (buffs.isEmpty()) {
            if (topBuff == null) {
                return UndoAction.DO_NOTHING;
            }
            else {
                Boolean prevTopBuff = topBuff;
                topBuff = null;
                return () -> topBuff = prevTopBuff;
            }
        }

        Boolean prevTopBuff = topBuff;
        topBuff = null;

        List<BoolPropertyBuff> prevBuffs = buffs;
        buffs = new ArrayList<>();

        return () -> {
            topBuff = prevTopBuff;
            buffs = prevBuffs;
        };
    }

    /**
     * Returns the buffed value of this property.
     */
    public boolean getValue() {
        if (topBuff != null) {
            return topBuff;
        }

        boolean result = baseValue.getAsBoolean();
        for (BoolPropertyBuff buff: buffs) {
            result = buff.buffProperty(result);
        }
        return result;
    }

    private static final class BoolPropertyBuffWrapper implements BoolPropertyBuff {
        private final BoolPropertyBuff wrapped;

        public BoolPropertyBuffWrapper(BoolPropertyBuff wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public boolean buffProperty(boolean prevValue) {
            return wrapped.buffProperty(prevValue);
        }
    }
}
