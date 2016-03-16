package info.hearthsim.brazier.events;

import info.hearthsim.brazier.*;
import info.hearthsim.brazier.Character;
import info.hearthsim.brazier.actions.ActionUtils;
import info.hearthsim.brazier.actions.AttackRequest;
import info.hearthsim.brazier.actions.CardPlayRef;
import info.hearthsim.brazier.actions.CardRef;
import info.hearthsim.brazier.minions.Minion;
import info.hearthsim.brazier.parsing.NamedArg;
import info.hearthsim.brazier.weapons.Weapon;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import org.jtrim.utils.ExceptionHelper;

/**
 * Predefined {@link GameEventFilter}s.
 */
public final class GameEventFilters {
    public static final GameEventFilter<Object, Object> ANY
            = (game, owner, eventSource) -> true;
    public static final GameEventFilter<Object, Object> SELF
            = (game, owner, eventSource) -> owner == eventSource;
    public static final GameEventFilter<Object, Object> NOT_SELF
            = (game, owner, eventSource) -> owner != eventSource;
    public static final GameEventFilter<Object, DamageEvent> DAMAGE_SOURCE_SELF
            = (game, owner, eventSource) -> owner == eventSource.getDamageSource();
    public static final GameEventFilter<Object, TargetRef> TARGET_SELF
            = (game, owner, eventSource) -> owner == eventSource.getTarget();
    public static final GameEventFilter<PlayerProperty, Object> SELF_TURN
            = (game, owner, eventSource) -> owner.getOwner().getGame().getCurrentPlayer() == owner.getOwner();
    public static final GameEventFilter<PlayerProperty, Object> NOT_SELF_TURN
            = (game, owner, eventSource) -> !SELF_TURN.applies(game, owner, SELF);
    public static final GameEventFilter<PlayerProperty, CardPlayEvent> CARD_TARGET_IS_HERO
            = (game, owner, eventSource) -> eventSource.getTarget() instanceof Hero;
    public static final GameEventFilter<PlayerProperty, CardPlayEvent> CARD_TARGET_IS_MINION
            = (game, owner, eventSource) -> eventSource.getTarget() instanceof Minion;
    public static final GameEventFilter<PlayerProperty, AttackRequest> ATTACKER_IS_SELF
            = (game, owner, eventSource) -> owner == eventSource.getAttacker();
    public static final GameEventFilter<PlayerProperty, AttackRequest> ATTACKER_IS_HERO
            = (game, owner, eventSource) -> eventSource.getAttacker() instanceof Hero;
    public static final GameEventFilter<PlayerProperty, AttackRequest> ATTACKER_IS_MINION
            = (game, owner, eventSource) -> eventSource.getAttacker() instanceof Minion;
    public static final GameEventFilter<PlayerProperty, AttackRequest> ATTACK_TARGET_IS_MINION
            = (game, owner, eventSource) -> eventSource.testExistingTarget((defender) -> defender instanceof Minion);
    public static final GameEventFilter<PlayerProperty, AttackRequest> ATTACK_TARGET_IS_OWN_HERO
            = (game, owner, eventSource) -> owner.getOwner().getHero() == eventSource.getTarget();
    public static final GameEventFilter<PlayerProperty, AttackRequest> ATTACK_TARGET_IS_OWNER
            = (game, owner, eventSource) -> eventSource.testExistingTarget((defender) -> owner.getOwner() == defender.getOwner());
    public static final GameEventFilter<PlayerProperty, AttackRequest> ATTACK_TARGET_IS_ENEMY
            = (game, owner, eventSource) -> eventSource.testExistingTarget((defender) -> owner.getOwner() != defender.getOwner());
    public static final GameEventFilter<PlayerProperty, AttackRequest> ATTACKER_IS_OWNER
            = (game, owner, eventSource) -> owner.getOwner() == eventSource.getAttacker().getOwner();
    public static final GameEventFilter<PlayerProperty, AttackRequest> ATTACKER_IS_ENEMY
            = (game, owner, eventSource) -> owner.getOwner() != eventSource.getAttacker().getOwner();
    public static final GameEventFilter<PlayerProperty, AttackRequest> ATTACKER_IS_ALIVE
            = (game, owner, eventSource) -> !eventSource.getAttacker().isDead();
    public static final GameEventFilter<PlayerProperty, DamageRequest> PREPARED_DAMAGE_IS_LETHAL = (game, owner, eventSource) -> {
        return eventSource.getTarget().isLethalDamage(eventSource.getDamage().getDamage());
    };
    public static final GameEventFilter<PlayerProperty, Object> HAS_SECRET = (game, owner, eventSource) -> {
        return owner.getOwner().getSecrets().hasSecret();
    };
    public static final GameEventFilter<PlayerProperty, Object> SELF_BOARD_IS_NOT_FULL = (game, owner, eventSource) -> {
        return !owner.getOwner().getBoard().isFull();
    };
    public static final GameEventFilter<PlayerProperty, TargetRef> DAMAGE_TARGET_IS_OWN_HERO
            = (game, owner, eventSource) -> owner.getOwner().getHero() == eventSource.getTarget();

