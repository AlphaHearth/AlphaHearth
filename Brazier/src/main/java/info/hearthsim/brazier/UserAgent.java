package info.hearthsim.brazier;

import info.hearthsim.brazier.cards.CardDescr;

import java.util.List;

public interface UserAgent {
    public CardDescr selectCard(boolean allowCancel, List<? extends CardDescr> cards);
}
