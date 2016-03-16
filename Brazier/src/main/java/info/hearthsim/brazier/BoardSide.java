package info.hearthsim.brazier;

import info.hearthsim.brazier.events.GameEvents;
import info.hearthsim.brazier.minions.Minion;
import info.hearthsim.brazier.actions.PlayArg;
import info.hearthsim.brazier.actions.undo.UndoAction;
import info.hearthsim.brazier.actions.undo.UndoableAction;
import info.hearthsim.brazier.events.CompletableGameActionEvents;
import info.hearthsim.brazier.minions.MinionBody;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import info.hearthsim.brazier.actions.undo.UndoableResult;
import org.jtrim.utils.ExceptionHelper;

public final class BoardSide implements PlayerProperty {
    private final Player owner;

    private final int maxSize;
    private final List<BoardMinionRef> minionRefs;

    public BoardSide(Player owner, int maxSize) {
        ExceptionHelper.checkNotNullArgument(owner, "owner");
        ExceptionHelper.checkArgumentInRange(maxSize, 0, Integer.MAX_VALUE, "maxSize");

        this.owner = owner;
        this.maxSize = maxSize;

        minionRefs = new ArrayList<>(maxSize);
    }

    /**
     * Returns a copy of this {@code BoardSide} of the given new owner.
     */
    public BoardSide copyFor(Player newOwner) {
        BoardSide result = new BoardSide(newOwner, this.maxSize);
        for (BoardMinionRef minionRef : minionRefs) {
            BoardMinionRef newMinionRef =
                new BoardMinionRef(minionRef.minion.copyFor(newOwner), minionRef.needsSpace);
            result.minionRefs.add(newMinionRef);
        }
        return result;
    }

    /**
     * Tries to add the given minion to the given index of the board.
     * If the given index is less than {@code 0}, the given minion will be added to the left most of the board;
     * if the given index is larger than or equal to the number of minions on this board,
     * the given minion will be added to the right most of the board.
     * <p>
     * For triggering minion-summoning event and battle-cry effect, see {@link #completeSummon(Minion)}.
     *
     * @param minion the given minion.
     * @param index the given index of the board.
     *
     * @see #completeSummon(Minion)
     */
    public UndoAction tryAddToBoard(Minion minion, int index) {
        if (index > minionRefs.size())
            return tryAddToBoard(minion);
        if (index < 0)
            index = 0;
        if (isFull())
            return UndoAction.DO_NOTHING;
        ExceptionHelper.checkNotNullArgument(minion, "minion");

        BoardMinionRef minionRef = new BoardMinionRef(minion);
        minionRefs.add(index, minionRef);
        minion.activatePassiveAbilities();

        return () -> minionRefs.remove(minionRef);
    }

    /**
     * Tries to add the given minion to the right most of the board. If the board is full, nothing will
     * happened.
     * <p>
     * For triggering minion-summoning event and battle-cry effect, see {@link #completeSummon(Minion)}.
     *
     * @param minion the given minion.
     * @see #completeSummon(Minion)
     */
    public UndoAction tryAddToBoard(Minion minion) {
        if (isFull())
            return UndoAction.DO_NOTHING;
        ExceptionHelper.checkNotNullArgument(minion, "minion");

        BoardMinionRef minionRef = new BoardMinionRef(minion);
        minionRefs.add(minionRef);
        minion.activatePassiveAbilities();

        return () -> minionRefs.remove(minionRef);
    }

    public UndoAction removeFromBoard(TargetId minionId) {
        int index = -1;
        for (int i = 0; i < minionRefs.size(); i++)
            if (minionRefs.get(i).minion.getTargetId() == minionId)
                index = i;
        BoardMinionRef removedMinion = minionRefs.remove(index);

        final int finalIndex = index;
        return () -> minionRefs.add(finalIndex, removedMinion);
    }

    /**
     * Schedules to destroy the minion with the given {@link TargetId}. Method {@link Game#resolveDeaths()}
     * guarantees the minion scheduled to destroy will be destroyed soon after.
     */
    public void scheduleToDestroy(TargetId minionId) {
        for (BoardMinionRef minionRef : minionRefs) {
            if (minionRef.minion.getTargetId() == minionId) {
                minionRef.needsSpace = false;
                return;
            }
        }
    }

