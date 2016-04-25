package com.github.mrdai.alphahearth.ai.policy;

import com.github.mrdai.alphahearth.Board;
import com.github.mrdai.alphahearth.move.*;
import info.hearthsim.brazier.game.Hand;
import info.hearthsim.brazier.game.Hero;
import info.hearthsim.brazier.game.HeroPower;
import info.hearthsim.brazier.game.Player;
import info.hearthsim.brazier.game.cards.Card;
import info.hearthsim.brazier.game.minions.Minion;
import info.hearthsim.brazier.game.weapons.Weapon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A {@code DefaultPolicy} who produces {@link Move} based on predefined rules.
 */
public class ExpertRuleBasedPolicy implements DefaultPolicy {
    private static final Logger LOG = LoggerFactory.getLogger(ExpertRuleBasedPolicy.class);
    private static final Comparator<Minion> CMP = new MinionPowerComparer();

    @Override
    public Move produceMode(Board board) {
        board = board.clone();
        Move.Builder builder = new Move.Builder();
        int i = 0;
        while (i < 30) {
            SingleMove move = produceSingleMove(board);
            if (move == null)
                break;
            move.applyTo(board);
            builder.addMove(move);
            i++;
        }
        return builder.build();
    }

    protected SingleMove produceSingleMove(Board board) {
        Player us = board.getCurrentPlayer();
        Player enemy = board.getCurrentOpponent();

        // Calculate the enemy's total attack point
        int enemyAttackPoint = 0;
        for (Minion minion : enemy.getBoard().findMinions((m) -> m.getAttackTool().canAttackWith()))
            enemyAttackPoint += minion.getAttackTool().getAttack() * minion.getAttackTool().getMaxAttackCount();
        Hero enemyHero = enemy.getHero();
        enemyAttackPoint += enemyHero.getAttackTool().getAttack() * enemyHero.getAttackTool().getMaxAttackCount();

        // Generate `CardPlaying` move
        SingleMove move = produceCardPlaying(us, enemy, enemyAttackPoint);
        if (move != null) {
            LOG.trace(move.toString(board));
            return move;
        }


        // Fetch friendly attackers and enemy targets
        List<Minion> enemyMinions = enemy.getBoard().getAliveMinions();
        List<Minion> enemyTaunt = enemy.getBoard().findMinions((m) -> m.getBody().isTaunt() && !m.getBody().isStealth());
        List<Minion> enemyDangerous = enemy.getBoard().findMinions((m) -> isEnemyDangerous(m));

        List<Minion> friendlyAttackers = us.getBoard().findMinions((m) -> m.getAttackTool().canAttackWith());
        List<Minion> friendlyTauntAttackers = friendlyAttackers.stream()
                                              .filter((m) -> m.getBody().isTaunt()).collect(Collectors.toList());
        List<Minion> friendlyNonTauntAttackers = friendlyAttackers.stream()
                                                 .filter((m) -> !m.getBody().isTaunt()).collect(Collectors.toList());
        enemyMinions.sort(CMP);
        enemyTaunt.sort(CMP);
        friendlyNonTauntAttackers.sort(CMP.reversed());
        friendlyTauntAttackers.sort(CMP.reversed());
        friendlyAttackers.sort(CMP.reversed());

        // Generate Minion Attack Move

        // Use friendly non-taunt minions to deal with enemy's taunt minions
        move = playKill(friendlyNonTauntAttackers, enemyTaunt);
        if (move != null) {
            LOG.trace(move.toString(board));
            return move;
        }

        // Use friendly taunt minions to deal with enemy's taunt minions
        move = playKill(friendlyTauntAttackers, enemyTaunt);
        if (move != null) {
            LOG.trace(move.toString(board));
            return move;
        }

        // At this point, no more friendly minion can kill any of enemy taunt minions
        // So, just attack them blindly.
        if (!enemyTaunt.isEmpty() && !friendlyAttackers.isEmpty()) {
            move = new DirectAttacking(friendlyAttackers.get(0), enemyTaunt.get(0));
            LOG.trace(move.toString(board));
            return move;
        }

        // Use friendly non-taunt minions to deal with enemy's dangerous minions
        move = playKill(friendlyNonTauntAttackers, enemyDangerous);
        if (move != null) {
            LOG.trace(move.toString(board));
            return move;
        }

        // At this point, no more friendly non-taunt minion can kill any of enemy dangerous minions
        // So, just attack them blindly.
        if (!friendlyNonTauntAttackers.isEmpty() && !enemyDangerous.isEmpty()) {
            move = new DirectAttacking(friendlyNonTauntAttackers.get(0), enemyDangerous.get(0));
            LOG.trace(move.toString(board));
            return move;
        }

        // At this point, if there is still any available friendly attackers,
        // there would be no taunt minion on enemy's side.

        // Deal with enemy non-taunt minions if enemy can kill us in the next turn.
        if (enemyTaunt.isEmpty() && us.getHero().getCurrentHp() - enemyAttackPoint <= 0) {
            for (Minion target : enemyMinions) {
                if (target.getAttackTool().getAttack() - target.getBody().getCurrentHp() > 1) {
                    Minion bestAttacker = bestAttacker(friendlyAttackers, target);
                    if (bestAttacker != null) {
                        move = new DirectAttacking(bestAttacker, target);
                        LOG.trace(move.toString(board));
                        return move;
                    }
                }
            }
        }

        // Deal with enemy non-taunt minions if enemy's attack point is high enough to threaten us.
        if (enemyTaunt.isEmpty() && us.getHero().getCurrentHp() - enemyAttackPoint <= 10) {
            for (Minion target : enemyMinions) {
                if (target.getAttackTool().getAttack() - target.getBody().getCurrentHp() > 3) {
                    Minion bestAttacker = bestAttacker(friendlyAttackers, target);
                    if (bestAttacker != null) {
                        move = new DirectAttacking(bestAttacker, target);
                        LOG.trace(move.toString(board));
                        return move;
                    }
                }
            }
        }

        // Attack enemy's face
        if (!friendlyAttackers.isEmpty()) {
            move = new DirectAttacking(friendlyAttackers.get(0), enemyHero);
            LOG.trace(move.toString(board));
            return move;
        }

        // Play Hero Power
        if (us.getMana() >= 2 && us.getHero().getHeroPower().isPlayable()) {
            HeroPower heroPower = us.getHero().getHeroPower();
            String name = heroPower.getPowerDef().getName();
            switch (name) {
                case "Life Tap": // Warlock
                    if (us.getHand().getCardCount() <= 8 && us.getHero().getCurrentHp() >= 5)
                        return new HeroPowerPlaying(us.getPlayerId());
                    if (us.getHero().getCurrentHp() < 12 && us.getHero().getCurrentHp() > enemyAttackPoint + 2)
                        return new HeroPowerPlaying(us.getPlayerId());
                    if (us.getHero().getCurrentHp() >= 12)
                        return new HeroPowerPlaying(us.getPlayerId());
                    break;
                case "Fireblast": // Deal damage
                case "Mind Shatter":
                case "Mind Spike":
                    return new HeroPowerPlaying(us.getPlayerId(), enemyHero);
                case "Reinforce": // Summon minion
                case "INFERNO!":
                case "Totemic Call":
                    if (!us.getBoard().isFull())
                        return new HeroPowerPlaying(us.getPlayerId());
                    break;
                case "Dagger Mastery":
                    Weapon weapon = us.tryGetWeapon();
                    if (weapon == null || weapon.getAttack() <= 1 && weapon.getDurability() <= 2)
                        return new HeroPowerPlaying(us.getPlayerId());
                    break;
                default:
                    return new HeroPowerPlaying(us.getPlayerId());
            }
        }

        // Attack with Hero
        if (us.getHero().getAttackTool().canAttackWith()) {
            if (enemyTaunt.isEmpty()) {
                move = new DirectAttacking(us.getHero(), enemyHero);
                LOG.trace(move.toString(board));
                return move;
            }
        }

        return null;
    }

