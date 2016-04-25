package com.github.mrdai.alphahearth.ai.policy;

import com.github.mrdai.alphahearth.ai.Node;

import java.util.List;

/**
 * A {@code TreePolicy} is used to select or create a leaf node from the nodes already contained
 * within the search tree in the <em>Selection</em> and <em>Expansion</em> stage.
 */
public interface TreePolicy {

    /**
     * Selects the best node from the given list of {@link Node}s.
     *
     * @param nodes the given list of {@code Node}s.
     * @return the best node from the given list of {@code Node}s.
     */
    public Node bestNode(List<Node> nodes);
}
