package info.hearthsim.brazier.parsing;

import info.hearthsim.brazier.*;
import info.hearthsim.brazier.abilities.Ability;
import info.hearthsim.brazier.actions.*;
import info.hearthsim.brazier.cards.CardProvider;
import info.hearthsim.brazier.cards.CardRarity;
import info.hearthsim.brazier.cards.CardType;
import info.hearthsim.brazier.minions.MinionDescr;
import info.hearthsim.brazier.Character;
import info.hearthsim.brazier.events.WorldEventFilters;
import info.hearthsim.brazier.actions.undo.UndoAction;
import info.hearthsim.brazier.cards.Card;
import info.hearthsim.brazier.cards.CardDescr;
import info.hearthsim.brazier.cards.CardId;
import info.hearthsim.brazier.cards.PlayAction;
import info.hearthsim.brazier.events.SimpleEventType;
import info.hearthsim.brazier.events.WorldEventActionDefs;
import info.hearthsim.brazier.weapons.WeaponDescr;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.jtrim.utils.ExceptionHelper;

public final class CardParser implements EntityParser<CardDescr> {
    private final JsonDeserializer objectParser;
    private final MinionParser minionParser;
    private final WeaponParser weaponParser;
    private final EventNotificationParser<Secret> secretParser;

    public CardParser(JsonDeserializer objectParser) {
        ExceptionHelper.checkNotNullArgument(objectParser, "objectParser");

        this.objectParser = objectParser;
        this.minionParser = new MinionParser(objectParser);
        this.weaponParser = new WeaponParser(objectParser);
        this.secretParser = new EventNotificationParser<>(
                Secret.class,
                objectParser,
                WorldEventFilters.NOT_SELF_TURN,
                CardParser::unregisterSecret);
    }

    /**
     * Parses the given {@link JsonTree} to a {@link CardDescr}.
     *
     * @param tree {@inheritDoc}
     * @return the parsed result as a {@code CardDescr}.
     * @throws ObjectParsingException {@inheritDoc}
     */
    @Override
    public CardDescr fromJson(JsonTree tree) throws ObjectParsingException {
        return fromJson(tree, null);
    }