    /**
     * Replaces the given old minion on the board with the given new minion.
     *
     * @throws IllegalArgumentException if the given old minion is not on this {@code BoardSide}.
     */
    // TODO Use `MinionDescr` as the type of new minion
    public UndoableResult<Minion> replace(Minion oldMinion, Minion newMinion) {
        int index = indexOf(oldMinion.getTargetId());
        if (index == -1)
            throw new IllegalArgumentException("The given old minion `" + oldMinion
                + "` does not belong to this side of board.");
        BoardMinionRef oldMinionRef = minionRefs.get(index);
        minionRefs.set(index, new BoardMinionRef(newMinion));
        return new UndoableResult<>(oldMinionRef.minion, () -> minionRefs.set(index, oldMinionRef));
    }

    /**
     * Refreshes the states of the minions on the board. That is,
     * sleeping minions will be awakened and their number of attacks will reset.
     * This method should be invoked every time a new turn starts.
     *
     * @return returns an action which undoes everything this method did, assuming
     *   the undo action is called in the same state as was before calling this
     *   {@code refreshStartOfTurn method}. This method never returns {@code null}.
     */
    public UndoAction refreshStartOfTurn() {
        UndoAction.Builder builder = new UndoAction.Builder(minionRefs.size());

        for (BoardMinionRef minionRef: minionRefs) {
            builder.addUndo(minionRef.minion.refreshStartOfTurn());
        }

        return builder;
    }

    /**
     * Refreshes the states of the minions on the board at the end of the turn. That is,
     * frozen minion will be unfrozen.
     * This method should be invoked every time a turn ends.
     */
    public UndoAction refreshEndOfTurn() {
        UndoAction.Builder builder = new UndoAction.Builder(minionRefs.size());

        for (BoardMinionRef minionRef: minionRefs) {
            builder.addUndo(minionRef.minion.refreshEndOfTurn());
        }

        return builder;
    }

    /**
     * Applies the aura effects if they are not already applied. This is
     * only needed for health auras to atomically update all health affecting
     * auras.
     * <P>
     * This method is idempotent.
     *
     * @return an action which undoes everything this method did, assuming
     *   the undo action is called in the same state as was before calling this
     *   {@code refresh method}. This method never returns {@code null}.
     */
    public UndoAction updateAuras() {
        UndoAction.Builder builder = new UndoAction.Builder(minionRefs.size());

        for (BoardMinionRef minionRef: minionRefs) {
            builder.addUndo(minionRef.minion.updateAuras());
        }

        return builder;
    }

    /**
     * Returns the {@link Minion} on the given index of this {@code BoardSide};
     * returns {@code null} if there is no {@code Minion} on the given location.
     */
    public Minion getMinion(int index) {
        if (index >= minionRefs.size() || index < 0)
            return null;
        return minionRefs.get(index).minion;
    }

    @Override
    public Player getOwner() {
        return owner;
    }

    private int getReservationCount() {
        int result = 0;
        for (BoardMinionRef minionRef: minionRefs) {
            if (minionRef.needsSpace) {
                result++;
            }
        }
        return result;
    }

    /** Returns if the {@code BoardSide} is full */
    public boolean isFull() {
        return getReservationCount() >= maxSize;
    }

    /** Returns the maximum number of minions for this {@code BoardSide}. */
    public int getMaxSize() {
        return maxSize;
    }

    /** Returns if there is any non-stealth taunt minion on the board. */
    public boolean hasNonStealthTaunt() {
        return findMinion((minion) -> {
            MinionBody body = minion.getBody();
            return body.isTaunt() && !body.isStealth();
        }) != null;
    }

    /** Finds the first minion with the given {@code TargetId}. */
    public Minion findMinion(TargetId targetId) {
        ExceptionHelper.checkNotNullArgument(targetId, "targetId");

        return findMinion((minion) -> targetId.equals(minion.getTargetId()));
    }

    /**
     * Returns the index of the minion with the given {@link TargetId};
     * returns {@code -1} if no such minion exist.
     */
    public int indexOf(TargetId targetId) {
        ExceptionHelper.checkNotNullArgument(targetId, "targetId");
        return indexOf((minion) -> minion.getTargetId() == targetId);
    }

