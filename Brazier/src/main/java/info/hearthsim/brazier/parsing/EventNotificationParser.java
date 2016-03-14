package info.hearthsim.brazier.parsing;

import info.hearthsim.brazier.events.CompletableWorldEventAction;
import info.hearthsim.brazier.events.WorldEvents;
import info.hearthsim.brazier.minions.Minion;
import info.hearthsim.brazier.PlayerProperty;
import info.hearthsim.brazier.Priorities;
import info.hearthsim.brazier.World;
import info.hearthsim.brazier.events.WorldEventFilters;
import info.hearthsim.brazier.actions.undo.UndoAction;
import info.hearthsim.brazier.events.CompletableWorldEventBasedActionDef;
import info.hearthsim.brazier.events.CompleteWorldEventAction;
import info.hearthsim.brazier.events.SimpleEventType;
import info.hearthsim.brazier.events.WorldActionEventsRegistry;
import info.hearthsim.brazier.events.WorldEventAction;
import info.hearthsim.brazier.events.WorldEventActionDefs;
import info.hearthsim.brazier.events.WorldEventBasedActionDef;
import info.hearthsim.brazier.events.WorldEventFilter;
import com.google.gson.JsonPrimitive;

import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;

import org.jtrim.utils.ExceptionHelper;

public final class EventNotificationParser <Self extends PlayerProperty> {
    private static final WorldEventAction<PlayerProperty, Object> DO_NOTHING
        = (world, self, arg) -> UndoAction.DO_NOTHING;

    private final Class<? extends Self> selfType;
    private final JsonDeserializer objectParser;
    private final WorldEventFilter<? super Self, Object> globalFilter;
    private final WorldEventAction<? super Self, Object> actionFinalizer;

    public EventNotificationParser(Class<? extends Self> selfType, JsonDeserializer objectParser) {
        this(selfType, objectParser, WorldEventFilters.ANY, DO_NOTHING);
    }

    public EventNotificationParser(
        Class<? extends Self> selfType,
        JsonDeserializer objectParser,
        WorldEventFilter<? super Self, Object> globalFilter,
        WorldEventAction<? super Self, Object> actionFinalizer) {
        ExceptionHelper.checkNotNullArgument(selfType, "selfType");
        ExceptionHelper.checkNotNullArgument(objectParser, "objectParser");
        ExceptionHelper.checkNotNullArgument(globalFilter, "globalFilter");
        ExceptionHelper.checkNotNullArgument(actionFinalizer, "actionFinalizer");

        this.selfType = selfType;
        this.objectParser = objectParser;
        this.globalFilter = globalFilter;
        this.actionFinalizer = actionFinalizer;
    }

    /**
     * Parses the given {@link JsonTree} to a {@link WorldEventActionDefs}.
     *
     * @param tree the given {@code JsonTree}.
     * @return the parsed result as a {@code CardDescr}.
     * @throws ObjectParsingException if failed to parse the given {@code JsonTree}.
     */
    public WorldEventActionDefs<Self> fromJson(JsonTree tree) throws ObjectParsingException {
        WorldEventActionDefs.Builder<Self> builder = new WorldEventActionDefs.Builder<>();

        // Try to parse the card json with every possible `SimpleEventType`
        for (SimpleEventType eventType : SimpleEventType.values()) {
            parseSimpleActionDefs(tree, eventType, builder);
        }

        parseOnSummonEvents(tree.getChild("on-summon"), builder);

        parseActionDefs(
            Minion.class,
            tree.getChild("start-summoning"),
            WorldEvents::startSummoningListeners,
            (actionDef) -> builder.addOnSummoningActionDef(actionDef.toStartEventDef(WorldEvents::summoningListeners)),
            null);

        parseActionDefs(
            Minion.class,
            tree.getChild("done-summoning"),
            WorldEvents::doneSummoningListeners,
            (actionDef) -> builder.addOnSummoningActionDef(actionDef.toDoneEventDef(WorldEvents::summoningListeners)),
            null);

        return builder.create();
    }

