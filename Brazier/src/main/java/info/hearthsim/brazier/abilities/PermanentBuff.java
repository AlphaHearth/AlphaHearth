package info.hearthsim.brazier.abilities;

import info.hearthsim.brazier.game.Entity;
import info.hearthsim.brazier.util.UndoAction;
import org.jtrim.utils.ExceptionHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Buffs that do not disappear until the owner is silenced. For temporary buff that
 * last only in the current turn, see {@link Buff}.
 * <p>
 * For predefined {@code PermanentBuff}s, see {@link Buffs}.
 *
 * @see Buffs
 */
public interface PermanentBuff<Target extends Entity> {
    /**
     * Adds the permanent buff to the given target in the given game with
     * the given arguments in {@link BuffArg}.
     */
    public UndoAction<Target> buff(Target target, BuffArg arg);

    /**
     * Merges the given collection of {@link PermanentBuff}s to one which added all of the merged buffs
     * to the given target instantly.
     *
     * @throws NullPointerException if any of the given buffs is {@code null}.
     */
    public static <Target extends Entity> PermanentBuff<Target> merge(
            Collection<? extends PermanentBuff<? super Target>> buffs) {
        ExceptionHelper.checkNotNullElements(buffs, "buffs");

        if (buffs.isEmpty()) {
            return (actor, arg) -> UndoAction.DO_NOTHING;
        }

        List<? extends PermanentBuff<? super Target>> buffsCopy = new ArrayList<>(buffs);

        return (Target target, BuffArg arg) -> {
            UndoAction.Builder<Target> result = new UndoAction.Builder<>(buffsCopy.size());
            for (PermanentBuff<? super Target> buff: buffsCopy) {
                result.add(buff.buff(target, arg));
            }
            return result;
        };
    }
}
