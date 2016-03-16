package info.hearthsim.brazier.events;

import info.hearthsim.brazier.Game;

public interface CompletableGameObjectAction <T> {
    public CompleteGameObjectAction<T> startAlterGame(Game game, T object);
}