    public static final GameEventFilter<Weapon, Object> SOURCE_WEAPON_HAS_CHARGE = (game, owner, eventSource) -> {
        return owner.getDurability() > 0;
    };

    public static final GameEventFilter<PlayerProperty, Character> EVENT_SOURCE_IS_NOT_DAMAGED
            = (game, owner, eventSource) -> !eventSource.isDamaged();

    public static final GameEventFilter<Object, TargetRef> TARGET_SURVIVES = (game, owner, eventSource) -> {
        return !eventSource.getTarget().isDead();
    };

    public static final GameEventFilter<PlayerProperty, PlayerProperty> HAS_DIFFERENT_OWNER_PLAYER = (game, owner, eventSource) -> {
        return owner.getOwner() != eventSource.getOwner();
    };

    public static final GameEventFilter<PlayerProperty, PlayerProperty> HAS_SAME_OWNER_PLAYER = (game, owner, eventSource) -> {
        return owner.getOwner() == eventSource.getOwner();
    };

    public static final GameEventFilter<PlayerProperty, Minion> HAS_OTHER_OWNED_BUFF_TARGET = (game, owner, eventSource) -> {
        BoardSide board = owner.getOwner().getBoard();
        return board.findMinion((minion) -> minion.notScheduledToDestroy() && minion != eventSource) != null;
    };

    public static final GameEventFilter<PlayerProperty, TargetRef> TARGET_HAS_SAME_OWNER_PLAYER = (game, owner, eventSource) -> {
        return owner.getOwner() == eventSource.getTarget().getOwner();
    };

    public static final GameEventFilter<PlayerProperty, Character> EVENT_SOURCE_DAMAGED = (game, owner, eventSource) -> {
        return eventSource.isDamaged();
    };

     public static final GameEventFilter<PlayerProperty, AttackRequest> HAS_MISSDIRECT_TARGET = (game, self, eventSource) -> {
         return hasValidTarget(game, validMisdirectTarget(eventSource));
     };

    public static final GameEventFilter<Object, LabeledEntity> EVENT_SOURCE_IS_SPELL = eventSourceHasKeyword(Keywords.SPELL);
    public static final GameEventFilter<Object, LabeledEntity> EVENT_SOURCE_IS_SECRET = eventSourceHasKeyword(Keywords.SECRET);

    public static final GameEventFilter<Object, Minion> SUMMONED_DEATH_RATTLE = (game, owner, eventSource) -> {
        return eventSource.getProperties().isDeathRattle();
    };