    /**
     * Returns the index of the given minion;
     * returns {@code -1} if the given minion does not belong to this {@code BoardSide}.
     */
    public int indexOf(Minion minion) {
        ExceptionHelper.checkNotNullArgument(minion, "minion");
        return indexOf(minion::equals);
    }

    /** Finds the first minion which satisfies the given predicate. */
    public Minion findMinion(Predicate<? super Minion> filter) {
        ExceptionHelper.checkNotNullArgument(filter, "filter");

        for (BoardMinionRef minionRef: minionRefs) {
            Minion minion = minionRef.minion;
            if (minion != null && filter.test(minion)) {
                return minion;
            }
        }

        return null;
    }

    /**
     * Returns the index of the first minion which satisfies the given predicate;
     * returns {@code -1} if no such minion exist.
     */
    public int indexOf(Predicate<? super Minion> filter) {
        ExceptionHelper.checkNotNullArgument(filter, "filter");

        for (int i = 0; i < minionRefs.size(); i++)
            if (filter.test(minionRefs.get(i).minion))
                return i;

        return -1;
    }

    /**
     * Appends all living minions which satisfy the given predicate to the end of the given {@code List}.
     */
    public void collectAliveMinions(List<? super Minion> result, Predicate<? super Minion> filter) {
        ExceptionHelper.checkNotNullArgument(filter, "filter");

        collectMinions(result, (minion) -> filter.test(minion) && isAlive(minion));
    }

    /** Returns if the given minion is alive. */
    private static boolean isAlive(Minion minion) {
        return !minion.isDead() && !minion.isScheduledToDestroy();
    }

    /**
     * Collects all minions to the end of the given {@code List}.
     */
    public void collectMinions(List<? super Minion> result) {
        collectMinions(result, (minion) -> true);
    }

    /**
     * Collects all minions which satisfy the given predicate to the end of the given {@code List}.
     */
    public void collectMinions(List<? super Minion> result, Predicate<? super Minion> filter) {
        ExceptionHelper.checkNotNullArgument(filter, "filter");

        for (BoardMinionRef minionRef: minionRefs) {
            Minion minion = minionRef.minion;
            if (minion != null && filter.test(minion)) {
                result.add(minion);
            }
        }
    }

    /**
     * Executes the given function to all minions on the board.
     */
    public UndoAction forAllMinions(Function<? super Minion, ? extends UndoAction> action) {
        ExceptionHelper.checkNotNullArgument(action, "action");

        int reservationCount = minionRefs.size();
        if (reservationCount == 0) {
            return UndoAction.DO_NOTHING;
        }

        UndoAction.Builder result = new UndoAction.Builder(reservationCount);
        boolean applied = false;
        for (BoardMinionRef minionRef: minionRefs) {
            Minion minion = minionRef.minion;
            if (minion != null) {
                applied = true;
                result.addUndo(action.apply(minion));
            }
        }
        return applied ? result : UndoAction.DO_NOTHING;
    }

    /**
     * Returns the number of minions which satisfy the given predicate.
     */
    public int countMinions(Predicate<? super Minion> filter) {
        ExceptionHelper.checkNotNullArgument(filter, "filter");

        int result = 0;
        for (BoardMinionRef minionRef: minionRefs) {
            Minion minion = minionRef.minion;
            if (minion != null && filter.test(minion)) {
                result++;
            }
        }
        return result;
    }

    public int getMinionCount() {
        return getReservationCount();
    }

    /**
     * Returns a {@code List} of all living minions.
     */
    public List<Minion> getAliveMinions() {
        return getMinions(BoardSide::isAlive);
    }

    public List<Minion> getAllMinions() {
        List<Minion> result = new ArrayList<>(minionRefs.size());
        collectMinions(result);
        return result;
    }

    /**
     * Returns a {@code List} of minions which satisfy the given predicate.
     */
    public List<Minion> getMinions(Predicate<? super Minion> filter) {
        List<Minion> result = new ArrayList<>(minionRefs.size());
        collectMinions(result, filter);
        return result;
    }

    @Override
    public Game getGame() {
        return getOwner().getGame();
    }

