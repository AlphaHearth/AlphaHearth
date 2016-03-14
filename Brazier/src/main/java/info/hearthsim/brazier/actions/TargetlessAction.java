package info.hearthsim.brazier.actions;

import info.hearthsim.brazier.World;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import info.hearthsim.brazier.actions.undo.UndoAction;
import org.jtrim.utils.ExceptionHelper;

/**
 * Actions in which the given actor alters the given {@code World} in some way. Usually used
 * as a functional interface with its sole un-implemented method {@link #alterWorld(World, Object)}.
 * <p>
 * For predefined {@code TargetlessAction}s, see {@link TargetlessActions}.
 *
 * @see TargetlessActions
 */
public interface TargetlessAction<Actor> extends WorldObjectAction<Actor> {
    public static final TargetlessAction<Object> DO_NOTHING = (world, actor) -> UndoAction.DO_NOTHING;

    /**
     * Alters the given {@link World} with the given {@code Actor}.
     *
     * @param world the given {@code World}.
     * @param actor the given {@code Actor}.
     */
    @Override    // Override to provide customized JavaDoc
    public UndoAction alterWorld(World world, Actor actor);

    /**
     * Converts this {@code TargetlessAction} to a {@link TargetedAction}.
     */
    public default TargetedAction<Actor, Object> toTargetedAction() {
        return (World world, Actor actor, Object target) -> alterWorld(world, actor);
    }

    /**
     * Merges the given collection of {@link TargetlessActions} to one which executes all the given
     * actions sequentially.
     *
     * @throws NullPointerException if any of the given actions is {@code null}.
     */
    public static <Actor> TargetlessAction<Actor> merge(
            Collection<? extends TargetlessAction<? super Actor>> actions) {
        ExceptionHelper.checkNotNullElements(actions, "actions");

        if (actions.isEmpty()) {
            return (world, actor) -> UndoAction.DO_NOTHING;
        }

        List<TargetlessAction<? super Actor>> actionsCopy = new ArrayList<>(actions);

        return (World world, Actor actor) -> {
            UndoAction.Builder result = new UndoAction.Builder(actionsCopy.size());
            for (TargetlessAction<? super Actor> action: actionsCopy) {
                result.addUndo(action.alterWorld(world, actor));
            }
            return result;
        };
    }
}
