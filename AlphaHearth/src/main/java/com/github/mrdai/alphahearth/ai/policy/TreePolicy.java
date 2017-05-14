package com.github.mrdai.alphahearth.ai.policy;

import com.github.mrdai.alphahearth.ai.Node;

/**
 * A {@code TreePolicy} is used to select or create a leaf node from the nodes already contained
 * within the search tree in the <em>Selection</em> and <em>Expansion</em> stage.
 */
public interface TreePolicy {

    /**
     * Selects and returns the best visited child of the given {@link Node}.
     *
     * @param node the given {@code Node}.
     * @return the best visited child of the given {@code Node}s.
     */
    public Node bestChild(Node node);
}
