package com.github.mrdai.alphahearth.mcts.budget;

/**
 * Computational {@link Budget} defined on the length of time used on iterations, i.e. terminating the search
 * when certain time limit is exceeded.
 */
public class TimeBudget implements Budget {
    private final long timeLimit;
    private long beginTime;

    /**
     * Creates a {@code TimeBudget} which will signal the MCTS to stop when the time used on searching
     * has exceeded the given amount of milliseconds.
     *
     * @param timeLimit the time limit for MCTS in milliseconds.
     */
    public TimeBudget(long timeLimit) {
        this.timeLimit = timeLimit;
    }

    @Override
    public void startSearch() {
        beginTime = System.currentTimeMillis();
    }

    @Override
    public void newIteration() {}

    @Override
    public boolean hasReached() {
        return System.currentTimeMillis() - beginTime > timeLimit;
    }
}
