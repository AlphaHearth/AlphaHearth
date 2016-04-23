package com.github.mrdai.alphahearth.mcts;

import com.github.mrdai.alphahearth.Board;
import com.github.mrdai.alphahearth.move.Move;

import java.util.LinkedList;
import java.util.List;

public class Node {
    private static final double PLY_PENALTY = 0.95;

    private final Node parent;
    final Move move;

    final LinkedList<Node> unvisitedChildren = new LinkedList<>();
    public final LinkedList<Node> visitedChildren = new LinkedList<>();
    boolean expanded;

    public double gameCount = 0;
    public double reward = 0;

    /**
     * Creates a root {@code Node} with the given {@link Board}.
     */
    public Node() {
        this.parent = null;
        this.move = null;
    }

    /**
     * Creates a non-root {@code Node} with the given {@link Move} resulting to this {@code Node}
     * and its parent {@code Node}.
     */
    public Node(Node parent, Move move) {
        this.parent = parent;
        this.move = move;
    }

    /**
     * Expands this {@code Node} with the given {@link List} of {@link Move}s.
     * All new children will be added to {@code unvisitedChildren}.
     *
     * @param moves the given {@code List} of {@code Move}s.
     */
    public void expand(List<Move> moves) {
        for (Move move : moves)
            unvisitedChildren.addLast(new Node(this, move));
        expanded = true;
    }

    /**
     * Back propagates the new score through the tree.
     */
    public void backPropagate(double score) {
        gameCount++;
        reward += score;

        if (parent != null)
            parent.backPropagate(PLY_PENALTY * score);
    }
}
