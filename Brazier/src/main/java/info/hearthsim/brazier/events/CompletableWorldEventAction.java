package info.hearthsim.brazier.events;

import info.hearthsim.brazier.PlayerProperty;
import info.hearthsim.brazier.World;

public interface CompletableWorldEventAction<Self extends PlayerProperty, EventSource> {
    public CompleteWorldEventAction<Self, EventSource> startEvent(World world, Self self, EventSource eventSource);
}
