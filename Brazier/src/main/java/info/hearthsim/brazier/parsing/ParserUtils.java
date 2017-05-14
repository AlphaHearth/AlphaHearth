package info.hearthsim.brazier.parsing;

import com.google.gson.*;
import info.hearthsim.brazier.PlayerProperty;
import info.hearthsim.brazier.abilities.*;
import info.hearthsim.brazier.actions.*;
import info.hearthsim.brazier.db.CardDescr;
import info.hearthsim.brazier.db.HearthStoneDb;
import info.hearthsim.brazier.db.MinionDescr;
import info.hearthsim.brazier.db.WeaponDescr;
import info.hearthsim.brazier.events.EventAction;
import info.hearthsim.brazier.events.EventFilter;
import info.hearthsim.brazier.events.SimpleEventType;
import info.hearthsim.brazier.events.TriggeringAbility;
import info.hearthsim.brazier.game.Character;
import info.hearthsim.brazier.game.*;
import info.hearthsim.brazier.game.cards.CardName;
import info.hearthsim.brazier.game.cards.CardProvider;
import info.hearthsim.brazier.game.cards.PlayAction;
import info.hearthsim.brazier.game.minions.Minion;
import info.hearthsim.brazier.game.minions.MinionName;
import info.hearthsim.brazier.game.minions.MinionProvider;
import info.hearthsim.brazier.game.weapons.WeaponName;
import info.hearthsim.brazier.game.weapons.WeaponProvider;
import org.jtrim.utils.ExceptionHelper;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Util class with static methods for parsing {@link JsonTree}.
 */
public final class ParserUtils {

    /**
     * The default packages for {@link #resolveClassName(String)} method to resolve an unqualified class name.
     */
    private static final String[] DEFAULT_PACKAGES = {
        "info.hearthsim.brazier.actions",
        "info.hearthsim.brazier.abilities",
        "info.hearthsim.brazier.events",
    };

    /**
     * Creates a default {@link JsonDeserializer} with the given {@link Supplier} of {@link HearthStoneDb}.
     */
    public static JsonDeserializer createDefaultDeserializer(Supplier<HearthStoneDb> dbRef) {
        ExceptionHelper.checkNotNullArgument(dbRef, "dbRef");

        JsonDeserializer.Builder builder = new JsonDeserializer.Builder(ParserUtils::resolveClassName);

        addCustomStringParsers(dbRef, builder);
        addTypeConversions(builder);
        addTypeMergers(builder);

        return builder.create();
    }

