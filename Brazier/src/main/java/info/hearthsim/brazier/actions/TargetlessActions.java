package info.hearthsim.brazier.actions;

import info.hearthsim.brazier.*;
import info.hearthsim.brazier.game.*;
import info.hearthsim.brazier.abilities.*;
import info.hearthsim.brazier.game.Character;
import info.hearthsim.brazier.util.UndoAction;
import info.hearthsim.brazier.events.*;
import info.hearthsim.brazier.game.minions.Minion;
import info.hearthsim.brazier.game.cards.Card;
import info.hearthsim.brazier.db.CardDescr;
import info.hearthsim.brazier.game.cards.CardName;
import info.hearthsim.brazier.game.cards.CardProvider;
import info.hearthsim.brazier.game.cards.CardType;
import info.hearthsim.brazier.events.EventFilter;
import info.hearthsim.brazier.game.minions.MinionBody;
import info.hearthsim.brazier.db.MinionDescr;
import info.hearthsim.brazier.game.minions.MinionName;
import info.hearthsim.brazier.game.minions.MinionProvider;
import info.hearthsim.brazier.parsing.NamedArg;
import info.hearthsim.brazier.game.weapons.Weapon;
import info.hearthsim.brazier.db.WeaponDescr;
import info.hearthsim.brazier.game.weapons.WeaponProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jtrim.utils.ExceptionHelper;

/**
 * Predefined {@link TargetlessAction}s.
 */
public final class TargetlessActions {

    /**
     * {@link TargetlessAction} which makes the actor's owner draw a card from the deck.
     */
    public static final TargetlessAction<PlayerProperty> DRAW_FOR_SELF =
        (PlayerProperty actor) ->
            actor.getOwner().drawCardToHand();

    /**
     * {@link TargetlessAction} which makes the opponent of the actor's owner draw a card from the deck.
     */
    public static final TargetlessAction<PlayerProperty> DRAW_FOR_OPPONENT =
        actWithOpponent(DRAW_FOR_SELF);

    /**
     * {@link TargetlessAction} which makes the actor's owner discard a random card from the hand.
     */
    public static final TargetlessAction<PlayerProperty> DISCARD_RANDOM_CARD =
        (actor) -> {
            Player player = actor.getOwner();
            Hand hand = player.getHand();
            int cardCount = hand.getCardCount();
            if (cardCount == 0)
                return;

            int cardIndex = actor.getGame().getRandomProvider().roll(cardCount);
            // TODO: Show discarded card to the opponent.
            hand.removeAtIndex(cardIndex);
        };

    /**
     * {@link TargetlessAction} which makes the actor's owner discard the card on the top of the deck.
     */
    public static final TargetlessAction<PlayerProperty> DISCARD_FROM_DECK =
        (actor) -> {
            Player player = actor.getOwner();
            Deck deck = player.getDeck();
            if (deck.getNumberOfCards() <= 0)
                return;

            deck.tryDrawOneCard();
            // TODO: Show discarded card to the opponent.
        };

    /**
     * {@code TargetlessAction} which re-summons a same minion on the right of the given minion.
     */
    public static final TargetlessAction<Minion> RESUMMON_RIGHT =
        (Minion minion) -> {
            BoardSide board = minion.getOwner().getBoard();
            int minionIndex = board.indexOf(minion.getEntityId());
            board.getOwner().summonMinion(minion.getBaseDescr(), minionIndex + 1);
        };

    /**
     * {@code TargetedAction} which resurrects all minions that died in the last turn.
     */
    public static final TargetlessAction<PlayerProperty> RESURRECT_DEAD_MINIONS =
        (actor) -> {
            Player player = actor.getOwner();
            List<Minion> deadMinions = player.getGraveyard().getMinionsDiedThisTurn();
            if (deadMinions.isEmpty())
                return;

            for (Minion minion : deadMinions)
                player.summonMinion(minion.getBaseDescr());
        };

    /**
     * {@link TargetlessAction} which destroys the opponent's weapon.
     */
    public static final TargetlessAction<PlayerProperty> DESTROY_OPPONENTS_WEAPON =
        (actor) -> actor.getOwner().destroyWeapon();

    /**
     * {@link TargetlessAction} which destroys all of the opponent's secrets.
     */
    public static final TargetlessAction<PlayerProperty> DESTROY_OPPONENT_SECRETS =
        (actor) -> {
            Player player = actor.getOwner();
            SecretContainer secrets = player.getOpponent().getSecrets();
            secrets.removeAllSecrets();
        };

    /**
     * {@code TargetedAction} which discards all cards in the player's hand.
     */
    public static final TargetlessAction<PlayerProperty> DISCARD_HAND =
        (actor) -> {
            Player player = actor.getOwner();
            // TODO: Display discarded cards to opponent
            player.getHand().discardAll();
        };

    /**
     * {@code TargetedAction} which reduces the player's weapon's durability by {@code 1}.
     */
    public static final TargetlessAction<PlayerProperty> REDUCE_WEAPON_DURABILITY =
        reduceWeaponDurability(1);

    /**
     * {@code TargetedAction} which swaps the given minion with a random minion card in the player's hand.
     */
    public static final TargetlessAction<Minion> SWAP_WITH_MINION_IN_HAND =
        (Minion minion) -> {
            Hand hand = minion.getOwner().getHand();
            int cardIndex = hand.chooseRandomCardIndex(Card::isMinionCard);
            if (cardIndex < 0)
                return;

            CardDescr newCard = minion.getBaseDescr().getBaseCard();
            Card replacedCard = hand.replaceAtIndex(cardIndex, newCard);
            Minion newMinion = replacedCard.getMinion();
            if (newMinion == null)
                throw new IllegalStateException("Selected card `" + replacedCard + "` is not a minion card.");

            minion.getOwner().getBoard().replace(minion, newMinion);
        };

    /**
     * {@link TargetlessAction} which summons a random minion from the deck of the actor's owner. If there
     * is no more minion left in the deck, nothing will be summoned.
     * <p>
     * See minion <em>Deathlord</em>.
     */
    public static final TargetlessAction<PlayerProperty> SUMMON_RANDOM_MINION_FROM_DECK =
        summonRandomMinionFromDeck(null);

    /**
     * {@link TargetlessAction} which summons a random minion from the hand of the actor's owner. If there
     * is no more minion in the hand, nothing will be summoned.
     * <p>
     * See spell <em>Ancestor's Call</em>.
     */
    public static TargetlessAction<PlayerProperty> SUMMON_RANDOM_MINION_FROM_HAND =
        (actor) -> {
            Player player = actor.getOwner();
            Hand hand = player.getHand();
            int cardIndex = hand.chooseRandomCardIndex(Card::isMinionCard);
            if (cardIndex < 0)
                return;

            Card removedCard = hand.removeAtIndex(cardIndex);
            Minion minion = removedCard.getMinion();
            if (minion == null)
                throw new IllegalStateException("Selected card `" + removedCard + "` is not a minion card.");


            player.summonMinion(minion);
        };

    /**
     * {@code TargetedAction} which creates a copy of the given minion on the right of that minion.
     */
    public static final TargetlessAction<Minion> COPY_SELF =
        (actor) -> {
            Player owner = actor.getOwner();
            if (owner.getBoard().isFull())
                return;

            Minion copy = new Minion(actor.getOwner(), actor.getBaseDescr());

            BoardSide board = owner.getBoard();
            copy.copyOther(actor);
            owner.summonMinion(copy, board.indexOf(actor) + 1);
        };

    /**
     * {@link TargetlessAction} which kills the actor (suicide).
     */
    public static final TargetlessAction<Character> SELF_DESTRUCT = Character::kill;

    /**
     * {@link TargetlessAction} which removes all of the overloaded mana crytals of the actor's owner.
     * <p>
     * See spell <em>Lava Shock</em>.
     */
    public static final TargetlessAction<PlayerProperty> REMOVE_OVERLOAD =
        (actor) -> {
            ManaResource mana = actor.getOwner().getManaResource();
            mana.setOverloadedMana(0);
            mana.setNextTurnOverload(0);
        };

    /**
     * {@link TargetlessAction} which makes the actor's owner draw a card for each damaged friendly character.
     * <p>
     * See spell <em>Battle Rage</em>.
     */
    public static final TargetlessAction<PlayerProperty> BATTLE_RAGE =
        (actor) -> {
            Player player = actor.getOwner();
            int cardsToDraw = player.getHero().isDamaged() ? 1 : 0;
            cardsToDraw += player.getBoard().countMinions(Minion::isDamaged);

            for (int i = 0; i < cardsToDraw; i++)
                player.drawCardToHand();
        };

