package info.hearthsim.brazier.events;

import info.hearthsim.brazier.*;
import info.hearthsim.brazier.Character;
import info.hearthsim.brazier.actions.ActionUtils;
import info.hearthsim.brazier.actions.AttackRequest;
import info.hearthsim.brazier.actions.CardPlayRef;
import info.hearthsim.brazier.actions.CardRef;
import info.hearthsim.brazier.cards.Card;
import info.hearthsim.brazier.minions.Minion;
import info.hearthsim.brazier.parsing.NamedArg;
import info.hearthsim.brazier.weapons.Weapon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import org.jtrim.utils.ExceptionHelper;

/**
 * Predefined {@link EventFilter}s.
 */
public final class EventFilters {
    public static final EventFilter ANY
        = (owner, eventSource) -> true;

    public static final EventFilter<GameProperty, Object> SELF
        = (owner, eventSource) -> owner == eventSource;

    public static final EventFilter<GameProperty, Object> NOT_SELF
        = (owner, eventSource) -> owner != eventSource;

    public static final EventFilter<Minion, CardRef> NOT_SELF_CARD
        = (owner, eventSource) -> owner != eventSource.getCard().getMinion();

    public static final EventFilter<GameProperty, DamageEvent> DAMAGE_SOURCE_SELF
        = (owner, eventSource) -> owner == eventSource.getDamageSource();

    public static final EventFilter<GameProperty, TargetRef> TARGET_SELF
        = (owner, eventSource) -> owner == eventSource.getTarget();

    public static final EventFilter<PlayerProperty, GameProperty> SELF_TURN
        = (owner, eventSource) ->
            eventSource.getGame().getCurrentPlayer().getPlayerId() == owner.getOwner().getPlayerId();

    public static final EventFilter<PlayerProperty, GameProperty> NOT_SELF_TURN
        = (owner, eventSource) ->
            eventSource.getGame().getCurrentPlayer().getPlayerId() != owner.getOwner().getPlayerId();

    public static final EventFilter<PlayerProperty, CardPlayEvent> CARD_TARGET_IS_HERO
        = (owner, eventSource) -> eventSource.getTarget() instanceof Hero;

    public static final EventFilter<PlayerProperty, CardPlayEvent> CARD_TARGET_IS_MINION
        = (owner, eventSource) -> eventSource.getTarget() instanceof Minion;

    public static final EventFilter<PlayerProperty, AttackRequest> ATTACKER_IS_SELF
        = (owner, eventSource) -> owner == eventSource.getAttacker();

    public static final EventFilter<PlayerProperty, AttackRequest> ATTACKER_IS_HERO
        = (owner, eventSource) -> eventSource.getAttacker() instanceof Hero;

    public static final EventFilter<PlayerProperty, AttackRequest> ATTACKER_IS_MINION
        = (owner, eventSource) -> eventSource.getAttacker() instanceof Minion;

    public static final EventFilter<PlayerProperty, AttackRequest> ATTACK_TARGET_IS_MINION
        = (owner, eventSource) -> eventSource.testExistingTarget((defender) -> defender instanceof Minion);

    public static final EventFilter<PlayerProperty, AttackRequest> ATTACK_TARGET_IS_OWN_HERO
        = (owner, eventSource) ->
            owner.getOwner().getHero().getEntityId() == eventSource.getTarget().getEntityId();

    public static final EventFilter<PlayerProperty, AttackRequest> ATTACK_TARGET_IS_OWNER
        = (owner, eventSource) -> eventSource.testExistingTarget((defender) -> owner.getOwner() == defender.getOwner());

    public static final EventFilter<PlayerProperty, AttackRequest> ATTACK_TARGET_IS_ENEMY
        = (owner, eventSource) -> eventSource.testExistingTarget((defender) -> owner.getOwner() != defender.getOwner());

    public static final EventFilter<PlayerProperty, AttackRequest> ATTACKER_IS_OWNER
        = (owner, eventSource) -> owner.getOwner() == eventSource.getAttacker().getOwner();

    public static final EventFilter<PlayerProperty, AttackRequest> ATTACKER_IS_ENEMY
        = (owner, eventSource) -> owner.getOwner() != eventSource.getAttacker().getOwner();