    /**
     * Parses the given {@link JsonTree} and adds the converted {@link WorldEventBasedActionDef} to the given builder
     * if the given {@code JsonTree} declares the given type of event.
     */
    private <T> void parseSimpleActionDefs(
        JsonTree tree,
        SimpleEventType eventType,
        WorldEventActionDefs.Builder<Self> builder) throws ObjectParsingException {

        JsonTree actionDefsElement = tree.getChild(eventType.getEventName());
        if (actionDefsElement == null) {
            return;
        }

        @SuppressWarnings("unchecked")
        Class<T> eventArgType = (Class<T>) eventType.getArgumentType();

        Function<WorldEvents, ? extends WorldActionEventsRegistry<T>> actionEventListenersGetter =
            (worldEvents) -> worldEvents.simpleListeners(eventType);

        Consumer<WorldEventBasedActionDef<Self, T>> actionDefAdder =
            (actionDef) -> builder.addSimpleEventDef(eventType, actionDef);

        parseActionDefs(
            eventArgType, actionDefsElement, actionEventListenersGetter,
            actionDefAdder, eventType.getGlobalFilter()
        );
    }

    /**
     * Parses the given {@link JsonTree} to {@link WorldEventBasedActionDef}(s) by using
     * {@link #tryParseActionDef(Class, JsonTree, Function, WorldEventFilter)} and consume the parsed result with the
     * given {@code actionDefAdder}.
     */
    private <T> void parseActionDefs(
        Class<T> targetType,
        JsonTree actionDefsElement,
        Function<WorldEvents, ? extends WorldActionEventsRegistry<T>> actionEventListenersGetter,
        Consumer<WorldEventBasedActionDef<Self, T>> actionDefAdder,
        WorldEventFilter<? super Self, ? super T> globalFilter) throws ObjectParsingException {

        if (actionDefsElement == null) {
            return;
        }

        if (actionDefsElement.isJsonArray()) {
            for (JsonTree singleActionDefElement : actionDefsElement.getChildren()) {
                WorldEventBasedActionDef<Self, T> actionDef
                    = tryParseActionDef(targetType, singleActionDefElement, actionEventListenersGetter, globalFilter);
                if (actionDef != null) {
                    actionDefAdder.accept(actionDef);
                }
            }
        } else {
            WorldEventBasedActionDef<Self, T> actionDef
                = tryParseActionDef(targetType, actionDefsElement, actionEventListenersGetter, globalFilter);
            if (actionDef != null) {
                actionDefAdder.accept(actionDef);
            }
        }
    }

    /**
     * Parses the given {@link JsonTree} to a {@link WorldEventBasedActionDef} of the {@code selfType} of this
     * {@code EventNotificationParser} and the given target type.
     */
    private <T> WorldEventBasedActionDef<Self, T> tryParseActionDef(
        Class<T> targetType,
        JsonTree actionDefElement,
        Function<WorldEvents, ? extends WorldActionEventsRegistry<T>> actionEventListenersGetter,
        WorldEventFilter<? super Self, ? super T> globalFilter) throws ObjectParsingException {

        if (actionDefElement == null) {
            return null;
        }

        if (!actionDefElement.isJsonObject()) {
            throw new ObjectParsingException("WorldEventBasedActionDef requires a JsonObject.");
        }

        WorldEventFilter<? super Self, ? super T> baseFilter = parseFilter(targetType, actionDefElement.getChild("filter"));
        WorldEventAction<? super Self, ? super T> action = parseAction(targetType, actionDefElement.getChild("action"));

        WorldEventFilter<? super Self, ? super T> filter;
        if (globalFilter != null) {
            filter = (world, self, arg) -> baseFilter.applies(world, self, arg) && globalFilter.applies(world, self, arg);
        } else {
            filter = baseFilter;
        }

        JsonTree triggerOnceElement = actionDefElement.getChild("triggerOnce");
        boolean triggerOnce = triggerOnceElement != null && triggerOnceElement.getAsBoolean();

        JsonTree lazyFilterElement = actionDefElement.getChild("lazyFilter");
        boolean lazyFilter = lazyFilterElement != null && lazyFilterElement.getAsBoolean();

        int priority = getPriority(actionDefElement);

        return new WorldEventBasedActionDef<>(lazyFilter, triggerOnce, priority, actionEventListenersGetter, filter, action);
    }

