package info.hearthsim.brazier.actions;

import info.hearthsim.brazier.*;
import info.hearthsim.brazier.Character;
import info.hearthsim.brazier.abilities.*;
import info.hearthsim.brazier.actions.undo.*;
import info.hearthsim.brazier.events.*;
import info.hearthsim.brazier.events.GameEvents;
import info.hearthsim.brazier.minions.Minion;
import info.hearthsim.brazier.abilities.Ability;
import info.hearthsim.brazier.cards.Card;
import info.hearthsim.brazier.cards.CardDescr;
import info.hearthsim.brazier.minions.MinionBody;
import info.hearthsim.brazier.minions.MinionDescr;
import info.hearthsim.brazier.minions.MinionProvider;
import info.hearthsim.brazier.parsing.NamedArg;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import org.jtrim.utils.ExceptionHelper;

/**
 * Predefined {@link TargetedAction}.
 */
public final class TargetedActions {

    /**
     * {@link TargetedAction} which restores the target to full health.
     * <p>
     * See spell <em>Ancestral Healing</em>.
     */
    public static final TargetedAction<DamageSource, Character> RESTORES_TO_FULL_HEALTH = (game, actor, target) -> {
        HpProperty hp = ActionUtils.tryGetHp(target);
        if (hp == null) {
            return UndoAction.DO_NOTHING;
        }

        int damage = hp.getCurrentHp() - hp.getMaxHp();
        if (damage >= 0) {
            return UndoAction.DO_NOTHING;
        }
        return ActionUtils.damageCharacter(actor, damage, target);
    };

    /**
     * {@link TargetedAction} which triggers the target's death rattle effect.
     * <p>
     * See spell <em>Feign Death</em>.
     */
    public static final TargetedAction<Object, Minion> TRIGGER_DEATHRATTLE = (game, actor, target) -> {
        return target.triggerDeathRattles();
    };

    /**
     * {@link TargetedAction} which kills the target.
     * <p>
     * See spell <em>Assassinate</em>.
     */
    public static final TargetedAction<Object, Character> KILL_TARGET = (game, actor, target) -> {
        return target.kill();
    };

    /**
     * {@link TargetedAction} which gives the target minion <b>Taunt</b>.
     * <p>
     * See spell <em>Rusty Horn</em>.
     */
    public static final TargetedAction<Object, Minion> TAUNT = (Game game, Object actor, Minion target) -> {
        return target.getBody().setTaunt(true);
    };

    /**
     * {@link TargetedAction} which gives the target minion <b>Divine Shield</b>.
     * <p>
     * See minion <em>Argent Protector</em>.
     */
    public static final TargetedAction<Object, Minion> GIVE_DIVINE_SHIELD = (game, actor, target) -> {
        return target.getProperties().getBody().setDivineShield(true);
    };

    /**
     * {@link TargetedAction} which gives the target minion <b>Charge</b>.
     * <p>
     * See spell <em>Charge</em>.
     */
    public static final TargetedAction<Object, Minion> CHARGE = (game, actor, target) -> {
        return target.setCharge(true);
    };

    /**
     * {@link TargetedAction} which gives the target minion <b>Stealth</b>.
     * <p>
     * See minion <em>Master of Disguise</em>.
     */
    public static final TargetedAction<Object, Minion> STEALTH = (game, actor, target) -> {
        return target.getBody().setStealth(true);
    };

    /**
     * {@link TargetedAction} which gives the target minion <b>Stealth</b> until the next turn.
     * <p>
     * See spell <em>Conceal</em>.
     */
    public static final TargetedAction<Object, Minion> STEALTH_FOR_A_TURN = (game, actor, target) -> {
        return target.addAndActivateAbility(ActionUtils.toUntilTurnStartsAbility(game, target, (Minion self) -> {
            return self.getBody().getStealthProperty().setValueTo(true);
        }));
    };

    /**
     * {@link TargetedAction} which <b>Silence</b>s the target.
     * <p>
     * See spell <em>Silence</em>.
     */
    public static final TargetedAction<Object, Silencable> SILENCE = (game, actor, target) -> {
        return target.silence();
    };

