package com.github.mrdai.alphahearth.mcts.budget;

import com.github.mrdai.alphahearth.mcts.MCSAgent;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Computational {@link Budget} defined on times of iterations, i.e. terminating the search when certain
 * times of iterations have been simulated.
 */
public class IterCountBudget implements Budget {
    private final int iterLimit;
    private final AtomicInteger currentNum;

    /**
     * Creates a {@code IterCountBudget} with the given limit of iteration times. The created instance
     * will signal the {@link MCSAgent} to stop when the time of iterations reaches the given value.
     *
     * @param iterLimit the given limit of iteration.
     */
    public IterCountBudget(int iterLimit) {
        this.iterLimit = iterLimit;
        this.currentNum = new AtomicInteger();
    }

    @Override
    public void startSearch() {
        currentNum.set(0);
    }

    @Override
    public void newIteration() {
        currentNum.getAndIncrement();
    }

    @Override
    public boolean hasReached() {
        return currentNum.get() >= iterLimit;
    }
}