    /**
     * Controls the given minion by dragging it to this side of board.
     *
     * @param minion the given minion.
     */
    public UndoAction takeOwnership(Minion minion) {
        ExceptionHelper.checkNotNullArgument(minion, "minion");

        if (minion.isDestroyed()) {
            return UndoAction.DO_NOTHING;
        }

        // We must not check if it is already on our board because
        // in this case, HearthStone still tries to put the minion back.
        // Which might result in the death of the minion due to full board.

        if (isFull()) {
            return minion.kill();
        }

        UndoAction.Builder builder = new UndoAction.Builder();

        BoardSide enemyBoard = minion.getOwner().getBoard();
        builder.addUndo(enemyBoard.removeFromBoard(minion.getTargetId()));

        Player prevOwner = minion.getOwner();
        minion.setOwner(getOwner());
        builder.addUndo(() -> minion.setOwner(prevOwner));

        UndoAction reserveUndo = tryAddToBoard(minion);

        builder.addUndo(reserveUndo);

        builder.addUndo(minion.refreshStartOfTurn());
        builder.addUndo(minion.exhaust());

        builder.addUndo(completeSummon(minion));

        return builder;
    }

    /**
     * Completes the action of summoning the given {@code Minion} without triggering any battlecry effect.
     * @param minion the given {@code Minion} which is just summoned.
     */
    public UndoAction completeSummon(Minion minion) {
        return completeSummonUnsafe(minion, null);
    }

    /**
     * Completes the action of summoning the given {@code Minion} with an {@code Optional} of
     * {@link Character}, designating the potential target of battle-cry effect.
     *
     * @param minion the given {@code Minion} which is just summoned.
     * @param battleCryTarget {@code Optional} of {@link Character},
     *                        designating the potential target of battle-cry effect.
     *
     * @throws NullPointerException if {@code battleCryTarget} is {@code null}.
     */
    public UndoAction completeSummon(
        Minion minion,
        Optional<Character> battleCryTarget) {
        ExceptionHelper.checkNotNullArgument(battleCryTarget, "battleCryTarget");
        return completeSummonUnsafe(minion, battleCryTarget);
    }

    /**
     * Completes the action of summoning the given {@code Minion} with an {@code Optional} of
     * {@link Character}, designating the potential target of battlecry effect.
     *
     * @param minion the given {@code Minion} which is just summoned.
     * @param battleCryTarget {@code Optional} of {@link Character},
     *                        designating the potential target of battlecry effect.
     *                        Pass {@code null} to indicate there is no battlecry effect to be
     *                        triggered.
     */
    private UndoAction completeSummonUnsafe(
        Minion minion,
        Optional<Character> battleCryTarget) {
        ExceptionHelper.checkNotNullArgument(minion, "minion");

        Game game = getGame();
        GameEvents events = game.getEvents();

        UndoAction.Builder result = new UndoAction.Builder();

        CompletableGameActionEvents<Minion> summoningListeners = events.summoningListeners();
        UndoableResult<UndoableAction> summoningFinalizer = summoningListeners.triggerEvent(minion);
        result.addUndo(summoningFinalizer.getUndoAction());

        if (battleCryTarget != null) {
            PlayArg<Minion> battleCryArg = new PlayArg<>(minion, battleCryTarget);
            result.addUndo(minion.getBaseDescr().executeBattleCriesNow(owner, battleCryArg));
        }

        result.addUndo(summoningFinalizer.getResult().doAction());

        return result;
    }

    /**
     * Wrapper for a minion on the board, with additional boolean field {@code needsSpace}, which
     * indicates if the minion takes up one space of the board.
     */
    private static class BoardMinionRef {
        private Minion minion;
        private boolean needsSpace;

        /**
         * Creates a new {@code BoardMinionRef} with the given {@code Minion} with will take up one
         * space of the board.
         */
        BoardMinionRef(Minion minion) {
            this(minion, true);
        }

        /**
         * Creates a new {@code BoardMinionRef} with the given {@code Minion} and the given boolean
         * field {@code needsSpace}, which indicates if the {@code Minion} will take up one space
         * of the board.
         */
        BoardMinionRef(Minion minion, boolean needsSpace) {
            ExceptionHelper.checkNotNullArgument(minion, "minion");

            this.minion = minion;
            this.needsSpace = needsSpace;
        }
    }
}