    /**
     * {@link TargetedAction} which <b>Freeze</b>s the target.
     * <p>
     * See spell <em>Frost Nova</em>.
     */
    public static final TargetedAction<Object, Character> FREEZE_TARGET = (game, actor, target) -> {
        return target.getAttackTool().freeze();
    };

    /**
     * {@link TargetedAction} which returns the target minion to its owner's hand.
     * <p>
     * See spell <em>Sap</em>.
     */
    public static final TargetedAction<Object, Minion> RETURN_MINION = returnMinion(0);

    /**
     * {@link TargetedAction} which controls the target minion.
     * <p>
     * See spell <em>Mind Control</em>.
     */
    public static final TargetedAction<PlayerProperty, Minion> TAKE_CONTROL = (game, actor, target) -> {
        return actor.getOwner().getBoard().takeOwnership(target);
    };

    /**
     * {@link TargetedAction} which adds a copy of the target card to your hand.
     * <p>
     * See minion <em>Chromaggus</em>.
     */
    public static final TargetedAction<PlayerProperty, Card> COPY_TARGET_CARD = (game, self, target) -> {
        Hand hand = self.getOwner().getHand();
        return hand.addCard(target.getCardDescr());
    };

    /**
     * {@link TargetedAction} which transforms the actor minion into the target minion.
     * <p>
     * See minion <em>Faceless Manipulator</em>.
     */
    public static TargetedAction<Minion, Minion> COPY_OTHER_MINION = (game, actor, target) -> {
        return actor.copyOther(target);
    };

    /**
     * {@link TargetedAction} which shuffles the target minion to its owner's deck.
     * <p>
     * See minion <em>Malorne</em>.
     */
    public static final TargetedAction<Object, Minion> SHUFFLE_MINION = (game, actor, minion) -> {
        Player owner = minion.getOwner();
        Deck deck = owner.getDeck();
        CardDescr baseCard = minion.getBaseDescr().getBaseCard();

        UndoAction removeUndo = owner.getBoard().removeFromBoard(minion.getTargetId());
        UndoAction shuffleUndo = deck.shuffle(game.getRandomProvider(), baseCard);
        return () -> {
            shuffleUndo.undo();
            removeUndo.undo();
        };
    };

    /**
     * {@link TargetedAction} which deals damage equal to the actor hero's attack to the target.
     * <p>
     * See spell <em>Savagery</em>.
     */
    // TODO The damage of Savagery will be affected by spell power. Check if it is supported by this method.
    public static final TargetedAction<DamageSource, Character> SAVAGERY = (game, actor, target) -> {
        int damage = actor.getOwner().getHero().getAttackTool().getAttack();
        return ActionUtils.damageCharacter(actor, damage, target);
    };

    /**
     * {@link TargetedAction} which deals damage equal to the actor hero's armor to the target.
     * <p>
     * See spell <em>Shield Slam</em>.
     */
    // TODO The damage of Shield Slam will be affected by spell power. Check if it is supported by this method.
    public static final TargetedAction<DamageSource, Character> SHIELD_SLAM = (game, actor, target) -> {
        int damage = actor.getOwner().getHero().getCurrentArmor();
        return ActionUtils.damageCharacter(actor, damage, target);
    };

    /**
     * {@link TargetedAction} which controls the target minion until end of turn.
     * <p>
     * See spell <em>Shadow Madness</em>.
     */
    public static final TargetedAction<PlayerProperty, Minion> SHADOW_MADNESS = (game, actor, target) -> {
        return takeControlForThisTurn(actor.getOwner(), target);
    };

    /**
     * {@link TargetedAction} which draws a card for the actor's owner and deals damage equal to its cost to
     * the target.
     * <p>
     * See spell <em>Holy Wrath</em>.
     */
    public static final TargetedAction<DamageSource, Character> HOLY_WRATH = (game, actor, target) -> {
        Player player = actor.getOwner();

        UndoableResult<Card> cardRef = player.drawCardToHand();
        Card card = cardRef.getResult();

        int damage = card != null ? card.getCardDescr().getManaCost() : 0;
        UndoAction damageUndo = ActionUtils.damageCharacter(actor, damage, target);

        return () -> {
            damageUndo.undo();
            cardRef.undo();
        };
    };

