package info.hearthsim.brazier;

import info.hearthsim.brazier.actions.undo.UndoAction;

public interface DestroyableEntity extends BornEntity {
    public UndoAction scheduleToDestroy();
    public boolean isScheduledToDestroy();
    public UndoAction destroy();
}
