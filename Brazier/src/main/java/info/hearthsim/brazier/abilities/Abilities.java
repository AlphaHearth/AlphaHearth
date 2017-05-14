package info.hearthsim.brazier.abilities;

import info.hearthsim.brazier.game.Character;
import info.hearthsim.brazier.game.*;
import info.hearthsim.brazier.game.cards.Card;
import info.hearthsim.brazier.game.minions.Minion;
import info.hearthsim.brazier.game.weapons.Weapon;
import info.hearthsim.brazier.parsing.NamedArg;
import info.hearthsim.brazier.util.UndoAction;
import org.jtrim.utils.ExceptionHelper;

/**
 * Predefined {@link Ability}s.
 * <p>
 * More predefined {@code Ability}s for {@link Minion}s can be found in {@link MinionAbilities}.
 *
 * @see MinionAbilities
 */
public final class Abilities {

    /**
     * Returns an {@code Ability} which adds a {@link ActiveAura} with the given {@link Aura}
     * and a {@link AuraTargetProvider} which returns the aura source to
     * the {@code Game} when activated.
     */
    public static <Self extends Character> Ability<Self> selfAura(
        @NamedArg("aura") Aura<? super Self, ? super Self> aura) {
        return selfAura(AuraFilter.ANY, aura);
    }

    /**
     * Returns an {@code Ability} which adds a {@link ActiveAura} with the given {@link AuraFilter},
     * {@link Aura} and a {@link AuraTargetProvider} which returns the aura source to
     * the {@code Game} when activated.
     */
    public static <Self extends Character> Ability<Self> selfAura(
        @NamedArg("filter") AuraFilter<? super Self, ? super Self> filter,
        @NamedArg("aura") Aura<? super Self, ? super Self> aura) {
        return aura(AuraTargetProviders.selfProvider(), filter, aura);
    }

    /**
     * Returns an {@code Ability} which adds a {@link ActiveAura} with the given {@link Aura}
     * and a {@link AuraTargetProvider} which returns minions on the same board side as the aura source to
     * the {@code Game} when activated.
     */
    public static <Self extends Entity> Ability<Self> sameBoardAura(
        @NamedArg("aura") Aura<? super Self, ? super Minion> aura) {
        return aura(AuraTargetProviders.SAME_BOARD_MINION_PROVIDER, AuraFilter.ANY, aura);
    }

    /**
     * Returns an {@code Ability} which adds a {@link ActiveAura} with the given {@link AuraFilter},
     * {@link Aura} and a {@link AuraTargetProvider} which returns minions on the same board side as the
     * aura source to the {@code Game} when activated.
     */
    public static <Self extends Entity> Ability<Self> sameBoardAura(
        @NamedArg("filter") AuraFilter<? super Self, ? super Minion> filter,
        @NamedArg("aura") Aura<? super Self, ? super Minion> aura) {
        return aura(AuraTargetProviders.SAME_BOARD_MINION_PROVIDER, filter, aura);
    }

    /**
     * Returns an {@code Ability} which adds a {@link ActiveAura} with the given {@link Aura}
     * and a {@link AuraTargetProvider} which returns minions on the board to
     * the {@code Game} when activated.
     */
    public static <Self extends Entity> Ability<Self> boardAura(
        @NamedArg("aura") Aura<? super Self, ? super Minion> aura) {
        return aura(AuraTargetProviders.MINION_PROVIDER, AuraFilter.ANY, aura);
    }

    /**
     * Returns an {@code Ability} which adds a {@link ActiveAura} with the given {@link AuraFilter},
     * {@link Aura} and a {@link AuraTargetProvider} which returns minions on the board to
     * the {@code Game} when activated.
     */
    public static <Self extends Entity> Ability<Self> boardAura(
        @NamedArg("filter") AuraFilter<? super Self, ? super Minion> filter,
        @NamedArg("aura") Aura<? super Self, ? super Minion> aura) {
        return aura(AuraTargetProviders.MINION_PROVIDER, filter, aura);
    }

    /**
     * Returns an {@code Ability} which adds a {@link ActiveAura} with the given {@link Aura}
     * and a {@link AuraTargetProvider} which returns cards in the hand of the aura source's owner to
     * the {@code Game} when activated.
     */
    public static <Self extends Entity> Ability<Self> ownCardAura(
        @NamedArg("aura") Aura<? super Self, ? super Card> aura) {
        return aura(AuraTargetProviders.OWN_HAND_PROVIDER, AuraFilter.ANY, aura);
    }

