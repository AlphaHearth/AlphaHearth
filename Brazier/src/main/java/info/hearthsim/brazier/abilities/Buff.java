package info.hearthsim.brazier.abilities;

import info.hearthsim.brazier.game.Entity;
import info.hearthsim.brazier.util.UndoAction;

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
public interface Buff<Target extends Entity> {
    /**
     * Adds the temporary buff to the given target in the given game with the given arguments in {@link BuffArg}.
     */
    public UndoAction<Target> buff(Target target, BuffArg arg);

    public default PermanentBuff<Target> toPermanent() {
        return this::buff;
    }

    /**
     * Merges the given collection of {@link Buff}s to one which added all of the merged buffs to the given target
     * instantly.
     *
     * @throws NullPointerException if any of the given buffs is {@code null}.
     */
    public static <Target extends Entity> Buff<Target> merge(
            Collection<? extends Buff<? super Target>> buffs) {
        ExceptionHelper.checkNotNullElements(buffs, "buffs");

        if (buffs.isEmpty()) {
            return (actor, arg) -> UndoAction.DO_NOTHING;
        }

        List<? extends Buff<? super Target>> buffsCopy = new ArrayList<>(buffs);

        return (Target target, BuffArg arg) -> {
            UndoAction.Builder<Target> result = new UndoAction.Builder<>(buffsCopy.size());
            for (Buff<? super Target> buff: buffsCopy) {
                result.add(buff.buff(target, arg));
            }
            return result;
        };
    }
}
