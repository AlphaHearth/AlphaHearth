package info.hearthsim.brazier.game;

import java.util.List;
import org.jtrim.collections.CollectionsEx;
import org.jtrim.utils.ExceptionHelper;

/**
 * The result of a game, giving out which player(s) died in the game.
 */
public final class GameResult {
    private final List<PlayerId> deadPlayers;

    public GameResult(List<PlayerId> deadPlayers) {
        this.deadPlayers = CollectionsEx.readOnlyCopy(deadPlayers);
        ExceptionHelper.checkNotNullElements(this.deadPlayers, "deadPlayers");
    }

    public boolean hasWon(PlayerId playerId) {
        return !deadPlayers.contains(playerId);
    }

    public List<PlayerId> getDeadPlayers() {
        return deadPlayers;
    }

    @Override
    public String toString() {
        return "GameResult{" + "deadPlayers=" + deadPlayers + '}';
    }
}
