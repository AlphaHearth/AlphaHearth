package info.hearthsim.brazier.events;

import info.hearthsim.brazier.PlayerProperty;
import info.hearthsim.brazier.Game;

public interface CompletableGameEventAction <Self extends PlayerProperty, EventSource> {
    public CompleteGameEventAction<Self, EventSource> startEvent(Game game, Self self, EventSource eventSource);
}
