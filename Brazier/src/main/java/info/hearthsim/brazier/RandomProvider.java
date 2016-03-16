package info.hearthsim.brazier;

/**
 * A {@code RandomProvider} is responsible of generating random integers with a given range.
 * It is usually used as a functional interface with its sole un-implemented method {@link #roll(int)},
 * and implemented by lambda expression.
 */
@FunctionalInterface
public interface RandomProvider {

    /**
     * Returns a random integer value between {@code 0} (inclusive)
     * and the given value (exclusive).
     *
     * @param bound the upper bound (exclusive) of the random integer
     * @return a random integer, uniformly distributed int value between {@code 0} (inclusive)
     *         and the specified value (exclusive).
     */
    public int roll(int bound);

    /**
     * Returns a random integer value between the given lower bound (inclusive) and upper
     * bound (exclusive).
     *
     * @param minValue the lower bound (inclusive) of the random integer
     * @param maxValue the upper bound (exclusive) of the random integer
     * @return a random integer, uniformly distributed int value between the specified lower
     *         bound (inclusive) and the upper bound (exclusive).
     */
    public default int roll(int minValue, int maxValue) {
        if (minValue == maxValue) {
            return minValue;
        }

        return minValue + roll(maxValue - minValue + 1);
    };
}
