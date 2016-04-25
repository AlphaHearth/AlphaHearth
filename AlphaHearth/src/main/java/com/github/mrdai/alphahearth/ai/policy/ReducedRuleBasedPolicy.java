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
import info.hearthsim.brazier.game.weapons.Weapon;
import info.hearthsim.brazier.ui.PlayerTargetNeed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * A {@code DefaultPolicy} who produces {@link Move} based on predefined rules.
 */
public class ReducedRuleBasedPolicy extends ExpertRuleBasedPolicy implements DefaultPolicy {
    private static final Logger LOG = LoggerFactory.getLogger(ReducedRuleBasedPolicy.class);
    private static final Comparator<Card> CMP = new CardPowerComparer();

    private final Random random = new Random();

    protected SingleMove produceSingleMove(Board board) {
        Game game = board.getGame();

        Player us = board.getCurrentPlayer();
        Player enemy = board.getCurrentOpponent();

        // Minion Attack
        for (Minion minion : us.getBoard().findMinions((m) -> m.getAttackTool().canAttackWith())) {
            if (!enemy.getBoard().hasNonStealthTaunt())
                return new DirectAttacking(minion, enemy.getHero());
            else
                return new DirectAttacking(minion, enemy.getBoard().findMinion(
                    (m) -> m.getBody().isTaunt() && !m.getBody().isStealth()
                ));
        }

        // Play Card
        List<Card> cardsToPlay = us.getHand().getCards((c) -> c.getActiveManaCost() <= us.getMana());
        Card card = cardsToPlay.stream().max(CMP).get();
        if (card != null) {
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

        // Play Hero Power
        if (us.getMana() >= 2 && us.getHero().getHeroPower().isPlayable()) {
            HeroPower heroPower = us.getHero().getHeroPower();
            String name = heroPower.getPowerDef().getName();
            switch (name) {
                case "Life Tap": // Warlock
                    if (us.getHand().getCardCount() <= 8 && us.getHero().getCurrentHp() >= 5)
                        return new HeroPowerPlaying(us.getPlayerId());
                    if (us.getHero().getCurrentHp() >= 12)
                        return new HeroPowerPlaying(us.getPlayerId());
                    break;
                case "Fireblast": // Deal damage
                case "Mind Shatter":
                case "Mind Spike":
                    return new HeroPowerPlaying(us.getPlayerId(), enemy.getHero());
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

        // Hero Attack
        if (us.getHero().getAttackTool().canAttackWith()) {
            if (!enemy.getBoard().hasNonStealthTaunt())
                return new DirectAttacking(us.getHero(), enemy.getHero());
            else
                return new DirectAttacking(us.getHero(), enemy.getBoard().findMinion(
                    (m) -> m.getBody().isTaunt() && !m.getBody().isStealth()
                ));
        }

        return null;
    }

    private static class CardPowerComparer implements Comparator<Card> {
        @Override
        public int compare(Card o1, Card o2) {
            return Integer.compare(o1.getActiveManaCost(), o2.getActiveManaCost());
        }
    }

    private <T> T randomElem(List<T> list) {
        assert !list.isEmpty();
        return list.get(random.nextInt(list.size()));
    }

    public void close() {}
}
