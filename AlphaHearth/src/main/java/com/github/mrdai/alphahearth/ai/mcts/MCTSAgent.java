package com.github.mrdai.alphahearth.ai.mcts;

import com.github.mrdai.alphahearth.Agent;
import com.github.mrdai.alphahearth.Board;
import com.github.mrdai.alphahearth.ai.Node;
import com.github.mrdai.alphahearth.ai.budget.Budget;
import com.github.mrdai.alphahearth.ai.budget.IterCountBudget;
import com.github.mrdai.alphahearth.ai.policy.DefaultPolicy;
import com.github.mrdai.alphahearth.ai.policy.RandomPolicy;
import com.github.mrdai.alphahearth.ai.policy.TreePolicy;
import com.github.mrdai.alphahearth.ai.policy.UCBPolicy;
import com.github.mrdai.alphahearth.move.Move;
import com.github.mrdai.alphahearth.move.SingleMove;
import info.hearthsim.brazier.game.Game;
import info.hearthsim.brazier.game.GameResult;
import info.hearthsim.brazier.game.Player;
import info.hearthsim.brazier.game.PlayerId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Supplier;

public class MCTSAgent implements Agent {
    private static final Logger LOG = LoggerFactory.getLogger(MCTSAgent.class);

    private final ExecutorService executor = Executors.newFixedThreadPool(5);

    private final PlayerId aiPlayerId;
    private final Supplier<Budget> budgetSupplier;
    private final TreePolicy treePolicy;
    private final DefaultPolicy defaultPolicy;

    private final int deterNum;

    public MCTSAgent(PlayerId aiPlayerId) {
        this(aiPlayerId, new UCBPolicy(), new RandomPolicy(),
            () -> new IterCountBudget(500), 500);
    }

    public MCTSAgent(PlayerId aiPlayerId, DefaultPolicy defaultPolicy, Supplier<Budget> budgetSupplier, int deterNum) {
        this(aiPlayerId, new UCBPolicy(), defaultPolicy, budgetSupplier, deterNum);
    }

    public MCTSAgent(PlayerId aiPlayerId, TreePolicy treePolicy,
                     DefaultPolicy defaultPolicy,
                     Supplier<Budget> budgetSupplier, int deterNum) {
        this.aiPlayerId = aiPlayerId;
        this.budgetSupplier = budgetSupplier;
        this.treePolicy = treePolicy;
        this.defaultPolicy = defaultPolicy;
        this.deterNum = deterNum;
    }

