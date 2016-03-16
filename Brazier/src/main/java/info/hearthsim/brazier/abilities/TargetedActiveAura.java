package info.hearthsim.brazier.abilities;

import info.hearthsim.brazier.actions.undo.UndoableUnregisterAction;
import info.hearthsim.brazier.Player;
import info.hearthsim.brazier.Game;
import info.hearthsim.brazier.actions.undo.UndoAction;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.jtrim.utils.ExceptionHelper;

/**
 * An active aura with source and target. Compare to {@link Aura} which can only be used to describe the effect of
 * a certain aura, {@code TargetedActiveAura} can represent a complete in-game aura, with aura source,
 * {@link AuraTargetProvider}, {@link AuraFilter} and {@link Aura} included as its fields.
 */
public final class TargetedActiveAura<Source, Target> implements ActiveAura {
    private final Source source;
    private final AuraTargetProvider<? super Source, ? extends Target> targetProvider;
    private final AuraFilter<? super Source, ? super Target> targetFilter;
    private final Aura<? super Source, ? super Target> aura;

    private Map<Target, UndoableUnregisterAction> currentlyApplied;

    /**
     * Creates a {@code TargetedActiveAura} with the designated source of the aura and
     * the given {@code AuraTargetProvider}, {@code AuraFilter} and {@code Aura} representing
     * the implementation of the aura effect.
     *
     * @param source the source of the aura.
     * @param targetProvider the target provider of the aura, which will be used to list out all
     *                       applicable targets for the aura in the given {@code Game}.
     * @param targetFilter the given filter on the possible targets.
     * @param aura the {@code Aura} representing the actual effect of this {@code TargetedActiveAura}.
     */
    public TargetedActiveAura(
            Source source,
            AuraTargetProvider<? super Source, ? extends Target> targetProvider,
            AuraFilter<? super Source, ? super Target> targetFilter,
            Aura<? super Source, ? super Target> aura) {
        ExceptionHelper.checkNotNullArgument(source, "source");
        ExceptionHelper.checkNotNullArgument(targetProvider, "targetProvider");
        ExceptionHelper.checkNotNullArgument(targetFilter, "targetFilter");
        ExceptionHelper.checkNotNullArgument(aura, "aura");

        this.source = source;
        this.targetProvider = targetProvider;
        this.targetFilter = targetFilter;
        this.aura = aura;
        this.currentlyApplied = new IdentityHashMap<>(2 * Player.MAX_BOARD_SIZE);
    }

    /**
     * {@inheritDoc}
     * <p>
     * The target provider will be used first to list out all possible targets, and then the target filter
     * to rule out all inapplicable targets. The remaining applicable targets will be updated by using the
     * {@code Aura}.
     *
     * @param game the given {@code Game}.
     */
    @Override
    public UndoAction updateAura(Game game) {
        ExceptionHelper.checkNotNullArgument(game, "game");

        List<? extends Target> targets = targetProvider.getPossibleTargets(game, source);

        Map<Target, UndoableUnregisterAction> newCurrentlyApplied = new IdentityHashMap<>();

        UndoAction.Builder builder = new UndoAction.Builder();

        boolean didAnything = false;
        Map<Target, UndoableUnregisterAction> currentlyAppliedCopy = new IdentityHashMap<>(currentlyApplied);
        for (Target target: targets) {
            UndoableUnregisterAction ref = currentlyAppliedCopy.remove(target);
            boolean needAura = targetFilter.isApplicable(game, source, target);

            if (ref == null) {
                if (needAura) {
                    UndoableUnregisterAction newRef = aura.applyAura(game, source, target);
                    Objects.requireNonNull(newRef, "Aura.applyAura");

                    builder.addUndo(newRef);

                    newCurrentlyApplied.put(target, newRef);
                }
                didAnything = true;
            }
            else {
                if (needAura) {
                    newCurrentlyApplied.put(target, ref);
                }
                else {
                    builder.addUndo(ref.unregister());
                    didAnything = true;
                }
            }
        }

        for (UndoableUnregisterAction ref: currentlyAppliedCopy.values()) {
            didAnything = true;
            builder.addUndo(ref.unregister());
        }

        if (didAnything) {
            Map<Target, UndoableUnregisterAction> prevCurrentlyApplied = currentlyApplied;
            currentlyApplied = newCurrentlyApplied;
            builder.addUndo(() -> currentlyApplied = prevCurrentlyApplied);
        }

        return builder;
    }

    @Override
    public UndoAction deactivate() {
        if (currentlyApplied.isEmpty()) {
            return UndoAction.DO_NOTHING;
        }

        UndoAction.Builder builder = new UndoAction.Builder();

        Map<Target, UndoableUnregisterAction> prevCurrentlyApplied = currentlyApplied;
        currentlyApplied = new IdentityHashMap<>();
        builder.addUndo(() -> currentlyApplied = prevCurrentlyApplied);

        for (UndoableUnregisterAction ref: prevCurrentlyApplied.values()) {
            builder.addUndo(ref.unregister());
        }

        return builder;
    }
}