    public static final EventFilter<PlayerProperty, AttackRequest> ATTACKER_IS_ALIVE
        = (owner, eventSource) -> !eventSource.getAttacker().isDead();

    public static final EventFilter<PlayerProperty, DamageRequest> PREPARED_DAMAGE_IS_LETHAL =
        (owner, eventSource) -> eventSource.getTarget().isLethalDamage(eventSource.getDamage().getDamage());

    public static final EventFilter<PlayerProperty, Object> HAS_SECRET =
        (owner, eventSource) -> owner.getOwner().getSecrets().hasSecret();

    public static final EventFilter<PlayerProperty, Object> SELF_BOARD_IS_NOT_FULL =
        (owner, eventSource) -> !owner.getOwner().getBoard().isFull();

    public static final EventFilter<PlayerProperty, TargetRef> DAMAGE_TARGET_IS_OWN_HERO
        = (owner, eventSource) -> owner.getOwner().getHero() == eventSource.getTarget();

    public static final EventFilter<Weapon, Object> SOURCE_WEAPON_HAS_CHARGE =
        (owner, eventSource) -> owner.getDurability() > 0;

    public static final EventFilter<PlayerProperty, Character> EVENT_SOURCE_IS_NOT_DAMAGED =
        (owner, eventSource) -> !eventSource.isDamaged();

    public static final EventFilter<GameProperty, TargetRef> TARGET_SURVIVES =
        (owner, eventSource) -> !eventSource.getTarget().isDead();

    public static final EventFilter<PlayerProperty, PlayerProperty> HAS_DIFFERENT_OWNER_PLAYER =
        (owner, eventSource) -> owner.getOwner().getPlayerId() != eventSource.getOwner().getPlayerId();

    public static final EventFilter<PlayerProperty, PlayerProperty> HAS_SAME_OWNER_PLAYER =
        (owner, eventSource) -> owner.getOwner().getPlayerId() == eventSource.getOwner().getPlayerId();

    public static final EventFilter<PlayerProperty, Minion> HAS_OTHER_OWNED_BUFF_TARGET =
        (owner, eventSource) -> {
            BoardSide board = owner.getOwner().getBoard();
            return board.findMinion((minion) -> minion.notScheduledToDestroy() && minion != eventSource) != null;
        };

    public static final EventFilter<PlayerProperty, TargetRef> TARGET_HAS_SAME_OWNER_PLAYER =
        (owner, eventSource) -> owner.getOwner() == eventSource.getTarget().getOwner();

    public static final EventFilter<PlayerProperty, Character> EVENT_SOURCE_DAMAGED =
        (owner, eventSource) -> eventSource.isDamaged();

    public static final EventFilter<PlayerProperty, AttackRequest> HAS_MISSDIRECT_TARGET =
        (PlayerProperty self, AttackRequest eventSource) ->
            hasValidTarget(self.getGame(), validMisdirectTarget(eventSource));

    public static final EventFilter<GameProperty, LabeledEntity> EVENT_SOURCE_IS_SPELL =
        eventSourceHasKeyword(Keywords.SPELL);

    public static final EventFilter<GameProperty, LabeledEntity> EVENT_SOURCE_IS_SECRET =
        eventSourceHasKeyword(Keywords.SECRET);

    public static final EventFilter<GameProperty, Minion> SUMMONED_DEATH_RATTLE =
        (owner, eventSource) -> eventSource.getProperties().isDeathRattle();

    public static final EventFilter<PlayerProperty, Object> OWNER_HAS_SECRET =
        (owner, eventSource) -> owner.getOwner().getSecrets().getSecrets().size() > 0;

    private static boolean hasValidTarget(
        Game game,
        Predicate<? super Character> filter) {
        return hasValidTarget(game.getPlayer1(), filter)
            || hasValidTarget(game.getPlayer2(), filter);
    }

    private static boolean hasValidTarget(
        Player player,
        Predicate<? super Character> filter) {
        if (filter.test(player.getHero())) {
            return true;
        }
        return player.getBoard().findMinion(filter) != null;
    }

