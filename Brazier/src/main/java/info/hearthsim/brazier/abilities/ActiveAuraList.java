package info.hearthsim.brazier.abilities;

import info.hearthsim.brazier.Game;
import info.hearthsim.brazier.GameProperty;
import info.hearthsim.brazier.util.UndoAction;

import java.util.ArrayList;
import java.util.List;

import org.jtrim.utils.ExceptionHelper;

/**
 * List of {@link ActiveAura}, providing methods {@link #addAura(ActiveAura, boolean)} and {@link #updateAllAura()}
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
        for (AuraWrapper aura : auras) {
            if (aura.toCopy)
                result.addAura(aura.aura.copyFor(newGame, null), true);
        }
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
    public UndoAction<ActiveAuraList> addAura(ActiveAura aura, boolean toCopy) {
        // We wrap "aura" to ensure that we remove the one
        // added by this method call in the returned reference.
        AuraWrapper auraWrapper = new AuraWrapper(aura, toCopy);
        auras.add(auraWrapper);

        return (aal) -> {
            for (AuraWrapper a : aal.auras) {
                if (a.aura.getEntityId() == aura.getEntityId()) {
                    a.deactivate();
                    aal.auras.remove(a);
                    return;
                }
            }
        };
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
            aura.updateAura();
    }

    @Override
    public Game getGame() {
        return game;
    }

    private final class AuraWrapper {
        private final ActiveAura aura;
        private final boolean toCopy;

        public AuraWrapper(ActiveAura aura, boolean toCopy) {
            ExceptionHelper.checkNotNullArgument(aura, "aura");
            this.aura = aura;
            this.toCopy = toCopy;
        }

        public void updateAura() {
            aura.applyAura(getGame());
        }

        public void deactivate() {
            aura.deactivate(getGame());
        }
    }
}
