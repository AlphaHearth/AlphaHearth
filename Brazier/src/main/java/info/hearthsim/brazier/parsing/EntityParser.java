package info.hearthsim.brazier.parsing;

import info.hearthsim.brazier.db.HearthStoneEntity;

/**
 * Parser for {@link HearthStoneEntity}, which parse a given {@link JsonTree} to a
 * {@code HearthStoneEntity}. {@link #fromJson(JsonTree)} is the sole un-implemented
 * method of this interface, meaning it can be used as a functional interface.
 */
public interface EntityParser<Entity extends HearthStoneEntity> {

    /**
     * Parses the given {@link JsonTree} to a {@link HearthStoneEntity}.
     *
     * @param root the given {@code JsonTree}.
     * @return the parsed result as a {@code HearthStoneEntity}.
     * @throws ObjectParsingException if fails to parse the given {@code JsonTree}.
     */
    public Entity fromJson(JsonTree root) throws ObjectParsingException;
}
