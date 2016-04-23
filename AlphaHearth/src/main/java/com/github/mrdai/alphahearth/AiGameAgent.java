package com.github.mrdai.alphahearth;

import com.github.mrdai.alphahearth.mcts.MCSAgent;
import com.github.mrdai.alphahearth.mcts.budget.IterCountBudget;
import com.github.mrdai.alphahearth.mcts.policy.RandomPolicy;
import com.github.mrdai.alphahearth.mcts.policy.ExpertRuleBasedPolicy;
import info.hearthsim.brazier.DeckBuilder;
import info.hearthsim.brazier.game.Game;
import info.hearthsim.brazier.db.HearthStoneDb;
import info.hearthsim.brazier.game.Player;
import info.hearthsim.brazier.game.PlayerId;
import info.hearthsim.brazier.game.cards.CardName;
import info.hearthsim.brazier.game.cards.HeroClass;
import info.hearthsim.brazier.parsing.ObjectParsingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class AiGameAgent {
    private static final Logger LOG = LoggerFactory.getLogger(AiGameAgent.class);

    public static final PlayerId AI_PLAYER = new PlayerId("AiPlayer");
    public static final PlayerId AI_OPPONENT = new PlayerId("AiOpponent");

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

    private Game game;
    private Board board;
    private Agent aiPlayer = new MCSAgent(AI_PLAYER, new ExpertRuleBasedPolicy(), new IterCountBudget(500));
    private Agent aiOpponent = new MCSAgent(AI_OPPONENT, new RandomPolicy(), new IterCountBudget(500));

    public static void main(String[] args) {

        int[] iterNumArr = {
            0, 5, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 200, 300, 400, 500,
            600, 700, 800, 900, 1000, 1500, 2000
        };

        int[] winCounts = new int[iterNumArr.length];

        for (int i = 0; i < iterNumArr.length; i++) {
            LOG.warn("Begin for iterNum " + iterNumArr[i]);
            AiGameAgent agent = new AiGameAgent();
            agent.aiPlayer = new MCSAgent(AI_PLAYER, new RandomPolicy(), new IterCountBudget(iterNumArr[i]));
            agent.aiOpponent = new ExpertRuleBasedPolicy();
            int winCount = 0;
            for (int j = 1; j <= 100; j++) {
                try {
                    boolean hasAiWon = agent.roll();
                    if (hasAiWon) {
                        winCount++;
                        LOG.warn("#" + j + " game finished, AiPlayer won.");
                    } else {
                        LOG.warn("#" + j + " game finished, AiPlayer lost.");
                    }
                    LOG.info("AiPlayer already won " + winCount + " game(s).");
                } catch (Throwable thr) {
                    LOG.error("Exception occurred during #" + j + " roll out.", thr);
                    LOG.error("Retrying...");
                    j--;
                }
            }
            LOG.warn("100 games finished, AiPlayer won in " + winCount + " games.");
            winCounts[i] = winCount;
        }
        StringBuilder builder = new StringBuilder("Results: {");
        for (int i = 0; i < iterNumArr.length; i++)
            builder.append(String.format("%d: %d, ", iterNumArr[i], winCounts[i]));
        builder.append("}");
        LOG.warn(builder.toString());
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
            if (game.getCurrentPlayer().getPlayerId() == AI_PLAYER) {
                board.applyMoves(aiPlayer.produceMode(board), true);
            } else {
                board.applyMoves(aiOpponent.produceMode(board), true);
            }
            LOG.info("End turn");
            game.endTurn();
        }
        LOG.info("+++++++++++++++++++ Game over +++++++++++++++++++");
        LOG.info("The final board is\n" + board);

        return !game.getPlayer(AI_PLAYER).getHero().isDead();
    }

    private void startNewGame() {
        LOG.info("Initiating game...");
        game = new Game(HEARTH_DB, AI_PLAYER, AI_OPPONENT);
        board = new Board(game);

        Player aiPlayer = game.getPlayer(AI_PLAYER);
        Player aiOpponent = game.getOpponent(AI_PLAYER);

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
            aiOpponent.startNewTurn();
            game.setCurrentPlayerId(aiOpponent.getPlayerId());
        } else {
            LOG.info("The AI Player will play first.");
            aiOpponent.drawCardToHand();
            aiOpponent.addCardToHand(game.getDb().getCardDb().getById(new CardName("The Coin")));
            aiPlayer.startNewTurn();
            game.setCurrentPlayerId(aiPlayer.getPlayerId());
        }

        LOG.info("+++++++++++++++++++ Game start +++++++++++++++++++");
    }
}
