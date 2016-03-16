package info.hearthsim.brazier.abilities;

import info.hearthsim.brazier.Game;
import info.hearthsim.brazier.actions.undo.UndoAction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.jtrim.utils.ExceptionHelper;

/**
 * Buffs that do not disappear until the owner is silenced. For temporary buff that
 * last only in the current turn, see {@link Buff}.
 * <p>
 * For predefined {@code PermanentBuff}s, see {@link Buffs}.
 *
 * @see Buffs
 */
public interface PermanentBuff<Target> {
    /**
     * Adds the permanent buff to the given target in the given game with
     * the given arguments in {@link BuffArg}.
     */
    public UndoAction buff(Game game, Target target, BuffArg arg);

    /**
     * Merges the given collection of {@link PermanentBuff}s to one which added all of the merged buffs
     * to the given target instantly.
     *
     * @throws NullPointerException if any of the given buffs is {@code null}.
     */
    public static <Target> PermanentBuff<Target> merge(
            Collection<? extends PermanentBuff<? super Target>> buffs) {
        ExceptionHelper.checkNotNullElements(buffs, "buffs");

        if (buffs.isEmpty()) {
            return (game, actor, arg) -> UndoAction.DO_NOTHING;
        }

        List<? extends PermanentBuff<? super Target>> buffsCopy = new ArrayList<>(buffs);

        return (Game game, Target target, BuffArg arg) -> {
            UndoAction.Builder result = new UndoAction.Builder(buffsCopy.size());
            for (PermanentBuff<? super Target> buff: buffsCopy) {
                result.addUndo(buff.buff(game, target, arg));
            }
            return result;
        };
    }
}
