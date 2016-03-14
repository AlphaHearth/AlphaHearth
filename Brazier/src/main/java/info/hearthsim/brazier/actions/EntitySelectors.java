package info.hearthsim.brazier.actions;

import info.hearthsim.brazier.Hero;
import info.hearthsim.brazier.Keyword;
import info.hearthsim.brazier.PlayerProperty;
import info.hearthsim.brazier.cards.CardProvider;
import info.hearthsim.brazier.minions.Minion;
import info.hearthsim.brazier.minions.MinionDescr;
import info.hearthsim.brazier.Player;
import info.hearthsim.brazier.TargetableCharacter;
import info.hearthsim.brazier.World;
import info.hearthsim.brazier.cards.Card;
import info.hearthsim.brazier.cards.CardDescr;
import info.hearthsim.brazier.parsing.NamedArg;
import info.hearthsim.brazier.weapons.WeaponDescr;
import info.hearthsim.brazier.weapons.Weapon;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import org.jtrim.utils.ExceptionHelper;

/**
 * Predefined {@link EntitySelector}s.
 */
public final class EntitySelectors {
    /**
     * Returns a {@link EntitySelector} which returns nothing.
     */
    public static <Actor, Selection> EntitySelector<Actor, Selection> empty() {
        return (World world, Actor actor) -> Stream.empty();
    }

    /**
     * Returns a {@link EntitySelector} which returns the given actor.
     */
    public static <Actor, Target> EntitySelector<Actor, Actor> self() {
        return (World world, Actor actor) -> Stream.of(actor);
    }

    /**
     * Returns a {@link EntitySelector} which returns {@link CardDescr}s belong to the hero class of
     * the given actor's opponent.
     * <p>
     * See minion <em>Nefarian</em>.
     */
    public static <Actor extends PlayerProperty> EntitySelector<Actor, CardDescr> opponentClassCardsWithKeywords(
            @NamedArg("fallbackCard") CardProvider fallbackCard,
            @NamedArg("keywords") Keyword... keywords) {
        Keyword[] keywordsCopy = keywords.clone();
        ExceptionHelper.checkNotNullElements(keywordsCopy, "keywords");

        return (World world, Actor actor) -> {
            Keyword[] allKeywords = new Keyword[keywordsCopy.length + 1];
            allKeywords[0] = actor.getOwner().getOpponent().getHero().getHeroClass();
            System.arraycopy(keywordsCopy, 0, allKeywords, 1, keywordsCopy.length);

            List<CardDescr> cards = world.getDb().getCardDb().getByKeywords(allKeywords);
            if (fallbackCard != null && cards.isEmpty()) {
                return Stream.of(fallbackCard.getCard());
            }

            return cards.stream();
        };
    }

    /**
     * Returns a {@link EntitySelector} which returns {@link CardDescr}s with all of the given {@link Keyword}s.
     */
    public static <Actor> EntitySelector<Actor, CardDescr> cardsWithKeywords(
            @NamedArg("keywords") Keyword... keywords) {
        Keyword[] keywordsCopy = keywords.clone();
        ExceptionHelper.checkNotNullElements(keywordsCopy, "keywords");

        return (World world, Actor actor) -> {
            List<CardDescr> cards = world.getDb().getCardDb().getByKeywords(keywordsCopy);
            return cards.stream();
        };
    }

    /**
     * Returns a {@link EntitySelector} which returns {@link MinionDescr}s with all of the given {@link Keyword}s.
     */
    public static <Actor> EntitySelector<Actor, MinionDescr> minionsWithKeywords(
            @NamedArg("keywords") Keyword... keywords) {
        Keyword[] keywordsCopy = keywords.clone();
        ExceptionHelper.checkNotNullElements(keywordsCopy, "keywords");

        return (World world, Actor actor) -> {
            List<MinionDescr> cards = world.getDb().getMinionDb().getByKeywords(keywordsCopy);
            return cards.stream();
        };
    }

