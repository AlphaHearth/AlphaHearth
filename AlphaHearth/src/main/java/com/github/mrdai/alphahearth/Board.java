package com.github.mrdai.alphahearth;

import info.hearthsim.brazier.*;
import info.hearthsim.brazier.Character;
import info.hearthsim.brazier.actions.PlayTargetRequest;
import info.hearthsim.brazier.cards.Card;
import info.hearthsim.brazier.minions.Minion;
import info.hearthsim.brazier.ui.PlayerTargetNeed;
import com.github.mrdai.alphahearth.move.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

public class Board {
    private static final Logger LOG = LoggerFactory.getLogger(Board.class);

    public static final PlayerId AI_PLAYER = new PlayerId("AiPlayer");
    public static final PlayerId AI_OPPONENT = new PlayerId("AiOpponent");

    private final GameAgent playAgent;

    public Board(Game game) {
        playAgent = new GameAgent(game);
    }

    private Board(Board other) {
        playAgent = new GameAgent(other.playAgent.getGame().copy());
    }

    /**
     * Returns all of the available {@link Move}s for the current player.
     */
    public List<Move> getAvailableMoves() {
        List<Move> availableMoves = new LinkedList<>();
        availableMoves.add(Move.EMPTY_MOVE);

        for (int i = 0; i < availableMoves.size(); i++)
            expandMove(availableMoves, i);

        return availableMoves;
    }

    private void expandMove(List<Move> availableMoves, int expandMoveIndex) {
        Move selectedMove = availableMoves.get(expandMoveIndex);

        Board copiedBoard = clone();
        copiedBoard.applyMoves(selectedMove);
        Game currentGame = copiedBoard.playAgent.getGame();
        Player curPlayer = currentGame.getCurrentPlayer();
        PlayerId curPlayerId = curPlayer.getPlayerId();
        Player curOpponent = currentGame.getOpponent(curPlayer.getPlayerId());
        Hero friendlyHero = curPlayer.getHero();
        Hero enemyHero = curOpponent.getHero();
        BoardSide friendlyMinions = curPlayer.getBoard();
        BoardSide enemyMinions = curOpponent.getBoard();

        // List Hero Power
        HeroPower heroPower = friendlyHero.getHeroPower();
        if (heroPower.isPlayable()) {
            PlayerTargetNeed targetNeed =
                new PlayerTargetNeed(new TargeterDef(curPlayerId, true, false), heroPower.getTargetNeed());
            if (!targetNeed.getTargetNeed().hasTarget()) {
                SingleMove heroPowerPlaying = new HeroPowerPlaying(null);
                add(selectedMove, heroPowerPlaying, availableMoves);
            } else {
                currentGame.getTargets().stream().filter(targetNeed::isAllowedTarget).forEach((target) -> {
                    SingleMove heroPowerPlaying = new HeroPowerPlaying(curPlayerId, target.getEntityId());
                    add(selectedMove, heroPowerPlaying, availableMoves);
                });
            }
        }

        // List direct attack
        currentGame.getTargets((target) -> target.getOwner() == curPlayer && target.getAttackTool().canAttackWith())
            .forEach((attacker) -> {
                if (enemyMinions.hasNonStealthTaunt()) {
                    enemyMinions.findMinions((minion) -> minion.getBody().isTaunt() && !minion.getBody().isStealth())
                        .forEach((target) -> {
                            SingleMove directAttacking = new DirectAttacking(attacker.getEntityId(), target.getEntityId());
                            add(selectedMove, directAttacking, availableMoves);
                        });
                } else {
                    if (!enemyHero.isImmune()) {
                        SingleMove directAttacking = new DirectAttacking(attacker.getEntityId(), enemyHero.getEntityId());
                        add(selectedMove, directAttacking, availableMoves);
                    }
                    enemyMinions.findMinions((minion) -> !minion.getBody().isStealth() && !minion.getBody().isImmune())
                        .forEach((target) -> {
                            SingleMove directAttacking = new DirectAttacking(attacker.getEntityId(), target.getEntityId());
                            add(selectedMove, directAttacking, availableMoves);
                        });
                }
            });

        if (curPlayer.getPlayerId() != AI_PLAYER)
            return; // We do not consider the AI opponent's card for now.

        // List Card
        Hand hand = curPlayer.getHand();
        for (int cardIndex = 0; cardIndex < hand.getCardCount(); cardIndex++) {
            Card card = hand.getCard(cardIndex);
            if (card.getActiveManaCost() > curPlayer.getMana())
                continue;

            PlayerTargetNeed targetNeed =
                new PlayerTargetNeed(new TargeterDef(AI_PLAYER, true, false), card.getTargetNeed());
            if (card.isMinionCard()) {
                if (card.getTargetNeed().hasTarget()) { // Minion card with battle cry target
                    for (Character target : currentGame.getTargets()) {
                        if (targetNeed.isAllowedTarget(target)) {
                            for (int minionLoc = 0; minionLoc <= friendlyMinions.getMinionCount(); minionLoc++) {
                                SingleMove cardPlaying = new CardPlaying(curPlayerId, cardIndex, minionLoc, target.getEntityId());
                                add(selectedMove, cardPlaying, availableMoves);
                            }
                        }
                    }
                } else { // Minion card without battle cry target
                    for (int minionLoc = 0; minionLoc <= friendlyMinions.getMinionCount(); minionLoc++) {
                        SingleMove cardPlaying = new CardPlaying(curPlayerId, cardIndex, minionLoc);
                        add(selectedMove, cardPlaying, availableMoves);
                    }
                }
            } else {
                if (card.getTargetNeed().hasTarget()) { // Spell or Weapon card with target
                    for (Character target : currentGame.getTargets()) {
                        if (targetNeed.isAllowedTarget(target)) {
                            SingleMove cardPlaying = new CardPlaying(curPlayerId, cardIndex, target.getEntityId());
                            add(selectedMove, cardPlaying, availableMoves);
                        }
                    }
                } else { // Spell or Weapon card without target
                    SingleMove cardPlaying = new CardPlaying(curPlayerId, cardIndex);
                    add(selectedMove, cardPlaying, availableMoves);
                }
            }
        }

    }

