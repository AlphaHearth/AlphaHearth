package info.hearthsim.brazier.actions;

import info.hearthsim.brazier.GameProperty;
import org.jtrim.utils.ExceptionHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Actions that can be used to alter a given {@code Game} and a given object. Usually used
 * as a functional interface with its sole un-implemented method {@link #apply(T)}.
 */
public interface GameObjectAction <T extends GameProperty> {
    public static final GameObjectAction DO_NOTHING = (obj) -> {};

    public void apply(T object);

    /**
     * Executes the given collection of {@code GameObjectAction} and returns the corresponding
     * {@code GameObjectAction} which can be used to undo.
     *
     * @param actions the collection of {@code GameObjectAction}
     * @return {@code GameObjectAction} which can undo the given actions.
     */
    public static <T extends GameProperty> GameObjectAction<T>
        merge(Collection<? extends GameObjectAction<T>> actions) {

        List<GameObjectAction<T>> actionsCopy = new ArrayList<>(actions);
        ExceptionHelper.checkNotNullElements(actionsCopy, "actions");

        int count = actionsCopy.size();
        if (count == 0)
            return DO_NOTHING;

        if (count == 1)
            return actionsCopy.get(0);

        return (T self) -> {
            for (GameObjectAction<T> action : actionsCopy) {
                action.apply(self);
            }
        };
    }
}
