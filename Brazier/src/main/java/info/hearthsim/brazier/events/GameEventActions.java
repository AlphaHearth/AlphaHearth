package info.hearthsim.brazier.events;

import info.hearthsim.brazier.*;
import info.hearthsim.brazier.Character;
import info.hearthsim.brazier.actions.undo.UndoableResult;
import info.hearthsim.brazier.minions.Minion;
import info.hearthsim.brazier.minions.MinionProvider;
import info.hearthsim.brazier.parsing.NamedArg;
import info.hearthsim.brazier.actions.ActionUtils;
import info.hearthsim.brazier.actions.AttackRequest;
import info.hearthsim.brazier.actions.CardRef;
import info.hearthsim.brazier.actions.EntitySelector;
import info.hearthsim.brazier.actions.TargetedAction;
import info.hearthsim.brazier.actions.TargetlessAction;
import info.hearthsim.brazier.actions.undo.UndoAction;
import info.hearthsim.brazier.cards.Card;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.jtrim.utils.ExceptionHelper;

import static info.hearthsim.brazier.events.GameEventFilters.validMisdirectTarget;

public final class GameEventActions {
    public static final GameEventAction<PlayerProperty, CardPlayEvent> PREVENT_CARD_PLAY =
        (game, self, eventSource) -> eventSource.vetoPlay();

    public static final GameEventAction<PlayerProperty, DamageRequest> PREVENT_PREPARED_DAMAGE =
        (game, self, eventSource) -> eventSource.vetoDamage();

    public static GameEventAction<DamageSource, DamageEvent> LIFE_STEAL_FOR_HERO =
        (game, self, event) -> {
            int damageDealt = event.getDamageDealt();
            if (damageDealt <= 0) {
                return UndoAction.DO_NOTHING;
            }

            return ActionUtils.damageCharacter(self, -damageDealt, self.getOwner().getHero());
        };

    public static final GameEventAction<PlayerProperty, AttackRequest> MISS_TARGET_SOMETIMES
        = missTargetSometimes(1, 2);

    public static final GameEventAction<PlayerProperty, AttackRequest> MISSDIRECT =
        (game, self, eventSource) -> {
            Predicate<Character> filter = GameEventFilters.validMisdirectTarget(eventSource);
            List<Character> targets = new ArrayList<>();
            ActionUtils.collectAliveTargets(game.getPlayer1(), targets, filter);
            ActionUtils.collectAliveTargets(game.getPlayer2(), targets, filter);

            Character selected = ActionUtils.pickRandom(game, targets);
            if (selected == null) {
                return UndoAction.DO_NOTHING;
            }

            return eventSource.replaceTarget(selected);
        };

    public static GameEventAction<PlayerProperty, AttackRequest> missTargetSometimes(
        @NamedArg("missCount") int missCount,
        @NamedArg("attackCount") int attackCount) {

        return (Game game, PlayerProperty self, AttackRequest eventSource) -> {
            Character defender = eventSource.getTarget();
            if (defender == null) {
                return UndoAction.DO_NOTHING;
            }

            int roll = game.getRandomProvider().roll(attackCount);
            if (roll >= missCount) {
                return UndoAction.DO_NOTHING;
            }

            List<Character> targets = new ArrayList<>(Player.MAX_BOARD_SIZE);
            ActionUtils.collectAliveTargets(defender.getOwner(), targets, (target) -> target != defender);
            Character newTarget = ActionUtils.pickRandom(game, targets);
            if (newTarget == null) {
                return UndoAction.DO_NOTHING;
            }

            return eventSource.replaceTarget(newTarget);
        };
    }

    public static GameEventAction<PlayerProperty, AttackRequest> summonNewTargetForAttack(
        @NamedArg("minion") MinionProvider minion) {
        return (game, self, eventSource) -> {
            Player targetPlayer = self.getOwner();
            if (targetPlayer.getBoard().isFull()) {
                return UndoAction.DO_NOTHING;
            }

            Minion summonedMinion = new Minion(targetPlayer, minion.getMinion());
            UndoAction summonUndo = targetPlayer.summonMinion(summonedMinion);
            UndoAction retargetUndo = eventSource.replaceTarget(summonedMinion);
            return () -> {
                retargetUndo.undo();
                summonUndo.undo();
            };
        };
    }