    // Add the new Move
    private void add(Move parentMoves, SingleMove newMove, List<Move> moves) {
        Move newMoves = parentMoves.withNewMove(newMove);
        moves.add(newMoves);
    }

    /**
     * Applies the given {@link Move} to this {@code Board}.
     *
     * @param logMove whether to log the applied moves.
     */
    public void applyMoves(Move move, boolean logMove) {
        if (move.getActualMoves().isEmpty()) {
            if (logMove)
                LOG.info(getGame().getCurrentPlayer().getPlayerId() + " does nothing.");
            else
                LOG.debug(getGame().getCurrentPlayer().getPlayerId() + " does nothing.");
        }
        else
            move.getActualMoves().forEach((m) -> applyMove(m, logMove));
    }

    /**
     * Applies the given {@link Move} to this {@code Board}.
     */
    public void applyMoves(Move move) {
        applyMoves(move, false);
    }

    /**
     * Applies the given {@link SingleMove} to this {@code Board}.
     *
     * @param logMove whether to log the applied move
     */
    private void applyMove(SingleMove move, boolean logMove) {
        if (move == null)
            return;

        if (move instanceof CardPlaying) {
            CardPlaying cardPlaying = (CardPlaying) move;
            if (logMove)
                LOG.info(cardPlaying.toString(this));
            else if (LOG.isDebugEnabled())
                LOG.debug(cardPlaying.toString(this));
            playAgent.playCard(cardPlaying.getCardIndex(),
                new PlayTargetRequest(cardPlaying.getPlayerId(), cardPlaying.getMinionLocation(), cardPlaying.getTarget()));
        } else if (move instanceof HeroPowerPlaying) {
            HeroPowerPlaying heroPowerPlaying = (HeroPowerPlaying) move;
            if (logMove)
                LOG.info(heroPowerPlaying.toString(this));
            else if (LOG.isDebugEnabled())
                LOG.debug(heroPowerPlaying.toString(this));
            playAgent.playHeroPower(new PlayTargetRequest(heroPowerPlaying.getPlayerId(), -1, heroPowerPlaying.getTarget()));
        } else if (move instanceof DirectAttacking) {
            DirectAttacking directAttacking = (DirectAttacking) move;
            if (logMove)
                LOG.info(directAttacking.toString(this));
            else if (LOG.isDebugEnabled())
                LOG.debug(directAttacking.toString(this));
            playAgent.attack(directAttacking.getAttacker(), directAttacking.getTarget());
        }
    }

