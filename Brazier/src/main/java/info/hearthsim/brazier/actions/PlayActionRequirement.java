package info.hearthsim.brazier.actions;

import info.hearthsim.brazier.game.Player;
import org.jtrim.utils.ExceptionHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Requirement of a play action, usually used as a functional interface. The sole un-implemented
 * method {@link #meetsRequirement(Player)} can be used to test if the given player meets the requirement
 * or not.
 * <p>
 * For predefined {@code PlayActionRequirement}s, see {@link PlayActionRequirements}.
 *
 * @see PlayActionRequirements
 */
public interface PlayActionRequirement {
    /**
     * Returns a {@code PlayActionRequirement} whose {@link #meetsRequirement(Player)} method
     * always return {@code true}.
     */
    public static final PlayActionRequirement ALLOWED = (player) -> true;

    /**
     * Returns if the given player meets the requirement.
     */
    public boolean meetsRequirement(Player player);

    /**
     * Merges the given collection of {@code PlayActionRequirement} into one {@code PlayActionRequirement},
     * which tests the given {@link Player} with every {@code PlayActionRequirement} and returns {@code true}
     * only if it passed all of them.
     */
    public static PlayActionRequirement merge(Collection<? extends PlayActionRequirement> requirements) {
        List<PlayActionRequirement> requirementsCopy = new ArrayList<>(requirements);
        ExceptionHelper.checkNotNullElements(requirementsCopy, "requirements");

        return (player) -> {
            for (PlayActionRequirement requirement: requirementsCopy) {
                if (!requirement.meetsRequirement(player)) {
                    return false;
                }
            }
            return true;
        };
    }
}
