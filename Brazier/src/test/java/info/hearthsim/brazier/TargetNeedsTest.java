package info.hearthsim.brazier;

import info.hearthsim.brazier.actions.TargetNeed;
import info.hearthsim.brazier.minions.Minion;
import info.hearthsim.brazier.parsing.TestDb;
import info.hearthsim.brazier.ui.PlayerTargetNeed;
import info.hearthsim.brazier.cards.CardDescr;
import info.hearthsim.brazier.cards.CardName;

import java.util.List;

import info.hearthsim.brazier.utils.BrazierTest;
import org.junit.Test;

import static info.hearthsim.brazier.utils.TestCards.*;
import static org.junit.Assert.*;

public final class TargetNeedsTest extends BrazierTest {
    private static boolean canTarget(Player player, CardDescr card, Character target) {
        TargetNeed cardNeed = card.getCombinedTargetNeed(player);
        boolean hero = target instanceof Hero;
        TargeterDef targeterDef = new TargeterDef(player.getPlayerId(), hero, false);
        PlayerTargetNeed targetNeed = new PlayerTargetNeed(targeterDef, cardNeed);
        return targetNeed.isAllowedTarget(target);
    }

    private static CardDescr getCard(String cardId) {
        return TestDb.getTestDb().getCardDb().getById(new CardName(cardId));
    }

    @Test
    public void testExecute() {
        agent.setMana("p1", 10);
        agent.setMana("p2", 10);

        agent.playMinionCard("p1", YETI, 0);
        agent.playMinionCard("p1", SLIME, 1);

        agent.playNonMinionCard("p2", MOONFIRE, "p1:0");

        agent.expectBoard("p1",
            expectedMinion(YETI, 4, 4),
            expectedMinion(SLIME, 1, 2));

        agent.expectPlayer("p1", (player) -> {
            Player opponent = player.getGame().getOpponent(player.getPlayerId());

            List<Minion> minions = player.getBoard().getAllMinions();
            CardDescr execute = getCard(EXECUTE);

            assertTrue("Target:Yeti", canTarget(opponent, execute, minions.get(0)));
            assertFalse("Target:Slime", canTarget(opponent, execute, minions.get(1)));
        });
    }
}
