package info.hearthsim.brazier.actions;

import info.hearthsim.brazier.game.Game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jtrim.utils.ExceptionHelper;

/**
 * Action with target. It is usually used as a functional interface with its sole un-implemented method
 * {@link #apply(Actor, Target)}, which alters the given {@link Game} with the given {@code actor}
 * and {@code target}.
 * <p>
 * For predefined {@code TargetedAction}s, see {@link TargetedActions}.
 *
 * @see TargetedActions
 */
public interface TargetedAction<Actor, Target> {
    public static final TargetedAction DO_NOTHING = (actor, target) -> {};

    /**
     * Alters the game with the given {@code Actor} and {@code Target}.
     *
     * @param actor the given {@code Actor}.
     * @param target the given {@code Target}.
     */
    public void apply(Actor actor, Target target);

    /**
     * Merges the given collection of {@code TargetedAction}s to one which executes the
     * {@code TargetedAction}s' {@link #apply(Actor, Target)} methods
     * sequentially.
     *
     * @throws NullPointerException if any of the given actions is {@code null}.
     */
    public static <Actor, Target> TargetedAction<Actor, Target> merge(
            Collection<? extends TargetedAction<? super Actor, ? super Target>> actions) {
        ExceptionHelper.checkNotNullElements(actions, "actions");

        if (actions.isEmpty())
            return DO_NOTHING;

        List<TargetedAction<? super Actor, ? super Target>> actionsCopy = new ArrayList<>(actions);

        return (Actor actor, Target target) -> {
            for (TargetedAction<? super Actor, ? super Target> action: actionsCopy)
                action.apply(actor, target);
        };
    }
}
