package info.hearthsim.brazier.parsing;

import info.hearthsim.brazier.Keyword;
import info.hearthsim.brazier.minions.MinionDescr;
import info.hearthsim.brazier.minions.MinionId;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import static org.junit.Assert.*;

public class MinionParserTest {
    private void assertKeywords(MinionDescr minion, String... expectedKeywords) {
        Set<Keyword> keywordSet = new HashSet<>(expectedKeywords.length);
        for (String keyword: expectedKeywords) {
            keywordSet.add(Keyword.create(keyword));
        }
        assertEquals("keywords", keywordSet, minion.getKeywords());
    }

    private static MinionDescr getMinion(String minionName) {
        MinionDescr result = TestDb.getTestDb().getMinionDb().getById(new MinionId(minionName));
        assertEquals(minionName, result.getId().getName());
        return result;
    }

    @Test
    public void testChillwindYeti() throws Exception {
        MinionDescr minion = getMinion("Chillwind Yeti");

        assertEquals(4, minion.getAttack());
        assertEquals(5, minion.getHp());
        assertFalse("taunt", minion.isTaunt());

        assertKeywords(minion, "collectible", "4-cost", "common", "minion", "neutral");
    }
}