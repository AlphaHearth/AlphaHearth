package info.hearthsim.brazier.events;

import info.hearthsim.brazier.PlayerProperty;
import info.hearthsim.brazier.World;
import info.hearthsim.brazier.actions.undo.UndoAction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jtrim.utils.ExceptionHelper;

public interface WorldEventAction<Self extends PlayerProperty, EventSource> {
    public static final WorldEventAction<PlayerProperty, Object> DO_NOTHING
            = (world, self, eventSource) -> UndoAction.DO_NOTHING;

    public UndoAction alterWorld(World world, Self self, EventSource eventSource);

    public static <Self extends PlayerProperty, EventSource> WorldEventAction<? super Self, ? super EventSource> merge(
            Collection<? extends WorldEventAction<? super Self, ? super EventSource>> actions) {

        int filterCount = actions.size();
        if (filterCount == 0) {
            return (world, self, eventSource) -> UndoAction.DO_NOTHING;
        }
        if (filterCount == 1) {
            return actions.iterator().next();
        }

        List<WorldEventAction<? super Self, ? super EventSource>> actionsCopy = new ArrayList<>(actions);
        ExceptionHelper.checkNotNullElements(actionsCopy, "actions");

        return (World world, Self owner, EventSource eventSource) -> {
            UndoAction.Builder result = new UndoAction.Builder(actionsCopy.size());
            for (WorldEventAction<? super Self, ? super EventSource> action: actionsCopy) {
                result.addUndo(action.alterWorld(world, owner, eventSource));
            }
            return result;
        };
    }
}
