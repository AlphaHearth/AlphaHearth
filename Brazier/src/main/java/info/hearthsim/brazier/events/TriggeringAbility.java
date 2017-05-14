package info.hearthsim.brazier.events;

import info.hearthsim.brazier.abilities.Ability;
import info.hearthsim.brazier.game.Entity;
import info.hearthsim.brazier.game.GameProperty;
import info.hearthsim.brazier.util.UndoAction;
import org.jtrim.collections.CollectionsEx;
import org.jtrim.utils.ExceptionHelper;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * A triggering ability for a Hearthstone entity uses an underlying map of {@code SimpleEventType}
 * to {@code List} of {@code EventActionDef}s to represent the triggering ability, while its
 * {@link #activate(Owner)} method registers all these triggering actions to the game's {@link GameEvents}.
 * {@code TriggeringAbility} does not have any {@code public} constructor. The only way to construct
 * a {@code TriggeringAbility} is through its {@link info.hearthsim.brazier.events.TriggeringAbility.Builder Builder}
 * class, which guarantees a constructed {@code TriggeringAbility} being stateless and immutable.
 *
 * @param <Owner> The type of the owner of this ability.
 *
 * @see EventActionDef
 */
public final class TriggeringAbility <Owner extends Entity> implements Ability<Owner> {
    private final Map<SimpleEventType, ActionDefList<Owner, ?>> simpleEventDefs;

    private final List<RegTask<Owner>> regTasks;
    private final boolean hasAnyActionDef;

    private TriggeringAbility(Builder<Owner> builder) {
        this.regTasks = new ArrayList<>();

        this.simpleEventDefs = new EnumMap<>(SimpleEventType.class);
        builder.simpleEventDefs.values().forEach(this::importListeners);

        this.hasAnyActionDef = !regTasks.isEmpty();
    }

    @Override
    public UndoAction<Owner> activate(Owner owner) {
        if (!hasAnyActionDef)
            return UndoAction.DO_NOTHING;

        GameEvents gameEvents = owner.getGame().getEvents();

        UndoAction.Builder<Owner> result = new UndoAction.Builder<>(regTasks.size());
        for (RegTask<Owner> regTask : regTasks) {
            UndoAction<GameEvents> undoRef = regTask.register(gameEvents, owner);
            result.add((o) -> undoRef.undo(o.getGame().getEvents()));
        }
        return result;
    }

    /**
     * Imports the registration of the given {@code ActionDefList} to the {@code regTasks} field.
     */
    private <Source extends GameProperty> void importListeners(ActionDefList<Owner, Source> actionDefs) {
        ActionDefList<Owner, Source> importedActionDefs = actionDefs.importInto(simpleEventDefs);
        List<EventActionDef<Owner, Source>> actionDefList = importedActionDefs.actionDefs;
        regTasks.add(
            (GameEvents gameEvents, Owner owner) ->
                EventActionDef.registerAll(actionDefList, gameEvents, owner)
        );
    }

    public boolean hasAnyActionDef() {
        return hasAnyActionDef;
    }

    private interface RegTask <Self> {
        public UndoAction<GameEvents> register(GameEvents gameEvents, Self self);
    }

    public static final class Builder <Owner extends Entity> {
        private final Map<SimpleEventType, ActionDefList<Owner, ?>> simpleEventDefs;

        public Builder() {
            this.simpleEventDefs = new EnumMap<>(SimpleEventType.class);
        }

        public <Source extends GameProperty> void addSimpleEventDef(SimpleEventType eventType,
                                                                    EventActionDef<Owner, Source> def) {
            ExceptionHelper.checkNotNullArgument(def, "def");

            @SuppressWarnings("unchecked")
            ActionDefList<Owner, Source> defs
                = (ActionDefList<Owner, Source>) simpleEventDefs.computeIfAbsent(eventType, ActionDefList::new);
            defs.add(def);
        }

        public TriggeringAbility<Owner> create() {
            return new TriggeringAbility<>(this);
        }
    }

    /**
     * {@code List} of {@code EventActionDef}, with their related {@code SimpleEventType}.
     */
    private static final class ActionDefList <Owner extends Entity, Source extends GameProperty> {
        private final SimpleEventType eventType;
        private final List<EventActionDef<Owner, Source>> actionDefs;

        public ActionDefList(SimpleEventType eventType) {
            this.eventType = eventType;
            this.actionDefs = new ArrayList<>();
        }

        public ActionDefList(ActionDefList<Owner, Source> other) {
            // Immutable copy
            this.eventType = other.eventType;
            this.actionDefs = CollectionsEx.readOnlyCopy(other.actionDefs);
        }

        public void add(EventActionDef<Owner, Source> actionDef) {
            actionDefs.add(actionDef);
        }

        /**
         * Puts a copy of this {@code ActionDefList} to the given map.
         */
        public ActionDefList<Owner, Source> importInto(Map<SimpleEventType, ActionDefList<Owner, ?>> map) {
            ActionDefList<Owner, Source> imported = new ActionDefList<>(this);
            map.put(eventType, imported);
            return imported;
        }
    }
}
