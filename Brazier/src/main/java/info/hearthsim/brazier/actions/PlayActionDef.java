package info.hearthsim.brazier.actions;

import info.hearthsim.brazier.Player;
import info.hearthsim.brazier.World;
import info.hearthsim.brazier.cards.PlayAction;
import java.util.Collection;

import info.hearthsim.brazier.actions.undo.UndoAction;
import org.jtrim.utils.ExceptionHelper;

/**
 * Definition of a {@link PlayAction}, combined with its {@link TargetNeed} and {@link PlayActionRequirement}.
 */
// TODO refactor this bizarre PlayAction framework.
public final class PlayActionDef<Actor> {

    private final TargetNeed targetNeed;
    private final PlayActionRequirement requirement;
    private final PlayAction<Actor> action;

    /**
     * Creates a new {@code PlayActionDef} with the given {@link TargetNeed}, {@link PlayActionRequirement}
     * and {@link PlayAction}.
     *
     * @param targetNeed the given {@code TargetNeed}.
     * @param requirement the given {@code PlayActionRequirement}.
     * @param action the given {@code PlayAction}.
     *
     * @throws NullPointerException if any of the given parameters is {@code null}.
     */
    public PlayActionDef(
            TargetNeed targetNeed,
            PlayActionRequirement requirement,
            PlayAction<Actor> action) {
        ExceptionHelper.checkNotNullArgument(targetNeed, "targetNeed");
        ExceptionHelper.checkNotNullArgument(requirement, "requirement");
        ExceptionHelper.checkNotNullArgument(action, "action");

        this.targetNeed = targetNeed;
        this.requirement = requirement;
        this.action = action;
    }

    public static <Actor> TargetNeed combineNeeds(Player player, Collection<? extends PlayActionDef<Actor>> actions) {
        TargetNeed result = TargetNeeds.NO_NEED;
        for (PlayActionDef<?> action: actions) {
            if (action.getRequirement().meetsRequirement(player)) {
                result = result.combine(action.getTargetNeed());
            }
        }
        return result;
    }

    /**
     * Return the {@link TargetNeed} of this {@code PlayActionDef}.
     */
    public TargetNeed getTargetNeed() {
        return targetNeed;
    }

    /**
     * Return the {@link PlayActionRequirement} of this {@code PlayActionDef}.
     */
    public PlayActionRequirement getRequirement() {
        return requirement;
    }

    /**
     * Return the {@link PlayAction} of this {@code PlayActionDef}.
     */
    public PlayAction<Actor> getAction() {
        return action;
    }

    /**
     * Plays the {@link PlayAction} defined in this {@code PlayActionDef} with the
     * given {@link World} and {@link PlayArg}. The method has the same effect as
     * calling the underlying {@link PlayAction#doPlay(World, PlayArg)} method.
     *
     * @param world the given {@code World}.
     * @param arg the given {@code PlayArg}.
     */
    public UndoAction doPlay(World world, PlayArg<Actor> arg) {
        return action.doPlay(world, arg);
    }

    @Override
    public String toString() {
        return "PlayCardActionDef{" + "targetNeed=" + targetNeed + ", action=" + action + '}';
    }
}