    /**
     * Returns an {@code Ability} which adds a {@link ActiveAura} with the given {@link AuraFilter},
     * {@link Aura} and a {@link AuraTargetProvider} which returns cards in the hand of the aura source's owner to
     * the {@code Game} when activated.
     */
    public static <Self extends Entity> Ability<Self> ownCardAura(
        @NamedArg("filter") AuraFilter<? super Self, ? super Card> filter,
        @NamedArg("aura") Aura<? super Self, ? super Card> aura) {
        return aura(AuraTargetProviders.OWN_HAND_PROVIDER, filter, aura);
    }

    /**
     * Returns an {@code Ability} which adds a {@link ActiveAura} with the given {@link Aura}
     * and a {@link AuraTargetProvider} which returns cards in the hands of both players to the aura
     * source's {@code Game} when activated.
     */
    public static <Self extends Entity> Ability<Self> cardAura(
        @NamedArg("aura") Aura<? super Self, ? super Card> aura) {
        return aura(AuraTargetProviders.HAND_PROVIDER, AuraFilter.ANY, aura);
    }

    /**
     * Returns an {@code Ability} which adds a {@link ActiveAura} with the given {@link AuraFilter},
     * {@link Aura} and a {@link AuraTargetProvider} which returns cards in the hands of both players to the aura
     * source's {@code Game} when activated.
     */
    public static <Self extends Entity> Ability<Self> cardAura(
        @NamedArg("filter") AuraFilter<? super Self, ? super Card> filter,
        @NamedArg("aura") Aura<? super Self, ? super Card> aura) {
        return aura(AuraTargetProviders.HAND_PROVIDER, filter, aura);
    }

    /**
     * Returns an {@code Ability} which adds a {@link ActiveAura} with the given {@link Aura}
     * and a {@link AuraTargetProvider} which returns the owner's hero of the aura source to the aura
     * source's {@code Game} when activated.
     */
    public static <Self extends Entity> Ability<Self> ownHeroAura(
        @NamedArg("aura") Aura<? super Self, ? super Hero> aura) {
        return aura(AuraTargetProviders.OWN_HERO_PROVIDER, AuraFilter.ANY, aura);
    }

    /**
     * Returns an {@code Ability} which adds a {@link ActiveAura} with the given {@link AuraFilter},
     * {@link Aura} and a {@link AuraTargetProvider} which returns the owner's hero of the aura source to the aura
     * source's {@code Game} when activated.
     */
    public static <Self extends Entity> Ability<Self> ownHeroAura(
        @NamedArg("filter") AuraFilter<? super Self, ? super Hero> filter,
        @NamedArg("aura") Aura<? super Self, ? super Hero> aura) {
        return aura(AuraTargetProviders.OWN_HERO_PROVIDER, filter, aura);
    }

    /**
     * Returns an {@code Ability} which adds a {@link ActiveAura} with the given {@link Aura}
     * and a {@link AuraTargetProvider} which returns both players' heroes to the aura source's
     * {@code Game} when activated.
     */
    public static <Self extends Entity> Ability<Self> heroAura(
        @NamedArg("aura") Aura<? super Self, ? super Hero> aura) {
        return aura(AuraTargetProviders.HERO_PROVIDER, AuraFilter.ANY, aura);
    }

    /**
     * Returns an {@code Ability} which adds a {@link ActiveAura} with the given {@link AuraFilter},
     * {@link Aura} and a {@link AuraTargetProvider} which returns both players' heroes to the aura source's
     * {@code Game} when activated.
     */
    public static <Self extends Entity> Ability<Self> heroAura(
        @NamedArg("filter") AuraFilter<? super Self, ? super Hero> filter,
        @NamedArg("aura") Aura<? super Self, ? super Hero> aura) {
        return aura(AuraTargetProviders.HERO_PROVIDER, filter, aura);
    }

    /**
     * Returns an {@code Ability} which adds a {@link ActiveAura} with the given {@link Aura}
     * and a {@link AuraTargetProvider} which returns the owning player of the aura source
     * to the {@code Game} when activated.
     */
    public static <Self extends Entity> Ability<Self> ownPlayerAura(
        @NamedArg("aura") Aura<? super Self, ? super Player> aura) {
        return aura(AuraTargetProviders.OWN_PLAYER_PROVIDER, AuraFilter.ANY, aura);
    }

    /**
     * Returns an {@code Ability} which adds a {@link ActiveAura} with the given {@link AuraFilter},
     * {@link Aura} and a {@link AuraTargetProvider} which returns the owning player of the aura source
     * to the {@code Game} when activated.
     */
    public static <Self extends Entity> Ability<Self> ownPlayerAura(
        @NamedArg("filter") AuraFilter<? super Self, ? super Player> filter,
        @NamedArg("aura") Aura<? super Self, ? super Player> aura) {
        return aura(AuraTargetProviders.OWN_PLAYER_PROVIDER, filter, aura);
    }

