package info.hearthsim.brazier;

import info.hearthsim.brazier.abilities.ActivatableAbilityList;
import info.hearthsim.brazier.actions.undo.UndoAction;

public final class CharacterAbilities<Self extends WorldProperty> implements Silencable {
    private final ActivatableAbilityList<Self> ownedAbilities;
    private final ActivatableAbilityList<Self> externalAbilities;

    public CharacterAbilities(Self self) {
        this.ownedAbilities = new ActivatableAbilityList<>(self);
        this.externalAbilities = new ActivatableAbilityList<>(self);
    }

    private CharacterAbilities(ActivatableAbilityList<Self> ownedAbilities, ActivatableAbilityList<Self> externalAbilities) {
        this.ownedAbilities = ownedAbilities;
        this.externalAbilities = externalAbilities;
    }

    public PreparedResult<CharacterAbilities<Self>> copyFor(Self other) {
        PreparedResult<ActivatableAbilityList<Self>> newOwnedAbilities = ownedAbilities.copyFor(other);
        ActivatableAbilityList<Self> newExternalAbilities = new ActivatableAbilityList<>(other);
        CharacterAbilities<Self> result = new CharacterAbilities<>(
                newOwnedAbilities.getResult(),
                newExternalAbilities);

        return new PreparedResult<>(result, newOwnedAbilities::activate);
    }

    public ActivatableAbilityList<Self> getOwned() {
        return ownedAbilities;
    }

    public ActivatableAbilityList<Self> getExternal() {
        return externalAbilities;
    }

    @Override
    public UndoAction silence() {
        return ownedAbilities.deactivate();
    }

    public UndoAction deactivateAll() {
        return ownedAbilities.deactivate();
    }
}