    /**
     * {@link TargetlessAction} which adds a copy of a random card from the opponent's hand to the actor's owner's hand.
     * <p>
     * See spell <em>Mind Vision</em>.
     */
    public static final TargetlessAction<PlayerProperty> MIND_VISION =
        (actor) -> {
            Player player = actor.getOwner();
            Card card = player.getOpponent().getHand().getRandomCard();
            if (card == null)
                return;

            player.getHand().addCard(card.getCardDescr());
        };

    /**
     * {@link TargetlessAction} which gives the actor's owner {@code 50%} chance to draw a card.
     * <p>
     * See minion <em>Nat Pagle</em>.
     */
    public static final TargetlessAction<PlayerProperty> FISH_CARD_FOR_SELF =
        (actor) -> {
            Player player = actor.getOwner();
            if (actor.getGame().getRandomProvider().roll(2) < 1)
                player.drawCardToHand();
        };

    /**
     * {@link TargetlessAction} which summons a random friendly minion that died this game for the actor's owner.
     * <p>
     * See spell <em>Resurrect</em>.
     */
    public static final TargetlessAction<PlayerProperty> SUMMON_DEAD_MINION =
        (actor) -> {
            Player player = actor.getOwner();
            List<Minion> deadMinions = player.getGraveyard().getDeadMinions();
            if (deadMinions.isEmpty())
                return;

            RandomProvider rng = actor.getGame().getRandomProvider();
            Minion minion = deadMinions.get(rng.roll(deadMinions.size()));
            player.summonMinion(minion.getBaseDescr());
        };

    /**
     * {@link TargetlessAction} which detroys the weapon of the actor's owner and deals its damage to all
     * enemies.
     * <p>
     * See spell <em>Blade Flurry</em>.
     */
    // TODO The action now deals damage equal to the hero's attack, instead of the weapon's attack. See if
    // TODO it's accurate when the hero is buffed with something like `Rockbiter Weapon` or `Shapeshift`.
    public static final TargetlessAction<DamageSource> BLADE_FLURRY =
        (DamageSource actor) -> {
            Player player = actor.getOwner();
            if (player.tryGetWeapon() == null)
                return;

            int damage = player.getHero().getAttackTool().getAttack();
            player.destroyWeapon();

            EntitySelector<DamageSource, Character> targets = EntitySelectors.enemyTargets();
            TargetlessAction<DamageSource> damageAction = damageTarget(targets, damage);

            damageAction.apply(actor);
        };

    /**
     * {@link TargetlessAction} which eats other minions' <b>Divine Shield</b> and buffs the actor minion
     * {@code +3/+3} for each <b>Divine Shield</b> eaten.
     * <p>
     * See minion <em>Blood Knight</em>.
     */
    public static final TargetlessAction<Minion> EAT_DIVINE_SHIELDS = eatDivineShields(3, 3);

    /**
     * {@link TargetlessAction} which destroys the actor minion's neighbouring minions and adds their attack and health
     * to the actor minion.
     * <p>
     * See spell <em>Void Terror</em>.
     */
    public static final TargetlessAction<Minion> CONSUME_NEIGHBOURS =
        (Minion actor) -> {
            int attackBuff = 0;
            int hpBuff = 0;

            BoardSide board = actor.getOwner().getBoard();
            int actorLoc = board.indexOf(actor);

            Minion left = board.getMinion(actorLoc - 1);
            if (left != null) {
                attackBuff += left.getAttackTool().getAttack();
                hpBuff += left.getBody().getCurrentHp();
                TargetedActions.KILL_TARGET.apply(actor, left);
            }

            Minion right = board.getMinion(actorLoc + 1);
            if (right != null) {
                attackBuff += right.getAttackTool().getAttack();
                hpBuff += right.getBody().getCurrentHp();
                TargetedActions.KILL_TARGET.apply(actor, right);
            }

            if (hpBuff != 0 && attackBuff != 0) {
                actor.getBuffableAttack().addBuff(attackBuff);
                actor.getBody().getHp().buffHp(hpBuff);
            }
        };

    /**
     * {@link TargetlessAction} which destroys the opponent's weapon and draw cards for the actor's owner
     * equal to its Durability.
     * <p>
     * See minion <em>Harrison Jones</em>.
     */
    public static final TargetlessAction<PlayerProperty> DRAW_CARD_FOR_OPPONENTS_WEAPON =
        (actor) -> {
            Player player = actor.getOwner();
            Player opponent = player.getOpponent();
            Weapon weapon = opponent.tryGetWeapon();
            if (weapon == null)
                return;

            int durability = weapon.getDurability();
            DESTROY_OPPONENTS_WEAPON.apply(player);

            for (int i = 0; i < durability; i++)
                TargetlessActions.DRAW_FOR_SELF.apply(player);
        };

    /**
     * {@link TargetlessAction} which steals a random secret from the opponent.
     * <p>
     * See minion <em>Kezan Mystic</em>.
     */
    public static final TargetlessAction<PlayerProperty> STEAL_SECRET =
        (PlayerProperty actor) -> {
            Player player = actor.getOwner();
            Player opponent = player.getOpponent();
            List<Secret> opponentSecrets = opponent.getSecrets().getSecrets();
            if (opponentSecrets.isEmpty())
                return;

            Map<EntityName, Secret> stealCandidates = new HashMap<>();
            opponentSecrets.forEach((secret) -> stealCandidates.put(secret.getSecretId(), secret));

            List<Secret> ourSecrets = player.getSecrets().getSecrets();
            ourSecrets.forEach((secret) -> stealCandidates.remove(secret.getSecretId()));

            if (stealCandidates.isEmpty()) {
                Secret selected = ActionUtils.pickRandom(actor.getGame(), opponentSecrets);
                if (selected == null)
                    return;

                opponent.getSecrets().removeSecret(selected);
            } else {
                Secret selected = ActionUtils.pickRandom(actor.getGame(), new ArrayList<>(stealCandidates.values()));
                if (selected == null)
                    return;

                player.getSecrets().stealActivatedSecret(opponent.getSecrets(), selected);
            }
        };

    /**
     * {@link TargetlessAction} which steals a random living minion from the opponent.
     * <p>
     * See minion <em>Sylvanas Windrunner</em>.
     */
    public static final TargetlessAction<PlayerProperty> STEAL_RANDOM_MINION =
        (actor) -> {
            Player player = actor.getOwner();
            Player opponent = player.getOpponent();
            List<Minion> minions = opponent.getBoard().getAliveMinions();
            if (minions.isEmpty())
                return;

            Minion stolenMinion = minions.get(actor.getGame().getRandomProvider().roll(minions.size()));
            player.getBoard().takeOwnership(stolenMinion);
        };

    /**
     * {@link TargetlessAction} which randomly selects a minion and destroys all other minions. Minions with
     * {@link Keywords#BRAWLER} will always be considered first.
     * <p>
     * See spell <em>Brawl</em> and minion <em>Dark Iron Bouncer</em>.
     */
    public static final TargetlessAction<? extends GameProperty> BRAWL =
        (actor) -> {
            Game game = actor.getGame();
            List<Minion> minions = new ArrayList<>(2 * Player.MAX_BOARD_SIZE);
            game.getPlayer1().getBoard().collectMinions(minions);
            game.getPlayer2().getBoard().collectMinions(minions);

            // TODO: Brawler shouldn't be a keyword because keywords cannot be silenced.
            List<Minion> brawlers = minions.stream()
                .filter((minion) -> minion.getKeywords().contains(Keywords.BRAWLER))
                .collect(Collectors.toList());

            Minion winner = brawlers.isEmpty()
                ? ActionUtils.pickRandom(game, minions)
                : ActionUtils.pickRandom(game, brawlers);

            for (Minion minion : minions)
                if (minion != winner)
                    minion.kill();
        };

    /**
     * {@link TargetlessAction} which summons a copy of the actor minion for the opponent.
     * <p>
     * See secret <em>Mirror Entity</em>.
     */
    public static final TargetlessAction<Minion> SUMMON_COPY_FOR_OPPONENT =
        (Minion minion) -> {
            Player receiver = minion.getOwner().getOpponent();
            Minion newMinion = new Minion(receiver, minion.getBaseDescr());
            newMinion.copyOther(minion);

            receiver.summonMinion(newMinion);
        };

