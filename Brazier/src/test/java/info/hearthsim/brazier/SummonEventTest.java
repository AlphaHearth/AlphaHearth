package info.hearthsim.brazier;

import info.hearthsim.brazier.game.Hand;
import info.hearthsim.brazier.game.cards.Card;
import info.hearthsim.brazier.game.minions.MinionName;
import info.hearthsim.brazier.utils.BrazierTest;
import info.hearthsim.brazier.utils.TestAgent;
import org.jtrim.collections.CollectionsEx;
import org.junit.Ignore;
import org.junit.Test;

import java.util.*;
import java.util.function.Consumer;

import static info.hearthsim.brazier.utils.TestCards.*;
import static org.junit.Assert.*;

public final class SummonEventTest extends BrazierTest {
    @Test
    public void testWarsongCommander() {
        agent.setMana("p1", 10);
        agent.setMana("p2", 10);

        agent.playMinionCard("p1", WARSONG_COMMANDER, 0);
        agent.playMinionCard("p1", YETI, 1);
        agent.playMinionCard("p1", SLIME, 2);

        agent.expectMinion("p1:1", (minion) -> {
            assertFalse("charge", minion.isCharge());
        });
        agent.expectMinion("p1:2", (minion) -> {
            assertTrue("charge", minion.isCharge());
        });

        agent.playMinionCard("p2", SLIME, 0);

        agent.expectMinion("p2:0", (minion) -> {
            assertFalse("charge", minion.isCharge());
        });
    }

    private MinionName singleMinionScript(String minionLocation, Consumer<TestAgent> scriptConfig) {
        List<Set<MinionName>> results = new LinkedList<>();

        scriptConfig.accept(agent);

        agent.expectMinion(minionLocation,
            (minion) -> results.add(Collections.singleton(minion.getBaseDescr().getId())));

        Set<MinionName> firstResult = results.remove(0);
        for (Set<MinionName> result : results) {
            assertEquals("board result", firstResult, result);
        }

        return firstResult.iterator().next();
    }

    private MinionName testAlarmOBot(int roll) {
        return singleMinionScript("p1:0", (script) -> {
            script.setMana("p1", 10);
            script.setMana("p2", 10);

            script.addToHand("p1", YETI, PYROBLAST, FIRE_ELEMENTAL, FIERY_WAR_AXE);

            script.playMinionCard("p1", WISP, 0);
            script.playMinionCard("p1", WISP, 0);
            script.playMinionCard("p1", WISP, 0);
            script.playMinionCard("p1", WISP, 0);
            script.playMinionCard("p1", WISP, 0);
            script.playMinionCard("p1", WISP, 0);
            script.playMinionCard("p1", ALARM_O_BOT, 0);

            script.expectBoard("p1",
                expectedMinion(ALARM_O_BOT, 0, 3),
                expectedMinion(WISP, 1, 1),
                expectedMinion(WISP, 1, 1),
                expectedMinion(WISP, 1, 1),
                expectedMinion(WISP, 1, 1),
                expectedMinion(WISP, 1, 1),
                expectedMinion(WISP, 1, 1));

            script.setCurrentPlayer("p1");
            script.endTurn();
            script.addRoll(2, roll);
            script.endTurn();

            script.expectPlayer("p1", (player) -> {
                MinionName minionId = player.getBoard().getAllMinions().get(0).getBaseDescr().getId();

                List<String> expectedCards;
                if (YETI.equals(minionId.getName())) {
                    expectedCards = Arrays.asList(ALARM_O_BOT, PYROBLAST, FIRE_ELEMENTAL, FIERY_WAR_AXE);
                } else {
                    expectedCards = Arrays.asList(YETI, PYROBLAST, ALARM_O_BOT, FIERY_WAR_AXE);
                }

                Hand hand = player.getHand();
                List<Card> handCards = hand.getCards();
                List<String> handCardNames = new ArrayList<>(handCards.size());
                handCards.forEach((card) -> handCardNames.add(card.getCardDescr().getId().getName()));

                assertEquals("hand", expectedCards, handCardNames);
            });
        });
    }

    private Set<MinionName> minionIds(String... names) {
        Set<MinionName> result = CollectionsEx.newHashSet(names.length);
        for (String name : names) {
            result.add(new MinionName(name));
        }
        return result;
    }

    @Test
    @Ignore("Not supported for dynamic testing")
    public void testAlarmOBot() {
        testAlarmOBot(0);
        setUp(); // Set up again to reset TestAgent
        testAlarmOBot(1);
    }

    @Test
    public void testAlarmOBotWithNoMinions() {
        agent.setMana("p1", 10);
        agent.setMana("p2", 10);

        agent.addToHand("p1", PYROBLAST, FIERY_WAR_AXE);

        agent.playMinionCard("p1", WISP, 0);
        agent.playMinionCard("p1", WISP, 0);
        agent.playMinionCard("p1", WISP, 0);
        agent.playMinionCard("p1", WISP, 0);
        agent.playMinionCard("p1", WISP, 0);
        agent.playMinionCard("p1", WISP, 0);
        agent.playMinionCard("p1", ALARM_O_BOT, 0);

        agent.expectBoard("p1",
            expectedMinion(ALARM_O_BOT, 0, 3),
            expectedMinion(WISP, 1, 1),
            expectedMinion(WISP, 1, 1),
            expectedMinion(WISP, 1, 1),
            expectedMinion(WISP, 1, 1),
            expectedMinion(WISP, 1, 1),
            expectedMinion(WISP, 1, 1));

        agent.setCurrentPlayer("p1");
        agent.endTurn();
        agent.endTurn();

        agent.expectBoard("p1",
            expectedMinion(ALARM_O_BOT, 0, 3),
            expectedMinion(WISP, 1, 1),
            expectedMinion(WISP, 1, 1),
            expectedMinion(WISP, 1, 1),
            expectedMinion(WISP, 1, 1),
            expectedMinion(WISP, 1, 1),
            expectedMinion(WISP, 1, 1));
    }
}
