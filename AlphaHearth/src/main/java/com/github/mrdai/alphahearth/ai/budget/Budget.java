package com.github.mrdai.alphahearth.ai.budget;

import com.github.mrdai.alphahearth.ai.mcs.MCSAgent;

/**
 * Computational budget, used to tell a MCTS when to stop. Typically, the computational budget can be defined
 * on time, memory or iteration constraint. When it is reached, the search will be halted and the best performing
 * root action returned.
 * <p>
 * A {@code Budget} should guarantee to be <em>thread-safe</em> as the search method may run iterations in
 * parallel.
 */
public interface Budget {

    /**
     * The {@link MCSAgent} will invoke this method to signal the start of a new search.
     * Typically, the {@code Budget} should reset all its inner states when this method is invoked.
     */
    public void startSearch();

    /**
     * The {@link MCSAgent} will invoke this method to signal the start of a new iteration of MCTS.
     */
    public void newIteration();

    /**
     * The {@link MCSAgent} will invoke this method every time an iteration ends, and determine if to
     * terminate the search based on the value returned.
     *
     * @return {@code true} is the computational budget is reached and the search should be terminated;
     *         {@code false} otherwise.
     */
    public boolean hasReached();

}