    /**
     * Adds default type mergers to the given {@link JsonDeserializer.Builder}.
     */
    private static void addTypeMergers(JsonDeserializer.Builder builder) {
        builder.setTypeMerger(PlayActionRequirement.class, (elements) -> PlayActionRequirement.merge(elements));
        builder.setTypeMerger(EventFilter.class, (gameEventFilters) -> {
            // Unsafe but there is nothing to do.
            @SuppressWarnings("unchecked")
            Collection<? extends EventFilter<GameProperty, Object>> unsafeElements
                = (Collection<? extends EventFilter<GameProperty, Object>>) gameEventFilters;
            return EventFilter.merge(unsafeElements);
        });
        builder.setTypeMerger(EventAction.class, (gameEventActions) -> {
            // Unsafe but there is nothing to do.
            @SuppressWarnings("unchecked")
            Collection<? extends EventAction<PlayerProperty, Object>> unsafeElements
                = (Collection<? extends EventAction<PlayerProperty, Object>>) gameEventActions;
            return EventAction.merge(unsafeElements);
        });

        builder.setTypeMerger(TargetNeed.class, (targetNeeds) -> {
            // Unsafe but there is nothing to do.
            TargetNeed mergedNeed = TargetNeeds.NO_NEED;
            for (TargetNeed need : targetNeeds) {
                mergedNeed = mergedNeed.combine(need);
            }
            return mergedNeed;
        });
        builder.setTypeMerger(Ability.class, (activatableAbilities) -> {
            // Unsafe but there is nothing to do.
            @SuppressWarnings("unchecked")
            Collection<? extends Ability<Entity>> unsafeElements
                = (Collection<? extends Ability<Entity>>) activatableAbilities;
            return Ability.merge(unsafeElements);
        });
        builder.setTypeMerger(Aura.class, (auras) -> {
            // Unsafe but there is nothing to do.
            @SuppressWarnings("unchecked")
            Collection<? extends Aura<? super Entity, ? super Entity>> unsafeElements =
                (Collection<? extends Aura<? super Entity, ? super Entity>>) auras;
            return Aura.merge(unsafeElements);
        });
        builder.setTypeMerger(AuraFilter.class, (auraFilters) -> {
            // Unsafe but there is nothing to do.
            @SuppressWarnings("unchecked")
            Collection<? extends AuraFilter<Object, Object>> unsafeElements
                = (Collection<? extends AuraFilter<Object, Object>>) auraFilters;
            return AuraFilter.merge(unsafeElements);
        });
        builder.setTypeMerger(GameObjectAction.class, (gameObjectActions) -> {
            // Unsafe but there is nothing to do.
            @SuppressWarnings("unchecked")
            Collection<? extends GameObjectAction<GameProperty>> unsafeElements
                = (Collection<? extends GameObjectAction<GameProperty>>) gameObjectActions;
            return GameObjectAction.merge(unsafeElements);
        });
        builder.setTypeMerger(TargetedAction.class, (targetedActions) -> {
            // Unsafe but there is nothing to do.
            @SuppressWarnings("unchecked")
            Collection<? extends TargetedAction<Object, Object>> unsafeElements
                = (Collection<? extends TargetedAction<Object, Object>>) targetedActions;
            return TargetedAction.merge(unsafeElements);
        });
        builder.setTypeMerger(TargetlessAction.class, (targetlessActions) -> {
            // Unsafe but there is nothing to do.
            @SuppressWarnings("unchecked")
            Collection<? extends TargetlessAction<GameProperty>> unsafeElements
                = (Collection<? extends TargetlessAction<GameProperty>>) targetlessActions;
            return TargetlessAction.merge(unsafeElements);
        });
        builder.setTypeMerger(EntityFilter.class, (entityFilters) -> {
            // Unsafe but there is nothing to do.
            @SuppressWarnings("unchecked")
            Collection<? extends EntityFilter<Object>> unsafeElements
                = (Collection<? extends EntityFilter<Object>>) entityFilters;
            return EntityFilter.merge(unsafeElements);
        });
        builder.setTypeMerger(TargetedActionCondition.class, (targetedActionConditions) -> {
            // Unsafe but there is nothing to do.
            @SuppressWarnings("unchecked")
            Collection<? extends TargetedActionCondition<Object, Object>> unsafeElements
                = (Collection<? extends TargetedActionCondition<Object, Object>>) targetedActionConditions;
            return TargetedActionCondition.merge(unsafeElements);
        });
        builder.setTypeMerger(Predicate.class, (predicates) -> {
            // Unsafe but there is nothing to do.
            @SuppressWarnings("unchecked")
            Collection<? extends Predicate<Object>> unsafeElements
                = (Collection<? extends Predicate<Object>>) predicates;
            return mergePredicates(unsafeElements);
        });
        builder.setTypeMerger(TargetedEntitySelector.class, (targetedEntitySelectors) -> {
            // Unsafe but there is nothing to do.
            @SuppressWarnings("unchecked")
            Collection<? extends TargetedEntitySelector<GameProperty, Object, Object>> unsafeElements
                = (Collection<? extends TargetedEntitySelector<GameProperty, Object, Object>>) targetedEntitySelectors;
            return TargetedEntitySelector.merge(unsafeElements);
        });
        builder.setTypeMerger(EntitySelector.class, (entitySelectors) -> {
            // Unsafe but there is nothing to do.
            @SuppressWarnings("unchecked")
            Collection<? extends EntitySelector<GameProperty, Object>> unsafeElements
                = (Collection<? extends EntitySelector<GameProperty, Object>>) entitySelectors;
            return EntitySelector.merge(unsafeElements);
        });
        builder.setTypeMerger(Buff.class, (buffs) -> {
            // Unsafe but there is nothing to do.
            @SuppressWarnings("unchecked")
            Collection<? extends Buff<Entity>> unsafeElements
                = (Collection<? extends Buff<Entity>>) buffs;
            return Buff.merge(unsafeElements);
        });
        builder.setTypeMerger(PermanentBuff.class, (permanentBuffs) -> {
            // Unsafe but there is nothing to do.
            @SuppressWarnings("unchecked")
            Collection<? extends PermanentBuff<Entity>> unsafeElements
                = (Collection<? extends PermanentBuff<Entity>>) permanentBuffs;
            return PermanentBuff.merge(unsafeElements);
        });
    }

