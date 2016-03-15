package info.hearthsim.brazier.cards;

import info.hearthsim.brazier.*;
import info.hearthsim.brazier.Character;
import info.hearthsim.brazier.actions.PlayArg;
import info.hearthsim.brazier.actions.TargetedAction;
import info.hearthsim.brazier.actions.undo.UndoAction;

import java.util.Optional;

/**
 * An action of playing something, with a potential target.
 *
 * @param <Actor> the type of the actor.
 */
public interface PlayAction<Actor> extends TargetedAction<Actor, Optional<Character>> {
    /**
     * Executes the {@code PlayAction} with the given {@link World} and {@link PlayArg}.
     */
    public default UndoAction doPlay(World world, PlayArg<Actor> arg) {
        return alterWorld(world, arg.getActor(), arg.getTarget());
    }

    /**
     * Returns a {@link PlayAction} which does nothing.
     */
    public static <Actor> PlayAction<Actor> doNothing() {
        return (World world, Actor actor, Optional<info.hearthsim.brazier.Character> target) -> {
            return UndoAction.DO_NOTHING;
        };
    }
}
