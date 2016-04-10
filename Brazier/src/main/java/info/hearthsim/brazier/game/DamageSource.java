package info.hearthsim.brazier.game;

import info.hearthsim.brazier.PlayerProperty;

/**
 * The source of damaging, meaning the implementations have ability
 * to deal damage.
 */
public interface DamageSource extends PlayerProperty {
    public Damage createDamage(int damage);
}
