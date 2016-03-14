package info.hearthsim.brazier.parsing;

import java.lang.reflect.Type;

/**
 * Functional interface with its sole un-implemented method {@link #checkType(Type)}, which
 * checks the given {@link Type} argument.
 */
public interface TypeChecker {
    public static final TypeChecker DO_NOTHING = (type) -> {};

    /**
     * Checks the given {@link Type} argument and throws {@link ObjectParsingException} if it failed to pass
     * the check.
     */
    public void checkType(Type type) throws ObjectParsingException;
}
