package info.hearthsim.brazier.parsing;

import info.hearthsim.brazier.*;
import info.hearthsim.brazier.events.*;
import info.hearthsim.brazier.events.GameEventActions;
import com.google.gson.JsonPrimitive;

import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;

import info.hearthsim.brazier.game.Entity;
import info.hearthsim.brazier.game.GameProperty;
import info.hearthsim.brazier.game.Secret;
import org.jtrim.utils.ExceptionHelper;

/**
 * Parser for Event Notification, which will be triggered when certain event happens. Such
 * things may include {@link Secret} and some abilities triggered when event happens, like
 * what <em>Knife Juggler</em> has. An {@code EventNotificationParser} uses an underlying
 * {@link JsonDeserializer} to parse Json card files.
 */
public final class EventNotificationParser <Self extends Entity> {
    private final Class<? extends Self> selfType;
    private final JsonDeserializer objectParser;
    private final EventFilter globalFilter;
    private final EventAction actionFinalizer;

    public EventNotificationParser(Class<? extends Self> selfType, JsonDeserializer objectParser) {
        this(selfType, objectParser, EventFilters.ANY, EventAction.DO_NOTHING);
    }

    public EventNotificationParser(
        Class<? extends Self> selfType,
        JsonDeserializer objectParser,
        EventFilter globalFilter,
        EventAction<? super Self, ?> actionFinalizer) {
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
     * Parses the given {@link JsonTree} to a {@link TriggeringAbility}.
     *
     * @param tree the given {@code JsonTree}.
     * @return the parsed result as a {@code CardDescr}.
     * @throws ObjectParsingException if failed to parse the given {@code JsonTree}.
     */
    public TriggeringAbility<Self> fromJson(JsonTree tree) throws ObjectParsingException {
        TriggeringAbility.Builder<Self> builder = new TriggeringAbility.Builder<>();

        // Try to parse the card json with every possible `SimpleEventType`
        for (SimpleEventType eventType : SimpleEventType.values())
            parseSimpleActionDefs(tree, eventType, builder);

        // parseOnSummonEvents(tree.getChild("on-summon"), builder);

        return builder.create();
    }

    /**
     * Parses the given {@link JsonTree} to an {@link EventFilter} of the {@code selfType} of this
     * {@code EventNotificationParser} and the given target type. The global filter of this
     * {@code EventNotificationParser} will be added to the converted filter before it is returned.
     *
     * @param targetType    the given target type.
     * @param filterElement the given {@code JsonTree}.
     * @return the converted result.
     * @throws ObjectParsingException if failed to convert the given {@code JsonTree}.
     */
    public <T> EventFilter<? super Self, ? super T> parseFilter(
        Class<T> targetType,
        JsonTree filterElement) throws ObjectParsingException {
        if (filterElement == null) {
            return globalFilter;
        }

        // This is not safe at all but there is nothing we can do about it.
        @SuppressWarnings("unchecked")
        EventFilter<? super Self, ? super T> result = (EventFilter<? super Self, ? super T>) objectParser
            .toJavaObject(filterElement, EventFilter.class, eventFilterTypeChecker(targetType));
        // checks the actual type arguments of the generated EventFilter actually super `Self` and `T`
        if (globalFilter == EventFilters.ANY)
            return result;

        return (Self owner, T eventSource) ->
            globalFilter.applies(owner, eventSource) && result.applies(owner, eventSource);
    }

    /**
     * Parses the given {@link JsonTree} to an {@link EventAction} of the {@code selfType} of this
     * {@code EventNotificationParser} and the given target type. The action finalizer of this
     * {@code EventNotificationParser} will be appended to the converted action before it is returned.
     *
     * @param targetType    the given target type.
     * @param actionElement the given {@code JsonTree}.
     * @return the converted result.
     * @throws ObjectParsingException if failed to convert the given {@code JsonTree}.
     */
    public <T> EventAction<? super Self, ? super T> parseAction(
        Class<T> targetType,
        JsonTree actionElement) throws ObjectParsingException {
        if (actionElement == null) {
            throw new ObjectParsingException("Missing action definition.");
        }

        EventAction<?, ?> resultObj = objectParser.toJavaObject(
            actionElement,
            EventAction.class,
            actionFilterTypeChecker(targetType)
            // checks the actual type arguments of the generated EventAction actually super `Self` and `T`
        );
        return toFinalizedAction(unsafeCastToEventAction(resultObj));
    }

    /**
     * Parses the given {@link JsonTree} and adds the converted {@link EventActionDef} to the given builder
     * if the given {@code JsonTree} declares the given type of event.
     */
    private <T extends GameProperty> void parseSimpleActionDefs(
        JsonTree tree,
        SimpleEventType eventType,
        TriggeringAbility.Builder<Self> builder) throws ObjectParsingException {

        JsonTree actionDefsElement = tree.getChild(eventType.getEventName());
        if (actionDefsElement == null) {
            return;
        }

        @SuppressWarnings("unchecked")
        Class<T> eventArgType = (Class<T>) eventType.getArgumentType();

        Function<GameEvents, GameEventActions<T>> actionEventListenersGetter =
            (gameEvents) -> gameEvents.simpleListeners(eventType);

        Consumer<EventActionDef<Self, T>> actionDefAdder =
            (actionDef) -> builder.addSimpleEventDef(eventType, actionDef);

        parseActionDefs(
            eventArgType, actionDefsElement, actionEventListenersGetter,
            actionDefAdder, eventType.getGlobalFilter()
        );
    }

    /**
     * Parses the given {@link JsonTree} to {@link EventActionDef}(s) by using
     * {@link #tryParseActionDef(Class, JsonTree, Function, EventFilter)} and consume the parsed result with the
     * given {@code actionDefAdder}.
     */
    private <T extends GameProperty> void parseActionDefs(
        Class<T> targetType,
        JsonTree actionDefsElement,
        Function<GameEvents, GameEventActions<T>> actionEventListenersGetter,
        Consumer<EventActionDef<Self, T>> actionDefAdder,
        EventFilter<? super Self, ? super T> globalFilter) throws ObjectParsingException {

        if (actionDefsElement == null) {
            return;
        }

        if (actionDefsElement.isJsonArray()) {
            for (JsonTree singleActionDefElement : actionDefsElement.getChildren()) {
                EventActionDef<Self, T> actionDef
                    = tryParseActionDef(targetType, singleActionDefElement, actionEventListenersGetter, globalFilter);
                if (actionDef != null) {
                    actionDefAdder.accept(actionDef);
                }
            }
        } else {
            EventActionDef<Self, T> actionDef
                = tryParseActionDef(targetType, actionDefsElement, actionEventListenersGetter, globalFilter);
            if (actionDef != null) {
                actionDefAdder.accept(actionDef);
            }
        }
    }

    /**
     * Parses the given {@link JsonTree} to a {@link EventActionDef} of the {@code selfType} of this
     * {@code EventNotificationParser} and the given target type.
     */
    private <T extends GameProperty> EventActionDef<Self, T> tryParseActionDef(
        Class<T> targetType,
        JsonTree actionDefElement,
        Function<GameEvents, GameEventActions<T>> actionEventListenersGetter,
        EventFilter<? super Self, ? super T> globalFilter) throws ObjectParsingException {

        if (actionDefElement == null) {
            return null;
        }

        if (!actionDefElement.isJsonObject()) {
            throw new ObjectParsingException("EventActionDef requires a JsonObject.");
        }

        EventFilter<? super Self, ? super T> baseFilter =
            parseFilter(targetType, actionDefElement.getChild("filter"));
        EventAction<? super Self, ? super T> action =
            parseAction(targetType, actionDefElement.getChild("action"));

        EventFilter<? super Self, ? super T> filter;
        if (globalFilter != null)
            filter = (self, arg) -> baseFilter.applies(self, arg) && globalFilter.applies(self, arg);
        else
            filter = baseFilter;

        JsonTree triggerOnceElement = actionDefElement.getChild("triggerOnce");
        boolean triggerOnce = triggerOnceElement != null && triggerOnceElement.getAsBoolean();

        JsonTree lazyFilterElement = actionDefElement.getChild("lazyFilter");
        boolean lazyFilter = lazyFilterElement != null && lazyFilterElement.getAsBoolean();

        int priority = getPriority(actionDefElement);

        return new EventActionDef<>(lazyFilter, triggerOnce, priority,
            actionEventListenersGetter, filter, action);
    }

    /**
     * Returns a {@link TypeChecker} which checks if it is assignable from the actual type arguments of the testing
     * generic type to the {@code selfType} of this {@code EventNotificationParser} and the given target type when
     * the raw type of the testing generic type is {@link EventFilter}.
     */
    private TypeChecker eventFilterTypeChecker(Class<?> targetType) {
        return TypeCheckers.genericTypeChecker(EventFilter.class, selfType, targetType);
    }

    /**
     * Returns a {@link TypeChecker} which checks if it is assignable from the actual type arguments of the testing
     * generic type to the {@code selfType} of this {@code EventNotificationParser} and the given target type when
     * the raw type of the testing generic type is {@link EventAction}.
     */
    private TypeChecker actionFilterTypeChecker(Class<?> targetType) {
        return TypeCheckers.genericTypeChecker(EventAction.class, selfType, targetType);
    }

    // This is not safe at all but there is nothing we can do about it.
    @SuppressWarnings("unchecked")
    private <T> EventAction<? super Self, ? super T> unsafeCastToEventAction(Object obj) {
        return (EventAction<? super Self, ? super T>) obj;
    }

    /**
     * Appends the action finalizer of this {@code EventNotificationParser} to the given action and returns.
     */
    private <T> EventAction<? super Self, ? super T>
        toFinalizedAction(EventAction<? super Self, ? super T> action) {

        if (actionFinalizer == EventAction.DO_NOTHING)
            return action;

        return (Self self, T eventSource) -> {
            action.trigger(self, eventSource);
            actionFinalizer.trigger(self, eventSource);
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
/*
    private void parseSingleOnSummonEvent(
        JsonTree actionDefElement,
        TriggeringAbility.Builder<Self> result) throws ObjectParsingException {

        if (actionDefElement == null) {
            return;
        }

        EventFilter<? super Self, ? super Minion> filter =
            parseFilter(Minion.class, actionDefElement.getChild("filter"));
        EventAction<? super Self, ? super Minion> action =
            parseAction(Minion.class, actionDefElement.getChild("action"));

        JsonTree triggerOnceElement = actionDefElement.getChild("triggerOnce");
        boolean triggerOnce = triggerOnceElement != null ? triggerOnceElement.getAsBoolean() : false;

        int priority = getPriority(actionDefElement);

        EventAction<Self, Minion> eventDef = (Self self, Minion eventSource) -> {
            if (self == eventSource)
                return;

            if (filter.applies(self, eventSource)) {
                action.trigger(self, eventSource);
                return;
            }

            if (filter.applies(self, eventSource))
                action.trigger(self, eventSource);
        };

        result.addOnSummoningActionDef(new CompletableGameEventBasedActionDef<>(triggerOnce, priority, GameEvents::summoningListeners, eventDef));
    }

    private void parseOnSummonEvents(
        JsonTree actionDefsElement,
        TriggeringAbility.Builder<Self> result) throws ObjectParsingException {
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
    }*/
}