    /**
     * Adds default type converters to the given {@link JsonDeserializer.Builder}.
     */
    private static void addTypeConversions(JsonDeserializer.Builder builder) {
        builder.addTypeConversion(GameAction.class, EventAction.class,
            (action) -> (self, eventSource) -> action.apply(self.getGame()));
        builder.addTypeConversion(TargetlessAction.class, EventAction.class, (action) -> {
            // Not truly safe but there is not much to do.
            // The true requirement is that the "Actor" extends the "Self" object of
            // EventAction.
            @SuppressWarnings("unchecked")
            TargetlessAction<PlayerProperty> safeAction = (TargetlessAction<PlayerProperty>) action;
            return (PlayerProperty self, Object eventSource) -> safeAction.apply(self);
        });

        builder.addTypeConversion(TargetedAction.class, TargetlessAction.class, (action) -> {
            // Not truly safe but there is not much to do.
            // The true requirement is that the "Actor" extends the "Self" object of
            // EventAction.
            @SuppressWarnings("unchecked")
            TargetedAction<Object, Object> safeAction = (TargetedAction<Object, Object>) action;
            return (GameProperty actor) -> safeAction.apply(actor, actor);
        });

        builder.addTypeConversion(Predicate.class, EntityFilter.class, (predicate) -> {
            // Not truly safe but there is not much to do.
            @SuppressWarnings("unchecked")
            Predicate<Object> safePredicate = (Predicate<Object>) predicate;
            return EntityFilters.fromPredicate(safePredicate);
        });

        builder.addTypeConversion(TargetlessAction.class, TargetedAction.class,
            (action) -> action.toTargetedAction());

        builder.addTypeConversion(EntitySelector.class, TargetedEntitySelector.class,
            (action) -> action.toTargeted());

        builder.addTypeConversion(Buff.class, PermanentBuff.class,
            (action) -> action.toPermanent());
    }

    /**
     * Adds default {@link JsonDeserializer.CustomStringParser} to the given {@link JsonDeserializer.Builder}.
     */
    private static void addCustomStringParsers(Supplier<HearthStoneDb> dbRef, JsonDeserializer.Builder result) {
        result.setCustomStringParser(CardProvider.class, (str) -> toCardProvider(dbRef, str));
        result.setCustomStringParser(MinionProvider.class, (str) -> toMinionProvider(dbRef, str));
        result.setCustomStringParser(WeaponProvider.class, (str) -> toWeaponProvider(dbRef, str));
        result.setCustomStringParser(TargetNeed.class, ParserUtils::toTargetNeed);
        result.setCustomStringParser(Keyword.class, (str) -> Keyword.create(str));
        result.setCustomStringParser(CardName.class, (str) -> new CardName(str));
        result.setCustomStringParser(MinionName.class, (str) -> new MinionName(str));
        result.setCustomStringParser(WeaponName.class, (str) -> new WeaponName(str));
        result.setCustomStringParser(SimpleEventType.class, SimpleEventType::tryParse);

        result.setCustomStringParser(Buff.class, (String str) -> {
            BuffDescr buffDescr = BuffDescr.tryCreate(str);
            return buffDescr != null ? Buffs.temporaryBuff(buffDescr.attack, buffDescr.hp) : null;
        });
        result.setCustomStringParser(PermanentBuff.class, (String str) -> {
            BuffDescr buffDescr = BuffDescr.tryCreate(str);
            return buffDescr != null ? Buffs.buff(buffDescr.attack, buffDescr.hp) : null;
        });
    }

