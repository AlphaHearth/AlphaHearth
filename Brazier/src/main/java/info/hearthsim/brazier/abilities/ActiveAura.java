package info.hearthsim.brazier.abilities;

import info.hearthsim.brazier.game.Entity;
import info.hearthsim.brazier.game.EntityId;
import info.hearthsim.brazier.game.Game;
import info.hearthsim.brazier.game.Player;
import info.hearthsim.brazier.util.UndoAction;
import org.jtrim.utils.ExceptionHelper;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * An active aura with source and target. Compare to {@link Aura} which can only be used to describe the effect of
 * a certain aura, {@code ActiveAura} can represent a complete in-game aura, with aura source,
 * {@link AuraTargetProvider}, {@link AuraFilter} and {@link Aura} included as its fields.
 */
public final class ActiveAura <Source extends Entity, Target extends Entity> implements Entity {
    private final EntityId entityId;
    private final Source source;
    private final AuraTargetProvider<? super Source, ? extends Target> targetProvider;
    private final AuraFilter<? super Source, ? super Target> targetFilter;
    private final Aura<? super Source, ? super Target> aura;

    private Map<EntityId, UndoAction<Game>> currentlyApplied;

    /**
     * Creates a {@code ActiveAura} with the designated source of the aura and
     * the given {@code AuraTargetProvider}, {@code AuraFilter} and {@code Aura} representing
     * the implementation of the aura effect.
     *
     * @param source         the source of the aura.
     * @param targetProvider the target provider of the aura, which will be used to list out all
     *                       applicable targets for the aura in the given {@code Game}.
     * @param targetFilter   the given filter on the possible targets.
     * @param aura           the {@code Aura} representing the actual effect of this {@code ActiveAura}.
     */
    public ActiveAura(
        Source source,
        AuraTargetProvider<? super Source, ? extends Target> targetProvider,
        AuraFilter<? super Source, ? super Target> targetFilter,
        Aura<? super Source, ? super Target> aura) {
        ExceptionHelper.checkNotNullArgument(source, "source");
        ExceptionHelper.checkNotNullArgument(targetProvider, "targetProvider");
        ExceptionHelper.checkNotNullArgument(targetFilter, "targetFilter");
        ExceptionHelper.checkNotNullArgument(aura, "aura");

        this.entityId = new EntityId();
        this.source = source;
        this.targetProvider = targetProvider;
        this.targetFilter = targetFilter;
        this.aura = aura;
        this.currentlyApplied = new IdentityHashMap<>(2 * Player.MAX_BOARD_SIZE);
    }

    private ActiveAura(ActiveAura<Source, Target> other) {
        this.entityId = other.entityId;
        this.source = other.source;
        this.targetProvider = other.targetProvider;
        this.targetFilter = other.targetFilter;
        this.aura = other.aura;
        this.currentlyApplied = new IdentityHashMap<>(2 * Player.MAX_BOARD_SIZE);
    }

    @Override
    public EntityId getEntityId() {
        return entityId;
    }

    public ActiveAura<Source, Target> copyFor(Game newGame, Player newOwner) {
        return new ActiveAura<>(this);
    }

    /**
     * Applies this {@code ActiveAura} to the given {@code Game}.
     * <p>
     * The target provider will be used first to list out all possible targets, and then the target filter
     * to rule out all inapplicable targets. The remaining applicable targets will be updated by using the
     * {@code Aura}.
     */
    public void applyAura(Game game) {
        ExceptionHelper.checkNotNullArgument(game, "game");

        List<? extends Target> targets = targetProvider.getPossibleTargets(game, source);

        Map<EntityId, UndoAction<Game>> newCurrentlyApplied = new IdentityHashMap<>();

        boolean didAnything = false;
        Map<EntityId, UndoAction<Game>> currentlyAppliedCopy = new IdentityHashMap<>(currentlyApplied);
        for (Target target : targets) {
            UndoAction<Game> ref = currentlyAppliedCopy.remove(target.getEntityId());
            boolean needAura = targetFilter.isApplicable(game, source, target);

            if (ref == null) {
                if (needAura) {
                    UndoAction<? super Target> undoRef = aura.applyAura(source, target);
                    Objects.requireNonNull(undoRef, "Aura.applyAura");

                    newCurrentlyApplied.put(target.getEntityId(),
                        (g) -> undoRef.undo((Target) g.findEntity(target.getEntityId())));
                }
                didAnything = true;
            } else {
                if (needAura) {
                    newCurrentlyApplied.put(target.getEntityId(), ref);
                } else {
                    ref.undo(game);
                    didAnything = true;
                }
            }
        }

        for (UndoAction<Game> ref : currentlyAppliedCopy.values()) {
            didAnything = true;
            ref.undo(game);
        }

        if (didAnything) {
            currentlyApplied = newCurrentlyApplied;
        }
    }

    /**
     * Deactivates the aura from the given {@code Game}.
     */
    public void deactivate(Game game) {
        if (currentlyApplied.isEmpty())
            return;

        for (UndoAction<Game> ref : currentlyApplied.values())
            ref.undo(game);
        currentlyApplied.clear();
    }

    @Override
    public Player getOwner() {
        return source.getOwner();
    }
}
