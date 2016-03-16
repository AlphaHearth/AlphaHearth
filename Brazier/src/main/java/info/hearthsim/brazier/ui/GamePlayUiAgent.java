package info.hearthsim.brazier.ui;

import info.hearthsim.brazier.*;
import info.hearthsim.brazier.Game;
import info.hearthsim.brazier.actions.PlayTargetRequest;
import info.hearthsim.brazier.actions.undo.UndoAction;
import info.hearthsim.brazier.actions.GameAction;
import org.jtrim.event.CopyOnTriggerListenerManager;
import org.jtrim.event.EventListeners;
import org.jtrim.event.ListenerManager;
import org.jtrim.event.ListenerRef;
import org.jtrim.property.PropertySource;
import org.jtrim.utils.ExceptionHelper;

public final class GamePlayUiAgent {
    private GameAgent playAgent;
    private final TargetManager targetManager;
    private final UndoManager undoManager;
    private final ListenerManager<Runnable> refreshGameActions;

    public GamePlayUiAgent(Game game, PlayerId startingPlayer, TargetManager targetManager) {
        ExceptionHelper.checkNotNullArgument(targetManager, "targetManager");

        this.playAgent = new GameAgent(game, startingPlayer);
        this.targetManager = targetManager;
        this.undoManager = new UndoManager();
        this.refreshGameActions = new CopyOnTriggerListenerManager<>();
    }

    public void resetGame(Game game) {
        resetGame(game, game.getPlayer1().getPlayerId());
    }

    public void resetGame(Game game, PlayerId startingPlayer) {
        GameAgent newAgent = new GameAgent(game, startingPlayer);

        GameAgent prevAgent = playAgent;
        playAgent = newAgent;

        undoManager.addUndo(() -> playAgent = prevAgent);
        refreshGame();
    }

    public void alterGame(GameAction action) {
        undoManager.addUndo(action.alterGame(playAgent.getGame()));
        refreshGame();
    }

    public ListenerRef addRefreshGameAction(Runnable action) {
        return refreshGameActions.registerListener(action);
    }

    public void undoLastAction() {
        undoManager.undo();
        refreshGame();
    }

    public PropertySource<Boolean> hasUndos() {
        return undoManager.hasUndos();
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
        undoManager.addUndo(playAgent.endTurn());
        refreshGame();
    }

    public void attack(TargetId attacker, TargetId defender) {
        UndoAction result = playAgent.attack(attacker, defender);
        undoManager.addUndo(result);

        // TODO: Update game over state
        refreshGame();
    }

    public void playCard(int cardIndex, PlayTargetRequest playTarget) {
        UndoAction result = playAgent.playCard(cardIndex, playTarget);
        undoManager.addUndo(result);

        // TODO: Update game over state
        refreshGame();
    }

    public void playHeroPower(PlayTargetRequest playTarget) {
        UndoAction result = playAgent.playHeroPower(playTarget);
        undoManager.addUndo(result);

        // TODO: Update game over state
        refreshGame();
    }
}
