package info.hearthsim.brazier.game;

import info.hearthsim.brazier.GameAgent;
import info.hearthsim.brazier.actions.PlayTargetRequest;
import org.jtrim.utils.ExceptionHelper;

public final class PlayerAgent {
    private final GameAgent playAgent;
    private final PlayerId playerId;

    public PlayerAgent(GameAgent playAgent, PlayerId playerId) {
        ExceptionHelper.checkNotNullArgument(playAgent, "playAgent");
        ExceptionHelper.checkNotNullArgument(playerId, "playerId");

        this.playAgent = playAgent;
        this.playerId = playerId;
    }

    public Player getPlayer() {
        return playAgent.getGame().getPlayer(playerId);
    }

    public void attack(EntityId attacker, EntityId defender) {
        // TODO: Check the validity of the move.
        playAgent.attack(attacker, defender);
    }

    public void playNonMinionCard(int cardIndex) {
        playCard(cardIndex, -1, null);
    }

    public void playNonMinionCard(int cardIndex, EntityId target) {
        playCard(cardIndex, -1, target);
    }

    public void playMinionCard(int cardIndex, int minionLocation) {
        ExceptionHelper.checkArgumentInRange(minionLocation, 0, Integer.MAX_VALUE, "minionLocation");

        playCard(cardIndex, minionLocation, null);
    }

    public void playMinionCard(int cardIndex, int minionLocation, EntityId target) {
        ExceptionHelper.checkArgumentInRange(minionLocation, 0, Integer.MAX_VALUE, "minionLocation");
        ExceptionHelper.checkNotNullArgument(target, "target");

        playCard(cardIndex, minionLocation, target);
    }

    private void playCard(int cardIndex, int minionLocation, EntityId target) {
        // TODO: Check the validity of the move
        playAgent.playCard(cardIndex, new PlayTargetRequest(playerId, minionLocation, target));
    }
}
