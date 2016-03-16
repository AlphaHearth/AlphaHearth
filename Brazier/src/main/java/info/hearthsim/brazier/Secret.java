package info.hearthsim.brazier;

import info.hearthsim.brazier.actions.undo.UndoableUnregisterAction;
import info.hearthsim.brazier.abilities.Ability;
import info.hearthsim.brazier.actions.undo.UndoAction;
import info.hearthsim.brazier.cards.CardDescr;

import java.util.Set;

import info.hearthsim.brazier.actions.undo.UndoableResult;
import org.jtrim.utils.ExceptionHelper;

public final class Secret implements PlayerProperty, GameProperty, LabeledEntity, DamageSource {
    private Player owner;
    private final CardDescr baseCard;
    private final Ability<? super Secret> ability;
    private UndoableUnregisterAction ref;

    public Secret(Player owner, CardDescr baseCard, Ability<? super Secret> ability) {
        ExceptionHelper.checkNotNullArgument(owner, "owner");
        ExceptionHelper.checkNotNullArgument(baseCard, "baseCard");
        ExceptionHelper.checkNotNullArgument(ability, "ability");

        this.owner = owner;
        this.baseCard = baseCard;
        this.ability = ability;
        this.ref = null;
    }

    /**
     * Creates a copy for the given {@code Secret} for the given owner.
     */
    private Secret(Player newOwner, Secret other) {
        this.owner = newOwner;
        this.baseCard = other.baseCard;
        this.ability = other.ability;
        this.ref = other.ref;
    }

    /**
     * Returns a copy of this {@code Secret} with the given new owner.
     */
    public Secret copyFor(Player newOwner) {
        return new Secret(newOwner, this);
    }

    @Override
    public UndoableResult<Damage> createDamage(int damage) {
        return new UndoableResult<>(getOwner().getSpellDamage(damage));
    }

    public UndoAction setOwner(Player newOwner) {
        ExceptionHelper.checkNotNullArgument(newOwner, "newOwner");

        Player prevOwner = owner;
        owner = newOwner;
        return () -> owner = prevOwner;
    }

    public CardDescr getBaseCard() {
        return baseCard;
    }

    @Override
    public Set<Keyword> getKeywords() {
        return baseCard.getKeywords();
    }

    public EntityId getSecretId() {
        return baseCard.getId();
    }

    public UndoAction activate() {
        if (ref != null) {
            return UndoAction.DO_NOTHING;
        }

        UndoableUnregisterAction newRef = ability.activate(this);
        ref = newRef;
        return () -> {
            newRef.unregister();
            ref = null;
        };
    }

    public UndoAction deactivate() {
        if (ref == null) {
            return UndoAction.DO_NOTHING;
        }

        UndoableUnregisterAction prevRef = ref;

        UndoAction unregisterUndo = ref.unregister();
        ref = null;
        return () -> {
            unregisterUndo.undo();
            ref = prevRef;
        };
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
}