    /**
     * Parses the given {@link JsonTree} to an {@link WorldEventFilter} of the {@code selfType} of this
     * {@code EventNotificationParser} and the given target type. The global filter of this
     * {@code EventNotificationParser} will be added to the converted filter before it is returned.
     *
     * @param targetType the given target type.
     * @param filterElement the given {@code JsonTree}.
     * @param <T> the given target type.
     * @return the converted result.
     * @throws ObjectParsingException if failed to convert the given {@code JsonTree}.
     */
    public <T> WorldEventFilter<? super Self, ? super T> parseFilter(
        Class<T> targetType,
        JsonTree filterElement) throws ObjectParsingException {
        if (filterElement == null) {
            return globalFilter;
        }

        // This is not safe at all but there is nothing we can do about it.
        @SuppressWarnings("unchecked")
        WorldEventFilter<? super Self, ? super T> result = (WorldEventFilter<? super Self, ? super T>) objectParser
            .toJavaObject(filterElement, WorldEventFilter.class, eventFilterTypeChecker(targetType));
          // checks the actual type arguments of the generated WorldEventFilter actually super `Self` and `T`
        if (globalFilter == WorldEventFilters.ANY) {
            return result;
        }

        return (World world, Self owner, T eventSource) -> {
            return globalFilter.applies(world, owner, eventSource)
                && result.applies(world, owner, eventSource);
        };
    }

    /**
     * Parses the given {@link JsonTree} to an {@link WorldEventAction} of the {@code selfType} of this
     * {@code EventNotificationParser} and the given target type. The action finalizer of this
     * {@code EventNotificationParser} will be appended to the converted action before it is returned.
     *
     * @param targetType the given target type.
     * @param actionElement the given {@code JsonTree}.
     * @param <T> the given target type.
     * @return the converted result.
     * @throws ObjectParsingException if failed to convert the given {@code JsonTree}.
     */
    public <T> WorldEventAction<? super Self, ? super T> parseAction(
        Class<T> targetType,
        JsonTree actionElement) throws ObjectParsingException {
        if (actionElement == null) {
            throw new ObjectParsingException("Missing action definition.");
        }

        WorldEventAction<?, ?> resultObj = objectParser.toJavaObject(
            actionElement,
            WorldEventAction.class,
            actionFilterTypeChecker(targetType)
            // checks the actual type arguments of the generated WorldEventAction actually super `Self` and `T`
        );
        return toFinalizedAction(unsafeCastToEventAction(resultObj));
    }

    /**
     * Returns a {@link TypeChecker} which checks if it is assignable from the actual type arguments of the testing
     * generic type to the {@code selfType} of this {@code EventNotificationParser} and the given target type when
     * the raw type of the testing generic type is {@link WorldEventFilter}.
     */
    private TypeChecker eventFilterTypeChecker(Class<?> targetType) {
        return TypeCheckers.genericTypeChecker(WorldEventFilter.class, selfType, targetType);
    }

    /**
     * Returns a {@link TypeChecker} which checks if it is assignable from the actual type arguments of the testing
     * generic type to the {@code selfType} of this {@code EventNotificationParser} and the given target type when
     * the raw type of the testing generic type is {@link WorldEventAction}.
     */
    private TypeChecker actionFilterTypeChecker(Class<?> targetType) {
        return TypeCheckers.genericTypeChecker(WorldEventAction.class, selfType, targetType);
    }