    /**
     * Returns a copy of this {@code Board}, which represents the same game state
     * without sharing any reference to same object with this {@code Board} unless
     * the object is immutable.
     */
    @Override
    public Board clone() {
        return new Board(this);
    }

    /**
     * Returns if the game is over (any player is dead).
     */
    public boolean isGameOver() {
        return playAgent.getGame().isGameOver();
    }

    /**
     * Returns the current {@link Player}.
     */
    public Player getCurrentPlayer() {
        return playAgent.getCurrentPlayer();
    }

    /**
     * Returns the opponent of the current player.
     */
    public Player getCurrentOpponent() {
        return playAgent.getGame().getOpponent(playAgent.getCurrentPlayerId());
    }

    /**
     * Returns the score of this game.
     *
     * @throws IllegalStateException if this method is invoked before the game ends.
     */
    public double getScore() {
        if (!isGameOver())
            throw new IllegalStateException("Error: Evaluating score before the game ends");
        if (playAgent.getGame().getPlayer(AI_PLAYER).getHero().isDead())
            return 0;
        else
            return 1;
    }

    public Game getGame() {
        return playAgent.getGame();
    }

    public String toString() {
        StringBuilder builder = new StringBuilder("====== Board ======\n");
        builder.append("Current Player: ").append(playAgent.getGame().getCurrentPlayer().getPlayerId());
        builder.append("\n------------------\n");
        Player aiOpponent = playAgent.getGame().getPlayer(AI_OPPONENT);
        builder.append("AiOpponent - Health: ").append(aiOpponent.getHero().getCurrentHp())
            .append(", Armor: ").append(aiOpponent.getHero().getCurrentArmor())
            .append("\nWeapon: ").append(aiOpponent.tryGetWeapon())
            .append("\nHero Power playable: ").append(aiOpponent.getHero().getHeroPower().isPlayable());
        builder.append("\nCards in Hand: ");
        for (Card card : aiOpponent.getHand().getCards())
            builder.append(card).append(", ");
        builder.append("\nCards in Deck: ").append(aiOpponent.getDeck().getNumberOfCards());
        builder.append("\n------------------\n");
        logBoardSide(builder, aiOpponent.getBoard());
        builder.append("\n------------------\n");
        Player aiPlayer = playAgent.getGame().getPlayer(AI_PLAYER);
        logBoardSide(builder, aiPlayer.getBoard());
        builder.append("\n------------------\n");
        builder.append("AiPlayer - Health: ").append(aiPlayer.getHero().getCurrentHp())
            .append(", Armor: ").append(aiPlayer.getHero().getCurrentArmor())
            .append("\nWeapon: ").append(aiPlayer.tryGetWeapon())
            .append("\nHero Power playable: ").append(aiPlayer.getHero().getHeroPower().isPlayable());
        builder.append("\nCards in Hand: ");
        for (Card card : aiPlayer.getHand().getCards())
            builder.append(card).append(", ");
        builder.append("\nCards in Deck: ").append(aiPlayer.getDeck().getNumberOfCards());
        builder.append("\n===================");
        return builder.toString();
    }

    private static void logBoardSide(StringBuilder builder, BoardSide board) {
        for (Minion minion : board.getAllMinions())
            builder.append(minion).append("\n");
    }
}
