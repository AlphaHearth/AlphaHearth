package info.hearthsim.brazier.abilities;

import info.hearthsim.brazier.game.Silencable;
import info.hearthsim.brazier.util.UndoAction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Implementation base for aura aware properties, including {@link AuraAwareBoolProperty} and
 * {@link AuraAwareIntProperty}. Aura-aware property class can be implemented by using an underlying
 * {@code AuraAwarePropertyBase}, to which all buffs management can be delegate.
 * <p>
 * An {@code AuraAwarePropertyBase} can be instantiated by providing a customized {@link AuraAwarePropertyBase.BuffCombiner},
 * which determines how the {@code AuraAwarePropertyBase} evaluates the buffed value. The {@code AuraAwarePropertyBase}
 * itself will maintain a list of added buffs, which are organised in the decreasing order of their registered priority,
 * and the list will be used as the parameter of the sole un-implemented method of {@code BuffCombiner} each time
 * a buffed value evaluation is needed. The method {@link AuraAwarePropertyBase.BuffCombiner#viewCombinedBuffs(Collection)}
 * combines the given collection of added buffs together and the result can be used to evaluate the buffed value.
 */
public final class AuraAwarePropertyBase<T> implements Silencable {
    private final BuffCombiner<T> buffCombiner;
    private final T combinedView;
    private final List<BuffRef<T>> buffRefs;

    public AuraAwarePropertyBase(BuffCombiner<T> buffCombiner) {
        this.buffCombiner = buffCombiner;
        this.buffRefs = new ArrayList<>();
        this.combinedView = buffCombiner.viewCombinedBuffs(Collections.unmodifiableList(this.buffRefs));
    }

    private AuraAwarePropertyBase(AuraAwarePropertyBase<T> other, boolean copyExternal) {
        this.buffCombiner = other.buffCombiner;
        this.buffRefs = new ArrayList<>(other.buffRefs.size());
        this.combinedView = other.buffCombiner.viewCombinedBuffs(Collections.unmodifiableList(this.buffRefs));
        for (BuffRef<T> buffRef: other.buffRefs) {
            if (copyExternal || !buffRef.external) {
                this.buffRefs.add(buffRef);
            }
        }
    }

    /**
     * Added the given buff to this {@code AuraAwarePropertyBase}.
     */
    public UndoAction<AuraAwarePropertyBase> addRemovableBuff(BuffArg buffArg, T toAdd) {
        int priority = buffArg.getPriority();
        boolean external = buffArg.isExternal();

        int buffPos = findInsertPos(priority);

        BuffRef<T> buffRef = new BuffRef<>(priority, external, toAdd);
        buffRefs.add(buffPos, buffRef);
        return (aapb) -> aapb.buffRefs.remove(buffRef);
    }

    private int findInsertPos(int priority) {
        for (int i = buffRefs.size() - 1; i >= 0; i--) {
            BuffRef<?> buffRef = buffRefs.get(i);
            if (buffRef.priority <= priority) {
                return i + 1;
            }
        }
        return 0;
    }

    /**
     * Returns a new copy of this {@code AuraAwarePropertyBase}.
     */
    public AuraAwarePropertyBase<T> copy() {
        return copy(false);
    }

    public AuraAwarePropertyBase<T> copy(boolean copyExternal) {
        return new AuraAwarePropertyBase<>(this, copyExternal);
    }

    /**
     * Returns if there is any external buff in this {@code AuraAwarePropertyBase}.
     */
    private boolean hasNonExternalBuff() {
        for (BuffRef<?> buffRef: buffRefs) {
            if (!buffRef.external) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void silence() {
        if (!hasNonExternalBuff())
            return;

        List<BuffRef<T>> prevRefs = new ArrayList<>(buffRefs);
        buffRefs.clear();
        for (BuffRef<T> buffRef: prevRefs) {
            if (buffRef.external) {
                buffRefs.add(buffRef);
            }
        }
    }

    public T getCombinedView() {
        return combinedView;
    }

    /**
     * Functional interface with the sole un-implemented method {@link #viewCombinedBuffs(Collection)},
     * which combines the given collection of {@link BuffRef}s to one single object.
     */
    public static interface BuffCombiner <T> {
        /**
         * Combines the given collection of buffs together and returns.
         */
        public T viewCombinedBuffs(Collection<? extends BuffRef<T>> buffs);
    }

    /**
     * Reference to a buff added to this {@code AuraAwarePropertyBase}, also includes its {@code priority} and
     * {@code external} fields given when it's added.
     */
    public static final class BuffRef<T> {
        private final int priority;
        private final boolean external;
        private final T buff;

        public BuffRef(int priority, boolean external, T buff) {
            this.priority = priority;
            this.external = external;
            this.buff = buff;
        }

        public int getPriority() {
            return priority;
        }

        public boolean isExternal() {
            return external;
        }

        public T getBuff() {
            return buff;
        }
    }
}