    /**
     * Returns a {@link EntitySelector} which returns {@link WeaponDescr}s with all of the given {@link Keyword}s.
     */
    public static <Actor> EntitySelector<Actor, WeaponDescr> weaponsWithKeywords(
            @NamedArg("keywords") Keyword... keywords) {
        Keyword[] keywordsCopy = keywords.clone();
        ExceptionHelper.checkNotNullElements(keywordsCopy, "keywords");

        return (World world, Actor actor) -> {
            List<WeaponDescr> cards = world.getDb().getWeaponDb().getByKeywords(keywordsCopy);
            return cards.stream();
        };
    }

    /**
     * Returns a {@link EntitySelector} which returns the selected results from the given {@code EntitySelector},
     * sorted according to the given {@link Comparator}.
     */
    public static <Actor, Selection> EntitySelector<Actor, Selection> sorted(
            EntitySelector<? super Actor, ? extends Selection> selector,
            Comparator<? super Selection> cmp) {
        ExceptionHelper.checkNotNullArgument(selector, "selector");
        ExceptionHelper.checkNotNullArgument(cmp, "cmp");

        return (World world, Actor actor) -> {
            Stream<? extends Selection> selection = selector.select(world, actor);
            return selection.sorted(cmp);
        };
    }

    /**
     * Returns a {@link EntitySelector} which returns the results selected by the given {@code EntitySelector}
     * which also pass the given {@link EntityFilter}.
     */
    public static <Actor, Selection> EntitySelector<Actor, Selection> filtered(
            @NamedArg("filter") EntityFilter<Selection> filter,
            @NamedArg("selector") EntitySelector<? super Actor, ? extends Selection> selector) {
        ExceptionHelper.checkNotNullArgument(filter, "filter");
        ExceptionHelper.checkNotNullArgument(selector, "selector");

        return (World world, Actor actor) -> {
            Stream<? extends Selection> selection = selector.select(world, actor);
            return filter.select(world, selection);
        };
    }

    /**
     * Returns a {@link EntitySelector} which returns the results selected by the given {@code EntitySelector}
     * which are also not the actor itself.
     */
    public static <Actor, Selection> EntitySelector<Actor, Selection> notSelf(
            @NamedArg("selector") EntitySelector<? super Actor, ? extends Selection> selector) {
        ExceptionHelper.checkNotNullArgument(selector, "selector");

        return (World world, Actor actor) -> {
            Stream<? extends Selection> selection = selector.select(world, actor);
            return selection.filter((entity) -> entity != actor);
        };
    }

    /**
     * Returns a {@link EntitySelector} which returns the results selected by the given {@code EntitySelector},
     * selecting from the opponent of the given actor.
     */
    public static <Actor extends PlayerProperty, Selection> EntitySelector<Actor, Selection> fromOpponent(
            @NamedArg("selector") EntitySelector<? super Player, ? extends Selection> selector) {
        ExceptionHelper.checkNotNullArgument(selector, "selector");
        return (World world, Actor actor) -> {
            return selector.select(world, actor.getOwner().getOpponent());
        };
    }

    /**
     * Returns a {@link EntitySelector} which returns the {@link Minion} of the given actor {@link Card}.
     */
    public static EntitySelector<Card, Minion> cardsMinion() {
        return (World world, Card card) -> {
            Minion minion = card.getMinion();
            return minion != null ? Stream.of(minion) : Stream.empty();
        };
    }

    /**
     * Returns a {@link EntitySelector} which returns the {@link Hero} of the actor's owner.
     */
    public static <Actor extends PlayerProperty> EntitySelector<Actor, Hero> friendlyHero() {
        return (World world, Actor actor) -> Stream.of(actor.getOwner().getHero());
    }

    /**
     * Returns a {@link EntitySelector} which returns the {@link Card}s in the hands of the actor's owner.
     */
    public static <Actor extends PlayerProperty> EntitySelector<Actor, Card> friendlyHand() {
        return (World world, Actor actor) -> actor.getOwner().getHand().getCards().stream();
    }

    /**
     * Returns a {@link EntitySelector} which returns the {@link Card}s in the deck of the actor's owner.
     */
    public static <Actor extends PlayerProperty> EntitySelector<Actor, Card> friendlyDeck() {
        return (World world, Actor actor) -> actor.getOwner().getDeck().getCards().stream();
    }

    /**
     * Returns a {@link EntitySelector} which returns the {@link Hero} of the actor's opponent.
     */
    public static <Actor extends PlayerProperty> EntitySelector<Actor, Hero> enemyHero() {
        return fromOpponent(friendlyHero());
    }