    // TODO Refactor this hardcoded parsing rules
    private static TargetNeed toTargetNeed(String str) throws ObjectParsingException {
        String normNeedStr = str.toLowerCase(Locale.ROOT);
        switch (normNeedStr) {
            case "all-heroes":
                return TargetNeeds.ALL_HEROES;
            case "all-minions":
                return TargetNeeds.ALL_MINIONS;
            case "friendly-minions":
                return TargetNeeds.FRIENDLY_MINIONS;
            case "enemy-minions":
                return TargetNeeds.ENEMY_MINIONS;
            case "all":
                return TargetNeeds.ALL_TARGETS;
            case "friendly":
                return TargetNeeds.FRIENDLY_TARGETS;
            case "enemy":
                return TargetNeeds.ENEMY_TARGETS;
            default:
                return null;
        }
    }

    private static CardProvider toCardProvider(Supplier<HearthStoneDb> dbRef, String cardId) {
        AtomicReference<CardDescr> cache = new AtomicReference<>(null);
        return () -> {
            CardDescr result = cache.get();
            if (result == null) {
                HearthStoneDb db = Objects.requireNonNull(dbRef.get(), "HearthStoneDb");
                result = db.getCardDb().getById(new CardName(cardId));
                if (!cache.compareAndSet(null, result)) {
                    result = cache.get();
                }
            }
            return result;
        };
    }

    private static MinionProvider toMinionProvider(Supplier<HearthStoneDb> dbRef, String minionId) {
        AtomicReference<MinionDescr> cache = new AtomicReference<>(null);
        return () -> {
            MinionDescr result = cache.get();
            if (result == null) {
                HearthStoneDb db = Objects.requireNonNull(dbRef.get(), "HearthStoneDb");
                result = db.getMinionDb().getById(new MinionName(minionId));
                if (!cache.compareAndSet(null, result)) {
                    result = cache.get();
                }
            }
            return result;
        };
    }

    private static WeaponProvider toWeaponProvider(Supplier<HearthStoneDb> dbRef, String weaponId) {
        AtomicReference<WeaponDescr> cache = new AtomicReference<>(null);
        return () -> {
            WeaponDescr result = cache.get();
            if (result == null) {
                HearthStoneDb db = Objects.requireNonNull(dbRef.get(), "HearthStoneDb");
                result = db.getWeaponDb().getById(new WeaponName(weaponId));
                if (!cache.compareAndSet(null, result)) {
                    result = cache.get();
                }
            }
            return result;
        };
    }

    /**
     * Tries to resolve the given unqualified class name by adding each package from {@link #DEFAULT_PACKAGES} before
     * it.
     *
     * @throws ObjectParsingException if failed to find such class from {@code DEFAULT_PACKAGES}.
     */
    private static Class<?> resolveClassName(String unqualifiedClassName) throws ObjectParsingException {
        for (String packageName: DEFAULT_PACKAGES) {
            try {
                return Class.forName(packageName + '.' + unqualifiedClassName);
            } catch (ClassNotFoundException ex) {
                // Ignore and try another class.
            }
        }
        throw new ObjectParsingException("Cannot resolve class name: " + unqualifiedClassName);
    }

