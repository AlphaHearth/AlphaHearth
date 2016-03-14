package info.hearthsim.brazier.abilities;

import info.hearthsim.brazier.PlayerProperty;
import info.hearthsim.brazier.actions.undo.UndoableUnregisterAction;
import info.hearthsim.brazier.events.WorldEventAction;
import info.hearthsim.brazier.actions.undo.UndoAction;
import info.hearthsim.brazier.events.WorldEventActionDefs;
import org.jtrim.utils.ExceptionHelper;

// TODO understand this class.
public final class LivingEntitiesAbilities<Self extends PlayerProperty> {
    private final ActivatableAbility<? super Self> ability;
    private final WorldEventActionDefs<Self> eventActionDefs;
    private final WorldEventAction<? super Self, ? super Self> deathRattle;

    public LivingEntitiesAbilities(
            ActivatableAbility<? super Self> ability,
            WorldEventActionDefs<Self> eventActionDefs,
            WorldEventAction<? super Self, ? super Self> deathRattle) {
        ExceptionHelper.checkNotNullArgument(eventActionDefs, "eventActionDefs");

        this.ability = ability;
        this.eventActionDefs = eventActionDefs;
        this.deathRattle = deathRattle;
    }

    public static <Self extends PlayerProperty> LivingEntitiesAbilities<Self> noAbilities() {
        return new LivingEntitiesAbilities<>(
            null,
            new WorldEventActionDefs.Builder<Self>().create(),
            null);
    }

    public ActivatableAbility<? super Self> tryGetAbility() {
        return ability;
    }

    public ActivatableAbility<? super Self> getAbility() {
        return ability != null
                ? ability
                : (self) -> UndoableUnregisterAction.DO_NOTHING;
    }

    public WorldEventActionDefs<Self> getEventActionDefs() {
        return eventActionDefs;
    }

    public WorldEventAction<? super Self, ? super Self> tryGetDeathRattle() {
        return deathRattle;
    }

    public WorldEventAction<? super Self, ? super Self> getDeathRattle() {
        return deathRattle != null
                ? deathRattle
                : (world, self, eventSource) -> UndoAction.DO_NOTHING;
    }
}
