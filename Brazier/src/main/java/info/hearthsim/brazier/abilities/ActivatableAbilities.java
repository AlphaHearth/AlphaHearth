package info.hearthsim.brazier.abilities;

import info.hearthsim.brazier.Hero;
import info.hearthsim.brazier.Player;
import info.hearthsim.brazier.PlayerProperty;
import info.hearthsim.brazier.WorldProperty;
import info.hearthsim.brazier.cards.Card;
import info.hearthsim.brazier.minions.Minion;
import info.hearthsim.brazier.parsing.NamedArg;
import info.hearthsim.brazier.weapons.Weapon;
import org.jtrim.utils.ExceptionHelper;

/**
 * Predefined {@link ActivatableAbility}s.
 * <p>
 * More predefined {@code ActivatableAbility}s for {@link Minion}s can be found in {@link MinionAbilities}.
 *
 * @see MinionAbilities
 */
public final class ActivatableAbilities {

    /**
     * Returns an {@code ActivatableAbility} which adds a {@link TargetedActiveAura} with the given {@link Aura}
     * and a {@link AuraTargetProvider} which returns the aura source to
     * the {@code World} when activated.
     */
    public static <Self extends WorldProperty> ActivatableAbility<Self> selfAura(
        @NamedArg("aura") Aura<? super Self, ? super Self> aura) {
        return selfAura(AuraFilter.ANY, aura);
    }

    /**
     * Returns an {@code ActivatableAbility} which adds a {@link TargetedActiveAura} with the given {@link AuraFilter},
     * {@link Aura} and a {@link AuraTargetProvider} which returns the aura source to
     * the {@code World} when activated.
     */
    public static <Self extends WorldProperty> ActivatableAbility<Self> selfAura(
        @NamedArg("filter") AuraFilter<? super Self, ? super Self> filter,
        @NamedArg("aura") Aura<? super Self, ? super Self> aura) {
        return aura(AuraTargetProviders.selfProvider(), filter, aura);
    }

    /**
     * Returns an {@code ActivatableAbility} which adds a {@link TargetedActiveAura} with the given {@link Aura}
     * and a {@link AuraTargetProvider} which returns minions on the same board side as the aura source to
     * the {@code World} when activated.
     */
    public static <Self extends PlayerProperty> ActivatableAbility<Self> sameBoardAura(
        @NamedArg("aura") Aura<? super Self, ? super Minion> aura) {
        return aura(AuraTargetProviders.SAME_BOARD_MINION_PROVIDER, AuraFilter.ANY, aura);
    }

    /**
     * Returns an {@code ActivatableAbility} which adds a {@link TargetedActiveAura} with the given {@link AuraFilter},
     * {@link Aura} and a {@link AuraTargetProvider} which returns minions on the same board side as the aura source to
     * the {@code World} when activated.
     */
    public static <Self extends PlayerProperty> ActivatableAbility<Self> sameBoardAura(
        @NamedArg("filter") AuraFilter<? super Self, ? super Minion> filter,
        @NamedArg("aura") Aura<? super Self, ? super Minion> aura) {
        return aura(AuraTargetProviders.SAME_BOARD_MINION_PROVIDER, filter, aura);
    }

    /**
     * Returns an {@code ActivatableAbility} which adds a {@link TargetedActiveAura} with the given {@link Aura}
     * and a {@link AuraTargetProvider} which returns minions on the board to
     * the {@code World} when activated.
     */
    public static <Self extends WorldProperty> ActivatableAbility<Self> boardAura(
        @NamedArg("aura") Aura<? super Self, ? super Minion> aura) {
        return aura(AuraTargetProviders.MINION_PROVIDER, AuraFilter.ANY, aura);
    }

    /**
     * Returns an {@code ActivatableAbility} which adds a {@link TargetedActiveAura} with the given {@link AuraFilter},
     * {@link Aura} and a {@link AuraTargetProvider} which returns minions on the board to
     * the {@code World} when activated.
     */
    public static <Self extends WorldProperty> ActivatableAbility<Self> boardAura(
        @NamedArg("filter") AuraFilter<? super Self, ? super Minion> filter,
        @NamedArg("aura") Aura<? super Self, ? super Minion> aura) {
        return aura(AuraTargetProviders.MINION_PROVIDER, filter, aura);
    }

    /**
     * Returns an {@code ActivatableAbility} which adds a {@link TargetedActiveAura} with the given {@link Aura}
     * and a {@link AuraTargetProvider} which returns cards in the hand of the aura source's owner to
     * the {@code World} when activated.
     */
    public static <Self extends PlayerProperty> ActivatableAbility<Self> ownCardAura(
        @NamedArg("aura") Aura<? super Self, ? super Card> aura) {
        return aura(AuraTargetProviders.OWN_HAND_PROVIDER, AuraFilter.ANY, aura);
    }