    public static JsonObject fromJsonFile(Path file) throws IOException {
        JsonParser jsonParser = new JsonParser();
        try (Reader inputReader = Files.newBufferedReader(file, Charset.forName("UTF-8"))) {
            JsonElement result = jsonParser.parse(inputReader);
            if (result.isJsonObject()) {
                return result.getAsJsonObject();
            }
            else {
                throw new IOException("The JsonElement is not an object in " + file);
            }
        }
    }

    /**
     * Parses the given {@link JsonTree} to {@link Keyword}s.
     * <p>
     * The given {@code JsonTree} will be treated as a {@link JsonPrimitive} or {@link JsonArray} of {@code String}(s)
     * and its value or elements will be parsed to {@code Keyword}(s) by using {@link Keyword#create(String)}.
     */
    public static Set<Keyword> parseKeywords(JsonTree keywordsElement) {
        Set<Keyword> keywords = new HashSet<>();
        ParserUtils.parseKeywords(keywordsElement, keywords::add);
        return keywords;
    }

    /**
     * Parses the given {@link JsonTree} to {@link Keyword}s and consumes them with the given {@link Consumer}.
     * <p>
     * The given {@code JsonTree} will be treated as a {@link JsonPrimitive} or {@link JsonArray} of {@code String}(s)
     * and its value or elements will be parsed to {@code Keyword}(s) by using {@link Keyword#create(String)}.
     */
    public static void parseKeywords(JsonTree keywords, Consumer<? super Keyword> keywordAdder) {
        ExceptionHelper.checkNotNullArgument(keywords, "keywords");
        ExceptionHelper.checkNotNullArgument(keywordAdder, "keywordAdder");

        if (keywords.isJsonPrimitive()) {
            keywordAdder.accept(Keyword.create(keywords.getAsString()));
        }
        else if (keywords.isJsonArray()) {
            for (JsonTree keywordElement: keywords.getChildren()) {
                keywordAdder.accept(Keyword.create(keywordElement.getAsString()));
            }
        }
    }

    /**
     * Gets the designated {@code String} field from the given {@link JsonTree}, and throws
     * {@link ObjectParsingException} if there is no such field.
     *
     * @param tree the given {@link JsonTree}.
     * @param fieldName the name of the field.
     * @return the value of the designated field.
     * @throws ObjectParsingException if there is no such field.
     */
    public static String getStringField(JsonTree tree, String fieldName) throws ObjectParsingException {
        String result = tryGetStringField(tree, fieldName);
        if (result == null) {
            throw new ObjectParsingException("Missing required field: " + fieldName);
        }
        return result;
    }

    /**
     * Gets the designated {@code String} field from the given {@link JsonTree}.
     *
     * @param tree the given {@link JsonTree}.
     * @param fieldName the name of the field.
     * @return the value of the designated field; {@code null} if there is no such field.
     */
    public static String tryGetStringField(JsonTree tree, String fieldName) {
        ExceptionHelper.checkNotNullArgument(tree, "tree");
        ExceptionHelper.checkNotNullArgument(fieldName, "fieldName");

        JsonTree fieldValue = tree.getChild(fieldName);
        return fieldValue != null ? fieldValue.getAsString() : null;
    }

    /**
     * Gets the designated {@code int} field from the given {@link JsonTree}, and throws
     * {@link ObjectParsingException} if there is no such field.
     *
     * @param tree the given {@link JsonTree}.
     * @param fieldName the name of the field.
     * @return the value of the designated field.
     * @throws ObjectParsingException if there is no such field.
     */
    public static int getIntField(JsonTree tree, String fieldName) throws ObjectParsingException {
        JsonTree fieldValue = tree.getChild(fieldName);
        if (fieldValue == null) {
            throw new ObjectParsingException("Missing required field: " + fieldName);
        }

        return fieldValue.getAsInt();
    }

