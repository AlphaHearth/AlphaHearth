package info.hearthsim.brazier.abilities;

import info.hearthsim.brazier.World;
import info.hearthsim.brazier.actions.undo.UndoableUnregisterAction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jtrim.utils.ExceptionHelper;

/**
 * Functional interface with its sole un-implemented method {@link #applyAura(World, Object, Object)}, which
 * applies the aura on the given source and target in the given world.
 * <p>
 * For predefined {@code Aura}s, see {@link Auras}.
 *
 * @see Auras
 */
public interface Aura<Source, Target> {
    /**
     * Applies the aura on the given source and target in the given world.
     */
    public UndoableUnregisterAction applyAura(World world, Source source, Target target);

    /**
     * Merges the given collection of {@link Aura}s together.
     *
     * @throws NullPointerException if any of the given {@code Aura}s is {@code null}.
     */
    public static <Source, Target> Aura<Source, Target>
    merge(Collection<? extends Aura<? super Source, ? super Target>> auras) {
        ExceptionHelper.checkNotNullElements(auras, "auras");

        if (auras.isEmpty()) {
            return (world, source, target) -> UndoableUnregisterAction.DO_NOTHING;
        }

        List<Aura<? super Source, ? super Target>> aurasCopy = new ArrayList<>(auras);

        return (world, source, target) -> {
            UndoableUnregisterAction.Builder builder = new UndoableUnregisterAction.Builder(aurasCopy.size());
            for (Aura<? super Source, ? super Target> aura: aurasCopy) {
                builder.addRef(aura.applyAura(world, source, target));
            }
            return builder;
        };
    }
}
