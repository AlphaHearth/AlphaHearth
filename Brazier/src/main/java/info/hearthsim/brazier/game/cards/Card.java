package info.hearthsim.brazier.game.cards;

import info.hearthsim.brazier.*;
import info.hearthsim.brazier.actions.TargetNeed;
import info.hearthsim.brazier.game.*;
import info.hearthsim.brazier.db.CardDescr;
import info.hearthsim.brazier.util.UndoAction;
import info.hearthsim.brazier.actions.CardRef;
import info.hearthsim.brazier.actions.ManaCostAdjuster;
import info.hearthsim.brazier.game.minions.Minion;
import info.hearthsim.brazier.db.MinionDescr;
import info.hearthsim.brazier.abilities.AuraAwareIntProperty;

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
public final class Card implements Entity<Card>, PlayerProperty, LabeledEntity, CardRef, DamageSource {
    private final EntityId cardId;
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

        this.cardId = new EntityId();
        this.owner = owner;
        this.cardDescr = cardDescr;
        this.manaCost = new AuraAwareIntProperty(cardDescr.getManaCost());
        this.manaCost.addExternalBuff(this::adjustManaCost);

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

        this.cardId = card.cardId;
        this.owner = owner;
        this.cardDescr = card.cardDescr;
        this.manaCost = card.manaCost.copy();
        this.manaCost.addExternalBuff(this::adjustManaCost);
        if (card.minion == null)
            this.minion = null;
        else
            this.minion = card.minion.copyFor(owner.getGame(), owner);
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
    public Damage createDamage(int damage) {
        if (minion != null) {
            return minion.createDamage(damage);
        }

        if (cardDescr.getCardType() == CardType.Spell) {
            return getOwner().getSpellDamage(damage);
        }

        return new Damage(this, damage);
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
    public UndoAction<Card> decreaseManaCost(int amount) {
        return UndoAction.of(this, (c) -> c.manaCost, (mc) -> mc.addBuff(-amount));
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
    public Card copyFor(Game newGame, Player newOwner) {
        return new Card(newOwner, this);
    }

    @Override
    public String toString() {
        return cardDescr.toString();
    }

    @Override
    public EntityId getEntityId() {
        return cardId;
    }
}