    /**
     * Returns an {@code ActivatableAbility} which adds a {@link TargetedActiveAura} with the given {@link AuraFilter},
     * {@link Aura} and a {@link AuraTargetProvider} which returns cards in the hand of the aura source's owner to
     * the {@code World} when activated.
     */
    public static <Self extends PlayerProperty> ActivatableAbility<Self> ownCardAura(
        @NamedArg("filter") AuraFilter<? super Self, ? super Card> filter,
        @NamedArg("aura") Aura<? super Self, ? super Card> aura) {
        return aura(AuraTargetProviders.OWN_HAND_PROVIDER, filter, aura);
    }

    /**
     * Returns an {@code ActivatableAbility} which adds a {@link TargetedActiveAura} with the given {@link Aura}
     * and a {@link AuraTargetProvider} which returns cards in the hands of both players to the aura
     * source's {@code World} when activated.
     */
    public static <Self extends WorldProperty> ActivatableAbility<Self> cardAura(
        @NamedArg("aura") Aura<? super Self, ? super Card> aura) {
        return aura(AuraTargetProviders.HAND_PROVIDER, AuraFilter.ANY, aura);
    }

    /**
     * Returns an {@code ActivatableAbility} which adds a {@link TargetedActiveAura} with the given {@link AuraFilter},
     * {@link Aura} and a {@link AuraTargetProvider} which returns cards in the hands of both players to the aura
     * source's {@code World} when activated.
     */
    public static <Self extends WorldProperty> ActivatableAbility<Self> cardAura(
        @NamedArg("filter") AuraFilter<? super Self, ? super Card> filter,
        @NamedArg("aura") Aura<? super Self, ? super Card> aura) {
        return aura(AuraTargetProviders.HAND_PROVIDER, filter, aura);
    }

    /**
     * Returns an {@code ActivatableAbility} which adds a {@link TargetedActiveAura} with the given {@link Aura}
     * and a {@link AuraTargetProvider} which returns the owner's hero of the aura source to the aura
     * source's {@code World} when activated.
     */
    public static <Self extends PlayerProperty> ActivatableAbility<Self> ownHeroAura(
        @NamedArg("aura") Aura<? super Self, ? super Hero> aura) {
        return aura(AuraTargetProviders.OWN_HERO_PROVIDER, AuraFilter.ANY, aura);
    }

    /**
     * Returns an {@code ActivatableAbility} which adds a {@link TargetedActiveAura} with the given {@link AuraFilter},
     * {@link Aura} and a {@link AuraTargetProvider} which returns the owner's hero of the aura source to the aura
     * source's {@code World} when activated.
     */
    public static <Self extends PlayerProperty> ActivatableAbility<Self> ownHeroAura(
        @NamedArg("filter") AuraFilter<? super Self, ? super Hero> filter,
        @NamedArg("aura") Aura<? super Self, ? super Hero> aura) {
        return aura(AuraTargetProviders.OWN_HERO_PROVIDER, filter, aura);
    }

    /**
     * Returns an {@code ActivatableAbility} which adds a {@link TargetedActiveAura} with the given {@link Aura}
     * and a {@link AuraTargetProvider} which returns both players' heroes to the aura source's
     * {@code World} when activated.
     */
    public static <Self extends WorldProperty> ActivatableAbility<Self> heroAura(
        @NamedArg("aura") Aura<? super Self, ? super Hero> aura) {
        return aura(AuraTargetProviders.HERO_PROVIDER, AuraFilter.ANY, aura);
    }

    /**
     * Returns an {@code ActivatableAbility} which adds a {@link TargetedActiveAura} with the given {@link AuraFilter},
     * {@link Aura} and a {@link AuraTargetProvider} which returns both players' heroes to the aura source's
     * {@code World} when activated.
     */
    public static <Self extends WorldProperty> ActivatableAbility<Self> heroAura(
        @NamedArg("filter") AuraFilter<? super Self, ? super Hero> filter,
        @NamedArg("aura") Aura<? super Self, ? super Hero> aura) {
        return aura(AuraTargetProviders.HERO_PROVIDER, filter, aura);
    }

    /**
     * Returns an {@code ActivatableAbility} which adds a {@link TargetedActiveAura} with the given {@link Aura}
     * and a {@link AuraTargetProvider} which returns the owning player of the aura source
     * to the {@code World} when activated.
     */
    public static <Self extends PlayerProperty> ActivatableAbility<Self> ownPlayerAura(
        @NamedArg("aura") Aura<? super Self, ? super Player> aura) {
        return aura(AuraTargetProviders.OWN_PLAYER_PROVIDER, AuraFilter.ANY, aura);
    }

