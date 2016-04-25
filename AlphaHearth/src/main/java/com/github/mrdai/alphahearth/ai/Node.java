package com.github.mrdai.alphahearth.ai;

import com.github.mrdai.alphahearth.Board;
import com.github.mrdai.alphahearth.move.Move;
import info.hearthsim.brazier.game.PlayerId;

import java.util.LinkedList;
import java.util.List;

/**
 * Tree nodes for Monte Carlo Tree. Three of the four basic properties of a Monte Carlo Tree node
 * can be found in this class, including:
 * <ul>
 *     <li>The incoming action: {@code move} field;</li>
 *     <li>The total simulation reward: {@code reward} field;</li>
 *     <li>The visit count: {@code gameCount} field.</li>
 * </ul>
 * The associated state is not stored in this class, as storing every possible state in a Monte Carlo Tree
 * can cause huge memory overhead. Applying the associated {@code move} of the node to the current {@link Board}
 * while traversing the tree is more efficient.
 * <p>
 * This class can be used for both MCS and MCTS.
 */
public class Node {
    public final Node parent;
    public final Move move;

    public final PlayerId ownerId;

    public final LinkedList<Node> unvisitedChildren = new LinkedList<>();
    public final LinkedList<Node> visitedChildren = new LinkedList<>();
    public boolean expanded;

    public double gameCount = 0;
    public double reward = 0;

    /**
     * Creates a root {@code Node}. Used only in MCS.
     */
    public Node() {
        this(null);
    }

    /**
     * Creates a root {@code Node} with the given {@link PlayerId ownerId}.
     */
    public Node(PlayerId ownerId) {
        this(null, null, ownerId);
    }

    /**
     * Creates a non-root {@code Node} with the given {@link Move} resulting to this {@code Node}
     * and its parent {@code Node}.
     */
    public Node(Node parent, Move move, PlayerId ownerId) {
        this.parent = parent;
        this.move = move;
        this.ownerId = ownerId;
    }

    /**
     * Expands this {@code Node} with the given {@link List} of {@link Move}s.
     * All new children will be added to {@code unvisitedChildren}.
     *
     * @param moves the given {@code List} of {@code Move}s.
     * @param childOwner the {@code ownerId} of the children nodes.
     */
    public void expand(List<Move> moves, PlayerId childOwner) {
        for (Move move : moves)
            unvisitedChildren.addLast(new Node(this, move, childOwner));
        expanded = true;
    }

    /**
     * Back propagates the new score through the tree.
     */
    public void backPropagate(double score) {
        backPropagate(score, 1);
    }

    /**
     * Back propagates the new score through the tree with the given ply penalty.
     * Each time the score back propagates to the parent, the score will be timed
     * with the given ply penalty. 
     */
    public void backPropagate(double score, double plyPenalty) {
        gameCount++;
        reward += score;

        if (parent != null)
            parent.backPropagate(plyPenalty * score, plyPenalty);
    }
}
