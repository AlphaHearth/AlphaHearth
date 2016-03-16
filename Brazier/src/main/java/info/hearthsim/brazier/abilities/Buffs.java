package info.hearthsim.brazier.abilities;

import info.hearthsim.brazier.*;
import info.hearthsim.brazier.Character;
import info.hearthsim.brazier.actions.ActionUtils;
import info.hearthsim.brazier.actions.undo.UndoableUnregisterAction;
import info.hearthsim.brazier.minions.Minion;
import info.hearthsim.brazier.parsing.NamedArg;
import info.hearthsim.brazier.Game;
import info.hearthsim.brazier.actions.undo.UndoAction;
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
    public static Buff<Character> IMMUNE = (Game game, Character target, BuffArg arg) -> {
        if (target instanceof Minion) {
            Minion minion = (Minion)target;
            return minion.getProperties().getBody().getImmuneProperty().setValueTo(arg, true);
        }
        else if (target instanceof Hero) {
            Hero hero = (Hero)target;
            return hero.getImmuneProperty().setValueTo(true);
        }
        else {
            return UndoableUnregisterAction.DO_NOTHING;
        }
    };

    /**
     * {@link Buff} that doubles the target {@link Minion}'s attack.
     */
    public static final Buff<Minion> DOUBLE_ATTACK = (game, target, arg) -> {
        return target.getProperties().getBuffableAttack().addBuff(arg, (prev) -> 2 * prev);
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
    public static final PermanentBuff<Minion> TWILIGHT_BUFF = (game, target, arg) -> {
        return buff(target, arg, 0, target.getOwner().getHand().getCardCount());
    };

    /**
     * {@link PermanentBuff} that sets the target {@link Minion}'s attack equals to its current health point.
     * <p>
     * See spell <em>Inner Fire</em>.
     */
    public static final PermanentBuff<Minion> INNER_FIRE = (game, target, arg) -> {
        int hp = target.getBody().getCurrentHp();
        return target.getProperties().getBuffableAttack().setValueTo(arg, hp);
    };

    /**
     * {@link PermanentBuff} that increases the target {@link Minion}'s attack and health point each with {@code 1}
     * (<em>+1/+1</em>) for each enemy death rattle minions.
     * <p>
     * See minion <em>Lil' Exorcist</em>.
     */
    public static final PermanentBuff<Minion> EXORCIST_BUFF = (Game game, Minion target, BuffArg arg) -> {
        BoardSide opponentBoard = target.getOwner().getOpponent().getBoard();
        int buff = opponentBoard.countMinions((opponentMinion) -> opponentMinion.getProperties().isDeathRattle());

        UndoAction attackBuffUndo = target.getBuffableAttack().addBuff(arg, buff);
        UndoAction hpBuffUndo = target.getBody().getHp().buffHp(arg, buff);
        return () -> {
            hpBuffUndo.undo();
            attackBuffUndo.undo();
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
        return (Game game, Minion target, BuffArg arg) -> {
            return target.getBuffableAttack().setValueTo(arg, attack);
        };
    }

    private static PermanentBuff<Character> adjustHp(Function<HpProperty, UndoAction> action) {
        return (Game game, Character target, BuffArg arg) -> {
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
            }
            else {
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
        return (Game game, Minion target, BuffArg arg) -> {
            int buff = game.getRandomProvider().roll(minAttack, maxAttack);
            return target.getBuffableAttack().addBuff(arg, buff);
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
        return (Game game, Character target, BuffArg arg) -> {
            return buff(target, arg, attack, hp);
        };
    }

    private static UndoAction buff(Character target, BuffArg arg, int attack, int hp) {
        if (target instanceof Minion) {
            return buffMinion((Minion)target, arg, attack, hp);
        }
        if (target instanceof Hero) {
            return buffHero((Hero)target, arg, attack, hp);
        }
        return UndoAction.DO_NOTHING;
    }

    private static UndoAction buffMinion(Minion minion, BuffArg arg, int attack, int hp) {
        if (attack == 0) {
            return minion.getBody().getHp().buffHp(arg, hp);
        }
        if (hp == 0) {
            return minion.getBuffableAttack().addBuff(arg, attack);
        }

        UndoAction attackBuffUndo = minion.getBuffableAttack().addBuff(arg, attack);
        UndoAction hpBuffUndo = minion.getBody().getHp().buffHp(hp);
        return () -> {
            hpBuffUndo.undo();
            attackBuffUndo.undo();
        };
    }

    private static UndoAction buffHero(Hero hero, BuffArg arg, int attack, int hp) {
        // FIXME: Attack buff is only OK because everything buffing a hero's
        // FIXME: attack only lasts until the end of turn.

        if (attack == 0) {
            return hero.getHp().buffHp(arg, hp);
        }
        if (hp == 0) {
            return hero.addExtraAttackForThisTurn(attack);
        }

        UndoAction attackBuffUndo = hero.addExtraAttackForThisTurn(attack);
        UndoAction hpBuffUndo = hero.getHp().buffHp(arg, hp);
        return () -> {
            hpBuffUndo.undo();
            attackBuffUndo.undo();
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

        return (Game game, Character target, BuffArg arg) -> {
            if (target instanceof Minion) {
                return ((Minion)target).getBuffableAttack().addBuff(arg, attack);
            }
            if (target instanceof Hero) {
                // FIXME: This is only OK because everything buffing a hero's
                //        attack only lasts until the end of turn.
                return ((Hero)target).addExtraAttackForThisTurn(attack);
            }
            return UndoableUnregisterAction.DO_NOTHING;
        };
    }

    /**
     * Returns a {@link Buff} which increases the target {@link Minion}'s attack with the given number for each
     * attack point of the weapon equipped by the minion's owner.
     */
    public static Buff<Minion> weaponAttackBuff(
            @NamedArg("buffPerAttack") int buffPerAttack) {

        return (Game game, Minion target, BuffArg arg) -> {
            Weapon weapon = target.getOwner().tryGetWeapon();
            if (weapon == null) {
                return UndoableUnregisterAction.DO_NOTHING;
            }

            int buff = weapon.getAttack();
            return target.getBuffableAttack().addBuff(arg, buffPerAttack * buff);
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
            return (game, target, arg) -> target.getBuffableAttack().addBuff(arg, attack);
        }

        return (game, target, arg) -> {
            arg.checkNormalBuff();
            UndoAction attackBuffUndo = target.getBuffableAttack().addBuff(arg, attack);
            UndoAction incChargesUndo = target.increaseDurability(durability);

            return () -> {
                incChargesUndo.undo();
                attackBuffUndo.undo();
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
        return (Game game, Minion minion, BuffArg arg) -> {
            int mul = minion.getOwner().getCardsPlayedThisTurn() - 1;
            if (mul <= 0) {
                return UndoAction.DO_NOTHING;
            }

            return buff(minion, arg, attack * mul, hp * mul);
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
        return (Game game, Minion target, BuffArg arg) -> {
            Predicate<LabeledEntity> appliedFilter = minionFilter.and((otherMinion) -> target != otherMinion);
            int buff = target.getOwner().getBoard().countMinions(appliedFilter);
            if (buff <= 0) {
                return UndoAction.DO_NOTHING;
            }

            UndoAction attackBuffUndo = target.getBuffableAttack().addBuff(arg, attack * buff);
            UndoAction hpBuffUndo = target.getBody().getHp().buffHp(hp * buff);

            return () -> {
                hpBuffUndo.undo();
                attackBuffUndo.undo();
            };
        };
    }

    private Buffs() {
        throw new AssertionError();
    }
}
