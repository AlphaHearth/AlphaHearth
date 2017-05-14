package info.hearthsim.brazier.abilities;

import info.hearthsim.brazier.Priorities;
import info.hearthsim.brazier.actions.ActionUtils;
import info.hearthsim.brazier.game.*;
import info.hearthsim.brazier.game.cards.Card;
import info.hearthsim.brazier.game.minions.Minion;
import info.hearthsim.brazier.game.weapons.Weapon;
import info.hearthsim.brazier.parsing.NamedArg;
import info.hearthsim.brazier.util.UndoAction;
import org.jtrim.utils.ExceptionHelper;

import java.util.function.Predicate;

/**
 * Predefined {@link Aura}s.
 */
public final class Auras {

    /* Auras for Cards */

    /**
     * Returns a {@code Aura} which increases the target {@code Card}'s mana cost with the given amount.
     */
    public static Aura<Entity, Card> increaseManaCost(@NamedArg("amount") int amount) {
        return (Entity source, Card card) -> {
            UndoAction<AuraAwareIntProperty> undoRef = card.getRawManaCost().addExternalBuff(amount);
            return (c) -> undoRef.undo(c.getRawManaCost());
        };
    }

    /**
     * Returns a {@code Aura} which decreases the target {@code Card}'s mana cost with the given amount,
     * without making the mana cost less than {@code 0}.
     */
    public static Aura<Entity, Card> decreaseManaCost(@NamedArg("amount") int amount) {
        return decreaseManaCostWithLimit(amount, 0);
    }

    /**
     * Returns a {@code Aura} which decreases the target {@code Card}'s mana cost with the given amount,
     * without making the mana cost less than the given limit.
     */
    public static Aura<Entity, Card> decreaseManaCostWithLimit(
        @NamedArg("amount") int amount,
        @NamedArg("limit") int limit) {
        BuffArg buffArg = new BuffArg(Priorities.LOWEST_PRIORITY, true);
        return (Entity source, Card card) -> {
            UndoAction<AuraAwareIntProperty> undoRef = card.getRawManaCost().addBuff(buffArg,
                (prevValue) -> Math.max(limit, prevValue - amount));
            return (c) -> undoRef.undo(c.getRawManaCost());
        };
    }

    /**
     * Returns a {@code Aura} which sets the target {@code Card}'s mana cost to the given amount.
     */
    public static Aura<Entity, Card> setManaCost(@NamedArg("manaCost") int manaCost) {
        return (Entity source, Card target) -> {
            UndoAction<AuraAwareIntProperty> undoRef = target.getRawManaCost().addExternalBuff((prevValue) -> 0);
            return (c) -> undoRef.undo(c.getRawManaCost());
        };
    }

    /* Auras for Players */

    /**
     * {@link Aura} which grants the target hero immunity.
     */
    public static final Aura<Entity, Hero> GRANT_HERO_IMMUNITY = (source, hero) -> {
        UndoAction<AuraAwareBoolProperty> undoRef = hero.getImmuneProperty().setValueTo(true);
        return (h) -> undoRef.undo(h.getImmuneProperty());
    };

    /**
     * {@link Aura} which makes the target player's minions trigger their death rattles twice when they are dead.
     */
    public static final Aura<Entity, Player> DUPLICATE_DEATH_RATTLE = (source, player) -> {
        UndoAction<AuraAwareIntProperty> undoRef = player.getDeathRattleTriggerCount().addExternalBuff((int prev) -> Math.max(prev, 2));
        return (p) -> undoRef.undo(p.getDeathRattleTriggerCount());
    };

    /**
     * {@link Aura} which makes the target player's healing to damage with same amount.
     */
    public static final Aura<Entity, Player> DAMAGING_HEAL = (source, player) -> {
        UndoAction<AuraAwareBoolProperty> undoRef = player.getDamagingHealAura().setValueTo(true);
        return (p) -> undoRef.undo(p.getDamagingHealAura());
    };

    /**
     * Returns an {@link Aura} which adds the given {@link Keyword} to the target player.
     */
    public static Aura<Entity, Player> playerFlag(@NamedArg("flag") Keyword flag) {
        ExceptionHelper.checkNotNullArgument(flag, "flag");

        return (Entity source, Player player) -> {
            UndoAction<FlagContainer> undoRef = player.getAuraFlags().registerFlag(flag);
            return (p) -> undoRef.undo(p.getAuraFlags());
        };
    }

    /**
     * Returns an {@link Aura} which increase the target {@link Weapon}'s attack with the given amount.
     */
    public static Aura<Entity, Weapon> weaponAttackBuff(@NamedArg("attack") int attack) {
        return (Entity source, Weapon weapon) -> {
            UndoAction<AuraAwareIntProperty> undoRef = weapon.getBuffableAttack().addExternalBuff(attack);
            return (w) -> undoRef.undo(w.getBuffableAttack());
        };
    }

    /* Auras for Minions */


