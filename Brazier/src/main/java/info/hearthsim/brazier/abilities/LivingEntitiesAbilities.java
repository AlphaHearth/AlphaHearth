package info.hearthsim.brazier.abilities;

import info.hearthsim.brazier.events.EventAction;
import info.hearthsim.brazier.events.TriggeringAbility;
import info.hearthsim.brazier.game.Entity;
import org.jtrim.utils.ExceptionHelper;

/**
 * Abilities of a living entity, including its {@link Ability}, {@code trigger} (as a {@link TriggeringAbility})
 * and death rattle effect (as a {@link EventAction}).
 */
public final class LivingEntitiesAbilities<Self extends Entity> {
    private final Ability<? super Self> ability;
    private final TriggeringAbility<Self> triggers;
    private final EventAction<? super Self, ? super Self> deathRattle;

    public LivingEntitiesAbilities(
            Ability<? super Self> ability,
            TriggeringAbility<Self> triggers,
            EventAction<? super Self, ? super Self> deathRattle) {
        ExceptionHelper.checkNotNullArgument(triggers, "triggers");

        this.ability = ability;
        this.triggers = triggers;
        this.deathRattle = deathRattle;
    }

    public static <Self extends Entity> LivingEntitiesAbilities<Self> noAbilities() {
        return new LivingEntitiesAbilities<>(
            null,
            new TriggeringAbility.Builder<Self>().create(),
            null);
    }

    public Ability<? super Self> tryGetAbility() {
        return ability;
    }

    public Ability<? super Self> getAbility() {
        if (ability == null)
            return Ability.DO_NOTHING;
        return ability;
    }

    public TriggeringAbility<Self> getTriggers() {
        return triggers;
    }

    public EventAction<? super Self, ? super Self> tryGetDeathRattle() {
        return deathRattle;
    }

    public EventAction<? super Self, ? super Self> getDeathRattle() {
        return deathRattle != null
                ? deathRattle
                : (self, eventSource) -> {};
    }
}