    /**
     * Returns a {@link EntitySelector} which returns the {@link Card}s in the hands of the actor's opponent.
     */
    public static <Actor extends PlayerProperty> EntitySelector<Actor, Card> enemyHand() {
        return fromOpponent(friendlyHand());
    }

    /**
     * Returns a {@link EntitySelector} which returns the {@link Card}s in the deck of the actor's opponent.
     */
    public static <Actor extends PlayerProperty> EntitySelector<Actor, Card> enemyDeck() {
        return fromOpponent(friendlyDeck());
    }

    /**
     * Returns a {@link EntitySelector} which returns the neighbouring {@link Minion}s
     * of the given actor {@link Minion}.
     */
    public static <Actor extends Minion> EntitySelector<Actor, Minion> neighbours() {
        TargetedEntitySelector<Actor, Minion, Minion> targetedSelector = TargetedEntitySelectors.targetsNeighbours();
        return (World world, Actor actor) -> {
            return targetedSelector.select(world, actor, actor);
        };
    }

    /**
     * Returns a {@link EntitySelector} which returns the {@link Weapon} of the actor's owner.
     */
    public static <Actor extends PlayerProperty> EntitySelector<Actor, Weapon> friendlyWeapon() {
        return (World world, Actor actor) -> {
            Weapon weapon = actor.getOwner().tryGetWeapon();
            return weapon != null ? Stream.of(weapon) : Stream.empty();
        };
    }

    /**
     * Returns a {@link EntitySelector} which returns the {@link Weapon} of the actor's opponent.
     */
    public static <Actor extends PlayerProperty> EntitySelector<Actor, Weapon> enemyWeapon() {
        return (World world, Actor actor) -> {
            Weapon weapon = actor.getOwner().getOpponent().tryGetWeapon();
            return weapon != null ? Stream.of(weapon) : Stream.empty();
        };
    }

    /* EntitySelectors for Minions */

    /**
     * Returns a {@link EntitySelector} which returns all {@link Minion}s.
     */
    public static <Actor extends PlayerProperty> EntitySelector<Actor, Minion> allMinions() {
        return EntitySelector.merge(Arrays.asList(friendlyMinions(), enemyMinions()));
    }

    /**
     * Returns a {@link EntitySelector} which returns all buffable {@link Minion}s.
     */
    public static <Actor extends PlayerProperty> EntitySelector<Actor, Minion> allBuffableMinions() {
        return EntitySelector.merge(Arrays.asList(friendlyBuffableMinions(), enemyBuffableMinions()));
    }

    /**
     * Returns a {@link EntitySelector} which returns all living {@link Minion}s.
     */
    public static <Actor extends PlayerProperty> EntitySelector<Actor, Minion> allLivingMinions() {
        return EntitySelector.merge(Arrays.asList(friendlyLivingMinions(), enemyLivingMinions()));
    }

    /**
     * Returns a {@link EntitySelector} which returns all the friendly {@link Minion}s.
     */
    public static <Actor extends PlayerProperty> EntitySelector<Actor, Minion> friendlyMinions() {
        return (World world, Actor actor) -> actor.getOwner().getBoard().getAllMinions().stream();
    }

    /**
     * Returns a {@link EntitySelector} which returns all the buffable friendly {@link Minion}s.
     */
    public static <Actor extends PlayerProperty> EntitySelector<Actor, Minion> friendlyBuffableMinions() {
        return filtered(EntityFilters.fromPredicate(EntityFilters.buffableMinion()), friendlyMinions());
    }

    /**
     * Returns a {@link EntitySelector} which returns all the living friendly {@link Minion}s.
     */
    public static <Actor extends PlayerProperty> EntitySelector<Actor, Minion> friendlyLivingMinions() {
        return filtered(EntityFilters.fromPredicate(EntityFilters.isAlive()), friendlyMinions());
    }

    /**
     * Returns a {@link EntitySelector} which returns the {@link Minion}s on the different board side than the actor.
     */
    public static <Actor extends PlayerProperty> EntitySelector<Actor, Minion> enemyMinions() {
        return fromOpponent(friendlyMinions());
    }

