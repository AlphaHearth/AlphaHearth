package info.hearthsim.brazier.abilities;

import info.hearthsim.brazier.Game;
import info.hearthsim.brazier.actions.undo.UndoableUnregisterAction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jtrim.utils.ExceptionHelper;

/**
 * Buffs that only last for one turn and disappear when the turn ends.
 * <p>
 * For predefined {@code Buff}s, see {@link Buffs}.
 *
 * @see Buffs
 */
public interface Buff<Target> {
    /**
     * Adds the temporary buff to the given target in the given game with the given arguments in {@link BuffArg}.
     */
    public UndoableUnregisterAction buff(Game game, Target target, BuffArg arg);

    public default PermanentBuff<Target> toPermanent() {
        return (Game game, Target target, BuffArg arg) -> {
            return buff(game, target, arg);
        };
    }

    /**
     * Merges the given collection of {@link Buff}s to one which added all of the merged buffs to the given target
     * instantly.
     *
     * @throws NullPointerException if any of the given buffs is {@code null}.
     */
    public static <Target> Buff<Target> merge(
            Collection<? extends Buff<? super Target>> buffs) {
        ExceptionHelper.checkNotNullElements(buffs, "buffs");

        if (buffs.isEmpty()) {
            return (game, actor, arg) -> UndoableUnregisterAction.DO_NOTHING;
        }

        List<? extends Buff<? super Target>> buffsCopy = new ArrayList<>(buffs);

        return (Game game, Target target, BuffArg arg) -> {
            UndoableUnregisterAction.Builder result = new UndoableUnregisterAction.Builder(buffsCopy.size());
            for (Buff<? super Target> buff: buffsCopy) {
                result.addRef(buff.buff(game, target, arg));
            }
            return result;
        };
    }
}
