package com.github.mrdai.alphahearth.mcts;

import com.github.mrdai.alphahearth.Board;
import com.github.mrdai.alphahearth.mcts.budget.Budget;
import com.github.mrdai.alphahearth.mcts.budget.TimeBudget;
import com.github.mrdai.alphahearth.mcts.policy.*;
import com.github.mrdai.alphahearth.move.Move;
import com.github.mrdai.alphahearth.move.SingleMove;
import info.hearthsim.brazier.game.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;

import static com.github.mrdai.alphahearth.Board.AI_OPPONENT;
import static com.github.mrdai.alphahearth.Board.AI_PLAYER;

public class MCTS {
    private static final Logger LOG = LoggerFactory.getLogger(MCTS.class);

    private final Budget budget = new TimeBudget(10000);
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

        LOG.debug("Expanding...");
        expand(rootBoard, rootNode);
        while (!budget.hasReached()) {
            LOG.debug("Start iteration #" + iterNum);
            budget.newIteration();
            Board currentBoard = rootBoard.clone();
            LOG.debug("Selecting...");
            // Selection
            Node selectedLeaf = select(currentBoard, rootNode);

            // Already won, stop searching.
            if (currentBoard.isGameOver() && !currentBoard.getGame().getPlayer(Board.AI_PLAYER).getHero().isDead()) {
                long finishTime = System.currentTimeMillis();
                LOG.info("Search finished in " + (finishTime - startTime) + "ms with " + iterNum + " iterations.");
                return selectedLeaf.move;
            }

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
        StringBuilder builder = new StringBuilder("Visited direct children include: \n");
        Comparator<Node> CMP = (o1, o2) -> -1 * Double.compare(o1.reward / o1.gameCount, o2.reward / o2.gameCount);
        rootNode.visitedChildren.sort(CMP);
        for (Node node : rootNode.visitedChildren) {
            Board board = rootBoard.clone();
            if (node.move.getActualMoves().isEmpty())
                builder.append("AiPlayer does nothing\n");
            for (SingleMove move : node.move.getActualMoves()) {
                builder.append(move.toString(board)).append("\n");
                board.applyMoves(move.toMove());
            }
            builder.append("Game count: " + node.gameCount + ", Average Reward: " + node.reward / node.gameCount + "\n");
            builder.append("----------\n");
        }
        builder.append("=====================");
        LOG.info(builder.toString());

        return rootNode.visitedChildren.get(0).move;
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
     * @param rootNode    Node from which to start selection.
     * @return the most urgent expandable node.
     */
    private Node select(Board copiedBoard, Node rootNode) {
        if (!rootNode.unvisitedChildren.isEmpty()) {
            Node selectedChild = rootNode.unvisitedChildren.get(0);
            rootNode.unvisitedChildren.remove(0);
            rootNode.visitedChildren.add(selectedChild);
            return selectedChild;
        } else {
            rootNode = bestChild(rootNode);
            copiedBoard.applyMoves(rootNode.move);
        }
        return rootNode;
    }

    private void expand(Board board, Node rootNode) {
        if (!rootNode.expanded) {
            List<Move> moves = board.getAvailableMoves();
            // Prune moves that looks plain stupid
            for (int i = moves.size() - 1; i >= 0; i--) {
                if (moves.size() == 1)  // Don't prune any more
                    break;
                Move move = moves.get(i);
                Board copiedBoard = board.clone();
                copiedBoard.applyMoves(move);

                if (copiedBoard.getGame().isGameOver())
                    continue;

                Player aiPlayer = copiedBoard.getGame().getPlayer(AI_PLAYER);
                Player aiOpponent = copiedBoard.getGame().getOpponent(aiPlayer.getPlayerId());
                if (aiPlayer.getBoard().countMinions((m) -> m.getAttackTool().canAttackWith()) > 0
                        && !aiOpponent.getBoard().hasNonStealthTaunt())
                    moves.remove(i);
                else if (aiPlayer.getHero().getHeroPower().isPlayable())
                    moves.remove(i);
                else if (!aiPlayer.getHand().getCards((c) -> c.isMinionCard() && c.getActiveManaCost() < aiPlayer.getMana()).isEmpty())
                    moves.remove(i);
            }
            LOG.info("Added " + moves.size() + " moves.");
            rootNode.expand(moves);
        }
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
