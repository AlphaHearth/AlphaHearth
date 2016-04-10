package info.hearthsim.brazier.game.cards;

import info.hearthsim.brazier.game.Keyword;
import info.hearthsim.brazier.game.Keywords;

/**
 * The type of a card.
 */
public enum CardType {
    Spell(Keywords.SPELL),
    Minion(Keywords.MINION),
    Weapon(Keywords.WEAPON),
    HeroPower(Keywords.HERO_POWER),
    Hero(Keywords.HERO),
    UNKNOWN(Keyword.create("unknown-card-type"));

    private final Keyword keyword;

    private CardType(Keyword keyword) {
        this.keyword = keyword;
    }

    public Keyword getKeyword() {
        return keyword;
    }
}
