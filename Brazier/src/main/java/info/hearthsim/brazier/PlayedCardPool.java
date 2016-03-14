package info.hearthsim.brazier;

import info.hearthsim.brazier.actions.undo.UndoAction;
import info.hearthsim.brazier.cards.CardDescr;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.jtrim.utils.ExceptionHelper;

public final class PlayedCardPool {
    private final List<CardDescr> cards;
    private final List<CardDescr> cardsView;

    public PlayedCardPool() {
        this.cards = new LinkedList<>();
        this.cardsView = Collections.unmodifiableList(cards);
    }

    public List<CardDescr> getCards() {
        return cardsView;
    }

    public UndoAction addCard(CardDescr card) {
        ExceptionHelper.checkNotNullArgument(card, "card");
        cards.add(card);
        return () -> cards.remove(cards.size() - 1);
    }
}
