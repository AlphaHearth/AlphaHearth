package com.github.mrdai.alphahearth.mcts;

import com.github.mrdai.alphahearth.Board;
import com.github.mrdai.alphahearth.move.Move;

import java.util.ArrayList;

class Node {
    public double[] score;
    public double games;
    public Move move;
    public ArrayList<Node> unvisitedChildren;
    public ArrayList<Node> visitedChildren;
    public Node parent;
    public int player;
    public double[] pess;
    public double[] opti;
    public boolean pruned;
    public int depth;

    /**
     * Creates a root {@code Node} with the given {@link Board}.
     */
    public Node(Board board) {
        visitedChildren = new ArrayList<>();
        player = board.getCurrentPlayer();
        score = new double[board.getQuantityOfPlayers()];
        pess = new double[board.getQuantityOfPlayers()];
        opti = new double[board.getQuantityOfPlayers()];
        for (int i = 0; i < board.getQuantityOfPlayers(); i++)
            opti[i] = 1;
    }

    /**
     * Creates a non-root {@code Node} with the given {@link Board},
     * {@link Move} resulting to this {@code Node} and its parent {@code Node}.
     */
    public Node(Node parent, Board board, Move move) {
        visitedChildren = new ArrayList<>();
        this.parent = parent;
        depth = this.parent.depth + 1;
        this.move = move;
        Board newBoard = board.clone();
        newBoard.applyMoves(move);
        player = newBoard.getCurrentPlayer();
        score = new double[newBoard.getQuantityOfPlayers()];
        pess = new double[newBoard.getQuantityOfPlayers()];
        opti = new double[newBoard.getQuantityOfPlayers()];
        for (int i = 0; i < newBoard.getQuantityOfPlayers(); i++)
            opti[i] = 1;
    }

    /**
     * Return the upper confidence bound of this state
     *
     * @param c typically sqrt(2). Increase to emphasize exploration. Decrease
     *          to incr. exploitation
     */
    public double upperConfidenceBound(double c) {
        return score[parent.player] / games + c
            * Math.sqrt(Math.log(parent.games + 1) / games);
    }

    /**
     * Update the tree with the new score.
     */
    public void backPropagateScore(double[] scr) {
        this.games++;
        for (int i = 0; i < scr.length; i++)
            this.score[i] += scr[i];

        if (parent != null)
            parent.backPropagateScore(scr);
    }

    /**
     * Backpropagate the bounds.
     */
    public void backPropagateBounds(double[] optimistic, double[] pessimistic) {
        for (int i = 0; i < optimistic.length; i++) {
            opti[i] = optimistic[i];
            pess[i] = pessimistic[i];
        }

        if (parent != null)
            parent.backPropagateBoundsHelper();
    }

    private void backPropagateBoundsHelper() {
        for (int i = 0; i < opti.length; i++) {
            if (i == player) {
                opti[i] = 0;
                pess[i] = 0;
            } else {
                opti[i] = 1;
                pess[i] = 1;
            }
        }

        for (int i = 0; i < opti.length; i++) {
            for (Node c : visitedChildren) {
                if (i == player) {
                    if (opti[i] < c.opti[i])
                        opti[i] = c.opti[i];
                    if (pess[i] < c.pess[i])
                        pess[i] = c.pess[i];
                } else {
                    if (opti[i] > c.opti[i])
                        opti[i] = c.opti[i];
                    if (pess[i] > c.pess[i])
                        pess[i] = c.pess[i];
                }
            }
        }

        // This compares against a dummy node with bounds 1 0
        // if not all children have been explored
        if (!unvisitedChildren.isEmpty()) {
            for (int i = 0; i < opti.length; i++) {
                if (i == player) {
                    opti[i] = 1;
                } else {
                    pess[i] = 0;
                }
            }
        }

        pruneBranches();
        if (parent != null)
            parent.backPropagateBoundsHelper();
    }

    public void pruneBranches() {
        visitedChildren.stream().filter((child) -> pess[player] >= child.opti[player])
            .forEach((child) -> child.pruned = true);

        if (parent != null)
            parent.pruneBranches();
    }
}
