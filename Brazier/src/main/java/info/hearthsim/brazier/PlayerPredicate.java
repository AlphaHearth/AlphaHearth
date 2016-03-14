package info.hearthsim.brazier;

import java.util.function.Predicate;

/**
 * Functional interface, which is similar to {@link Predicate} and provide a {@link PlayerId} as the
 * additional parameter of the {@link #test(PlayerId, T)} method.
 * @param <T> the type of another parameter in {@link #test(PlayerId, T)} method.
 */
public interface PlayerPredicate<T> {
    /** {@link PlayerPredicate} which always returns {@code true} */
    public static final PlayerPredicate<Object> ANY = (playerId, arg) -> true;
    /** {@link PlayerPredicate} which always returns {@code false} */
    public static final PlayerPredicate<Object> NONE = (playerId, arg) -> false;

    /**
     * Returns if the given {@link PlayerId} and the argument satisfy this {@code PlayerPredicate}.
     */
    public boolean test(PlayerId playerId, T arg);

    /**
     * Combines this {@code PlayerPredicate} and another. A new {@code PlayerPredicate} will be returned.
     */
    public default PlayerPredicate<T> and(PlayerPredicate<? super T> other) {
        return (playerId, arg) -> this.test(playerId, arg) && other.test(playerId, arg);
    }

    /**
     * Converts the {@code PlayerPredicate<T>} to a {@link Predicate} with the given {@link PlayerId}.
     *
     * @param playerId the given {@link PlayerId}.
     * @return the equivalent {@link Predicate} with the given {@link PlayerId}.
     */
    public default Predicate<T> toPredicate(PlayerId playerId) {
        return (arg) -> test(playerId, arg);
    }
}
