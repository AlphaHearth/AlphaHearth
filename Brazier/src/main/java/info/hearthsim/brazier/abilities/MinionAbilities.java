package info.hearthsim.brazier.abilities;

import info.hearthsim.brazier.game.Entity;
import info.hearthsim.brazier.util.UndoAction;
import info.hearthsim.brazier.game.minions.Minion;
import info.hearthsim.brazier.parsing.NamedArg;

/**
 * Predefined {@link Ability}s for {@link Minion}s.
 * <p>
 * For more general predefined {@code Ability}s, see {@link Abilities}.
 *
 * @see Abilities
 */
public final class MinionAbilities {
    /**
     * Returns an {@link Ability} which adds given number of spell power to the player.
     * <p>
     * See minion <em>Malygos</em>.
     */
    public static Ability<Entity> spellPower(@NamedArg("spellPower") int spellPower) {
        return (Entity self) -> {
            return UndoAction.of(self, (e) -> e.getOwner().getSpellPower(), (sp) -> sp.addExternalBuff(spellPower));
        };
    }

    /**
     * Returns an {@link Ability} which multiplies the spell power of the player by the given number
     * of time.
     * <p>
     * See minion <em>Prophet Velen</em>.
     */
    public static Ability<Entity> spellMultiplier(@NamedArg("mul") int mul) {
        return (Entity self) -> {
            return UndoAction.of(self, (e) -> e.getOwner().getSpellPower(),
                (sp) -> sp.addExternalBuff((prev) -> prev * mul));
        };
    }

    /**
     * Returns an {@link Ability} which applies the given {@link Aura} to adjacent minions.
     * <p>
     * See minion <em>Flametongue Totem</em>.
     */
    public static Ability<Minion> neighboursAura(
            @NamedArg("aura") Aura<? super Minion, ? super Minion> aura) {
        return neighboursAura(AuraFilter.ANY, aura);
    }

    /**
     * Returns an {@link Ability} with the given filter which applies the given {@link Aura}
     * to adjacent minions.
     * <p>
     * See minion <em>Flametongue Totem</em>.
     */
    public static Ability<Minion> neighboursAura(
            @NamedArg("filter") AuraFilter<? super Minion, ? super Minion> filter,
            @NamedArg("aura") Aura<? super Minion, ? super Minion> aura) {
        return Abilities.aura(AuraTargetProviders.NEIGHBOURS_MINION_PROVIDER, filter, aura);
    }

    /**
     * Returns an {@link Ability} which applies the given {@link Aura} to other friendly minions.
     * <p>
     * See minion <em>Raid Leader</em>.
     */
    public static Ability<Minion> sameBoardOthersAura(
            @NamedArg("aura") Aura<? super Minion, ? super Minion> aura) {
        return sameBoardOthersAura(AuraFilter.ANY, aura);
    }

    /**
     * Returns an {@link Ability} with the given filter which applies the given {@link Aura}
     * to other friendly minions.
     * <p>
     * See minion <em>Raid Leader</em>.
     */
    public static Ability<Minion> sameBoardOthersAura(
            @NamedArg("filter") AuraFilter<? super Minion, ? super Minion> filter,
            @NamedArg("aura") Aura<? super Minion, ? super Minion> aura) {
        return Abilities.sameBoardAura(AuraFilter.and(AuraFilters.SAME_OWNER_OTHERS, filter), aura);
    }

    private MinionAbilities() {
        throw new AssertionError();
    }
}