    /**
     * {@link TargetlessAction} which makes the actor's owner draw cards until he/she has as many in hand as his/her
     * opponent.
     * <p>
     * See spell <em>Divine Favor</em>.
     */
    // TODO Check what would happen if there is not enough cards left in the deck.
    public static final TargetlessAction<PlayerProperty> DIVINE_FAVOR =
        (actor) -> {
            Player player = actor.getOwner();

            int playerHand = player.getHand().getCardCount();
            int opponentHand = player.getOpponent().getHand().getCardCount();
            if (playerHand >= opponentHand)
                return;

            int drawCount = opponentHand - playerHand;
            for (int i = 0; i < drawCount; i++)
                player.drawCardToHand();
        };

    /**
     * {@link TargetlessAction} which puts a copy of each friendly minion to the hand of the actor's owner.
     * <p>
     * See spell <em>Echo of Medivh</em>.
     */
    public static final TargetlessAction<PlayerProperty> ECHO_MINIONS =
        (actor) -> {
            Player player = actor.getOwner();
            BoardSide board = player.getBoard();
            List<Minion> minions = new ArrayList<>(board.getMaxSize());
            player.getBoard().collectMinions(minions);
            BornEntity.sortEntities(minions);

            Hand hand = player.getHand();
            for (Minion minion : minions)
                hand.addCard(minion.getBaseDescr().getBaseCard());
        };

    /**
     * Returns a {@link TargetlessAction} of {@code GameProperty} which will try to convert
     * the actor to the respective {@link Minion} and executes the given {@code TargetlessAction}
     * of {@code Minion}.
     */
    public static TargetlessAction<? extends GameProperty> withMinion(
        @NamedArg("action") TargetlessAction<? super Minion> action) {
        return applyToMinionAction(action);
    }

    /**
     * Returns a {@code TargetlessAction} which applies the given {@code TargetedAction}
     * to the given actor.
     */
    public static <Actor extends GameProperty> TargetlessAction<Actor> forSelf(
        @NamedArg("action") TargetedAction<? super Actor, ? super Actor> action) {
        return forTargets(EntitySelectors.self(), action);
    }

    /**
     * Returns a {@code TargetlessAction} which applies the given {@code TargetlessAction}
     * with the actors selected by the given {@code EntitySelector}.
     */
    public static <Actor extends GameProperty, FinalActor> TargetlessAction<Actor> forActors(
        @NamedArg("actors") EntitySelector<? super Actor, ? extends FinalActor> actors,
        @NamedArg("action") TargetlessAction<? super FinalActor> action) {
        ExceptionHelper.checkNotNullArgument(actors, "actors");
        ExceptionHelper.checkNotNullArgument(action, "action");

        return (Actor initialActor) -> actors.select(initialActor).forEach(action::apply);
    }

    /**
     * Returns a {@code TargetlessAction} which selects the {@code BornEntity} targets with the
     * given {@code EntitySelector} and applies the given {@code TargetedAction} to these targets
     * in the increasing order of their birth time.
     */
    public static <Actor extends GameProperty, Target extends BornEntity> TargetlessAction<Actor>
        forBornTargets(
        @NamedArg("selector") EntitySelector<? super Actor, ? extends Target> selector,
        @NamedArg("action") TargetedAction<? super Actor, ? super Target> action) {
        return forBornTargets(selector, action, false);
    }

    /**
     * Returns a {@code TargetlessAction} which selects the {@code BornEntity} targets with the
     * given {@code EntitySelector} and applies the given {@code TargetedAction} to these targets
     * in the increasing order of their birth time.
     * The given boolean field {@code atomic} determines if this action will be executed atomically.
     *
     * @see GameEvents#doAtomic(Action)
     */
    public static <Actor extends GameProperty, Target extends BornEntity> TargetlessAction<Actor>
        forBornTargets(
        @NamedArg("selector") EntitySelector<? super Actor, ? extends Target> selector,
        @NamedArg("action") TargetedAction<? super Actor, ? super Target> action,
        @NamedArg("atomic") boolean atomic) {
        return forTargets(EntitySelectors.sorted(selector, BornEntity.CMP), action, atomic);
    }

    /**
     * Returns a {@code TargetlessAction} which selects the targets with the given {@code EntitySelector}
     * and applies the given {@code TargetedAction} to those targets which is not the actor itself.
     */
    public static <Actor extends GameProperty, Target> TargetlessAction<Actor> forOtherTargets(
        @NamedArg("selector") EntitySelector<? super Actor, ? extends Target> selector,
        @NamedArg("action") TargetedAction<? super Actor, ? super Target> action) {
        return forOtherTargets(selector, action, false);
    }

    /**
     * Returns a {@code TargetlessAction} which selects the targets with the given {@code EntitySelector}
     * and applies the given {@code TargetedAction} to those targets which is not the actor itself.
     * The given boolean field {@code atomic} determines if this action will be executed atomically.
     *
     * @see GameEvents#doAtomic(Action)
     */
    public static <Actor extends GameProperty, Target> TargetlessAction<Actor> forOtherTargets(
        @NamedArg("selector") EntitySelector<? super Actor, ? extends Target> selector,
        @NamedArg("action") TargetedAction<? super Actor, ? super Target> action,
        @NamedArg("atomic") boolean atomic) {
        return forTargets(EntitySelectors.notSelf(selector), action, atomic);
    }

    /**
     * Returns a {@code TargetlessAction} which applies the given {@code TargetedAction} to all
     * targets selected by the given {@code EntitySelector}.
     */
    public static <Actor extends GameProperty, Target> TargetlessAction<Actor> forTargets(
        @NamedArg("selector") EntitySelector<? super Actor, ? extends Target> selector,
        @NamedArg("action") TargetedAction<? super Actor, ? super Target> action) {
        return forTargets(selector, action, false);
    }

    /**
     * Returns a {@code TargetlessAction} which applies the given {@code TargetedAction} to all
     * targets selected by the given {@code EntitySelector}. The given boolean field {@code atomic}
     * determines if this action will be executes atomically.
     *
     * @see GameEvents#doAtomic(Action)
     */
    public static <Actor extends GameProperty, Target> TargetlessAction<Actor> forTargets(
        @NamedArg("selector") EntitySelector<? super Actor, ? extends Target> selector,
        @NamedArg("action") TargetedAction<? super Actor, ? super Target> action,
        @NamedArg("atomic") boolean atomic) {
        ExceptionHelper.checkNotNullArgument(selector, "targets");
        ExceptionHelper.checkNotNullArgument(action, "action");

        TargetlessAction<Actor> resultAction = (Actor actor) -> {
            selector.select(actor).forEach(
                (Target target) -> action.apply(actor, target)
            );
        };

        if (atomic)
            return (Actor actor) ->
                actor.getGame().getEvents().doAtomic(() -> resultAction.apply(actor));
        return resultAction;
    }

    /**
     * Returns a {@code TargetlessAction} which executes the given {@code TargetlessAction}
     * if the given actor satisfies the given {@code Predicate}.
     */
    public static <Actor extends GameProperty> TargetlessAction<Actor> doIf(
        @NamedArg("condition") Predicate<? super Actor> condition,
        @NamedArg("if") TargetlessAction<? super Actor> ifAction) {
        return doIf(condition, ifAction, TargetlessAction.DO_NOTHING);
    }

    /**
     * Returns a {@code TargetlessAction} which executes the given {@code ifAction} if the given
     * actor satisfies the given {@code Predicate}, and executes the given {@code elseAction}
     * otherwise.
     */
    public static <Actor extends GameProperty> TargetlessAction<Actor> doIf(
        @NamedArg("condition") Predicate<? super Actor> condition,
        @NamedArg("if") TargetlessAction<? super Actor> ifAction,
        @NamedArg("else") TargetlessAction<? super Actor> elseAction) {
        ExceptionHelper.checkNotNullArgument(condition, "condition");
        ExceptionHelper.checkNotNullArgument(ifAction, "ifAction");
        ExceptionHelper.checkNotNullArgument(elseAction, "elseAction");

        return (Actor actor) -> {
            if (condition.test(actor))
                ifAction.apply(actor);
            else
                elseAction.apply(actor);
        };
    }

    /**
     * Returns a {@code TargetlessAction} which applies the given action on the opponent
     * of the given actor.
     */
    public static <Actor extends PlayerProperty> TargetlessAction<Actor> actWithOpponent(
        @NamedArg("action") TargetlessAction<? super Player> action) {
        ExceptionHelper.checkNotNullArgument(action, "action");
        return (Actor actor) -> action.apply(actor.getOwner().getOpponent());
    }

