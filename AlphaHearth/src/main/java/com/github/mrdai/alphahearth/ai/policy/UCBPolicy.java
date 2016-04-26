package com.github.mrdai.alphahearth.ai.policy;

import com.github.mrdai.alphahearth.ai.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link TreePolicy} which selects the child with the highest Upper Confidence Bound.
 * <p>
 * The constructor of this class can receive a parameter {@code cp}, which stands for the exploration factor
 * of the UCT formulation. Assigning it with a greater value emphasizes the exploration of the MCTS, while
 * setting it to a lesser value emphasizes the exploitation. It is set to {@code 1 / sqrt(2)} by default.
 */
public class UCBPolicy implements TreePolicy {
    private static final Logger LOG = LoggerFactory.getLogger(UCBPolicy.class);

    private final double cp;

    /**
     * Creates a {@code UCBPolicy} with its Exploration Factor setting to {@code 1 / sqrt(2)}.
     */
    public UCBPolicy() {
        this(1 / Math.sqrt(2));
    }

    /**
     * Creates a {@code UCBPolicy} with the given Exploration Factor.
     */
    public UCBPolicy(double cp) {
        this.cp = cp;
    }

    @Override
    public Node bestChild(Node node) {
        double currentMax = Double.MAX_VALUE * -1;
        Node maxNode = null;
        for (Node child : node.visitedChildren) {
            double uct = child.reward / child.gameCount
                         + 2 * cp * Math.sqrt(2 * Math.log(node.gameCount) / child.gameCount);
            if (uct > currentMax) {
                currentMax = uct;
                maxNode = child;
            }
        }
        return maxNode == null ? node.visitedChildren.peekFirst() : maxNode;
    }
}
