package info.hearthsim.brazier.actions;

import info.hearthsim.brazier.World;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import info.hearthsim.brazier.actions.undo.UndoAction;
import org.jtrim.utils.ExceptionHelper;

/**
 * Action with target. It is usually used as a functional interface with its sole un-implemented method
 * {@link #alterWorld(World, Actor, Target)}, which alters the given {@link World} with the given {@code actor}
 * and {@code target}.
 * <p>
 * For predefined {@code TargetedAction}s, see {@link TargetedActions}.
 *
 * @see TargetedActions
 */
public interface TargetedAction<Actor, Target> {
    public static final TargetedAction<Object, Object> DO_NOTHING = (world, actor, target) -> UndoAction.DO_NOTHING;

    /**
     * Alters the given {@link World} with the given {@code Actor} and {@code Target}.
     *
     * @param world the given {@code World}.
     * @param actor the given {@code Actor}.
     * @param target the given {@code Target}.
     */
    public UndoAction alterWorld(World world, Actor actor, Target target);

    /**
     * Merges the given collection of {@code TargetedAction}s to one which executes the
     * {@code TargetedAction}s' {@link #alterWorld(World, Actor, Target)} methods
     * sequentially.
     *
     * @throws NullPointerException if any of the given actions is {@code null}.
     */
    public static <Actor, Target> TargetedAction<Actor, Target> merge(
            Collection<? extends TargetedAction<? super Actor, ? super Target>> actions) {
        ExceptionHelper.checkNotNullElements(actions, "actions");

        if (actions.isEmpty()) {
            return (world, actor, target) -> UndoAction.DO_NOTHING;
        }

        List<TargetedAction<? super Actor, ? super Target>> actionsCopy = new ArrayList<>(actions);

        return (World world, Actor actor, Target target) -> {
            UndoAction.Builder builder = new UndoAction.Builder(actionsCopy.size());
            for (TargetedAction<? super Actor, ? super Target> action: actionsCopy) {
                builder.addUndo(action.alterWorld(world, actor, target));
            }
            return builder;
        };
    }
}
