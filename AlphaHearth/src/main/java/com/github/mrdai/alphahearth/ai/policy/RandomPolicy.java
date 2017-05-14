package com.github.mrdai.alphahearth.ai.policy;

import com.github.mrdai.alphahearth.Board;
import com.github.mrdai.alphahearth.move.*;
import info.hearthsim.brazier.TargeterDef;
import info.hearthsim.brazier.game.Character;
import info.hearthsim.brazier.game.Game;
import info.hearthsim.brazier.game.HeroPower;
import info.hearthsim.brazier.game.Player;
import info.hearthsim.brazier.game.cards.Card;
import info.hearthsim.brazier.game.minions.Minion;
import info.hearthsim.brazier.ui.PlayerTargetNeed;

import java.util.List;
import java.util.Random;

/**
 * A {@link DefaultPolicy} which produces {@link Move} by randomly selecting from all the
 * valid moves of the given {@link Board}.
 */
public class RandomPolicy implements DefaultPolicy {
    private final Random random = new Random();

    @Override
    public Move produceMode(Board board) {
        board = board.clone();
        Move.Builder builder = new Move.Builder();

        while (true) {
            int choice = random.nextInt(4);
            SingleMove move = null;
            if (choice == 0)
                move = minionAttack(board);
            else if (choice == 1)
                move = heroAttack(board);
            else if (choice == 2)
                move = cardPlaying(board);
            else if (choice == 3)
                move = heroPowerPlaying(board);

            if (move != null) {
                builder.addMove(move);
                move.applyTo(board);
            } else {
                break;
            }
        }

        return builder.build();
    }

    DirectAttacking minionAttack(Board board) {
        Game game = board.getGame();
        Player us = board.getCurrentPlayer();
        Player enemy = board.getCurrentOpponent();

        List<Minion> attackers = us.getBoard().findMinions((m) -> m.getAttackTool().canAttackWith());
        if (attackers.isEmpty())
            return null;

        // Randomly select attacker and target
        Minion attacker = randomElem(attackers);
        Character target;
        if (enemy.getBoard().hasNonStealthTaunt()) {
            List<Minion> validTargets = enemy.getBoard().findMinions((m) -> m.getBody().isTaunt());
            target = randomElem(validTargets);
        } else {
            List<Character> validTargets = game.getTargets((t) -> t.getOwner().getPlayerId() != us.getPlayerId());
            target = randomElem(validTargets);
        }
        return new DirectAttacking(attacker, target);
    }

    DirectAttacking heroAttack(Board board) {
        Game game = board.getGame();
        Player us = board.getCurrentPlayer();
        Player enemy = board.getCurrentOpponent();

        // Randomly select target
        if (us.getHero().getAttackTool().canAttackWith() && random.nextBoolean()) {
            Character target;
            if (enemy.getBoard().hasNonStealthTaunt()) {
                List<Minion> validTargets = enemy.getBoard().findMinions((m) -> m.getBody().isTaunt());
                target = randomElem(validTargets);
            } else {
                List<Character> validTargets = game.getTargets((t) -> t.getOwner().getPlayerId() != us.getPlayerId());
                target = randomElem(validTargets);
            }
            return new DirectAttacking(us.getHero(), target);
        }

        return null;
    }

    HeroPowerPlaying heroPowerPlaying(Board board) {
        Game game = board.getGame();
        Player us = board.getCurrentPlayer();

        // Randomly use Hero Power
        if (us.getHero().getHeroPower().isPlayable() && random.nextBoolean()) {
            HeroPower heroPower = us.getHero().getHeroPower();
            PlayerTargetNeed targetNeed =
                new PlayerTargetNeed(new TargeterDef(us.getPlayerId(), true, false), heroPower.getTargetNeed());
            if (!targetNeed.getTargetNeed().hasTarget()) {
                return new HeroPowerPlaying(us.getPlayerId());
            } else {
                List<Character> validTargets = game.getTargets(targetNeed::isAllowedTarget);
                Character target = randomElem(validTargets);
                return new HeroPowerPlaying(us.getPlayerId(), target);
            }
        }

        return null;
    }

    CardPlaying cardPlaying(Board board) {
        Game game = board.getGame();
        Player us = board.getCurrentPlayer();

        // Randomly use Card
        List<Card> availableCards = us.getHand().getCards((c) -> c.getActiveManaCost() <= us.getMana());
        if (availableCards.isEmpty()) // No more card, break it
            return null;

        Card card = randomElem(availableCards); // Randomly select a card
        PlayerTargetNeed targetNeed =
            new PlayerTargetNeed(new TargeterDef(us.getPlayerId(), true, false), card.getTargetNeed());
        if (card.isMinionCard()) {
            if (card.getTargetNeed().hasTarget()) { // Minion card with battle cry target
                List<Character> validTargets = game.getTargets(targetNeed::isAllowedTarget);
                if (validTargets.isEmpty())
                    return null;
                Character target = randomElem(validTargets);
                return new CardPlaying(card, random.nextInt(7), target);
            } else { // Minion card without battle cry target
                return new CardPlaying(card, random.nextInt(7));
            }
        } else {
            if (card.getTargetNeed().hasTarget()) { // Spell or Weapon card with target
                List<Character> validTargets = game.getTargets(targetNeed::isAllowedTarget);
                if (validTargets.isEmpty())
                    return null;
                Character target = randomElem(validTargets);
                return new CardPlaying(card, target);
            } else { // Spell or Weapon card without target
                return new CardPlaying(card);
            }
        }
    }

    private <T> T randomElem(List<T> list) {
        assert !list.isEmpty();
        return list.get(random.nextInt(list.size()));
    }

    public void close() {}
}
