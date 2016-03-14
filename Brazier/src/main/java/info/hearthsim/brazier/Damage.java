package info.hearthsim.brazier;

import org.jtrim.utils.ExceptionHelper;

/**
 * Damage with a specific amount dealt by a {@link DamageSource}.
 */
public final class Damage {
    private final DamageSource source;
    private final int damage;

    /**
     * Creates a new {@code Damage} with the given {@link DamageSource} and amount.
     */
    public Damage(DamageSource source, int damage) {
        ExceptionHelper.checkNotNullArgument(source, "source");

        this.source = source;
        this.damage = damage;
    }

    /** Returns the {@link DamageSource} which deals this {@code Damage}. */
    public DamageSource getSource() {
        return source;
    }

    /** Returns the amount of this {@code Damage}. */
    public int getDamage() {
        return damage;
    }

    @Override
    public String toString() {
        return "Damage{" + damage + ", source=" + source + '}';
    }
}