    /**
     * Parses the {@code targets} element of the given {@code action} Json element,
     * and converts it to the respective {@link TargetNeed}.
     */
    public static TargetNeed getTargetNeedOfAction(
            JsonDeserializer objectParser,
            JsonTree actionElement) throws ObjectParsingException {
        ExceptionHelper.checkNotNullArgument(objectParser, "objectParser");
        ExceptionHelper.checkNotNullArgument(actionElement, "actionElement");

        JsonTree needsElement = actionElement.getChild("targets");
        if (needsElement != null) {
            return objectParser.toJavaObject(needsElement, TargetNeed.class);
        }

        return TargetNeeds.NO_NEED;
    }

    public static PlayActionRequirement getPlayRequirement(
            JsonDeserializer objectParser,
            JsonTree requiresElement) throws ObjectParsingException {
        ExceptionHelper.checkNotNullArgument(objectParser, "objectParser");
        ExceptionHelper.checkNotNullArgument(requiresElement, "requiresElement");

        return objectParser.toJavaObject(requiresElement, PlayActionRequirement.class);
    }

    /**
     * Parses the {@code requires} element of the given {@code action} Json element
     * and converts it to the respective {@link PlayActionRequirement}.
     */
    public static PlayActionRequirement getPlayRequirementOfAction(
            JsonDeserializer objectParser,
            JsonTree actionElement) throws ObjectParsingException {
        ExceptionHelper.checkNotNullArgument(objectParser, "objectParser");
        ExceptionHelper.checkNotNullArgument(actionElement, "actionElement");

        JsonTree requiresElement = actionElement.getChild("requires");
        return requiresElement != null
                ?getPlayRequirement(objectParser, requiresElement)
                : PlayActionRequirement.ALLOWED;
    }

    /**
     * Parses the {@code actionCondition} element of the given {@code action} Json element
     * and converts it to the respective {@link PlayActionRequirement}.
     */
    public static PlayActionRequirement getActionConditionOfAction(
            JsonDeserializer objectParser,
            JsonTree actionElement) throws ObjectParsingException {
        ExceptionHelper.checkNotNullArgument(objectParser, "objectParser");
        ExceptionHelper.checkNotNullArgument(actionElement, "actionElement");

        JsonTree requiresElement = actionElement.getChild("actionCondition");
        return requiresElement != null
                ?getPlayRequirement(objectParser, requiresElement)
                : PlayActionRequirement.ALLOWED;
    }

    /**
     * Parses the given triggers Json element to the respective {@link TriggeringAbility}.
     */
    private static <Self extends Entity> TriggeringAbility<Self> parseEventActionDefs(
            EventNotificationParser<Self> eventNotificationParser,
            JsonTree triggersElement) throws ObjectParsingException {
        if (triggersElement == null) {
            return new TriggeringAbility.Builder<Self>().create();
        }

        return eventNotificationParser.fromJson(triggersElement);
    }

    /**
     * Parses the given {@code ability} Json element to the respective {@link Ability} object.
     *
     * @see JsonDeserializer#toJavaObject(JsonTree, Class, TypeChecker)
     */
    private static <Self> Ability<? super Self> parseAbility(
            Class<Self> selfClass,
            JsonDeserializer objectParser,
            JsonTree abilityElement) throws ObjectParsingException {
        if (abilityElement == null) {
            return null;
        }

        // Unsafe but there is nothing we can do about it.
        @SuppressWarnings("unchecked")
        Ability<? super Self> ability = (Ability<? super Self>)objectParser.toJavaObject(
                abilityElement,
                Ability.class,
                TypeCheckers.genericTypeChecker(Ability.class, selfClass));
        return ability;
    }

