package info.hearthsim.brazier.events;

import info.hearthsim.brazier.DamageSource;
import info.hearthsim.brazier.PlayerProperty;
import info.hearthsim.brazier.TargetRef;
import info.hearthsim.brazier.actions.undo.UndoableResult;
import info.hearthsim.brazier.minions.Minion;
import info.hearthsim.brazier.minions.MinionProvider;
import info.hearthsim.brazier.parsing.NamedArg;
import info.hearthsim.brazier.Damage;
import info.hearthsim.brazier.Player;
import info.hearthsim.brazier.TargetableCharacter;
import info.hearthsim.brazier.World;
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

import static info.hearthsim.brazier.events.WorldEventFilters.validMisdirectTarget;

public final class WorldEventActions {
    public static final WorldEventAction<PlayerProperty, CardPlayEvent> PREVENT_CARD_PLAY = (world, self, eventSource) -> {
        return eventSource.vetoPlay();
    };

    public static final WorldEventAction<PlayerProperty, DamageRequest> PREVENT_PREPARED_DAMAGE = (world, self, eventSource) -> {
        return eventSource.vetoDamage();
    };

    public static WorldEventAction<DamageSource, DamageEvent> LIFE_STEAL_FOR_HERO = (world, self, event) -> {
        int damageDealt = event.getDamageDealt();
        if (damageDealt <= 0) {
            return UndoAction.DO_NOTHING;
        }

        return ActionUtils.damageCharacter(self, -damageDealt, self.getOwner().getHero());
    };

    public static final WorldEventAction<PlayerProperty, AttackRequest> MISS_TARGET_SOMETIMES
            = missTargetSometimes(1, 2);

     public static final WorldEventAction<PlayerProperty, AttackRequest> MISSDIRECT = (world, self, eventSource) -> {
         Predicate<TargetableCharacter> filter = WorldEventFilters.validMisdirectTarget(eventSource);
         List<TargetableCharacter> targets = new ArrayList<>();
         ActionUtils.collectAliveTargets(world.getPlayer1(), targets, filter);
         ActionUtils.collectAliveTargets(world.getPlayer2(), targets, filter);

         TargetableCharacter selected = ActionUtils.pickRandom(world, targets);
         if (selected == null) {
             return UndoAction.DO_NOTHING;
         }

         return eventSource.replaceTarget(selected);
     };

    public static WorldEventAction<PlayerProperty, AttackRequest> missTargetSometimes(
            @NamedArg("missCount") int missCount,
            @NamedArg("attackCount") int attackCount) {

        return (World world, PlayerProperty self, AttackRequest eventSource) -> {
            TargetableCharacter defender = eventSource.getTarget();
            if (defender == null) {
                return UndoAction.DO_NOTHING;
            }

            int roll = world.getRandomProvider().roll(attackCount);
            if (roll >=  missCount) {
                return UndoAction.DO_NOTHING;
            }

            List<TargetableCharacter> targets = new ArrayList<>(Player.MAX_BOARD_SIZE);
            ActionUtils.collectAliveTargets(defender.getOwner(), targets, (target) -> target != defender);
            TargetableCharacter newTarget = ActionUtils.pickRandom(world, targets);
            if (newTarget == null) {
                return UndoAction.DO_NOTHING;
            }

            return eventSource.replaceTarget(newTarget);
        };
    }

    public static WorldEventAction<PlayerProperty, AttackRequest> summonNewTargetForAttack(
            @NamedArg("minion") MinionProvider minion) {
        return (world, self, eventSource) -> {
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

    public static <Actor extends PlayerProperty> WorldEventAction<Actor, TargetRef> forDamageTarget(
            @NamedArg("action") TargetedAction<? super Actor, ? super TargetableCharacter> action) {
        ExceptionHelper.checkNotNullArgument(action, "action");
        return (World world, Actor self, TargetRef eventSource) -> {
            return action.alterWorld(world, self, eventSource.getTarget());
        };
    }

    public static <Actor extends PlayerProperty> WorldEventAction<Actor, AttackRequest> forAttacker(
            @NamedArg("action") TargetedAction<? super Actor, ? super TargetableCharacter> action) {
        ExceptionHelper.checkNotNullArgument(action, "action");
        return (World world, Actor self, AttackRequest eventSource) -> {
            return action.alterWorld(world, self, eventSource.getAttacker());
        };
    }

    public static <Actor extends PlayerProperty, Target> WorldEventAction<Actor, Target> forEventArgTarget(
            @NamedArg("action") TargetedAction<? super Actor, ? super Target> action) {
        return action::alterWorld;
    }

    public static <Actor extends PlayerProperty> WorldEventAction<Actor, CardRef> forEventArgCardTarget(
            @NamedArg("action") TargetedAction<? super Actor, ? super Card> action) {
        ExceptionHelper.checkNotNullArgument(action, "action");
        return (World world, Actor self, CardRef eventSource) -> {
            return action.alterWorld(world, self, eventSource.getCard());
        };
    }

    public static <Actor extends PlayerProperty> WorldEventAction<Actor, Object> forEventArgMinionTarget(
            @NamedArg("action") TargetedAction<? super Actor, ? super Minion> action) {
        ExceptionHelper.checkNotNullArgument(action, "action");
        return (World world, Actor self, Object eventSource) -> {
            Minion minion = ActionUtils.tryGetMinion(eventSource);
            if (minion != null) {
                return action.alterWorld(world, self, minion);
            }
            else {
                return UndoAction.DO_NOTHING;
            }
        };
    }

    public static <Actor extends PlayerProperty> WorldEventAction<Actor, Object> withSelf(
            @NamedArg("action") TargetlessAction<? super Actor> action) {
        ExceptionHelper.checkNotNullArgument(action, "action");
        return (World world, Actor self, Object eventSource) -> {
            return action.alterWorld(world, self);
        };
    }

    public static <Actor extends PlayerProperty> WorldEventAction<Actor, Object> withEventArgMinion(
            @NamedArg("action") TargetlessAction<? super Minion> action) {
        ExceptionHelper.checkNotNullArgument(action, "action");
        return (World world, Actor self, Object eventSource) -> {
            Minion minion = ActionUtils.tryGetMinion(eventSource);
            if (minion != null) {
                return action.alterWorld(world, minion);
            }
            else {
                return UndoAction.DO_NOTHING;
            }
        };
    }

    public static WorldEventAction<PlayerProperty, CardPlayEvent> summonNewTargetForCardPlay(
            @NamedArg("minion") MinionProvider minion) {
        ExceptionHelper.checkNotNullArgument(minion, "minion");
        return (world, self, eventSource) -> {
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

    public static <Actor extends DamageSource> WorldEventAction<Actor, DamageEvent> reflectDamage(
            @NamedArg("selector") EntitySelector<? super Actor, ? extends TargetableCharacter> selector) {
        ExceptionHelper.checkNotNullArgument(selector, "selector");
        return (world, self, eventSource) -> {
            int damage = eventSource.getDamageDealt();
            UndoableResult<Damage> damageRef = self.createDamage(damage);
            UndoAction damageUndo = selector.forEach(world, self, (target) -> target.damage(damageRef.getResult()));
            return () -> {
                damageUndo.undo();
                damageRef.undo();
            };
        };
    }
}
