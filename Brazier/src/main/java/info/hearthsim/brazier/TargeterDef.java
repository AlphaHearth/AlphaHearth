package info.hearthsim.brazier;

import java.util.Objects;
import org.jtrim.utils.ExceptionHelper;

/**
 * Definition of the targeter of a targeting event (pointing a targeted spell or attackable minion to a target),
 * including fields:
 * <ul>
 *     <li>{@code playerId}: the {@link PlayerId} of the targeter;</li>
 *     <li>
 *         {@code hero}: {@code boolean} field, representing if the target is being targeted from a hero by
 *         hero power or spell card;
 *     </li>
 *     <li>
 *         {@code directAttack}: {@code boolean} field, representing if the target is being targeted by a direct
 *         attack attempt.
 *     </li>
 * </ul>
 */
public final class TargeterDef {
    private final PlayerId playerId;
    private final boolean hero;
    private final boolean directAttack;

    /**
     * Creates a new {@code TargeterDef} with the given {@code playerId}, {@code hero} and
     * {@code directAttack}.
     */
    public TargeterDef(PlayerId playerId, boolean hero, boolean directAttack) {
        ExceptionHelper.checkNotNullArgument(playerId, "playerId");

        this.playerId = playerId;
        this.hero = hero;
        this.directAttack = directAttack;
    }

    /**
     * Returns if the {@code playerId} of this {@code TargeterDef} is the same as the {@link PlayerId} of the
     * given {@link PlayerProperty}'s owner.
     */
    public boolean hasSameOwner(PlayerProperty property) {
        return Objects.equals(playerId, property.getOwner().getPlayerId());
    }

    /**
     * Returns {@code playerId}.
     */
    public PlayerId getPlayerId() {
        return playerId;
    }

    /**
     * Returns {@code hero}.
     */
    public boolean isHero() {
        return hero;
    }

    /**
     * Returns {@code directAttack}.
     */
    public boolean isDirectAttack() {
        return directAttack;
    }
}
