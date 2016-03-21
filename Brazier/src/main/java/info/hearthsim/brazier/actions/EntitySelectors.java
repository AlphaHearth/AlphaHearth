package info.hearthsim.brazier.actions;

import info.hearthsim.brazier.*;
import info.hearthsim.brazier.Character;
import info.hearthsim.brazier.cards.CardProvider;
import info.hearthsim.brazier.minions.Minion;
import info.hearthsim.brazier.minions.MinionDescr;
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
    public static <Actor extends GameProperty, Selection> EntitySelector<Actor, Selection> empty() {
        return (Actor actor) -> Stream.empty();
    }

    /**
     * Returns a {@link EntitySelector} which returns the given actor.
     */
    public static <Actor extends GameProperty, Target> EntitySelector<Actor, Actor> self() {
        return (Actor actor) -> Stream.of(actor);
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

        return (Actor actor) -> {
            Keyword[] allKeywords = new Keyword[keywordsCopy.length + 1];
            allKeywords[0] = actor.getOwner().getOpponent().getHero().getHeroClass();
            System.arraycopy(keywordsCopy, 0, allKeywords, 1, keywordsCopy.length);

            List<CardDescr> cards = actor.getGame().getDb().getCardDb().getByKeywords(allKeywords);
            if (fallbackCard != null && cards.isEmpty()) {
                return Stream.of(fallbackCard.getCard());
            }

            return cards.stream();
        };
    }

    /**
     * Returns a {@link EntitySelector} which returns {@link CardDescr}s with all of the given {@link Keyword}s.
     */
    public static <Actor extends GameProperty> EntitySelector<Actor, CardDescr> cardsWithKeywords(
            @NamedArg("keywords") Keyword... keywords) {
        Keyword[] keywordsCopy = keywords.clone();
        ExceptionHelper.checkNotNullElements(keywordsCopy, "keywords");

        return (Actor actor) -> {
            List<CardDescr> cards = actor.getGame().getDb().getCardDb().getByKeywords(keywordsCopy);
            return cards.stream();
        };
    }

    /**
     * Returns a {@link EntitySelector} which returns {@link MinionDescr}s with all of the given {@link Keyword}s.
     */
    public static <Actor extends GameProperty> EntitySelector<Actor, MinionDescr> minionsWithKeywords(
            @NamedArg("keywords") Keyword... keywords) {
        Keyword[] keywordsCopy = keywords.clone();
        ExceptionHelper.checkNotNullElements(keywordsCopy, "keywords");

        return (Actor actor) -> {
            List<MinionDescr> cards = actor.getGame().getDb().getMinionDb().getByKeywords(keywordsCopy);
            return cards.stream();
        };
    }

    /**
     * Returns a {@link EntitySelector} which returns {@link WeaponDescr}s with all of the given {@link Keyword}s.
     */
    public static <Actor extends GameProperty> EntitySelector<Actor, WeaponDescr> weaponsWithKeywords(
            @NamedArg("keywords") Keyword... keywords) {
        Keyword[] keywordsCopy = keywords.clone();
        ExceptionHelper.checkNotNullElements(keywordsCopy, "keywords");

        return (Actor actor) -> {
            List<WeaponDescr> cards = actor.getGame().getDb().getWeaponDb().getByKeywords(keywordsCopy);
            return cards.stream();
        };
    }

    /**
     * Returns a {@link EntitySelector} which returns the selected results from the given {@code EntitySelector},
     * sorted according to the given {@link Comparator}.
     */
    public static <Actor extends GameProperty, Selection> EntitySelector<Actor, Selection> sorted(
            EntitySelector<? super Actor, ? extends Selection> selector,
            Comparator<? super Selection> cmp) {
        ExceptionHelper.checkNotNullArgument(selector, "selector");
        ExceptionHelper.checkNotNullArgument(cmp, "cmp");

        return (Actor actor) -> {
            Stream<? extends Selection> selection = selector.select(actor);
            return selection.sorted(cmp);
        };
    }

    /**
     * Returns a {@link EntitySelector} which returns the results selected by the given {@code EntitySelector}
     * which also pass the given {@link EntityFilter}.
     */
    public static <Actor extends GameProperty, Selection> EntitySelector<Actor, Selection> filtered(
            @NamedArg("filter") EntityFilter<Selection> filter,
            @NamedArg("selector") EntitySelector<? super Actor, ? extends Selection> selector) {
        ExceptionHelper.checkNotNullArgument(filter, "filter");
        ExceptionHelper.checkNotNullArgument(selector, "selector");

        return (Actor actor) -> {
            Stream<? extends Selection> selection = selector.select(actor);
            return filter.select(actor.getGame(), selection);
        };
    }

    /**
     * Returns a {@link EntitySelector} which returns the results selected by the given {@code EntitySelector}
     * which are also not the actor itself.
     */
    public static <Actor extends GameProperty, Selection> EntitySelector<Actor, Selection> notSelf(
            @NamedArg("selector") EntitySelector<? super Actor, ? extends Selection> selector) {
        ExceptionHelper.checkNotNullArgument(selector, "selector");

        return (Actor actor) -> {
            Stream<? extends Selection> selection = selector.select(actor);
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
        return (Actor actor) -> {
            return selector.select(actor.getOwner().getOpponent());
        };
    }

    /**
     * Returns a {@link EntitySelector} which returns the {@link Minion} of the given actor {@link Card}.
     */
    public static EntitySelector<Card, Minion> cardsMinion() {
        return (Card card) -> {
            Minion minion = card.getMinion();
            return minion != null ? Stream.of(minion) : Stream.empty();
        };
    }

    /**
     * Returns a {@link EntitySelector} which returns the {@link Hero} of the actor's owner.
     */
    public static <Actor extends PlayerProperty> EntitySelector<Actor, Hero> friendlyHero() {
        return (Actor actor) -> Stream.of(actor.getOwner().getHero());
    }

    /**
     * Returns a {@link EntitySelector} which returns the {@link Card}s in the hands of the actor's owner.
     */
    public static <Actor extends PlayerProperty> EntitySelector<Actor, Card> friendlyHand() {
        return (Actor actor) -> actor.getOwner().getHand().getCards().stream();
    }

    /**
     * Returns a {@link EntitySelector} which returns the {@link Card}s in the deck of the actor's owner.
     */
    public static <Actor extends PlayerProperty> EntitySelector<Actor, Card> friendlyDeck() {
        return (Actor actor) -> actor.getOwner().getDeck().getCards().stream();
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
        return (Actor actor) -> {
            return targetedSelector.select(actor, actor);
        };
    }

    /**
     * Returns a {@link EntitySelector} which returns the {@link Weapon} of the actor's owner.
     */
    public static <Actor extends PlayerProperty> EntitySelector<Actor, Weapon> friendlyWeapon() {
        return (Actor actor) -> {
            Weapon weapon = actor.getOwner().tryGetWeapon();
            return weapon != null ? Stream.of(weapon) : Stream.empty();
        };
    }

    /**
     * Returns a {@link EntitySelector} which returns the {@link Weapon} of the actor's opponent.
     */
    public static <Actor extends PlayerProperty> EntitySelector<Actor, Weapon> enemyWeapon() {
        return (Actor actor) -> {
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
        return (Actor actor) -> actor.getOwner().getBoard().getAllMinions().stream();
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
     * Returns a {@link EntitySelector} which returns all {@link Character},
     * including {@link Hero} and {@link Minion}s.
     */
    public static <Actor extends PlayerProperty> EntitySelector<Actor, Character> allTargets() {
        return EntitySelector.merge(Arrays.asList(friendlyTargets(), enemyTargets()));
    }

    /**
     * Returns a {@link EntitySelector} which returns all buffable {@link Character},
     * including {@link Hero} and {@link Minion}s.
     */
    public static <Actor extends PlayerProperty> EntitySelector<Actor, Character> allBuffableTargets() {
        return EntitySelector.merge(Arrays.asList(friendlyBuffableTargets(), enemyBuffableTargets()));
    }

    /**
     * Returns a {@link EntitySelector} which returns all living {@link Character},
     * including {@link Hero} and {@link Minion}s.
     */
    public static <Actor extends PlayerProperty> EntitySelector<Actor, Character> allLivingTargets() {
        return EntitySelector.merge(Arrays.asList(friendlyLivingTargets(), enemyLivingTargets()));
    }

    /**
     * Returns a {@link EntitySelector} which returns all enemy {@link Character}s,
     * including {@link Hero} and {@link Minion}s.
     */
    public static <Actor extends PlayerProperty> EntitySelector<Actor, Character> enemyTargets() {
        return EntitySelector.merge(Arrays.asList(enemyMinions(), enemyHero()));
    }

    /**
     * Returns a {@link EntitySelector} which returns all enemy buffable {@link Character}s,
     * including {@link Hero} and {@link Minion}s.
     */
    public static <Actor extends PlayerProperty> EntitySelector<Actor, Character> enemyBuffableTargets() {
        return EntitySelector.merge(Arrays.asList(enemyBuffableMinions(), enemyHero()));
    }

    /**
     * Returns a {@link EntitySelector} which returns all enemy living {@link Character}s,
     * including {@link Hero} and {@link Minion}s.
     */
    public static <Actor extends PlayerProperty> EntitySelector<Actor, Character> enemyLivingTargets() {
        return EntitySelector.merge(Arrays.asList(enemyLivingMinions(), enemyHero()));
    }

    /**
     * Returns a {@link EntitySelector} which returns all friendly {@link Character}s,
     * including {@link Hero} and {@link Minion}s.
     */
    public static <Actor extends PlayerProperty> EntitySelector<Actor, Character> friendlyTargets() {
        return EntitySelector.merge(Arrays.asList(friendlyMinions(), friendlyHero()));
    }

    /**
     * Returns a {@link EntitySelector} which returns all friendly buffable {@link Character}s,
     * including {@link Hero} and {@link Minion}s.
     */
    public static <Actor extends PlayerProperty> EntitySelector<Actor, Character> friendlyBuffableTargets() {
        return EntitySelector.merge(Arrays.asList(friendlyBuffableMinions(), friendlyHero()));
    }

    /**
     * Returns a {@link EntitySelector} which returns all friendly living {@link Character}s,
     * including {@link Hero} and {@link Minion}s.
     */
    public static <Actor extends PlayerProperty> EntitySelector<Actor, info.hearthsim.brazier.Character> friendlyLivingTargets() {
        return EntitySelector.merge(Arrays.asList(friendlyLivingMinions(), friendlyHero()));
    }

    private EntitySelectors() {
        throw new AssertionError();
    }
}
