package info.hearthsim.brazier.utils;

import info.hearthsim.brazier.HearthStoneDb;
import info.hearthsim.brazier.PlayerId;
import info.hearthsim.brazier.cards.CardDescr;
import info.hearthsim.brazier.cards.CardId;

import java.util.ArrayList;
import java.util.List;

public final class TestUtils {
    public static final PlayerId PLAYER1_ID = new PlayerId("Player1");
    public static final PlayerId PLAYER2_ID = new PlayerId("Player2");

    /**
     * Returns the corresponding {@link PlayerId} based on the given name of the player.
     *
     * @param name the name of the player.
     * @return the corresponding {@link PlayerId}.
     *
     * @throws IllegalArgumentException if the given name does not belong to any player
     */
    public static PlayerId parsePlayerName(String name) {
        if (name.equalsIgnoreCase("p1") || name.equalsIgnoreCase(PLAYER1_ID.getName())) {
            return PLAYER1_ID;
        }
        else if (name.equalsIgnoreCase("p2") || name.equalsIgnoreCase(PLAYER2_ID.getName())) {
            return PLAYER2_ID;
        }
        throw new IllegalArgumentException("Illegal player name: " + name);
    }

    public static List<CardDescr> parseCards(HearthStoneDb db, String... cardNames) {
        List<CardDescr> result = new ArrayList<>(cardNames.length);
        for (String cardName : cardNames) {
            result.add(db.getCardDb().getById(new CardId(cardName)));
        }
        return result;
    }

    private TestUtils() {
        throw new AssertionError();
    }
}