    /**
     * Parses the given {@link JsonTree} to a {@link CardDescr} with the predefined {@link CardType}.
     *
     * @param tree the given {@code JsonTree}.
     * @return the parsed result as a {@code CardDescr} with the predefined {@link CardType}.
     * @throws ObjectParsingException if fails to parse the given {@code JsonTree}.
     */
    public CardDescr fromJson(JsonTree tree, CardType predefinedCardType) throws ObjectParsingException {
        String name = ParserUtils.getStringField(tree, "name");
        int manaCost = ParserUtils.getIntField(tree, "manaCost");
        Set<Keyword> keywords = new HashSet<>();
        CardType cardType = predefinedCardType != null
            ? predefinedCardType
            : parseCardType(tree.getChild("type"));

        JsonTree minionElement = tree.getChild("minion");
        if (minionElement != null && cardType == CardType.UNKNOWN)
            cardType = CardType.MINION;

        JsonTree weaponElement = tree.getChild("weapon");
        if (weaponElement != null) {
            if (cardType != CardType.UNKNOWN && cardType != CardType.WEAPON) {
                throw new ObjectParsingException("Weapon containing card cannot have this type: " + cardType);
            }
            cardType = CardType.WEAPON;
        }

        CardId cardId = new CardId(name);
        CardDescr.Builder builder = new CardDescr.Builder(cardId, cardType, manaCost);

        keywords.add(cardType.getKeyword());
        keywords.add(Keywords.manaCost(manaCost));

        String description = ParserUtils.tryGetStringField(tree, "description");
        if (description != null) {
            builder.setDescription(description);
        }

        keywords.add(isCollectible(tree.getChild("collectible"))
            ? Keywords.COLLECTIBLE
            : Keywords.NON_COLLECTIBLE);

        JsonTree keywordsElement = tree.getChild("keywords");
        if (keywordsElement != null) {
            ParserUtils.parseKeywords(keywordsElement, keywords::add);
        }

        JsonTree rarityElement = tree.getChild("rarity");
        CardRarity rarity = CardRarity.COMMON;
        if (rarityElement != null) {
            rarity = objectParser.toJavaObject(rarityElement, CardRarity.class);
        }

        builder.setRarity(rarity);
        keywords.add(Keyword.create(rarity.name()));

        JsonTree displayName = tree.getChild("displayName");
        if (displayName != null) {
            builder.setDisplayName(displayName.getAsString());
        }

        JsonTree overloadElement = tree.getChild("overload");
        if (overloadElement != null) {
            int overload = overloadElement.getAsInt();
            if (overload > 0) {
                keywords.add(Keywords.OVERLOAD);
            }
            builder.setOverload(overload);
        }

        JsonTree drawActions = tree.getChild("drawActions");
        if (drawActions != null) {
            @SuppressWarnings("unchecked")
            TargetlessAction<? super Card> onDrawAction = objectParser.toJavaObject(
                drawActions,
                TargetlessAction.class,
                TypeCheckers.genericTypeChecker(TargetlessAction.class, Card.class));
            builder.addOnDrawAction(onDrawAction);
        }

        JsonTree chooseOneElement = tree.getChild("chooseOne");
        if (chooseOneElement != null) {
            CardProvider[] choices = objectParser.toJavaObject(chooseOneElement, CardProvider[].class);
            for (CardProvider choice: choices) {
                builder.addChooseOneAction(choice);
            }
        }

        JsonTree classElement = tree.getChild("class");
        if (classElement == null) {
            throw new ObjectParsingException("Class of card is unspecified for " + name);
        }
        Keyword cardClass = Keyword.create(classElement.getAsString());
        builder.setCardClass(cardClass);
        keywords.add(cardClass);

        ParserUtils.parsePlayActionDefs(objectParser, tree.getChild("playActions"), Card.class, builder::addOnPlayAction);
        parseCardAdjusters(tree.getChild("manaCostAdjusters"), builder);

        AtomicReference<CardDescr> cardRef = new AtomicReference<>();
        if (parseSecretPlayAction(tree.getChild("secret"), cardId, cardRef::get, builder)) {
            keywords.add(Keywords.SECRET);
        }

        parseAbility(tree.getChild("inHandAbility"), builder);

        keywords.forEach(builder::addKeyword);
        // To ensure that we no longer add keywords from here on by accident.
        keywords = Collections.unmodifiableSet(keywords);


        if (minionElement == null && cardType == CardType.MINION) {
            throw new ObjectParsingException("Minion cards must have an explicit minion declaration.");
        }

        if (minionElement != null) {
            MinionDescr minion = minionParser.fromJson(minionElement, name, keywords, cardRef::get);
            builder.setMinion(minion);
        }

        if (weaponElement != null) {
            WeaponDescr weapon = weaponParser.fromJson(weaponElement, name, keywords);
            builder.setWeapon(weapon);
            PlayAction<Card> playAction = (World world, Card actor, Optional<Character> target) -> {
                return actor.getOwner().equipWeapon(weapon);
            };

            builder.addOnPlayAction(new PlayActionDef<>(TargetNeeds.NO_NEED, PlayActionRequirement.ALLOWED, playAction));
        }

        CardDescr card = builder.create();
        cardRef.set(card);
        return card;
    }

    /**
     * Removes the given {@link Secret} from the given {@link World} and triggers a
     * {@link SimpleEventType#SECRET_REVEALED SECRET_REVEALED} event.
     *
     * @param world the given {@code World}.
     * @param secret the given {@code Secret}.
     * @param eventSource the source of the event.
     */
    private static UndoAction unregisterSecret(World world, Secret secret, Object eventSource) {
        UndoAction removeUndo = secret.getOwner().getSecrets().removeSecret(secret);
        UndoAction eventUndo = world.getEvents().triggerEvent(SimpleEventType.SECRET_REVEALED, secret);
        return () -> {
            eventUndo.undo();
            removeUndo.undo();
        };
    }

    private void parseAbility(
            JsonTree abilityElement,
            CardDescr.Builder abilities) throws ObjectParsingException {
        if (abilityElement == null) {
            return;
        }

        // Unsafe but there is nothing we can do about it.
        @SuppressWarnings("unchecked")
        Ability<? super Card> ability = (Ability<? super Card>)objectParser.toJavaObject(
                abilityElement,
                Ability.class,
                TypeCheckers.genericTypeChecker(Ability.class, Card.class));
        abilities.setInHandAbility(ability);
    }

