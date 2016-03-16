package info.hearthsim.brazier.actions;

import info.hearthsim.brazier.Game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import info.hearthsim.brazier.actions.undo.UndoAction;
import org.jtrim.utils.ExceptionHelper;

/**
 * Actions in which the given actor alters the given {@code Game} in some way. Usually used
 * as a functional interface with its sole un-implemented method {@link #alterGame(Game, Object)}.
 * <p>
 * For predefined {@code TargetlessAction}s, see {@link TargetlessActions}.
 *
 * @see TargetlessActions
 */
public interface TargetlessAction<Actor> extends GameObjectAction<Actor> {
    public static final TargetlessAction<Object> DO_NOTHING = (game, actor) -> UndoAction.DO_NOTHING;

    /**
     * Alters the given {@link Game} with the given {@code Actor}.
     *
     * @param game the given {@code Game}.
     * @param actor the given {@code Actor}.
     */
    @Override    // Override to provide customized JavaDoc
    public UndoAction alterGame(Game game, Actor actor);

    /**
     * Converts this {@code TargetlessAction} to a {@link TargetedAction}.
     */
    public default TargetedAction<Actor, Object> toTargetedAction() {
        return (Game game, Actor actor, Object target) -> alterGame(game, actor);
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
            return (game, actor) -> UndoAction.DO_NOTHING;
        }

        List<TargetlessAction<? super Actor>> actionsCopy = new ArrayList<>(actions);

        return (Game game, Actor actor) -> {
            UndoAction.Builder result = new UndoAction.Builder(actionsCopy.size());
            for (TargetlessAction<? super Actor> action: actionsCopy) {
                result.addUndo(action.alterGame(game, actor));
            }
            return result;
        };
    }
}
