package info.hearthsim.brazier.abilities;

import info.hearthsim.brazier.*;
import info.hearthsim.brazier.Character;
import info.hearthsim.brazier.actions.ActionUtils;
import info.hearthsim.brazier.actions.undo.UndoObjectAction;
import info.hearthsim.brazier.minions.Minion;
import info.hearthsim.brazier.parsing.NamedArg;
import info.hearthsim.brazier.weapons.Weapon;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Predefined {@link Buff}s.
 */
public final class Buffs {
    /**
     * {@link Buff} that makes the given {@link Character} immune.
     */
    public static Buff<Character> IMMUNE = (Character target, BuffArg arg) -> {
        if (target instanceof Minion) {
            Minion minion = (Minion) target;
            UndoObjectAction<AuraAwareBoolProperty> undoRef =
                minion.getProperties().getBody().getImmuneProperty().setValueTo(arg, true);
            return (c) -> undoRef.undo(((Minion) c).getProperties().getBody().getImmuneProperty());
        } else if (target instanceof Hero) {
            Hero hero = (Hero) target;
            UndoObjectAction<AuraAwareBoolProperty> undoRef = hero.getImmuneProperty().setValueTo(true);
            return (c) -> undoRef.undo(((Hero) c).getImmuneProperty());
        } else {
            return UndoObjectAction.DO_NOTHING;
        }
    };

    /**
     * {@link Buff} that doubles the target {@link Minion}'s attack.
     */
    public static final Buff<Minion> DOUBLE_ATTACK = (minion, arg) -> {
        UndoObjectAction<AuraAwareIntProperty> undoRef =
            minion.getProperties().getBuffableAttack().addBuff(arg, (prev) -> 2 * prev);
        return (m) -> undoRef.undo(m.getProperties().getBuffableAttack());
    };

    /**
     * {@link Buff} that increases the target {@link Minion}'s attack with the amount
     * equals to the attack of the equipped weapon of the {@code Minion}'s owner.
     * <p>
     * See minion <em>Bloodsail Raider</em>.
     */
    public static final Buff<Minion> WEAPON_ATTACK_BUFF = weaponAttackBuff(1);

    /**
     * {@link PermanentBuff} that increases the target {@link Minion}'s hp with the amount
     * equals to the number of cards in the hands of the {@code Minion}'s owner.
     * <p>
     * See minion <em>Twilight Drake</em>.
     */
    public static final PermanentBuff<Minion> TWILIGHT_BUFF = (minion, arg) ->
        buff(minion, arg, 0, minion.getOwner().getHand().getCardCount());

    /**
     * {@link PermanentBuff} that sets the target {@link Minion}'s attack equals to its current health point.
     * <p>
     * See spell <em>Inner Fire</em>.
     */
    public static final PermanentBuff<Minion> INNER_FIRE = (target, arg) -> {
        int hp = target.getBody().getCurrentHp();
        return UndoObjectAction.of(target, (m) -> m.getProperties().getBuffableAttack(),
            (ba) -> ba.setValueTo(arg, hp));
    };

    /**
     * {@link PermanentBuff} that increases the target {@link Minion}'s attack and health point each with {@code 1}
     * (<em>+1/+1</em>) for each enemy death rattle minions.
     * <p>
     * See minion <em>Lil' Exorcist</em>.
     */
    public static final PermanentBuff<Minion> EXORCIST_BUFF = (Minion target, BuffArg arg) -> {
        BoardSide opponentBoard = target.getOwner().getOpponent().getBoard();
        int buff = opponentBoard.countMinions((opponentMinion) -> opponentMinion.getProperties().isDeathRattle());

        UndoObjectAction<AuraAwareIntProperty> attackBuffUndo = target.getBuffableAttack().addBuff(arg, buff);
        UndoObjectAction<HpProperty> hpBuffUndo = target.getBody().getHp().buffHp(arg, buff);
        return (m) -> {
            attackBuffUndo.undo(m.getBuffableAttack());
            hpBuffUndo.undo(m.getBody().getHp());
        };
    };

    /**
     * {@link PermanentBuff} that gains {@code +1/+1} for each friendly minion.
     * <p>
     * See minion <em>Lil' Exorcist</em>.
     */
    public static final PermanentBuff<Minion> WARLORD_BUFF = minionLeaderBuff(1, 1);

    /**
     * {@link PermanentBuff} that sets the target {@link Minion}'s attack to the given value.
     */
    public static PermanentBuff<Minion> setAttack(@NamedArg("attack") int attack) {
        return (Minion target, BuffArg arg) ->
            UndoObjectAction.of(target, Minion::getBuffableAttack, (ba) -> ba.setValueTo(arg, attack));
    }

    private static PermanentBuff<Character> adjustHp(Function<HpProperty, UndoObjectAction<HpProperty>> action) {
        return (Character target, BuffArg arg) -> {
            arg.checkNormalBuff();
            return ActionUtils.adjustHp(target, action);
        };
    }

