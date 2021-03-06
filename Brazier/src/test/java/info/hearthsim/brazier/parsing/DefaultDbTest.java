package info.hearthsim.brazier.parsing;

import info.hearthsim.brazier.db.CardDescr;
import info.hearthsim.brazier.db.HearthStoneEntityDatabase;
import info.hearthsim.brazier.db.MinionDescr;
import info.hearthsim.brazier.game.Keyword;
import info.hearthsim.brazier.game.Keywords;
import info.hearthsim.brazier.game.cards.CardType;
import info.hearthsim.brazier.utils.TestAgent;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public final class DefaultDbTest {
    @Test
    public void testDbIsAvailable() throws IOException, ObjectParsingException {
        TestDb.getTestDbUnsafe();
    }

    @Test
    public void testAllMinionIsSummonable() {
        TestAgent agent = new TestAgent();

        HearthStoneEntityDatabase<MinionDescr> minionDb = TestDb.getTestDb().getMinionDb();
        for (MinionDescr minion: minionDb.getAll())
            agent.applyToPlayer("p1", (player) -> player.summonMinion(minion));
    }

    @Test
    public void testCardTypeKeywords() {
        HearthStoneEntityDatabase<CardDescr> cardDb = TestDb.getTestDb().getCardDb();
        for (CardDescr card: cardDb.getAll()) {
            try {
                Set<Keyword> keywords = card.getKeywords();

                switch (card.getCardType()) {
                    case Spell:
                        assertFalse(keywords.contains(Keywords.MINION));
                        assertTrue(keywords.contains(Keywords.SPELL));
                        assertFalse(keywords.contains(Keywords.WEAPON));
                        break;
                    case Minion:
                        assertTrue(keywords.contains(Keywords.MINION));
                        assertFalse(keywords.contains(Keywords.SPELL));
                        assertFalse(keywords.contains(Keywords.WEAPON));
                        break;
                    case Weapon:
                        assertFalse(keywords.contains(Keywords.MINION));
                        assertFalse(keywords.contains(Keywords.SPELL));
                        assertTrue(keywords.contains(Keywords.WEAPON));
                        break;
                    default:
                        throw new AssertionError(card.getCardType().name());
                }
            } catch (Throwable ex) {
                throw new AssertionError("Card type error: " + card.getId(), ex);
            }
        }
    }

    private void verifyChooseOneDescription(CardDescr card, String description) {
        List<CardDescr> actions = card.getChooseOneChoices();
        if (actions.isEmpty()) {
            return;
        }

        if (!description.contains("Choose One - ")) {
            fail("Choose one card has to contain Choose One in its description.");
        }
    }

    @Test
    public void testBasicKeywordsInDescription() {
        HearthStoneEntityDatabase<CardDescr> cardDb = TestDb.getTestDb().getCardDb();
        for (CardDescr card: cardDb.getAll()) {
            try {
                String description = card.getDescription();

                if (card.getOverload() > 0) {
                    assertTrue("Overload", description.contains("Overload"));
                }

                boolean needDescription;
                MinionDescr minion = card.getMinion();
                if (minion != null) {
                    if (minion.isTaunt()) {
                        assertTrue("Taunt", description.contains("Taunt"));
                    }
                    if (minion.isDivineShield()) {
                        assertTrue("Divine Shield", description.contains("Divine Shield"));
                    }
                    if (minion.isStealth()) {
                        assertTrue("Stealth", description.contains("Stealth"));
                    }
                    if (!minion.getBattleCries().isEmpty()) {
                        assertTrue("Battlecry", description.contains("Battlecry"));
                    }
                    int overload = minion.getBaseCard().getOverload();
                    if (overload > 0) {
                        assertTrue("Overload", description.contains("Overload: (" + overload + ")"));
                    }
                    if (minion.getMaxAttackCount() == 2) {
                        assertTrue("Windfury", description.contains("Windfury"));
                    }
                    if (!minion.isCanAttack()) {
                        assertTrue("Can't Attack", description.contains("Can't Attack"));
                    }
                    if (!minion.isTargetable()) {
                        assertTrue("Not targetable", description.contains("Can't be targeted by spells or Hero Powers"));
                    }
                    if (minion.tryGetDeathRattle() != null) {
                        assertTrue("Deathrattle", description.contains("Deathrattle"));
                    }
                    needDescription = minion.getBattleCries().size() > 0
                            || minion.getEventActionDefs().hasAnyActionDef()
                            || minion.tryGetAbility() != null
                            || card.tryGetInHandAbility() != null;
                }
                else {
                    // Improve this for weapons
                    needDescription = card.getCardType() == CardType.Spell;
                }

                verifyChooseOneDescription(card, description);

                if (needDescription) {
                    if (description.isEmpty()) {
                        fail("This card has some custom action defined, so need a description.");
                    }
                }
            } catch (Throwable ex) {
                throw new AssertionError("Wrong card description for " + card.getId(), ex);
            }
        }
    }

    @Test
    public void testAllMinionsHaveCard() {
        HearthStoneEntityDatabase<MinionDescr> minionDb = TestDb.getTestDb().getMinionDb();
        for (MinionDescr minion: minionDb.getAll()) {
            assertNotNull("baseCard", minion.getBaseCard());
        }
    }

    @Test
    public void testAllMinionCardsHaveMinion() {
        HearthStoneEntityDatabase<CardDescr> cardDb = TestDb.getTestDb().getCardDb();
        for (CardDescr card: cardDb.getAll()) {
            if (card.getCardType() == CardType.Minion) {
                assertNotNull("minion", card.getMinion());
            }
        }
    }

    @Test
    public void testAllChooseOneCardsMustExist() {
        HearthStoneEntityDatabase<CardDescr> cardDb = TestDb.getTestDb().getCardDb();
        for (CardDescr card: cardDb.getAll()) {
            try {
                for (CardDescr chooseOneCard: card.getChooseOneChoices()) {
                    assertNotNull(chooseOneCard.getId().getName(), chooseOneCard);
                }
            } catch (Throwable ex) {
                throw new AssertionError("Choose one card does not exist for " + card.getId(), ex);
            }
        }
    }

    @Test
    public void testKnownCardClasses() {
        Set<Keyword> allowedClasses = new HashSet<>(Arrays.asList(
                Keywords.CLASS_DRUID,
                Keywords.CLASS_HUNTER,
                Keywords.CLASS_MAGE,
                Keywords.CLASS_NEUTRAL,
                Keywords.CLASS_PALADIN,
                Keywords.CLASS_PRIEST,
                Keywords.CLASS_ROUGE,
                Keywords.CLASS_SHAMAN,
                Keywords.CLASS_WARLOCK,
                Keywords.CLASS_WARRIOR,
                Keywords.CLASS_BOSS));
        HearthStoneEntityDatabase<CardDescr> cardDb = TestDb.getTestDb().getCardDb();
        for (CardDescr card: cardDb.getAll()) {
            if (!allowedClasses.contains(card.getCardClass())) {
                fail("Card " + card.getId() + " has an unexpected card class: " + card.getCardClass());
            }
            if (card.getCardType() == CardType.Minion) {
                assertNotNull("minion", card.getMinion());
            }
        }
    }
}
