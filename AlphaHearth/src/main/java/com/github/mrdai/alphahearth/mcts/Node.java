package com.github.mrdai.alphahearth.mcts;

import com.github.mrdai.alphahearth.Board;
import com.github.mrdai.alphahearth.move.Move;

import java.util.ArrayList;
import java.util.List;

public class Node {
    private final Node parent;
    final Move move;

    final List<Node> unvisitedChildren = new ArrayList<>();
    public final List<Node> visitedChildren = new ArrayList<>();
    boolean expanded;

    public int gameCount;
    public double reward;

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
            unvisitedChildren.add(new Node(this, move));
        expanded = true;
    }

    /**
     * Back propagates the new score through the tree.
     */
    public void backPropagate(double score) {
        gameCount++;
        reward += score;

        if (parent != null)
            parent.backPropagate(score);
    }
}