    /**
     * Returns a {@code TargetlessAction} which executes the given action for the given
     * number of times.
     */
    public static <Actor extends GameProperty> TargetlessAction<Actor> doMultipleTimes(
        @NamedArg("actionCount") int actionCount,
        @NamedArg("action") TargetlessAction<? super Actor> action) {
        ExceptionHelper.checkNotNullArgument(action, "action");
        return (Actor actor) -> {
            for (int i = 0; i < actionCount; i++)
                action.apply(actor);
        };
    }

    /**
     * Returns a {@code TargetlessAction} which makes the owner of the given actor equip the
     * {@code Weapon} provided by the given {@link WeaponProvider}.
     */
    public static <Actor extends PlayerProperty> TargetlessAction<Actor> equipWeapon(
        @NamedArg("weapon") WeaponProvider weapon) {
        ExceptionHelper.checkNotNullArgument(weapon, "weapon");
        return (Actor actor) -> {
            Player player = actor.getOwner();
            player.equipWeapon(weapon.getWeapon());
        };
    }

    /**
     * Returns a {@code TargetlessAction} which makes the owner of the given actor equip the
     * first weapon returned by the given {@code EntitySelector}.
     */
    public static <Actor extends PlayerProperty> TargetlessAction<Actor> equipSelectedWeapon(
        @NamedArg("weapon") EntitySelector<? super Actor, ? extends WeaponDescr> weapon) {
        ExceptionHelper.checkNotNullArgument(weapon, "weapon");
        return (Actor actor) -> {
            Player player = actor.getOwner();
            // Equip the first weapon, since equiping multiple weapons make no sense.
            WeaponDescr toEquip = weapon.select(actor).findFirst().orElse(null);
            if (toEquip != null)
                player.equipWeapon(toEquip);
        };
    }

    /**
     * Returns a {@code TargetlessAction} which summons the minion provided by the given
     * {@code MinionProvider} on the left of the given actor.
     */
    public static TargetlessAction<Minion> summonMinionLeft(
        @NamedArg("minion") MinionProvider minion) {
        ExceptionHelper.checkNotNullArgument(minion, "minion");
        return (Minion actor) -> {
            Player owner = actor.getOwner();
            int actorIndex = owner.getBoard().indexOf(actor);
            if (actorIndex == -1)
                throw new IllegalStateException("The given actor minion `" + actor
                    + "` is not on the board");

            owner.summonMinion(minion.getMinion(), actorIndex - 1);
        };
    }

    /**
     * Returns a {@code TargetlessAction} which summons the minion provided by the given
     * {@code MinionProvider} on the right of the given actor.
     */
    public static TargetlessAction<Minion> summonMinionRight(
        @NamedArg("minion") MinionProvider minion) {
        ExceptionHelper.checkNotNullArgument(minion, "minion");
        return (Minion actor) -> {
            Player owner = actor.getOwner();
            int actorIndex = owner.getBoard().indexOf(actor);
            if (actorIndex == -1)
                throw new IllegalStateException("The given actor minion `" + actor
                    + "` is not on the board");

            owner.summonMinion(minion.getMinion(), actorIndex + 1);
        };
    }

    /**
     * Returns a {@code TargetlessAction} which summons the minion provided by the given
     * {@code MinionProvider} on the right most of the player's board.
     */
    public static <Actor extends PlayerProperty> TargetlessAction<Actor> summonMinion(
        @NamedArg("minion") MinionProvider minion) {
        ExceptionHelper.checkNotNullArgument(minion, "minion");
        return (Actor actor) -> {
            Player player = actor.getOwner();
            player.summonMinion(minion.getMinion());
        };
    }

    /**
     * Returns a {@code TargetlessAction} which summons given number of the minions provided
     * by the given {@code MinionProvider} on the right most of the player's board.
     */
    public static <Actor extends PlayerProperty> TargetlessAction<Actor> summonMinion(
        @NamedArg("minionCount") int minionCount,
        @NamedArg("minion") MinionProvider minion) {
        ExceptionHelper.checkNotNullArgument(minion, "minion");

        if (minionCount <= 0)
            return TargetlessAction.DO_NOTHING;

        if (minionCount == 1)
            return summonMinion(minion);

        return summonMinion(minionCount, minionCount, minion);
    }

    /**
     * Returns a {@code TargetlessAction} which summons the minions provided
     * by the given {@code MinionProvider} on the right most of the player's board for number of
     * times, randomly selected within the given {@code min} and {@code max} boundary.
     */
    public static <Actor extends PlayerProperty> TargetlessAction<Actor> summonMinion(
        @NamedArg("minMinionCount") int minMinionCount,
        @NamedArg("maxMinionCount") int maxMinionCount,
        @NamedArg("minion") MinionProvider minion) {
        return (actor) -> {
            Player player = actor.getOwner();

            MinionDescr minionDescr = minion.getMinion();

            int minionCount = actor.getGame().getRandomProvider().roll(minMinionCount, maxMinionCount);

            for (int i = 0; i < minionCount; i++)
                player.summonMinion(minionDescr);
        };
    }

    /**
     * Returns a {@code TargetlessAction} which summons the minions selected by the given
     * {@code EntitySelector} on the right most of the player's board.
     */
    public static <Actor extends PlayerProperty> TargetlessAction<Actor> summonSelectedMinion(
        @NamedArg("minion") EntitySelector<? super Actor, ? extends MinionDescr> minion) {
        ExceptionHelper.checkNotNullArgument(minion, "minion");
        return (Actor actor) -> {
            Player player = actor.getOwner();
            minion.forEach(actor, (toSummon) -> player.summonMinion(toSummon));
        };
    }

    /**
     * Returns a {@code TargetlessAction} which summons the minions selected by the given
     * {@code EntitySelector} on the right of the given actor minion.
     */
    public static TargetlessAction<Minion> summonSelectedRight(
        @NamedArg("minion") EntitySelector<? super Minion, ? extends MinionDescr> minion) {
        ExceptionHelper.checkNotNullArgument(minion, "minion");
        return (Minion actor) -> {
            Player owner = actor.getOwner();
            int actorIndex = owner.getBoard().indexOf(actor);
            if (actorIndex == -1)
                throw new IllegalStateException("The given actor minion `" + actor
                    + "` is not on the board");
            minion.forEach(actor, (toSummon) -> owner.summonMinion(toSummon, actorIndex + 1));
        };
    }

    /**
     * Returns a {@code TargetlessAction} which shuffles the {@code Card} provided by the given
     * {@code CardProvider} to the deck of the given actor's owner.
     */
    public static TargetlessAction<PlayerProperty> shuffleCardIntoDeck(
        @NamedArg("card") CardProvider card) {
        ExceptionHelper.checkNotNullArgument(card, "card");

        return (PlayerProperty actor) -> {
            Deck deck = actor.getOwner().getDeck();
            deck.shuffle(actor.getGame().getRandomProvider(), card.getCard());
        };
    }

    /**
     * Returns a {@code TargetlessAction} which damages all the targets selected by the given
     * {@code EntitySelector} atomically for the given amount.
     */
    public static <Actor extends DamageSource> TargetlessAction<Actor> damageTarget(
        @NamedArg("selector") EntitySelector<Actor, ? extends Character> selector,
        @NamedArg("damage") int damage) {
        return damageTarget(selector, damage, damage);
    }

    /**
     * Returns a {@code TargetlessAction} which damages all the targets selected by the given
     * {@code EntitySelector} atomically, with the damage amount randomly selected withing
     * the given {@code min} and {@code max} value.
     */
    public static <Actor extends DamageSource> TargetlessAction<Actor> damageTarget(
        @NamedArg("selector") EntitySelector<Actor, ? extends Character> selector,
        @NamedArg("minDamage") int minDamage,
        @NamedArg("maxDamage") int maxDamage) {
        return forBornTargets(selector, TargetedActions.damageTarget(minDamage, maxDamage), true);
    }

    /**
     * Returns a {@code TargetlessAction} which adds the given number of mana crystals to the
     * actor's owner.
     */
    public static <Actor extends PlayerProperty> TargetlessAction<Actor> addManaCrystal(
        @NamedArg("amount") int amount) {
        return addManaCrystal(true, amount);
    }

    /**
     * Returns a {@code TargetlessAction} which adds the given number of mana crystals to the
     * actor's owner. The given boolean field {@code empty} determines if these mana crystals
     * are empty.
     */
    public static <Actor extends PlayerProperty> TargetlessAction<Actor> addManaCrystal(
        @NamedArg("empty") boolean empty,
        @NamedArg("amount") int amount) {
        return (Actor actor) -> {
            ManaResource manaResource = actor.getOwner().getManaResource();
            manaResource.setManaCrystals(Math.max(0, manaResource.getManaCrystals() + amount));
            if (empty)
                return;

            manaResource.setMana(manaResource.getMana() + amount);
        };
    }

