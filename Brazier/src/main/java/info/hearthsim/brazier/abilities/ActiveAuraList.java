package info.hearthsim.brazier.abilities;

import info.hearthsim.brazier.Game;
import info.hearthsim.brazier.GameProperty;
import info.hearthsim.brazier.actions.undo.UndoObjectAction;
import info.hearthsim.brazier.actions.undo.UndoableUnregisterAction;
import info.hearthsim.brazier.actions.undo.UndoAction;

import java.util.ArrayList;
import java.util.List;

import org.jtrim.utils.ExceptionHelper;

/**
 * List of {@link ActiveAura}, providing methods {@link #addAura(ActiveAura)} and {@link #updateAllAura()}
 * to manage a group of {@code ActiveAura}s.
 */
public final class ActiveAuraList implements GameProperty {
    private final Game game;
    private final List<AuraWrapper> auras;

    public ActiveAuraList(Game game) {
        this.game = game;
        this.auras = new ArrayList<>();
    }

    /**
     * Returns a copy of this {@code ActiveAuraList}.
     */
    public ActiveAuraList copyFor(Game newGame) {
        ActiveAuraList result = new ActiveAuraList(newGame);
        for (AuraWrapper aura : auras)
            result.addAura(aura.aura.copyFor(newGame));
        return result;
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
    public UndoObjectAction<ActiveAuraList> addAura(ActiveAura aura) {
        // We wrap "aura" to ensure that we remove the one
        // added by this method call in the returned reference.
        AuraWrapper auraWrapper = new AuraWrapper(aura);
        auras.add(auraWrapper);

        return UndoObjectAction.toIdempotent((aal) -> {
            auraWrapper.deactivate(aal);
            aal.removeAndGetIndex(aal.auras, auraWrapper);
        });
    }

    /**
     * Updates all {@code ActiveAura} added to this list with the given game by calling the
     * {@link ActiveAura#applyAura(Game)} method for each of them.
     */
    public void updateAllAura() {
        if (auras.isEmpty())
            return;

        // Copy the list to ensure that it does not change during iteration
        for (AuraWrapper aura: new ArrayList<>(auras))
            aura.updateAura(this);
    }

    @Override
    public Game getGame() {
        return game;
    }

    private static final class AuraWrapper {
        private final ActiveAura aura;

        public AuraWrapper(ActiveAura aura) {
            ExceptionHelper.checkNotNullArgument(aura, "aura");
            this.aura = aura;
        }

        public void updateAura(ActiveAuraList list) {
            aura.applyAura(list.getGame());
        }

        public void deactivate(ActiveAuraList list) {
            aura.deactivate(list.getGame());
        }
    }
}
