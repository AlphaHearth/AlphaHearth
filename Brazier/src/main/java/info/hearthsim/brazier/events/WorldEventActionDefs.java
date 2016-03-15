package info.hearthsim.brazier.events;

import info.hearthsim.brazier.PlayerProperty;
import info.hearthsim.brazier.actions.undo.UndoableUnregisterAction;
import info.hearthsim.brazier.minions.Minion;
import info.hearthsim.brazier.abilities.Ability;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jtrim.collections.CollectionsEx;
import org.jtrim.utils.ExceptionHelper;

public final class WorldEventActionDefs <Self extends PlayerProperty> implements Ability<Self> {
    private final Map<SimpleEventType, ActionDefList<Self, ?>> simpleEventDefs;
    private final List<CompletableWorldEventBasedActionDef<Self, Minion>> onSummoningActionDefs;

    private final List<RegTask<Self>> regTasks;
    private final boolean hasAnyActionDef;

    private WorldEventActionDefs(Builder<Self> builder) {
        this.regTasks = new ArrayList<>(20);

        this.simpleEventDefs = new EnumMap<>(SimpleEventType.class);
        builder.simpleEventDefs.values().forEach(this::importListeners);

        this.onSummoningActionDefs = importCompletableListeners(builder.onSummoningActionDefs);
        this.hasAnyActionDef = !regTasks.isEmpty();
    }

    private <E> List<CompletableWorldEventBasedActionDef<Self, E>> importCompletableListeners(
        List<CompletableWorldEventBasedActionDef<Self, E>> actionDefs) {

        List<CompletableWorldEventBasedActionDef<Self, E>> result = CollectionsEx.readOnlyCopy(actionDefs);
        if (!result.isEmpty()) {
            regTasks.add((WorldEvents worldEvents, Self self) -> {
                return CompletableWorldEventBasedActionDef.registerAll(result, worldEvents, self);
            });
        }
        return result;
    }

    private <E> void importListeners(ActionDefList<Self, E> actionDefs) {
        ActionDefList<Self, E> importedActionDefs = actionDefs.importInto(simpleEventDefs);
        List<WorldEventBasedActionDef<Self, E>> actionDefList = importedActionDefs.actionDefs;
        regTasks.add((WorldEvents worldEvents, Self self) -> {
            return WorldEventBasedActionDef.registerAll(actionDefList, worldEvents, self);
        });
    }

    public UndoableUnregisterAction registerOnSummoningAction(WorldEvents worldEvents, Self self) {
        return CompletableWorldEventBasedActionDef.registerAll(onSummoningActionDefs, worldEvents, self);
    }

    @Override
    public UndoableUnregisterAction activate(Self self) {
        if (!hasAnyActionDef) {
            return UndoableUnregisterAction.DO_NOTHING;
        }

        WorldEvents worldEvents = self.getWorld().getEvents();

        UndoableUnregisterAction.Builder result = new UndoableUnregisterAction.Builder(regTasks.size());
        for (RegTask<Self> regTask : regTasks) {
            result.addRef(regTask.register(worldEvents, self));
        }
        return result;
    }

    public boolean hasAnyActionDef() {
        return hasAnyActionDef;
    }

    private interface RegTask <Self> {
        public UndoableUnregisterAction register(WorldEvents worldEvents, Self self);
    }

    public static final class Builder <Self extends PlayerProperty> {
        private final Map<SimpleEventType, ActionDefList<Self, ?>> simpleEventDefs;
        private final List<CompletableWorldEventBasedActionDef<Self, Minion>> onSummoningActionDefs;

        public Builder() {
            this.simpleEventDefs = new EnumMap<>(SimpleEventType.class);
            this.onSummoningActionDefs = new LinkedList<>();
        }

        public <E> void addSimpleEventDef(SimpleEventType eventType, WorldEventBasedActionDef<Self, E> def) {
            ExceptionHelper.checkNotNullArgument(def, "def");

            @SuppressWarnings("unchecked")
            ActionDefList<Self, E> defs
                = (ActionDefList<Self, E>) simpleEventDefs.computeIfAbsent(eventType, ActionDefList::new);
            defs.add(def);
        }

        public void addOnSummoningActionDef(CompletableWorldEventBasedActionDef<Self, Minion> def) {
            ExceptionHelper.checkNotNullArgument(def, "def");
            onSummoningActionDefs.add(def);
        }

        public WorldEventActionDefs<Self> create() {
            return new WorldEventActionDefs<>(this);
        }
    }

    private static final class ActionDefList <Self extends PlayerProperty, E> {
        private final SimpleEventType eventType;
        private final List<WorldEventBasedActionDef<Self, E>> actionDefs;

        public ActionDefList(SimpleEventType eventType) {
            this.eventType = eventType;
            this.actionDefs = new ArrayList<>();
        }

        public ActionDefList(ActionDefList<Self, E> other) {
            // Immutable copy
            this.eventType = other.eventType;
            this.actionDefs = CollectionsEx.readOnlyCopy(other.actionDefs);
        }

        public void add(WorldEventBasedActionDef<Self, E> actionDef) {
            actionDefs.add(actionDef);
        }

        public ActionDefList<Self, E> importInto(Map<SimpleEventType, ActionDefList<Self, ?>> result) {
            ActionDefList<Self, E> imported = new ActionDefList<>(this);
            result.put(eventType, imported);
            return imported;
        }
    }
}