    /**
     * Returns an {@code ActivatableAbility} which adds a {@link TargetedActiveAura} with the given {@link AuraFilter},
     * {@link Aura} and a {@link AuraTargetProvider} which returns the owning player of the aura source
     * to the {@code World} when activated.
     */
    public static <Self extends PlayerProperty> ActivatableAbility<Self> ownPlayerAura(
        @NamedArg("filter") AuraFilter<? super Self, ? super Player> filter,
        @NamedArg("aura") Aura<? super Self, ? super Player> aura) {
        return aura(AuraTargetProviders.OWN_PLAYER_PROVIDER, filter, aura);
    }

    /**
     * Returns an {@code ActivatableAbility} which adds a {@link TargetedActiveAura} with the given {@link Aura}
     * and a {@link AuraTargetProvider} which returns both players to the {@code World}
     * when activated.
     */
    public static <Self extends WorldProperty> ActivatableAbility<Self> playerAura(
        @NamedArg("aura") Aura<? super Self, ? super Player> aura) {
        return aura(AuraTargetProviders.PLAYER_PROVIDER, AuraFilter.ANY, aura);
    }

    /**
     * Returns an {@code ActivatableAbility} which adds a {@link TargetedActiveAura} with the given {@link AuraFilter},
     * {@link Aura} and a {@link AuraTargetProvider} which returns both players to the {@code World}
     * when activated.
     */
    public static <Self extends WorldProperty> ActivatableAbility<Self> playerAura(
        @NamedArg("filter") AuraFilter<? super Self, ? super Player> filter,
        @NamedArg("aura") Aura<? super Self, ? super Player> aura) {
        return aura(AuraTargetProviders.PLAYER_PROVIDER, filter, aura);
    }

    /**
     * Returns an {@code ActivatableAbility} which adds a {@link TargetedActiveAura} with the given {@link Aura},
     * and a {@link AuraTargetProvider} which returns the weapon equipped by the owner of the aura source
     * to the {@code World} when activated.
     */
    public static <Self extends PlayerProperty> ActivatableAbility<Self> ownWeaponAura(
        @NamedArg("aura") Aura<? super Self, ? super Weapon> aura) {
        return aura(AuraTargetProviders.OWN_WEAPON_PROVIDER, AuraFilter.ANY, aura);
    }

    /**
     * Returns an {@code ActivatableAbility} which adds a {@link TargetedActiveAura} with the given {@link AuraFilter},
     * {@link Aura} and a {@link AuraTargetProvider} which returns the weapon equipped by the owner of the aura source
     * to the {@code World} when activated.
     */
    public static <Self extends PlayerProperty> ActivatableAbility<Self> ownWeaponAura(
        @NamedArg("filter") AuraFilter<? super Self, ? super Weapon> filter,
        @NamedArg("aura") Aura<? super Self, ? super Weapon> aura) {
        return aura(AuraTargetProviders.OWN_WEAPON_PROVIDER, filter, aura);
    }

    /**
     * Returns an {@code ActivatableAbility} which adds a {@link TargetedActiveAura} with the given {@link Aura},
     * and a {@link AuraTargetProvider} which returns weapon(s) equipped by players to the aura source's
     * {@code World} when activated.
     */
    public static <Self extends WorldProperty> ActivatableAbility<Self> weaponAura(
        @NamedArg("aura") Aura<? super Self, ? super Weapon> aura) {
        return aura(AuraTargetProviders.WEAPON_PROVIDER, AuraFilter.ANY, aura);
    }

    /**
     * Returns an {@code ActivatableAbility} which adds a {@link TargetedActiveAura} with the given {@link AuraFilter},
     * {@link Aura} and a {@link AuraTargetProvider} which returns weapon(s) equipped by players to the aura source's
     * {@code World} when activated.
     */
    public static <Self extends WorldProperty> ActivatableAbility<Self> weaponAura(
        @NamedArg("filter") AuraFilter<? super Self, ? super Weapon> filter,
        @NamedArg("aura") Aura<? super Self, ? super Weapon> aura) {
        return aura(AuraTargetProviders.WEAPON_PROVIDER, filter, aura);
    }

    /**
     * Returns an {@code ActivatableAbility} which adds a {@link TargetedActiveAura} with the given
     * {@link AuraTargetProvider}, {@link AuraFilter} and {@link Aura} to the {@code World}
     * when activated.
     */
    public static <Self extends WorldProperty, Target> ActivatableAbility<Self> aura(
        @NamedArg("target") AuraTargetProvider<? super Self, ? extends Target> target,
        @NamedArg("filter") AuraFilter<? super Self, ? super Target> filter,
        @NamedArg("aura") Aura<? super Self, ? super Target> aura) {

        ExceptionHelper.checkNotNullArgument(target, "target");
        ExceptionHelper.checkNotNullArgument(filter, "filter");
        ExceptionHelper.checkNotNullArgument(aura, "aura");

        return (Self self) -> self.getWorld().addAura(new TargetedActiveAura<>(self, target, filter, aura));
    }

    private ActivatableAbilities() {
        throw new AssertionError();
    }
}
