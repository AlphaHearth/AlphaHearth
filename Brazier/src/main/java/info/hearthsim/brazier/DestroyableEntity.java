package info.hearthsim.brazier;

import info.hearthsim.brazier.minions.Minion;
import info.hearthsim.brazier.weapons.Weapon;

/**
 * Destroyable entity, including {@link Minion} and {@link Weapon}. This interface defines a
 * {@code scheduledToDestroy} field for entities, which should be set to {@code true} when its
 * {@link #scheduleToDestroy()} method is invoked, which will be invoked only when the entity
 * is dead (its health point being below {@code 0}). The entity will soon be destroyed by invoking
 * its {@link #destroy()} method.
 */
public interface DestroyableEntity extends BornEntity {
    /**
     * Schedules to destroy this entity. An entity is scheduled to destroy only when it is dead (its
     * health point hits {@code 0}). Once an entity is scheduled to destroy, it will be destroyed
     * soon after by invoking its {@link #destroy()} method.
     */
    public void scheduleToDestroy();

    /**
     * Returns if this entity has been scheduled to destroy.
     */
    public boolean isScheduledToDestroy();

    /**
     * Destroys this entity.
     */
    public void destroy();
}
