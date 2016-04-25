package com.github.mrdai.alphahearth;

import info.hearthsim.brazier.*;
import info.hearthsim.brazier.game.*;
import info.hearthsim.brazier.game.cards.Card;
import info.hearthsim.brazier.game.Character;
import info.hearthsim.brazier.game.minions.Minion;
import info.hearthsim.brazier.game.weapons.AttackTool;
import info.hearthsim.brazier.ui.PlayerTargetNeed;
import com.github.mrdai.alphahearth.move.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.github.mrdai.alphahearth.AiGameAgent.AI_OPPONENT;
import static com.github.mrdai.alphahearth.AiGameAgent.AI_PLAYER;
import static com.github.mrdai.alphahearth.BoardUtils.*;

public class Board {
    private static final Logger LOG = LoggerFactory.getLogger(Board.class);

    public final GameAgent playAgent;

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
        DistinctMoveList availableMoves = new DistinctMoveList();
        availableMoves.add(clone(), Move.EMPTY_MOVE);

        for (int i = 0; i < availableMoves.size(); i++) {
            expandMove(availableMoves, i);
            if (LOG.isTraceEnabled())
                LOG.trace("Move list size: " + availableMoves.size());
            if (availableMoves.size() > 1000)
                break;
        }

        return availableMoves.toMoveList(500);
    }

    private void expandMove(DistinctMoveList availableMoves, int expandMoveIndex) {
        Move selectedMove = availableMoves.get(expandMoveIndex);

        Board copiedBoard = clone();
        copiedBoard.applyMoves(selectedMove);
        Game currentGame = copiedBoard.playAgent.getGame();
        Player curPlayer = currentGame.getCurrentPlayer();
        PlayerId curPlayerId = curPlayer.getPlayerId();
        Player curOpponent = currentGame.getCurrentOpponent();
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
                HeroPowerPlaying heroPowerPlaying = new HeroPowerPlaying(curPlayerId);
                if (LOG.isTraceEnabled())
                    LOG.trace("Adding " + heroPowerPlaying.toString(copiedBoard) + " on\n" + copiedBoard);
                add(selectedMove, heroPowerPlaying, availableMoves);
            } else {
                currentGame.getTargets().stream().filter(targetNeed::isAllowedTarget).forEach((target) -> {
                    HeroPowerPlaying heroPowerPlaying = new HeroPowerPlaying(curPlayerId, target);
                    if (LOG.isTraceEnabled())
                        LOG.trace("Adding " + heroPowerPlaying.toString(copiedBoard) + " on\n" + copiedBoard);
                    add(selectedMove, heroPowerPlaying, availableMoves);
                });
            }
        }

        // List Hero Attack
        if (friendlyHero.getAttackTool().canAttackWith()) {
            List<Minion> targetMinions;
            if (enemyMinions.hasNonStealthTaunt()) {
                targetMinions = enemyMinions.findMinions((m) -> m.getBody().isTaunt() && !m.getBody().isStealth() && !m.getBody().isImmune());
            } else {
                // Attacking enemy hero
                if (!enemyHero.isImmune()) {
                    DirectAttacking directAttacking = new DirectAttacking(friendlyHero, enemyHero);
                    if (LOG.isTraceEnabled())
                        LOG.trace("Adding " + directAttacking.toString(copiedBoard) + " on\n" + copiedBoard);
                    add(selectedMove, directAttacking, availableMoves);
                }
                targetMinions = enemyMinions.findMinions((m) -> !m.getBody().isStealth() && !m.getBody().isImmune());
            }
            // Attacking enemy minions
            for (Minion target : targetMinions) {
                DirectAttacking directAttacking = new DirectAttacking(friendlyHero, target);
                if (LOG.isTraceEnabled())
                    LOG.trace("Adding " + directAttacking.toString(copiedBoard) + " on\n" + copiedBoard);
                add(selectedMove, directAttacking, availableMoves);
            }
        }

        // List Minion attack
        List<Minion> attackers = friendlyMinions.findMinions((m) -> m.getAttackTool().canAttackWith());
        List<Minion> targetMinions;
        if (enemyMinions.hasNonStealthTaunt()) {
            targetMinions = enemyMinions.findMinions((m) -> m.getBody().isTaunt() && !m.getBody().isStealth() && !m.getBody().isImmune());
        } else {
            if (!enemyHero.isImmune()) { // Attack enemy hero
                for (Minion attacker : attackers) {
                    DirectAttacking directAttacking = new DirectAttacking(attacker, enemyHero);
                    if (LOG.isTraceEnabled())
                        LOG.trace("Adding " + directAttacking.toString(copiedBoard) + " on\n" + copiedBoard);
                    add(selectedMove, directAttacking, availableMoves);
                }
            }
            targetMinions = enemyMinions.findMinions((m) -> !m.getBody().isStealth() && !m.getBody().isImmune());
        }
        for (Minion attacker : attackers) {
            for (Minion target : targetMinions) {
                DirectAttacking directAttacking = new DirectAttacking(attacker, target);
                if (LOG.isTraceEnabled())
                    LOG.trace("Adding " + directAttacking.toString(copiedBoard) + " on\n" + copiedBoard);
                add(selectedMove, directAttacking, availableMoves);
            }
        }

        // List Card
        Hand hand = curPlayer.getHand();
        for (Card card : hand.getCards()) {
            if (card.getActiveManaCost() > curPlayer.getMana())
                continue;

            PlayerTargetNeed targetNeed =
                new PlayerTargetNeed(new TargeterDef(curPlayerId, true, false), card.getTargetNeed());
            if (card.isMinionCard()) {
                if (card.getTargetNeed().hasTarget()) { // Minion card with battle cry target
                    for (Character target : currentGame.getTargets()) {
                        if (targetNeed.isAllowedTarget(target)) {
                            for (int minionLoc = 0; minionLoc <= friendlyMinions.getMinionCount(); minionLoc++) {
                                CardPlaying cardPlaying = new CardPlaying(card, minionLoc, target);
                                if (LOG.isTraceEnabled())
                                    LOG.trace("Adding " + cardPlaying.toString(copiedBoard) + " on " + copiedBoard);
                                add(selectedMove, cardPlaying, availableMoves);
                            }
                        }
                    }
                } else { // Minion card without battle cry target
                    for (int minionLoc = 0; minionLoc <= friendlyMinions.getMinionCount(); minionLoc++) {
                        CardPlaying cardPlaying = new CardPlaying(card, minionLoc);
                        if (LOG.isTraceEnabled())
                            LOG.trace("Adding " + cardPlaying.toString(copiedBoard) + " on " + copiedBoard);
                        add(selectedMove, cardPlaying, availableMoves);
                    }
                }
            } else {
                if (card.getTargetNeed().hasTarget()) { // Spell or Weapon card with target
                    for (Character target : currentGame.getTargets()) {
                        if (targetNeed.isAllowedTarget(target)) {
                            CardPlaying cardPlaying = new CardPlaying(card, -1, target);
                            if (LOG.isTraceEnabled())
                                LOG.trace("Adding " + cardPlaying.toString(copiedBoard) + " on " + copiedBoard);
                            add(selectedMove, cardPlaying, availableMoves);
                        }
                    }
                } else { // Spell or Weapon card without target
                    CardPlaying cardPlaying = new CardPlaying(card);
                    if (LOG.isTraceEnabled())
                        LOG.trace("Adding " + cardPlaying.toString(copiedBoard) + " on " + copiedBoard);
                    add(selectedMove, cardPlaying, availableMoves);
                }
            }
        }

    }

    // Add the new Move
    private void add(Move parentMoves, SingleMove newMove, DistinctMoveList moves) {
        Move newMoves = parentMoves.withNewMove(newMove);
        moves.add(clone(), newMoves);
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
                LOG.trace(getGame().getCurrentPlayer().getPlayerId() + " does nothing.");
        } else
            move.getActualMoves().forEach((m) -> m.applyTo(this, logMove));
    }

    /**
     * Applies the given {@link Move} to this {@code Board}.
     */
    public void applyMoves(Move move) {
        applyMoves(move, false);
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
     * Returns if the game is over and the player with the given id has won.
     */
    public boolean hasWon(PlayerId playerId) {
        return isGameOver() && !getGame().getPlayer(playerId).getHero().isDead();
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
    public double getScore(PlayerId playerId) {
        double TURN_PENALTY = 0.97;

        return getScore(TURN_PENALTY, playerId);
    }

    public double getScore(double turnPenalty, PlayerId playerId) {
        double TURN_PENALTY = 0.97;

        if (!isGameOver())
            throw new IllegalStateException("Error: Evaluating score before the game ends");
        Player aiPlayer = playAgent.getGame().getPlayer(playerId);
        if (aiPlayer.getHero().isDead())
            return 0;
        else
            return Math.pow(TURN_PENALTY, aiPlayer.getTurnNum());
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
        builder.append("\nMana: " + aiOpponent.getManaResource().getMana() + "/" + aiOpponent.getManaResource().getManaCrystals());
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
        builder.append("\nMana: " + aiPlayer.getManaResource().getMana() + "/" + aiPlayer.getManaResource().getManaCrystals());
        builder.append("\n===================");
        return builder.toString();
    }

    private static void logBoardSide(StringBuilder builder, BoardSide board) {
        for (Minion minion : board.getAllMinions())
            builder.append(minion).append("\n");
    }

    @Override
    public int hashCode() {
        return (int) getValue();
    }

    /**
     * Returns the coarse value of this current {@code Board}. More advantageous the current player is,
     * higher the value is. If two given {@code Board} {@code a} and {@code b} has {@code a.equals(b) = true},
     * {@code a.getValue() = b.getValue()}. If {@code a.getValue() != b.getValue()}, {@code a.equals(b) = false}.
     * <p>
     * This method is also used as the inner implementation of {@link #hashCode()}.
     */
    public double getValue() {
        double HERO_HEALTH_FACTOR = 1;
        double MINION_COST_FACTOR = 0.4;
        double HAND_SIZE_FACTOR = 0.3;
        double UNATTACKED_PENALTY = 1.2;
        double UNUSED_CARD_PENALTY = 0.4;

        int result = 0;

        Game game = getGame();

        Player us = game.getCurrentPlayer();
        Player enemy = game.getCurrentOpponent();

        // Calculate instant-death situation
        if (us.getHero().isDead())
            return Integer.MIN_VALUE;
        if (enemy.getHero().isDead())
            return Integer.MAX_VALUE;

        int ourAttackPoint = 0;
        int enemyAttackPoint = 0;

        List<Minion> friendlyMinions = us.getBoard().getAllMinions();
        List<Minion> enemyMinions = enemy.getBoard().getAllMinions();

        for (Minion minion : friendlyMinions) {
            // Calculate minions
            result += MINION_COST_FACTOR * minion.getCard().getCardDescr().getManaCost();

            AttackTool attack = minion.getAttackTool();
            ourAttackPoint += attack.getAttack() * attack.getMaxAttackCount();

            if (attack.canAttackWith())
                result -= UNATTACKED_PENALTY * attack.getAttack();
        }
        for (Minion minion : enemyMinions) {
            result -= MINION_COST_FACTOR * minion.getCard().getCardDescr().getManaCost();

            AttackTool attack = minion.getAttackTool();
            enemyAttackPoint += attack.getAttack() * attack.getMaxAttackCount();
        }

        Hero enemyHero = enemy.getHero();
        enemyAttackPoint += enemyHero.getAttackTool().getAttack() * enemyHero.getAttackTool().getMaxAttackCount();

        Hero ourHero = us.getHero();
        ourAttackPoint += ourHero.getAttackTool().getAttack() * enemyHero.getAttackTool().getMaxAttackCount();
        if (ourHero.getAttackTool().canAttackWith())
            result -= UNATTACKED_PENALTY * ourHero.getAttackTool().getAttack();

        // Calculate left health in next turn for both heroes.
        int enemyLeftHealth = enemyHero.getCurrentHp() + enemyHero.getCurrentArmor() - ourAttackPoint;
        int ourLeftHealth = ourHero.getCurrentHp() + ourHero.getCurrentArmor() - enemyAttackPoint;

        result += HERO_HEALTH_FACTOR * (Math.log(-enemyLeftHealth) - Math.log(-ourLeftHealth));

        // Calculate hand size distance
        result += HAND_SIZE_FACTOR * (us.getHand().getCardCount() + us.getSecrets().getSecrets().size()
                                          - enemy.getHand().getCardCount() - enemy.getSecrets().getSecrets().size());

        // Calculate unused card penalty
        int mana = us.getMana();
        for (Card card : us.getHand().getCards()) {
            if (card.getActiveManaCost() <= mana)
                result -= UNUSED_CARD_PENALTY * card.getActiveManaCost();
        }

        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Board))
            return false;
        Board otherBoard = (Board) other;

        // Compare Hero
        if (!compareHero(getGame().getPlayer1().getHero(), otherBoard.getGame().getPlayer1().getHero()))
            return false;
        if (!compareHero(getGame().getPlayer2().getHero(), otherBoard.getGame().getPlayer2().getHero()))
            return false;

        // Compare Weapon
        if (!compareWeapon(getGame().getPlayer1().tryGetWeapon(), otherBoard.getGame().getPlayer1().tryGetWeapon()))
            return false;
        if (!compareWeapon(getGame().getPlayer2().tryGetWeapon(), otherBoard.getGame().getPlayer2().tryGetWeapon()))
            return false;

        // Compare Deck Size
        if (getGame().getPlayer1().getDeck().getNumberOfCards() != otherBoard.getGame().getPlayer1().getDeck().getNumberOfCards())
            return false;
        if (getGame().getPlayer2().getDeck().getNumberOfCards() != otherBoard.getGame().getPlayer2().getDeck().getNumberOfCards())
            return false;

        // Compare Fatigue Damage
        if (getGame().getPlayer1().getFatigueDamage() != otherBoard.getGame().getPlayer1().getFatigueDamage())
            return false;
        if (getGame().getPlayer2().getFatigueDamage() != otherBoard.getGame().getPlayer2().getFatigueDamage())
            return false;

        // Compare Hand Size
        if (getGame().getPlayer1().getHand().getCardCount() != otherBoard.getGame().getPlayer1().getHand().getCardCount())
            return false;
        if (getGame().getPlayer2().getHand().getCardCount() != otherBoard.getGame().getPlayer2().getHand().getCardCount())
            return false;

        // Compare Minion on board
        if (!compareBoardSide(getGame().getPlayer1().getBoard(), otherBoard.getGame().getPlayer1().getBoard()))
            return false;
        if (!compareBoardSide(getGame().getPlayer2().getBoard(), otherBoard.getGame().getPlayer2().getBoard()))
            return false;

        // Compare Spell Damage
        if (getGame().getPlayer1().getSpellPower().getValue() != otherBoard.getGame().getPlayer1().getSpellPower().getValue())
            return false;
        if (getGame().getPlayer2().getSpellPower().getValue() != otherBoard.getGame().getPlayer2().getSpellPower().getValue())
            return false;

        // Compare Secrets
        if (!compareSecrets(getGame().getPlayer1().getSecrets(), otherBoard.getGame().getPlayer1().getSecrets()))
            return false;
        if (!compareSecrets(getGame().getPlayer2().getSecrets(), otherBoard.getGame().getPlayer2().getSecrets()))
            return false;

        // Compare Mana Resource
        if (!compareMana(getGame().getPlayer1().getManaResource(), otherBoard.getGame().getPlayer1().getManaResource()))
            return false;
        if (!compareMana(getGame().getPlayer2().getManaResource(), otherBoard.getGame().getPlayer2().getManaResource()))
            return false;

        return true;
    }
}
