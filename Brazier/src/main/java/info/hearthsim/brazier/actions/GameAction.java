package info.hearthsim.brazier.actions;

import info.hearthsim.brazier.Game;
import info.hearthsim.brazier.actions.undo.UndoAction;

/**
 * Actions that can be used to alter a given {@code Game}. Usually used as a functional
 * interface with its sole un-implemented method {@link #alterGame(Game)}.
 */
public interface GameAction extends GameObjectAction<Void> {
    public static final GameAction DO_NOTHING = (game) -> UndoAction.DO_NOTHING;

    /**
     * Alters the game and returns an action which can undo the action
     * done by this method, assuming the game is in the same state as
     * it was right after calling this method.
     *
     * @param game the game to be altered by this action. This argument
     *   cannot be {@code null}.
     * @return the action which can undo the action done by this method call.
     *   This may never return {@code null}.
     */
    public UndoAction alterGame(Game game);

    @Override
    public default UndoAction alterGame(Game game, Void object) {
        return alterGame(game);
    }
}