    /**
     * {@link TargetedAction} which gives the target minion <b>Windfury</b>.
     * <p>
     * See spell <em>Windfury</em>.
     */
    public static final TargetedAction<Object, Minion> WIND_FURY = windFury(2);

    /**
     * {@link TargetedAction} which swaps the attack and health of the target minion.
     * <p>
     * See minion <em>Crazed Alchemist</em>.
     */
    public static final TargetedAction<Object, Minion> ATTACK_HP_SWITCH = (Game game, Object actor, Minion target) -> {
        MinionBody body = target.getBody();

        int attack = target.getAttackTool().getAttack();
        int hp = body.getCurrentHp();

        UndoAction attackUndo = target.getBuffableAttack().setValueTo(hp);
        UndoAction hpUndo = body.getHp().setMaxHp(attack);
        UndoAction currentHpUndo = body.getHp().setCurrentHp(body.getMaxHp());
        return () -> {
            currentHpUndo.undo();
            hpUndo.undo();
            attackUndo.undo();
        };
    };

    /**
     * {@link TargetedAction} which transforms the target minion into a random minion with the same cost.
     * <p>
     * See minion <em>Recombobulator</em>.
     */
    public static final TargetedAction<Object, Minion> RECOMBOBULATE = transformMinion((Minion target) -> {
        Game game = target.getGame();

        int manaCost = target.getBaseDescr().getBaseCard().getManaCost();
        Keyword manaCostKeyword = Keywords.manaCost(manaCost);
        List<CardDescr> possibleMinions = game.getDb().getCardDb().getByKeywords(Keywords.MINION, manaCostKeyword);
        CardDescr selected = ActionUtils.pickRandom(game, possibleMinions);
        if (selected == null) {
            return null;
        }

        MinionDescr result = selected.getMinion();
        if (result == null) {
            throw new IllegalStateException("Minion keyword was applied to a non-minion card: " + selected.getId());
        }
        return result;
    });

    /**
     * {@link TargetedAction} which swaps the actor minion's health with the target minion.
     * <p>
     * See minion <em>Vol'jin</em>.
     */
    public static final TargetedAction<Minion, Minion> SWAP_HP_WITH_TARGET = (Game game, Minion actor, Minion target) -> {
        HpProperty targetHpProperty = target.getBody().getHp();
        HpProperty ourHpProperty = actor.getBody().getHp();

        int targetHp = targetHpProperty.getCurrentHp();
        int ourHp = ourHpProperty.getCurrentHp();

        UndoAction targetHpUndo = targetHpProperty.setMaxAndCurrentHp(ourHp);
        UndoAction ourHpUndo = ourHpProperty.setMaxAndCurrentHp(targetHp);
        return () -> {
            ourHpUndo.undo();
            targetHpUndo.undo();
        };
    };

    /**
     * {@link TargetedAction} which makes the target minion lose <b>Stealth</b>.
     * <p>
     * See spell <em>Flare</em>.
     */
    public static final TargetedAction<Object, Minion> DESTROY_STEALTH = (game, actor, target) -> {
        return target.getBody().setStealth(false);
    };

    /**
     * Returns a {@link TargetedAction} which applies the given {@code action} with the given actor's opponent
     * being the actor.
     */
    public static <Actor extends PlayerProperty, Target> TargetedAction<Actor, Target> actWithOpponent(
            @NamedArg("action") TargetedAction<? super Player, ? super Target> action) {
        ExceptionHelper.checkNotNullArgument(action, "action");
        return (Game game, Actor actor, Target target) -> {
            return action.alterGame(game, actor.getOwner().getOpponent(), target);
        };
    }

    /**
     * Returns a {@link TargetedAction} which applies the given {@code action} with the given target
     * being actor and target.
     */
    public static <Target> TargetedAction<Object, Target> actWithTarget(
            @NamedArg("action") TargetedAction<? super Target, ? super Target> action) {
        ExceptionHelper.checkNotNullArgument(action, "action");
        return (Game game, Object actor, Target target) -> {
            return action.alterGame(game, target, target);
        };
    }

    // TODO Is `actWithTarget` and `withTarget` the same?

