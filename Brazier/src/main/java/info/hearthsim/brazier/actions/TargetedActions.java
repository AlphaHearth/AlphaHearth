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
    public static final TargetedAction<DamageSource, Character> RESTORES_TO_FULL_HEALTH = (actor, target) -> {
        HpProperty hp = ActionUtils.tryGetHp(target);
        if (hp == null)
            return;

        int damage = hp.getCurrentHp() - hp.getMaxHp();
        if (damage >= 0)
            return;

        ActionUtils.damageCharacter(actor, damage, target);
    };

    /**
     * {@link TargetedAction} which triggers the target's death rattle effect.
     * <p>
     * See spell <em>Feign Death</em>.
     */
    public static final TargetedAction<Object, Minion> TRIGGER_DEATHRATTLE =
        (actor, target) -> target.triggerDeathRattles();

    /**
     * {@link TargetedAction} which kills the target.
     * <p>
     * See spell <em>Assassinate</em>.
     */
    public static final TargetedAction<Object, Character> KILL_TARGET =
        (actor, character) -> character.kill();

    /**
     * {@link TargetedAction} which gives the target minion <b>Taunt</b>.
     * <p>
     * See spell <em>Rusty Horn</em>.
     */
    public static final TargetedAction<Object, Minion> TAUNT =
        (actor, minion) -> minion.getBody().setTaunt(true);

    /**
     * {@link TargetedAction} which gives the target minion <b>Divine Shield</b>.
     * <p>
     * See minion <em>Argent Protector</em>.
     */
    public static final TargetedAction<Object, Minion> GIVE_DIVINE_SHIELD =
        (actor, minion) -> minion.getProperties().getBody().setDivineShield(true);

    /**
     * {@link TargetedAction} which gives the target minion <b>Charge</b>.
     * <p>
     * See spell <em>Charge</em>.
     */
    public static final TargetedAction<Object, Minion> CHARGE =
        (actor, minion) -> minion.setCharge(true);

    /**
     * {@link TargetedAction} which gives the target minion <b>Stealth</b>.
     * <p>
     * See minion <em>Master of Disguise</em>.
     */
    public static final TargetedAction<Object, Minion> STEALTH =
        (actor, minion) -> minion.getBody().setStealth(true);

    /**
     * {@link TargetedAction} which gives the target minion <b>Stealth</b> until the next turn.
     * <p>
     * See spell <em>Conceal</em>.
     */
    public static final TargetedAction<Object, Minion> STEALTH_FOR_A_TURN = (actor, minion) -> {
        UndoObjectAction<AuraAwareBoolProperty> undoRef = minion.getBody().getStealthProperty().setValueTo(true);
        minion.getGame().getEvents().turnStartsListeners().register((actionPlayer) -> {
            if (actionPlayer.getPlayerId() == minion.getOwner().getPlayerId())
                undoRef.undo(actionPlayer.getGame().getMinion(minion.getEntityId()).getBody().getStealthProperty());
        });
    };

    /**
     * {@link TargetedAction} which <b>Silence</b>s the target.
     * <p>
     * See spell <em>Silence</em>.
     */
    public static final TargetedAction<Object, Silencable> SILENCE =
        (actor, target) -> target.silence();

    /**
     * {@link TargetedAction} which <b>Freeze</b>s the target.
     * <p>
     * See spell <em>Frost Nova</em>.
     */
    public static final TargetedAction<Object, Character> FREEZE_TARGET =
        (actor, character) -> character.getAttackTool().freeze();

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
    public static final TargetedAction<PlayerProperty, Minion> TAKE_CONTROL =
        (actor, minion) -> actor.getOwner().getBoard().takeOwnership(minion);

    /**
     * {@link TargetedAction} which adds a copy of the target card to your hand.
     * <p>
     * See minion <em>Chromaggus</em>.
     */
    public static final TargetedAction<PlayerProperty, Card> COPY_TARGET_CARD =
        (self, card) -> {
            Hand hand = self.getOwner().getHand();
            hand.addCard(card.getCardDescr());
        };

    /**
     * {@link TargetedAction} which transforms the actor minion into the target minion.
     * <p>
     * See minion <em>Faceless Manipulator</em>.
     */
    public static TargetedAction<Minion, Minion> COPY_OTHER_MINION = Minion::copyOther;

    /**
     * {@link TargetedAction} which shuffles the target minion to its owner's deck.
     * <p>
     * See minion <em>Malorne</em>.
     */
    public static final TargetedAction<Object, Minion> SHUFFLE_MINION =
        (actor, minion) -> {
            Player owner = minion.getOwner();
            Deck deck = owner.getDeck();
            CardDescr baseCard = minion.getBaseDescr().getBaseCard();

            owner.getBoard().removeFromBoard(minion.getEntityId());
            deck.shuffle(minion.getGame().getRandomProvider(), baseCard);
        };

    /**
     * {@link TargetedAction} which deals damage equal to the actor hero's attack to the target.
     * <p>
     * See spell <em>Savagery</em>.
     */
    // TODO The damage of Savagery will be affected by spell power. Check if it is supported by this method.
    public static final TargetedAction<DamageSource, Character> SAVAGERY =
        (actor, character) -> {
            int damage = actor.getOwner().getHero().getAttackTool().getAttack();
            ActionUtils.damageCharacter(actor, damage, character);
        };

    /**
     * {@link TargetedAction} which deals damage equal to the actor hero's armor to the target.
     * <p>
     * See spell <em>Shield Slam</em>.
     */
    // TODO The damage of Shield Slam will be affected by spell power. Check if it is supported by this method.
    public static final TargetedAction<DamageSource, Character> SHIELD_SLAM =
        (actor, character) -> {
            int damage = actor.getOwner().getHero().getCurrentArmor();
            ActionUtils.damageCharacter(actor, damage, character);
        };

    /**
     * {@link TargetedAction} which controls the target minion until end of turn.
     * <p>
     * See spell <em>Shadow Madness</em>.
     */
    public static final TargetedAction<PlayerProperty, Minion> SHADOW_MADNESS =
        (actor, minion) -> takeControlForThisTurn(actor.getOwner(), minion);

    /**
     * {@link TargetedAction} which draws a card for the actor's owner and deals damage equal to its cost to
     * the target.
     * <p>
     * See spell <em>Holy Wrath</em>.
     */
    public static final TargetedAction<DamageSource, Character> HOLY_WRATH =
        (actor, character) -> {
            Player player = actor.getOwner();

            Card card = player.drawCardToHand();

            int damage = card != null ? card.getCardDescr().getManaCost() : 0;
            ActionUtils.damageCharacter(actor, damage, character);
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
    public static final TargetedAction<Object, Minion> ATTACK_HP_SWITCH =
        (actor, minion) -> {
            MinionBody body = minion.getBody();

            int attack = minion.getAttackTool().getAttack();
            int hp = body.getCurrentHp();

            minion.getBuffableAttack().setValueTo(hp);
            body.getHp().setMaxHp(attack);
            body.getHp().setCurrentHp(body.getMaxHp());
        };

    /**
     * {@link TargetedAction} which transforms the target minion into a random minion with the same cost.
     * <p>
     * See minion <em>Recombobulator</em>.
     */
    public static final TargetedAction<Object, Minion> RECOMBOBULATE = transformMinion((Minion minion) -> {
        Game game = minion.getGame();

        int manaCost = minion.getBaseDescr().getBaseCard().getManaCost();
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
    public static final TargetedAction<Minion, Minion> SWAP_HP_WITH_TARGET =
        (actor, minion) -> {
            HpProperty targetHpProperty = minion.getBody().getHp();
            HpProperty ourHpProperty = actor.getBody().getHp();

            int targetHp = targetHpProperty.getCurrentHp();
            int ourHp = ourHpProperty.getCurrentHp();

            targetHpProperty.setMaxAndCurrentHp(ourHp);
            ourHpProperty.setMaxAndCurrentHp(targetHp);
        };

    /**
     * {@link TargetedAction} which makes the target minion lose <b>Stealth</b>.
     * <p>
     * See spell <em>Flare</em>.
     */
    public static final TargetedAction<Object, Minion> DESTROY_STEALTH =
        (actor, minion) -> minion.getBody().setStealth(false);

    /**
     * Returns a {@link TargetedAction} which applies the given {@code action} with the given actor's opponent
     * being the actor.
     */
    public static <Actor extends PlayerProperty, Target> TargetedAction<Actor, Target> actWithOpponent(
        @NamedArg("action") TargetedAction<? super Player, ? super Target> action) {
        ExceptionHelper.checkNotNullArgument(action, "action");

        return (Actor actor, Target target) -> action.apply(actor.getOwner().getOpponent(), target);
    }

    /**
     * Returns a {@link TargetedAction} which applies the given {@code action} with the given target
     * being actor and target.
     */
    public static <Target> TargetedAction<Object, Target> actWithTarget(
        @NamedArg("action") TargetedAction<? super Target, ? super Target> action) {
        ExceptionHelper.checkNotNullArgument(action, "action");

        return (actor, target) -> action.apply(target, target);
    }

    // TODO Is `actWithTarget` and `withTarget` the same?

    /**
     * Returns a {@link TargetedAction} which applies the given {@code action} with the given target
     * being actor and target.
     */
    public static <Target> TargetedAction<Target, Target> withTarget(
        @NamedArg("action") TargetedAction<? super Target, ? super Target> action) {

        return (Target actor, Target target) -> action.apply(target, target);
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
        return (Actor actor, Minion target) -> {
            target.addAndActivateAbility((Minion self) -> {
                GameEvents events = self.getGame().getEvents();
                GameActionEvents<AttackRequest> listeners = events.simpleListeners(SimpleEventType.ATTACK_INITIATED);

                Predicate<AttackRequest> condition =
                    (attackRequest) -> attackRequest.getAttacker().getEntityId() == self.getEntityId();
                UndoObjectAction<GameActionEvents> undoRef =
                    listeners.register(Priorities.LOW_PRIORITY, condition,
                        (attackRequest) -> action.apply(actor));
                return (s) -> undoRef.undo(s.getGame().getEvents().simpleListeners(SimpleEventType.ATTACK_INITIATED));
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

        return (actor, target) -> {
            Minion minion = ActionUtils.tryGetMinion(actor);
            if (minion != null)
                action.apply(minion, target);
        };
    }

    /**
     * Returns a {@link TargetedAction} which executes the given action, ensuring that it won't be interrupted
     * by event notifications scheduled to any of the event listeners.
     *
     * @see GameEvents#doAtomic(Action)
     */
    public static <Actor extends GameProperty, Target> TargetedAction<Actor, Target> doAtomic(
        @NamedArg("action") TargetedAction<? super Actor, ? super Target> action) {
        ExceptionHelper.checkNotNullArgument(action, "action");

        return (Actor actor, Target target) ->
            actor.getGame().getEvents().doAtomic(() -> action.apply(actor, target));
    }

    /**
     * Returns a {@link TargetedAction} which uses entities returned by the given {@link TargetedEntitySelector} as
     * targets and executes the given {@code TargetedAction}.
     */
    public static <Actor extends GameProperty, Target, FinalTarget> TargetedAction<Actor, Target> forTargets(
        @NamedArg("selector") TargetedEntitySelector<? super Actor, ? super Target, ? extends FinalTarget> selector,
        @NamedArg("action") TargetedAction<? super Actor, ? super FinalTarget> action) {
        ExceptionHelper.checkNotNullArgument(selector, "targets");
        ExceptionHelper.checkNotNullArgument(action, "action");

        return (Actor actor, Target initialTarget) -> {
            selector.select(actor, initialTarget).forEach((FinalTarget target) -> {
                action.apply(actor, target);
            });
        };
    }

    /**
     * Returns a {@link TargetedAction} which uses entities returned by the given {@link TargetedEntitySelector} as
     * actors and executes the given {@code TargetedAction}.
     */
    public static <Actor extends GameProperty, Target, FinalActor> TargetedAction<Actor, Target> forActors(
        @NamedArg("actors") TargetedEntitySelector<? super Actor, ? super Target, ? extends FinalActor> actors,
        @NamedArg("action") TargetedAction<? super FinalActor, ? super Target> action) {
        ExceptionHelper.checkNotNullArgument(actors, "actors");
        ExceptionHelper.checkNotNullArgument(action, "action");

        return (Actor initialActor, Target target) -> {
            actors.select(initialActor, target).forEach(
                (FinalActor actor) -> action.apply(actor, target)
            );
        };
    }

    /**
     * {@link TargetedAction} which deals damage the given target equal to the actor's attack.
     * <p>
     * See spell <em>Betrayal</em> and <em>Lightbomb</em>.
     */
    public static TargetedAction<Character, Character> DAMAGE_TARGET =
        (actor, target) -> {
            int attack = actor.getAttackTool().getAttack();
            ActionUtils.damageCharacter(actor, attack, target);
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
        return (DamageSource actor, Character target) -> {
            int damage = target.getGame().getRandomProvider().roll(minDamage, maxDamage);
            ActionUtils.damageCharacter(actor, damage, target);
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
    public static <Actor extends GameProperty, Target> TargetedAction<Actor, Target> doIf(
        @NamedArg("condition") TargetedActionCondition<? super Actor, ? super Target> condition,
        @NamedArg("if") TargetedAction<? super Actor, ? super Target> ifAction,
        @NamedArg("else") TargetedAction<? super Actor, ? super Target> elseAction) {
        ExceptionHelper.checkNotNullArgument(condition, "condition");
        ExceptionHelper.checkNotNullArgument(ifAction, "ifAction");
        ExceptionHelper.checkNotNullArgument(elseAction, "elseAction");

        return (Actor actor, Target target) -> {
            Game game = actor.getGame();
            if (condition.applies(game, actor, target))
                ifAction.apply(actor, target);
            else
                elseAction.apply(actor, target);
        };
    }

    /**
     * Returns a {@link TargetedAction} which adds the given {@link Ability} to the target minion.
     */
    public static TargetedAction<Object, Minion> addAbility(
        @NamedArg("ability") Ability<? super Minion> ability) {
        ExceptionHelper.checkNotNullArgument(ability, "ability");
        return (actor, minion) -> minion.addAndActivateAbility(ability);
    }

    /**
     * Returns a {@link TargetedAction} which adds an {@link Ability} to the target minion which
     * executes the given {@link EventAction} on start of turn.
     */
    public static TargetedAction<PlayerProperty, Minion> addOnActorsStartOfTurnAbility(
        @NamedArg("action") EventAction<? super Minion, ? super Player> action) {
        ExceptionHelper.checkNotNullArgument(action, "action");

        return addOnActorsPlayerEventAbility(action, SimpleEventType.TURN_STARTS);
    }

    /**
     * Returns a {@link TargetedAction} which adds an {@link Ability} to the target minion which
     * executes the given {@link EventAction} on end of turn.
     */
    public static TargetedAction<PlayerProperty, Minion> addOnActorsEndOfTurnAbility(
        @NamedArg("action") EventAction<? super Minion, ? super Player> action) {
        return addOnActorsPlayerEventAbility(action, SimpleEventType.TURN_ENDS);
    }

    /**
     * Returns a {@link TargetedAction} which adds an {@link Ability} to the target minion which
     * executes the given {@link EventAction} on the given type of event.
     */
    private static TargetedAction<PlayerProperty, Minion> addOnActorsPlayerEventAbility(
        EventAction<? super Minion, ? super Player> action,
        SimpleEventType eventType) {
        ExceptionHelper.checkNotNullArgument(action, "action");
        ExceptionHelper.checkNotNullArgument(eventType, "eventType");

        return (PlayerProperty actor, Minion target) -> {
            EventFilter<Minion, Player> filter
                = (owner, eventSource) -> eventSource.getOwner() == actor.getOwner();
            Ability<Minion> ability
                = Ability.onEventAbility(filter, action, eventType);
            target.addAndActivateAbility(ability);
        };
    }

    /**
     * Returns a {@link TargetedAction} which adds the given {@link PermanentBuff} to the
     * target.
     */
    public static <Actor, Target extends Entity> TargetedAction<Actor, Target> buffTarget(
        @NamedArg("buff") PermanentBuff<? super Target> buff) {
        ExceptionHelper.checkNotNullArgument(buff, "buff");
        return (actor, target) -> buff.buff(target, BuffArg.NORMAL_BUFF);
    }

    /**
     * Returns a {@link TargetedAction} which adds the given {@link Buff} to the
     * target.
     * <p>
     * See minion <em>Abusive Sergeant</em>.
     */
    public static <Actor, Target extends Entity> TargetedAction<Actor, Target> buffTargetThisTurn(
        @NamedArg("buff") Buff<? super Target> buff) {
        ExceptionHelper.checkNotNullArgument(buff, "buff");
        return (actor, target) -> ActionUtils.doTemporary(target, () -> buff.buff(target, BuffArg.NORMAL_BUFF));
    }

    /**
     * Returns a {@link TargetedAction} which adds the given {@link EventAction} as
     * a death rattle effect to the target minion.
     */
    public static TargetedAction<Object, Minion> addDeathRattle(
        @NamedArg("action") EventAction<? super Minion, ? super Minion> action) {
        ExceptionHelper.checkNotNullArgument(action, "action");

        return (actor, target) -> target.addDeathRattle(action);
    }

    /**
     * Returs a {@link TargetedAction} which returns the target minion to its owner's hand, and reduces its cost
     * with the given amount.
     * <p>
     * See spell <em>Shadowstep</em> and secret <em>Freezing Trap</em>.
     */
    public static TargetedAction<Object, Minion> returnMinion(@NamedArg("costReduction") int costReduction) {
        return (actor, target) -> {
            Player owner = target.getOwner();
            CardDescr baseCard = target.getBaseDescr().getBaseCard();

            owner.getBoard().removeFromBoard(target.getEntityId());

            Card card = new Card(owner, baseCard);
            if (costReduction != 0)
                card.decreaseManaCost(costReduction);
            owner.getHand().addCard(card);
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

        return (actor, target) -> {
            MinionDescr newMinion = newMinionGetter.apply(target);
            target.transformTo(newMinion);
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

        return (actor, target) -> {
            int amount = target.getGame().getRandomProvider().roll(minDamage, maxDamage);
            Damage damage = actor.createDamage(amount);

            int damageDealt = target.damage(damage);
            Player player = actor.getOwner();
            MinionDescr summonedMinion = minion.getMinion();
            for (int i = 0; i < damageDealt; i++)
                player.summonMinion(summonedMinion);
        };
    }

    /**
     * Returns a {@link TargetedAction} which multiplies the given target's health point by the given number of times.
     * <p>
     * See spell <em>Divine Spirit</em>.
     */
    public static TargetedAction<Object, Character> multiplyHp(@NamedArg("mul") int mul) {
        Function<HpProperty, UndoObjectAction<HpProperty>> buffAction =
            (hp) -> hp.buffHp((mul - 1) * hp.getCurrentHp());
        return (actor, target) -> ActionUtils.adjustHp(target, buffAction);
    }

    /**
     * Returns a {@link TargetedAction} which adds given number of copies of the target {@link Card} to the hand of
     * the actor's owner.
     * <p>
     * See minion <em>Lorewalker Cho</em>.
     */
    public static TargetedAction<PlayerProperty, Card> copyTargetToHand(@NamedArg("copyCount") int copyCount) {
        return (PlayerProperty actor, Card target) -> {
            CardDescr baseCard = target.getCardDescr();
            Hand hand = actor.getOwner().getHand();

            for (int i = 0; i < copyCount; i++)
                hand.addCard(baseCard);
        };
    }

    /**
     * Returns a {@link TargetedAction} which decreases the cost of the target {@link Card} with the given amount.
     * <p>
     * See minion <em>Emperor Thaurissan</em>.
     */
    public static TargetedAction<Object, Card> decreaseCostOfTarget(@NamedArg("amount") int amount) {
        return (actor, target) -> target.decreaseManaCost(amount);
    }

    /**
     * Returns a {@link TargetedAction} which randomly selects an action from the given array of {@code TargetedAction}s
     * and executes.
     * <p>
     * See minion <em>Enhance-o Mechano</em>.
     */
    public static <Actor extends GameProperty, Target> TargetedAction<Actor, Target> randomAction(
        @NamedArg("actions") TargetedAction<? super Actor, ? super Target>[] actions) {
        ExceptionHelper.checkNotNullElements(actions, "actions");
        ExceptionHelper.checkArgumentInRange(actions.length, 1, Integer.MAX_VALUE, "actions.length");

        TargetedAction<? super Actor, ? super Target>[] actionsCopy = actions.clone();

        return (Actor actor, Target target) -> {
            TargetedAction<? super Actor, ? super Target> selected =
                ActionUtils.pickRandom(actor.getGame(), actionsCopy);
            selected.apply(actor, target);
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
        return (actor, target) -> {
            AuraAwareIntProperty maxAttackCount = target.getProperties().getMaxAttackCountProperty();
            maxAttackCount.addExternalBuff((prev) -> Math.max(prev, attackCount));
        };
    }

    private static void takeControlForThisTurn(Player newOwner, Minion minion) {
        minion.addAndActivateAbility(ActionUtils.toSingleTurnAbility((Minion self) -> {
            Player originalOwner = self.getOwner();
            newOwner.getBoard().takeOwnership(self);
            self.refreshStartOfTurn();

            return (Minion m) -> {
                if (!m.isDead()) {
                    m.getGame().getPlayer(originalOwner.getPlayerId()).getBoard().takeOwnership(m);
                }
            };
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
        return (Actor actor, Character target) -> {
            int damage = target.getAttackTool().getAttack();
            TargetlessAction<Actor> damageAction = TargetlessActions.damageTarget(selector, damage);
            damageAction.apply(actor);
        };
    }

    /**
     * Returns a {@link TargetedAction} which re-summons the target minion on the right of it with the given amount
     * of hp.
     * <p>
     * See secret <em>Redemption</em>.
     */
    public static TargetedAction<Object, Minion> resummonMinionWithHp(@NamedArg("hp") int hp) {
        return (actor, target) -> {
            Minion newMinion = new Minion(target.getOwner(), target.getBaseDescr());

            Player owner = target.getOwner();

            owner.summonMinion(newMinion, owner.getBoard().indexOf(target) + 1);
            newMinion.getProperties().getBody().getHp().setCurrentHp(hp);
        };
    }

    /**
     * Returns a {@link TargetedAction} which destroys the target minion and then return it to life with full health.
     * <p>
     * See spell <em>Reincarnate</em>.
     */
    public static <Actor> TargetedAction<Actor, Minion> reincarnate() {
        return (Actor actor, Minion target) -> {
            Player owner = target.getOwner();

            target.kill();
            owner.getGame().endPhase();

            Minion newMinion = new Minion(owner, target.getBaseDescr());
            owner.summonMinion(newMinion);
        };
    }

    private TargetedActions() {
        throw new AssertionError();
    }
}
