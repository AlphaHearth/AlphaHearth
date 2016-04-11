package com.github.mrdai.alphahearth.mcts;

import com.github.mrdai.alphahearth.Board;
import com.github.mrdai.alphahearth.mcts.budget.Budget;
import com.github.mrdai.alphahearth.mcts.budget.TimeBudget;
import com.github.mrdai.alphahearth.mcts.policy.*;
import com.github.mrdai.alphahearth.move.Move;
import com.github.mrdai.alphahearth.move.SingleMove;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.github.mrdai.alphahearth.Board.AI_OPPONENT;

public class MCTS {
    private static final Logger LOG = LoggerFactory.getLogger(MCTS.class);

    private final Budget budget = new TimeBudget(90000);
    private final TreePolicy treePolicy = new UCTTreePolicy();
    private final DefaultPolicy enemyDefaultPolicy = new RuleBasedPolicy();
    private final DefaultPolicy ourDefaultPolicy = new RuleBasedPolicy();

    /**
     * The main entry point of the MCTS class, which uses the given {@link Board} as the root node
     * of the MCT and runs iterations on it until a certain computational budget is reached.
     */
    public Move search(Board rootBoard) {
        LOG.info("Start new MCTS");
        Node rootNode = new Node();
        budget.startSearch();
        long startTime = System.currentTimeMillis();
        int iterNum = 1;

        while (!budget.hasReached()) {
            LOG.debug("Start iteration #" + iterNum);
            budget.newIteration();
            Board currentBoard = rootBoard.clone();
            LOG.debug("Selecting...");
            // Selection
            Node selectedLeaf = select(currentBoard, rootNode);
            LOG.debug("Simulating...");
            // Simulation
            simulate(currentBoard);
            LOG.debug("Back propagating...");
            // Back Propagation
            selectedLeaf.backPropagate(currentBoard.getScore());
            iterNum++;
        }
        long finishTime = System.currentTimeMillis();
        LOG.info("Search finished in " + (finishTime - startTime) + "ms with " + iterNum + " iterations.");
        StringBuilder builder = new StringBuilder("Direct children include: \n=== Visited Children ===\n");
        for (Node node : rootNode.visitedChildren) {
            Board board = rootBoard.clone();
            if (node.move.getActualMoves().isEmpty())
                builder.append("AiPlayer does nothing\n");
            for (SingleMove move : node.move.getActualMoves()) {
                builder.append(move.toString(board)).append("\n");
                board.applyMoves(move.toMove());
            }
            builder.append("Reward: " + node.reward + ", Game count: " + node.gameCount + "\n");
            builder.append("----------\n");
        }
        if (!rootNode.unvisitedChildren.isEmpty()) {
            builder.append("=== Unvisited Children ===");
            for (Node node : rootNode.unvisitedChildren) {
                Board board = rootBoard.clone();
                if (node.move.getActualMoves().isEmpty())
                    builder.append("AiPlayer does nothing\n");
                for (SingleMove move : node.move.getActualMoves()) {
                    builder.append(move.toString(board)).append("\n");
                    board.applyMoves(move.toMove());
                }
                builder.append("----------\n");
            }
        }
        LOG.info(builder.toString());

        return bestChild(rootNode).move;
    }

    /**
     * Selects an expandable node with the given root node of the Monte Carlo Tree and
     * the copy of {@code Board} used for this iteration.
     * <p>
     * While traversing through the tree to look for the best candidate, the {@code Move} stored
     * in every visited {@code Node} will also be applied on the given {@code Board},
     * resulting it standing for the exact game state of the selected node when the method returns.
     *
     * @param copiedBoard the copied {@code Board} used for this iteration.
     * @param rootNode Node from which to start selection.
     *
     * @return the most urgent expandable node.
     */
    private Node select(Board copiedBoard, Node rootNode) {
        Node currentNode = rootNode;
        while (!copiedBoard.isGameOver()) {
            // Expansion
            if (!currentNode.expanded)
                currentNode.expand(copiedBoard.getAvailableMoves());

            if (!currentNode.unvisitedChildren.isEmpty()) {
                Node selectedChild = currentNode.unvisitedChildren.get(0);
                currentNode.unvisitedChildren.remove(0);
                currentNode.visitedChildren.add(selectedChild);
                return selectedChild;
            } else {
                currentNode = bestChild(currentNode);
                copiedBoard.applyMoves(currentNode.move);
            }
        }
        return currentNode;
    }

    /**
     * Selects the best child from all the visited children of the given {@link Node}
     * with the {@link TreePolicy} of this {@code MCTS}.
     *
     * @param node the given {@code Node}.
     * @return the best child from the visited children of the given {@code Node}.
     */
    private Node bestChild(Node node) {
        return treePolicy.bestChild(node);
    }

    /**
     * Plays out the given selected {@code Node} with the given starting {@code Board}.
     */
    private void simulate(Board copiedBoard) {
        copiedBoard.getGame().getPlayer1().getDeck().shuffle();
        copiedBoard.getGame().getPlayer2().getDeck().shuffle();
        // Start playing moves with the default policy until the game is over
        while (!copiedBoard.isGameOver()) {
            if (copiedBoard.getGame().getCurrentPlayer().getPlayerId() == AI_OPPONENT)
                copiedBoard.applyMoves(enemyDefaultPolicy.produceMode(copiedBoard));
            else
                copiedBoard.applyMoves(ourDefaultPolicy.produceMode(copiedBoard));
            copiedBoard.getGame().endTurn();
        }
    }
}
