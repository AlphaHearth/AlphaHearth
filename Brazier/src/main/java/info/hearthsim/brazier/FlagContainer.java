package info.hearthsim.brazier;

import info.hearthsim.brazier.actions.undo.UndoableUnregisterAction;
import java.util.HashMap;
import java.util.Map;
import org.jtrim.utils.ExceptionHelper;

/**
 * Container of flags, which can be used to registered any thing.
 */
public final class FlagContainer {
    private final Map<Object, Integer> flags;

    public FlagContainer() {
        this.flags = new HashMap<>();
    }

    /**
     * Returns if the given flag has already been registered in this container.
     */
    public boolean hasFlag(Object flag) {
        ExceptionHelper.checkNotNullArgument(flag, "flag");
        return flags.containsKey(flag);
    }

    /**
     * Registers the given flag in this container.
     *
     * @param flag the given flag.
     * @return an {@link UndoableUnregisterAction} which can be used to unregister the given flag.
     */
    public UndoableUnregisterAction registerFlag(Object flag) {
        ExceptionHelper.checkNotNullArgument(flag, "flag");

        int newValue = flags.compute(flag, (key, prevValue) -> prevValue != null ? prevValue + 1 : 1);
        int prevValue = newValue - 1;

        return UndoableUnregisterAction.makeIdempotent(() -> {
            Integer currentValue;
            if (prevValue > 0) {
                currentValue = flags.put(flag, prevValue);
            } else {
                currentValue = flags.remove(flag);
            }
            return () -> {
                if (currentValue != null) {
                    flags.put(flag, currentValue);
                } else {
                    flags.remove(flag);
                }
            };
        });
    }
}
