package info.hearthsim.brazier.parsing;

import info.hearthsim.brazier.Game;
import info.hearthsim.brazier.events.*;
import info.hearthsim.brazier.minions.Minion;
import info.hearthsim.brazier.PlayerProperty;
import info.hearthsim.brazier.Priorities;
import info.hearthsim.brazier.actions.undo.UndoAction;
import info.hearthsim.brazier.events.GameActionEventsRegistry;
import com.google.gson.JsonPrimitive;

import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;

import org.jtrim.utils.ExceptionHelper;

public final class EventNotificationParser <Self extends PlayerProperty> {
    private static final GameEventAction<PlayerProperty, Object> DO_NOTHING
        = (game, self, arg) -> UndoAction.DO_NOTHING;

    private final Class<? extends Self> selfType;
    private final JsonDeserializer objectParser;
    private final GameEventFilter<? super Self, Object> globalFilter;
    private final GameEventAction<? super Self, Object> actionFinalizer;

    public EventNotificationParser(Class<? extends Self> selfType, JsonDeserializer objectParser) {
        this(selfType, objectParser, GameEventFilters.ANY, DO_NOTHING);
    }

    public EventNotificationParser(
        Class<? extends Self> selfType,
        JsonDeserializer objectParser,
        GameEventFilter<? super Self, Object> globalFilter,
        GameEventAction<? super Self, Object> actionFinalizer) {
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
     * Parses the given {@link JsonTree} to a {@link GameEventActionDefs}.
     *
     * @param tree the given {@code JsonTree}.
     * @return the parsed result as a {@code CardDescr}.
     * @throws ObjectParsingException if failed to parse the given {@code JsonTree}.
     */
    public GameEventActionDefs<Self> fromJson(JsonTree tree) throws ObjectParsingException {
        GameEventActionDefs.Builder<Self> builder = new GameEventActionDefs.Builder<>();

        // Try to parse the card json with every possible `SimpleEventType`
        for (SimpleEventType eventType : SimpleEventType.values()) {
            parseSimpleActionDefs(tree, eventType, builder);
        }

        parseOnSummonEvents(tree.getChild("on-summon"), builder);

        parseActionDefs(
            Minion.class,
            tree.getChild("start-summoning"),
            GameEvents::startSummoningListeners,
            (actionDef) -> builder.addOnSummoningActionDef(actionDef.toStartEventDef(GameEvents::summoningListeners)),
            null);

        parseActionDefs(
            Minion.class,
            tree.getChild("done-summoning"),
            GameEvents::doneSummoningListeners,
            (actionDef) -> builder.addOnSummoningActionDef(actionDef.toDoneEventDef(GameEvents::summoningListeners)),
            null);

        return builder.create();
    }

    /**
     * Parses the given {@link JsonTree} and adds the converted {@link GameEventBasedActionDef} to the given builder
     * if the given {@code JsonTree} declares the given type of event.
     */
    private <T> void parseSimpleActionDefs(
        JsonTree tree,
        SimpleEventType eventType,
        GameEventActionDefs.Builder<Self> builder) throws ObjectParsingException {

        JsonTree actionDefsElement = tree.getChild(eventType.getEventName());
        if (actionDefsElement == null) {
            return;
        }

        @SuppressWarnings("unchecked")
        Class<T> eventArgType = (Class<T>) eventType.getArgumentType();

        Function<GameEvents, ? extends GameActionEventsRegistry<T>> actionEventListenersGetter =
            (gameEvents) -> gameEvents.simpleListeners(eventType);

        Consumer<GameEventBasedActionDef<Self, T>> actionDefAdder =
            (actionDef) -> builder.addSimpleEventDef(eventType, actionDef);

        parseActionDefs(
            eventArgType, actionDefsElement, actionEventListenersGetter,
            actionDefAdder, eventType.getGlobalFilter()
        );
    }

    /**
     * Parses the given {@link JsonTree} to {@link GameEventBasedActionDef}(s) by using
     * {@link #tryParseActionDef(Class, JsonTree, Function, GameEventFilter)} and consume the parsed result with the
     * given {@code actionDefAdder}.
     */
    private <T> void parseActionDefs(
        Class<T> targetType,
        JsonTree actionDefsElement,
        Function<GameEvents, ? extends GameActionEventsRegistry<T>> actionEventListenersGetter,
        Consumer<GameEventBasedActionDef<Self, T>> actionDefAdder,
        GameEventFilter<? super Self, ? super T> globalFilter) throws ObjectParsingException {

        if (actionDefsElement == null) {
            return;
        }

        if (actionDefsElement.isJsonArray()) {
            for (JsonTree singleActionDefElement : actionDefsElement.getChildren()) {
                GameEventBasedActionDef<Self, T> actionDef
                    = tryParseActionDef(targetType, singleActionDefElement, actionEventListenersGetter, globalFilter);
                if (actionDef != null) {
                    actionDefAdder.accept(actionDef);
                }
            }
        } else {
            GameEventBasedActionDef<Self, T> actionDef
                = tryParseActionDef(targetType, actionDefsElement, actionEventListenersGetter, globalFilter);
            if (actionDef != null) {
                actionDefAdder.accept(actionDef);
            }
        }
    }

    /**
     * Parses the given {@link JsonTree} to a {@link GameEventBasedActionDef} of the {@code selfType} of this
     * {@code EventNotificationParser} and the given target type.
     */
    private <T> GameEventBasedActionDef<Self, T> tryParseActionDef(
        Class<T> targetType,
        JsonTree actionDefElement,
        Function<GameEvents, ? extends GameActionEventsRegistry<T>> actionEventListenersGetter,
        GameEventFilter<? super Self, ? super T> globalFilter) throws ObjectParsingException {

        if (actionDefElement == null) {
            return null;
        }

        if (!actionDefElement.isJsonObject()) {
            throw new ObjectParsingException("GameEventBasedActionDef requires a JsonObject.");
        }

        GameEventFilter<? super Self, ? super T> baseFilter = parseFilter(targetType, actionDefElement.getChild("filter"));
        GameEventAction<? super Self, ? super T> action = parseAction(targetType, actionDefElement.getChild("action"));

        GameEventFilter<? super Self, ? super T> filter;
        if (globalFilter != null) {
            filter = (game, self, arg) -> baseFilter.applies(game, self, arg) && globalFilter.applies(game, self, arg);
        } else {
            filter = baseFilter;
        }

        JsonTree triggerOnceElement = actionDefElement.getChild("triggerOnce");
        boolean triggerOnce = triggerOnceElement != null && triggerOnceElement.getAsBoolean();

        JsonTree lazyFilterElement = actionDefElement.getChild("lazyFilter");
        boolean lazyFilter = lazyFilterElement != null && lazyFilterElement.getAsBoolean();

        int priority = getPriority(actionDefElement);

        return new GameEventBasedActionDef<>(lazyFilter, triggerOnce, priority, actionEventListenersGetter, filter, action);
    }

    /**
     * Parses the given {@link JsonTree} to an {@link GameEventFilter} of the {@code selfType} of this
     * {@code EventNotificationParser} and the given target type. The global filter of this
     * {@code EventNotificationParser} will be added to the converted filter before it is returned.
     *
     * @param targetType the given target type.
     * @param filterElement the given {@code JsonTree}.
     * @param <T> the given target type.
     * @return the converted result.
     * @throws ObjectParsingException if failed to convert the given {@code JsonTree}.
     */
    public <T> GameEventFilter<? super Self, ? super T> parseFilter(
        Class<T> targetType,
        JsonTree filterElement) throws ObjectParsingException {
        if (filterElement == null) {
            return globalFilter;
        }

        // This is not safe at all but there is nothing we can do about it.
        @SuppressWarnings("unchecked")
        GameEventFilter<? super Self, ? super T> result = (GameEventFilter<? super Self, ? super T>) objectParser
            .toJavaObject(filterElement, GameEventFilter.class, eventFilterTypeChecker(targetType));
          // checks the actual type arguments of the generated GameEventFilter actually super `Self` and `T`
        if (globalFilter == GameEventFilters.ANY) {
            return result;
        }

        return (Game game, Self owner, T eventSource) -> {
            return globalFilter.applies(game, owner, eventSource)
                && result.applies(game, owner, eventSource);
        };
    }

    /**
     * Parses the given {@link JsonTree} to an {@link GameEventAction} of the {@code selfType} of this
     * {@code EventNotificationParser} and the given target type. The action finalizer of this
     * {@code EventNotificationParser} will be appended to the converted action before it is returned.
     *
     * @param targetType the given target type.
     * @param actionElement the given {@code JsonTree}.
     * @param <T> the given target type.
     * @return the converted result.
     * @throws ObjectParsingException if failed to convert the given {@code JsonTree}.
     */
    public <T> GameEventAction<? super Self, ? super T> parseAction(
        Class<T> targetType,
        JsonTree actionElement) throws ObjectParsingException {
        if (actionElement == null) {
            throw new ObjectParsingException("Missing action definition.");
        }

        GameEventAction<?, ?> resultObj = objectParser.toJavaObject(
            actionElement,
            GameEventAction.class,
            actionFilterTypeChecker(targetType)
            // checks the actual type arguments of the generated GameEventAction actually super `Self` and `T`
        );
        return toFinalizedAction(unsafeCastToEventAction(resultObj));
    }

    /**
     * Returns a {@link TypeChecker} which checks if it is assignable from the actual type arguments of the testing
     * generic type to the {@code selfType} of this {@code EventNotificationParser} and the given target type when
     * the raw type of the testing generic type is {@link GameEventFilter}.
     */
    private TypeChecker eventFilterTypeChecker(Class<?> targetType) {
        return TypeCheckers.genericTypeChecker(GameEventFilter.class, selfType, targetType);
    }

    /**
     * Returns a {@link TypeChecker} which checks if it is assignable from the actual type arguments of the testing
     * generic type to the {@code selfType} of this {@code EventNotificationParser} and the given target type when
     * the raw type of the testing generic type is {@link GameEventAction}.
     */
    private TypeChecker actionFilterTypeChecker(Class<?> targetType) {
        return TypeCheckers.genericTypeChecker(GameEventAction.class, selfType, targetType);
    }

    // This is not safe at all but there is nothing we can do about it.
    @SuppressWarnings("unchecked")
    private <T> GameEventAction<? super Self, ? super T> unsafeCastToEventAction(Object obj) {
        return (GameEventAction<? super Self, ? super T>) obj;
    }

    /**
     * Appends the action finalizer of this {@code EventNotificationParser} to the given action and returns.
     */
    private <T> GameEventAction<? super Self, ? super T> toFinalizedAction(GameEventAction<? super Self, ? super T> action) {
        if (actionFinalizer == DO_NOTHING) {
            return action;
        }

        return (Game game, Self self, T eventSource) -> {
            UndoAction actionUndo = action.alterGame(game, self, eventSource);
            UndoAction finalizeUndo = actionFinalizer.alterGame(game, self, eventSource);
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
        GameEventActionDefs.Builder<Self> result) throws ObjectParsingException {

        if (actionDefElement == null) {
            return;
        }

        GameEventFilter<? super Self, ? super Minion> filter = parseFilter(Minion.class, actionDefElement.getChild("filter"));
        GameEventAction<? super Self, ? super Minion> action = parseAction(Minion.class, actionDefElement.getChild("action"));

        JsonTree triggerOnceElement = actionDefElement.getChild("triggerOnce");
        boolean triggerOnce = triggerOnceElement != null ? triggerOnceElement.getAsBoolean() : false;

        int priority = getPriority(actionDefElement);

        CompletableGameEventAction<Self, Minion> eventDef = (Game game, Self self, Minion eventSource) -> {
            if (self == eventSource) {
                return CompleteGameEventAction.doNothing(UndoAction.DO_NOTHING);
            }

            if (filter.applies(game, self, eventSource)) {
                UndoAction alterGame = action.alterGame(game, self, eventSource);
                return CompleteGameEventAction.doNothing(alterGame);
            }

            return CompleteGameEventAction.nothingToUndo((completeGame, completeSelf, completeEventSource) -> {
                if (filter.applies(game, self, eventSource)) {
                    return action.alterGame(game, self, eventSource);
                } else {
                    return UndoAction.DO_NOTHING;
                }
            });
        };

        result.addOnSummoningActionDef(new CompletableGameEventBasedActionDef<>(triggerOnce, priority, GameEvents::summoningListeners, eventDef));
    }

    private void parseOnSummonEvents(
        JsonTree actionDefsElement,
        GameEventActionDefs.Builder<Self> result) throws ObjectParsingException {
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
