package info.hearthsim.brazier.cards;

import info.hearthsim.brazier.*;
import info.hearthsim.brazier.Character;
import info.hearthsim.brazier.actions.PlayArg;
import info.hearthsim.brazier.actions.TargetedAction;

import java.util.Optional;

/**
 * An action of playing something, with a potential target.
 *
 * @param <Actor> the type of the actor.
 */
public interface PlayAction<Actor extends GameProperty> extends TargetedAction<Actor, Optional<Character>> {
    public static final PlayAction DO_NOTHING = (actor, target) -> {};

    /**
     * Executes the {@code PlayAction} with the given {@link Game} and {@link PlayArg}.
     */
    public default void doPlay(PlayArg<Actor> arg) {
        apply(arg.getActor(), arg.getTarget());
    }
}