    /**
     * {@link Aura} which makes the target {@link Minion} cannot be targeted by spell and hero power.
     */
    public static final Aura<Entity, Minion> UNTARGETABLE = (Entity source, Minion minion) -> {
        UndoAction<AuraAwareBoolProperty> undoRef =
            minion.getProperties().getBody().getUntargetableProperty().setValueToExternal(true);
        return (m) ->
            undoRef.undo(m.getProperties().getBody().getUntargetableProperty());
    };

    /**
     * {@link Aura} which grants the target {@link Minion} immunity.
     */
    public static final Aura<Entity, Minion> GRANT_MINION_IMMUNITY = (source, minion) -> {
        UndoAction<AuraAwareBoolProperty> undoRef =
            minion.getBody().getImmuneProperty().setValueToExternal(true);
        return (m) -> undoRef.undo(m.getProperties().getBody().getImmuneProperty());
    };

    /**
     * {@link Aura} which grants the target {@link Minion} charge.
     */
    public static final Aura<Entity, Minion> CHARGE = (source, target) -> {
        UndoAction<AuraAwareBoolProperty> undoRef =
            target.getProperties().getChargeProperty().setValueToExternal(true);
        return (m) -> undoRef.undo(m.getProperties().getChargeProperty());
    };

    /**
     * {@link Aura} which grants the target {@link Minion} wind fury.
     */
    public static final Aura<Entity, Minion> WIND_FURY = windFury(2);

    /**
     * Returns an {@link Aura} which increase the target {@link Minion}'s attack with the given amount
     * with each {@link Minion}s with all the given {@link Keyword}s on board.
     */
    public static Aura<Entity, Minion> attackForOtherMinionsBuff(
        @NamedArg("attack") int attack,
        @NamedArg("keywords") Keyword[] keywords) {
        Predicate<LabeledEntity> keywordFilter = ActionUtils.includedKeywordsFilter(keywords);
        return (Entity source, Minion minion) -> {
            Predicate<Minion> filter = (m) -> {
                return minion != m && keywordFilter.test(m);
            };

            int count1 = source.getGame().getPlayer1().getBoard().countMinions(filter);
            int count2 = source.getGame().getPlayer2().getBoard().countMinions(filter);

            int buff = attack * (count1 + count2);
            UndoAction<AuraAwareIntProperty> undoRef = minion.getBuffableAttack().addExternalBuff(buff);
            return (m) -> undoRef.undo(m.getBuffableAttack());
        };
    }

    /**
     * Returns an {@link Aura} which increases the target {@link Minion}'s attack with the given amount.
     */
    public static Aura<Entity, Minion> minionAttackBuff(@NamedArg("attack") int attack) {
        return (Entity source, Minion minion) -> {
            UndoAction<AuraAwareIntProperty> undoRef = minion.getBuffableAttack().addExternalBuff(attack);
            return (m) -> undoRef.undo(m.getBuffableAttack());
        };
    }

    /**
     * Returns an {@link Aura} which increases the target {@link Minion}'s hp with the given amount.
     */
    public static Aura<Entity, Minion> minionHpBuff(@NamedArg("hp") int hp) {
        return (Entity source, Minion minion) -> {
            UndoAction<HpProperty> undoRef = minion.getBody().getHp().addAuraBuff(hp);
            return (m) -> undoRef.undo(m.getBody().getHp());
        };
    }

    /**
     * Returns an {@link Aura} which increases the target {@link Minion}'s attack and hp with the given amount.
     */
    public static Aura<Entity, Minion> minionBodyBuff(
        @NamedArg("attack") int attack,
        @NamedArg("hp") int hp) {
        return (Entity source, Minion minion) -> {

            UndoAction<AuraAwareIntProperty> attackUndo = minion.getBuffableAttack().addExternalBuff(attack);
            UndoAction<HpProperty> hpUndo = minion.getBody().getHp().addAuraBuff(hp);

            return (m) -> {
                attackUndo.undo(m.getBuffableAttack());
                hpUndo.undo(m.getBody().getHp());
            };
        };
    }

    /**
     * Returns an {@link Aura} which makes the target {@link Minion}'s hp cannot be less than the given amount.
     */
    public static Aura<Entity, Minion> minionMinHp(@NamedArg("hp") int hp) {
        return (Entity source, Minion target) -> {
            UndoAction<AuraAwareIntProperty> undoRef =
                target.getBody().getMinHpProperty().addExternalBuff((prev) -> Math.max(prev, hp));
            return (m) -> undoRef.undo(m.getBody().getMinHpProperty());
        };
    }

    /**
     * Returns an {@link Aura} which makes the target {@link Minion} can attack given number of times in each turn.
     */
    public static Aura<Entity, Minion> windFury(@NamedArg("attackCount") int attackCount) {
        return (Entity source, Minion target) -> {
            UndoAction<AuraAwareIntProperty> undoRef =
                target.getProperties().getMaxAttackCountProperty().addExternalBuff((prev) -> Math.max(prev, attackCount));
            return (m) -> undoRef.undo(m.getProperties().getMaxAttackCountProperty());
        };
    }

    private Auras() {
        throw new AssertionError();
    }
}