    /**
     * Returns a {@link TargetedAction} which applies the given {@code action} with the given target
     * being actor and target.
     */
    public static <Target> TargetedAction<Target, Target> withTarget(
        @NamedArg("action") TargetedAction<? super Target, ? super Target> action) {
        return (Game game, Target actor, Target target) -> {
            return action.alterGame(game, target, target);
        };
    }

    /**
     * Returns a {@link TargetedAction} which executes the given {@link TargetlessAction} when the target minion
     * attacks.
     * <p>
     * See spell <em>Blessing of Wisdom</em>.
     */
    public static <Actor extends PlayerProperty> TargetedAction<Actor, Minion> doOnAttack(
            @NamedArg("action") TargetlessAction<? super Actor> action) {
        ExceptionHelper.checkNotNullArgument(action, "action");
        return (Game game, Actor actor, Minion target) -> {
            return target.addAndActivateAbility((Minion self) -> {
                GameEvents events = game.getEvents();
                GameActionEvents<AttackRequest> listeners = events.simpleListeners(SimpleEventType.ATTACK_INITIATED);

                Predicate<AttackRequest> condition = (attackRequest) -> attackRequest.getAttacker() == self;
                return listeners.addAction(Priorities.LOW_PRIORITY, condition, (attackGame, attackRequest) -> {
                    return action.alterGame(attackGame, actor);
                });
            });
        };
    }

    /**
     * Returns a {@link TargetedAction} which executes the given action only when the given actor can be converted
     * to respective {@link Minion} object.
     *
     * @see ActionUtils#tryGetMinion(Object)
     */
    public static <Target> TargetedAction<Object, Target> withMinion(
            @NamedArg("action") TargetedAction<? super Minion, ? super Target> action) {
        ExceptionHelper.checkNotNullArgument(action, "action");

        return (Game game, Object actor, Target target) -> {
            Minion minion = ActionUtils.tryGetMinion(actor);
            return minion != null ? action.alterGame(game, minion, target) : UndoAction.DO_NOTHING;
        };
    }

    /**
     * Returns a {@link TargetedAction} which executes the given action, ensuring that it won't be interrupted
     * by event notifications scheduled to any of the event listeners.
     *
     * @see GameEvents#doAtomic(UndoableAction)
     */
    public static <Actor, Target> TargetedAction<Actor, Target> doAtomic(
            @NamedArg("action") TargetedAction<? super Actor, ? super Target> action) {
        ExceptionHelper.checkNotNullArgument(action, "action");

        return (Game game, Actor actor, Target target) -> {
            return game.getEvents().doAtomic(() -> action.alterGame(game, actor, target));
        };
    }

    /**
     * Returns a {@link TargetedAction} which uses entities returned by the given {@link TargetedEntitySelector} as
     * targets and executes the given {@code TargetedAction}.
     */
    public static <Actor, Target, FinalTarget> TargetedAction<Actor, Target> forTargets(
            @NamedArg("selector") TargetedEntitySelector<? super Actor, ? super Target, ? extends FinalTarget> selector,
            @NamedArg("action") TargetedAction<? super Actor, ? super FinalTarget> action) {
        ExceptionHelper.checkNotNullArgument(selector, "targets");
        ExceptionHelper.checkNotNullArgument(action, "action");

        return (Game game, Actor actor, Target initialTarget) -> {
            UndoAction.Builder builder = new UndoAction.Builder();
            selector.select(game, actor, initialTarget).forEach((FinalTarget target) -> {
                builder.addUndo(action.alterGame(game, actor, target));
            });
            return builder;
        };
    }

    /**
     * Returns a {@link TargetedAction} which uses entities returned by the given {@link TargetedEntitySelector} as
     * actors and executes the given {@code TargetedAction}.
     */
    public static <Actor, Target, FinalActor> TargetedAction<Actor, Target> forActors(
            @NamedArg("actors") TargetedEntitySelector<? super Actor, ? super Target, ? extends FinalActor> actors,
            @NamedArg("action") TargetedAction<? super FinalActor, ? super Target> action) {
        ExceptionHelper.checkNotNullArgument(actors, "actors");
        ExceptionHelper.checkNotNullArgument(action, "action");

        return (Game game, Actor initialActor, Target target) -> {
            UndoAction.Builder builder = new UndoAction.Builder();
            actors.select(game, initialActor, target).forEach((FinalActor actor) -> {
                builder.addUndo(action.alterGame(game, actor, target));
            });
            return builder;
        };
    }