    /**
     * Returns a {@link EntitySelector} which returns the buffable {@link Minion}s
     * on the different board side than the actor.
     */
    public static <Actor extends PlayerProperty> EntitySelector<Actor, Minion> enemyBuffableMinions() {
        return fromOpponent(friendlyBuffableMinions());
    }

    /**
     * Returns a {@link EntitySelector} which returns the living {@link Minion}s
     * on the different board side than the actor.
     */
    public static <Actor extends PlayerProperty> EntitySelector<Actor, Minion> enemyLivingMinions() {
        return fromOpponent(friendlyLivingMinions());
    }

    /* EntitySelectors for TargetableCharactors */

    /**
     * Returns a {@link EntitySelector} which returns all {@link TargetableCharacter},
     * including {@link Hero} and {@link Minion}s.
     */
    public static <Actor extends PlayerProperty> EntitySelector<Actor, TargetableCharacter> allTargets() {
        return EntitySelector.merge(Arrays.asList(friendlyTargets(), enemyTargets()));
    }

    /**
     * Returns a {@link EntitySelector} which returns all buffable {@link TargetableCharacter},
     * including {@link Hero} and {@link Minion}s.
     */
    public static <Actor extends PlayerProperty> EntitySelector<Actor, TargetableCharacter> allBuffableTargets() {
        return EntitySelector.merge(Arrays.asList(friendlyBuffableTargets(), enemyBuffableTargets()));
    }

    /**
     * Returns a {@link EntitySelector} which returns all living {@link TargetableCharacter},
     * including {@link Hero} and {@link Minion}s.
     */
    public static <Actor extends PlayerProperty> EntitySelector<Actor, TargetableCharacter> allLivingTargets() {
        return EntitySelector.merge(Arrays.asList(friendlyLivingTargets(), enemyLivingTargets()));
    }

    /**
     * Returns a {@link EntitySelector} which returns all enemy {@link TargetableCharacter}s,
     * including {@link Hero} and {@link Minion}s.
     */
    public static <Actor extends PlayerProperty> EntitySelector<Actor, TargetableCharacter> enemyTargets() {
        return EntitySelector.merge(Arrays.asList(enemyMinions(), enemyHero()));
    }

    /**
     * Returns a {@link EntitySelector} which returns all enemy buffable {@link TargetableCharacter}s,
     * including {@link Hero} and {@link Minion}s.
     */
    public static <Actor extends PlayerProperty> EntitySelector<Actor, TargetableCharacter> enemyBuffableTargets() {
        return EntitySelector.merge(Arrays.asList(enemyBuffableMinions(), enemyHero()));
    }

    /**
     * Returns a {@link EntitySelector} which returns all enemy living {@link TargetableCharacter}s,
     * including {@link Hero} and {@link Minion}s.
     */
    public static <Actor extends PlayerProperty> EntitySelector<Actor, TargetableCharacter> enemyLivingTargets() {
        return EntitySelector.merge(Arrays.asList(enemyLivingMinions(), enemyHero()));
    }

    /**
     * Returns a {@link EntitySelector} which returns all friendly {@link TargetableCharacter}s,
     * including {@link Hero} and {@link Minion}s.
     */
    public static <Actor extends PlayerProperty> EntitySelector<Actor, TargetableCharacter> friendlyTargets() {
        return EntitySelector.merge(Arrays.asList(friendlyMinions(), friendlyHero()));
    }

    /**
     * Returns a {@link EntitySelector} which returns all friendly buffable {@link TargetableCharacter}s,
     * including {@link Hero} and {@link Minion}s.
     */
    public static <Actor extends PlayerProperty> EntitySelector<Actor, TargetableCharacter> friendlyBuffableTargets() {
        return EntitySelector.merge(Arrays.asList(friendlyBuffableMinions(), friendlyHero()));
    }

    /**
     * Returns a {@link EntitySelector} which returns all friendly living {@link TargetableCharacter}s,
     * including {@link Hero} and {@link Minion}s.
     */
    public static <Actor extends PlayerProperty> EntitySelector<Actor, TargetableCharacter> friendlyLivingTargets() {
        return EntitySelector.merge(Arrays.asList(friendlyLivingMinions(), friendlyHero()));
    }

    private EntitySelectors() {
        throw new AssertionError();
    }
}
