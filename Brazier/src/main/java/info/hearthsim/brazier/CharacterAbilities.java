package info.hearthsim.brazier;

import info.hearthsim.brazier.abilities.AbilityList;
import info.hearthsim.brazier.actions.undo.UndoAction;

public final class CharacterAbilities<Self extends WorldProperty> implements Silencable {
    private final AbilityList<Self> ownedAbilities;
    private final AbilityList<Self> externalAbilities;

    public CharacterAbilities(Self self) {
        this.ownedAbilities = new AbilityList<>(self);
        this.externalAbilities = new AbilityList<>(self);
    }

    private CharacterAbilities(AbilityList<Self> ownedAbilities, AbilityList<Self> externalAbilities) {
        this.ownedAbilities = ownedAbilities;
        this.externalAbilities = externalAbilities;
    }

    public PreparedResult<CharacterAbilities<Self>> copyFor(Self other) {
        PreparedResult<AbilityList<Self>> newOwnedAbilities = ownedAbilities.copyFor(other);
        AbilityList<Self> newExternalAbilities = new AbilityList<>(other);
        CharacterAbilities<Self> result = new CharacterAbilities<>(
                newOwnedAbilities.getResult(),
                newExternalAbilities);

        return new PreparedResult<>(result, newOwnedAbilities::activate);
    }

    public AbilityList<Self> getOwned() {
        return ownedAbilities;
    }

    public AbilityList<Self> getExternal() {
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
