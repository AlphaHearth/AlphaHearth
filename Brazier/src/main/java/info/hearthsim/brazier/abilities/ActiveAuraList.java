package info.hearthsim.brazier.abilities;

import info.hearthsim.brazier.actions.undo.UndoableUnregisterAction;
import info.hearthsim.brazier.World;
import info.hearthsim.brazier.actions.undo.UndoAction;

import java.util.ArrayList;
import java.util.List;

import org.jtrim.utils.ExceptionHelper;

/**
 * List of {@link ActiveAura}, providing methods {@link #addAura(ActiveAura)} and {@link #updateAllAura(World)}
 * to manage a group of {@code ActiveAura}s.
 */
public final class ActiveAuraList {
    private final List<AuraWrapper> auras;

    public ActiveAuraList() {
        this.auras = new ArrayList<>();
    }

    /**
     * Removes the element wit the given value from the given list, and returns its index.
     */
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
     * Adds (registers) a new {@code ActiveAura} to this {@code ActiveAuraList}.
     */
    public UndoableUnregisterAction addAura(ActiveAura aura) {
        // We wrap "aura" to ensure that we remove the one
        // added by this method call in the returned reference.
        AuraWrapper auraWrapper = new AuraWrapper(aura);
        auras.add(auraWrapper);

        return UndoableUnregisterAction.makeIdempotent(new UndoableUnregisterAction() {
            @Override
            public UndoAction unregister() {
                int prevIndex = removeAndGetIndex(auras, auraWrapper);
                UndoAction deactivateUndo = auraWrapper.deactivate();

                if (prevIndex >= 0) {
                    return () -> {
                        deactivateUndo.undo();
                        auras.add(prevIndex, auraWrapper);
                    };
                } else {
                    return deactivateUndo;
                }
            }

            @Override
            public void undo() {
                removeAndGetIndex(auras, auraWrapper);
            }
        });
    }

    /**
     * Updates all {@code ActiveAura} added to this list with the given world by calling the
     * {@link ActiveAura#updateAura(World)} method for each of them.
     */
    public UndoAction updateAllAura(World world) {
        if (auras.isEmpty()) {
            return UndoAction.DO_NOTHING;
        }

        UndoAction.Builder builder = new UndoAction.Builder(auras.size());

        // Copy the list to ensure that it does not change during iteration
        for (AuraWrapper aura: new ArrayList<>(auras)) {
            builder.addUndo(aura.updateAura(world));
        }

        return builder;
    }

    private static final class AuraWrapper {
        private final ActiveAura aura;

        public AuraWrapper(ActiveAura aura) {
            ExceptionHelper.checkNotNullArgument(aura, "aura");
            this.aura = aura;
        }

        public UndoAction updateAura(World world) {
            return aura.updateAura(world);
        }

        public UndoAction deactivate() {
            return aura.deactivate();
        }
    }
}