    /**
     * Returns a {@code TargetlessAction} which makes the given actor deal the given number of
     * missile damages to targets randomly selected from all enemy living targets.
     * <p>
     * See spell <em>Arcane Missiles</em>.
     */
    public static <Actor extends DamageSource> TargetlessAction<Actor> dealMissileDamage(
        @NamedArg("missileCount") int missileCount) {
        return dealMissileDamage(
            EntitySelectors.filtered(EntityFilters.random(), EntitySelectors.enemyLivingTargets()),
            missileCount);
    }

    /**
     * Returns a {@code TargetlessAction} which makes the given actor deal the given number of
     * missile damages to all target selected by the given {@code EntitySelector}.
     */
    public static <Actor extends DamageSource> TargetlessAction<Actor> dealMissileDamage(
        @NamedArg("selector") EntitySelector<Actor, ? extends Character> selector,
        @NamedArg("missileCount") int missileCount) {
        ExceptionHelper.checkNotNullArgument(selector, "selector");

        return (Actor actor) -> {
            int appliedMissileCount = actor.createDamage(missileCount).getDamage();

            Damage damage = new Damage(actor, 1);
            Consumer<Character> damageAction =
                (target) -> target.damage(damage);
            for (int i = 0; i < appliedMissileCount; i++)
                selector.select(actor).forEach(damageAction);
        };
    }

    /**
     * Returns a {@code TargetlessAction} which adds the given card to the hand of
     * the given actor's owner.
     */
    public static <Actor extends PlayerProperty> TargetlessAction<Actor> addCard(
        @NamedArg("card") CardProvider card) {
        return addSelectedCard((Actor actor) -> Stream.of(card.getCard()));
    }

    /**
     * Returns a {@code TargetlessAction} which adds the cards selected by the given
     * {@code EntitySelector} to the hand of the given actor's owner.
     */
    public static <Actor extends PlayerProperty> TargetlessAction<Actor> addSelectedCard(
        @NamedArg("card") EntitySelector<? super Actor, ? extends CardDescr> card) {
        ExceptionHelper.checkNotNullArgument(card, "card");
        return addSelectedCard(0, card);
    }

    /**
     * Returns a {@code TargetlessAction} which adds the cards selected by the given
     * {@code EntitySelector} to the hand of the given actor's owner, with given number
     * of cost reduction.
     */
    public static <Actor extends PlayerProperty> TargetlessAction<Actor> addSelectedCard(
        @NamedArg("costReduction") int costReduction,
        @NamedArg("card") EntitySelector<? super Actor, ? extends CardDescr> card) {
        ExceptionHelper.checkNotNullArgument(card, "card");

        return (Actor actor) ->
            addCards(actor, card, costReduction);
    }

    /**
     * Adds the cards selected by the given {@code EntitySelector} to the hand of the given actor's
     * owner, with given number of cost reduction.
     */
    private static <Actor extends PlayerProperty, CardType extends CardDescr> void addCards(
        Actor actor,
        EntitySelector<? super Actor, CardType> cards,
        int costReduction) {
        Player player = actor.getOwner();
        Hand hand = actor.getOwner().getHand();
        if (costReduction == 0) {
            cards.forEach(actor, hand::addCard);
        } else {
            cards.forEach(actor, (cardDescr) -> {
                Card toAdd = new Card(player, cardDescr);
                toAdd.decreaseManaCost(costReduction);
                hand.addCard(toAdd);
            });
        }
    }

    /**
     * Returns a {@code TargetlessAction} which buffs the given actor with the given
     * {@code PermanentBuff}.
     */
    public static TargetlessAction<? extends GameProperty> buffSelfMinion(
        @NamedArg("buff") PermanentBuff<? super Minion> buff) {
        TargetlessAction<Minion> buffAction = forSelf(TargetedActions.buffTarget(buff));
        return applyToMinionAction(buffAction);
    }

    /**
     * Returns a {@code TargetlessAction} which buffs the given actor with the given
     * {@code Buff}.
     */
    public static TargetlessAction<? extends GameProperty> buffSelfMinionThisTurn(
        @NamedArg("buff") Buff<? super Minion> buff) {
        TargetlessAction<Minion> buffAction = forSelf(TargetedActions.buffTargetThisTurn(buff));
        return applyToMinionAction(buffAction);
    }

    /**
     * Returns a {@link TargetlessAction} of {@code GameProperty} which will try to convert
     * the actor to the respective {@link Minion} and executes the given {@code TargetlessAction}
     * of {@code Minion}.
     */
    private static TargetlessAction<? extends GameProperty>
    applyToMinionAction(TargetlessAction<? super Minion> action) {

        return (actor) -> {
            Minion minion = ActionUtils.tryGetMinion(actor);
            if (minion != null)
                action.apply(minion);
        };
    }

    /**
     * Returns a {@link TargetlessAction} which increases the actor's owner's armor with
     * the given amount.
     */
    public static TargetlessAction<PlayerProperty> armorUp(@NamedArg("armor") int armor) {
        return (actor) -> actor.getOwner().getHero().armorUp(armor);
    }

    /**
     * Randomly summons a totem provided by the given {@code MinionProvider}s to the given
     * {@code Player}'s board. The totem on the board will not be summoned again.
     */
    private static void rollTotem(Player player, MinionProvider[] totems) {
        Map<MinionName, MinionDescr> allowedMinions = new HashMap<>();
        for (MinionProvider minionProvider : totems) {
            MinionDescr minion = minionProvider.getMinion();
            allowedMinions.put(minion.getId(), minion);
        }
        for (Minion minion : player.getBoard().getAliveMinions())
            allowedMinions.remove(minion.getBaseDescr().getId());

        int allowedCount = allowedMinions.size();
        if (allowedCount == 0)
            return;

        int totemIndex = player.getGame().getRandomProvider().roll(allowedCount);

        Iterator<MinionDescr> minionItr = allowedMinions.values().iterator();
        MinionDescr selected = minionItr.next();
        for (int i = 0; i < totemIndex; i++)
            selected = minionItr.next();

        player.summonMinion(selected);
    }

    /**
     * Returns a {@code TargetlessAction} which randomly summons a totem provided by the given
     * {@code MinionProvider}s for the actor's owner. Totems on the board will not be summoned
     * again.
     * <p>
     * See Hero Power <em>Totemic Call</em>.
     */
    public static <Actor extends PlayerProperty> TargetlessAction<Actor> summonRandomTotem(
        @NamedArg("totems") MinionProvider... totems) {
        MinionProvider[] totemsCopy = totems.clone();
        ExceptionHelper.checkNotNullElements(totemsCopy, "totems");

        return (actor) -> rollTotem(actor.getOwner(), totemsCopy);
    }

    /**
     * Returns a {@link TargetlessAction} which eats other minions' <b>Divine Shield</b> and
     * buffs the actor minion with the given amount of attack and hp for each <b>Divine Shield</b> eaten.
     * <p>
     * See minion <em>Blood Knight</em>.
     */
    public static TargetlessAction<Minion> eatDivineShields(
        @NamedArg("attackPerShield") int attackPerShield,
        @NamedArg("hpPerShield") int hpPerShield) {
        return (Minion actor) -> {
            Game game = actor.getGame();

            AtomicInteger shieldCountRef = new AtomicInteger(0);
            Consumer<Minion> collector = (Minion minion) -> {
                MinionBody body = minion.getBody();
                if (body.isDivineShield()) {
                    shieldCountRef.incrementAndGet();
                    body.setDivineShield(false);
                }
            };

            game.getPlayer1().getBoard().forAllMinions(collector);
            game.getPlayer2().getBoard().forAllMinions(collector);
            int shieldCount = shieldCountRef.get();
            if (shieldCount <= 0) {
                return;
            }

            actor.getBuffableAttack().addBuff(attackPerShield * shieldCount);
            actor.getBody().getHp().buffHp(hpPerShield * shieldCount);
        };
    }

    /**
     * Returns a {@link TargetlessAction} which reduces the player's weapon's durability with
     * the given amount.
     */
    public static TargetlessAction<PlayerProperty> reduceWeaponDurability(@NamedArg("amount") int amount) {
        ExceptionHelper.checkArgumentInRange(amount, 1, Integer.MAX_VALUE, "amount");

        return (PlayerProperty actor) -> {
            Weapon weapon = actor.getOwner().tryGetWeapon();
            if (weapon == null)
                return;

            for (int i = 0; i < amount; i++)
                weapon.decreaseCharges();
        };
    }

