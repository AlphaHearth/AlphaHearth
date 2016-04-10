package info.hearthsim.brazier.events;

import info.hearthsim.brazier.actions.CardPlayRef;
import info.hearthsim.brazier.actions.PlayArg;
import info.hearthsim.brazier.game.cards.Card;
import info.hearthsim.brazier.game.Keyword;
import info.hearthsim.brazier.game.LabeledEntity;
import info.hearthsim.brazier.game.Player;
import info.hearthsim.brazier.PlayerProperty;
import info.hearthsim.brazier.TargetRef;
import info.hearthsim.brazier.game.Character;

import java.util.Optional;
import java.util.Set;

import org.jtrim.utils.ExceptionHelper;

/**
 * Event of playing a card. Three fields are included in such an event, which are the {@link PlayArg}
 * of the card-playing event, the mana cost and a flag designating if the play has been vetoed.
 */
// TODO check what "veto" means
public final class CardPlayEvent implements PlayerProperty, LabeledEntity, CardPlayRef, TargetRef {
    private PlayArg<Card> cardPlayArg;
    private final int manaCost;
    private boolean vetoedPlay;

    /**
     * Creates a {@code CardPlayEvent} with the given {@link PlayArg} and {@code manaCost}.
     *
     * @param cardPlayArg the given {@code PlayArg}.
     * @param manaCost the mana cost.
     */
    public CardPlayEvent(PlayArg<Card> cardPlayArg, int manaCost) {
        ExceptionHelper.checkNotNullArgument(cardPlayArg, "cardPlayArg");

        this.cardPlayArg = cardPlayArg;
        this.manaCost = manaCost;
        this.vetoedPlay = false;
    }

    /**
     * Replaces the {@code CardPlayEvent}'s {@link PlayArg} with a new one with the given
     * {@link Character} as its target.
     *
     * @param newTarget the new target for the new {@code PlayArg}.
     */
    public void replaceTarget(Character newTarget) {
        if (newTarget == getTarget())
            return;

        replaceTarget(Optional.ofNullable(newTarget));
    }

    /**
     * Replaces the {@code CardPlayEvent}'s {@link PlayArg} with a new one with the given
     * {@link Character} as its target.
     *
     * @param newTarget the new target for the new {@code PlayArg}.
     */
    public void replaceTarget(Optional<Character> newTarget) {
        ExceptionHelper.checkNotNullArgument(newTarget, "newTarget");

        cardPlayArg = new PlayArg<>(getCard(), newTarget);
    }

    public PlayArg<Card> getCardPlayArg() {
        return cardPlayArg;
    }

    @Override
    public Character getTarget() {
        return cardPlayArg.getTarget().orElse(null);
    }

    /**
     * Returns {@link Keyword}s of the playing card.
     */
    @Override
    public Set<Keyword> getKeywords() {
        return getCard().getKeywords();
    }

    /**
     * Vetoes this card-playing event.
     */
    public void vetoPlay() {
        if (vetoedPlay)
            return;

        vetoedPlay = true;
    }

    /**
     * Returns if the {@code CardPlayEvent} has been vetoed.
     */
    public boolean isVetoedPlay() {
        return vetoedPlay;
    }

    @Override
    public Card getCard() {
        return cardPlayArg.getActor();
    }

    @Override
    public Player getOwner() {
        return getCard().getOwner();
    }

    @Override
    public int getManaCost() {
        return manaCost;
    }
}