    public static Predicate<Character> validMisdirectTarget(AttackRequest request) {
        return validMisdirectTarget(request.getAttacker(), request.getTarget());
    }

    public static Predicate<Character> validMisdirectTarget(Character attacker, Character defender) {
        return (target) -> {
            if (target == attacker || target == defender) {
                return false;
            }
            if (target instanceof Minion) {
                if (((Minion) target).getBody().isStealth()) {
                    return false;
                }
            }
            return true;
        };
    }

    public static EventFilter<GameProperty, Object> minionDiedWithKeyword(
        @NamedArg("keywords") Keyword[] keywords) {
        Keyword[] keywordsCopy = keywords.clone();
        ExceptionHelper.checkNotNullElements(keywordsCopy, "keywords");

        return (GameProperty owner, Object eventSource) -> {
            Game game = owner.getGame();
            return game.getPlayer1().getGraveyard().hasWithKeyword(keywordsCopy)
                || game.getPlayer2().getGraveyard().hasWithKeyword(keywordsCopy);
        };
    }

    public static EventFilter<PlayerProperty, Object> ownBoardSizeIsLess(
        @NamedArg("minionCount") int minionCount) {
        return (PlayerProperty owner, Object eventSource) -> {
            return owner.getOwner().getBoard().getMinionCount() < minionCount;
        };
    }

    public static EventFilter<GameProperty, Character> targetAttackIsLess(@NamedArg("attack") int attack) {
        return (GameProperty owner, Character eventSource)
            -> eventSource.getAttackTool().getAttack() < attack;
    }

    public static EventFilter<GameProperty, LabeledEntity> eventSourceHasKeyword(
        @NamedArg("keywords") Keyword... keywords) {
        List<Keyword> requiredKeywords = new ArrayList<>(Arrays.asList(keywords));
        ExceptionHelper.checkNotNullElements(requiredKeywords, "keywords");

        return (GameProperty owner, LabeledEntity eventSource) -> {
            return eventSource.getKeywords().containsAll(requiredKeywords);
        };
    }

    public static EventFilter<GameProperty, LabeledEntity> eventSourceDoesNotHaveKeyword(@NamedArg("keywords") Keyword... keywords) {
        Predicate<LabeledEntity> filter = ActionUtils.excludedKeywordsFilter(keywords);
        return (GameProperty owner, LabeledEntity eventSource) -> {
            return filter.test(eventSource);
        };
    }

    public static EventFilter<GameProperty, CardRef> cardMinionAttackEquals(@NamedArg("attack") int attack) {
        return (GameProperty owner, CardRef eventSource) -> {
            Minion minion = eventSource.getCard().getMinion();
            return minion != null && minion.getAttackTool().getAttack() == attack;
        };
    }

    public static EventFilter<GameProperty, CardPlayRef> manaCostEquals(@NamedArg("manaCost") int manaCost) {
        return (GameProperty owner, CardPlayRef eventSource)
            -> eventSource.getManaCost() == manaCost;
    }

    public static EventFilter<GameProperty, LabeledEntity> targetHasKeyword(
        @NamedArg("keywords") Keyword... keywords) {
        List<Keyword> keywordsCopy = new ArrayList<>(Arrays.asList(keywords));
        ExceptionHelper.checkNotNullElements(keywordsCopy, "keywords");

        return (GameProperty source, LabeledEntity target) -> {
            return target.getKeywords().containsAll(keywordsCopy);
        };
    }

    public static EventFilter<GameProperty, LabeledEntity> targetDoesntHaveKeyword(
        @NamedArg("keywords") Keyword... keywords) {
        Predicate<LabeledEntity> targetFilter = ActionUtils.excludedKeywordsFilter(keywords);

        return (GameProperty source, LabeledEntity target) -> {
            return targetFilter.test(target);
        };
    }

    public static EventFilter<PlayerProperty, Object> handSizeIsLess(@NamedArg("size") int size) {
        return (PlayerProperty owner, Object eventSource)
            -> owner.getOwner().getHand().getCardCount() < size;
    }

    private EventFilters() {
        throw new AssertionError();
    }
}
