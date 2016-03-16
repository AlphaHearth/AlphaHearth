package info.hearthsim.brazier.abilities;

import info.hearthsim.brazier.Game;

import java.util.List;

/**
 * Target provider for an aura, which can be used to list out all possible targets in the given {@code Game}
 * for a certain aura. This interface is a functional interface.
 * <p>
 * For predefined {@code AuraTargetProvider}s, see {@link AuraTargetProviders}.
 *
 * @see AuraTargetProviders
 */
public interface AuraTargetProvider<Source, Target> {
    /**
     * Lists out all possible targets in the given {@code Game} for a certain aura from the given source.
     *
     * @param game the given {@code Game}.
     * @param source the given source.
     * @return all possible targets for the aura.
     */
    public List<Target> getPossibleTargets(Game game, Source source);
}
