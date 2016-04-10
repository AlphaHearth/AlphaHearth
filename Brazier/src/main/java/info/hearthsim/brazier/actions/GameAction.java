package info.hearthsim.brazier.actions;

import info.hearthsim.brazier.game.Game;

/**
 * Actions that can be used to alter a given {@code Game}. Usually used as a functional
 * interface with its sole un-implemented method {@link #apply(Game)}.
 */
public interface GameAction extends GameObjectAction<Game> {
    public static final GameAction DO_NOTHING = (game) -> {};

    /**
     * Alters the game and returns an action which can undo the action
     * done by this method, assuming the game is in the same state as
     * it was right after calling this method.
     *
     * @param game the game to be altered by this action. This argument
     *   cannot be {@code null}.
     */
    public void apply(Game game);
}