    private static CardPlaying produceCardPlaying(Player us, Player enemy, int enemyAttackPoint) {
        CardPlaying emergencyCardPlaying = playEmergencyCard(us.getMana(), us.getHand(), us.getHero(), enemy.getHero(), enemyAttackPoint);
        if (emergencyCardPlaying != null)
            return emergencyCardPlaying;

        // Generate CardPlaying Move
        List<Card> handCards = us.getHand().getCards();
        for (Card card : handCards) {
            // Not the coin
            if (card.getCardDescr().getName().equals("The Coin"))
                continue;
            if (card.getActiveManaCost() > us.getMana())
                continue;

            // Play Animal Companion
            if (card.getCardDescr().getName().equals("Animal Companion"))
                return new CardPlaying(card);

            Minion minion = card.getMinion();
            if (minion != null && !us.getBoard().isFull()) {
                if (us.getBoard().getMinionCount() < 6 || minion.isCharge() || minion.getBody().isTaunt())
                    return new CardPlaying(card, 0);
            }
        }

        return null;
    }

    /**
     * For the given list of enemy targets, produces a {@link DirectAttacking} if a best attacker can be found
     * for any target from the given lists.
     * <p>
     * Returns {@code null} if no such pair of attacker and target can be found.
     *
     * @see #bestAttacker(List, Minion)
     */
    private static DirectAttacking playKill(List<Minion> friendlyAttackers, List<Minion> enemyTargets) {
        Minion bestAttacker;

        for (Minion target : enemyTargets) {
            bestAttacker = bestAttacker(friendlyAttackers, target);
            if (bestAttacker != null)
                return new DirectAttacking(bestAttacker, target);
        }

        return null;
    }

