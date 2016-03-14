package info.hearthsim.brazier;

import info.hearthsim.brazier.actions.undo.UndoAction;

/**
 * Marker interface, stands for a silencable entity, with a sole un-implemented
 * method {@link #silence()}, which silences the entity.
 */
public interface Silencable {
    /**
     * Silences the entity.
     *
     * @return the corresponding {@code UndoAction}
     */
    public UndoAction silence();
}
