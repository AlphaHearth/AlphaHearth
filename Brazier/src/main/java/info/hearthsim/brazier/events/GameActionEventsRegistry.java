package info.hearthsim.brazier.events;

import info.hearthsim.brazier.actions.GameObjectAction;
import info.hearthsim.brazier.Priorities;

import java.util.function.Predicate;

public interface GameActionEventsRegistry <T> {
    /**
     * Adds the given action to the {@code GameActionEventsRegistry} with normal priority.
     * @param action the given action.
     *
     * @see Priorities#NORMAL_PRIORITY
     */
    public default RegisterId addAction(GameObjectAction<T> action) {
        return addAction(Priorities.NORMAL_PRIORITY, action);
    }

    public default RegisterId addAction(
            int priority,
            GameObjectAction<T> action) {
        return addAction(priority, (arg) -> true, action);
    }

    public RegisterId addAction(
            int priority,
            Predicate<? super T> condition,
            GameObjectAction<? super T> action);

    /**
     * Unregisters the action with the given {@code RegisterId} from this {@code GameActionEventsRegistry}.
     */
    public boolean unregister(RegisterId registerId);
}