    public static final GameEventFilter<PlayerProperty, Object> OWNER_HAS_SECRET = (game, owner, eventSource) -> {
        return owner.getOwner().getSecrets().getSecrets().size() > 0;
    };

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
                 if (((Minion)target).getBody().isStealth()) {
                     return false;
                 }
             }
             return true;
        };
    }

    public static GameEventFilter<Object, Object> minionDiedWithKeyword(
            @NamedArg("keywords") Keyword[] keywords) {
        Keyword[] keywordsCopy = keywords.clone();
        ExceptionHelper.checkNotNullElements(keywordsCopy, "keywords");

        return (Game game, Object owner, Object eventSource) -> {
            return game.getPlayer1().getGraveyard().hasWithKeyword(keywordsCopy)
                    || game.getPlayer2().getGraveyard().hasWithKeyword(keywordsCopy);
        };
    }

    public static GameEventFilter<PlayerProperty, Object> ownBoardSizeIsLess(@NamedArg("minionCount") int minionCount) {
        return (Game game, PlayerProperty owner, Object eventSource) -> {
            return owner.getOwner().getBoard().getMinionCount() < minionCount;
        };
    }

    public static GameEventFilter<Object, Character> targetAttackIsLess(@NamedArg("attack") int attack) {
        return (Game game, Object owner, Character eventSource)
                -> eventSource.getAttackTool().getAttack()< attack;
    }

    public static GameEventFilter<Object, LabeledEntity> eventSourceHasKeyword(@NamedArg("keywords") Keyword... keywords) {
        List<Keyword> requiredKeywords = new ArrayList<>(Arrays.asList(keywords));
        ExceptionHelper.checkNotNullElements(requiredKeywords, "keywords");

        return (Game game, Object owner, LabeledEntity eventSource) -> {
            return eventSource.getKeywords().containsAll(requiredKeywords);
        };
    }

    public static GameEventFilter<Object, LabeledEntity> eventSourceDoesNotHaveKeyword(@NamedArg("keywords") Keyword... keywords) {
        Predicate<LabeledEntity> filter = ActionUtils.excludedKeywordsFilter(keywords);
        return (Game game, Object owner, LabeledEntity eventSource) -> {
            return filter.test(eventSource);
        };
    }

    public static GameEventFilter<Object, CardRef> cardMinionAttackEquals(@NamedArg("attack") int attack) {
        return (Game game, Object owner, CardRef eventSource) -> {
            Minion minion = eventSource.getCard().getMinion();
            if (minion == null) {
                return false;
            }
            return minion.getAttackTool().getAttack() == attack;
        };
    }

    public static GameEventFilter<Object, CardPlayRef> manaCostEquals(@NamedArg("manaCost") int manaCost) {
        return (Game game, Object owner, CardPlayRef eventSource)
                -> eventSource.getManaCost() == manaCost;
    }

    public static GameEventFilter<Object, LabeledEntity> targetHasKeyword(@NamedArg("keywords") Keyword... keywords) {
        List<Keyword> keywordsCopy = new ArrayList<>(Arrays.asList(keywords));
        ExceptionHelper.checkNotNullElements(keywordsCopy, "keywords");

        return (Game game, Object source, LabeledEntity target) -> {
            return target.getKeywords().containsAll(keywordsCopy);
        };
    }

    public static GameEventFilter<Object, LabeledEntity> targetDoesntHaveKeyword(@NamedArg("keywords") Keyword... keywords) {
        Predicate<LabeledEntity> targetFilter = ActionUtils.excludedKeywordsFilter(keywords);

        return (Game game, Object source, LabeledEntity target) -> {
            return targetFilter.test(target);
        };
    }

    public static GameEventFilter<PlayerProperty, Object> handSizeIsLess(@NamedArg("size") int size) {
        return (Game game, PlayerProperty owner, Object eventSource)
                -> owner.getOwner().getHand().getCardCount() < size;
    }

    private GameEventFilters() {
        throw new AssertionError();
    }
}
