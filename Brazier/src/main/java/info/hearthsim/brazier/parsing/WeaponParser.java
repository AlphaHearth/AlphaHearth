package info.hearthsim.brazier.parsing;

import info.hearthsim.brazier.game.Keyword;
import info.hearthsim.brazier.db.WeaponDescr;
import info.hearthsim.brazier.game.weapons.WeaponName;
import info.hearthsim.brazier.game.weapons.Weapon;

import java.util.Collections;
import java.util.Set;

import org.jtrim.utils.ExceptionHelper;

public final class WeaponParser implements EntityParser<WeaponDescr> {
    private final JsonDeserializer objectParser;
    private final EventNotificationParser<Weapon> notificationParser;

    public WeaponParser(JsonDeserializer objectParser) {
        ExceptionHelper.checkNotNullArgument(objectParser, "objectParser");

        this.objectParser = objectParser;
        this.notificationParser = new EventNotificationParser<>(Weapon.class, objectParser);
    }

    @Override
    public WeaponDescr fromJson(JsonTree root) throws ObjectParsingException {
        String name = ParserUtils.getStringField(root, "name");

        JsonTree keywordsElement = root.getChild("keywords");
        Set<Keyword> keywords = keywordsElement != null
                ? ParserUtils.parseKeywords(keywordsElement)
                : Collections.emptySet();

        return fromJson(root, name, keywords);
    }

    public WeaponDescr fromJson(JsonTree root, String name, Set<Keyword> keywords) throws ObjectParsingException {
        int attack = ParserUtils.getIntField(root, "attack");
        int durability = ParserUtils.getIntField(root, "durability");

        WeaponDescr.Builder builder = new WeaponDescr.Builder(new WeaponName(name), attack, durability);

        keywords.forEach(builder::addKeyword);

        JsonTree maxAttackCountElement = root.getChild("maxAttackCount");
        if (maxAttackCountElement != null) {
            builder.setMaxAttackCount(maxAttackCountElement.getAsInt());
        }

        JsonTree canRetaliateWithElement = root.getChild("canRetaliateWith");
        if (canRetaliateWithElement != null) {
            builder.setCanRetaliateWith(canRetaliateWithElement.getAsBoolean());
        }

        JsonTree canTargetRetaliate = root.getChild("canTargetRetaliate");
        if (canTargetRetaliate != null) {
            builder.setCanTargetRetaliate(canTargetRetaliate.getAsBoolean());
        }

        builder.setAbilities(ParserUtils.parseAbilities(Weapon.class, objectParser, notificationParser, root));

        return builder.create();
    }
}
