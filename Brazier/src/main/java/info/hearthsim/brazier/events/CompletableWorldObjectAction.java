package info.hearthsim.brazier.events;

import info.hearthsim.brazier.World;

public interface CompletableWorldObjectAction<T> {
    public CompleteWorldObjectAction<T> startAlterWorld(World world, T object);
}
