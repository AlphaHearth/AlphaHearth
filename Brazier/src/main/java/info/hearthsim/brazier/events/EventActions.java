package info.hearthsim.brazier.events;

import info.hearthsim.brazier.*;
import info.hearthsim.brazier.game.*;
import info.hearthsim.brazier.game.Character;
import info.hearthsim.brazier.game.minions.Minion;
import info.hearthsim.brazier.game.minions.MinionProvider;
import info.hearthsim.brazier.parsing.NamedArg;
import info.hearthsim.brazier.actions.ActionUtils;
import info.hearthsim.brazier.actions.AttackRequest;
import info.hearthsim.brazier.actions.CardRef;
import info.hearthsim.brazier.actions.EntitySelector;
import info.hearthsim.brazier.actions.TargetedAction;
import info.hearthsim.brazier.actions.TargetlessAction;
import info.hearthsim.brazier.game.cards.Card;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.jtrim.utils.ExceptionHelper;

import static info.hearthsim.brazier.events.EventFilters.validMisdirectTarget;

public final class EventActions {
    public static final EventAction<PlayerProperty, CardPlayEvent> PREVENT_CARD_PLAY =
        (self, eventSource) -> eventSource.vetoPlay();

    public static final EventAction<PlayerProperty, DamageRequest> PREVENT_PREPARED_DAMAGE =
        (self, eventSource) -> eventSource.vetoDamage();

    public static EventAction<DamageSource, DamageEvent> LIFE_STEAL_FOR_HERO =
        (self, event) -> {
            int damageDealt = event.getDamageDealt();
            if (damageDealt <= 0)
                return;

            ActionUtils.damageCharacter(self, -damageDealt, self.getOwner().getHero());
        };

    public static final EventAction<PlayerProperty, AttackRequest> MISS_TARGET_SOMETIMES
        = missTargetSometimes(1, 2);

    public static final EventAction<PlayerProperty, AttackRequest> MISSDIRECT =
        (self, eventSource) -> {
            Game game = self.getGame();
            Predicate<Character> filter = EventFilters.validMisdirectTarget(eventSource);
            List<Character> targets = new ArrayList<>();
            ActionUtils.collectAliveTargets(game.getPlayer1(), targets, filter);
            ActionUtils.collectAliveTargets(game.getPlayer2(), targets, filter);

            Character selected = ActionUtils.pickRandom(game, targets);
            if (selected == null)
                return;

            eventSource.replaceTarget(selected);
        };

    public static EventAction<PlayerProperty, AttackRequest> missTargetSometimes(
        @NamedArg("missCount") int missCount,
        @NamedArg("attackCount") int attackCount) {

        return (PlayerProperty self, AttackRequest eventSource) -> {
            Character defender = eventSource.getTarget();
            if (defender == null)
                return;

            int roll = self.getGame().getRandomProvider().roll(attackCount);
            if (roll >= missCount)
                return;

            List<Character> targets = new ArrayList<>(Player.MAX_BOARD_SIZE);
            ActionUtils.collectAliveTargets(defender.getOwner(), targets, (target) -> target != defender);
            Character newTarget = ActionUtils.pickRandom(self.getGame(), targets);
            if (newTarget == null)
                return;

            eventSource.replaceTarget(newTarget);
        };
    }

    public static EventAction<PlayerProperty, AttackRequest> summonNewTargetForAttack(
        @NamedArg("minion") MinionProvider minion) {
        return (self, eventSource) -> {
            Player targetPlayer = self.getOwner();
            if (targetPlayer.getBoard().isFull())
                return;

            Minion summonedMinion = new Minion(targetPlayer, minion.getMinion());
            targetPlayer.summonMinion(summonedMinion);
            eventSource.replaceTarget(summonedMinion);
        };
    }

    public static <Actor extends PlayerProperty> EventAction<Actor, TargetRef> forDamageTarget(
        @NamedArg("action") TargetedAction<? super Actor, ? super Character> action) {
        ExceptionHelper.checkNotNullArgument(action, "action");
        return (Actor self, TargetRef eventSource) -> action.apply(self, eventSource.getTarget());
    }

    public static <Actor extends PlayerProperty> EventAction<Actor, AttackRequest> forAttacker(
        @NamedArg("action") TargetedAction<? super Actor, ? super Character> action) {
        ExceptionHelper.checkNotNullArgument(action, "action");
        return (Actor self, AttackRequest eventSource) -> {
            try {
                action.apply(self, eventSource.getAttacker());
            } catch (Throwable thr) {
                System.out.println("Hello!");
            }
        };
    }

    public static <Actor extends PlayerProperty, Target> EventAction<Actor, Target> forEventArgTarget(
        @NamedArg("action") TargetedAction<? super Actor, ? super Target> action) {
        return action::apply;
    }

    public static <Actor extends PlayerProperty> EventAction<Actor, CardRef> forEventArgCardTarget(
        @NamedArg("action") TargetedAction<? super Actor, ? super Card> action) {
        ExceptionHelper.checkNotNullArgument(action, "action");
        return (Actor self, CardRef eventSource) -> action.apply(self, eventSource.getCard());
    }

    public static <Actor extends PlayerProperty> EventAction<Actor, Object> forEventArgMinionTarget(
        @NamedArg("action") TargetedAction<? super Actor, ? super Minion> action) {
        ExceptionHelper.checkNotNullArgument(action, "action");
        return (Actor self, Object eventSource) -> {
            Minion minion = ActionUtils.tryGetMinion(eventSource);
            if (minion != null)
                action.apply(self, minion);
        };
    }

    public static <Actor extends PlayerProperty> EventAction<Actor, Object> withSelf(
        @NamedArg("action") TargetlessAction<? super Actor> action) {
        ExceptionHelper.checkNotNullArgument(action, "action");
        return (Actor self, Object eventSource) -> action.apply(self);
    }

    public static <Actor extends PlayerProperty> EventAction<Actor, Object> withEventArgMinion(
        @NamedArg("action") TargetlessAction<? super Minion> action) {
        ExceptionHelper.checkNotNullArgument(action, "action");
        return (Actor self, Object eventSource) -> {
            Minion minion = ActionUtils.tryGetMinion(eventSource);
            if (minion != null)
                action.apply(minion);
        };
    }

    public static EventAction<PlayerProperty, CardPlayEvent> summonNewTargetForCardPlay(
        @NamedArg("minion") MinionProvider minion) {
        ExceptionHelper.checkNotNullArgument(minion, "minion");
        return (self, eventSource) -> {
            Player targetPlayer = self.getOwner();
            if (targetPlayer.getBoard().isFull())
                return;

            Minion summonedMinion = new Minion(targetPlayer, minion.getMinion());
            targetPlayer.summonMinion(summonedMinion);
            eventSource.replaceTarget(summonedMinion);
        };
    }

    public static <Actor extends DamageSource> EventAction<Actor, DamageEvent> reflectDamage(
        @NamedArg("selector") EntitySelector<? super Actor, ? extends Character> selector) {
        ExceptionHelper.checkNotNullArgument(selector, "selector");
        return (self, eventSource) -> {
            int damage = eventSource.getDamageDealt();
            Damage d = self.createDamage(damage);
            selector.forEach(self, (target) -> target.damage(d));
        };
    }
}
