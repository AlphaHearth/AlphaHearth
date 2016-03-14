package info.hearthsim.brazier.events;

import info.hearthsim.brazier.Keyword;
import info.hearthsim.brazier.actions.CardPlayRef;
import info.hearthsim.brazier.LabeledEntity;
import info.hearthsim.brazier.Player;
import info.hearthsim.brazier.PlayerProperty;
import info.hearthsim.brazier.cards.Card;
import java.util.Set;

import org.jtrim.utils.ExceptionHelper;

/**
 * An event of playing a card with a specific mana cost by the specific player.
 * <p>
 * {@code CardPlayedEvent} is triggered after a card is played and evaluated.
 * For the event which is triggered just before playing a specific card, see
 * {@link CardPlayEvent}
 *
 * @see CardPlayEvent
 */
public final class CardPlayedEvent implements PlayerProperty, LabeledEntity, CardPlayRef {
    private final Card card;
    private final int manaCost;

    public CardPlayedEvent(Card card, int manaCost) {
        ExceptionHelper.checkNotNullArgument(card, "card");

        this.card = card;
        this.manaCost = manaCost;
    }

    @Override
    public Set<Keyword> getKeywords() {
        return card.getKeywords();
    }

    @Override
    public Player getOwner() {
        return card.getOwner();
    }

    @Override
    public Card getCard() {
        return card;
    }

    @Override
    public int getManaCost() {
        return manaCost;
    }
}
