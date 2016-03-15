package info.hearthsim.brazier.actions;

import info.hearthsim.brazier.*;
import info.hearthsim.brazier.Character;
import info.hearthsim.brazier.minions.Minion;
import info.hearthsim.brazier.minions.MinionId;
import info.hearthsim.brazier.parsing.NamedArg;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jtrim.utils.ExceptionHelper;

/**
 * Predefined {@link EntityFilter}s.
 */
public final class EntityFilters {

    /**
     * Returns an {@link EntityFilter} which returns nothing.
     */
    public static <Entity> EntityFilter<Entity> empty() {
        return (world, entities) -> Stream.empty();
    }

    /**
     * Returns an {@link EntityFilter} which selects a random entity from the given {@code Stream}.
     */
    public static <Entity> EntityFilter<Entity> random() {
        return (World world, Stream<? extends Entity> entities) -> {
            List<Entity> elements = entities.collect(Collectors.<Entity>toList());
            Entity result = ActionUtils.pickRandom(world, elements);
            if (result == null) {
                return Stream.empty();
            }
            else {
                return Stream.of(result);
            }
        };
    }

    /**
     * Returns an {@link EntityFilter} which selects given number of random entities from the given {@code Stream}.
     */
    public static <Entity> EntityFilter<Entity> random(@NamedArg("count") int count) {
        ExceptionHelper.checkArgumentInRange(count, 0, Integer.MAX_VALUE, "count");
        if (count == 0) {
            return empty();
        }
        if (count == 1) {
            return random();
        }

        return (World world, Stream<? extends Entity> entities) -> {
            List<Entity> elements = entities.collect(Collectors.<Entity>toList());
            return ActionUtils.pickMultipleRandom(world, count, elements).stream();
        };
    }

    /**
     * Returns a {@link Predicate} of {@link LabeledEntity} which checks if the given entity has
     * every given {@link Keyword}.
     */
    public static <Entity extends LabeledEntity> Predicate<Entity> withKeywords(
            @NamedArg("keywords") Keyword... keywords) {
        return ActionUtils.includedKeywordsFilter(keywords);
    }

    /**
     * Returns a {@link Predicate} of {@link LabeledEntity} which checks if the given entity does not have
     * any given {@link Keyword}.
     */
    public static <Entity extends LabeledEntity> Predicate<Entity> withoutKeywords(
            @NamedArg("keywords") Keyword... keywords) {
        return ActionUtils.excludedKeywordsFilter(keywords);
    }

    /**
     * Returns a {@link Predicate} of {@link Character} which checks if the given character has
     * {@link Keywords#RACE_BEAST}.
     */
    public static <Entity extends Character> Predicate<Entity> isBeast() {
        return withKeywords(Keywords.RACE_BEAST);
    }

    /**
     * Returns a {@link Predicate} of {@link Character} which checks if the given character has
     * {@link Keywords#RACE_DEMON}.
     */
    public static <Entity extends Character> Predicate<Entity> isDemon() {
        return withKeywords(Keywords.RACE_DEMON);
    }

    /**
     * Returns a {@link Predicate} of {@link Character} which checks if the given character has
     * {@link Keywords#RACE_DRAGON}.
     */
    public static <Entity extends Character> Predicate<Entity> isDragon() {
        return withKeywords(Keywords.RACE_DRAGON);
    }

    /**
     * Returns a {@link Predicate} of {@link Character} which checks if the given character has
     * {@link Keywords#RACE_MECH}.
     */
    public static <Entity extends Character> Predicate<Entity> isMech() {
        return withKeywords(Keywords.RACE_MECH);
    }

    /**
     * Returns a {@link Predicate} of {@link Character} which checks if the given character has
     * {@link Keywords#RACE_MURLOC}.
     */
    public static <Entity extends info.hearthsim.brazier.Character> Predicate<Entity> isMurloc() {
        return withKeywords(Keywords.RACE_MURLOC);
    }

    /**
     * Returns a {@link Predicate} of {@link Character} which checks if the given character has
     * {@link Keywords#RACE_PIRATE}.
     */
    public static <Entity extends Character> Predicate<Entity> isPirate() {
        return withKeywords(Keywords.RACE_PIRATE);
    }