    /**
     * Returns the corresponding {@link CardType} of the given {@code String} {@link JsonTree},
     * and throws {@link ObjectParsingException} if the value of the {@code JsonTree} cannot be
     * parsed into a {@code CardType}.
     *
     * @param cardTypeElement the given {@code String} {@link JsonTree}.
     * @return the corresponding {@link CardType}
     * @throws ObjectParsingException if the value of the {@code JsonTree} cannot be
     *                                parsed into a {@code CardType}.
     */
    private static CardType parseCardType(JsonTree cardTypeElement) throws ObjectParsingException {
        if (cardTypeElement == null) {
            return CardType.UNKNOWN;
        }

        String cardTypeStr = cardTypeElement.getAsString().toUpperCase(Locale.ROOT);
        try {
            return CardType.valueOf(cardTypeStr);
        } catch (IllegalArgumentException ex) {
            throw new ObjectParsingException("Unknown card type: " + cardTypeElement.getAsString());
        }
    }

    /**
     * Returns if the given {@code boolean} {@link JsonTree} designating the card is collectible or not.
     *
     * @param collectibleElement the given {@code boolean} {@link JsonTree}.
     * @return the card is collectible or not.
     */
    private static boolean isCollectible(JsonTree collectibleElement) {
        return collectibleElement == null || collectibleElement.getAsBoolean();
    }

    private void parseSingleCardAdjuster(JsonTree cardAdjusters, CardDescr.Builder result) throws ObjectParsingException {
        ManaCostAdjuster adjuster = objectParser.toJavaObject(cardAdjusters, ManaCostAdjuster.class);
        result.addManaCostAdjuster(adjuster);
    }

    private void parseCardAdjusters(JsonTree cardAdjusters, CardDescr.Builder result) throws ObjectParsingException {
        if (cardAdjusters == null) {
            return;
        }

        if (cardAdjusters.isJsonArray()) {
            for (JsonTree singleCardAdjuster: cardAdjusters.getChildren()) {
                parseSingleCardAdjuster(singleCardAdjuster, result);
            }
        }
        else {
            parseSingleCardAdjuster(cardAdjusters, result);
        }
    }

    /**
     * Used in {@link #fromJson(JsonTree, CardType)} to parse a {@code Secret} card, which delegates the parsing
     * to an {@link EventNotificationParser}.
     *
     * @param secretElement the {@code Secret}'s {@link JsonTree}.
     * @param secretId the {@link EntityId} of the {@code Secret}.
     * @param cardRef a {@link Supplier} which returns the {@link CardDescr}.
     * @param builder the {@link CardDescr.Builder CardDescr.Builder} of the
     *                current {@code Secret} card.
     * @return {@code true} if the given {@code secretElement} is not {@code null}.
     * @throws ObjectParsingException if fails to parse the given {@code JsonTree}.
     */
    private boolean parseSecretPlayAction(
        JsonTree secretElement,
        EntityId secretId,
        Supplier<CardDescr> cardRef,
        CardDescr.Builder builder) throws ObjectParsingException {

        if (secretElement == null) {
            return false;
        }

        WorldEventActionDefs<Secret> secretActionDef = secretParser.fromJson(secretElement);

        builder.addOnPlayAction(new PlayActionDef<>(
            TargetNeeds.NO_NEED,
            secretRequirement(secretId),
            secretAction(cardRef, secretActionDef)));
        return true;
    }

    /**
     * Returns the {@link PlayActionRequirement} for playing the {@code Secret} with the given id.
     *
     * @param secretId the given {@link EntityId} of the {@code Secret}.
     * @return the {@link PlayActionRequirement} for playing the {@code Secret}.
     */
    private PlayActionRequirement secretRequirement(EntityId secretId) {
        return (Player player) -> {
            SecretContainer secrets = player.getSecrets();
            return !secrets.isFull() && secrets.findById(secretId) == null;
        };
    }

    private PlayAction<Card> secretAction(Supplier<CardDescr> cardRef, WorldEventActionDefs<Secret> secretActionDef) {
        ExceptionHelper.checkNotNullArgument(secretActionDef, "secretActionDef");
        return (World world, Card actor, Optional<info.hearthsim.brazier.Character> target) -> {
            CardDescr card = cardRef.get();
            Player player = actor.getOwner();
            Secret secret = new Secret(player, card, secretActionDef);
            return player.getSecrets().addSecret(secret);
        };
    }
}
