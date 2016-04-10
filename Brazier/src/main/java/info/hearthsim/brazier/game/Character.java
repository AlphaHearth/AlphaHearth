package info.hearthsim.brazier.game;

import info.hearthsim.brazier.PlayerProperty;
import info.hearthsim.brazier.TargeterDef;
import info.hearthsim.brazier.game.weapons.AttackTool;

/**
 * A targetable character, which can be targeted by targeting action like attack, spell casting and buffing.
 */
public interface Character<T> extends Entity<T>, PlayerProperty, LabeledEntity, DamageSource, BornEntity {

    /** Returns the {@link AttackTool} of this {@code Character}. */
    public AttackTool getAttackTool();

    /** Instantly kills the character. */
    public void kill();

    /** Deals the given {@link Damage} to this {@code Character}. */
    public int damage(Damage damage);

    /** Returns if the given amount of damage is lethal to this character. */
    public boolean isLethalDamage(int damage);

    /** Returns if the character is targetable by the given {@link TargeterDef}. */
    public boolean isTargetable(TargeterDef targeterDef);

    /** Returns if the {@code Character} is dead. */
    public boolean isDead();

    /** Returns if the {@code Character} is damaged. */
    public boolean isDamaged();
}
