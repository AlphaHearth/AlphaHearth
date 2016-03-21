package info.hearthsim.brazier.parsing;

import info.hearthsim.brazier.Keyword;
import info.hearthsim.brazier.minions.Minion;
import info.hearthsim.brazier.Keywords;
import info.hearthsim.brazier.cards.CardDescr;
import info.hearthsim.brazier.minions.MinionDescr;
import info.hearthsim.brazier.minions.MinionName;
import java.util.Set;
import java.util.function.Supplier;

import org.jtrim.utils.ExceptionHelper;

public final class MinionParser {
    private final EventNotificationParser<Minion> eventNotificationParser;
    private final JsonDeserializer objectParser;

    public MinionParser(JsonDeserializer objectParser) {
        ExceptionHelper.checkNotNullArgument(objectParser, "objectParser");

        this.eventNotificationParser = new EventNotificationParser<>(Minion.class, objectParser);
        this.objectParser = objectParser;
    }

    /**
     * Parses the given {@link JsonTree} to a {@link MinionDescr} with the given name and {@link Keyword}s.
     * <p>
     * The given {@code JsonTree} should be the root of any minion JSON document. Sees {@code .card} files in
     * {@code cardDb}.
     *
     * @param minionRoot the given {@code JsonTree}.
     * @param name the name of the minion.
     * @param keywords the keywords to be added to the {@code MinionDescr}.
     * @param cardRef the reference to the minion card's {@link CardDescr}, represented as a {@link Supplier}.
     * @return the parsed {@code MinionDescr}.
     * @throws ObjectParsingException if failed to parse the given {@code JsonTree}.
     */
    public MinionDescr fromJson(
            JsonTree minionRoot,
            String name,
            Set<Keyword> keywords,
            Supplier<CardDescr> cardRef) throws ObjectParsingException {
        int attack = ParserUtils.getIntField(minionRoot, "attack");
        int hp = ParserUtils.getIntField(minionRoot, "hp");

        MinionName minionId = new MinionName(name);

        MinionDescr.Builder builder = new MinionDescr.Builder(minionId, attack, hp, cardRef);

        JsonTree maxAttackCountElement = minionRoot.getChild("maxAttackCount");
        if (maxAttackCountElement != null) {
            builder.setMaxAttackCount(maxAttackCountElement.getAsInt());
        }

        JsonTree canAttackElement = minionRoot.getChild("canAttack");
        if (canAttackElement != null) {
            builder.setCanAttack(canAttackElement.getAsBoolean());
        }

        JsonTree displayNameElement = minionRoot.getChild("displayName");
        if (displayNameElement != null) {
            builder.setDisplayName(displayNameElement.getAsString());
        }

        JsonTree tauntElement = minionRoot.getChild("taunt");
        if (tauntElement != null) {
            builder.setTaunt(tauntElement.getAsBoolean());
        }

        JsonTree divineShieldElement = minionRoot.getChild("divineShield");
        if (divineShieldElement != null) {
            builder.setDivineShield(divineShieldElement.getAsBoolean());
        }

        JsonTree chargeElement = minionRoot.getChild("charge");
        if (chargeElement != null) {
            builder.setCharge(chargeElement.getAsBoolean());
        }

        JsonTree targetableElement = minionRoot.getChild("targetable");
        if (targetableElement != null) {
            builder.setTargetable(targetableElement.getAsBoolean());
        }

        JsonTree attackWithHpElement = minionRoot.getChild("attackWithHp");
        if (attackWithHpElement != null) {
            if (attackWithHpElement.getAsBoolean()) {
                builder.setAttackFinalizer((owner, prev) -> owner.getBody().getCurrentHp());
            }
        }

        JsonTree stealthElement = minionRoot.getChild("stealth");
        if (stealthElement != null) {
            builder.setStealth(stealthElement.getAsBoolean());
        }

        JsonTree attackLeftElement = minionRoot.getChild("attackLeft");
        if (attackLeftElement != null) {
            builder.setAttackLeft(attackLeftElement.getAsBoolean());
        }

        JsonTree attackRightElement = minionRoot.getChild("attackRight");
        if (attackRightElement != null) {
            builder.setAttackRight(attackRightElement.getAsBoolean());
        }

        keywords.forEach(builder::addKeyword);

        boolean hasBattleCry = ParserUtils.parsePlayActionDefs(
                objectParser,
                minionRoot.getChild("battleCries"),
                Minion.class,
                builder::addBattleCry);
        if (hasBattleCry) {
            builder.addKeyword(Keywords.BATTLE_CRY);
        }

        builder.setAbilities(ParserUtils.parseAbilities(Minion.class, objectParser, eventNotificationParser, minionRoot));

        return builder.create();
    }
}
