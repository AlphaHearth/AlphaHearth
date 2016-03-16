package info.hearthsim.brazier.actions;

import info.hearthsim.brazier.*;
import info.hearthsim.brazier.Character;
import info.hearthsim.brazier.abilities.*;
import info.hearthsim.brazier.events.*;
import info.hearthsim.brazier.minions.Minion;
import info.hearthsim.brazier.actions.undo.UndoableResult;
import info.hearthsim.brazier.cards.Card;
import info.hearthsim.brazier.cards.CardDescr;
import info.hearthsim.brazier.cards.CardId;
import info.hearthsim.brazier.cards.CardProvider;
import info.hearthsim.brazier.cards.CardType;
import info.hearthsim.brazier.actions.undo.UndoableUnregisterAction;
import info.hearthsim.brazier.events.GameEventFilter;
import info.hearthsim.brazier.minions.MinionBody;
import info.hearthsim.brazier.minions.MinionDescr;
import info.hearthsim.brazier.minions.MinionId;
import info.hearthsim.brazier.minions.MinionProvider;
import info.hearthsim.brazier.parsing.NamedArg;
import info.hearthsim.brazier.actions.undo.UndoAction;
import info.hearthsim.brazier.weapons.Weapon;
import info.hearthsim.brazier.weapons.WeaponDescr;
import info.hearthsim.brazier.weapons.WeaponProvider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
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
    public static final TargetlessAction<PlayerProperty> DRAW_FOR_SELF = (Game game, PlayerProperty actor) -> {
        return actor.getOwner().drawCardToHand();
    };

    /**
     * {@link TargetlessAction} which makes the opponent of the actor's owner draw a card from the deck.
     */
    public static final TargetlessAction<PlayerProperty> DRAW_FOR_OPPONENT = actWithOpponent(DRAW_FOR_SELF);

    /**
     * {@link TargetlessAction} which makes the actor's owner discard a random card from the hand.
     */
    public static final TargetlessAction<PlayerProperty> DISCARD_RANDOM_CARD = (game, actor) -> {
        Player player = actor.getOwner();
        Hand hand = player.getHand();
        int cardCount = hand.getCardCount();
        if (cardCount == 0) {
            return UndoAction.DO_NOTHING;
        }

        int cardIndex = game.getRandomProvider().roll(cardCount);
        // TODO: Show discarded card to the opponent.
        return hand.removeAtIndex(cardIndex);
    };

    /**
     * {@link TargetlessAction} which makes the actor's owner discard the card on the top of the deck.
     */
    public static final TargetlessAction<PlayerProperty> DISCARD_FROM_DECK = (game, actor) -> {
        Player player = actor.getOwner();
        Deck deck = player.getDeck();
        if (deck.getNumberOfCards() <= 0) {
            return UndoAction.DO_NOTHING;
        }

        UndoableResult<Card> cardRef = deck.tryDrawOneCard();
        // TODO: Show discarded card to the opponent.
        return cardRef != null ? cardRef.getUndoAction() : UndoAction.DO_NOTHING;
    };

    /**
     * {@code TargetlessAction} which re-summons a same minion on the right of the given minion.
     */
    public static final TargetlessAction<Minion> RESUMMON_RIGHT = (Game game, Minion minion) -> {
        BoardSide board = minion.getOwner().getBoard();
        int minionIndex = board.indexOf(minion.getTargetId());
        return board.getOwner().summonMinion(minion.getBaseDescr(), minionIndex + 1);
    };

    /**
     * {@code TargetedAction} which resurrects all minions that died in the last turn.
     */
    public static final TargetlessAction<PlayerProperty> RESURRECT_DEAD_MINIONS = (game, actor) -> {
        Player player = actor.getOwner();
        List<Minion> deadMinions = player.getGraveyard().getMinionsDiedThisTurn();
        if (deadMinions.isEmpty()) {
            return UndoAction.DO_NOTHING;
        }

        UndoAction.Builder result = new UndoAction.Builder(deadMinions.size());
        for (Minion minion: deadMinions) {
            result.addUndo(player.summonMinion(minion.getBaseDescr()));
        }
        return result;
    };

    /**
     * {@link TargetlessAction} which destroys the opponent's weapon.
     */
    public static final TargetlessAction<PlayerProperty> DESTROY_OPPONENTS_WEAPON = (game, actor) -> {
        return actor.getOwner().destroyWeapon();
    };

    /**
     * {@link TargetlessAction} which destroys all of the opponent's secrets.
     */
    public static final TargetlessAction<PlayerProperty> DESTROY_OPPONENT_SECRETS = (game, actor) -> {
        Player player = actor.getOwner();
        SecretContainer secrets = player.getOpponent().getSecrets();
        return secrets.removeAllSecrets();
    };

    /**
     * {@code TargetedAction} which discards all cards in the player's hand.
     */
    public static final TargetlessAction<PlayerProperty> DISCARD_HAND = (game, actor) -> {
        Player player = actor.getOwner();
        // TODO: Display discarded cards to opponent
        return player.getHand().discardAll();
    };

    /**
     * {@code TargetedAction} which reduces the player's weapon's durability by {@code 1}.
     */
    public static final TargetlessAction<PlayerProperty> REDUCE_WEAPON_DURABILITY = reduceWeaponDurability(1);

    /**
     * {@code TargetedAction} which swaps the given minion with a random minion card in the player's hand.
     */
    public static final TargetlessAction<Minion> SWAP_WITH_MINION_IN_HAND = (Game game, Minion minion) -> {
        Hand hand = minion.getOwner().getHand();
        int cardIndex = hand.chooseRandomCardIndex(Card::isMinionCard);
        if (cardIndex < 0) {
            return UndoAction.DO_NOTHING;
        }

        CardDescr newCard = minion.getBaseDescr().getBaseCard();
        UndoableResult<Card> replaceCardRef = hand.replaceAtIndex(cardIndex, newCard);
        Minion newMinion = replaceCardRef.getResult().getMinion();
        if (newMinion == null) {
            throw new IllegalStateException("Selected a card with no minion.");
        }

        UndoAction replaceMinionUndo = minion.getOwner().getBoard().replace(minion, newMinion);
        return () -> {
            replaceMinionUndo.undo();
            replaceCardRef.undo();
        };
    };

    /**
     * {@link TargetlessAction} which summons a random minion from the deck of the actor's owner. If there
     * is no more minion left in the deck, nothing will be summoned.
     * <p>
     * See minion <em>Deathlord</em>.
     */
    public static final TargetlessAction<PlayerProperty> SUMMON_RANDOM_MINION_FROM_DECK = summonRandomMinionFromDeck(null);

    /**
     * {@link TargetlessAction} which summons a random minion from the hand of the actor's owner. If there
     * is no more minion in the hand, nothing will be summoned.
     * <p>
     * See spell <em>Ancestor's Call</em>.
     */
    public static TargetlessAction<PlayerProperty> SUMMON_RANDOM_MINION_FROM_HAND = (game, actor) -> {
        Player player = actor.getOwner();
        Hand hand = player.getHand();
        int cardIndex = hand.chooseRandomCardIndex(Card::isMinionCard);
        if (cardIndex < 0) {
            return UndoAction.DO_NOTHING;
        }

        UndoableResult<Card> removedCardRef = hand.removeAtIndex(cardIndex);
        Minion minion = removedCardRef.getResult().getMinion();
        assert minion != null;

        UndoAction summonUndo = player.summonMinion(minion);
        return () -> {
            summonUndo.undo();
            removedCardRef.undo();
        };
    };

    /**
     * {@code TargetedAction} which creates a copy of the given minion on the right of that minion.
     */
    public static final TargetlessAction<Minion> COPY_SELF = (game, actor) -> {
        Player owner = actor.getOwner();
        if (owner.getBoard().isFull()) {
            return UndoAction.DO_NOTHING;
        }

        Minion copy = new Minion(actor.getOwner(), actor.getBaseDescr());

        BoardSide board = owner.getBoard();
        UndoAction copyUndo = copy.copyOther(actor);
        UndoAction summonUndo = owner.summonMinion(copy, board.indexOf(actor) + 1);
        return () -> {
            summonUndo.undo();
            copyUndo.undo();
        };
    };

    /**
     * {@link TargetlessAction} which kills the actor.
     */
    public static final TargetlessAction<Character> SELF_DESTRUCT = (game, actor) -> {
        return actor.kill();
    };

    /**
     * {@link TargetlessAction} which removes all of the overloaded mana crytals of the actor's owner.
     * <p>
     * See spell <em>Lava Shock</em>.
     */
    public static final TargetlessAction<PlayerProperty> REMOVE_OVERLOAD = (game, actor) -> {
        ManaResource mana = actor.getOwner().getManaResource();
        UndoAction thisTurnUndo = mana.setOverloadedMana(0);
        UndoAction nextTurnUndo = mana.setNextTurnOverload(0);
        return () -> {
            nextTurnUndo.undo();
            thisTurnUndo.undo();
        };
    };

    /**
     * {@link TargetlessAction} which makes the actor's owner draw a card for each damaged friendly character.
     * <p>
     * See spell <em>Battle Rage</em>.
     */
    public static final TargetlessAction<PlayerProperty> BATTLE_RAGE = (game, actor) -> {
        Player player = actor.getOwner();
        int cardsToDraw = player.getHero().isDamaged() ? 1 : 0;
        cardsToDraw += player.getBoard().countMinions(Minion::isDamaged);

        UndoAction.Builder builder = new UndoAction.Builder(cardsToDraw);
        for (int i = 0; i < cardsToDraw; i++) {
            builder.addUndo(player.drawCardToHand());
        }
        return builder;
    };

    /**
     * {@link TargetlessAction} which adds a copy of a random card from the opponent's hand to the actor's owner's hand.
     * <p>
     * See spell <em>Mind Vision</em>.
     */
    public static final TargetlessAction<PlayerProperty> MIND_VISION = (game, actor) -> {
        Player player = actor.getOwner();
        Card card = player.getOpponent().getHand().getRandomCard();
        if (card == null) {
            return UndoAction.DO_NOTHING;
        }
        return player.getHand().addCard(card.getCardDescr());
    };

    /**
     * {@link TargetlessAction} which gives the actor's owner {@code 50%} chance to draw a card.
     * <p>
     * See minion <em>Nat Pagle</em>.
     */
    public static final TargetlessAction<PlayerProperty> FISH_CARD_FOR_SELF = (game, actor) -> {
        Player player = actor.getOwner();
        if (game.getRandomProvider().roll(2) < 1) {
            return player.drawCardToHand();
        }
        else {
            return UndoAction.DO_NOTHING;
        }
    };

    /**
     * {@link TargetlessAction} which summons a random friendly minion that died this game for the actor's owner.
     * <p>
     * See spell <em>Resurrect</em>.
     */
    public static final TargetlessAction<PlayerProperty> SUMMON_DEAD_MINION = (game, actor) -> {
        Player player = actor.getOwner();
        List<Minion> deadMinions = player.getGraveyard().getDeadMinions();
        if (deadMinions.isEmpty()) {
            return UndoAction.DO_NOTHING;
        }

        RandomProvider rng = game.getRandomProvider();
        Minion minion = deadMinions.get(rng.roll(deadMinions.size()));
        return player.summonMinion(minion.getBaseDescr());
    };

    /**
     * {@link TargetlessAction} which detroys the weapon of the actor's owner and deals its damage to all
     * enemies.
     * <p>
     * See spell <em>Blade Flurry</em>.
     */
    // TODO The action now deals damage equal to the hero's attack, instead of the weapon's attack. See if
    // TODO it's accurate when the hero is buffed with something like `Rockbiter Weapon` or `Shapeshift`.
    public static final TargetlessAction<DamageSource> BLADE_FLURRY = (Game game, DamageSource actor) -> {
        Player player = actor.getOwner();
        if (player.tryGetWeapon() == null) {
            return UndoAction.DO_NOTHING;
        }

        int damage = player.getHero().getAttackTool().getAttack();

        UndoAction destroyUndo = player.destroyWeapon();

        EntitySelector<DamageSource, Character> targets = EntitySelectors.enemyTargets();
        TargetlessAction<DamageSource> damageAction = damageTarget(targets, damage);

        UndoAction damageUndo = damageAction.alterGame(game, actor);
        return () -> {
            damageUndo.undo();
            destroyUndo.undo();
        };
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
    public static final TargetlessAction<Minion> CONSUME_NEIGHBOURS = (Game game, Minion actor) -> {
        int attackBuff = 0;
        int hpBuff = 0;

        UndoAction.Builder builder = new UndoAction.Builder(4);

        BoardSide board = actor.getOwner().getBoard();
        int actorLoc = board.indexOf(actor);

        Minion left = board.getMinion(actorLoc - 1);
        if (left != null) {
            attackBuff += left.getAttackTool().getAttack();
            hpBuff += left.getBody().getCurrentHp();
            builder.addUndo(TargetedActions.KILL_TARGET.alterGame(game, actor, left));
        }

        Minion right = board.getMinion(actorLoc + 1);
        if (right != null) {
            attackBuff += right.getAttackTool().getAttack();
            hpBuff += right.getBody().getCurrentHp();
            builder.addUndo(TargetedActions.KILL_TARGET.alterGame(game, actor, right));
        }

        if (hpBuff != 0 && attackBuff != 0) {
            builder.addUndo(actor.getBuffableAttack().addBuff(attackBuff));
            builder.addUndo(actor.getBody().getHp().buffHp(hpBuff));
        }

        return builder;
    };

    /**
     * {@link TargetlessAction} which destroys the opponent's weapon and draw cards for the actor's owner
     * equal to its Durability.
     * <p>
     * See minion <em>Harrison Jones</em>.
     */
    public static final TargetlessAction<PlayerProperty> DRAW_CARD_FOR_OPPONENTS_WEAPON = (game, actor) -> {
        Player player = actor.getOwner();
        Player opponent = player.getOpponent();
        Weapon weapon = opponent.tryGetWeapon();
        if (weapon == null) {
            return UndoAction.DO_NOTHING;
        }

        int durability = weapon.getDurability();
        UndoAction.Builder builder = new UndoAction.Builder(durability + 1);
        builder.addUndo(DESTROY_OPPONENTS_WEAPON.alterGame(game, player));

        for (int i = 0; i < durability; i++) {
            builder.addUndo(TargetlessActions.DRAW_FOR_SELF.alterGame(game, player));
        }
        return builder;
    };

    /**
     * {@link TargetlessAction} which steals a random secret from the opponent.
     * <p>
     * See minion <em>Kezan Mystic</em>.
     */
    public static final TargetlessAction<PlayerProperty> STEAL_SECRET = (Game game, PlayerProperty actor) -> {
        Player player = actor.getOwner();
        Player opponent = player.getOpponent();
        List<Secret> opponentSecrets = opponent.getSecrets().getSecrets();
        if (opponentSecrets.isEmpty()) {
            return UndoAction.DO_NOTHING;
        }

        Map<EntityId, Secret> stealCandidates = new HashMap<>();
        opponentSecrets.forEach((secret) -> stealCandidates.put(secret.getSecretId(), secret));

        List<Secret> ourSecrets = player.getSecrets().getSecrets();
        ourSecrets.forEach((secret) -> stealCandidates.remove(secret.getSecretId()));

        if (stealCandidates.isEmpty()) {
            Secret selected = ActionUtils.pickRandom(game, opponentSecrets);
            if (selected == null) {
                return UndoAction.DO_NOTHING;
            }

            return opponent.getSecrets().removeSecret(selected);
        }
        else {
            Secret selected = ActionUtils.pickRandom(game, new ArrayList<>(stealCandidates.values()));
            if (selected == null) {
                return UndoAction.DO_NOTHING;
            }

            return player.getSecrets().stealActivatedSecret(opponent.getSecrets(), selected);
        }
    };

    /**
     * {@link TargetlessAction} which steals a random living minion from the opponent.
     * <p>
     * See minion <em>Sylvanas Windrunner</em>.
     */
    public static final TargetlessAction<PlayerProperty> STEAL_RANDOM_MINION = (game, actor) -> {
        Player player = actor.getOwner();
        Player opponent = player.getOpponent();
        List<Minion> minions = opponent.getBoard().getAliveMinions();
        if (minions.isEmpty()) {
            return UndoAction.DO_NOTHING;
        }

        Minion stolenMinion = minions.get(game.getRandomProvider().roll(minions.size()));
        return player.getBoard().takeOwnership(stolenMinion);
    };

    /**
     * {@link TargetlessAction} which randomly selects a minion and destroys all other minions. Minions with
     * {@link Keywords#BRAWLER} will always be considered first.
     * <p>
     * See spell <em>Brawl</em> and minion <em>Dark Iron Bouncer</em>.
     */
    public static final TargetlessAction<Object> BRAWL = (game, actor) -> {
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

        UndoAction.Builder builder = new UndoAction.Builder();
        for (Minion minion: minions) {
            if (minion != winner) {
                builder.addUndo(minion.kill());
            }
        }
        return builder;
    };

    /**
     * {@link TargetlessAction} which summons a copy of the actor minion for the opponent.
     * <p>
     * See secret <em>Mirror Entity</em>.
     */
    public static final TargetlessAction<Minion> SUMMON_COPY_FOR_OPPONENT = (Game game, Minion minion) -> {
        Player receiver = minion.getOwner().getOpponent();
        Minion newMinion = new Minion(receiver, minion.getBaseDescr());
        newMinion.copyOther(minion);

        return receiver.summonMinion(newMinion);
    };

    /**
     * {@link TargetlessAction} which makes the actor's owner draw cards until he/she has as many in hand as his/her
     * opponent.
     * <p>
     * See spell <em>Divine Favor</em>.
     */
    // TODO Check what would happen if there is not enough cards left in the deck.
    public static final TargetlessAction<PlayerProperty> DIVINE_FAVOR = (game, actor) -> {
        Player player = actor.getOwner();

        int playerHand = player.getHand().getCardCount();
        int opponentHand = player.getOpponent().getHand().getCardCount();
        if (playerHand >= opponentHand) {
            return UndoAction.DO_NOTHING;
        }

        int drawCount = opponentHand - playerHand;
        UndoAction.Builder result = new UndoAction.Builder(drawCount);
        for (int i = 0; i < drawCount; i++) {
            result.addUndo(player.drawCardToHand());
        }
        return result;
    };

    /**
     * {@link TargetlessAction} which puts a copy of each friendly minion to the hand of the actor's owner.
     * <p>
     * See spell <em>Echo of Medivh</em>.
     */
    public static final TargetlessAction<PlayerProperty> ECHO_MINIONS = (game, actor) -> {
        Player player = actor.getOwner();
        BoardSide board = player.getBoard();
        List<Minion> minions = new ArrayList<>(board.getMaxSize());
        player.getBoard().collectMinions(minions);
        BornEntity.sortEntities(minions);

        UndoAction.Builder builder = new UndoAction.Builder(minions.size());
        Hand hand = player.getHand();
        for (Minion minion: minions) {
            builder.addUndo(hand.addCard(minion.getBaseDescr().getBaseCard()));
        }
        return builder;
    };

    public static TargetlessAction<Object> withMinion(
            @NamedArg("action") TargetlessAction<? super Minion> action) {
        return applyToMinionAction(action);
    }

    public static <Actor> TargetlessAction<Actor> forSelf(
            @NamedArg("action") TargetedAction<? super Actor, ? super Actor> action) {
        return forTargets(EntitySelectors.self(), action);
    }

    public static <Actor, FinalActor> TargetlessAction<Actor> forActors(
            @NamedArg("actors") EntitySelector<? super Actor, ? extends FinalActor> actors,
            @NamedArg("action") TargetlessAction<? super FinalActor> action) {
        ExceptionHelper.checkNotNullArgument(actors, "actors");
        ExceptionHelper.checkNotNullArgument(action, "action");

        return (Game game, Actor initialActor) -> {
            UndoAction.Builder result = new UndoAction.Builder();
            actors.select(game, initialActor).forEach((FinalActor actor) -> {
                result.addUndo(action.alterGame(game, actor));
            });
            return result;
        };
    }

    public static <Actor, Target extends BornEntity> TargetlessAction<Actor> forBornTargets(
            @NamedArg("selector") EntitySelector<? super Actor, ? extends Target> selector,
            @NamedArg("action") TargetedAction<? super Actor, ? super Target> action) {
        return forBornTargets(selector, action, false);
    }

    public static <Actor, Target extends BornEntity> TargetlessAction<Actor> forBornTargets(
            @NamedArg("selector") EntitySelector<? super Actor, ? extends Target> selector,
            @NamedArg("action") TargetedAction<? super Actor, ? super Target> action,
            @NamedArg("atomic") boolean atomic) {
        return forTargets(EntitySelectors.sorted(selector, BornEntity.CMP), action, atomic);
    }

    public static <Actor, Target> TargetlessAction<Actor> forOtherTargets(
            @NamedArg("selector") EntitySelector<? super Actor, ? extends Target> selector,
            @NamedArg("action") TargetedAction<? super Actor, ? super Target> action) {
        return forOtherTargets(selector, action, false);
    }

    public static <Actor, Target> TargetlessAction<Actor> forOtherTargets(
            @NamedArg("selector") EntitySelector<? super Actor, ? extends Target> selector,
            @NamedArg("action") TargetedAction<? super Actor, ? super Target> action,
            @NamedArg("atomic") boolean atomic) {
        return forTargets(EntitySelectors.notSelf(selector), action, atomic);
    }

    public static <Actor, Target> TargetlessAction<Actor> forTargets(
            @NamedArg("selector") EntitySelector<? super Actor, ? extends Target> selector,
            @NamedArg("action") TargetedAction<? super Actor, ? super Target> action) {
        return forTargets(selector, action, false);
    }

    public static <Actor, Target> TargetlessAction<Actor> forTargets(
            @NamedArg("selector") EntitySelector<? super Actor, ? extends Target> selector,
            @NamedArg("action") TargetedAction<? super Actor, ? super Target> action,
            @NamedArg("atomic") boolean atomic) {
        ExceptionHelper.checkNotNullArgument(selector, "targets");
        ExceptionHelper.checkNotNullArgument(action, "action");

        TargetlessAction<Actor> resultAction = (Game game, Actor actor) -> {
            UndoAction.Builder result = new UndoAction.Builder();
            selector.select(game, actor).forEach((Target target) -> {
                result.addUndo(action.alterGame(game, actor, target));
            });
            return result;
        };

        if (atomic) {
            return (Game game, Actor actor) -> {
                return game.getEvents().doAtomic(() -> resultAction.alterGame(game, actor));
            };
        }
        else {
            return resultAction;
        }
    }

    public static <Actor> TargetlessAction<Actor> doIf(
            @NamedArg("condition") Predicate<? super Actor> condition,
            @NamedArg("if") TargetlessAction<? super Actor> ifAction) {
        return doIf(condition, ifAction, TargetlessAction.DO_NOTHING);
    }

    public static <Actor> TargetlessAction<Actor> doIf(
            @NamedArg("condition") Predicate<? super Actor> condition,
            @NamedArg("if") TargetlessAction<? super Actor> ifAction,
            @NamedArg("else") TargetlessAction<? super Actor> elseAction) {
        ExceptionHelper.checkNotNullArgument(condition, "condition");
        ExceptionHelper.checkNotNullArgument(ifAction, "ifAction");
        ExceptionHelper.checkNotNullArgument(elseAction, "elseAction");

        return (Game game, Actor actor) -> {
            return condition.test(actor)
                    ? ifAction.alterGame(game, actor)
                    : elseAction.alterGame(game, actor);
        };
    }

    public static <Actor extends PlayerProperty> TargetlessAction<Actor> actWithOpponent(
            @NamedArg("action") TargetlessAction<? super Player> action) {
        ExceptionHelper.checkNotNullArgument(action, "action");
        return (Game game, Actor actor) -> {
            return action.alterGame(game, actor.getOwner().getOpponent());
        };
    }

    public static <Actor> TargetlessAction<Actor> doMultipleTimes(
            @NamedArg("actionCount") int actionCount,
            @NamedArg("action") TargetlessAction<? super Actor> action) {
        ExceptionHelper.checkNotNullArgument(action, "action");
        return (Game game, Actor actor) -> {
            UndoAction.Builder result = new UndoAction.Builder(actionCount);
            for (int i = 0; i < actionCount; i++) {
                result.addUndo(action.alterGame(game, actor));
            }
            return result;
        };
    }

    public static <Actor extends PlayerProperty> TargetlessAction<Actor> equipWeapon(
            @NamedArg("weapon") WeaponProvider weapon) {
        ExceptionHelper.checkNotNullArgument(weapon, "weapon");
        return (Game game, Actor actor) -> {
            Player player = actor.getOwner();
            return player.equipWeapon(weapon.getWeapon());
        };
    }

    public static <Actor extends PlayerProperty> TargetlessAction<Actor> equipSelectedWeapon(
            @NamedArg("weapon") EntitySelector<? super Actor, ? extends WeaponDescr> weapon) {
        ExceptionHelper.checkNotNullArgument(weapon, "weapon");
        return (Game game, Actor actor) -> {
            Player player = actor.getOwner();
            // Equip the first weapon, since equiping multiple weapons make no sense.
            WeaponDescr toEquip = weapon.select(game, actor).findFirst().orElse(null);
            return toEquip != null
                    ? player.equipWeapon(toEquip)
                    : UndoAction.DO_NOTHING;
        };
    }

    public static TargetlessAction<Minion> summonMinionLeft(
            @NamedArg("minion") MinionProvider minion) {
        ExceptionHelper.checkNotNullArgument(minion, "minion");
        return (Game game, Minion actor) -> {
            Player owner = actor.getOwner();
            return owner.summonMinion(minion.getMinion(), owner.getBoard().indexOf(actor) - 1);
        };
    }

    public static TargetlessAction<Minion> summonMinionRight(
            @NamedArg("minion") MinionProvider minion) {
        ExceptionHelper.checkNotNullArgument(minion, "minion");
        return (Game game, Minion actor) -> {
            Player owner = actor.getOwner();
            return owner.summonMinion(minion.getMinion(), owner.getBoard().indexOf(actor) + 1);
        };
    }

    public static <Actor extends PlayerProperty> TargetlessAction<Actor> summonMinion(
            @NamedArg("minion") MinionProvider minion) {
        ExceptionHelper.checkNotNullArgument(minion, "minion");
        return (Game game, Actor actor) -> {
            Player player = actor.getOwner();
            return player.summonMinion(minion.getMinion());
        };
    }

    public static <Actor extends PlayerProperty> TargetlessAction<Actor> summonMinion(
            @NamedArg("minionCount") int minionCount,
            @NamedArg("minion") MinionProvider minion) {
        ExceptionHelper.checkNotNullArgument(minion, "minion");

        if (minionCount <= 0) {
            return (game, player) -> UndoAction.DO_NOTHING;
        }
        if (minionCount == 1) {
            return summonMinion(minion);
        }
        return summonMinion(minionCount, minionCount, minion);
    }

    public static <Actor extends PlayerProperty> TargetlessAction<Actor> summonMinion(
            @NamedArg("minMinionCount") int minMinionCount,
            @NamedArg("maxMinionCount") int maxMinionCount,
            @NamedArg("minion") MinionProvider minion) {
        return (game, actor) -> {
            Player player = actor.getOwner();

            MinionDescr minionDescr = minion.getMinion();

            int minionCount = game.getRandomProvider().roll(minMinionCount, maxMinionCount);

            UndoAction.Builder result = new UndoAction.Builder(minionCount);
            for (int i = 0; i < minionCount; i++) {
                result.addUndo(player.summonMinion(minionDescr));
            }
            return result;
        };
    }

    public static <Actor extends PlayerProperty> TargetlessAction<Actor> summonSelectedMinion(
            @NamedArg("minion") EntitySelector<? super Actor, ? extends MinionDescr> minion) {
        ExceptionHelper.checkNotNullArgument(minion, "minion");
        return (Game game, Actor actor) -> {
            Player player = actor.getOwner();
            return minion.forEach(game, actor, (toSummon) -> player.summonMinion(toSummon));
        };
    }

    public static TargetlessAction<Minion> summonSelectedRight(
            @NamedArg("minion") EntitySelector<? super Minion, ? extends MinionDescr> minion) {
        ExceptionHelper.checkNotNullArgument(minion, "minion");
        return (Game game, Minion actor) -> minion.forEach(game, actor,
            (toSummon) -> {
                Player owner = actor.getOwner();
                return owner.summonMinion(toSummon, owner.getBoard().indexOf(actor) + 1);
            });
    }

    public static <Actor extends DamageSource> TargetlessAction<Actor> damageTarget(
            @NamedArg("selector") EntitySelector<Actor, ? extends Character> selector,
            @NamedArg("damage") int damage) {
        return damageTarget(selector, damage, damage);
    }

    public static TargetlessAction<PlayerProperty> shuffleCardIntoDeck(@NamedArg("card") CardProvider card) {
        ExceptionHelper.checkNotNullArgument(card, "card");

        return (Game game, PlayerProperty actor) -> {
            Deck deck = actor.getOwner().getDeck();
            return deck.shuffle(game.getRandomProvider(), card.getCard());
        };
    }

    public static <Actor extends DamageSource> TargetlessAction<Actor> damageTarget(
            @NamedArg("selector") EntitySelector<Actor, ? extends Character> selector,
            @NamedArg("minDamage") int minDamage,
            @NamedArg("maxDamage") int maxDamage) {
        return forBornTargets(selector, TargetedActions.damageTarget(minDamage, maxDamage), true);
    }

    public static <Actor extends PlayerProperty> TargetlessAction<Actor> addManaCrystal(@NamedArg("amount") int amount) {
        return addManaCrystal(true, amount);
    }

    public static <Actor extends PlayerProperty> TargetlessAction<Actor> addManaCrystal(
            @NamedArg("empty") boolean empty,
            @NamedArg("amount") int amount) {
        return (Game game, Actor actor) -> {
            ManaResource manaResource = actor.getOwner().getManaResource();
            UndoAction crystalUndo = manaResource.setManaCrystals(Math.max(0, manaResource.getManaCrystals() + amount));
            if (empty) {
                return crystalUndo;
            }
            UndoAction manaUndo = manaResource.setMana(manaResource.getMana() + amount);
            return () -> {
                manaUndo.undo();
                crystalUndo.undo();
            };
        };
    }

    public static <Actor extends DamageSource> TargetlessAction<Actor> dealMissleDamage(
            @NamedArg("missleCount") int missleCount) {
        return dealMissleDamage(
            EntitySelectors.filtered(EntityFilters.random(), EntitySelectors.enemyLivingTargets()),
            missleCount);
    }

    public static <Actor extends DamageSource> TargetlessAction<Actor> dealMissleDamage(
            @NamedArg("selector") EntitySelector<Actor, ? extends Character> selector,
            @NamedArg("missleCount") int missleCount) {
        ExceptionHelper.checkNotNullArgument(selector, "selector");

        return (Game game, Actor actor) -> {
            UndoableResult<Damage> missleCountRef = actor.createDamage(missleCount);
            int appliedMissleCount = missleCountRef.getResult().getDamage();

            UndoAction.Builder result = new UndoAction.Builder(appliedMissleCount + 1);
            result.addUndo(missleCountRef.getUndoAction());

            Damage damage = new Damage(actor, 1);
            Consumer<info.hearthsim.brazier.Character> damageAction = (target) -> {
                result.addUndo(target.damage(damage));
            };
            for (int i = 0; i < appliedMissleCount; i++) {
                selector.select(game, actor).forEach(damageAction);
            }
            return result;
        };
    }

    public static <Actor extends PlayerProperty> TargetlessAction<Actor> addCard(
            @NamedArg("card") CardProvider card) {
        return addSelectedCard((Game game, Actor actor) -> Stream.of(card.getCard()));
    }

    public static <Actor extends PlayerProperty> TargetlessAction<Actor> addSelectedCard(
            @NamedArg("card") EntitySelector<? super Actor, ? extends CardDescr> card) {
        ExceptionHelper.checkNotNullArgument(card, "card");
        return addSelectedCard(0, card);
    }

    public static <Actor extends PlayerProperty> TargetlessAction<Actor> addSelectedCard(
            @NamedArg("costReduction") int costReduction,
            @NamedArg("card") EntitySelector<? super Actor, ? extends CardDescr> card) {
        ExceptionHelper.checkNotNullArgument(card, "card");

        return (Game game, Actor actor) -> {
            return addCards(game, actor, card, costReduction);
        };
    }

    private static <Actor extends PlayerProperty, CardType extends CardDescr> UndoAction addCards(
            Game game,
            Actor actor,
            EntitySelector<? super Actor, CardType> card,
            int costReduction) {
        Player player = actor.getOwner();
        Hand hand = actor.getOwner().getHand();
        if (costReduction == 0) {
            return card.forEach(game, actor, hand::addCard);
        }
        else {
            return card.forEach(game, actor, (cardDescr) -> {
                Card toAdd = new Card(player, cardDescr);
                toAdd.decreaseManaCost(costReduction);
                return hand.addCard(toAdd);
            });
        }
    }

    public static TargetlessAction<Object> buffSelfMinion(
            @NamedArg("buff") PermanentBuff<? super Minion> buff) {
        TargetlessAction<Minion> buffAction = forSelf(TargetedActions.buffTarget(buff));
        return applyToMinionAction(buffAction);
    }

    public static TargetlessAction<Object> buffSelfMinionThisTurn(
            @NamedArg("buff") Buff<? super Minion> buff) {
        TargetlessAction<Minion> buffAction = forSelf(TargetedActions.buffTargetThisTurn(buff));
        return applyToMinionAction(buffAction);
    }

    /**
     * Returns a {@link TargetlessAction} of {@code Object} which will try to convert the actor to the respective
     * {@link Minion} and executes the given {@code TargetlessAction} of {@code Minion}.
     */
    private static TargetlessAction<Object> applyToMinionAction(TargetlessAction<? super Minion> action) {
        return (Game game, Object actor) -> {
            Minion minion = ActionUtils.tryGetMinion(actor);
            return minion != null ? action.alterGame(game, minion) : UndoAction.DO_NOTHING;
        };
    }

    /**
     * Returns a {@link TargetlessAction} which increases the actor's owner's armor with the given amount.
     */
    public static TargetlessAction<PlayerProperty> armorUp(@NamedArg("armor") int armor) {
        return (game, actor) -> {
            return actor.getOwner().getHero().armorUp(armor);
        };
    }

    private static UndoAction rollRandomTotem(Game game, Player player, MinionProvider[] totems) {
        Map<MinionId, MinionDescr> allowedMinions = new HashMap<>();
        for (MinionProvider minionProvider: totems) {
            MinionDescr minion = minionProvider.getMinion();
            allowedMinions.put(minion.getId(), minion);
        }
        for (Minion minion: player.getBoard().getAliveMinions()) {
            allowedMinions.remove(minion.getBaseDescr().getId());
        }

        int allowedCount = allowedMinions.size();
        if (allowedCount == 0) {
            return UndoAction.DO_NOTHING;
        }

        int totemIndex = game.getRandomProvider().roll(allowedCount);

        Iterator<MinionDescr> minionItr = allowedMinions.values().iterator();
        MinionDescr selected = minionItr.next();
        for (int i = 0; i < totemIndex; i++) {
            selected = minionItr.next();
        }

        return player.summonMinion(selected);
    }

    public static <Actor extends PlayerProperty> TargetlessAction<Actor> summonRandomTotem(
            @NamedArg("totems") MinionProvider... totems) {
        MinionProvider[] totemsCopy = totems.clone();
        ExceptionHelper.checkNotNullElements(totemsCopy, "totems");

        return (game, actor) -> {
            return rollRandomTotem(game, actor.getOwner(), totemsCopy);
        };
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
        return (Game game, Minion actor) -> {
            AtomicInteger shieldCountRef = new AtomicInteger(0);
            Function<Minion, UndoAction> collector = (Minion minion) -> {
                MinionBody body = minion.getBody();
                if (body.isDivineShield()) {
                    shieldCountRef.incrementAndGet();
                    return body.setDivineShield(false);
                }
                else {
                    return UndoAction.DO_NOTHING;
                }
            };

            UndoAction collect1Undo = game.getPlayer1().getBoard().forAllMinions(collector);
            UndoAction collect2Undo = game.getPlayer2().getBoard().forAllMinions(collector);
            int shieldCount = shieldCountRef.get();
            if (shieldCount <= 0) {
                return UndoAction.DO_NOTHING;
            }

            UndoAction.Builder builder = new UndoAction.Builder();
            builder.addUndo(collect1Undo);
            builder.addUndo(collect2Undo);

            builder.addUndo(actor.getBuffableAttack().addBuff(attackPerShield * shieldCount));
            builder.addUndo(actor.getBody().getHp().buffHp(hpPerShield * shieldCount));

            return builder;
        };
    }

    /**
     * Returns a {@link TargetlessAction} which reduces the player's weapon's durability with
     * the given amount.
     */
    public static TargetlessAction<PlayerProperty> reduceWeaponDurability(@NamedArg("amount") int amount) {
        ExceptionHelper.checkArgumentInRange(amount, 1, Integer.MAX_VALUE, "amount");

        return (Game game, PlayerProperty actor) -> {
            Weapon weapon = actor.getOwner().tryGetWeapon();
            if (weapon == null) {
                return UndoAction.DO_NOTHING;
            }

            if (amount == 1) {
                return weapon.decreaseCharges();
            }

            UndoAction.Builder result = new UndoAction.Builder(amount);
            for (int i = 0; i < amount; i++) {
                result.addUndo(weapon.decreaseCharges());
            }
            return result;
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

        return (Game game, DamageSource actor) -> {
            UndoAction.Builder builder = new UndoAction.Builder();

            UndoableResult<Damage> damageRef = actor.createDamage(baseDamage);
            builder.addUndo(damageRef.getUndoAction());

            List<Minion> targets = new ArrayList<>();
            for (int i = 0; i < maxBounces; i++) {
                targets.clear();
                game.getPlayer1().getBoard().collectMinions(targets, minionFilter);
                game.getPlayer2().getBoard().collectMinions(targets, minionFilter);

                Minion selected = ActionUtils.pickRandom(game, targets);
                if (selected == null) {
                    break;
                }

                builder.addUndo(selected.damage(damageRef.getResult()));
                if (selected.getBody().getCurrentHp() <= 0) {
                    break;
                }
            }
            return builder;
        };
    }

    /**
     * Returns a {@link TargetlessAction} which draws a card for the actor's owner and reduce its cost with
     * the given amount.
     * <p>
     * See spell <em>Far Sight</em>.
     */
    public static TargetlessAction<PlayerProperty> drawCard(@NamedArg("costReduction") int costReduction) {
        return drawCard(costReduction, GameEventFilter.ANY);
    }

    /**
     * Returns a {@link TargetlessAction} which draws a card for the actor's owner and reduce its cost with
     * the given amount if it satisfies the given {@link GameEventFilter}.
     * <p>
     * See spell <em>Call Pet</em>.
     */
    public static TargetlessAction<PlayerProperty> drawCard(
            @NamedArg("costReduction") int costReduction,
            @NamedArg("costReductionFilter") GameEventFilter<? super Player, ? super Card> costReductionFilter) {
        ExceptionHelper.checkNotNullArgument(costReductionFilter, "costReductionFilter");
        if (costReduction == 0) {
            return TargetlessActions.DRAW_FOR_SELF;
        }

        return (Game game, PlayerProperty actor) -> {
            Player player = actor.getOwner();
            UndoableResult<Card> cardRef = player.drawFromDeck();
            Card card = cardRef.getResult();
            if (card == null) {
                return cardRef.getUndoAction();
            }

            if (costReductionFilter.applies(game, player, card)) {
                card.decreaseManaCost(costReduction);
            }
            UndoAction addUndo = player.addCardToHand(card);
            return () -> {
                addUndo.undo();
                cardRef.undo();
            };
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

        return (Game game, PlayerProperty actor) -> {
            Player player = actor.getOwner();

            UndoAction.Builder builder = new UndoAction.Builder();

            boolean mayHaveCard = true;
            for (int i = 0; i < cardCount; i++) {
                UndoableResult<Card> selectedRef = mayHaveCard
                        ? ActionUtils.pollDeckForCard(player, cardFilter)
                        : null;

                Card selected;
                if (selectedRef == null) {
                    mayHaveCard = false;
                    selected = fallbackCard != null
                            ? new Card(player, fallbackCard.getCard())
                            : null;
                }
                else {
                    builder.addUndo(selectedRef.getUndoAction());
                    selected = selectedRef.getResult();
                }

                if (selected == null) {
                    break;
                }

                builder.addUndo(player.getHand().addCard(selected));
            }

            return builder;
        };
    }

    public static TargetlessAction<Minion> mimironTransformation(@NamedArg("minion") MinionProvider minion) {
        ExceptionHelper.checkNotNullArgument(minion, "minion");

        Predicate<LabeledEntity> mechFilter = ActionUtils.includedKeywordsFilter(Keywords.RACE_MECH);

        return (Game game, Minion actor) -> {
            Player player = actor.getOwner();

            List<Minion> mechs = new ArrayList<>();
            player.getBoard().collectAliveMinions(mechs, mechFilter);

            if (mechs.size() >= 3) {
                UndoAction.Builder result = new UndoAction.Builder(mechs.size() + 2);
                for (Minion mech: mechs) {
                    result.addUndo(mech.kill());
                }
                result.addUndo(game.endPhase());
                result.addUndo(player.summonMinion(minion.getMinion()));
                return result;
            }
            else {
                return UndoAction.DO_NOTHING;
            }
        };
    }

    public static <Actor> TargetlessAction<Actor> addThisTurnAbility(
            @NamedArg("ability") Ability<? super Actor> ability) {
        ExceptionHelper.checkNotNullArgument(ability, "ability");
        return (game, actor) -> {
            return ActionUtils.doTemporary(game, () -> ability.activate(actor));
        };
    }

    /**
     * Returns a {@link TargetlessAction} which randomly summons a minion from the deck of the actor's owner,
     * and summons the provided {@code fallbackMinion} if there is no more minion left in the deck.
     * If the {@code fallbackMinion} is {@code null} and there is no more minion in the deck, nothing will be summoned.
     */
    public static TargetlessAction<PlayerProperty> summonRandomMinionFromDeck(
            @NamedArg("fallbackMinion") MinionProvider fallbackMinion) {

        Predicate<Card> appliedFilter = (card) -> card.getMinion() != null;
        return (Game game, PlayerProperty actor) -> {
            Player player = actor.getOwner();

            UndoableResult<Card> cardRef = ActionUtils.pollDeckForCard(player, appliedFilter);
            if (cardRef == null && fallbackMinion == null) {
                return UndoAction.DO_NOTHING;
            }

            MinionDescr minion = cardRef != null
                    ? cardRef.getResult().getMinion().getBaseDescr()
                    : fallbackMinion.getMinion();
            assert minion != null;

            UndoAction summonUndo = player.summonMinion(minion);
            return () -> {
                summonUndo.undo();
                if (cardRef != null) {
                    cardRef.undo();
                }
            };
        };
    }

    /**
     * Returns a {@link TargetlessAction} which summons a random minion with all of the given {@link Keyword}s
     * from the actor's hand. If no such minion exists, nothing will be summoned.
     */
    public static TargetlessAction<Minion> summonRandomMinionFromHand(
            @NamedArg("keywords") Keyword[] keywords) {
        Predicate<LabeledEntity> cardFilter = ActionUtils.includedKeywordsFilter(keywords);

        return (game, actor) -> {
            Hand hand = actor.getOwner().getHand();
            int cardIndex = hand.chooseRandomCardIndex(cardFilter);
            if (cardIndex < 0) {
                return UndoAction.DO_NOTHING;
            }

            UndoableResult<Card> removedCardRef = hand.removeAtIndex(cardIndex);
            Minion minion = removedCardRef.getResult().getMinion();
            assert minion != null;

            Player owner = actor.getOwner();
            UndoAction summonUndo = owner.summonMinion(minion, owner.getBoard().indexOf(actor) + 1);
            return () -> {
                summonUndo.undo();
                removedCardRef.undo();
            };
        };
    }

    private static Ability<Player> deactivateAfterPlay(
            Ability<Player> ability,
            AuraFilter<? super Player, ? super Card> filter) {
        return deactivateAfterCardPlay(ability, (card) -> {
            return filter.isApplicable(card.getGame(), card.getOwner(), card);
        });
    }

    private static Ability<Player> deactivateAfterCardPlay(
            Ability<Player> ability,
            Predicate<Card> deactivateCondition) {
        ExceptionHelper.checkNotNullArgument(ability, "ability");
        ExceptionHelper.checkNotNullArgument(deactivateCondition, "deactivateCondition");

        return (Player self) -> {
            UndoableUnregisterAction.Builder result = new UndoableUnregisterAction.Builder(2);

            UndoableUnregisterAction abilityRef = ability.activate(self);
            result.addRef(abilityRef);

            GameEvents events = self.getGame().getEvents();

            GameActionEvents<CardPlayEvent> listeners = events.simpleListeners(SimpleEventType.START_PLAY_CARD);
            UndoableUnregisterAction listenerRef = listeners.addAction((Game game, CardPlayEvent playEvent) -> {
                if (deactivateCondition.test(playEvent.getCard())) {
                    return abilityRef.unregister();
                }
                else {
                    return UndoAction.DO_NOTHING;
                }
            });
            result.addRef(listenerRef);

            return result;
        };
    }

    public static TargetlessAction<PlayerProperty> experiment(
            @NamedArg("replaceCard") CardProvider replaceCard) {
        ExceptionHelper.checkNotNullArgument(replaceCard, "replaceCard");

        return (Game game, PlayerProperty actor) -> {
            Player player = actor.getOwner();
            UndoableResult<Card> cardRef = player.drawFromDeck();
            Card card = cardRef.getResult();
            if (card == null) {
                return cardRef.getUndoAction();
            }

            CardDescr cardDescr = card.getCardDescr();

            if (cardDescr.getCardType() == CardType.MINION) {
                UndoAction drawActionsUndo = GameActionList.executeActionsNow(game, card, cardDescr.getOnDrawActions());
                UndoAction addCardUndo = player.getHand().addCard(replaceCard.getCard());
                UndoAction eventUndo = game.getEvents().triggerEvent(SimpleEventType.DRAW_CARD, card);

                return () -> {
                    eventUndo.undo();
                    addCardUndo.undo();
                    drawActionsUndo.undo();
                    cardRef.undo();
                };
            }
            else {
                UndoAction drawUndo = player.addCardToHand(cardRef.getResult());
                return () -> {
                    drawUndo.undo();
                    cardRef.undo();
                };
            }
        };
    }

    public static TargetlessAction<PlayerProperty> drawCardToFillHand(@NamedArg("targetHandSize") int targetHandSize) {
        return (Game game, PlayerProperty actor) -> {
            Player player = actor.getOwner();
            int currentHandSize = player.getHand().getCardCount();
            if (currentHandSize >= targetHandSize) {
                return UndoAction.DO_NOTHING;
            }

            int drawCount = targetHandSize - currentHandSize;
            UndoAction.Builder result = new UndoAction.Builder(drawCount);
            for (int i = 0; i < drawCount; i++) {
                result.addUndo(player.drawCardToHand());
            }
            return result;
        };
    }

    public static TargetlessAction<Object> killAndReplaceMinions(
            @NamedArg("minion") MinionProvider minion) {
        ExceptionHelper.checkNotNullArgument(minion, "minion");

        return (Game game, Object actor) -> {
            List<Minion> minions1 = new ArrayList<>(Player.MAX_BOARD_SIZE);
            List<Minion> minions2 = new ArrayList<>(Player.MAX_BOARD_SIZE);

            Player player1 = game.getPlayer1();
            Player player2 = game.getPlayer2();

            player1.getBoard().collectMinions(minions1);
            player2.getBoard().collectMinions(minions2);

            UndoAction.Builder result = new UndoAction.Builder();

            for (Minion killedMinion: minions1) {
                result.addUndo(killedMinion.kill());
            }
            for (Minion killedMinion: minions2) {
                result.addUndo(killedMinion.kill());
            }

            result.addUndo(game.endPhase());

            result.addUndo(TargetlessActions.summonMinion(minions1.size(), minion).alterGame(game, player1));
            result.addUndo(TargetlessActions.summonMinion(minions2.size(), minion).alterGame(game, player2));

            return result;
        };
    }

    public static TargetlessAction<Minion> replaceHero(
            @NamedArg("heroClass") Keyword heroClass,
            @NamedArg("heroPower") CardId heroPower) {

        return (Game game, Minion actor) -> {
            Player player = actor.getOwner();
            UndoAction removeUndo = player.getBoard().removeFromBoard(actor.getTargetId());

            MinionBody body = actor.getBody();

            Hero hero = new Hero(player, body.getHp(), 0, heroClass, actor.getKeywords());
            hero.setCurrentHp(body.getCurrentHp());
            hero.setHeroPower(game.getDb().getHeroPowerDb().getById(heroPower));

            UndoAction setHeroUndo = player.setHero(hero);
            return () -> {
                setHeroUndo.undo();
                removeUndo.undo();
            };
        };
    }

    public static TargetlessAction<PlayerProperty> replaceHero(
            @NamedArg("maxHp") int maxHp,
            @NamedArg("armor") int armor,
            @NamedArg("heroPower") String heroPower,
            @NamedArg("heroClass") Keyword heroClass) {
        return replaceHero(maxHp, armor, heroPower, heroClass, new Keyword[0]);
    }

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

        return (Game game, PlayerProperty actor) -> {
            Player player = actor.getOwner();
            Hero hero = new Hero(player, maxHp, armor, heroClass, keywordsCopy);
            hero.setHeroPower(game.getDb().getHeroPowerDb().getById(new CardId(heroPower)));

            return player.setHero(hero);
        };
    }

    public static TargetlessAction<PlayerProperty> replaceHeroPower(
            @NamedArg("heroPower") CardId[] heroPower) {
        ExceptionHelper.checkArgumentInRange(heroPower.length, 1, Integer.MAX_VALUE, "heroPower.length");
        CardId[] heroPowerCopy = heroPower.clone();
        ExceptionHelper.checkNotNullElements(heroPowerCopy, "heroPower");


        return (Game game, PlayerProperty actor) -> {
            Hero hero = actor.getOwner().getHero();

            CardId currentId = hero.getHeroPower().getPowerDef().getId();
            CardId newId = heroPowerCopy[0];
            for (int i = 0; i < heroPowerCopy.length; i++) {
                if (currentId.equals(heroPowerCopy[i])) {
                    int selectedIndex = i + 1;
                    newId = selectedIndex >= heroPowerCopy.length
                            ? heroPowerCopy[heroPowerCopy.length - 1]
                            : heroPowerCopy[selectedIndex];
                    break;
                }
            }

            return hero.setHeroPower(game.getDb().getHeroPowerDb().getById(newId));
        };
    }

    public static TargetlessAction<PlayerProperty> gainMana(@NamedArg("mana") int mana) {
        return (game, actor) -> {
            Player player = actor.getOwner();
            return player.setMana(player.getMana() + mana);
        };
    }

    public static TargetlessAction<PlayerProperty> setManaCostThisTurn(
            @NamedArg("manaCost") int manaCost,
            @NamedArg("filter") AuraFilter<? super Player, ? super Card> filter) {
        ExceptionHelper.checkNotNullArgument(filter, "filter");

        return (Game game, PlayerProperty actor) -> {
            Ability<Player> aura = Abilities.aura(
                AuraTargetProviders.OWN_HAND_PROVIDER,
                filter,
                Auras.setManaCost(manaCost));
            aura = deactivateAfterPlay(aura, filter);
            aura = ActionUtils.toSingleTurnAbility(game, aura);

            return aura.activate(actor.getOwner());
        };
    }

    public static TargetlessAction<PlayerProperty> reduceManaCostThisTurn(
            @NamedArg("amount") int amount,
            @NamedArg("filter") AuraFilter<? super Player, ? super Card> filter) {
        ExceptionHelper.checkNotNullArgument(filter, "filter");

        return (Game game, PlayerProperty actor) -> {
            Ability<Player> aura = Abilities.aura(
                AuraTargetProviders.OWN_HAND_PROVIDER,
                filter,
                Auras.decreaseManaCost(amount));
            aura = deactivateAfterPlay(aura, filter);
            aura = ActionUtils.toSingleTurnAbility(game, aura);

            return aura.activate(actor.getOwner());
        };
    }

    public static TargetlessAction<PlayerProperty> reduceManaCostNextCard(
            @NamedArg("amount") int amount,
            @NamedArg("filter") AuraFilter<? super Player, ? super Card> filter) {
        ExceptionHelper.checkNotNullArgument(filter, "filter");

        return (Game game, PlayerProperty actor) -> {
            Ability<Player> aura = Abilities.aura(
                AuraTargetProviders.OWN_HAND_PROVIDER,
                filter,
                Auras.decreaseManaCost(amount));
            aura = deactivateAfterPlay(aura, filter);

            return aura.activate(actor.getOwner());
        };
    }

    public static <Target> TargetlessAction<PlayerProperty> untilTurnStartsAura(
            @NamedArg("target") AuraTargetProvider<? super Player, ? extends Target> target,
            @NamedArg("aura") Aura<? super Player, ? super Target> aura) {
        return untilTurnStartsAura(target, AuraFilter.ANY, aura);
    }

    public static <Target> TargetlessAction<PlayerProperty> untilTurnStartsAura(
            @NamedArg("target") AuraTargetProvider<? super Player, ? extends Target> target,
            @NamedArg("filter") AuraFilter<? super Player, ? super Target> filter,
            @NamedArg("aura") Aura<? super Player, ? super Target> aura) {

        return (Game game, PlayerProperty actor) -> {
            Player player = actor.getOwner();
            return ActionUtils.doUntilNewTurnStart(player.getGame(), player, () -> {
                return player.getGame().addAura(new TargetedActiveAura<>(player, target, filter, aura));
            });
        };
    }

    public static <Actor extends PlayerProperty> TargetlessAction<Actor> doOnEndOfTurn(
            @NamedArg("action") TargetlessAction<? super Actor> action) {
        ExceptionHelper.checkNotNullArgument(action, "action");

        return (Game game, Actor actor) -> {
            return ActionUtils.doOnEndOfTurn(game, () -> action.alterGame(game, actor));
        };
    }

    public static <Actor extends PlayerProperty> TargetlessAction<Actor> drawAndPlayCard(@NamedArg("keywords") Keyword[] keywords) {
        Predicate<LabeledEntity> cardFilter = ActionUtils.includedKeywordsFilter(keywords);
        return drawAndPlayCard(cardFilter);
    }

    private static <Actor extends PlayerProperty> TargetlessAction<Actor> drawAndPlayCard(Predicate<? super Card> cardFilter) {
        ExceptionHelper.checkNotNullArgument(cardFilter, "cardFilter");

        return (Game game, Actor actor) -> {
            Player player = actor.getOwner();
            UndoableResult<Card> cardRef = ActionUtils.pollDeckForCard(player, cardFilter);
            if (cardRef == null) {
                return UndoAction.DO_NOTHING;
            }

            UndoAction playUndo = player.playCardEffect(cardRef.getResult());

            return () -> {
                playUndo.undo();
                cardRef.undo();
            };
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

    public static TargetlessAction<PlayerProperty> trackCard(@NamedArg("cardCount") int cardCount) {
        ExceptionHelper.checkArgumentInRange(cardCount, 1, Integer.MAX_VALUE, "cardCount");

        return (Game game, PlayerProperty actor) -> {
            Player player = actor.getOwner();
            Deck deck = player.getDeck();

            UndoAction.Builder result = new UndoAction.Builder(cardCount + 1);
            List<CardDescr> choosenCards = new ArrayList<>(cardCount);
            for (int i = 0; i < cardCount; i++) {
                UndoableResult<Card> cardRef = deck.tryDrawOneCard();
                if (cardRef == null) {
                    break;
                }

                result.addUndo(cardRef.getUndoAction());
                choosenCards.add(cardRef.getResult().getCardDescr());
            }

            CardDescr chosenCard = chooseCard(game, choosenCards);
            if (chosenCard != null) {
                result.addUndo(player.getHand().addCard(chosenCard));
            }

            return result;
        };
    }

    public static TargetlessAction<PlayerProperty> unleashMinions(
            @NamedArg("minion") MinionProvider minion) {
        ExceptionHelper.checkNotNullArgument(minion, "minion");

        return (Game game, PlayerProperty actor) -> {
            Player player = actor.getOwner();
            int minionCount = player.getOpponent().getBoard().getMinionCount();

            MinionDescr toSummon = minion.getMinion();
            UndoAction.Builder result = new UndoAction.Builder(minionCount);
            for (int i = 0; i < minionCount; i++) {
                result.addUndo(player.summonMinion(toSummon));
            }
            return result;
        };
    }

    private TargetlessActions() {
        throw new AssertionError();
    }
}
