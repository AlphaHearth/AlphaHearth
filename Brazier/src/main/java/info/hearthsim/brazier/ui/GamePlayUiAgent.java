package info.hearthsim.brazier.ui;

import info.hearthsim.brazier.GameAgent;
import info.hearthsim.brazier.actions.GameAction;
import info.hearthsim.brazier.actions.PlayTargetRequest;
import info.hearthsim.brazier.game.EntityId;
import info.hearthsim.brazier.game.Game;
import info.hearthsim.brazier.game.Player;
import info.hearthsim.brazier.game.PlayerId;
import org.jtrim.event.CopyOnTriggerListenerManager;
import org.jtrim.event.EventListeners;
import org.jtrim.event.ListenerManager;
import org.jtrim.event.ListenerRef;
import org.jtrim.utils.ExceptionHelper;

public final class GamePlayUiAgent {
    private GameAgent playAgent;
    private final TargetManager targetManager;
    private final ListenerManager<Runnable> refreshGameActions;

    public GamePlayUiAgent(Game game, PlayerId startingPlayer, TargetManager targetManager) {
        ExceptionHelper.checkNotNullArgument(targetManager, "targetManager");

        this.playAgent = new GameAgent(game, startingPlayer);
        this.targetManager = targetManager;
        this.refreshGameActions = new CopyOnTriggerListenerManager<>();
    }

    public void resetGame(Game game) {
        resetGame(game, game.getPlayer1().getPlayerId());
    }

    public void resetGame(Game game, PlayerId startingPlayer) {
        playAgent = new GameAgent(game, startingPlayer);
        refreshGame();
    }

    public void alterGame(GameAction action) {
        action.apply(playAgent.getGame());
        refreshGame();
    }

    public ListenerRef addRefreshGameAction(Runnable action) {
        return refreshGameActions.registerListener(action);
    }

    public Game getGame() {
        return playAgent.getGame();
    }

    public TargetManager getTargetManager() {
        return targetManager;
    }

    public Player getCurrentPlayer() {
        return playAgent.getCurrentPlayer();
    }

    public PlayerId getCurrentPlayerId() {
        return playAgent.getCurrentPlayerId();
    }

    private void refreshGame() {
        EventListeners.dispatchRunnable(refreshGameActions);
    }

    public void endTurn() {
        playAgent.endTurn();
        refreshGame();
    }

    public void attack(EntityId attacker, EntityId defender) {
        playAgent.attack(attacker, defender);

        // TODO: Update game over state
        refreshGame();
    }

    public void playCard(int cardIndex, PlayTargetRequest playTarget) {
        playAgent.playCard(cardIndex, playTarget);

        // TODO: Update game over state
        refreshGame();
    }

    public void playHeroPower(PlayTargetRequest playTarget) {
        playAgent.playHeroPower(playTarget);

        // TODO: Update game over state
        refreshGame();
    }
}
