package info.hearthsim.brazier;

import info.hearthsim.brazier.abilities.Ability;
import info.hearthsim.brazier.util.UndoAction;
import info.hearthsim.brazier.cards.CardDescr;

import java.util.Set;

import org.jtrim.utils.ExceptionHelper;

public final class Secret implements Entity<Secret>, LabeledEntity, DamageSource {
    private final EntityId secretId;
    private Player owner;
    private final CardDescr baseCard;
    private final Ability<Secret> ability;
    private UndoAction<Secret> ref;

    public Secret(Player owner, CardDescr baseCard, Ability<Secret> ability) {
        ExceptionHelper.checkNotNullArgument(owner, "owner");
        ExceptionHelper.checkNotNullArgument(baseCard, "baseCard");
        ExceptionHelper.checkNotNullArgument(ability, "ability");

        this.secretId = new EntityId();
        this.owner = owner;
        this.baseCard = baseCard;
        this.ability = ability;
        this.ref = null;
    }

    /**
     * Creates a copy for the given {@code Secret} for the given owner.
     */
    private Secret(Player newOwner, Secret other) {
        this.secretId = other.secretId;
        this.owner = newOwner;
        this.baseCard = other.baseCard;
        this.ability = other.ability;
    }

    /**
     * Returns a copy of this {@code Secret} with the given new owner.
     */
    public Secret copyFor(Game newGame, Player newOwner) {
        return new Secret(newOwner, this);
    }

    @Override
    public Damage createDamage(int damage) {
        return getOwner().getSpellDamage(damage);
    }

    public void setOwner(Player newOwner) {
        ExceptionHelper.checkNotNullArgument(newOwner, "newOwner");

        owner = newOwner;
    }

    public CardDescr getBaseCard() {
        return baseCard;
    }

    @Override
    public Set<Keyword> getKeywords() {
        return baseCard.getKeywords();
    }

    public EntityName getSecretId() {
        return baseCard.getId();
    }

    public void activate() {
        if (ref != null)
            return;

        ref = ability.activate(this);
    }

    public void deactivate() {
        if (ref == null)
            return;

        ref.undo(this);
        ref = null;
    }

    @Override
    public Player getOwner() {
        return owner;
    }

    @Override
    public Game getGame() {
        return owner.getGame();
    }

    @Override
    public String toString() {
        return "Secret: " + getSecretId().getName();
    }

    @Override
    public EntityId getEntityId() {
        return secretId;
    }
}
