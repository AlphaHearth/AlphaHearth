package com.github.mrdai.alphahearth.ai.mcs;

import com.github.mrdai.alphahearth.Agent;
import com.github.mrdai.alphahearth.Board;
import com.github.mrdai.alphahearth.ai.Node;
import com.github.mrdai.alphahearth.ai.MultipleExecutor;
import com.github.mrdai.alphahearth.ai.budget.Budget;
import com.github.mrdai.alphahearth.ai.budget.IterCountBudget;
import com.github.mrdai.alphahearth.ai.policy.*;
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

public class MCSAgent implements Agent {
    private static final Logger LOG = LoggerFactory.getLogger(MCSAgent.class);

    private final MultipleExecutor executor = new MultipleExecutor(6);

    private final PlayerId aiPlayerId;
    private final Budget budget;
    private final TreePolicy treePolicy;
    private final DefaultPolicy defaultPolicy;

    public MCSAgent(PlayerId aiPlayerId) {
        this(aiPlayerId, new UCBPolicy(), new RandomPolicy(), new IterCountBudget(500));
    }

    public MCSAgent(PlayerId aiPlayerId, DefaultPolicy defaultPolicy, Budget budget) {
        this(aiPlayerId, new UCBPolicy(), defaultPolicy, budget);
    }

    public MCSAgent(PlayerId aiPlayerId, TreePolicy treePolicy, DefaultPolicy defaultPolicy, Budget budget) {
        this.aiPlayerId = aiPlayerId;
        this.budget = budget;
        this.treePolicy = treePolicy;
        this.defaultPolicy = defaultPolicy;
    }

    /**
     * The main entry point of the MCS class, which uses the given {@link Board} as the root node
     * of the MCT and runs iterations on it until a certain computational budget is reached.
     */
    public synchronized Move search(Board rootBoard) {
        LOG.info("Start new MCS");
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
            while (!Thread.interrupted()) {
                Node child;
                synchronized (rootNode.unvisitedChildren) {
                    child = rootNode.unvisitedChildren.pollFirst();
                    if (child == null)
                        break;
                }
                Board currentBoard = rootBoard.clone();
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
                child.backPropagate(aiPlayerId, currentBoard.getScore(aiPlayerId));
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
                selectedLeaf.backPropagate(aiPlayerId, currentBoard.getScore(aiPlayerId));

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

        return rootNode.visitedChildren.peekFirst().move;
    }

    /**
     * Selects the best child from all the direct children of the given root node.
     * The {@link Move} associated to the selected child will be applied to the
     * given {@link Board} before the method returns.
     *
     * @param copiedBoard the copied {@code Board} used for applying the best move.
     * @param rootNode    the given root node.
     * @return the best child of the given root node.
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
            rootNode.expand(moves, aiPlayerId);
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

    public void close() {}
}
