package info.hearthsim.brazier.actions;

import info.hearthsim.brazier.game.GameProperty;
import org.jtrim.utils.ExceptionHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Actions in which the given actor alters the given {@code Game} in some way. Usually used
 * as a functional interface with its sole un-implemented method {@link #apply(Actor)}.
 * <p>
 * Instances of {@code TargetlessAction} must be <b>immutable</b>: no state can be stored. Using
 * the interface as a functional interface and implementing it with lambda expression is highly
 * recommended.
 * <p>
 * For predefined {@code TargetlessAction}s, see {@link TargetlessActions}.
 *
 * @see TargetlessActions
 */
@FunctionalInterface
public interface TargetlessAction<Actor extends GameProperty> extends GameObjectAction<Actor> {
    public static final TargetlessAction DO_NOTHING = (actor) -> {};

    /**
     * Alters the game with the given {@code Actor}.
     *
     * @param actor the given {@code Actor}.
     */
    @Override
    public void apply(Actor actor);

    /**
     * Converts this {@code TargetlessAction} to a {@link TargetedAction}.
     */
    public default TargetedAction<Actor, Object> toTargetedAction() {
        return (Actor actor, Object target) -> apply(actor);
    }

    /**
     * Merges the given collection of {@link TargetlessActions} to one which executes all the given
     * actions sequentially.
     *
     * @throws NullPointerException if any of the given actions is {@code null}.
     */
    public static <Actor extends GameProperty> TargetlessAction<Actor> merge(
            Collection<? extends TargetlessAction<? super Actor>> actions) {
        ExceptionHelper.checkNotNullElements(actions, "actions");

        if (actions.isEmpty())
            return TargetlessAction.DO_NOTHING;

        List<TargetlessAction<? super Actor>> actionsCopy = new ArrayList<>(actions);

        return (Actor actor) -> {
            for (TargetlessAction<? super Actor> action: actionsCopy) {
                action.apply(actor);
            }
        };
    }
}
