package info.hearthsim.brazier.events;

import info.hearthsim.brazier.actions.CardPlayRef;
import info.hearthsim.brazier.actions.PlayArg;
import info.hearthsim.brazier.actions.undo.UndoAction;
import info.hearthsim.brazier.cards.Card;
import info.hearthsim.brazier.Keyword;
import info.hearthsim.brazier.LabeledEntity;
import info.hearthsim.brazier.Player;
import info.hearthsim.brazier.PlayerProperty;
import info.hearthsim.brazier.TargetRef;
import info.hearthsim.brazier.Character;

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
    public UndoAction replaceTarget(Character newTarget) {
        if (newTarget == getTarget()) {
            return UndoAction.DO_NOTHING;
        }

        return replaceTarget(Optional.ofNullable(newTarget));
    }

    /**
     * Replaces the {@code CardPlayEvent}'s {@link PlayArg} with a new one with the given
     * {@link Character} as its target.
     *
     * @param newTarget the new target for the new {@code PlayArg}.
     */
    public UndoAction replaceTarget(Optional<Character> newTarget) {
        ExceptionHelper.checkNotNullArgument(newTarget, "newTarget");

        PlayArg<Card> prevArg = cardPlayArg;
        cardPlayArg = new PlayArg<>(getCard(), newTarget);
        return () -> cardPlayArg = prevArg;
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
    public UndoAction vetoPlay() {
        if (vetoedPlay) {
            return UndoAction.DO_NOTHING;
        }

        vetoedPlay = true;
        return () -> vetoedPlay = false;
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