    /**
     * {@link TargetedAction} which deals damage the given target equal to the actor's attack.
     * <p>
     * See spell <em>Betrayal</em> and <em>Lightbomb</em>.
     */
    public static TargetedAction<Character, Character> DAMAGE_TARGET = (game, actor, target) -> {
        int attack = actor.getAttackTool().getAttack();
        return ActionUtils.damageCharacter(actor, attack, target);
    };

    /**
     * Returns a {@link TargetedAction} which deals damage to the given target equal to the given amount.
     */
    public static TargetedAction<DamageSource, Character> damageTarget(@NamedArg("damage") int damage) {
        return damageTarget(damage, damage);
    }

    /**
     * Returns a {@link TargetedAction} which deals damage to the given target with the amount randomly selected
     * within the given minimum and maximum value.
     * <p>
     * See spell <em>Crackle</em>.
     */
    public static TargetedAction<DamageSource, Character> damageTarget(
            @NamedArg("minDamage") int minDamage,
            @NamedArg("maxDamage") int maxDamage) {
        return (Game game, DamageSource actor, Character target) -> {
            int damage = game.getRandomProvider().roll(minDamage, maxDamage);
            return ActionUtils.damageCharacter(actor, damage, target);
        };
    }

    /**
     * Returns a {@link TargetedAction} which executes the given {@code ifAction} if the given
     * {@link TargetedActionCondition} is satisfied.
     */
    public static <Actor, Target> TargetedAction<Actor, Target> doIf(
            @NamedArg("condition") TargetedActionCondition<? super Actor, ? super Target> condition,
            @NamedArg("if") TargetedAction<? super Actor, ? super Target> ifAction) {
        return doIf(condition, ifAction, TargetedAction.DO_NOTHING);
    }

    /**
     * Returns a {@link TargetedAction} which executes the given {@code ifAction} if the given
     * {@link TargetedActionCondition} is satisfied and executes the given {@code elseAction} otherwise.
     */
    public static <Actor, Target> TargetedAction<Actor, Target> doIf(
            @NamedArg("condition") TargetedActionCondition<? super Actor, ? super Target> condition,
            @NamedArg("if") TargetedAction<? super Actor, ? super Target> ifAction,
            @NamedArg("else") TargetedAction<? super Actor, ? super Target> elseAction) {
        ExceptionHelper.checkNotNullArgument(condition, "condition");
        ExceptionHelper.checkNotNullArgument(ifAction, "ifAction");
        ExceptionHelper.checkNotNullArgument(elseAction, "elseAction");

        return (Game game, Actor actor, Target target) -> {
            return condition.applies(game, actor, target)
                    ? ifAction.alterGame(game, actor, target)
                    : elseAction.alterGame(game, actor, target);
        };
    }

    /**
     * Returns a {@link TargetedAction} which adds the given {@link Ability} to the target minion.
     */
    public static TargetedAction<Object, Minion> addAbility(
            @NamedArg("ability") Ability<? super Minion> ability) {
        ExceptionHelper.checkNotNullArgument(ability, "ability");
        return (Game game, Object actor, Minion target) -> {
            return target.addAndActivateAbility(ability);
        };
    }

    /**
     * Returns a {@link TargetedAction} which adds an {@link Ability} to the target minion which
     * executes the given {@link GameEventAction} on start of turn.
     */
    public static TargetedAction<PlayerProperty, Minion> addOnActorsStartOfTurnAbility(
            @NamedArg("action") GameEventAction<? super Minion, ? super Player> action) {
        ExceptionHelper.checkNotNullArgument(action, "action");

        return addOnActorsPlayerEventAbility(action, SimpleEventType.TURN_STARTS);
    }

    /**
     * Returns a {@link TargetedAction} which adds an {@link Ability} to the target minion which
     * executes the given {@link GameEventAction} on end of turn.
     */
    public static TargetedAction<PlayerProperty, Minion> addOnActorsEndOfTurnAbility(
            @NamedArg("action") GameEventAction<? super Minion, ? super Player> action) {
        return addOnActorsPlayerEventAbility(action, SimpleEventType.TURN_ENDS);
    }