    /**
     * Returns a {@link TargetlessAction} which deals the given amount of damage to a random minion and repeats until
     * a minion's health drops to {@code 0} or it reaches the given maximum number of times.
     * <p>
     * See spell <em>Bouncing Blade</em>.
     */
    // TODO Check if Bouncing Blade has a maximum bounce time.
    public static TargetlessAction<DamageSource> bouncingBlade(
        @NamedArg("maxBounces") int maxBounces,
        @NamedArg("baseDamage") int baseDamage) {

        Predicate<Minion> minionFilter = (minion) -> {
            MinionBody body = minion.getBody();
            int currentHp = body.getCurrentHp();
            return currentHp > 0 && !body.isImmune() && body.getMinHpProperty().getValue() < currentHp;
        };

        return (DamageSource actor) -> {
            Game game = actor.getGame();
            Damage damage = actor.createDamage(baseDamage);

            List<Minion> targets = new ArrayList<>();
            for (int i = 0; i < maxBounces; i++) {
                targets.clear();
                game.getPlayer1().getBoard().collectMinions(targets, minionFilter);
                game.getPlayer2().getBoard().collectMinions(targets, minionFilter);

                Minion selected = ActionUtils.pickRandom(game, targets);
                if (selected == null) {
                    break;
                }

                selected.damage(damage);
                if (selected.getBody().getCurrentHp() <= 0)
                    break;
            }
        };
    }

    /**
     * Returns a {@link TargetlessAction} which draws a card for the actor's owner and reduce its cost with
     * the given amount.
     * <p>
     * See spell <em>Far Sight</em>.
     */
    public static TargetlessAction<PlayerProperty> drawCard(@NamedArg("costReduction") int costReduction) {
        return drawCard(costReduction, EventFilter.ANY);
    }

    /**
     * Returns a {@link TargetlessAction} which draws a card for the actor's owner and reduce its cost with
     * the given amount if it satisfies the given {@link EventFilter}.
     * <p>
     * See spell <em>Call Pet</em>.
     */
    public static TargetlessAction<PlayerProperty> drawCard(
        @NamedArg("costReduction") int costReduction,
        @NamedArg("costReductionFilter") EventFilter<? super Player, ? super Card> costReductionFilter) {
        ExceptionHelper.checkNotNullArgument(costReductionFilter, "costReductionFilter");
        if (costReduction == 0) {
            return TargetlessActions.DRAW_FOR_SELF;
        }

        return (PlayerProperty actor) -> {
            Player player = actor.getOwner();
            Card card = player.drawFromDeck();
            if (card == null)
                return;

            if (costReductionFilter.applies(player, card))
                card.decreaseManaCost(costReduction);

            player.addCardToHand(card);
        };
    }

    /**
     * Returns a {@link TargetlessAction} which draws a random card which has all of the given {@link Keyword}s
     * from the deck of the actor's owner and adds nothing to the player's hand if no such card exists.
     */
    public static TargetlessAction<PlayerProperty> getRandomFromDeck(
        @NamedArg("keywords") Keyword[] keywords) {
        return getRandomFromDeck(1, keywords);
    }

    /**
     * Returns a {@link TargetlessAction} which draws given number of random cards which has all of the given
     * {@link Keyword}s from the deck of the actor's owner and adds nothing to the player's hand if no such card exists.
     */
    public static TargetlessAction<PlayerProperty> getRandomFromDeck(
        @NamedArg("cardCount") int cardCount,
        @NamedArg("keywords") Keyword[] keywords) {
        return getRandomFromDeck(cardCount, keywords, null);
    }

    /**
     * Returns a {@link TargetlessAction} which draws a random card which has all of the given {@link Keyword}s
     * from the deck of the actor's owner and adds {@code fallbackCard} to the player's hand
     * if no such card exists.
     */
    public static TargetlessAction<PlayerProperty> getRandomFromDeck(
        @NamedArg("keywords") Keyword[] keywords,
        @NamedArg("fallbackCard") CardProvider fallbackCard) {
        return getRandomFromDeck(1, keywords, fallbackCard);
    }

    /**
     * Returns a {@link TargetlessAction} which draws given number of random cards which has all of the given
     * {@link Keyword}s from the deck of the actor's owner and adds {@code fallbackCard} to the player's hand
     * if no such card exists.
     */
    public static TargetlessAction<PlayerProperty> getRandomFromDeck(
        @NamedArg("cardCount") int cardCount,
        @NamedArg("keywords") Keyword[] keywords,
        @NamedArg("fallbackCard") CardProvider fallbackCard) {

        Predicate<LabeledEntity> cardFilter = ActionUtils.includedKeywordsFilter(keywords);
        return getRandomFromDeck(cardCount, cardFilter, fallbackCard);
    }

    /**
     * Returns a {@link TargetlessAction} which draws given number of random cards which satisfies the given
     * {@link Predicate} from the deck of the actor's owner and adds {@code fallbackCard} to the player's hand
     * if no such card exists.
     */
    public static TargetlessAction<PlayerProperty> getRandomFromDeck(
        int cardCount,
        Predicate<? super Card> cardFilter,
        CardProvider fallbackCard) {
        ExceptionHelper.checkArgumentInRange(cardCount, 1, Integer.MAX_VALUE, "cardCount");
        ExceptionHelper.checkNotNullArgument(cardFilter, "cardFilter");

        return (PlayerProperty actor) -> {
            Player player = actor.getOwner();

            boolean mayHaveCard = true;
            for (int i = 0; i < cardCount; i++) {
                Card selected = mayHaveCard
                    ? ActionUtils.pollDeckForCard(player, cardFilter)
                    : null;

                if (selected == null)
                    break;

                player.getHand().addCard(selected);
            }
        };
    }

    /**
     * Returns a {@code TargetlessAction} which destroys all the player's mech minions and summons the minion
     * provided by the given {@link MinionProvider} if the player has at least {@code 3} mech minions.
     * <p>
     * See minion <em>Mimiron's Head</em>.
     */
    public static TargetlessAction<Minion> mimironTransformation(@NamedArg("minion") MinionProvider minion) {
        ExceptionHelper.checkNotNullArgument(minion, "minion");

        Predicate<LabeledEntity> mechFilter = ActionUtils.includedKeywordsFilter(Keywords.RACE_MECH);

        return (Minion actor) -> {
            Player player = actor.getOwner();

            List<Minion> mechs = new ArrayList<>();
            player.getBoard().collectAliveMinions(mechs, mechFilter);

            if (mechs.size() >= 3) {
                for (Minion mech : mechs)
                    mech.kill();

                actor.getGame().endPhase();
                player.summonMinion(minion.getMinion());
            }
        };
    }

    /**
     * Returns a {@code TargetlessAction} which activates the given {@code Ability} for the given actor
     * until the end of this turn.
     * <p>
     * See spell <em>Commanding Shout</em>.
     */
    public static <Actor extends Entity> TargetlessAction<Actor> addThisTurnAbility(
        @NamedArg("ability") Ability<Actor> ability) {
        ExceptionHelper.checkNotNullArgument(ability, "ability");
        Ability<Actor> onTurnAbility = ActionUtils.toSingleTurnAbility(ability);
        return onTurnAbility::activate;
    }

    /**
     * Returns a {@link TargetlessAction} which randomly summons a minion from the deck of the actor's owner,
     * and summons the provided {@code fallbackMinion} if there is no more minion left in the deck.
     * If the {@code fallbackMinion} is {@code null} and there is no more minion in the deck, nothing will be summoned.
     */
    public static TargetlessAction<PlayerProperty> summonRandomMinionFromDeck(
        @NamedArg("fallbackMinion") MinionProvider fallbackMinion) {

        Predicate<Card> appliedFilter = (card) -> card.getMinion() != null;
        return (PlayerProperty actor) -> {
            Player player = actor.getOwner();

            Card card = ActionUtils.pollDeckForCard(player, appliedFilter);
            if (card == null && fallbackMinion == null)
                return;

            MinionDescr minion = card != null
                ? card.getMinion().getBaseDescr()
                : fallbackMinion.getMinion();
            assert minion != null;

            player.summonMinion(minion);
        };
    }

    /**
     * Returns a {@link TargetlessAction} which summons a random minion with all of the given {@link Keyword}s
     * from the actor's hand. If no such minion exists, nothing will be summoned.
     */
    public static TargetlessAction<Minion> summonRandomMinionFromHand(
        @NamedArg("keywords") Keyword[] keywords) {
        Predicate<LabeledEntity> cardFilter = ActionUtils.includedKeywordsFilter(keywords);

        return (actor) -> {
            Hand hand = actor.getOwner().getHand();
            int cardIndex = hand.chooseRandomCardIndex(cardFilter);
            if (cardIndex < 0)
                return;

            Card removedCard = hand.removeAtIndex(cardIndex);
            Minion minion = removedCard.getMinion();
            assert minion != null;

            Player owner = actor.getOwner();
            owner.summonMinion(minion, owner.getBoard().indexOf(actor) + 1);
        };
    }

