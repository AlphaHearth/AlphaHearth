package info.hearthsim.brazier.ui;

import info.hearthsim.brazier.PlayerId;
import org.jtrim.utils.ExceptionHelper;

public final class UiMinionIndexNeed {
    private final PlayerId playerId;

    public UiMinionIndexNeed(PlayerId playerId) {
        ExceptionHelper.checkNotNullArgument(playerId, "playerId");
        this.playerId = playerId;
    }

    public PlayerId getPlayerId() {
        return playerId;
    }
}
