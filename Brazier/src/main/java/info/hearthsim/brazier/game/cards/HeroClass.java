package info.hearthsim.brazier.game.cards;

import info.hearthsim.brazier.game.Keyword;
import info.hearthsim.brazier.game.Keywords;

/**
 * Classes of cards
 */
public enum HeroClass {
    Paladin(Keywords.CLASS_PALADIN),
    Warrior(Keywords.CLASS_WARRIOR),
    Hunter(Keywords.CLASS_HUNTER),
    Shaman(Keywords.CLASS_SHAMAN),
    Rogue(Keywords.CLASS_ROUGE),
    Druid(Keywords.CLASS_DRUID),
    Priest(Keywords.CLASS_PRIEST),
    Mage(Keywords.CLASS_MAGE),
    Warlock(Keywords.CLASS_WARLOCK),
    Neutral(Keywords.CLASS_NEUTRAL);

    private final Keyword keyword;
    HeroClass(Keyword keyword) {
        this.keyword = keyword;
    }
    public Keyword getKeyword() {
        return keyword;
    }
}