    /**
     * Returns a {@link TargetedAction} which adds an {@link Ability} to the target minion which
     * executes the given {@link GameEventAction} on the given type of event.
     */
    private static TargetedAction<PlayerProperty, Minion> addOnActorsPlayerEventAbility(
            GameEventAction<? super Minion, ? super Player> action,
            SimpleEventType eventType) {
        ExceptionHelper.checkNotNullArgument(action, "action");
        ExceptionHelper.checkNotNullArgument(eventType, "eventType");

        return (Game game, PlayerProperty actor, Minion target) -> {
            GameEventFilter<Minion, Player> filter
                    = (filterGame, owner, eventSource) -> eventSource.getOwner() == actor.getOwner();
            Ability<Minion> ability
                    = Ability.onEventAbility(filter, action, eventType);
            return target.addAndActivateAbility(ability);
        };
    }

    /**
     * Returns a {@link TargetedAction} which adds the given {@link PermanentBuff} to the
     * target.
     */
    public static <Target> TargetedAction<Object, Target> buffTarget(
            @NamedArg("buff") PermanentBuff<? super Target> buff) {
        ExceptionHelper.checkNotNullArgument(buff, "buff");
        return (Game game, Object actor, Target target) -> {
            return buff.buff(game, target, BuffArg.NORMAL_BUFF);
        };
    }

    /**
     * Returns a {@link TargetedAction} which adds the given {@link Buff} to the
     * target.
     * <p>
     * See spell <em>Ancestral Spirit</em>.
     */
    public static <Target> TargetedAction<Object, Target> buffTargetThisTurn(
            @NamedArg("buff") Buff<? super Target> buff) {
        ExceptionHelper.checkNotNullArgument(buff, "buff");
        return (Game game, Object actor, Target target) -> {
            return ActionUtils.doTemporary(game, () -> buff.buff(game, target, BuffArg.NORMAL_BUFF));
        };
    }

    /**
     * Returns a {@link TargetedAction} which adds the given {@link GameEventAction} as
     * a death rattle effect to the target minion.
     */
    public static TargetedAction<Object, Minion> addDeathRattle(
            @NamedArg("action") GameEventAction<? super Minion, ? super Minion> action) {
        ExceptionHelper.checkNotNullArgument(action, "action");

        return (Game game, Object actor, Minion target) -> {
            return target.addDeathRattle(action);
        };
    }

    /**
     * Returs a {@link TargetedAction} which returns the target minion to its owner's hand, and reduces its cost
     * with the given amount.
     * <p>
     * See spell <em>Shadowstep</em> and secret <em>Freezing Trap</em>.
     */
    public static TargetedAction<Object, Minion> returnMinion(@NamedArg("costReduction") int costReduction) {
        return (Game game, Object actor, Minion target) -> {
            Player owner = target.getOwner();
            CardDescr baseCard = target.getBaseDescr().getBaseCard();

            UndoAction.Builder builder = new UndoAction.Builder();
            builder.addUndo(owner.getBoard().removeFromBoard(target.getTargetId()));

            Card card = new Card(owner, baseCard);
            if (costReduction != 0) {
                builder.addUndo(card.decreaseManaCost(costReduction));
            }

            builder.addUndo(owner.getHand().addCard(card));

            return builder;
        };
    }

    /**
     * Returns a {@link TargetedAction} which transforms the target minion to another minion randomly selected from
     * the given {@link MinionProvider}s.
     */
    public static TargetedAction<Object, Minion> transformMinion(@NamedArg("minion") MinionProvider[] minion) {
        ExceptionHelper.checkNotNullElements(minion, "minion");
        List<MinionProvider> minionCopy = new ArrayList<>(Arrays.asList(minion));

        return transformMinion((originalMinion) -> {
            MinionProvider selected = ActionUtils.pickRandom(originalMinion.getGame(), minionCopy);
            return selected != null ? selected.getMinion() : null;
        });
    }

