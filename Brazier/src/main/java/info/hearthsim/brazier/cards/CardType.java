package info.hearthsim.brazier.cards;

import info.hearthsim.brazier.Keyword;
import info.hearthsim.brazier.Keywords;

/**
 * The type of a card.
 */
public enum CardType {
    SPELL(Keywords.SPELL),
    MINION(Keywords.MINION),
    WEAPON(Keywords.WEAPON),
    HERO_POWER(Keywords.HERO_POWER),
    UNKNOWN(Keyword.create("unknown-card-type"));

    private final Keyword keyword;

    private CardType(Keyword keyword) {
        this.keyword = keyword;
    }

    public Keyword getKeyword() {
        return keyword;
    }
}
