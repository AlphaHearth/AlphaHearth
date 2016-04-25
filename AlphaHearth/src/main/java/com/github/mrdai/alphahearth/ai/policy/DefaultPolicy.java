package com.github.mrdai.alphahearth.ai.policy;

import com.github.mrdai.alphahearth.Agent;
import com.github.mrdai.alphahearth.Board;
import com.github.mrdai.alphahearth.move.Move;

/**
 * A {@code DefaultPolicy} is used to play out the domain from a given non-terminal state
 * to produce a value estimate (simulation). Essentially, its sole un-implemented method
 * {@link #produceMode(Board)} produces the next {@link Move} for the simulation with the
 * given {@link Board}.
 */
public interface DefaultPolicy extends Agent {

    /**
     * Produces the next {@link Move} for the simulation with the given {@link Board}.
     *
     * @param board the given {@code Board}.
     * @return the next {@code Move} for the simulation
     */
    public Move produceMode(Board board);

}
