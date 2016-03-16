package info.hearthsim.brazier.abilities;

import info.hearthsim.brazier.PlayerProperty;
import info.hearthsim.brazier.actions.undo.UndoableUnregisterAction;
import info.hearthsim.brazier.events.GameEventAction;
import info.hearthsim.brazier.actions.undo.UndoAction;
import info.hearthsim.brazier.events.GameEventActionDefs;
import org.jtrim.utils.ExceptionHelper;

// TODO understand this class.
public final class LivingEntitiesAbilities<Self extends PlayerProperty> {
    private final Ability<? super Self> ability;
    private final GameEventActionDefs<Self> eventActionDefs;
    private final GameEventAction<? super Self, ? super Self> deathRattle;

    public LivingEntitiesAbilities(
            Ability<? super Self> ability,
            GameEventActionDefs<Self> eventActionDefs,
            GameEventAction<? super Self, ? super Self> deathRattle) {
        ExceptionHelper.checkNotNullArgument(eventActionDefs, "eventActionDefs");

        this.ability = ability;
        this.eventActionDefs = eventActionDefs;
        this.deathRattle = deathRattle;
    }

    public static <Self extends PlayerProperty> LivingEntitiesAbilities<Self> noAbilities() {
        return new LivingEntitiesAbilities<>(
            null,
            new GameEventActionDefs.Builder<Self>().create(),
            null);
    }

    public Ability<? super Self> tryGetAbility() {
        return ability;
    }

    public Ability<? super Self> getAbility() {
        return ability != null
                ? ability
                : (self) -> UndoableUnregisterAction.DO_NOTHING;
    }

    public GameEventActionDefs<Self> getEventActionDefs() {
        return eventActionDefs;
    }

    public GameEventAction<? super Self, ? super Self> tryGetDeathRattle() {
        return deathRattle;
    }

    public GameEventAction<? super Self, ? super Self> getDeathRattle() {
        return deathRattle != null
                ? deathRattle
                : (game, self, eventSource) -> UndoAction.DO_NOTHING;
    }
}
