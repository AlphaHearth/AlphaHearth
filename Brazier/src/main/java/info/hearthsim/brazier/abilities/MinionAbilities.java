package info.hearthsim.brazier.abilities;

import info.hearthsim.brazier.PlayerProperty;
import info.hearthsim.brazier.minions.Minion;
import info.hearthsim.brazier.parsing.NamedArg;

/**
 * Predefined {@link ActivatableAbility}s for {@link Minion}s.
 * <p>
 * For more general predefined {@code ActivatableAbility}s, see {@link ActivatableAbilities}.
 *
 * @see ActivatableAbilities
 */
public final class MinionAbilities {
    /**
     * Returns an {@link ActivatableAbility} which adds given number of spell power to the player.
     * <p>
     * See minion <em>Malygos</em>.
     */
    public static ActivatableAbility<PlayerProperty> spellPower(@NamedArg("spellPower") int spellPower) {
        return (PlayerProperty self) -> {
            AuraAwareIntProperty playersSpellPower = self.getOwner().getSpellPower();
            return playersSpellPower.addExternalBuff(spellPower);
        };
    }

    /**
     * Returns an {@link ActivatableAbility} which multiplies the spell power of the player by the given number
     * of time.
     * <p>
     * See minion <em>Prophet Velen</em>.
     */
    public static ActivatableAbility<PlayerProperty> spellMultiplier(@NamedArg("mul") int mul) {
        return (PlayerProperty self) -> {
            AuraAwareIntProperty playersSpellPower = self.getOwner().getHeroDamageMultiplier();
            return playersSpellPower.addExternalBuff((prev) -> prev * mul);
        };
    }

    /**
     * Returns an {@link ActivatableAbility} which applies the given {@link Aura} to adjacent minions.
     * <p>
     * See minion <em>Flametongue Totem</em>.
     */
    public static ActivatableAbility<Minion> neighboursAura(
            @NamedArg("aura") Aura<? super Minion, ? super Minion> aura) {
        return neighboursAura(AuraFilter.ANY, aura);
    }

    /**
     * Returns an {@link ActivatableAbility} with the given filter which applies the given {@link Aura}
     * to adjacent minions.
     * <p>
     * See minion <em>Flametongue Totem</em>.
     */
    public static ActivatableAbility<Minion> neighboursAura(
            @NamedArg("filter") AuraFilter<? super Minion, ? super Minion> filter,
            @NamedArg("aura") Aura<? super Minion, ? super Minion> aura) {
        return ActivatableAbilities.aura(AuraTargetProviders.NEIGHBOURS_MINION_PROVIDER, filter, aura);
    }

    /**
     * Returns an {@link ActivatableAbility} which applies the given {@link Aura} to other friendly minions.
     * <p>
     * See minion <em>Raid Leader</em>.
     */
    public static ActivatableAbility<Minion> sameBoardOthersAura(
            @NamedArg("aura") Aura<? super Minion, ? super Minion> aura) {
        return sameBoardOthersAura(AuraFilter.ANY, aura);
    }

    /**
     * Returns an {@link ActivatableAbility} with the given filter which applies the given {@link Aura}
     * to other friendly minions.
     * <p>
     * See minion <em>Raid Leader</em>.
     */
    public static ActivatableAbility<Minion> sameBoardOthersAura(
            @NamedArg("filter") AuraFilter<? super Minion, ? super Minion> filter,
            @NamedArg("aura") Aura<? super Minion, ? super Minion> aura) {
        return ActivatableAbilities.sameBoardAura(AuraFilter.and(AuraFilters.SAME_OWNER_OTHERS, filter), aura);
    }

    private MinionAbilities() {
        throw new AssertionError();
    }
}
