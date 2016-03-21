package info.hearthsim.brazier;

import info.hearthsim.brazier.actions.undo.UndoObjectAction;
import info.hearthsim.brazier.actions.undo.UndoableUnregisterAction;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import info.hearthsim.brazier.events.RegisterId;
import org.jtrim.utils.ExceptionHelper;

/**
 * Container of flags, which can be used to registered any thing.
 */
public final class FlagContainer {
    private final Set<Object> flags = new HashSet<>();

    /**
     * Returns a copy of this {@code FlagContainer}.
     */
    public FlagContainer copy() {
        FlagContainer result = new FlagContainer();
        result.flags.addAll(flags);
        return result;
    }

    /**
     * Returns if the given flag has already been registered in this container.
     */
    public boolean hasFlag(Object flag) {
        ExceptionHelper.checkNotNullArgument(flag, "flag");
        return flags.contains(flag);
    }

    /**
     * Registers the given flag in this container.
     *
     * @param flag the given flag.
     * @return an {@link UndoableUnregisterAction} which can be used to unregister the given flag.
     */
    public UndoObjectAction<FlagContainer> registerFlag(Object flag) {
        ExceptionHelper.checkNotNullArgument(flag, "flag");
        flags.add(flag);
        return (fc) -> fc.flags.remove(flag);
    }
}
