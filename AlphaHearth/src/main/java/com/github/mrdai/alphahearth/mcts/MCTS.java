package com.github.mrdai.alphahearth.mcts;

import com.github.mrdai.alphahearth.Board;
import com.github.mrdai.alphahearth.move.Move;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MCTS {
    private Random random;
    private Node rootNode;
    private double explorationConstant = Math.sqrt(2.0);
    private double pessimisticBias;
    private double optimisticBias;

    private boolean scoreBounds;
    private boolean trackTime; // display thinking time used

    public MCTS() {
        random = new Random();
    }

    /**
     * The main entry point of the MCTS class, which uses the given {@link Board} as the root node
     * of the MCT and runs given number of iterations on it.
     */
    public Move runMCTS(Board board, int iterNum) {
        rootNode = new Node(board);

        long startTime = System.nanoTime();

        for (int i = 0; i < iterNum; i++) {
            select(board.clone(), rootNode);
        }

        long endTime = System.nanoTime();

        if (this.trackTime)
            System.out.println("Thinking time per move in milliseconds: "
                + (endTime - startTime) / 1000000);

        return finalSelect(rootNode);
    }

    /**
     * This represents the select stage, or default policy, of the algorithm.
     * Traverse down to the bottom of the tree using the selection strategy
     * until you find an unexpanded child node. Expand it. Run a random playout
     * Backpropagate results of the playout.
     *
     * @param node Node from which to start selection
     */
    private void select(Board board, Node node) {
        while (true) {
            // Break procedure if end of tree
            if (board.isGameOver()) {
                node.backPropagateScore(board.getScore());
                if (scoreBounds) {
                    // This runs only if bounds propagation is enabled.
                    // It propagates bounds from solved nodes and prunes
                    // branches from the when needed.
                    node.backPropagateBounds(board.optimisticBounds(),
                        board.pessimisticBounds());
                }
                return;
            }

            if (node.unvisitedChildren == null) {
                List<Move> legalMoves = board.getAvailableMoves();
                node.unvisitedChildren = new ArrayList<>();
                for (Move move : legalMoves)
                    node.unvisitedChildren.add(new Node(node, board, move));
            }

            if (!node.unvisitedChildren.isEmpty()) {
                // it picks a move at random from list of unvisited children
                Node selectedNode = node.unvisitedChildren.remove(random.nextInt(node.unvisitedChildren.size()));
                node.visitedChildren.add(selectedNode);
                playout(selectedNode, board);
                return;
            } else {
                double bestValue = Double.NEGATIVE_INFINITY;
                Node bestChild = null;
                double tempBest;
                ArrayList<Node> bestNodes = new ArrayList<Node>();

                for (Node child : node.visitedChildren) {
                    // Pruned is only ever true if a branch has been pruned
                    // from the tree and that can only happen if bounds
                    // propagation mode is enabled.
                    if (!child.pruned) {
                        tempBest = child.upperConfidenceBound(explorationConstant)
                            + optimisticBias * child.opti[node.player]
                            + pessimisticBias * child.pess[node.player];

                        // If we found a better node
                        if (tempBest > bestValue) {
                            bestNodes.clear();
                            bestNodes.add(child);
                            bestChild = child;
                            bestValue = tempBest;
                        } else if (tempBest == bestValue) {
                            // If we found an equal node
                            bestNodes.add(child);
                        }
                    }
                }

                // This only occurs when all branches have been pruned from the
                // tree
                if (node == rootNode && bestChild == null)
                    return;

                Node finalNode = bestNodes.get(random.nextInt(bestNodes.size()));
                node = finalNode;
                board.applyMoves(finalNode.move);
            }
        }
    }

    /**
     * This is the final step of the algorithm, to pick the best move to
     * actually make.
     *
     * @param node this is the node whose children are considered the best Move the algorithm can find
     */
    private Move finalSelect(Node node) {
        double bestValue = Double.NEGATIVE_INFINITY;
        double tempBest;
        ArrayList<Node> bestNodes = new ArrayList<Node>();

        for (Node child : node.visitedChildren) {
            tempBest = child.games;
            if (tempBest > bestValue) {
                bestNodes.clear();
                bestNodes.add(child);
                bestValue = tempBest;
            } else if (tempBest == bestValue) {
                bestNodes.add(child);
            }
        }

        Node finalNode = bestNodes.get(random.nextInt(bestNodes.size()));

        System.out.println("Highest value: " + bestValue + ", O/P Bounds: "
            + finalNode.opti[node.player] + ", " + finalNode.pess[node.player]);
        return finalNode.move;
    }

    /**
     * Playout function for MCTS (non-flat)
     */
    private void playout(Node node, Board board) {
        List<Move> moves;
        Board newBoard = board.clone();

        // Start playing random moves until the game is over
        while (true) {
            if (newBoard.isGameOver()) {
                node.backPropagateScore(newBoard.getScore());
                return;
            }

            moves = newBoard.getAvailableMoves();
            newBoard.applyMoves(moves.get(random.nextInt(moves.size())));
        }
    }

    /**
     * Sets the exploration constant for the algorithm. You will need to find
     * the optimal value through testing. This can have a big impact on
     * performance. Default value is sqrt(2)
     */
    public void setExplorationConstant(double exp) {
        explorationConstant = exp;
    }

    public void setPessimisticBias(double b) {
        pessimisticBias = b;
    }

    public void setOptimisticBias(double b) {
        optimisticBias = b;
    }

    public void setScoreBounds(boolean b) {
        scoreBounds = b;
    }

    public void setTimeDisplay(boolean displayTime) {
        this.trackTime = displayTime;
    }
}
