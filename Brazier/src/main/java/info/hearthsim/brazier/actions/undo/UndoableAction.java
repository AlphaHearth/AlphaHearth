package info.hearthsim.brazier.actions.undo;

// TODO Remove the `undo` framework from the whole project,
// TODO as it brings unnecessary memory cost to the simulator.
/**
 * Action which is undoable. It is usually used as a functional interface, as the sole
 * un-implemented method {@link #doAction()} stands for the concrete actions of an
 * {@code UndoableAction} instance, which is designated by lambda expression.
 * <p>
 * The method {@link #doAction()} returns a {@link UndoAction} which can be used
 * to undo this {@code UndoableAction}.
 *
 * @see UndoAction
 */
public interface UndoableAction {
    public static final UndoableAction DO_NOTHING = () -> UndoAction.DO_NOTHING;

    /**
     * The concrete actions implementation of this {@code UndoableAction}, which can
     * be designated by lambda expression. Returns a {@link UndoAction} which can be
     * used to undo this {@code UndoableAction}.
     *
     * @return a {@link UndoAction} which can be used to undo this {@code UndoableAction}.
     */
    public UndoAction doAction();
}
