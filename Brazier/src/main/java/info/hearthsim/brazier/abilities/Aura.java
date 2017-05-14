package info.hearthsim.brazier.abilities;

import info.hearthsim.brazier.game.Entity;
import info.hearthsim.brazier.util.UndoAction;
import org.jtrim.utils.ExceptionHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Functional interface with its sole un-implemented method {@link #applyAura(Source, Target)}, which
 * applies the aura on the given source and target in the given {@code Game}.
 * <p>
 * For predefined {@code Aura}s, see {@link Auras}.
 *
 * @see Auras
 */
public interface Aura<Source extends Entity, Target extends Entity> {
    /**
     * Applies the aura on the given source and target in the given game.
     */
    public UndoAction<Target> applyAura(Source source, Target target);

    /**
     * Merges the given collection of {@link Aura}s together.
     *
     * @throws NullPointerException if any of the given {@code Aura}s is {@code null}.
     */
    public static <Source extends Entity, Target extends Entity> Aura<Source, Target>
    merge(Collection<? extends Aura<? super Source, ? super Target>> auras) {
        ExceptionHelper.checkNotNullElements(auras, "auras");

        if (auras.isEmpty()) {
            return (source, target) -> UndoAction.DO_NOTHING;
        }

        List<Aura<? super Source, ? super Target>> aurasCopy = new ArrayList<>(auras);

        return (source, target) -> {
            UndoAction.Builder<Target> builder = new UndoAction.Builder<>(aurasCopy.size());
            for (Aura<? super Source, ? super Target> aura: aurasCopy) {
                builder.add(aura.applyAura(source, target));
            }
            return builder;
        };
    }
}
