package info.hearthsim.brazier.game;

import org.jtrim.utils.ExceptionHelper;

public final class PlayerId extends EntityId {
    private final String name;

    public PlayerId(String name) {
        ExceptionHelper.checkNotNullArgument(name, "name");
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    public int hashCode() {
        return name.hashCode();
    }
}