    /**
     * Returns an {@code Ability} which adds a {@link ActiveAura} with the given {@link Aura}
     * and a {@link AuraTargetProvider} which returns both players to the {@code Game}
     * when activated.
     */
    public static <Self extends Entity> Ability<Self> playerAura(
        @NamedArg("aura") Aura<? super Self, ? super Player> aura) {
        return aura(AuraTargetProviders.PLAYER_PROVIDER, AuraFilter.ANY, aura);
    }

    /**
     * Returns an {@code Ability} which adds a {@link ActiveAura} with the given {@link AuraFilter},
     * {@link Aura} and a {@link AuraTargetProvider} which returns both players to the {@code Game}
     * when activated.
     */
    public static <Self extends Entity> Ability<Self> playerAura(
        @NamedArg("filter") AuraFilter<? super Self, ? super Player> filter,
        @NamedArg("aura") Aura<? super Self, ? super Player> aura) {
        return aura(AuraTargetProviders.PLAYER_PROVIDER, filter, aura);
    }

    /**
     * Returns an {@code Ability} which adds a {@link ActiveAura} with the given {@link Aura},
     * and a {@link AuraTargetProvider} which returns the weapon equipped by the owner of the aura source
     * to the {@code Game} when activated.
     */
    public static <Self extends Entity> Ability<Self> ownWeaponAura(
        @NamedArg("aura") Aura<? super Self, ? super Weapon> aura) {
        return aura(AuraTargetProviders.OWN_WEAPON_PROVIDER, AuraFilter.ANY, aura);
    }

    /**
     * Returns an {@code Ability} which adds a {@link ActiveAura} with the given {@link AuraFilter},
     * {@link Aura} and a {@link AuraTargetProvider} which returns the weapon equipped by the owner of the aura source
     * to the {@code Game} when activated.
     */
    public static <Self extends Entity> Ability<Self> ownWeaponAura(
        @NamedArg("filter") AuraFilter<? super Self, ? super Weapon> filter,
        @NamedArg("aura") Aura<? super Self, ? super Weapon> aura) {
        return aura(AuraTargetProviders.OWN_WEAPON_PROVIDER, filter, aura);
    }

    /**
     * Returns an {@code Ability} which adds a {@link ActiveAura} with the given {@link Aura},
     * and a {@link AuraTargetProvider} which returns weapon(s) equipped by players to the aura source's
     * {@code Game} when activated.
     */
    public static <Self extends Entity> Ability<Self> weaponAura(
        @NamedArg("aura") Aura<? super Self, ? super Weapon> aura) {
        return aura(AuraTargetProviders.WEAPON_PROVIDER, AuraFilter.ANY, aura);
    }

    /**
     * Returns an {@code Ability} which adds a {@link ActiveAura} with the given {@link AuraFilter},
     * {@link Aura} and a {@link AuraTargetProvider} which returns weapon(s) equipped by players to the aura source's
     * {@code Game} when activated.
     */
    public static <Self extends Entity> Ability<Self> weaponAura(
        @NamedArg("filter") AuraFilter<? super Self, ? super Weapon> filter,
        @NamedArg("aura") Aura<? super Self, ? super Weapon> aura) {
        return aura(AuraTargetProviders.WEAPON_PROVIDER, filter, aura);
    }

    /**
     * Returns an {@code Ability} which adds a {@link ActiveAura} with the given
     * {@link AuraTargetProvider}, {@link AuraFilter} and {@link Aura} to the {@code Game}
     * when activated.
     */
    public static <Self extends Entity, Target extends Entity> Ability<Self> aura(
        @NamedArg("target") AuraTargetProvider<? super Self, ? extends Target> target,
        @NamedArg("filter") AuraFilter<? super Self, ? super Target> filter,
        @NamedArg("aura") Aura<? super Self, ? super Target> aura) {

        return aura(target, filter, aura, false);
    }

    public static <Self extends Entity, Target extends Entity> Ability<Self> aura(
        AuraTargetProvider<? super Self, ? extends Target> target,
        AuraFilter<? super Self, ? super Target> filter,
        Aura<? super Self, ? super Target> aura,
        boolean toCopy) {

        ExceptionHelper.checkNotNullArgument(target, "target");
        ExceptionHelper.checkNotNullArgument(filter, "filter");
        ExceptionHelper.checkNotNullArgument(aura, "aura");

        return (Self self) -> {
            UndoAction<Game> undoRef = self.getGame()
                .addAura(new ActiveAura<>(self, target, filter, aura), toCopy);
            return (s) -> {
                undoRef.undo(s.getGame());
            };
        };
    }

    private Abilities() {
        throw new AssertionError();
    }
}
