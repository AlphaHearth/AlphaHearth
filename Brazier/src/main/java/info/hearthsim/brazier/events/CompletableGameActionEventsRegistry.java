package info.hearthsim.brazier.events;

import info.hearthsim.brazier.Priorities;

public interface CompletableGameActionEventsRegistry <T> {
    public default RegisterId addAction(CompletableGameObjectAction<? super T> action) {
        return addAction(Priorities.NORMAL_PRIORITY, action);
    }

    public RegisterId addAction(int priority, CompletableGameObjectAction<? super T> action);

    public boolean unregister(RegisterId registerId);
}
