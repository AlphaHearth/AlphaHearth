package com.github.mrdai.alphahearth.mcts;

import com.github.mrdai.alphahearth.Agent;
import com.github.mrdai.alphahearth.Board;
import com.github.mrdai.alphahearth.mcts.budget.Budget;
import com.github.mrdai.alphahearth.mcts.budget.IterCountBudget;
import com.github.mrdai.alphahearth.mcts.policy.*;
import com.github.mrdai.alphahearth.move.Move;
import com.github.mrdai.alphahearth.move.SingleMove;
import info.hearthsim.brazier.game.Player;
import info.hearthsim.brazier.game.PlayerId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class MCTS implements Agent {
    private static final Logger LOG = LoggerFactory.getLogger(MCTS.class);

    private final SimulateExecutor executor = new SimulateExecutor(6);

    private final PlayerId aiPlayerId;
    private final Budget budget;
    private final TreePolicy treePolicy;
    private final DefaultPolicy defaultPolicy;

    public MCTS(PlayerId aiPlayerId) {
        this(aiPlayerId, new UCTTreePolicy(), new RandomPolicy(), new IterCountBudget(500));
    }

    public MCTS(PlayerId aiPlayerId, DefaultPolicy defaultPolicy, Budget budget) {
        this(aiPlayerId, new UCTTreePolicy(), defaultPolicy, budget);
    }

    public MCTS(PlayerId aiPlayerId, TreePolicy treePolicy, DefaultPolicy defaultPolicy, Budget budget) {
        this.aiPlayerId = aiPlayerId;
        this.budget = budget;
        this.treePolicy = treePolicy;
        this.defaultPolicy = defaultPolicy;
    }

    /**
     * The main entry point of the MCTS class, which uses the given {@link Board} as the root node
     * of the MCT and runs iterations on it until a certain computational budget is reached.
     */
    public synchronized Move search(Board rootBoard) {
        LOG.info("Start new MCTS");
        Node rootNode = new Node();
        budget.startSearch();
        long startTime = System.currentTimeMillis();
        final AtomicInteger iterNum = new AtomicInteger(1);

        LOG.debug("Expanding...");
        expand(rootBoard, rootNode);

        if (rootNode.unvisitedChildren.size() == 1) {
            LOG.info("Found only one child. Return it directly.");
            return rootNode.unvisitedChildren.get(0).move;
        }

        final AtomicReference<Move> lethalRef = new AtomicReference<>();
        LOG.debug("Submitting first traversing task...");
        executor.execute(() -> {
            while (!rootNode.unvisitedChildren.isEmpty() && !Thread.interrupted()) {
                Board currentBoard = rootBoard.clone();
                Node child;
                try {
                   child = rootNode.unvisitedChildren.remove(0);
                } catch (IndexOutOfBoundsException e) {
                    // Already removed
                    break;
                }
                rootNode.visitedChildren.add(child);
                currentBoard.applyMoves(child.move);

                // Found lethal
                if (currentBoard.isGameOver() && !currentBoard.getGame().getPlayer(aiPlayerId).getHero().isDead()) {
                    LOG.debug("Found lethal. Interrupting worker threads...");
                    lethalRef.set(child.move);
                    executor.interrupt();
                    return;
                }

                simulate(currentBoard);
                child.backPropagate(currentBoard.getScore(aiPlayerId));
            }
        });
        executor.waitToFinish();

        Move lethalMove = lethalRef.get();
        if (lethalMove != null) {
            LOG.info("Found lethal. Returning...");
            return lethalMove;
        }

        LOG.debug("Submitting simulation task...");
        executor.execute(() -> {
            while (!budget.hasReached()) {
                LOG.debug("Start iteration #" + iterNum.get());
                budget.newIteration();
                Board currentBoard = rootBoard.clone();

                LOG.debug("Selecting...");
                Node selectedLeaf = select(currentBoard, rootNode);

                LOG.debug("Simulating...");
                simulate(currentBoard);

                LOG.debug("Back propagating...");
                selectedLeaf.backPropagate(currentBoard.getScore(aiPlayerId));

                iterNum.getAndIncrement();
            }
        });
        executor.waitToFinish();

        long finishTime = System.currentTimeMillis();
        LOG.info("Search finished in " + (finishTime - startTime) + "ms with " + iterNum + " iterations.");
        Comparator<Node> CMP = (o1, o2) -> -1 * Double.compare(o1.reward / o1.gameCount, o2.reward / o2.gameCount);
        rootNode.visitedChildren.sort(CMP);
        if (LOG.isInfoEnabled()) {
            StringBuilder builder = new StringBuilder("Visited direct children include: \n");
            for (Node node : rootNode.visitedChildren) {
                Board board = rootBoard.clone();
                if (node.move.getActualMoves().isEmpty())
                    builder.append("AiPlayer does nothing\n");
                for (SingleMove move : node.move.getActualMoves()) {
                    builder.append(move.toString(board)).append("\n");
                    move.applyTo(board);
                }
                builder.append("Game count: " + node.gameCount + ", Average Reward: " + node.reward / node.gameCount + "\n");
                builder.append("----------\n");
            }
            builder.append("=====================");
            LOG.info(builder.toString());
        }

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
        rootNode = bestChild(rootNode);
        copiedBoard.applyMoves(rootNode.move);

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

                if (copiedBoard.isGameOver()) {
                    if (!copiedBoard.hasWon(aiPlayerId))
                        moves.remove(i);
                    continue;
                }

                Player aiPlayer = copiedBoard.getGame().getPlayer(aiPlayerId);
                Player aiOpponent = copiedBoard.getGame().getOpponent(aiPlayerId);
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
            copiedBoard.applyMoves(defaultPolicy.produceMode(copiedBoard));
            copiedBoard.getGame().endTurn();
        }
    }

    @Override
    public Move produceMode(Board board) {
        return search(board);
    }
}
