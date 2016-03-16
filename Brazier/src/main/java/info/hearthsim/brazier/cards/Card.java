package info.hearthsim.brazier.cards;

import info.hearthsim.brazier.DamageSource;
import info.hearthsim.brazier.Keyword;
import info.hearthsim.brazier.PlayerProperty;
import info.hearthsim.brazier.actions.TargetNeed;
import info.hearthsim.brazier.actions.undo.UndoableResult;
import info.hearthsim.brazier.actions.CardRef;
import info.hearthsim.brazier.actions.ManaCostAdjuster;
import info.hearthsim.brazier.minions.Minion;
import info.hearthsim.brazier.minions.MinionDescr;
import info.hearthsim.brazier.Damage;
import info.hearthsim.brazier.LabeledEntity;
import info.hearthsim.brazier.Player;
import info.hearthsim.brazier.abilities.AuraAwareIntProperty;
import info.hearthsim.brazier.actions.undo.UndoAction;

import java.util.List;
import java.util.Set;

import org.jtrim.utils.ExceptionHelper;

/**
 * An instance of {@code Card} stands for a card in a specific game.
 * <p>
 * Unlike {@link CardDescr}, which describes a kind of cards in hearthstone, different cards
 * with the same name (say <em>Fireball</em>) in a game means different {@code Card} instance,
 * even though they have the same {@link CardDescr} property as they are of the same kind.
 * <p>
 * In this sense, more in-game properties can be included in this class, which include:
 * <ul>
 *     <li>{@code owner}: the owning {@link Player};</li>
 *     <li>
 *         {@code cardDescr}: the {@link CardDescr} which describes all its game-irrelevant properties;
 *     </li>
 *     <li>
 *         {@code manaCost}: an {@link AuraAwareIntProperty} which can be reduced by auras or buffs
 *         registered in the game, including <em>Emperor Thaurissan</em> and <em>Sorcerer's Apprentice</em>.
 *     </li>
 * </ul>
 */
public final class Card implements PlayerProperty, LabeledEntity, CardRef, DamageSource {
    private final Player owner;
    private final CardDescr cardDescr;
    private final Minion minion;

    private final AuraAwareIntProperty manaCost;

    /**
     * Creates a {@code Card} with the given {@code CardDescr} and the owning {@code Player}.
     *
     * @param owner the owning {@code Player}.
     * @param cardDescr the given {@code CardDescr}.
     */
    public Card(Player owner, CardDescr cardDescr) {
        ExceptionHelper.checkNotNullArgument(cardDescr, "cardDescr");
        ExceptionHelper.checkNotNullArgument(owner, "owner");

        this.owner = owner;
        this.cardDescr = cardDescr;
        this.manaCost = new AuraAwareIntProperty(cardDescr.getManaCost());
        this.manaCost.addBuff(this::adjustManaCost);

        MinionDescr minionDescr = cardDescr.getMinion();
        this.minion = minionDescr != null ? new Minion(owner, cardDescr.getMinion()) : null;
    }

    /**
     * Creates a copy of the given {@code Card} with the given new {@link Player owner}.
     *
     * @param owner the given new owner.
     * @param card the given {@code Card} to be copied.
     */
    private Card(Player owner, Card card) {
        ExceptionHelper.checkNotNullArgument(card, "card");
        ExceptionHelper.checkNotNullArgument(owner, "owner");

        this.owner = owner;
        this.cardDescr = card.cardDescr;
        this.manaCost = card.manaCost.copy();
        this.minion = card.minion.copyFor(owner);
    }

    public Minion getMinion() {
        return minion;
    }

    /**
     * {@inheritDoc}
     *
     * @implSpec return this
     *
     * @return {@inheritDoc}
     */
    @Override
    public Card getCard() {
        return this;
    }

    @Override
    public Player getOwner() {
        return owner;
    }

    @Override
    public Set<Keyword> getKeywords() {
        return cardDescr.getKeywords();
    }

    @Override
    public UndoableResult<Damage> createDamage(int damage) {
        if (minion != null) {
            return minion.createDamage(damage);
        }

        if (cardDescr.getCardType() == CardType.SPELL) {
            return new UndoableResult<>(getOwner().getSpellDamage(damage));
        }

        return new UndoableResult<>(new Damage(this, damage));
    }

    public TargetNeed getTargetNeed() {
        return cardDescr.getCombinedTargetNeed(getOwner());
    }

    /**
     * Returns the {@code cardDescr} field of this {@code Card}.
     */
    public CardDescr getCardDescr() {
        return cardDescr;
    }

    /**
     * Returns the {@code manaCost} field of this {@code Card}, whose value can be negative.
     */
    public AuraAwareIntProperty getRawManaCost() {
        // Note that the final value might be negative.
        return manaCost;
    }

    /**
     * Adds a buff to this {@code Card} which decreases its mana cost with the given amount.
     */
    public UndoAction decreaseManaCost(int amount) {
        return manaCost.addBuff(-amount);
    }

    /**
     * Returns the current mana cost of the card.
     */
    public int getActiveManaCost() {
        return Math.max(0, manaCost.getValue());
    }

    public boolean isMinionCard() {
        return cardDescr.getMinion() != null;
    }

    private int adjustManaCost(int baseCost) {
        List<ManaCostAdjuster> costAdjusters = cardDescr.getManaCostAdjusters();
        int result = baseCost;
        if (!costAdjusters.isEmpty()) {
            for (ManaCostAdjuster adjuster: costAdjusters) {
                result = adjuster.adjustCost(this, result);
            }
        }
        return result;
    }

    /**
     * Returns a copy of this {@code Card} with the given new {@link Player owner}.
     */
    public Card copyFor(Player newOwner) {
        return new Card(newOwner, this);
    }

    @Override
    public String toString() {
        return cardDescr.toString();
    }
}