    // This is not safe at all but there is nothing we can do about it.
    @SuppressWarnings("unchecked")
    private <T> WorldEventAction<? super Self, ? super T> unsafeCastToEventAction(Object obj) {
        return (WorldEventAction<? super Self, ? super T>) obj;
    }

    /**
     * Appends the action finalizer of this {@code EventNotificationParser} to the given action and returns.
     */
    private <T> WorldEventAction<? super Self, ? super T> toFinalizedAction(WorldEventAction<? super Self, ? super T> action) {
        if (actionFinalizer == DO_NOTHING) {
            return action;
        }

        return (World world, Self self, T eventSource) -> {
            UndoAction actionUndo = action.alterWorld(world, self, eventSource);
            UndoAction finalizeUndo = actionFinalizer.alterWorld(world, self, eventSource);
            return () -> {
                finalizeUndo.undo();
                actionUndo.undo();
            };
        };
    }

    /**
     * Gets the {@code priority} field of the given {@link JsonTree}.
     *
     * @see Priorities
     */
    private int getPriority(JsonTree actionDefElement) {
        JsonTree priorityElement = actionDefElement.getChild("priority");
        if (priorityElement == null) {
            return Priorities.NORMAL_PRIORITY;
        }

        JsonPrimitive value = priorityElement.getAsJsonPrimitive();
        if (value.isString()) {
            switch (value.getAsString().toLowerCase(Locale.ROOT)) {
                case "lowest":
                    return Priorities.LOWEST_PRIORITY;
                case "low":
                    return Priorities.LOW_PRIORITY;
                case "normal":
                    return Priorities.NORMAL_PRIORITY;
                case "high":
                    return Priorities.HIGH_PRIORITY;
                case "highest":
                    return Priorities.HIGHEST_PRIORITY;
            }
        }
        return priorityElement.getAsInt();
    }

    private void parseSingleOnSummonEvent(
        JsonTree actionDefElement,
        WorldEventActionDefs.Builder<Self> result) throws ObjectParsingException {

        if (actionDefElement == null) {
            return;
        }

        WorldEventFilter<? super Self, ? super Minion> filter = parseFilter(Minion.class, actionDefElement.getChild("filter"));
        WorldEventAction<? super Self, ? super Minion> action = parseAction(Minion.class, actionDefElement.getChild("action"));

        JsonTree triggerOnceElement = actionDefElement.getChild("triggerOnce");
        boolean triggerOnce = triggerOnceElement != null ? triggerOnceElement.getAsBoolean() : false;

        int priority = getPriority(actionDefElement);

        CompletableWorldEventAction<Self, Minion> eventDef = (World world, Self self, Minion eventSource) -> {
            if (self == eventSource) {
                return CompleteWorldEventAction.doNothing(UndoAction.DO_NOTHING);
            }

            if (filter.applies(world, self, eventSource)) {
                UndoAction alterWorld = action.alterWorld(world, self, eventSource);
                return CompleteWorldEventAction.doNothing(alterWorld);
            }

            return CompleteWorldEventAction.nothingToUndo((completeWorld, completeSelf, completeEventSource) -> {
                if (filter.applies(world, self, eventSource)) {
                    return action.alterWorld(world, self, eventSource);
                } else {
                    return UndoAction.DO_NOTHING;
                }
            });
        };

        result.addOnSummoningActionDef(new CompletableWorldEventBasedActionDef<>(triggerOnce, priority, WorldEvents::summoningListeners, eventDef));
    }

    private void parseOnSummonEvents(
        JsonTree actionDefsElement,
        WorldEventActionDefs.Builder<Self> result) throws ObjectParsingException {
        if (actionDefsElement == null) {
            return;
        }

        if (actionDefsElement.isJsonArray()) {
            for (JsonTree singleActionDefElement : actionDefsElement.getChildren()) {
                parseSingleOnSummonEvent(singleActionDefElement, result);
            }
        } else {
            parseSingleOnSummonEvent(actionDefsElement, result);
        }
    }
}
