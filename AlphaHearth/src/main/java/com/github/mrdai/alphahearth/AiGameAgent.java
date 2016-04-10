package com.github.mrdai.alphahearth;

import com.github.mrdai.alphahearth.mcts.MCTS;
import com.github.mrdai.alphahearth.mcts.policy.DefaultPolicy;
import com.github.mrdai.alphahearth.mcts.policy.RandomPolicy;
import com.github.mrdai.alphahearth.mcts.policy.RuleBasedPolicy;
import info.hearthsim.brazier.DeckBuilder;
import info.hearthsim.brazier.game.Game;
import info.hearthsim.brazier.db.HearthStoneDb;
import info.hearthsim.brazier.game.Player;
import info.hearthsim.brazier.game.cards.Card;
import info.hearthsim.brazier.game.cards.CardName;
import info.hearthsim.brazier.game.cards.HeroClass;
import info.hearthsim.brazier.parsing.ObjectParsingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AiGameAgent {
    private static final Logger LOG = LoggerFactory.getLogger(AiGameAgent.class);

    public static HearthStoneDb HEARTH_DB;
    static {
        try {
            HEARTH_DB = HearthStoneDb.readDefault();
        } catch (ObjectParsingException | IOException e) {
            LOG.error("Failed to load Hearthstone Database", e);
        }
    }
    private static final DeckBuilder HUNTER_TEST_DECK = new DeckBuilder(HeroClass.Hunter);
    static {
        HUNTER_TEST_DECK.addCard(HEARTH_DB.getCardDb().getById(new CardName("Hunter's Mark")), 2)
            .addCard(HEARTH_DB.getCardDb().getById(new CardName("Arcane Shot")), 2)
            .addCard(HEARTH_DB.getCardDb().getById(new CardName("Abusive Sergeant")), 2)
            .addCard(HEARTH_DB.getCardDb().getById(new CardName("Worgen Infiltrator")), 2)
        	.addCard(HEARTH_DB.getCardDb().getById(new CardName("Explosive Trap")), 2)
        	.addCard(HEARTH_DB.getCardDb().getById(new CardName("Freezing Trap")), 2)
        	.addCard(HEARTH_DB.getCardDb().getById(new CardName("Haunted Creeper")), 2)
        	.addCard(HEARTH_DB.getCardDb().getById(new CardName("Eaglehorn Bow")), 2)
        	.addCard(HEARTH_DB.getCardDb().getById(new CardName("Kill Command")), 2)
        	.addCard(HEARTH_DB.getCardDb().getById(new CardName("Unleash the Hounds")), 2)
        	.addCard(HEARTH_DB.getCardDb().getById(new CardName("Spider Tank")), 2)
        	.addCard(HEARTH_DB.getCardDb().getById(new CardName("Chillwind Yeti")), 2)
        	.addCard(HEARTH_DB.getCardDb().getById(new CardName("Leeroy Jenkins")), 1)
        	.addCard(HEARTH_DB.getCardDb().getById(new CardName("Boulderfist Ogre")), 2)
        	.addCard(HEARTH_DB.getCardDb().getById(new CardName("Gahz'rilla")), 1)
        	.addCard(HEARTH_DB.getCardDb().getById(new CardName("Force-Tank MAX")), 2);
    }

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
        game = new Game(HEARTH_DB, Board.AI_PLAYER, Board.AI_OPPONENT);
        board = new Board(game);

        Player aiPlayer = game.getPlayer1();
        aiPlayer.getManaResource().setManaCrystals(1);
        Player aiOpponent = game.getPlayer2();
        aiOpponent.getManaResource().setManaCrystals(1);

        LOG.info("Setting both players' decks...");
        // Add cards to both players' decks
        aiPlayer.getDeck().setCards(HUNTER_TEST_DECK.toCardList());
        aiPlayer.getHero().setHeroPower(HEARTH_DB.getHeroPowerDb().getById(new CardName("Steady Shot")));
        aiOpponent.getDeck().setCards(HUNTER_TEST_DECK.toCardList());
        aiOpponent.getHero().setHeroPower(HEARTH_DB.getHeroPowerDb().getById(new CardName("Steady Shot")));

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
        int ranNum = game.getRandomProvider().roll(2);
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
