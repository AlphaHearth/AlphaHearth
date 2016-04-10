package info.hearthsim.brazier.game;

import info.hearthsim.brazier.game.minions.Minion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.jtrim.utils.ExceptionHelper;

public final class Graveyard {
    private final List<Minion> deadMinions;
    private final List<Minion> deadMinionsView;

    private List<Minion> minionsDiedThisTurn;

    public Graveyard() {
        this.deadMinions = new ArrayList<>();
        this.deadMinionsView = Collections.unmodifiableList(deadMinions);
        this.minionsDiedThisTurn = new ArrayList<>();
    }

    /**
     * Returns a copy of this {@code Graveyard} with the given new owner.
     */
    public Graveyard copyFor(Player newOwner) {
        Graveyard result = new Graveyard();
        for (Minion minion : deadMinions)
            result.deadMinions.add(minion.copyFor(newOwner.getGame(), newOwner));
        for (Minion minion : minionsDiedThisTurn) // FIXME: Redundant copies of minions are made here
            result.minionsDiedThisTurn.add(minion.copyFor(newOwner.getGame(), newOwner));
        return result;
    }

    private static <T> boolean containsAll(Set<? extends T> set, T[] elements) {
        for (T element: elements) {
            if (!set.contains(element)) {
                return false;
            }
        }
        return true;
    }

    public Minion findMinion(EntityId id) {
        for (Minion minion : deadMinions)
            if (minion.getEntityId() == id)
                return minion;
        return null;
    }

    /**
     * Refreshes the {@code Graveyard} at end of turn by stop referring to the minions died
     * in this turn.
     */
    public void refreshEndOfTurn() {
        if (minionsDiedThisTurn.isEmpty())
            return;
        minionsDiedThisTurn.clear();
    }

    public int getNumberOfMinionsDiedThisTurn() {
        return minionsDiedThisTurn.size();
    }

    public List<Minion> getMinionsDiedThisTurn() {
        if (minionsDiedThisTurn.isEmpty()) {
            return Collections.emptyList();
        }
        return new ArrayList<>(minionsDiedThisTurn);
    }

    public boolean hasWithKeyword(Keyword[] keywords) {
        ExceptionHelper.checkNotNullElements(keywords, "keywords");

        for (Minion deadMinion: deadMinions) {
            if (containsAll(deadMinion.getKeywords(), keywords)) {
                return true;
            }
        }
        return false;
    }

    public List<Minion> getDeadMinions() {
        return deadMinionsView;
    }

    public void addDeadMinion(Minion minion) {
        ExceptionHelper.checkNotNullArgument(minion, "minion");

        minionsDiedThisTurn.add(minion);
        deadMinions.add(minion);
    }
}