    public static TargetlessAction<PlayerProperty> experiment(
        @NamedArg("replaceCard") CardProvider replaceCard) {
        ExceptionHelper.checkNotNullArgument(replaceCard, "replaceCard");

        return (PlayerProperty actor) -> {
            Player player = actor.getOwner();
            Card card = player.drawFromDeck();
            if (card == null)
                return;

            CardDescr cardDescr = card.getCardDescr();

            if (cardDescr.getCardType() == CardType.Minion) {
                Game game = actor.getGame();
                GameActionList.executeActionsNow(card, cardDescr.getOnDrawActions());
                player.getHand().addCard(replaceCard.getCard());
                game.getEvents().triggerEvent(SimpleEventType.DRAW_CARD, card);
            } else {
                player.addCardToHand(card);
            }
        };
    }

    /**
     * Returns a {@code TargetlessAction} which draws cards for the actor's owner until there is given
     * number of cards in the player's hand.
     */
    public static TargetlessAction<PlayerProperty> drawCardToFillHand(
        @NamedArg("targetHandSize") int targetHandSize) {
        return (PlayerProperty actor) -> {
            Player player = actor.getOwner();
            int currentHandSize = player.getHand().getCardCount();
            if (currentHandSize >= targetHandSize)
                return;


            int drawCount = targetHandSize - currentHandSize;
            for (int i = 0; i < drawCount; i++)
                player.drawCardToHand();
        };
    }

    /**
     * Returns a {@code TargetlessAction} which kills all the minions and replaces them with the
     * minion provided by the given {@code MinionProvider}.
     * <p>
     * See spell <em>Poison Seeds</em>.
     */
    public static TargetlessAction<GameProperty> killAndReplaceMinions(
        @NamedArg("minion") MinionProvider minion) {
        ExceptionHelper.checkNotNullArgument(minion, "minion");

        return (GameProperty actor) -> {
            Game game = actor.getGame();

            List<Minion> minions1 = new ArrayList<>(Player.MAX_BOARD_SIZE);
            List<Minion> minions2 = new ArrayList<>(Player.MAX_BOARD_SIZE);

            Player player1 = game.getPlayer1();
            Player player2 = game.getPlayer2();

            player1.getBoard().collectMinions(minions1);
            player2.getBoard().collectMinions(minions2);

            for (Minion killedMinion : minions1)
                killedMinion.kill();

            for (Minion killedMinion : minions2)
                killedMinion.kill();


            game.endPhase();

            TargetlessActions.summonMinion(minions1.size(), minion).apply(player1);
            TargetlessActions.summonMinion(minions2.size(), minion).apply(player2);
        };
    }

    /**
     * Returns a {@code TargetlessAction} which replaces the actor's owner's hero with the
     * given hero and hero power.
     * <p>
     * See minion <em>Lord Jaraxxus</em>.
     */
    public static TargetlessAction<Minion> replaceHero(
        @NamedArg("heroClass") Keyword heroClass,
        @NamedArg("heroPower") CardName heroPower) {

        return (Minion actor) -> {
            Player player = actor.getOwner();
            player.getBoard().removeFromBoard(actor.getEntityId());

            MinionBody body = actor.getBody();

            Hero hero = new Hero(player, body.getHp(), 0, heroClass, actor.getKeywords());
            hero.setCurrentHp(body.getCurrentHp());
            hero.setHeroPower(actor.getGame().getDb().getHeroPowerDb().getById(heroPower));

            player.setHero(hero);
        };
    }

    /**
     * Returns a {@code TargetlessAction} which replaces the player's hero with the new hero,
     * whose {@code hp}, {@code armor}, {@code heroPower} and {@code class}
     * are given by the parameters.
     * <p>
     * See minion <em>Majordomo Executus</em>.
     */
    public static TargetlessAction<PlayerProperty> replaceHero(
        @NamedArg("maxHp") int maxHp,
        @NamedArg("armor") int armor,
        @NamedArg("heroPower") String heroPower,
        @NamedArg("heroClass") Keyword heroClass) {
        return replaceHero(maxHp, armor, heroPower, heroClass, new Keyword[0]);
    }

    /**
     * Returns a {@code TargetlessAction} which replaces the player's hero with the new hero,
     * whose {@code hp}, {@code armor}, {@code heroPower}, {@code class} and {@code keywords}
     * are given by the parameters.
     * <p>
     * See minion <em>Majordomo Executus</em>.
     */
    public static TargetlessAction<PlayerProperty> replaceHero(
        @NamedArg("maxHp") int maxHp,
        @NamedArg("armor") int armor,
        @NamedArg("heroPower") String heroPower,
        @NamedArg("heroClass") Keyword heroClass,
        @NamedArg("keywords") Keyword[] keywords) {
        ExceptionHelper.checkNotNullArgument(heroPower, "heroPower");
        ExceptionHelper.checkNotNullArgument(heroClass, "heroClass");

        List<Keyword> keywordsCopy = new ArrayList<>(Arrays.asList(keywords));
        ExceptionHelper.checkNotNullElements(keywordsCopy, "keywords");

        return (PlayerProperty actor) -> {
            Player player = actor.getOwner();
            Hero hero = new Hero(player, maxHp, armor, heroClass, keywordsCopy);
            hero.setHeroPower(actor.getGame().getDb().getHeroPowerDb().getById(new CardName(heroPower)));

            player.setHero(hero);
        };
    }

    /**
     * See spell <em>Shadowform</em>.
     */
    public static TargetlessAction<PlayerProperty> replaceHeroPower(
        @NamedArg("heroPower") CardName[] heroPower) {
        ExceptionHelper.checkArgumentInRange(heroPower.length, 1, Integer.MAX_VALUE, "heroPower.length");
        CardName[] heroPowerCopy = heroPower.clone();
        ExceptionHelper.checkNotNullElements(heroPowerCopy, "heroPower");


        return (PlayerProperty actor) -> {
            Hero hero = actor.getOwner().getHero();

            CardName currentId = hero.getHeroPower().getPowerDef().getId();
            CardName newId = heroPowerCopy[0];
            for (int i = 0; i < heroPowerCopy.length; i++) {
                if (currentId.equals(heroPowerCopy[i])) {
                    int selectedIndex = i + 1;
                    newId = selectedIndex >= heroPowerCopy.length
                        ? heroPowerCopy[heroPowerCopy.length - 1]
                        : heroPowerCopy[selectedIndex];
                    break;
                }
            }

            hero.setHeroPower(actor.getGame().getDb().getHeroPowerDb().getById(newId));
        };
    }

    /**
     * Returns a {@code TargetlessAction} whichi gives the given number of mana crystals to the actor's owner
     * this turn.
     * <p>
     * See spell <em>The Coin</em>.
     */
    public static TargetlessAction<PlayerProperty> gainMana(@NamedArg("mana") int mana) {
        return (actor) -> {
            Player player = actor.getOwner();
            player.setMana(player.getMana() + mana);
        };
    }

    /**
     * Returns a {@code TargetlessAction} which sets the cost of the next card which satisfies
     * the given {@code AuraFilter} played by the actor's owner in this turn.
     * <p>
     * See minion <em>Kirin Tor Mage</em>.
     */
    public static TargetlessAction<PlayerProperty> setManaCostThisTurn(
        @NamedArg("manaCost") int manaCost,
        @NamedArg("filter") AuraFilter<? super Player, ? super Card> filter) {
        ExceptionHelper.checkNotNullArgument(filter, "filter");

        return (PlayerProperty actor) -> {
            Ability<Player> aura = Abilities.aura(
                AuraTargetProviders.OWN_HAND_PROVIDER,
                filter,
                Auras.setManaCost(manaCost));
            aura = deactivateAfterPlay(aura, filter);
            aura = ActionUtils.toSingleTurnAbility(aura);

            aura.activate(actor.getOwner());
        };
    }