    /**
     * Returns a {@link PermanentBuff} that sets the current health point of the target to the given value.
     * <p>
     * See minion <em>Alexstrasza</em>.
     */
    public static PermanentBuff<Character> setCurrentHp(@NamedArg("hp") int hp) {
        return adjustHp((hpProperty) -> {
            if (hpProperty.getMaxHp() >= hp) {
                return hpProperty.setCurrentHp(hp);
            } else {
                return hpProperty.setMaxAndCurrentHp(hp);
            }
        });
    }

    /**
     * Returns a {@link PermanentBuff} that sets the max health point of the target to the given value.
     * <p>
     * See spell <em>Hunter's Mark</em> and secret <em>Repentance</em>.
     */
    public static PermanentBuff<Character> setMaxHp(@NamedArg("hp") int hp) {
        return adjustHp((hpProperty) -> {
            return hpProperty.setMaxHp(hp);
        });
    }

    /**
     * Returns a {@link PermanentBuff} which increases the target {@link Minion}'s attack with a random
     * amount between the given minimum and maximum value.
     */
    public static PermanentBuff<Minion> buffAttack(
        @NamedArg("minAttack") int minAttack,
        @NamedArg("maxAttack") int maxAttack) {
        return (Minion target, BuffArg arg) -> {
            int buff = target.getGame().getRandomProvider().roll(minAttack, maxAttack);
            return UndoObjectAction.of(target, Minion::getBuffableAttack, (ba) -> ba.addBuff(arg, buff));
        };
    }

    /**
     * Returns a {@link PermanentBuff} which increases the target {@link Minion}'s health point with the given amount.
     */
    public static PermanentBuff<Character> buffHp(@NamedArg("hp") int hp) {
        return buff(0, hp);
    }

    /**
     * Returns a {@link PermanentBuff} which increases the target {@link Minion}'s attack with the given amount.
     */
    public static PermanentBuff<Character> buffAttack(@NamedArg("attack") int attack) {
        return buff(attack, 0);
    }

