package info.hearthsim.brazier.game;

import info.hearthsim.brazier.db.CardDescr;

import java.util.List;

public interface UserAgent {
    public CardDescr selectCard(boolean allowCancel, List<? extends CardDescr> cards);
}
