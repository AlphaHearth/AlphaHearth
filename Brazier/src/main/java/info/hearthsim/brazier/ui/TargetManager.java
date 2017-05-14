package info.hearthsim.brazier.ui;

import org.jtrim.utils.ExceptionHelper;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public final class TargetManager {
    private final JComponent parent;
    private boolean hasLastCursor;
    private Cursor lastCursor;

    private UiTargetCondition targetCondition;

    public TargetManager(JComponent parent) {
        ExceptionHelper.checkNotNullArgument(parent, "parent");

        this.parent = parent;
        this.targetCondition = null;
        this.hasLastCursor = false;
        this.lastCursor = null;
    }

    public void clearRequest() {
        targetCondition = null;
        if (hasLastCursor) {
            parent.setCursor(lastCursor);
            hasLastCursor = false;
            lastCursor = null;
        }
    }

    public void requestTarget(Object condition, Consumer<Object> targetCallback) {
        this.targetCondition = new UiTargetCondition(condition, targetCallback);

        if (!hasLastCursor) {
            this.hasLastCursor = true;
            this.lastCursor = parent.getCursor();
            parent.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
    }

    public Object getConditionObj() {
        return targetCondition != null ? targetCondition.getCondition() : null;
    }

    public UiTargetCondition getCondition() {
        return targetCondition;
    }
}