    /**
     * Returns a {@code TargetlessAction} which reduces the cost of the next card which satisfies
     * the given {@code AuraFilter} played by the actor's owner in this turn.
     * <p>
     * See spell <em>Preparation</em>.
     */
    public static TargetlessAction<PlayerProperty> reduceManaCostThisTurn(
        @NamedArg("amount") int amount,
        @NamedArg("filter") AuraFilter<? super Player, ? super Card> filter) {
        ExceptionHelper.checkNotNullArgument(filter, "filter");

        return (PlayerProperty actor) -> {
            Ability<Player> aura = Abilities.aura(
                AuraTargetProviders.OWN_HAND_PROVIDER,
                filter,
                Auras.decreaseManaCost(amount),
                true);
            aura = deactivateAfterPlay(aura, filter);
            aura = ActionUtils.toSingleTurnAbility(aura);

            aura.activate(actor.getOwner());
        };
    }

    /**
     * Returns a {@code TargetlessAction} which reduces the cost of the next card which satisfies
     * the given {@code AuraFilter} played by the actor's owner.
     * <p>
     * See minion <em>Dragon Consort</em>.
     */
    public static TargetlessAction<PlayerProperty> reduceManaCostNextCard(
        @NamedArg("amount") int amount,
        @NamedArg("filter") AuraFilter<? super Player, ? super Card> filter) {
        ExceptionHelper.checkNotNullArgument(filter, "filter");

        return (PlayerProperty actor) -> {
            Ability<Player> aura = Abilities.aura(
                AuraTargetProviders.OWN_HAND_PROVIDER,
                filter,
                Auras.decreaseManaCost(amount));
            aura = deactivateAfterPlay(aura, filter);

            aura.activate(actor.getOwner());
        };
    }

    /**
     * Returns a {@link Ability} which activates the given {@code Ability} when it's activated, and
     * deactivate the ability when the next card which satisfies the given {@code AuraFilter} is played.
     */
    private static Ability<Player> deactivateAfterPlay(
        Ability<Player> ability,
        AuraFilter<? super Player, ? super Card> filter) {
        return deactivateAfterCardPlay(ability,
            (card) -> filter.isApplicable(card.getGame(), card.getOwner(), card));
    }

    /**
     * Returns a {@link Ability} which activates the given {@code Ability} when it's activated, and
     * deactivates the ability when the next card which satisfies the given {@code Predicate} is played.
     */
    private static Ability<Player> deactivateAfterCardPlay(
        Ability<Player> ability,
        Predicate<Card> deactivateCondition) {
        ExceptionHelper.checkNotNullArgument(ability, "ability");
        ExceptionHelper.checkNotNullArgument(deactivateCondition, "deactivateCondition");

        return (Player self) -> {
            UndoAction.Builder<Player> undoRef = new UndoAction.Builder<>(2);

            UndoAction<Player> abilityUndo = ability.activate(self);
            undoRef.add(abilityUndo);

            GameEvents events = self.getGame().getEvents();

            GameEventActions<CardPlayEvent> listeners = events.simpleListeners(SimpleEventType.PLAY_CARD);
            UndoAction<GameEventActions> eventUndo =
                listeners.register((CardPlayEvent playEvent) -> {
                    if (deactivateCondition.test(playEvent.getCard()))
                        abilityUndo.undo(playEvent.getOwner());
                }, true);
            undoRef.add((Player p) ->
                eventUndo.undo(p.getGame().getEvents().simpleListeners(SimpleEventType.PLAY_CARD)));

            return undoRef;
        };
    }

    /**
     * Returns a {@code TargetlessAction} which adds the given {@code Aura} with the given
     * {@code AuraTargetProvider} until the start of turn.
     * <p>
     * See minion <em>Loatheb</em>.
     */
    public static <Target extends Entity> TargetlessAction<PlayerProperty> untilTurnStartsAura(
        @NamedArg("target") AuraTargetProvider<? super Player, ? extends Target> target,
        @NamedArg("aura") Aura<? super Player, ? super Target> aura) {
        return untilTurnStartsAura(target, AuraFilter.ANY, aura);
    }

    /**
     * Returns a {@code TargetlessAction} which adds the given {@code Aura} with the given
     * {@code AuraTargetProvider} and {@code AuraFilter} until the start of turn.
     * <p>
     * See minion <em>Loatheb</em>.
     */
    public static <Target extends Entity> TargetlessAction<PlayerProperty> untilTurnStartsAura(
        @NamedArg("target") AuraTargetProvider<? super Player, ? extends Target> target,
        @NamedArg("filter") AuraFilter<? super Player, ? super Target> filter,
        @NamedArg("aura") Aura<? super Player, ? super Target> aura) {

        return (PlayerProperty actor) -> {
            Player player = actor.getOwner();
            ActionUtils.doUntilNewTurnStart(player.getPlayerId(), () -> {
                return player.getGame().addAura(new ActiveAura<>(player, target, filter, aura));
            });
        };
    }

    /**
     * Returns a {@code TargetlessAction} which executes the given {@code TargetlessAction} on the end of turn.
     * <p>
     * See minion <em>Echoing Ooze</em>.
     */
    public static <Actor extends PlayerProperty> TargetlessAction<Actor> doOnEndOfTurn(
        @NamedArg("action") TargetlessAction<? super Actor> action) {
        ExceptionHelper.checkNotNullArgument(action, "action");

        return (Actor actor) -> ActionUtils.doOnEndOfTurn(actor.getGame(), (p) -> action.apply(actor));
    }

    /**
     * Returns a {@code TargetlessAction} which executes the given {@code TargetlessAction} on the end of turn.
     * <p>
     * See spell <em>Headcrack</em>.
     */
    public static <Actor extends Entity> TargetlessAction<Actor> doOnEndOfTurn(
        @NamedArg("action") TargetlessAction<? super Entity> action,
        @NamedArg("isFromSpell") boolean isFromSpell) {
        ExceptionHelper.checkNotNullArgument(action, "action");

        return (Actor actor) -> ActionUtils.doOnEndOfTurn(actor.getGame(),
            action::apply,
            isFromSpell);
    }

    /**
     * Returns a {@code TargetlessAction} which draws a random card with all of the given {@code Keyword}s
     * from the deck and plays it.
     */
    public static <Actor extends PlayerProperty> TargetlessAction<Actor> drawAndPlayCard(
        @NamedArg("keywords") Keyword[] keywords) {
        Predicate<LabeledEntity> cardFilter = ActionUtils.includedKeywordsFilter(keywords);
        return drawAndPlayCard(cardFilter);
    }

    private static <Actor extends PlayerProperty> TargetlessAction<Actor> drawAndPlayCard(Predicate<? super Card> cardFilter) {
        ExceptionHelper.checkNotNullArgument(cardFilter, "cardFilter");

        return (Actor actor) -> {
            Player player = actor.getOwner();
            Card card = ActionUtils.pollDeckForCard(player, cardFilter);
            if (card == null)
                return;

            player.playCardEffect(card);
        };
    }

    private static CardDescr chooseCard(Game game, List<CardDescr> cards) {
        int cardCount = cards.size();
        if (cardCount == 0) {
            return null;
        }

        if (cardCount == 1) {
            return cards.get(0);
        }

        return game.getUserAgent().selectCard(false, cards);
    }

    /**
     * See spell <em>Tracking</em>.
     */
    public static TargetlessAction<PlayerProperty> trackCard(@NamedArg("cardCount") int cardCount) {
        ExceptionHelper.checkArgumentInRange(cardCount, 1, Integer.MAX_VALUE, "cardCount");

        return (PlayerProperty actor) -> {
            Player player = actor.getOwner();
            Deck deck = player.getDeck();

            List<CardDescr> choosenCards = new ArrayList<>(cardCount);
            for (int i = 0; i < cardCount; i++) {
                Card card = deck.tryDrawOneCard();
                if (card == null)
                    break;

                choosenCards.add(card.getCardDescr());
            }

            CardDescr chosenCard = chooseCard(actor.getGame(), choosenCards);
            if (chosenCard != null)
                player.getHand().addCard(chosenCard);
        };
    }

    /**
     * Returns a {@code TargetlessAction} which summons a minion provided by the given {@code MinionProvider}
     * for each enemy minion.
     * <p>
     * See spell <em>Unleash the Hounds</em>.
     */
    public static TargetlessAction<PlayerProperty> unleashMinions(
        @NamedArg("minion") MinionProvider minion) {
        ExceptionHelper.checkNotNullArgument(minion, "minion");

        return (PlayerProperty actor) -> {
            Player player = actor.getOwner();
            int minionCount = player.getOpponent().getBoard().getMinionCount();

            MinionDescr toSummon = minion.getMinion();
            for (int i = 0; i < minionCount; i++)
                player.summonMinion(toSummon);
        };
    }

    private TargetlessActions() {
        throw new AssertionError();
    }
}