    /**
     * Returns a {@link PermanentBuff} which increases the target {@link Minion}'s attack and hp with the given amounts.
     */
    public static PermanentBuff<Character> buff(
        @NamedArg("attack") int attack,
        @NamedArg("hp") int hp) {
        return (Character target, BuffArg arg) -> buff(target, arg, attack, hp);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Character> UndoObjectAction<T> buff(T target, BuffArg arg, int attack, int hp) {
        if (target instanceof Minion) {
            return (UndoObjectAction) buffMinion((Minion) target, arg, attack, hp);
        }
        if (target instanceof Hero) {
            return (UndoObjectAction) buffHero((Hero) target, arg, attack, hp);
        }
        return UndoObjectAction.DO_NOTHING;
    }

    private static UndoObjectAction<Minion> buffMinion(Minion minion, BuffArg arg, int attack, int hp) {
        if (attack == 0) {
            return UndoObjectAction.of(minion, (m) -> m.getBody().getHp(), (hPro) -> hPro.buffHp(arg, hp));
        }
        if (hp == 0) {
            return UndoObjectAction.of(minion, Minion::getBuffableAttack, (ba) -> ba.addBuff(arg, attack));
        }

        UndoObjectAction<AuraAwareIntProperty> attackUndo = minion.getBuffableAttack().addBuff(arg, attack);
        UndoObjectAction<HpProperty> hpUndo = minion.getBody().getHp().buffHp(hp);
        return (m) -> {
            attackUndo.undo(m.getBuffableAttack());
            hpUndo.undo(m.getBody().getHp());
        };
    }

    private static UndoObjectAction<Hero> buffHero(Hero hero, BuffArg arg, int attack, int hp) {
        // FIXME: Attack buff is only OK because everything buffing a hero's
        // FIXME: attack only lasts until the end of turn.

        if (attack == 0) {
            hero.getHp().buffHp(arg, hp);
            return UndoObjectAction.of(hero, Hero::getHp, (hP) -> hP.buffHp(arg, hp));
        }
        if (hp == 0) {
            UndoObjectAction<Hero> undoRef = hero.addExtraAttackForThisTurn(attack);
            return undoRef::undo;
        }

        UndoObjectAction<Hero> attackBuffUndo = hero.addExtraAttackForThisTurn(attack);
        UndoObjectAction<HpProperty> hpBuffUndo = hero.getHp().buffHp(arg, hp);
        return (h) -> {
            hpBuffUndo.undo(h.getHp());
            attackBuffUndo.undo(h);
        };
    }

    /**
     * Returns a {@link Buff} which increases the target character's attack and health point with the given amounts
     * and disappears at the end of the turn.
     */
    public static Buff<Character> temporaryBuff(
        @NamedArg("attack") int attack,
        @NamedArg("hp") int hp) {

        if (hp != 0) {
            throw new UnsupportedOperationException("Temporary health buffs are not yet supported.");
        }

        return (Character target, BuffArg arg) -> {
            if (target instanceof Minion) {
                Minion minion = (Minion) target;
                UndoObjectAction<AuraAwareIntProperty> undoRef = minion.getBuffableAttack().addBuff(arg, attack);
                return (c) -> undoRef.undo(((Minion) c).getBuffableAttack());
            }
            if (target instanceof Hero) {
                // FIXME: This is only OK because everything buffing a hero's
                //        attack only lasts until the end of turn.
                UndoObjectAction<Hero> undoRef = ((Hero) target).addExtraAttackForThisTurn(attack);
                return (c) -> undoRef.undo((Hero) c);
            }
            return UndoObjectAction.DO_NOTHING;
        };
    }

    /**
     * Returns a {@link Buff} which increases the target {@link Minion}'s attack with the given number for each
     * attack point of the weapon equipped by the minion's owner.
     */
    public static Buff<Minion> weaponAttackBuff(
        @NamedArg("buffPerAttack") int buffPerAttack) {

        return (Minion minion, BuffArg arg) -> {
            Weapon weapon = minion.getOwner().tryGetWeapon();
            if (weapon == null) {
                return UndoObjectAction.DO_NOTHING;
            }

            int buff = weapon.getAttack();
            return UndoObjectAction.of(minion, Minion::getBuffableAttack, (m) -> m.addBuff(arg, buffPerAttack * buff));
        };
    }

    /**
     * Returns a {@link PermanentBuff} which increases the target {@link Weapon}'s attack with the given amount.
     */
    public static PermanentBuff<Weapon> buffWeapon(@NamedArg("attack") int attack) {
        return buffWeapon(attack, 0);
    }

    /**
     * Returns a {@link PermanentBuff} which increases the target {@link Weapon}'s attack and durability
     * with the given amount.
     */
    public static PermanentBuff<Weapon> buffWeapon(
        @NamedArg("attack") int attack,
        @NamedArg("durability") int durability) {
        if (durability == 0) {
            return (weapon, arg) -> UndoObjectAction.of(weapon, Weapon::getBuffableAttack,
                (ba) -> ba.addBuff(arg, attack));
        }

        return (weapon, arg) -> {
            arg.checkNormalBuff();
            UndoObjectAction<AuraAwareIntProperty> attackBuffUndo = weapon.getBuffableAttack().addBuff(arg, attack);
            UndoObjectAction<Weapon> incChargesUndo = weapon.increaseDurability(durability);

            return (w) -> {
                incChargesUndo.undo(w);
                attackBuffUndo.undo(w.getBuffableAttack());
            };
        };
    }

    /**
     * Returns a {@link PermanentBuff} which increases the target {@link Minion}'s attack and hp with the given amount
     * for each card played in this turn.
     * <p>
     * See minion <em>Edwin VanCleef</em>.
     */
    public static PermanentBuff<Minion> vancleefBuff(
        @NamedArg("attack") int attack,
        @NamedArg("hp") int hp) {
        return (Minion minion, BuffArg arg) -> {
            int mul = minion.getOwner().getCardsPlayedThisTurn() - 1;
            if (mul <= 0) {
                return UndoObjectAction.DO_NOTHING;
            }

            return (UndoObjectAction) buff(minion, arg, attack * mul, hp * mul);
        };
    }

    /**
     * Returns a {@link PermanentBuff} which increases the target {@link Minion}'s attack and hp with the given amount
     * for each other friendly minion having all the given {@link Keyword}s.
     * <p>
     * See minion <em>King of Beasts</em>.
     */
    public static PermanentBuff<Minion> minionLeaderBuff(
        @NamedArg("attack") int attack,
        @NamedArg("hp") int hp,
        @NamedArg("keywords") Keyword... keywords) {

        Predicate<LabeledEntity> minionFilter = ActionUtils.includedKeywordsFilter(keywords);
        return (Minion target, BuffArg arg) -> {
            Predicate<LabeledEntity> appliedFilter = minionFilter.and((otherMinion) -> target != otherMinion);
            int buff = target.getOwner().getBoard().countMinions(appliedFilter);
            if (buff <= 0) {
                return UndoObjectAction.DO_NOTHING;
            }

            UndoObjectAction<AuraAwareIntProperty> attackUndo = target.getBuffableAttack().addBuff(arg, attack * buff);
            UndoObjectAction<HpProperty> hpUndo = target.getBody().getHp().buffHp(hp * buff);

            return (m) -> {
                hpUndo.undo(m.getBody().getHp());
                attackUndo.undo(m.getBuffableAttack());
            };
        };
    }

    private Buffs() {
        throw new AssertionError();
    }
}