    /**
     * Returns a {@link TargetedAction} which transforms the target minion to the minion provided by the given
     * {@code minionGetter}, with the target minion as its parameter.
     */
    public static TargetedAction<Object, Minion> transformMinion(Function<? super Minion, MinionDescr> newMinionGetter) {
        ExceptionHelper.checkNotNullArgument(newMinionGetter, "newMinionGetter");

        return (Game game, Object actor, Minion target) -> {
            MinionDescr newMinion = newMinionGetter.apply(target);
            return target.transformTo(newMinion);
        };
    }

    /**
     * Returns a {@link TargetedAction} which deals damage to the target minion with the amount randomly selected
     * within the given minimum and maximum value, and summons a minion designated by the given {@link MinionProvider}
     * for each damage dealt.
     * <p>
     * See spell <em>Imp-losion</em>.
     */
    public static TargetedAction<DamageSource, Character> implosion(
            @NamedArg("minDamage") int minDamage,
            @NamedArg("maxDamage") int maxDamage,
            @NamedArg("minion") MinionProvider minion) {
        ExceptionHelper.checkArgumentInRange(minDamage, 0, maxDamage, "minDamage");
        ExceptionHelper.checkNotNullArgument(minion, "minion");

        return (game, actor, target) -> {
            UndoAction.Builder builder = new UndoAction.Builder();

            int damage = game.getRandomProvider().roll(minDamage, maxDamage);
            UndoableResult<Damage> damageRef = actor.createDamage(damage);
            builder.addUndo(damageRef.getUndoAction());

            UndoableIntResult damageUndo = target.damage(damageRef.getResult());
            builder.addUndo(damageUndo.getUndoAction());

            int damageDelt = damageUndo.getResult();
            Player player = actor.getOwner();
            MinionDescr summonedMinion = minion.getMinion();
            for (int i = 0; i < damageDelt; i++) {
                builder.addUndo(player.summonMinion(summonedMinion));
            }

            return builder;
        };
    }

    /**
     * Returns a {@link TargetedAction} which multiplies the given target's health point by the given number of times.
     * <p>
     * See spell <em>Divine Spirit</em>.
     */
    public static TargetedAction<Object, Character> multiplyHp(@NamedArg("mul") int mul) {
        Function<HpProperty, UndoAction> buffAction = (hp) -> hp.buffHp((mul - 1) * hp.getCurrentHp());
        return (Game game, Object actor, Character target) -> {
            return ActionUtils.adjustHp(target, buffAction);
        };
    }

    /**
     * Returns a {@link TargetedAction} which adds given number of copies of the target {@link Card} to the hand of
     * the actor's owner.
     * <p>
     * See minion <em>Lorewalker Cho</em>.
     */
    public static TargetedAction<PlayerProperty, Card> copyTargetToHand(@NamedArg("copyCount") int copyCount) {
        return (Game game, PlayerProperty actor, Card target) -> {
            CardDescr baseCard = target.getCardDescr();
            Hand hand = actor.getOwner().getHand();

            UndoAction.Builder builder = new UndoAction.Builder(copyCount);
            for (int i = 0; i < copyCount; i++) {
                builder.addUndo(hand.addCard(baseCard));
            }
            return builder;
        };
    }

    /**
     * Returns a {@link TargetedAction} which decreases the cost of the target {@link Card} with the given amount.
     * <p>
     * See minion <em>Emperor Thaurissan</em>.
     */
    public static TargetedAction<Object, Card> decreaseCostOfTarget(@NamedArg("amount") int amount) {
        return (Game game, Object actor, Card target) -> {
            return target.decreaseManaCost(amount);
        };
    }

    /**
     * Returns a {@link TargetedAction} which randomly selects an action from the given array of {@code TargetedAction}s
     * and executes.
     * <p>
     * See minion <em>Enhance-o Mechano</em>.
     */
    public static <Actor, Target> TargetedAction<Actor, Target> randomAction(
            @NamedArg("actions") TargetedAction<? super Actor, ? super Target>[] actions) {
        ExceptionHelper.checkNotNullElements(actions, "actions");
        ExceptionHelper.checkArgumentInRange(actions.length, 1, Integer.MAX_VALUE, "actions.length");

        TargetedAction<? super Actor, ? super Target>[] actionsCopy = actions.clone();

        return (Game game, Actor actor, Target target) -> {
            TargetedAction<? super Actor, ? super Target> selected = ActionUtils.pickRandom(game, actionsCopy);
            return selected.alterGame(game, actor, target);
        };
    }