    /**
     * Given a list of candidate attackers and enemy target, return the best attacker for the given target
     * from the given list.
     * <p>
     * The best attacker would be the minion with the least attack that can kill the target.
     * <p>
     * Returns {@code null} if there is no attacker from the list can kill the target.
     */
    private static Minion bestAttacker(List<Minion> attackerCandidates, Minion target) {
        Minion bestAttacker = null;

        for (Minion attacker : attackerCandidates) {
            if (target.getBody().getCurrentHp() <= attacker.getAttackTool().getAttack())
                bestAttacker = attacker;
            else if (bestAttacker != null && bestAttacker.getAttackTool().getAttack() > attacker.getAttackTool().getAttack())
                bestAttacker = attacker;
        }

        return bestAttacker;
    }

    private static CardPlaying playEmergencyCard(int mana, Hand hand, Hero friendlyHero,
                                                 Hero enemyHero, int enemyAttackPoint) {
        List<Card> handCards = hand.getCards();
        for (Card card : handCards) {
            if (mana < card.getActiveManaCost())
                continue;
            Minion minion = card.getMinion();
            if (minion != null && minion.getBody().isTaunt() && !friendlyHero.getOwner().getBoard().isFull()
                && (card.getActiveManaCost() == mana || card.getActiveManaCost() == mana - 2))
                return new CardPlaying(card, 0);
        }

        if (friendlyHero.getCurrentHp() - enemyAttackPoint < 10) {
            for (Card card : handCards) {
                if (mana < card.getActiveManaCost())
                    continue;
                Minion minion = card.getMinion();
                if (minion != null && minion.getBody().isTaunt() && !friendlyHero.getOwner().getBoard().isFull()
                    && (card.getActiveManaCost() == mana - 1 || card.getActiveManaCost() == mana - 3))
                    return new CardPlaying(card, 0);
            }
        }

        if (friendlyHero.getCurrentHp() - enemyHero.getCurrentHp() > 10) {
            for (Card card : handCards) {
                if (mana < card.getActiveManaCost())
                    continue;
                Minion minion = card.getMinion();
                if (minion != null && minion.isCharge() && !friendlyHero.getOwner().getBoard().isFull())
                    return new CardPlaying(card, 0);
            }
        }

        return null;
    }

    private static boolean isEnemyDangerous(Minion minion) {
        int attack = minion.getAttackTool().getAttack();
        if (attack > 4)
            return true;

        int health = minion.getBody().getCurrentHp();
        if (health > 4 && attack - health > 4)
            return true;

        if (health > 2 && attack - health > 3)
            return true;

        return attack - health > 2;
    }

    private static class MinionPowerComparer implements Comparator<Minion> {
        public int compare(Minion x, Minion y) {
            int powerX = getPower(x);
            int powerY = getPower(y);
            return powerX - powerY;
        }

        private int getPower(Minion m) {
            int power = m.getAttackTool().getAttack() * m.getAttackTool().getMaxAttackCount()
                        - m.getBody().getCurrentHp();
            if (m.getBody().isDivineShield())
                power -= 1;
            return power;
        }
    }

}