    public static <Actor extends PlayerProperty> GameEventAction<Actor, TargetRef> forDamageTarget(
        @NamedArg("action") TargetedAction<? super Actor, ? super info.hearthsim.brazier.Character> action) {
        ExceptionHelper.checkNotNullArgument(action, "action");
        return (Game game, Actor self, TargetRef eventSource) -> {
            return action.alterGame(game, self, eventSource.getTarget());
        };
    }

    public static <Actor extends PlayerProperty> GameEventAction<Actor, AttackRequest> forAttacker(
        @NamedArg("action") TargetedAction<? super Actor, ? super Character> action) {
        ExceptionHelper.checkNotNullArgument(action, "action");
        return (Game game, Actor self, AttackRequest eventSource) -> {
            return action.alterGame(game, self, eventSource.getAttacker());
        };
    }

    public static <Actor extends PlayerProperty, Target> GameEventAction<Actor, Target> forEventArgTarget(
        @NamedArg("action") TargetedAction<? super Actor, ? super Target> action) {
        return action::alterGame;
    }

    public static <Actor extends PlayerProperty> GameEventAction<Actor, CardRef> forEventArgCardTarget(
        @NamedArg("action") TargetedAction<? super Actor, ? super Card> action) {
        ExceptionHelper.checkNotNullArgument(action, "action");
        return (Game game, Actor self, CardRef eventSource) -> {
            return action.alterGame(game, self, eventSource.getCard());
        };
    }

    public static <Actor extends PlayerProperty> GameEventAction<Actor, Object> forEventArgMinionTarget(
        @NamedArg("action") TargetedAction<? super Actor, ? super Minion> action) {
        ExceptionHelper.checkNotNullArgument(action, "action");
        return (Game game, Actor self, Object eventSource) -> {
            Minion minion = ActionUtils.tryGetMinion(eventSource);
            if (minion != null) {
                return action.alterGame(game, self, minion);
            } else {
                return UndoAction.DO_NOTHING;
            }
        };
    }

    public static <Actor extends PlayerProperty> GameEventAction<Actor, Object> withSelf(
        @NamedArg("action") TargetlessAction<? super Actor> action) {
        ExceptionHelper.checkNotNullArgument(action, "action");
        return (Game game, Actor self, Object eventSource) -> {
            return action.alterGame(game, self);
        };
    }

    public static <Actor extends PlayerProperty> GameEventAction<Actor, Object> withEventArgMinion(
        @NamedArg("action") TargetlessAction<? super Minion> action) {
        ExceptionHelper.checkNotNullArgument(action, "action");
        return (Game game, Actor self, Object eventSource) -> {
            Minion minion = ActionUtils.tryGetMinion(eventSource);
            if (minion != null) {
                return action.alterGame(game, minion);
            } else {
                return UndoAction.DO_NOTHING;
            }
        };
    }

    public static GameEventAction<PlayerProperty, CardPlayEvent> summonNewTargetForCardPlay(
        @NamedArg("minion") MinionProvider minion) {
        ExceptionHelper.checkNotNullArgument(minion, "minion");
        return (game, self, eventSource) -> {
            Player targetPlayer = self.getOwner();
            if (targetPlayer.getBoard().isFull()) {
                return UndoAction.DO_NOTHING;
            }

            Minion summonedMinion = new Minion(targetPlayer, minion.getMinion());
            UndoAction summonUndo = targetPlayer.summonMinion(summonedMinion);
            UndoAction retargetUndo = eventSource.replaceTarget(summonedMinion);
            return () -> {
                retargetUndo.undo();
                summonUndo.undo();
            };
        };
    }

    public static <Actor extends DamageSource> GameEventAction<Actor, DamageEvent> reflectDamage(
        @NamedArg("selector") EntitySelector<? super Actor, ? extends Character> selector) {
        ExceptionHelper.checkNotNullArgument(selector, "selector");
        return (game, self, eventSource) -> {
            int damage = eventSource.getDamageDealt();
            UndoableResult<Damage> damageRef = self.createDamage(damage);
            UndoAction damageUndo = selector.forEach(game, self, (target) -> target.damage(damageRef.getResult()));
            return () -> {
                damageUndo.undo();
                damageRef.undo();
            };
        };
    }
}