    /**
     * Returns a {@link Predicate} of {@link Character} which checks if the given character has
     * {@link Keywords#RACE_TOTEM}.
     */
    public static <Entity extends Character> Predicate<Entity> isTotem() {
        return withKeywords(Keywords.RACE_TOTEM);
    }

    /**
     * Returns a {@link Predicate} of {@link Character} which checks if the given character is dead.
     */
    public static <Entity extends Character> Predicate<Entity> isDead() {
        return Entity::isDead;
    }

    /**
     * Returns a {@link Predicate} of {@link Character} which checks if the given character is alive.
     */
    public static <Entity extends Character> Predicate<Entity> isAlive() {
        return (target) -> !target.isDead();
    }

    /**
     * Returns a {@link Predicate} of {@link Character} which checks if the given character is damaged.
     */
    public static <Entity extends Character> Predicate<Entity> isDamaged() {
        return Entity::isDamaged;
    }

    /**
     * Returns a {@link Predicate} of {@link Character} which checks if the given character is not damaged.
     */
    public static <Entity extends Character> Predicate<Entity> isUndamaged() {
        return (target) -> !target.isDamaged();
    }

    /**
     * Returns a {@link Predicate} of {@link Character} which checks if the given character is frozen.
     */
    public static <Entity extends Character> Predicate<Entity> isFrozen() {
        return (target) -> target.getAttackTool().isFrozen();
    }

    /**
     * Returns a {@link Predicate} of {@link Minion} which checks if the given minion has death
     * rattle effect.
     */
    public static <Entity extends Minion> Predicate<Entity> isDeathRattle() {
        return (target) -> target.getProperties().isDeathRattle();
    }

    /**
     * Returns a {@link Predicate} of {@link Minion} which checks if the given minion is buffable.
     */
    public static Predicate<Minion> buffableMinion() {
        return (minion) -> !minion.isScheduledToDestroy();
    }

    /**
     * Returns a {@link Predicate} of {@link Minion} which checks if the given minion has the given name.
     */
    public static <Entity extends Minion> Predicate<Entity> minionNameIs(@NamedArg("name") MinionId name) {
        ExceptionHelper.checkNotNullArgument(name, "name");
        return (target) -> name.equals(target.getBaseDescr().getId());
    }

    /**
     * Returns a negation of the given {@link Predicate}.
     */
    public static <Entity> Predicate<Entity> not(@NamedArg("filter") Predicate<? super Entity> filter) {
        ExceptionHelper.checkNotNullArgument(filter, "filter");
        return (arg) -> !filter.test(arg);
    }

    /**
     * Returns a {@link EntityFilter} from the given {@link Predicate}.
     */
    public static <Entity> EntityFilter<Entity> fromPredicate(Predicate<? super Entity> filter) {
        ExceptionHelper.checkNotNullArgument(filter, "filter");

        return (World world, Stream<? extends Entity> entities) -> {
            return entities.filter(filter);
        };
    }

    /**
     * Returns a {@link Predicate} of {@link Character} which checks if the character's attack is less
     * than the given amount.
     */
    public static <Entity extends Character> Predicate<Entity> attackIsLess(@NamedArg("attack") int attack) {
        return (target) -> target.getAttackTool().getAttack() < attack;
    }

    /**
     * Returns a {@link Predicate} of {@link PlayerProperty} which checks if the property's owner has the
     * maximum mana crystals he can have.
     */
    public static <Entity extends PlayerProperty> Predicate<Entity> isMaxManaCrystals() {
        return (target) -> target.getOwner().getManaResource().getManaCrystals() >= Player.MAX_MANA;
    }

    /**
     * Returns a {@link Predicate} of {@link PlayerProperty} which checks if the property's owner's hands are empty.
     */
    public static <Entity extends PlayerProperty> Predicate<Entity> isEmptyHand() {
        return (target) -> target.getOwner().getHand().getCardCount() == 0;
    }

    private EntityFilters() {
        throw new AssertionError();
    }
}
