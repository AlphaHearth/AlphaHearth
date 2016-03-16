package info.hearthsim.brazier.abilities;

import info.hearthsim.brazier.Game;
import info.hearthsim.brazier.actions.undo.UndoAction;

/**
 * An active aura. Compare to {@link Aura} which can only be used to describe the effect of
 * a certain aura, {@code TargetedActiveAura} can represent a complete in-game aura.
 *
 * @see TargetedActiveAura
 */
public interface ActiveAura {
    /**
     * Updates the entities in the given {@link Game} with this {@code ActiveAura}.
     */
    public UndoAction applyAura(Game game);

    /**
     * Deactivates the aura.
     */
    public UndoAction deactivate();
}
