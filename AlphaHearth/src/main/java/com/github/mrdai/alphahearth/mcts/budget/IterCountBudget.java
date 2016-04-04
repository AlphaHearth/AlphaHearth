package com.github.mrdai.alphahearth.mcts.budget;

import com.github.mrdai.alphahearth.mcts.MCTS;

/**
 * Computational {@link Budget} defined on times of iterations, i.e. terminating the search when certain
 * times of iterations have been simulated.
 */
public class IterCountBudget implements Budget {
    private final int iterLimit;
    private int currentNum;

    /**
     * Creates a {@code IterCountBudget} with the given limit of iteration times. The created instance
     * will signal the {@link MCTS} to stop when the time of iterations reaches the given value.
     *
     * @param iterLimit the given limit of iteration.
     */
    public IterCountBudget(int iterLimit) {
        this.iterLimit = iterLimit;
    }

    @Override
    public void startSearch() {
        currentNum = 0;
    }

    @Override
    public void newIteration() {
        currentNum++;
    }

    @Override
    public boolean hasReached() {
        return currentNum >= iterLimit;
    }
}