    /**
     * Parses the {@code deathRattle} element of the given entity (minion or weapon) Json element
     * and converts it to the respective {@link EventAction}.
     */
    private static <Self extends Entity> EventAction<? super Self, ? super Self> parseDeathRattle(
            Class<Self> selfClass,
            EventNotificationParser<Self> eventNotificationParser,
            JsonTree entityElement) throws ObjectParsingException {

        JsonTree deathRattleElement = entityElement.getChild("deathRattle");
        if (deathRattleElement == null) {
            return null;
        }

        JsonTree deathRattleConditionElement = entityElement.getChild("deathRattleCondition");
        EventFilter<? super Self, ? super Self> deathRattleFilter = deathRattleConditionElement != null
                ? eventNotificationParser.parseFilter(selfClass, deathRattleConditionElement)
                : null;

        EventAction<? super Self, ? super Self> action
                = eventNotificationParser.parseAction(selfClass, deathRattleElement);

        if (deathRattleFilter != null) {
            return (Self self, Self eventSource) -> {
                if (deathRattleFilter.applies(self, eventSource))
                    action.trigger(self, eventSource);
            };
        }
        else {
            return action;
        }
    }

    /**
     * Parses the given entity (minion or weapon) Json element to the respective
     * {@link LivingEntitiesAbilities} object.
     */
    public static <Self extends Entity> LivingEntitiesAbilities<Self> parseAbilities(
            Class<Self> selfClass,
            JsonDeserializer objectParser,
            EventNotificationParser<Self> eventNotificationParser,
            JsonTree entityElement) throws ObjectParsingException {

        Ability<? super Self> ability = parseAbility(selfClass, objectParser, entityElement.getChild("ability"));
        TriggeringAbility<Self> eventActionDefs =
            parseEventActionDefs(eventNotificationParser, entityElement.getChild("triggers"));
        EventAction<? super Self, ? super Self> deathRattle =
            parseDeathRattle(selfClass, eventNotificationParser, entityElement);

        return new LivingEntitiesAbilities<>(ability, eventActionDefs, deathRattle);
    }

    public static <T> Predicate<T> mergePredicates(
            Collection<? extends Predicate<T>> filters) {
        List<Predicate<T>> filtersCopy = new ArrayList<>(filters);
        ExceptionHelper.checkNotNullElements(filtersCopy, "filters");

        int count = filtersCopy.size();
        if (count == 0) {
            return (arg) -> true;
        }
        if (count == 1) {
            return filtersCopy.get(0);
        }

        return (T arg) -> {
            for (Predicate<T> filter: filtersCopy) {
                if (!filter.test(arg)) {
                    return false;
                }
            }
            return true;
        };
    }

    private static <Actor extends GameProperty, Target> PlayAction<Actor> parseTargetedAction(
            JsonDeserializer objectParser,
            JsonTree actionElement,
            Class<Actor> actorType,
            Class<Target> targetType) throws ObjectParsingException {
        @SuppressWarnings("unchecked")
        TargetedAction<? super Actor, ? super Target> result = objectParser.toJavaObject(
            actionElement,
            TargetedAction.class,
            TypeCheckers.genericTypeChecker(TargetedAction.class, actorType, targetType));
        return (Actor actor, Optional<Character> optTarget) -> {
            if (!optTarget.isPresent())
                return;

            Character target = optTarget.get();
            if (targetType.isInstance(target))
                result.apply(actor, targetType.cast(target));
        };
    }

    private static <Actor extends GameProperty> PlayAction<Actor> parseTargetedActionRaw(
            JsonDeserializer objectParser,
            JsonTree actionElement,
            TargetNeed targetNeed,
            Class<Actor> actorType) throws ObjectParsingException {

        // FIXME: Remove these unreliable tests after no longer needed
        if (!targetNeed.mayTargetHero()) {
            if (!targetNeed.mayTargetMinion()) {
                @SuppressWarnings("unchecked")
                TargetlessAction<Actor> result = objectParser.toJavaObject(
                        actionElement,
                        TargetlessAction.class,
                        TypeCheckers.genericTypeChecker(TargetlessAction.class, actorType));
                return (actor, target) -> result.apply(actor);
            }
            else {
                return parseTargetedAction(objectParser, actionElement, actorType, Minion.class);
            }
        }
        else {
            if (!targetNeed.mayTargetMinion()) {
                return parseTargetedAction(objectParser, actionElement, actorType, Hero.class);
            }
            else {
                return parseTargetedAction(objectParser, actionElement, actorType, Character.class);
            }
        }
    }

