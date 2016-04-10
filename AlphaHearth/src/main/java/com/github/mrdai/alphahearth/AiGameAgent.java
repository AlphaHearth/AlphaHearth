package com.github.mrdai.alphahearth;

import com.github.mrdai.alphahearth.mcts.MCTS;
import com.github.mrdai.alphahearth.mcts.policy.DefaultPolicy;
import com.github.mrdai.alphahearth.mcts.policy.RandomPolicy;
import info.hearthsim.brazier.Game;
import info.hearthsim.brazier.HearthStoneDb;
import info.hearthsim.brazier.Player;
import info.hearthsim.brazier.cards.Card;
import info.hearthsim.brazier.cards.CardName;
import info.hearthsim.brazier.parsing.ObjectParsingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AiGameAgent {
    private static final Logger LOG = LoggerFactory.getLogger(AiGameAgent.class);
    private static final List<Card> T7_HUNTER = new ArrayList<>();

    private DefaultPolicy aiOpponentPolicy = new RandomPolicy();
    private Game game;
    private MCTS mcts = new MCTS();
    private Board board;

    public static void main(String[] args) {
        AiGameAgent agent = new AiGameAgent();
        boolean hasAiWon = agent.roll();
        LOG.info("hasAiWon: " + hasAiWon);
    }

    /**
     * Rolls out a new Hearthstone game.
     *
     * @return if the AI player won.
     */
    public boolean roll() {
        startNewGame();

        while (!board.isGameOver()) {
            LOG.info("Current board is\n" + board);
            if (game.getCurrentPlayer().getPlayerId() == Board.AI_PLAYER) {
                board.applyMoves(mcts.search(board), true);
            } else {
                board.applyMoves(aiOpponentPolicy.produceMode(board), true);
            }
            LOG.info("End turn");
            game.endTurn();
        }
        LOG.info("+++++++++++++++++++ Game over +++++++++++++++++++");
        LOG.info("The final board is\n" + board);

        return !game.getPlayer1().getHero().isDead();
    }

    private void startNewGame() {
        LOG.info("Initiating game...");
        try {
            game = new Game(HearthStoneDb.readDefault(), Board.AI_PLAYER, Board.AI_OPPONENT);
        } catch (ObjectParsingException | IOException e) {
            throw new AssertionError("Failed to initiate Hearthstone Database");
        }
        board = new Board(game);

        Player aiPlayer = game.getPlayer1();
        Player aiOpponent = game.getPlayer2();

        LOG.info("Setting both players' decks...");
        // Add cards to both players' decks
        // TODO

        // Shuffle both players' decks
        aiPlayer.getDeck().shuffle();
        aiOpponent.getDeck().shuffle();

        LOG.info("Adding cards to both players' hand...");
        // Both player draw three cards
        aiPlayer.drawCardToHand();
        aiPlayer.drawCardToHand();
        aiPlayer.drawCardToHand();
        aiOpponent.drawCardToHand();
        aiOpponent.drawCardToHand();
        aiOpponent.drawCardToHand();

        LOG.info("Choosing first player...");
        // Randomly select a player to play last
        int ranNum = game.getRandomProvider().roll(1);
        if (ranNum == 0) {
            LOG.info("The AI Opponent will play first.");
            aiPlayer.drawCardToHand();
            aiPlayer.addCardToHand(game.getDb().getCardDb().getById(new CardName("The Coin")));
            game.setCurrentPlayerId(aiOpponent.getPlayerId());
        } else {
            LOG.info("The AI Player will play first.");
            aiOpponent.drawCardToHand();
            aiOpponent.addCardToHand(game.getDb().getCardDb().getById(new CardName("The Coin")));
            game.setCurrentPlayerId(aiPlayer.getPlayerId());
        }

        LOG.info("+++++++++++++++++++ Game start +++++++++++++++++++");
    }
}
