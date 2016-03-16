package info.hearthsim.brazier.abilities;

import info.hearthsim.brazier.*;
import info.hearthsim.brazier.actions.ActionUtils;
import info.hearthsim.brazier.actions.undo.UndoableUnregisterAction;
import info.hearthsim.brazier.cards.Card;
import info.hearthsim.brazier.minions.Minion;
import info.hearthsim.brazier.parsing.NamedArg;
import info.hearthsim.brazier.weapons.Weapon;
import org.jtrim.utils.ExceptionHelper;

import java.util.function.Predicate;

/**
 * Predefined {@link Aura}s.
 */
public final class Auras {
    /**
     * Returns a {@link Buff} {@link Aura}, which adds the given buff to all minions affected by the aura.
     */
    public static <Source, Target> Aura<Source, Target> buffAura(
            @NamedArg("buff") Buff<Target> buff) {
        return buffAura(Priorities.HIGH_PRIORITY, buff);
    }

    /**
     * Returns a {@link Buff} {@link Aura}, which adds the given buff to all minions affected by the aura.
     */
    public static <Source, Target> Aura<Source, Target> buffAura(
            @NamedArg("priority") int priority,
            @NamedArg("buff") Buff<? super Target> buff) {
        ExceptionHelper.checkNotNullArgument(buff, "buff");
        BuffArg buffArg = BuffArg.externalBuff(priority);
        return (game, source, target) -> buff.buff(game, target, buffArg);
    }

    /* Auras for Cards */

    /**
     * Returns a {@code Aura} which increases the target {@code Card}'s mana cost with the given amount.
     */
    public static Aura<Object, Card> increaseManaCost(@NamedArg("amount") int amount) {
        return (Game game, Object source, Card target) -> {
            return target.getRawManaCost().addExternalBuff(amount);
        };
    }

    /**
     * Returns a {@code Aura} which decreases the target {@code Card}'s mana cost with the given amount,
     * without making the mana cost less than {@code 0}.
     */
    public static Aura<Object, Card> decreaseManaCost(@NamedArg("amount") int amount) {
        return decreaseManaCostWithLimit(amount, 0);
    }

    /**
     * Returns a {@code Aura} which decreases the target {@code Card}'s mana cost with the given amount,
     * without making the mana cost less than the given limit.
     */
    public static Aura<Object, Card> decreaseManaCostWithLimit(
        @NamedArg("amount") int amount,
        @NamedArg("limit") int limit) {
        BuffArg buffArg = new BuffArg(Priorities.LOWEST_PRIORITY, true);
        return (Game game, Object source, Card target) -> {
            return target.getRawManaCost().addBuff(
                buffArg,
                (prevValue) -> Math.max(limit, prevValue - amount));
        };
    }

    /**
     * Returns a {@code Aura} which sets the target {@code Card}'s mana cost to the given amount.
     */
    public static Aura<Object, Card> setManaCost(@NamedArg("manaCost") int manaCost) {
        return (Game game, Object source, Card target) -> {
            return target.getRawManaCost().addExternalBuff((prevValue) -> 0);
        };
    }

    /* Auras for Players */

    /**
     * {@link Aura} which grants the target hero immunity.
     */
    public static final Aura<Object, Hero> GRANT_HERO_IMMUNITY = (game, source, target) -> {
        return target.getImmuneProperty().setValueTo(true);
    };

    /**
     * {@link Aura} which makes the target player's minions trigger their death rattles twice when they are dead.
     */
    public static final Aura<Object, Player> DUPLICATE_DEATH_RATTLE = (game, source, target) -> {
        return target.getDeathRattleTriggerCount().addExternalBuff((int prev) -> Math.max(prev, 2));
    };

    /**
     * {@link Aura} which makes the target player's healing to damage with same amount.
     */
    public static final Aura<Object, Player> DAMAGING_HEAL = (game, source, target) -> {
        return target.getDamagingHealAura().addBuff(true);
    };

    /**
     * Returns an {@link Aura} which adds the given {@link Keyword} to the target player.
     */
    public static Aura<Object, Player> playerFlag(@NamedArg("flag") Keyword flag) {
        ExceptionHelper.checkNotNullArgument(flag, "flag");

        return (Game game, Object source, Player target) -> {
            return target.getAuraFlags().registerFlag(flag);
        };
    }

