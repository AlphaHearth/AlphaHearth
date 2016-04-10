package info.hearthsim.brazier.actions;

import info.hearthsim.brazier.game.Character;

import java.util.Optional;

import info.hearthsim.brazier.game.Game;
import info.hearthsim.brazier.game.GameProperty;
import org.jtrim.utils.ExceptionHelper;

/**
 * The arguments of acting (playing) something. Essentially, a {@code PlayArg} contains:
 * <ul>
 *     <li>{@code actor}: the source of the action;</li>
 *     <li>
 *         {@code target}: the potential target of the action, represented as
 *         a {@link Optional} of {@link Character}.
 *     </li>
 * </ul>
 */
public final class PlayArg<Actor extends GameProperty> implements GameProperty {
    private final Actor actor;
    private final Optional<Character> target;

    public PlayArg(Actor actor, Character target) {
        this(actor, Optional.ofNullable(target));
    }

    /**
     * Creates a {@code PlayArg} with the given {@code actor} and {@code target}.
     *
     * @param actor the given {@code actor}.
     * @param target the potential {@code target}.
     *
     * @throws NullPointerException if any of the given parameters is {@code null}.
     */
    public PlayArg(Actor actor, Optional<Character> target) {
        ExceptionHelper.checkNotNullArgument(actor, "actor");
        ExceptionHelper.checkNotNullArgument(target, "target");

        this.actor = actor;
        this.target = target;
    }

    /**
     * Returns the {@code actor}.
     */
    public Actor getActor() {
        return actor;
    }

    /**
     * Returns the {@code target}.
     */
    public Optional<Character> getTarget() {
        return target;
    }

    @Override
    public Game getGame() {
        return actor.getGame();
    }
}
