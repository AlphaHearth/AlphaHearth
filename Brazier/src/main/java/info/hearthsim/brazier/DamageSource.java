package info.hearthsim.brazier;

import info.hearthsim.brazier.actions.undo.UndoableResult;

/**
 * The source of damaging, meaning the implementations have ability
 * to deal damage.
 */
public interface DamageSource extends PlayerProperty {
    public UndoableResult<Damage> createDamage(int damage);
}
