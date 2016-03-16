package info.hearthsim.brazier;

import info.hearthsim.brazier.actions.PlayActionDef;
import info.hearthsim.brazier.actions.PlayActionRequirement;
import info.hearthsim.brazier.actions.PlayArg;
import info.hearthsim.brazier.cards.CardType;
import info.hearthsim.brazier.actions.TargetNeed;
import info.hearthsim.brazier.actions.undo.UndoAction;
import info.hearthsim.brazier.cards.Card;
import info.hearthsim.brazier.cards.CardDescr;
import info.hearthsim.brazier.cards.CardId;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.jtrim.utils.ExceptionHelper;

public final class HeroPower implements PlayerProperty {
    private final Hero hero;
    private final CardDescr powerDef;

    private int useCount;

    private final AtomicReference<Card> baseCardRef;

    public HeroPower(Hero hero, CardDescr powerDef) {
        ExceptionHelper.checkNotNullArgument(hero, "hero");
        ExceptionHelper.checkNotNullArgument(powerDef, "powerDef");

        this.hero = hero;
        this.powerDef = powerDef;
        this.useCount = 0;
        this.baseCardRef = new AtomicReference<>(null);
    }

    /**
     * Returns a copy of this {@code HeroPower} for the given new {@link Hero}.
     */
    public HeroPower copyFor(Hero newHero) {
        HeroPower result = new HeroPower(newHero, this.powerDef);
        result.useCount = this.useCount;
        return result;
    }

    private Card getBaseCard() {
        Card card = baseCardRef.get();
        if (card == null) {
            String name = "Power:" + powerDef.getId().getName();
            CardId id = new CardId(name);
            CardDescr.Builder result = new CardDescr.Builder(id, CardType.UNKNOWN, powerDef.getManaCost());
            card = new Card(hero.getOwner(), result.create());

            if (!baseCardRef.compareAndSet(null, card)) {
                card = baseCardRef.get();
            }
        }

        return card;
    }

    @Override
    public Player getOwner() {
        return hero.getOwner();
    }

    public CardDescr getPowerDef() {
        return powerDef;
    }

    public int getManaCost() {
        return powerDef.getManaCost();
    }

    public TargetNeed getTargetNeed() {
        return PlayActionDef.combineNeeds(getOwner(), powerDef.getOnPlayActions());
    }

    /**
     * Returns if the hero power is playable for the given player. The hero power
     * can not be played if there is not enough mana or has been played once in this turn,
     * or the player cannot satisfy any of the {@link PlayActionRequirement}s of the
     * hero power's {@link PlayActionDef}s.
     */
    public boolean isPlayable() {

        if (powerDef.getManaCost() > getOwner().getMana()) {
            return false;
        }
        if (useCount >= 1) {
            return false;
        }

        for (PlayActionDef<Card> action: powerDef.getOnPlayActions()) {
            if (action.getRequirement().meetsRequirement(getOwner())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Plays the hero power towards the optional target.
     */
    public UndoAction play(Game game, Optional<Character> target) {
        PlayArg<Card> playArg = new PlayArg<>(getBaseCard(), target);

        Player owner = getOwner();

        UndoAction.Builder result = new UndoAction.Builder();
        result.addUndo(owner.getManaResource().spendMana(powerDef.getManaCost(), 0));

        useCount++;
        result.addUndo(() -> useCount--);

        for (PlayActionDef<Card> action: powerDef.getOnPlayActions()) {
            result.addUndo(action.doPlay(game, playArg));
        }
        return result;
    }

    /**
     * Refreshes the hero power's use count.
     */
    public UndoAction refresh() {
        int prevUseCount = useCount;
        useCount = 0;
        return () -> useCount = prevUseCount;
    }
}
