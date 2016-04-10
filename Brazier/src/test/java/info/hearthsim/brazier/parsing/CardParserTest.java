package info.hearthsim.brazier.parsing;

import info.hearthsim.brazier.game.Keyword;
import info.hearthsim.brazier.db.MinionDescr;
import info.hearthsim.brazier.game.Keywords;
import info.hearthsim.brazier.db.CardDescr;
import info.hearthsim.brazier.game.cards.CardName;
import info.hearthsim.brazier.game.cards.CardType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;


public class CardParserTest {
    private void assertKeywords(CardDescr card, Keyword... expectedKeywords) {
        Set<Keyword> expected = new HashSet<>(Arrays.asList(expectedKeywords));
        expected.add(Keywords.manaCost(card.getManaCost()));
        assertEquals("keywords", expected, card.getKeywords());
    }

    private static CardDescr getCard(String cardName) {
        CardDescr result = TestDb.getTestDb().getCardDb().getById(new CardName(cardName));
        assertEquals(cardName, result.getId().getName());
        return result;
    }

    @Test
    public void testShieldBlock() throws Exception {
        CardDescr card = getCard("Shield Block");

        Assert.assertEquals(CardType.Spell, card.getCardType());
        assertEquals(3, card.getManaCost());
        assertEquals("Gain 5 Armor. Draw a card.", card.getDescription());
        assertKeywords(card, Keywords.CLASS_WARRIOR, Keywords.SPELL, Keywords.COLLECTIBLE, Keywords.RARITY_COMMON);

        assertEquals("DrawActions", 0, card.getOnDrawActions().size());
        assertEquals("PlayActions", 2, card.getOnPlayActions().size());
    }

    private void assertMinionCard(CardDescr card) {
        assertEquals(CardType.Minion, card.getCardType());
        MinionDescr minion = card.getMinion();
        assertNotNull("minion", minion);

        assertEquals(card.getId().getName(), minion.getId().getName());
    }

    @Test
    public void testChillwindYeti() throws Exception {
        CardDescr card = getCard("Chillwind Yeti");

        assertEquals(4, card.getManaCost());
        assertEquals("description", "", card.getDescription());
        assertKeywords(card, Keywords.CLASS_NEUTRAL, Keywords.MINION, Keywords.COLLECTIBLE, Keywords.RARITY_COMMON);

        assertEquals("DrawActions", 0, card.getOnDrawActions().size());
        assertEquals("PlayActions", 0, card.getOnPlayActions().size());

        assertMinionCard(card);
    }

    @Test
    public void testFrothingBerserker() throws Exception {
        CardDescr card = getCard("Frothing Berserker");

        assertEquals(3, card.getManaCost());
        assertEquals("Whenever a minion takes damage, gain +1 Attack.", card.getDescription());
        assertKeywords(card, Keywords.CLASS_WARRIOR, Keywords.MINION, Keywords.COLLECTIBLE, Keywords.RARITY_RARE);

        assertEquals("DrawActions", 0, card.getOnDrawActions().size());
        assertEquals("PlayActions", 0, card.getOnPlayActions().size());

        assertMinionCard(card);
    }
}