    /**
     * The main entry point of the MCTS class, which uses the given {@link Board} as the root node
     * of the MCT and runs iterations on it until a certain computational budget is reached.
     */
    public Move search(Board rootBoard) {
        // Initialize direct children
        List<Move> directMoves = getAvailableMoves(rootBoard);
        if (directMoves.size() == 1) {
            LOG.info("Found only one move. Return it directly.");
            return directMoves.get(0);
        }
        LinkedList<Node> directChildren = new LinkedList<>();
        for (Move move : directMoves) {
            Node node = new Node(null, move, aiPlayerId);
            Board copiedBoard = rootBoard.clone();
            copiedBoard.applyMoves(move);
            if (copiedBoard.isGameOver() && !copiedBoard.getGame().getPlayer(aiPlayerId).getHero().isDead()) {
                LOG.info("Found lethal. Return it directly.");
                return move;
            }
            copiedBoard.getGame().endTurn();
            simulate(copiedBoard);
            backPropergate(copiedBoard, node);
            directChildren.push(node);
        }

        // Initialize boards for determinized trees
        Board[] determinizedBoards = new Board[deterNum];
        for (int i = 0; i < deterNum; i++) {
            Board copiedBoard = rootBoard.clone();
            Game game = copiedBoard.getGame();
            // Shuffle AI's deck
            Player aiPlayer = game.getPlayer(aiPlayerId);
            aiPlayer.getDeck().shuffle();
            // Shuffle Opponent's hand and deck
            Player opponent = game.getOpponent(aiPlayerId);
            int handCount = opponent.getHand().getCardCount();
            for (int j = 0; j < handCount; j++) {
                opponent.getDeck().putOnTop(opponent.getHand().removeAtIndex(0));
            }
            opponent.getDeck().shuffle();
            for (int j = 0; j < handCount; j++)
                opponent.drawCardToHand();
            determinizedBoards[i] = copiedBoard;
        }

        // Submit search job for each determinized tree
        List<Future> futures = new ArrayList<>(deterNum);
        for (int i = 0; i < deterNum; i++) {
            // Initialize determinized trees
            Map<Node, Node> determinizedTrees = new HashMap<>();
            for (Node node : directChildren)
                determinizedTrees.put(node, new Node(node, null, aiPlayerId));

            final Board board = determinizedBoards[i];
            final Budget budget = budgetSupplier.get();
            final int deter = i + 1;
            LOG.debug("Submitting determinization {}", deter);
            //futures.add(executor.submit(() -> {
                budget.startSearch();
                int iterNum = 1;
                while (!budget.hasReached()) {
                    LOG.debug("Determinization {} starts iteration #{}", deter, iterNum);
                    Board currentBoard = board.clone();
                    LOG.debug("Determinization {} applying the best direct move...", deter);
                    Node bestDirectChild = treePolicy.bestNode(directChildren);
                    Node rootNode = determinizedTrees.get(bestDirectChild);
                    currentBoard.applyMoves(bestDirectChild.move);
                    currentBoard.getGame().endTurn();

                    LOG.debug("Determinization {} selecting...", deter);
                    Node selectedLeaf = select(currentBoard, rootNode);
                    LOG.debug("Determinization {} simulating...", deter);
                    simulate(currentBoard);
                    LOG.debug("Determinization {} back propagating...", deter);
                    backPropergate(currentBoard, selectedLeaf);
                    budget.newIteration();
                    iterNum++;
                }
                LOG.info("Determinization {} finished.", deter);
            //}));
        }
        LOG.info("Main thread waiting for determinizations to finish...");
        for (int i = 0; i < futures.size(); i++) {
            try {
                futures.get(i).get();
            } catch (Exception e) {
                LOG.error("Something bad happened on determinization " + (i + 1), e);
            }
        }

        Comparator<Node> CMP = (o1, o2) -> -1 * Double.compare(o1.reward / o1.gameCount, o2.reward / o2.gameCount);
        directChildren.sort(CMP);
        if (LOG.isInfoEnabled()) {
            StringBuilder builder = new StringBuilder("Visited direct children include: \n");
            for (Node node : directChildren) {
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

        return directChildren.peekFirst().move;
    }

    private void backPropergate(Board board, Node node) {
        GameResult result = board.getGame().tryGetGameResult();
        PlayerId opponentId = board.getGame().getOpponent(aiPlayerId).getPlayerId();
        if (result.hasWon(aiPlayerId)) {
            node.backPropagate(aiPlayerId, board.getScore(aiPlayerId));
        } else if (result.hasWon(opponentId)) {
            node.backPropagate(opponentId, board.getScore(opponentId));
        } else
            node.backPropagate(null, 0);
    }

    private List<Move> getAvailableMoves(Board board) {
        List<Move> moves = board.getAvailableMoves();
        // Prune moves that looks plain stupid
        for (int i = moves.size() - 1; i >= 0; i--) {
            if (moves.size() == 1)  // Don't prune any more
                break;
            Move move = moves.get(i);
            Board copiedBoard = board.clone();
            copiedBoard.applyMoves(move);

            if (copiedBoard.isGameOver()) {
                if (!copiedBoard.hasWon(board.getCurrentPlayer().getPlayerId()))
                    moves.remove(i);
                continue;
            }

            Player currentPlayer = copiedBoard.getGame().getCurrentPlayer();
            Player currentOpponent = copiedBoard.getGame().getCurrentOpponent();
            if (currentPlayer.getBoard().countMinions((m) -> m.getAttackTool().canAttackWith()) > 0
                    && !currentOpponent.getBoard().hasNonStealthTaunt())
                moves.remove(i);
            else if (currentPlayer.getHero().getHeroPower().isPlayable())
                moves.remove(i);
            else if (!currentPlayer.getHand().getCards((c) -> c.isMinionCard()
                     && c.getActiveManaCost() < currentPlayer.getMana()).isEmpty())
                moves.remove(i);
        }
        LOG.debug("Get {} available moves", moves.size());
        return moves;
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
        Node node = rootNode;

        while (!copiedBoard.isGameOver()) {
            if (!node.expanded) {
                node.expand(getAvailableMoves(copiedBoard),
                    copiedBoard.getGame().getCurrentOpponent().getPlayerId());
                LOG.debug("Expand {} moves for {}.", node.unvisitedChildren.size(),
                    copiedBoard.getGame().getCurrentOpponent().getPlayerId());
            }
            if (!node.unvisitedChildren.isEmpty()) {
                Node selectedLeaf = node.unvisitedChildren.pollFirst();
                node.visitedChildren.addLast(selectedLeaf);
                copiedBoard.applyMoves(selectedLeaf.move);
                copiedBoard.getGame().endTurn();
                return selectedLeaf;
            }
            node = treePolicy.bestNode(node.visitedChildren);
            copiedBoard.applyMoves(node.move);
            copiedBoard.getGame().endTurn();
        }

        return node;
    }

    /**
     * Plays out the given selected {@code Node} with the given starting {@code Board}.
     */
    private void simulate(Board copiedBoard) {
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

    public void close() {
        executor.shutdown();
    }
}
