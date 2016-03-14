package info.hearthsim.brazier.actions;

import info.hearthsim.brazier.Hero;
import info.hearthsim.brazier.PlayerId;
import info.hearthsim.brazier.PlayerPredicate;
import info.hearthsim.brazier.minions.Minion;

import java.util.Objects;

import org.jtrim.utils.ExceptionHelper;

/**
 * The need of target for a specific card. Fields of this class include:
 * <ul>
 *     <li>{@code hasTarget}: {@code boolean} field, representing if the related action needs any target;</li>
 *     <li>
 *         {@code allowHeroCondition}: {@link PlayerPredicate} of {@link Hero}, which can be used to test if the
 *         given {@code Hero} is valid for this {@code TargetNeed}. This field is meaningless if {@code hasTarget}
 *         is set to {@code false};
 *     </li>
 *     <li>
 *         {@code allowMinionCondition}: {@link PlayerPredicate} of {@link Minion}, which can be used to test if the
 *         given {@code Minion} is valid for this {@code TargetNeed}. This field is meaningless if {@code hasTarget}
 *         is set to {@code false}.
 *     </li>
 * </ul>
 *
 * Methods like {@link #mayTargetHero()} and {@link #mayTargetMinion()} can be used to test
 * if certain target is valid for this {@code TargetNeed}.
 * <p>
 * For predefined {@code TargetNeed}s, see {@link TargetNeeds}.
 *
 * @see TargetNeeds
 */
public final class TargetNeed {

    private final boolean hasTarget;
    private final PlayerPredicate<? super Hero> allowHeroCondition;
    private final PlayerPredicate<? super Minion> allowMinionCondition;

    public TargetNeed() {
        this(PlayerPredicate.NONE, PlayerPredicate.NONE, false);
    }

    public TargetNeed(PlayerPredicate<? super Hero> allowHeroCondition,
            PlayerPredicate<? super Minion> allowMinionCondition) {
        this(allowHeroCondition, allowMinionCondition, true);
    }

    public TargetNeed(PlayerPredicate<? super Hero> allowHeroCondition,
                      PlayerPredicate<? super Minion> allowMinionCondition,
                      boolean hasTarget) {
        ExceptionHelper.checkNotNullArgument(allowHeroCondition, "allowHeroCondition");
        ExceptionHelper.checkNotNullArgument(allowMinionCondition, "allowMinionCondition");

        this.allowHeroCondition = allowHeroCondition;
        this.allowMinionCondition = allowMinionCondition;
        this.hasTarget = hasTarget;
    }

    public TargetNeed combine(TargetNeed other) {
        if (!hasTarget) {
            return other;
        }
        if (!other.hasTarget) {
            return this;
        }

        PlayerPredicate<? super Hero> newHeroCond = combine(allowHeroCondition, other.allowHeroCondition);
        PlayerPredicate<? super Minion> newMinionCond = combine(allowMinionCondition, other.allowMinionCondition);

        return new TargetNeed(newHeroCond, newMinionCond);
    }

    public boolean hasTarget() {
        return hasTarget;
    }

    public boolean mayTargetHero() {
        // TODO such equivalence checking may be unsafe.
        return hasTarget && allowHeroCondition != PlayerPredicate.NONE;
    }

    public boolean mayTargetMinion() {
        // TODO such equivalence checking may be unsafe.
        return hasTarget && allowMinionCondition != PlayerPredicate.NONE;
    }

    public boolean canTargetHero(PlayerId playerId, Hero hero) {
        return allowHeroCondition.test(playerId, hero);
    }

    public PlayerPredicate<? super Hero> getAllowHeroCondition() {
        return allowHeroCondition;
    }

    public PlayerPredicate<? super Minion> getAllowMinionCondition() {
        return allowMinionCondition;
    }

    private static <T> PlayerPredicate<? super T> combine(
        PlayerPredicate<? super T> pred1,
        PlayerPredicate<? super T> pred2) {
        if (pred1 == PlayerPredicate.NONE || pred2 == PlayerPredicate.NONE) {
            return PlayerPredicate.NONE;
        }

        return (playerId, arg) -> {
            return pred1.test(playerId, arg) && pred2.test(playerId, arg);
        };
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (this.hasTarget ? 1 : 0);
        hash = 31 * hash + Objects.hashCode(this.allowHeroCondition);
        hash = 31 * hash + Objects.hashCode(this.allowMinionCondition);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;

        final TargetNeed other = (TargetNeed)obj;
        return this.hasTarget == other.hasTarget
                && Objects.equals(this.allowHeroCondition, other.allowHeroCondition)
                && Objects.equals(this.allowMinionCondition, other.allowMinionCondition);
    }

    @Override
    public String toString() {
        return "TargetNeed{hasTarget=" + hasTarget + '}';
    }

}
