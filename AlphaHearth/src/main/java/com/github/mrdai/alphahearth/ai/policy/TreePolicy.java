package com.github.mrdai.alphahearth.ai.policy;

import com.github.mrdai.alphahearth.ai.Node;

/**
 * A {@code TreePolicy} is used to select or create a leaf node from the nodes already contained
 * within the search tree in the <em>Selection</em> and <em>Expansion</em> stage.
 */
public interface TreePolicy {

    /**
     * Selects the best child from all the visited children of the given {@link Node}.
     *
     * @param node the given {@code Node}.
     * @return the best child from the visited children of the given {@code Node}.
     */
    public Node bestChild(Node node);
}
