package info.hearthsim.brazier.abilities;

import info.hearthsim.brazier.World;
import info.hearthsim.brazier.actions.undo.UndoAction;

/**
 * An active aura. Compare to {@link Aura} which can only be used to describe the effect of
 * a certain aura, {@code TargetedActiveAura} can represent a complete in-game aura.
 *
 * @see TargetedActiveAura
 */
public interface ActiveAura {
    /**
     * Updates the entities in the given {@link World} with this {@code ActiveAura}.
     */
    public UndoAction updateAura(World world);

    /**
     * Deactivates the aura.
     */
    public UndoAction deactivate();
}