    /**
     * Combines the given array of {@link TargetedAction}s into one.
     *
     * @see TargetedAction#merge(Collection)
     */
    public static <Actor, Target> TargetedAction<Actor, Target> combine(
            @NamedArg("actions") TargetedAction<Actor, Target>[] actions) {
        return TargetedAction.merge(Arrays.asList(actions));
    }

    /**
     * Returns a {@link TargetedAction} which buffs the target minion's attack count to the given amount.
     */
    public static TargetedAction<Object, Minion> windFury(@NamedArg("attackCount") int attackCount) {
        return (Game game, Object actor, Minion target) -> {
            AuraAwareIntProperty maxAttackCount = target.getProperties().getMaxAttackCountProperty();
            return maxAttackCount.addExternalBuff((prev) -> Math.max(prev, attackCount));
        };
    }

    private static UndoAction takeControlForThisTurn(Player newOwner, Minion minion) {
        Game game = newOwner.getGame();
        return minion.addAndActivateAbility(ActionUtils.toSingleTurnAbility(game, (Minion self) -> {
            Player originalOwner = self.getOwner();
            UndoAction takeOwnUndo = newOwner.getBoard().takeOwnership(self);
            UndoAction refreshUndo = self.refreshStartOfTurn();

            return UndoableUnregisterAction.makeIdempotent(new UndoableUnregisterAction() {
                @Override
                public UndoAction unregister() {
                    // We must not return this minion to its owner,
                    // if we are disabling this ability before destroying the
                    // minion.
                    if (self.isScheduledToDestroy()) {
                        return UndoAction.DO_NOTHING;
                    }
                    return originalOwner.getBoard().takeOwnership(self);
                }

                @Override
                public void undo() {
                    refreshUndo.undo();
                    takeOwnUndo.undo();
                }
            });
        }));
    }

    /**
     * Returns a {@link TargetedAction} which deals damage to each target selected by the given {@link EntitySelector}
     * equal to the actor's attack.
     * <p>
     * See spell <em>Shadow Flame</em>.
     */
    public static <Actor extends DamageSource> TargetedAction<Actor, Character> shadowFlameDamage(
            @NamedArg("selector") EntitySelector<Actor, ? extends Character> selector) {
        ExceptionHelper.checkNotNullArgument(selector, "selector");
        return (Game game, Actor actor, Character target) -> {
            int damage = target.getAttackTool().getAttack();
            TargetlessAction<Actor> damageAction = TargetlessActions.damageTarget(selector, damage);
            return damageAction.alterGame(game, actor);
        };
    }

    /**
     * Returns a {@link TargetedAction} which re-summons the target minion on the right of it with the given amount
     * of hp.
     * <p>
     * See secret <em>Redemption</em>.
     */
    public static TargetedAction<Object, Minion> resummonMinionWithHp(@NamedArg("hp") int hp) {
        return (Game game, Object actor, Minion target) -> {
            Minion newMinion = new Minion(target.getOwner(), target.getBaseDescr());

            Player owner = target.getOwner();

            UndoAction summonUndo = owner.summonMinion(newMinion, owner.getBoard().indexOf(target) + 1);
            UndoAction updateHpUndo = newMinion.getProperties().getBody().getHp().setCurrentHp(hp);
            return () -> {
                updateHpUndo.undo();
                summonUndo.undo();
            };
        };
    }

    /**
     * Returns a {@link TargetedAction} which destroys the target minion and then return it to life with full health.
     * <p>
     * See spell <em>Reincarnate</em>.
     */
    public static <Actor> TargetedAction<Actor, Minion> reincarnate() {
        return (Game game, Actor actor, Minion target) -> {
            Player owner = target.getOwner();

            UndoAction.Builder builder = new UndoAction.Builder();
            builder.addUndo(target.kill());
            builder.addUndo(game.endPhase());

            Minion newMinion = new Minion(owner, target.getBaseDescr());
            builder.addUndo(owner.summonMinion(newMinion));

            return builder;
        };
    }

    private TargetedActions() {
        throw new AssertionError();
    }
}