    public static <Actor extends GameProperty> PlayAction<Actor> parseTargetedAction(
            JsonDeserializer objectParser,
            JsonTree actionElement,
            TargetNeed targetNeed,
            Class<Actor> actorType) throws ObjectParsingException {

        if (actionElement.isJsonObject() && actionElement.getChild("class") == null) {
            JsonTree actionsDefElement = actionElement.getChild("actions");
            if (actionsDefElement == null) {
                throw new ObjectParsingException("Missing action definition for CardPlayAction.");
            }
            return parseTargetedActionRaw(objectParser, actionsDefElement, targetNeed, actorType);
        }

        return parseTargetedActionRaw(objectParser, actionElement, targetNeed, actorType);
    }

    /**
     * Parses the given {@link JsonTree} as a single Json object defining one on-play action of a certain card
     * and converts it to the respective {@link PlayActionDef} with the given type of {@code Actor}.
     */
    public static <Actor extends PlayerProperty> PlayActionDef<Actor> parseSinglePlayActionDef(
            JsonDeserializer objectParser,
            JsonTree actionDef,
            Class<Actor> actorType) throws ObjectParsingException {

        TargetNeed targetNeed = getTargetNeedOfAction(objectParser, actionDef);
        PlayActionRequirement requirement = getPlayRequirementOfAction(objectParser, actionDef);
        PlayAction<Actor> action = parseTargetedAction(objectParser, actionDef, targetNeed, actorType);

        PlayActionRequirement actionCondition = getActionConditionOfAction(objectParser, actionDef);
        action = addCondition(actionCondition, action);

        return new PlayActionDef<>(targetNeed, requirement, action);
    }

    /**
     * Parses the given {@link JsonTree} as an Json element (array or object) defining the on-play action(s) of a
     * certain card with the given type of {@code Actor} to the respective {@link PlayActionDef} and consumes it with
     * the given consumer.
     */
    public static <Actor extends PlayerProperty> boolean parsePlayActionDefs(
            JsonDeserializer objectParser,
            JsonTree actionDefsElement,
            Class<Actor> actorType,
            Consumer<PlayActionDef<Actor>> actionDefConsumer) throws ObjectParsingException {

        if (actionDefsElement == null)
            return false;

        if (actionDefsElement.isJsonArray()) {
            for (JsonTree singleActionDefElement: actionDefsElement.getChildren()) {
                actionDefConsumer.accept(parseSinglePlayActionDef(objectParser, singleActionDefElement, actorType));
            }
            return actionDefsElement.getChildCount() > 0;
        }
        else {
            actionDefConsumer.accept(parseSinglePlayActionDef(objectParser, actionDefsElement, actorType));
            return true;
        }
    }

    /**
     * Adds the given {@link PlayActionRequirement} to the given {@link PlayAction} and returns.
     */
    private static <Actor extends PlayerProperty> PlayAction<Actor> addCondition(
            PlayActionRequirement condition,
            PlayAction<Actor> action) {
        if (condition == PlayActionRequirement.ALLOWED) {
            return action;
        }

        return (Actor actor, Optional<Character> target) -> {
            if (condition.meetsRequirement(actor.getOwner()))
                action.apply(actor, target);
        };
    }

    private static final class BuffDescr {
        public final int attack;
        public final int hp;

        public BuffDescr(int attack, int hp) {
            this.attack = attack;
            this.hp = hp;
        }

        public static BuffDescr tryCreate(String str) {
            String[] attackHp = str.split("/");
            if (attackHp.length != 2) {
                return null;
            }

            try {
                int attack = Integer.parseInt(attackHp[0].trim());
                int hp = Integer.parseInt(attackHp[1].trim());
                return new BuffDescr(attack, hp);
            } catch (NumberFormatException ex) {
                return null;
            }
        }
    }

    private ParserUtils() {
        throw new AssertionError();
    }
}
