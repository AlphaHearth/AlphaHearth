package info.hearthsim.brazier;

import info.hearthsim.brazier.actions.undo.UndoAction;
import info.hearthsim.brazier.actions.undo.UndoableIntResult;
import info.hearthsim.brazier.weapons.AttackTool;

/**
 * A targetable character, which can be targeted by targeting action like attack, spell casting and buffing.
 */
public interface Character extends PlayerProperty, LabeledEntity, DamageSource, BornEntity {
    /** Returns the {@link TargetId} of this {@code Character}. */
    public TargetId getTargetId();

    /** Returns the {@link AttackTool} of this {@code Character}. */
    public AttackTool getAttackTool();

    /** Instantly kills the character. */
    public UndoAction kill();

    /** Deals the given {@link Damage} to this {@code Character}. */
    public UndoableIntResult damage(Damage damage);

    /** Returns if the given amount of damage is lethal to this character. */
    public boolean isLethalDamage(int damage);

    /** Returns if the character is targetable by the given {@link TargeterDef}. */
    public boolean isTargetable(TargeterDef targeterDef);

    /** Returns if the {@code Character} is dead. */
    public boolean isDead();

    /** Returns if the {@code Character} is damaged. */
    public boolean isDamaged();
}
