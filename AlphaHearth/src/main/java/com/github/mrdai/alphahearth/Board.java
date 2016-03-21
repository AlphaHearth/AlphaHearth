package com.github.mrdai.alphahearth;

import info.hearthsim.brazier.*;
import info.hearthsim.brazier.Character;
import info.hearthsim.brazier.actions.PlayTargetRequest;
import info.hearthsim.brazier.cards.Card;
import info.hearthsim.brazier.ui.PlayerTargetNeed;
import com.github.mrdai.alphahearth.move.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Board {
    private static final PlayerId AI_PLAYER = new PlayerId("AiPlayer");
    private static final PlayerId AI_OPPONENT = new PlayerId("AiOpponent");

    private final GameAgent playAgent;

    public Board(HearthStoneDb db) {
        playAgent = new GameAgent(new Game(db, AI_PLAYER, AI_OPPONENT));
    }

    /**
     * Returns all of the available {@link Move}s for the current player.
     */
    public List<Move> getAvailableMoves() {
        List<Move> availableMoves = new LinkedList<>();
        availableMoves.add(Move.EMPTY_MOVE);
        Map<Move, Board> cachedBoards = new HashMap<>();

        for (int i = 0; i < availableMoves.size(); i++)
            expandMove(availableMoves, i, cachedBoards);

        return availableMoves;
    }

    private void expandMove(List<Move> availableMoves, int expandMoveIndex, Map<Move, Board> cachedBoards) {
        Move selectedMove = availableMoves.get(expandMoveIndex);
        Board cachedBoard = cachedBoards.computeIfAbsent(selectedMove, (move) -> {
            Board newBoard = this.clone();
            newBoard.applyMoves(move);
            return newBoard;
        });

        Game currentGame = cachedBoard.playAgent.getGame();
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
                addAndCache(selectedMove, heroPowerPlaying, cachedBoard, availableMoves, cachedBoards);
            } else {
                currentGame.getTargets().stream().filter(targetNeed::isAllowedTarget).forEach((target) -> {
                    SingleMove heroPowerPlaying = new HeroPowerPlaying(curPlayerId, target.getEntityId());
                    addAndCache(selectedMove, heroPowerPlaying, cachedBoard, availableMoves, cachedBoards);
                });
            }
        }

        // List direct attack
        currentGame.getTargets((target) -> target.getOwner() == curPlayer && target.getAttackTool().canAttackWith())
            .forEach((attacker) -> {
                if (enemyMinions.hasNonStealthTaunt()) {
                    enemyMinions.getMinions((minion) -> minion.getBody().isTaunt() && !minion.getBody().isStealth())
                        .forEach((target) -> {
                            SingleMove directAttacking = new DirectAttacking(attacker.getEntityId(), target.getEntityId());
                            addAndCache(selectedMove, directAttacking, cachedBoard, availableMoves, cachedBoards);
                        });
                } else {
                    if (!enemyHero.isImmune()) {
                        SingleMove directAttacking = new DirectAttacking(attacker.getEntityId(), enemyHero.getEntityId());
                        addAndCache(selectedMove, directAttacking, cachedBoard, availableMoves, cachedBoards);
                    }
                    enemyMinions.getMinions((minion) -> !minion.getBody().isStealth() && !minion.getBody().isImmune())
                        .forEach((target) -> {
                            SingleMove directAttacking = new DirectAttacking(attacker.getEntityId(), target.getEntityId());
                            addAndCache(selectedMove, directAttacking, cachedBoard, availableMoves, cachedBoards);
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
                                addAndCache(selectedMove, cardPlaying, cachedBoard, availableMoves, cachedBoards);
                            }
                        }
                    }
                } else { // Minion card without battle cry target
                    for (int minionLoc = 0; minionLoc <= friendlyMinions.getMinionCount(); minionLoc++) {
                        SingleMove cardPlaying = new CardPlaying(curPlayerId, cardIndex, minionLoc);
                        addAndCache(selectedMove, cardPlaying, cachedBoard, availableMoves, cachedBoards);
                    }
                }
            } else {
                if (card.getTargetNeed().hasTarget()) { // Spell or Weapon card with target
                    for (Character target : currentGame.getTargets()) {
                        if (targetNeed.isAllowedTarget(target)) {
                            SingleMove cardPlaying = new CardPlaying(curPlayerId, cardIndex, target.getEntityId());
                            addAndCache(selectedMove, cardPlaying, cachedBoard, availableMoves, cachedBoards);
                        }
                    }
                } else { // Spell or Weapon card without target
                    SingleMove cardPlaying = new CardPlaying(curPlayerId, cardIndex);
                    addAndCache(selectedMove, cardPlaying, cachedBoard, availableMoves, cachedBoards);
                }
            }
        }

    }

    // Add the new Move and cache the new Board
    private void addAndCache(Move parentMoves, SingleMove newMove, Board parentBoard,
                             List<Move> moves, Map<Move, Board> cachedBoards) {
        Move newMoves = parentMoves.withNewMove(newMove);
        moves.add(newMoves);
        Board newBoard = parentBoard.clone();
        newBoard.applyMove(newMove);
        cachedBoards.put(newMoves, newBoard);
    }

    /**
     * Applies the given {@link Move} to this {@code Board}.
     */
    public void applyMoves(Move move) {
        move.getActualMoves().forEach(this::applyMove);
    }

    /**
     * Applies the given {@link SingleMove} to this {@code Board}.
     */
    public void applyMove(SingleMove move) {
        if (move == null)
            return;

        if (move instanceof CardPlaying) {
            CardPlaying cardPlaying = (CardPlaying) move;
            playAgent.playCard(cardPlaying.getCardIndex(),
                new PlayTargetRequest(cardPlaying.getPlayerId(), cardPlaying.getMinionLocation(), cardPlaying.getTarget()));
        } else if (move instanceof HeroPowerPlaying) {
            HeroPowerPlaying heroPowerPlaying = (HeroPowerPlaying) move;
            playAgent.playHeroPower(new PlayTargetRequest(heroPowerPlaying.getPlayerId(), -1, heroPowerPlaying.getTarget()));
        } else if (move instanceof DirectAttacking) {
            DirectAttacking directAttacking = (DirectAttacking) move;
            playAgent.attack(directAttacking.getAttacker(), directAttacking.getTarget());
        }
    }

    /**
     * Returns a clone of this {@code Board}, which represents the same game state
     * without sharing any reference to same object with this {@code Board} unless
     * the object is immutable.
     */
    @Override
    public Board clone() {
        // TODO To be implemented
        return null;
    }

    /**
     * Returns if the game is over (any player is dead).
     */
    public boolean isGameOver() {
        return playAgent.getGame().isGameOver();
    }

    /**
     * Returns the id of the current player. {@code 0} stands for the AI player
     * while {@code 1} stands for the opponent.
     */
    public int getCurrentPlayer() {
        return playAgent.getCurrentPlayer().getPlayerId() == AI_PLAYER ? 0 : 1;
    }

    public int getQuantityOfPlayers() {
        // TODO To be implemented
        return 0;
    }

    public double[] pessimisticBounds() {
        // TODO To be implemented
        return new double[0];
    }

    public double[] optimisticBounds() {
        // TODO To be implemented
        return new double[0];
    }

    public double[] getScore() {
        // TODO To be implemented
        return new double[0];
    }
}