    /**
     * Returns an {@link Aura} which increase the target {@link Weapon}'s attack with the given amount.
     */
    public static Aura<Object, Weapon> weaponAttackBuff(@NamedArg("attack") int attack) {
        return (Game game, Object source, Weapon target) -> {
            return target.getBuffableAttack().addExternalBuff(attack);
        };
    }

    /* Auras for Minions */



    /**
     * {@link Aura} which makes the target {@link Minion} cannot be targeted by spell and hero power.
     */
    public static final Aura<Object, Minion> UNTARGETABLE = (Game game, Object source, Minion target) -> {
        return target.getProperties().getBody().getUntargetableProperty().setValueToExternal(true);
    };

    /**
     * {@link Aura} which grants the target {@link Minion} immunity.
     */
    public static final Aura<Object, Minion> GRANT_MINION_IMMUNITY = (game, source, target) -> {
        return target.getBody().getImmuneProperty().setValueToExternal(true);
    };

    /**
     * {@link Aura} which grants the target {@link Minion} charge.
     */
    public static final Aura<Object, Minion> CHARGE = (game, source, target) -> {
        return target.getProperties().getChargeProperty().setValueToExternal(true);
    };

    /**
     * {@link Aura} which grants the target {@link Minion} wind fury.
     */
    public static final Aura<Object, Minion> WIND_FURY = windFury(2);

    /**
     * Returns an {@link Aura} which increase the target {@link Minion}'s attack with the given amount
     * with each {@link Minion}s with all the given {@link Keyword}s on board.
     */
    public static Aura<Object, Minion> attackForOtherMinionsBuff(
        @NamedArg("attack") int attack,
        @NamedArg("keywords") Keyword[] keywords) {
        Predicate<LabeledEntity> keywordFilter = ActionUtils.includedKeywordsFilter(keywords);
        return (Game game, Object source, Minion target) -> {
            Predicate<Minion> filter = (minion) -> {
                return target != minion && keywordFilter.test(minion);
            };

            int count1 = game.getPlayer1().getBoard().countMinions(filter);
            int count2 = game.getPlayer2().getBoard().countMinions(filter);

            int buff = attack * (count1 + count2);
            return target.getBuffableAttack().addExternalBuff(buff);
        };
    }

    /**
     * Returns an {@link Aura} which increases the target {@link Minion}'s attack with the given amount.
     */
    public static Aura<Object, Minion> minionAttackBuff(@NamedArg("attack") int attack) {
        return (Game game, Object source, Minion target) -> {
            return target.getBuffableAttack().addExternalBuff(attack);
        };
    }

    /**
     * Returns an {@link Aura} which increases the target {@link Minion}'s hp with the given amount.
     */
    public static Aura<Object, Minion> minionHpBuff(@NamedArg("hp") int hp) {
        return (Game game, Object source, Minion target) -> {
            return target.getBody().getHp().addAuraBuff(hp);
        };
    }

    /**
     * Returns an {@link Aura} which increases the target {@link Minion}'s attack and hp with the given amount.
     */
    public static Aura<Object, Minion> minionBodyBuff(
        @NamedArg("attack") int attack,
        @NamedArg("hp") int hp) {
        return (Game game, Object source, Minion target) -> {
            UndoableUnregisterAction.Builder result = new UndoableUnregisterAction.Builder(2);

            result.addRef(target.getBuffableAttack().addExternalBuff(attack));
            result.addRef(target.getBody().getHp().addAuraBuff(hp));

            return result;
        };
    }

    /**
     * Returns an {@link Aura} which makes the target {@link Minion}'s hp cannot be less than the given amount.
     */
    public static Aura<Object, Minion> minionMinHp(@NamedArg("hp") int hp) {
        return (Game game, Object source, Minion target) -> {
            return target.getBody().getMinHpProperty().addExternalBuff((prev) -> Math.max(prev, hp));
        };
    }

    /**
     * Returns an {@link Aura} which makes the target {@link Minion} can attack given number of times in each turn.
     */
    public static Aura<Object, Minion> windFury(@NamedArg("attackCount") int attackCount) {
        return (Game game, Object source, Minion target) -> {
            AuraAwareIntProperty maxAttackCount = target.getProperties().getMaxAttackCountProperty();
            return maxAttackCount.addExternalBuff((prev) -> Math.max(prev, attackCount));
        };
    }

    private Auras() {
        throw new AssertionError();
    }
}
